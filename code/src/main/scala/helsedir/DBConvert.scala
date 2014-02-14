package helsedir

import java.io.File
import java.text.SimpleDateFormat
import java.sql.Connection
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

case class Column(name: String, dataType: String)
case class Row(columns: Seq[String]) extends AnyVal

object Parsers {
  val IntRegEx = """\d+""".r
  def isInt(value: String): Boolean = {
    value match {
      case IntRegEx => true
      case _ => false
    }
  }

  val DoubleRegEx = """\d*(,\d+)*""".r
  def isDouble(value: String): Boolean = {
    value match {
      case DoubleRegEx => true
      case _ => false
    }
  }

  val DateFormatRegEx = """\d{4}-\d{2}""".r

  def isDate(value: String): Boolean = {
    value match {
      case DoubleRegEx => true
      case _ => false
    }
  }
}

object Defaults {
  val Encoding = "iso-8859-1"
}

/** Les aktivitets data **/
class ReadActivity(typeMapping: Map[String, String]) {
  val Seperator = "\t"

  def getDataTypes(line: String): Array[String] = {
    val columns = line.split(Seperator)
    columns.map { columnName =>
      typeMapping.get(columnName) match {
        case Some(dataType) => dataType
        case None => throw new Exception("Could not find data type for '" + columnName + "' perhaps you forgot to add in the typemap file?")
      }
    }
  }

  def parseColumns(header: String): Seq[Column] = {
    val dataTypes = getDataTypes(header)
    (header.split(Seperator) zip dataTypes).map(Column.tupled(_)).toSeq
  }

  def parseRow(line: String, columns: Seq[Column]): Row = {
    val index = line.split(Seperator).toSeq
    Row(TypeMap.convertRow(index, columns))
  }

  def read(file: File)(createTableIfNotExists: Seq[Column] => Unit)(insertRow: Row => Unit) = {
    val lines = io.Source.fromFile(file, Defaults.Encoding).getLines
    if (!lines.hasNext) throw new Exception(file + " has no lines")
    val headerLine = lines.next()
    val columns = TypeMap.convertColumns(parseColumns(headerLine))
    createTableIfNotExists(columns)
    lines.foreach { line =>
      insertRow(parseRow(line, columns))
    }
  }
}

object TypeMap {
  val inputFormat = new SimpleDateFormat("yyyy-MM")
  val outputFormat = new SimpleDateFormat("yyyy-MM-dd")

  def quote(value: String) = "'" + value + "'"

  def convertDate(value: String): String = {
    val d = inputFormat.parse(value)
    quote(outputFormat.format(d))
  }

  def convertInteger(value: String) = {
    if (value.trim().isEmpty()) "NULL"
    else value
  }

  def convertCommaDouble(value: String) = {
    val segments = value.split(",")
    assert(segments.size <= 2)
    segments.headOption.filter(_.trim().nonEmpty).getOrElse("0") + "." + segments.last
  }

  def convertVarchar(value: String) = {
    if (value.trim().isEmpty()) "NULL"
    else quote(value)
  }

  val TypeConversions = Map(
    "COMMA_DOUBLE" -> "DOUBLE",
    "DATE" -> "DATE",
    "VARCHAR" -> "VARCHAR",
    "INTEGER" -> "INTEGER")

  val ValueConversions = Map(
    "DOUBLE" -> convertCommaDouble _,
    "DATE" -> convertDate _,
    "VARCHAR" -> convertVarchar _,
    "INTEGER" -> convertInteger _)

  def convertColumns(columns: Seq[Column]): Seq[Column] = {
    columns.map { column =>
      TypeConversions.get(column.dataType) match {
        case Some(newType) => column.copy(dataType = newType)
        case None =>
          column
      }
    }
  }

  def convertRow(row: Seq[String], columns: Seq[Column]): Seq[String] = {
    assert(row.size == columns.size)
    (row zip columns).map {
      case (value, column) =>
        ValueConversions.get(column.dataType) match {
          case Some((func)) =>
            func(value)
          case None =>
            value
        }
    }
  }

  def read(file: File): Map[String, String] = {
    io.Source.fromFile(file, Defaults.Encoding).getLines.map { line =>
      val segments = line.split("=")
      //Types does not have = in them so take all equals up to the last one
      segments.init.mkString("=") -> segments.last //TODO: check type 
    }.toMap
  }
}

class DAO {
  lazy val connection = { //TODO: use connection pool or something more efficient&safe
    import org.h2.jdbcx.JdbcDataSource
    val ds = new JdbcDataSource()
    ds.setURL("jdbc:h2:~/helsedir/foo")
    ds.setUser("sa")
    ds.setPassword("")
    ds.getConnection()
  }

  def createTable(name: String, columns: Seq[Column]) = {
    val statement = connection.createStatement()
    try {
      statement.execute(
        s"""CREATE TABLE IF NOT EXISTS $name (${columns.map(c => c.name + " " + c.dataType).mkString("", ",", ",")} ID INTEGER AUTO_INCREMENT, PRIMARY KEY (ID))""")
    } finally {
      statement.close()
    }
  }

  def dropTable(name: String) = {
    val statement = connection.createStatement()
    try {
      statement.execute(
        s"""DROP TABLE IF EXISTS T$name""")
    } finally {
      statement.close()
    }
  }

  def insert(name: String, row: Row) = {
    val statement = connection.prepareStatement(s"INSERT INTO $name VALUES(${row.columns.mkString(",")}, NULL)")
    try {
      statement.execute()
    } finally {
      statement.close()
    }
  }
}

object DBConvert {

  def recreateActivityTable(file: File, readActivity: ReadActivity) = {
    val dao = new DAO
    val tableName = "file_" + file.getName.replace(".", "_")
    try {
      var i = 1
      def printProgress() = {
        val lastMessage = (i - 1).toString + " completed..."
        print(("\b" * lastMessage.size)) //erease last
        i = i + 1
        print((i - 1).toString + " completed...")
      }
      readActivity.read(file) { columns =>
        dao.dropTable(tableName)
        dao.createTable(tableName, columns)
      } { row =>
        printProgress()
        dao.insert(tableName, row)
        if (i % 100 == 0) dao.connection.commit()
      }
    } finally {
      dao.connection.close()
    }
  }

  def recreateReferenceTable(file: File) = {
    ???
    import collection.JavaConverters._
    val dao = new DAO

    val fis = new FileInputStream(file)
    try {
      val workbook = new XSSFWorkbook(fis)
      val sheetIterator = workbook.iterator.asScala
      sheetIterator.foreach { sheet =>
        val tableName = sheet.getSheetName
        
        val rowIterator = sheet.rowIterator().asScala
        
      }

    } finally {
      fis.close()
    }
  }

  def main(args: Array[String]) = {
    val typeMapFile = new File("../typemap.properties")

    val typeMapping = TypeMap.read(typeMapFile)

    recreateActivityTable(new File("../data/2013_12_aktivitet_HF_NASJ.txt"), new ReadActivity(typeMapping))

    //recreateReferenceTable(new File("../data/Referansetabell_2013 20 juni.xlsx"))

  }
}