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

    args.headOption match {
      case Some(importType) if importType == "activity" =>
        args.take(2).drop(1).headOption.map(new File(_)) match {
          case Some(activityFile) =>
            val typeMapFilename = args.take(3).drop(2).headOption.getOrElse("../typemap.properties")
            val typeMapFile = new File(typeMapFilename)

            if (!typeMapFile.isFile) {
              System.err.println("Typemap: " + typeMapFile.getAbsolutePath + " is not a file")
            } else if (!activityFile.isFile) {
              System.err.println("Activity data: " + activityFile.getAbsolutePath + " is not a file")
            } else if (typeMapFile.isFile && activityFile.isFile) {
              val typeMapping = TypeMap.read(typeMapFile)
              val activity = new ActivityData(activityFile, typeMapping)
              TableCreator.createTable(activity, dao, dropBeforeInsert = dropBeforeInsert)
            } else {
              throw new Exception("Unknown state!")
            }
          case None =>
            System.err.println("Please specify activity data filename")
        }
      case Some(importType) if importType == "ref" =>
        args.take(2).drop(1).headOption.map(new File(_)) match {
          case Some(refTableFile) =>
            if (refTableFile.isFile) {
              val refTable = new ReferenceTable(refTableFile)
              TableCreator.createTable(refTable, dao, dropBeforeInsert = dropBeforeInsert)
            } else {
              System.err.println("Ref table: " + refTableFile.getAbsolutePath + " is not a file")

            }
          case None =>
            System.err.println("Please specify ref table filename")
        }
      case Some(importType) => {
        System.err.println(s"Unknown import type '$importType'. Valid types: activity, ref")
      }
      case None =>
        System.err.println("Import-type required! Valid types: activity, ref")
    }
  }
}