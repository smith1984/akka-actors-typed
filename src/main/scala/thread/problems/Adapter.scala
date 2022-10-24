package thread.problems

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import thread.problems.Adapter.TaskDispatcher.{LogFilter, ParseUrl}

import java.util.UUID

object Adapter extends App {


  object TaskDispatcher {
    import LogWorker._
    import JsonParser._

    sealed trait CommandDispatcher
    case class ParseUrl(url: String) extends CommandDispatcher
    case class LogFilter(work: String) extends CommandDispatcher

    case class LogResponseWrapper(msg: LogResponse) extends CommandDispatcher
    case class ParseResponseWrapper(msg: ParseResponse) extends CommandDispatcher

    def apply(): Behavior[CommandDispatcher] = Behaviors.setup { ctx =>
      val logAdapter: ActorRef[LogResponse] = ctx.messageAdapter[LogResponse]((rs: LogResponse) => LogResponseWrapper(rs))
      val parseAdapter: ActorRef[ParseResponse] = ctx.messageAdapter[ParseResponse](rs => ParseResponseWrapper(rs))

      Behaviors.receiveMessage {
        case LogFilter(work) =>
          val logWorker: ActorRef[LogRequest] = ctx.spawn(LogWorker(), s"LogWorkerNo${UUID.randomUUID()}")
          ctx.log.info(s"Dispatcher received log $work")
          logWorker ! Log(work, logAdapter)
          Behaviors.same
        case ParseUrl(url) =>
          val urlParser = ctx.spawn(JsonParser(), s"JsonParser${UUID.randomUUID()}")
          ctx.log.info(s"Dispatcher received json $url")
          urlParser ! Parse(url, parseAdapter)
          Behaviors.same


        case LogResponseWrapper(m) =>
          ctx.log.info("Dispatcher Log Done")
          Behaviors.same
        case ParseResponseWrapper(m) =>
          ctx.log.info("Dispatcher Parse Done")
          Behaviors.same
      }
    }
  }

  object LogWorker {
    sealed trait LogRequest
    case class Log(l: String, replyTo: ActorRef[LogResponse]) extends LogRequest

    sealed trait LogResponse
    case class LogDone() extends LogResponse

    def apply(): Behavior[LogRequest] = Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Log(_, dispatcherAdapter) =>
          ctx.log.info("Log work in progress")
          Thread.sleep(10000)
          dispatcherAdapter ! LogDone()
          Behaviors.stopped
      }
    }

  }

  object JsonParser {
    sealed trait ParseCommand
    case class Parse(json: String, replyTo: ActorRef[ParseResponse]) extends ParseCommand

    sealed trait ParseResponse
    case class ParseDone() extends ParseResponse

    def apply(): Behavior[ParseCommand] = Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Parse(j, r) =>
          ctx.log.info(s"Parsing $j is done")
          r ! ParseDone()
          Behaviors.stopped
      }
    }
  }


  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val dispatcherActorRef = ctx.spawn(TaskDispatcher(), "dispatcher")

      dispatcherActorRef ! LogFilter("asdasdasd")

      dispatcherActorRef ! ParseUrl("adsasdasdasd")

      Behaviors.same
    }

  implicit val system: ActorSystem[NotUsed] = ActorSystem(Adapter(), "dispatcher")

  Thread.sleep(5000)

  system.terminate()
}