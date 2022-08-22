package thread.problems

import akka.NotUsed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed._

object Solution1  extends App {

  object FunctionalActor {
    private def doSomeLogicWork() = println("some logic work in progress")

    def apply(): Behavior[String] =
      Behaviors.setup { ctx =>
        Behaviors.receiveMessage {
          case msg =>
            ctx.log.info(msg)
            doSomeLogicWork()
            Behaviors.same
        }
      }
  }


  class ObjectActor(ctx: ActorContext[String]) extends AbstractBehavior[String](ctx) {
    override def onMessage(msg: String): Behavior[String] = {
      ctx.log.info(msg)
      // do some work
      this
    }
  }

  object ObjectActor{
    def apply(): Behavior[String] = Behaviors.setup{ ctx =>
      new ObjectActor(ctx)
    }
  }


  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val functionalActorRef: ActorRef[String] = ctx.spawn(FunctionalActor(), "functional_actor")

      FunctionalActor.apply()

      functionalActorRef ! "Hello I'm initiated functional actor"
      functionalActorRef.tell("Another message")



      val objectActorRef = ctx.spawn(ObjectActor(), "object_actor")

      objectActorRef ! "Hello I'm initiated object actor"

      // f(message) = behavior

      Behaviors.same
    }


    val value = Solution1()
    implicit val system: ActorSystem[NotUsed] = ActorSystem(value, "akka_typed")

    system.terminate()
}
