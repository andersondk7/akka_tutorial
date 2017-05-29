package adc.tutorial.scala.akka.step3

import java.util.concurrent.TimeoutException

import adc.tutorial.scala.akka.step3.Echo3.Message
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class Echo3Spec extends FunSpec with Matchers {
  implicit val timeout = Timeout(.5.seconds)
  private val system = ActorSystem("step3")
  // note: internally a supervisor is used but externally it is simply an actor ref
  private val echo = system.actorOf(Props[Echo3Supervisor], "supervisor")

  describe("echo3") {
    it("should return recognized message") {
      val content = "its in a bottle"
      val call = echo ? Message(content)
      val received = Await.result(call.mapTo[String], timeout.duration)
      received shouldBe content
    }
    it("should not receive and answer for an unknown message") {
      val content = "its in a bottle"
      val call = echo ? content
      Try {
        Await.result(call.mapTo[String], timeout.duration)
      } match {
        case Success(_) => fail("did not expect an answer")
        case Failure(t) => t match {
          case _: TimeoutException => succeed
          case t: Throwable => fail(t)
        }
      }
    }
    it("alternate: should not receive and answer for an unknown message") {
      val content = "its in a bottle"
      val call = echo ? content
      try {
        Await.result(call.mapTo[String], timeout.duration)
        fail("did not expect an answer")
      } catch {
        case _: TimeoutException => succeed
        case t: Throwable => fail(t)
      }
    }
  }

}
