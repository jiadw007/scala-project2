import akka.actor.{Props, ActorRef, Actor}
import scala.collection._
import scala.collection.mutable._



object boss {

  case class start(numNodes: Int, topo: topology, algorithm: String)
  case class fail(failCode: Int, failNodes: mutable.HashMap[Int, Int])
  var workers: Array[ActorRef] = _
  var endMark: Array[Boolean] = _
  var topo: topology = _
  var numNodes: Int = 0
  var startTime: Long = 0
  var deadCount = 0
  var estimation: Double = 0.0
  var printmap = false
}

class boss extends Actor {

  var algorithm: String = _


  def receive() = {

    case boss.start(numNodes: Int, topo: topology, algorithm: String) => {
      this.algorithm = algorithm
      boss.numNodes = numNodes

      //Init actors
      println("Boss started")
      boss.workers = new Array[ActorRef](numNodes)
      boss.endMark = (1 to numNodes).map(_ => false).toArray

      //Apply topology on actors
      boss.topo = topo
      val topoMap = boss.topo.BuildTopo(numNodes - 1)
      println("Topology done")


      //Generate actors
      if(algorithm == "gossip") {
        for(i <- 0 to numNodes - 1) {
          boss.workers(i) = context.system.actorOf(Props(new gossipWorker(i, topoMap(i))), s"WorkerInstance$i")
        }
      }
      else{
        for(i <- 0 to numNodes - 1) {
          boss.workers(i) = context.system.actorOf(Props(new pushWorker(i, topoMap(i))), s"WorkerInstance$i")
        }
      }
      println("Game started")
      boss.startTime = System.currentTimeMillis()
      //Trigger a worker
      if(algorithm == "gossip") {
        //-1 is the code for boss
        boss.workers((numNodes + 1) / 2) ! gossipWorker.fact(-1)
      } else {
        boss.workers((numNodes + 1) / 2) ! worker.start
      }
    }


    case worker.dead(index: Int) => {
      boss.deadCount += 1
      //Did everyone stop? => not reliable, set a threshold there
      if(this.algorithm != "gossip") {
        boss.workers.foreach(p => context.stop(p))
        context.system.shutdown()
      }
    }

    case worker.done(index: Int) => {
      if(!boss.endMark(index))
      {
        boss.endMark(index) = true
        if(boss.printmap) {
          boss.endMark.foreach(p => print(if(p) 1 else 0))
          println
        }
        val numEnds = boss.endMark.count(p => p)
        if(numEnds == boss.numNodes){
          boss.workers.foreach(p => context.stop(p))
          context.system.shutdown()
        }
      }
    }

    case worker.report(sum: Double) => {
      boss.estimation = sum
    }
  }

}
