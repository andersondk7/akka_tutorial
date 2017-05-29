package adc.tutorial.scala.akka.step1

import akka.actor.{ActorSystem, Inbox, Props}
import scala.concurrent.duration._

object Driver1 extends App {
  import Echo1._

  val duration = 5.seconds
  val messageCount = 10
  val system = ActorSystem("step1")
  val echo = system.actorOf(Props[Echo1])
  val inbox = Inbox.create(system)

  (1 to 10).foreach(i => echo ! Message(s"message #$i") )
  Thread.sleep(duration.toMillis)
  system.log.info(s"ending")
  System.exit(0)
}
