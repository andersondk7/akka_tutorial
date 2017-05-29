package adc.tutorial.scala.akka.step6

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}

class Echo6Supervisor(service: StorageService) extends Actor with ActorLogging {

  import Echo6._
  import Echo6Supervisor._

  private var goodMessages: Int = 0
  private var badMessages: Int = 0

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case m: UnknownMessageException =>
      val message = Message(s"please don't send messages of type ${m.messageType}")
      m.askedBy ! message
      badMessages = badMessages + 1
      Stop

    case fm: FailedMessageException =>
      val message = Message(s"could not process message because ${fm.reason.getMessage}")
      log.info(message.content, fm)
      fm.sender ! message
      badMessages = badMessages + 1
      Stop

    case ex: Exception =>
      log.info(s"restarting because ${ex.getMessage}")
      badMessages = badMessages + 1
      Stop
  }

  override def receive: Receive = {
    case GoodMessage => goodMessages = goodMessages + 1

    case CountReport => sender() ! Counts(goodMessages, badMessages)

    case x =>
      context.actorOf(Echo6.props(sender(), service)) forward x

  }
}

object Echo6Supervisor {
  def props(service: StorageService): Props = Props(classOf[Echo6Supervisor], service)
  case object GoodMessage
  case object CountReport
  case class Counts(good: Int, bad: Int)
}
