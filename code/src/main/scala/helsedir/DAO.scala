package helsedir

import java.text.SimpleDateFormat

case class Column(name: String, dataType: String)
case class Row(columns: Seq[String]) extends AnyVal

case class SourceColumn(column: String, readableName: String)
case class TargetColumn(table: String, column: String, includeNull: Boolean = true)

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

  def quoteColumn(name: String) = ColumnQuote + name + ColumnQuote
  
  def createTable(name: String, columns: Seq[Column]) = {
    val statement = connection.createStatement()
    try {
      statement.execute(
        s"""CREATE TABLE IF NOT EXISTS "$name" (${columns.map(c =>  quoteColumn(c.name) + " " + c.dataType).mkString("", ",", ",")} ID INTEGER AUTO_INCREMENT, PRIMARY KEY (ID))""")
    } finally {
      statement.close()
    }
  }

  def dropTable(name: String) = {
    val statement = connection.createStatement()
    try {
      statement.execute(
        s"""DROP TABLE IF EXISTS ${quoteTableName(name)}""")
    } finally {
      statement.close()
    }
  }
  
  def quoteTableName(name: String) = s"""$TableNameQuote$name$TableNameQuote"""

  def insert(name: String, row: Row) = {
    val statement = connection.prepareStatement(s"""INSERT INTO ${quoteTableName(name)} VALUES(${row.columns.map(quoteColumn).mkString(",")}, NULL)""")
    try {
      statement.execute()
    } finally {
      statement.close()
    }
  }

  def join(table: String, columns: Set[(SourceColumn, TargetColumn)]) = {
    val sourceTables = columns.map{ case (source, target) =>
      quoteTableName(target.table)
    }
    val whereClauses = columns.map{ case (source, target) =>
      (if (target.includeNull) (quoteColumn(source.column) + " IS "+Null+" OR ") else "") + 
        s"""${quoteTableName(table)}.${quoteColumn(source.column)} = ${quoteTableName(target.table)}.${quoteColumn(target.column)}""" 
    }
    
    val query = s"""
      SELECT * FROM ${quoteTableName(table)}, ${sourceTables.mkString(",")}
        WHERE ${whereClauses.mkString("AND")}
        """
    println(query)
    val statement = connection.prepareStatement(query)
    try {
      statement.executeQuery()
    } finally {
      statement.close()
    }

  }
}

