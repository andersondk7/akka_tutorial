package adc.tutorial.scala.akka.step5


import adc.tutorial.scala.akka.step5.Echo5.Message
import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}

class Echo5Supervisor extends Actor with ActorLogging {

  private val echoActor = context.actorOf(Props[Echo5], "worker")

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case m: UnknownMessageException =>
      val message = Message(s"please don't send messages of type ${m.messageType}")
      log.info(s"resuming due to unknown message of type ${m.messageType}")
      m.sender ! message
      Resume

    case ex: Exception =>
      log.info(s"restarting because ${ex.getMessage}")
      Restart
  }


  override def receive: Receive = {
    case x =>  echoActor forward x
  }
}

object Echo5Supervisor {
}
