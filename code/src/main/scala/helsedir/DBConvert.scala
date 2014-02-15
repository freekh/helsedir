package helsedir

import java.io.File
import java.text.SimpleDateFormat
import java.sql.Connection
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.DateUtil

object DBConvert {
  def main(args: Array[String]) = {
    val typeMapFile = new File("../typemap.properties")

    val typeMapping = TypeMap.read(typeMapFile)

    //    recreateActivityTable(new File("../data/2013_12_aktivitet_HF_NASJ.txt"), new ReadActivity(typeMapping))
    //    val activityFile = new File("../data/2013_12_aktivitet_HF_NASJ.txt")
    //    val tableName = "file_" + activityFile.getName.replace(".", "_")

    val refTable = new ReferenceTable(new File("../data/Referansetabell_2013 20 juni.xlsx"))
    refTable.slurp { columns =>
      println(columns)
    } { row =>
      println(row)
    }

  }
}