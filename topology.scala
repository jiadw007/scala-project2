import akka.actor.ActorRef
import scala.collection.JavaConversions._
import scala.util.Random


object topology {

  def createTopo(topoStr: String) : topology = {
    new topology(topoStr)
  }
}

class topology private (val TopoName: String) {

  //This function randomly destroy ONE connection between actors
  def DestroyConnection(worker: Array[ActorRef]) : String = {
    //TODO: Make a list of save destroyed connections and find any chance to restore it
    "TODO"
  }

  def BuildTopo(size: Int) : Map[Int, Array[Int]] = this.TopoName match {
    case "full" => {
      (0 to size).map(i => (i, (0 to size).filter(j => j != i).toArray)).toMap
    }
    case "2D" => {
      val row = math.sqrt(size+1).toInt + 1
      (0 to size).map(i => (i, {
        val x = i % row
        val y = i / row
        //if located on up/down/left/right, true, otherwise false
        (0 to size).filter(j => {
          val x0 = j % row
          val y0 = j / row
          (x0 == x && y0 == y - 1) || (x0 == x && y0 == y + 1) || (x0 == x - 1 && y0 == y) || (x0 == x + 1 && y0 == y)
        }).toArray
      })
      ).toMap
    }
    case "line" => {
      (0 to size).map(i => (i,
        (0 to size).filter(j => (j == i - 1) || (j == i + 1)).toArray
      )).toMap
    }
    case "imp2D" => {
      val row = math.sqrt(size + 1).toInt + 1
      (0 to size).map(i => (i, {
        val x = i % row
        val y = i / row
        //if located on up/down/left/right, true, otherwise false
        ((0 to size).filter(j => {
          val x0 = j % row
          val y0 = j / row
          (x0 == x && y0 == y - 1) || (x0 == x && y0 == y + 1) || (x0 == x - 1 && y0 == y) || (x0 == x + 1 && y0 == y)
        //:+ random node
        }).toList :+ Random.shuffle((0 to size).filter(j => j != i)).get(0)).toArray
      })).toMap
    }
  }
}