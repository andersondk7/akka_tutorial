package adc.tutorial.scala.akka.step6a


import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}

class Echo6aSupervisor(service: StorageService) extends Actor with ActorLogging {

  import Echo6a._
  import Echo6aSupervisor._

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case m: UnknownMessageException =>
      val message = Message(s"please don't send messages of type ${m.messageType}")
      m.askedBy ! message
      self ! BadMessage
      Stop

    case fm: FailedMessageException =>
      val message = Message(s"could not process message because ${fm.reason.getMessage}")
      log.info(message.content, fm)
      fm.askedBy ! message
      self ! BadMessage
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

    case CountReport => sender() ! counts

    case x =>
      context.actorOf(Echo6a.props(sender(), service)) forward x

  }
}

object Echo6aSupervisor {
  def props(service: StorageService): Props = Props(classOf[Echo6aSupervisor], service)
  case object GoodMessage
  case object BadMessage
  case object CountReport
  case class Counts(good: Int = 0, bad: Int = 0)
}
