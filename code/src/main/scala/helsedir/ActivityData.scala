package helsedir

import java.io.File

/** Aktivitets data **/
class ActivityData(file: File, typeMapping: Map[String, String]) extends TableCreator {
  override val id = file.getName
  
  val tableName = "file_" + file.getName.replace(".", "_") 
  
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

  def slurp(createTableIfNotExists: (String, Seq[Column]) => Unit)(insertRow: (String, Row) => Unit) = {
    val lines = io.Source.fromFile(file, Defaults.Encoding).getLines
    if (!lines.hasNext) throw new Exception(file + " has no lines")
    val headerLine = lines.next()
    val columns = TypeMap.convertColumns(parseColumns(headerLine))
    createTableIfNotExists(tableName, columns)
    lines.foreach { line =>
      insertRow(tableName, parseRow(line, columns))
    }
  }
}

