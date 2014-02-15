package helsedir

trait TableCreator {
  def slurp(createTableIfNotExists: Seq[Column] => Unit)(insertRow: Row => Unit)
}

object TableCreator {
  def createTable(tableName: String, tableCreator: TableCreator, dao: DAO, dropBeforeInsert: Boolean = false) = {
    try {
      var i = 1
      def printProgress() = {
        val lastMessage = (i - 1).toString + " completed..."
        print(("\b" * lastMessage.size)) //erease last
        i = i + 1
        print((i - 1).toString + " completed...")
      }
      tableCreator.slurp { columns =>
        if (dropBeforeInsert) dao.dropTable(tableName)
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

}