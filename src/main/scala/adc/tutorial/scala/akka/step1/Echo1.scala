package adc.tutorial.scala.akka.step1

import akka.actor.{Actor, ActorLogging}

class Echo1 extends Actor with ActorLogging {
  import Echo1._

  override def receive: Receive = {
    case Message(content) => log.info(s"received $content")
    case x => log.info(s"unknown message ${x.getClass.getName}")
  }
}

object Echo1 {
  case class Message(content: String)
}
