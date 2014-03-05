package helsedir

/*
import helsedir._
val (tableName, cubeMap) = CubeMap.read(new java.io.File("../aktivitet_hf.cubemap"))
val cube = new Cube(tableName, cubeMap, new DAO)
cube.join(Set("pasientregion", "drg"))
 */


class Cube(table: String, cubeMap: Map[SourceColumn, TargetColumn], dao: DAO) {
  lazy val nameMap = cubeMap.groupBy{ case (source, _) => source.readableName }
  //join("pasientregion", "drg")
  def join(names: Set[String]) = {
    val columns = names.map{ name =>
      val columns = nameMap.getOrElse(name, throw new Exception("Cannot join: " + name + " because there is no mapping to actual columns: " + cubeMap))
      assert(columns.size == 1, "Could not find exactly one column mapping for: " + name + ". Found: " + columns)
      val (source, target) = columns.head
      source -> target
    }
    dao.join(table, columns)
  }
}