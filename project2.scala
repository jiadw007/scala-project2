
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import scala.collection.mutable

object project2 {

  var failureControl : mutable.HashMap[Int, Int] = new mutable.HashMap[Int, Int]()

  def main(args: Array[String])
  {
    //Parse arguments
    val numNodes = args(0).toInt
    val strTopo = args(1)
    val strAlg  = args(2)

    //Print control
    if(args.length > 3) {
      boss.printmap = true
    }

    //Run
    val actorSystem = ActorSystem("Gossip", ConfigFactory.load())
    val bossInstance = actorSystem.actorOf(Props[boss], "BossInstance")
    val topo = topology.createTopo(strTopo)
    bossInstance ! boss.start(numNodes, topo, strAlg)
    actorSystem.awaitTermination
    print("DURATION=")
    println(System.currentTimeMillis() - boss.startTime)
    println(s"Dead nodes = ${boss.deadCount}")
    if(strAlg == "gossip") {

    } else {
      println(s"Average = ${boss.estimation}")
    }
    sys.exit(0)
  }
}
