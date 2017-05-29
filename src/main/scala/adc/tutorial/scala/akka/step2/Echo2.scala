package adc.tutorial.scala.akka.step2

import akka.actor.{Actor, ActorLogging}

class Echo2 extends Actor with ActorLogging {
  import Echo2._

  override def unhandled(message: Any): Unit = log.info(s"unknown message ${message.getClass.getName}")

  override def receive: Receive = {
    case Message(content) => sender() ! content
  }
}

object Echo2 {
  case class Message(content: String)
}
