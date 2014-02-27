package helsedir

import java.text.SimpleDateFormat

case class Column(name: String, dataType: String)
case class Row(columns: Seq[String]) extends AnyVal

object DAO {
  val StringType = "VARCHAR"
  val DateType = "DATE"
  val IntegerType = "INTEGER"
  val DoubleType = "DOUBLE"
  val BooleanType = "BOOLEAN"

  val Null = "NULL"

  val ColumnQuote = "\""
  val TableNameQuote = "\""
    
  val DateFormat = new SimpleDateFormat("yyyy-MM-dd")
}

class DAO {
  import DAO._

  lazy val connection = { //TODO: use connection pool or something more efficient&safe
    import org.h2.jdbcx.JdbcDataSource
    val ds = new JdbcDataSource()
    ds.setURL("jdbc:h2:~/Projects/helsedir/dbfiles")
    ds.setUser("sa")
    ds.setPassword("")
    ds.getConnection()
  }

  def createTable(name: String, columns: Seq[Column]) = {
    val statement = connection.createStatement()
    try {
      statement.execute(
        s"""CREATE TABLE IF NOT EXISTS "$name" (${columns.map(c => ColumnQuote + c.name + ColumnQuote + " " + c.dataType).mkString("", ",", ",")} ID INTEGER AUTO_INCREMENT, PRIMARY KEY (ID))""")
    } finally {
      statement.close()
    }
  }

  def dropTable(name: String) = {
    val statement = connection.createStatement()
    try {
      statement.execute(
        s"""DROP TABLE IF EXISTS $TableNameQuote$name$TableNameQuote""")
    } finally {
      statement.close()
    }
  }

  def insert(name: String, row: Row) = {
    val statement = connection.prepareStatement(s"""INSERT INTO $TableNameQuote$name$TableNameQuote VALUES(${row.columns.mkString(",")}, NULL)""")
    try {
      statement.execute()
    } finally {
      statement.close()
    }
  }
}

