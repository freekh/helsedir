package helsedir


case class Column(name: String, dataType: String)
case class Row(columns: Seq[String]) extends AnyVal


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

