package thread.problems

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import thread.problems.Solution1.{FunctionalActor, ObjectActor}

object State extends App {
  sealed trait Command
  case class Add(v: Int) extends Command
  case class Get(actorRef: ActorRef[Int]) extends Command


  object StateActor {
    def apply(init: Int): Behavior[Command] = inc(init)

    def inc(acc: Int): Behavior[Command] = Behaviors.setup{ ctx =>
      Behaviors.receiveMessage{
        case Add(v) =>
          val accNew = acc + v
          ctx.log.info(s"Add number $v to $acc. Total state is $accNew");
          inc(accNew)

        case Get(replyTo) =>
          replyTo ! acc
          Behaviors.same
      }

    }

  }


  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val stateActorRef = ctx.spawn(StateActor(10), "state_actor")

      stateActorRef ! Add(5)
      stateActorRef ! Add(7)
//      stateActorRef ! Get(stateActorRef)

      Behaviors.same
    }


  val value = State()
  implicit val system: ActorSystem[NotUsed] = ActorSystem(value, "akka_typed")

  system.terminate()

}
