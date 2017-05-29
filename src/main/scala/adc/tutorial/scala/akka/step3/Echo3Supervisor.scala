package adc.tutorial.scala.akka.step3

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}

class Echo3Supervisor extends Actor with ActorLogging {

  private val echoActor = context.actorOf(Props[Echo3], "worker")

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case ex: Exception =>
      log.info(s"restarting because ${ex.getMessage}")
      Restart
  }


  override def receive: Receive = {
    case x =>  echoActor forward x
  }
}

object Echo3Supervisor {
}