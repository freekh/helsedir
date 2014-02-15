package helsedir

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
