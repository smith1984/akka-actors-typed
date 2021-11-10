package thread.problems

object Problem2 extends App {
  object Consumer {
    var task: Runnable = null

    val backgroundThread: Thread = new Thread(() => {
      while (true) {
        while (task == null) {
          backgroundThread.synchronized {
            println("[background] waiting for a task...")
            backgroundThread.wait()
          }
        }

        task.synchronized {
          println("[background] I have a task!")
          throw new Exception();
          task.run()
          task = null
        }
      }
    })
  }

  object Producer {
    def sendTaskToBackgroundThread(r: Runnable): Unit = {
      if (Consumer.task == null) Consumer.task = r

      Consumer.backgroundThread.synchronized {
        Consumer.backgroundThread.notify()
      }
    }
  }

  Consumer.backgroundThread.start()
  Thread.sleep(1000)
  Producer.sendTaskToBackgroundThread(() => println(42))
  Thread.sleep(1000)
  Producer.sendTaskToBackgroundThread(() =>  println("this should run in the background"))
}
