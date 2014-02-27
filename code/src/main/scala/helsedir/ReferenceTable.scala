package helsedir

import java.io.File
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DateUtil
import java.io.FileInputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet

/** Excel referanse tabell **/
object ReferenceTable {
	import DAO._

  def determineColumnTypes(sheetName: String, rowIterator: Iterator[org.apache.poi.ss.usermodel.Row]) = {
    import collection.JavaConverters._
    if (!rowIterator.hasNext) throw new Exception("Could not find any columns in " + sheetName)
    val columns = rowIterator.next().cellIterator().asScala.toSeq //TODO: check empty
    val columnNames = columns.map(_.getStringCellValue()).toList

    var columnMap = columns.map(_.getColumnIndex() -> "").toMap

    rowIterator.foreach { row =>
      row.cellIterator().asScala.foreach { cell =>
        val columnIndex = cell.getColumnIndex()

        if (columnIndex < columns.size) {
          if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell) && (columnMap(columnIndex) == "" || columnMap(columnIndex) == DateType)) {
            columnMap += columnIndex -> DateType
          } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && (columnMap(columnIndex) == "" || columnMap(columnIndex) == DoubleType || columnMap(columnIndex) == IntegerType)) {
            //here if we can convert to int then to double and it is the same value (and all other are also Integers) then it is an integer
            if (cell.getNumericCellValue().toInt.toDouble == cell.getNumericCellValue() && (columnMap(columnIndex) == "" || columnMap(columnIndex) == IntegerType)) {
              columnMap += columnIndex -> IntegerType
            } else {
              columnMap += columnIndex -> DoubleType
            }
          } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN && (columnMap(columnIndex) == "" || columnMap(columnIndex) == BooleanType)) {
            columnMap += columnIndex -> BooleanType
          } else if (columnMap(columnIndex) == StringType || cell.getCellType() == Cell.CELL_TYPE_STRING) {
            columnMap += columnIndex -> StringType
          } else {
            if (cell.getCellType() != Cell.CELL_TYPE_BLANK) throw new Exception("Sheet: " + cell.getSheet().getSheetName + " row: " + cell.getRowIndex() + " col: " + cell.getColumnIndex() + " has an unknown type: " + cell.getCellType() + " content: " + cell + " column map: " + columnMap)
          }
        } else {
          if (cell.getCellType() != Cell.CELL_TYPE_BLANK) //do not warn if empty
            println("WARNING: Cell does not have a header (not part of table), so skipping location: sheet:" + cell.getSheet().getSheetName + " row: " + cell.getRowIndex() + " col: " + cell.getColumnIndex() + " type: " + cell.getCellType() + " content: '" + cell + "' column map: " + columnMap + " column names: " + columnNames)
        }
      }
    }

    if (columnMap.size != columnNames.size) throw new AssertionError(columnMap + " does not have the same size as the names: " + columnNames)
    (columnNames zip columnMap.toSeq.sortBy(_._1)).map {
      case (name, (_, guessedDataType)) =>
        val dataType = if (guessedDataType.isEmpty()) "VARCHAR" else guessedDataType //default is VARCHAR
        Column(name, dataType)
    }.toList -> columnMap
  }

 
  def sheetToTableName(sheet: Sheet): String = {
    sheet.getSheetName
  }

  def convertCell2DBValue(cell: Cell, columnMap: Map[Int, String]): String = {
    import Utils.quote
    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
      quote(DateFormat.format(cell.getDateCellValue()))
    } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
      val numeric = if (cell.getNumericCellValue().toInt.toDouble == cell.getNumericCellValue()) {
        cell.getNumericCellValue().toInt.toString
      } else cell.getNumericCellValue().toString
      if (columnMap(cell.getColumnIndex) == StringType) quote(numeric) //if there is a numeric value in a column which is no longer numeric (because there is one or more strings for example), we must qoute
      else numeric
    } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
      val boolValue = cell.getBooleanCellValue().toString
      if (columnMap(cell.getColumnIndex) == StringType) quote(boolValue) //if there is a boolean value in a column which is not boolean (because there is one or more strings for example), we must qoute
      else boolValue
    } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
      quote(cell.getStringCellValue())
    } else {
      if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
        Null
      } else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
        println("ERROR: Found error cell in sheet: " + cell.getSheet().getSheetName + " row: " + cell.getRowIndex() + " col: " + cell.getColumnIndex() + " type: " + cell.getCellType() + " content: '" + cell)
        Null //skip 
      } else throw new Exception("Sheet: " + cell.getSheet().getSheetName + " row: " + cell.getRowIndex() + " col: " + cell.getColumnIndex() + " has an unknown type: " + cell.getCellType() + " content: '" + cell)
    }
  }
}

class ReferenceTable(file: File) extends TableCreator {
  import DAO._
  override val id = file.getName

  
  import ReferenceTable._
  def slurp(createTableIfNotExists: (String, Seq[Column]) => Unit)(insertRow: (String, Row) => Unit) = {
    import collection.JavaConverters._

    val fis = new FileInputStream(file)
    try {
      val workbook = new XSSFWorkbook(fis)
      val sheetIterator = workbook.iterator.asScala
      sheetIterator.foreach { sheet =>
        val tableName = sheetToTableName(sheet)
        val result = try {
          val (columns, columnMap) = determineColumnTypes(sheet.getSheetName, sheet.rowIterator.asScala) //TODO: here we go through all rows 2 times and we have lots of non-DRY code :(
          createTableIfNotExists(tableName, columns)
          Right(columns -> columnMap)
        } catch {
          case e: Exception =>
            Left(("Could not create table" + e.getMessage) -> e)
        }
        result match {
          case Right((columns, columnMap)) =>

            val rowIterator = sheet.rowIterator().asScala
            if (rowIterator.hasNext) rowIterator.next() //skip header
            rowIterator.foreach { row =>
              val rawRowData = row.cellIterator().asScala.map { cell =>
                cell.getColumnIndex() -> convertCell2DBValue(cell, columnMap)
              }.toMap
              val rowData = Row(columns.zipWithIndex.map {
                case (_, index) =>
                  rawRowData.getOrElse(index, Null)
              }.toList)
              insertRow(tableName, rowData)
            }
          case Left((msg, e)) => 
            println("ERROR: skipping " +tableName + " because: " + msg)
        }
      }
    } finally {
      fis.close()
    }
  }

}