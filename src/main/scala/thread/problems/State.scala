package thread.problems

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import thread.problems.Solution1.{FunctionalActor, ObjectActor}

object State extends App {
  sealed trait Command
  case class Add(v: Int) extends Command
  case class Get() extends Command


  object StateActor {
  //  var state = 0

    def apply(init: Int): Behavior[Command] =
    {
//      state = init
      inc(init)
    }


    def inc(acc: Int): Behavior[Command] = Behaviors.setup{ ctx =>
      Behaviors.receiveMessage{
        case Add(v) =>
//          state = state + v
          val accNew = acc + v
          throw new Exception("sdfsdfsdf")
          ctx.log.info(s"Add number $v to $acc. Total state is $accNew");
          inc(accNew)

        case Get() =>
          ctx.log.info(s"Total state is $acc");
          Behaviors.same
      }
    }
  }


  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val stateActorRef = ctx.spawn(StateActor(10), "state_actor")

      stateActorRef ! Add(5)
      stateActorRef ! Add(7)
      stateActorRef ! Get()

      Behaviors.same
    }


  val value = State()
  implicit val system: ActorSystem[NotUsed] = ActorSystem(value, "akka_typed")

  system.terminate()

}
