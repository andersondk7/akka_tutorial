package adc.tutorial.scala.akka.step7

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}

class Echo7Supervisor(service: StorageService) extends Actor with ActorLogging {

  import Echo7._
  import Echo7Supervisor._

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case m: UnknownMessageException =>
      val message = Message(s"please don't send messages of type ${m.messageType}")
      // be sure to send the message to self first, so that it can be processed before the client has a chance to query for the count report
      self ! BadMessage
      m.askedBy ! message
      Stop

    case fm: FailedMessageException =>
      val message = Message(s"could not process message because ${fm.reason.getMessage}")
      log.info(message.content, fm)
      self ! BadMessage
      // be sure to send the message to self first, so that it can be processed before the client has a chance to query for the count report
      fm.askedBy ! message
      Stop

    case ex: Exception =>
      log.info(s"restarting because ${ex.getMessage}")
      self ! BadMessage
      Stop
  }

  override def receive: Receive = onMessage(counts = Counts())

  def onMessage(counts: Counts): Receive = {

    case GoodMessage =>
      val updateCount: Counts = counts.copy(good=counts.good+1)
      context.become(onMessage(counts = updateCount))

    case BadMessage =>
      val updateCount: Counts = counts.copy(bad=counts.bad+1)
      context.become(onMessage(counts = updateCount))

    case CountReport =>
      sender() ! counts

    case request: Request =>
      context.actorOf(Echo7.props(sender(), service)) forward request

    case x => // yes this does the same as a request, but here we have an opportunity to short circuit
      context.actorOf(Echo7.props(sender(), service)) forward x
  }
}

object Echo7Supervisor {
  def props(service: StorageService): Props = Props(classOf[Echo7Supervisor], service)
  case object GoodMessage
  case object BadMessage
  case object CountReport
  case class Counts(good: Int = 0, bad: Int = 0)
}
