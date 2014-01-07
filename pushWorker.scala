import akka.actor.{ActorRef, Actor}
import scala.collection.mutable.ListBuffer


object pushWorker {
  case class sendPair(s: Double, w: Double)

}

class pushWorker(Index: Int, TopoMap: Array[Int]) extends worker(Index, TopoMap) {

  var s = this.Index.toDouble
  var w = 1.0
  var last = s / w
  var count = 0
  var stopped = false
  val endCount = 3

  var NeighborDone = ListBuffer[Int]()

  def receive = {

    case worker.start => {
      if(!this.stopped)
        this.Send()
    }

    case pushWorker.sendPair(s_in: Double, w_in: Double) => {
      if(stopped) {
        sender ! worker.done(this.Index)
      } else {
        this.s += s_in
        this.w += w_in

        if (math.abs(this.s / this.w - this.last) < 1E-10) {
          this.count += 1
        } else {
          this.count = 0
        }
        this.last = this.s / this.w
        if (this.count == endCount) {
          val boss = context.actorSelection("/user/BossInstance")
          boss ! worker.report(this.s / this.w)
          boss ! worker.done(this.Index)
          this.stopped = true
          sender ! worker.done(this.Index)
        } else {

        }
        self ! worker.start
      }
    }

    //If receive message from an worker, remove this worker from its neighbor dictionary AND RESEND TO ANOTHER NEIGHBOR
    case worker.done(index: Int) => {
      //The actor become useless if all its neighbors are stopped!
      this.Neighbors -= index
      if(this.Neighbors.isEmpty) {
        //If a neighbor get isolated, message ping will die here.
        context.actorSelection("/user/BossInstance") ! worker.dead(this.Index)
        this.stopped = true
      } else {
        self ! worker.start
      }

    }
    case _ => {
      println(s"[${this.Index}] Unknown message")
    }
  }

  def Send() {

    val target = GetRandomNeighbor()
    //Holds last
    this.s = this.s / 2.0
    this.w = this.w / 2.0
    context.actorSelection(s"/user/WorkerInstance$target") ! pushWorker.sendPair(this.s, this.w)
  }


}
