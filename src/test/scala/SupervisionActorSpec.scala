import akka.actor.{Kill, PoisonPill}

import scala.concurrent.duration._
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect._
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, ScalaTestWithActorTestKit, TestInbox}
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import org.slf4j.event.Level
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}

import scala.util.Random


object CompanyActors {

  import akka.actor.typed.ActorRef
  import akka.actor.typed.Behavior
  import akka.actor.typed.DeathPactException
  import akka.actor.typed.SupervisorStrategy
  import akka.actor.typed.scaladsl.Behaviors

  sealed trait Command
  case class Fail(text: String) extends Command
  case class Hello(text: String, replyTo: ActorRef[String]) extends Command

  object Worker {
    def apply(): Behavior[Command] = Behaviors.setup { context =>
      context.log.info("Worker starting up")

      Behaviors.receiveMessage {
        case Fail(text) =>
          context.log.info(s"Worker received the Fail")
          throw new RuntimeException(text)
        case Hello(text, replyTo) =>
          context.log.info(s"Worker received the Hello")
          replyTo ! text
          Behaviors.same
      }
    }
  }

  object MiddleManagement {
    def apply(): Behavior[Command] =
      Behaviors.setup[Command] { context =>
        context.log.info("Middle management starting up")

        val child = context.spawn(Worker(), "child")
        context.watch(child)

        Behaviors.receiveMessage { message =>
          context.log.info(s"MiddleManagement received the message ${message} and resend to worker")
          child ! message
          Behaviors.same
        }
      }
  }

  object Boss {
    def apply(): Behavior[Command] =
      Behaviors
        .supervise(Behaviors.setup[Command] { context =>
          context.log.info("Boss starting up")

          val middleManagement = context.spawn(MiddleManagement(), "middle-management")
          context.watch(middleManagement)

          Behaviors.receiveMessage[Command] { message =>
            context.log.info(s"Boss received the message ${message} and resent to middleManagement")

            middleManagement ! message
            Behaviors.same
          }
        })
        .onFailure[DeathPactException](SupervisorStrategy.restart)
  }
}


class StoppingActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import CompanyActors._

    "send message back" in {
      val boss = spawn(Boss(), "upper-management")
      val replyProbe = createTestProbe[String]()

      boss ! Hello("hi 1", replyProbe.ref)

      replyProbe.expectMessage("hi 1")

      boss ! Fail("ping")

      eventually {
        boss ! Hello("hi 2", replyProbe.ref)
        replyProbe.expectMessage(200.millis, "hi 2")
      }
    }


}
