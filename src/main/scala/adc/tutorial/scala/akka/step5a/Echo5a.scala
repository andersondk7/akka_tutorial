package adc.tutorial.scala.akka.step5a

import akka.actor.{Actor, ActorLogging}

class Echo5a extends Actor with ActorLogging {
  import Echo5a._

  override def receive: Receive = onMessage(counts = Counts())

  def onMessage(counts: Counts): Receive = {
    case Message(content) =>
      val updateCount: Counts = counts.copy(good=counts.good+1)
      sender() ! Message(content)
      context.become(onMessage(counts = updateCount))
    case CountReport =>
      sender() ! counts
    case unknownMessage =>
      val updateCount: Counts = counts.copy(bad=counts.bad+1)
      context.become(onMessage(counts = updateCount))
      throw UnknownMessageException(sender(), unknownMessage.getClass.getName)
  }
}

object Echo5a {
  case class Message(content: String)
  case object CountReport
  case class Counts(good: Int = 0, bad: Int = 0)
}
