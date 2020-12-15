package com.github.kkhan01.amniscite

import com.github.kkhan01.amniservo.Amniservo
import com.github.kkhan01.amniscite.Handler

object Main {
  def main(args: Array[String]): Unit = {
    val server = new Amniservo
    Handler.setup(server)
    server.start()
  }
}
