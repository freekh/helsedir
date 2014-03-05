package helsedir

import java.io.File
import java.text.SimpleDateFormat

object CubeMap {

  def read(file: File): (String, Map[SourceColumn, TargetColumn]) = {
    var lineNo = 0
    var name: Option[String] = None
    var mappings = Map.empty[SourceColumn, TargetColumn]
    io.Source.fromFile(file, Defaults.Encoding).getLines.foreach { line =>
      if (line.trim().startsWith("#")) {
        //skip
      } else if (name.isEmpty) {
        if (line.contains(";")) throw new Exception("Cannot parse cubemap table name with ; in it")
        name = Some(line)
      } else {
        val segments = line.split(";")
        if (segments.size != 5) throw new Exception("Cannot parse: " + line + " (" + lineNo + ") in " + file.getAbsolutePath + ". Not enough columns. Format: source column;readable name;target table name;target column;null options")
        else {
          val sourceColumn = segments(0)
          val readableName = segments(1)
          val targetTableName = segments(2)
          val targetColumn = segments(3)
          val nullOptions = segments(4).trim()
          val includeNull =
            if (nullOptions == "include-nulls") true
            else if (nullOptions == "no-nulls") false
            else throw new Exception("Cannot parse: " + line + " (" + lineNo + ") in " + file.getAbsolutePath + ". Invalid null-options. Valid options: include-nulls, no-nulls. Got: '" + nullOptions + "'")
          mappings += SourceColumn(column = sourceColumn, readableName = readableName) ->
            TargetColumn(table = targetTableName, column = targetColumn, includeNull = includeNull)
        }
      }
      lineNo += 1
    }
    name.getOrElse("Cannot read cubemap file: " + file.getAbsolutePath + " because first line is not a name") -> mappings
  }
}