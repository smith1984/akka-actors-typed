package thread.problems

import scala.concurrent.Future

object Problem3 extends App{
  import scala.concurrent.ExecutionContext.Implicits.global

  val futures = (0 to 9)
    .map(i => BigInt(100000 * i) until BigInt(100000 * (i + 1))) // 0 - 99999, 100000 - 199999, 200000 - 299999 etc
    .map(range => Future {
//      if (range.contains(546735))
      throw new RuntimeException("invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _)
  sumFuture.onComplete(println)


}
