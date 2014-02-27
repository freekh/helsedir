package helsedir

import java.io.File
import java.text.SimpleDateFormat

//Move to separate package with ActivityData
object TypeMap {
  import DAO._
  
  val inputFormat = new SimpleDateFormat("yyyy-MM")
  val outputFormat = DateFormat


  def convertDate(value: String): String = {
    val d = inputFormat.parse(value)
    Utils.quote(outputFormat.format(d))
  }

  def convertInteger(value: String) = {
    if (value.trim().isEmpty()) Null
    else value
  }

  def convertCommaDouble(value: String) = {
    val segments = value.split(",")
    assert(segments.size <= 2)
    segments.headOption.filter(_.trim().nonEmpty).getOrElse("0") + "." + segments.last
  }

  def convertVarchar(value: String) = {
    if (value.trim().isEmpty()) Null
    else Utils.quote(value)
  }

  val TypeConversions = Map(
    "COMMA_DOUBLE" -> DoubleType,
    "DATE" -> DateType,
    "VARCHAR" -> StringType,
    "INTEGER" -> IntegerType)

  val ValueConversions = Map(
    DoubleType-> convertCommaDouble _,
    DateType -> convertDate _,
    StringType -> convertVarchar _,
    IntegerType -> convertInteger _)

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