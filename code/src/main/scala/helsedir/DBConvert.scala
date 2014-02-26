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
    val dropBeforeInsert = true
    val dao = new DAO
    
    val typeMapFile = new File("../typemap.properties")

    val typeMapping = TypeMap.read(typeMapFile)

    //val activityFile = new File("../data/2013_12_aktivitet_HF_NASJ.txt")
    val activityFile = new File("../data/2011_12_aktivitet_HF_NASJ.txt")
    val activity = new ActivityData(activityFile, typeMapping)
    TableCreator.createTable(activity, dao, dropBeforeInsert = dropBeforeInsert)

    val refTable = new ReferenceTable(new File("../data/Referansetabell_2013 20 juni.xlsx"))

//    TableCreator.createTable(refTable, dao, dropBeforeInsert = dropBeforeInsert)
    
  }
}