package com.github.kkhan01.amniservo

import com.github.kkhan01.amniservo.Amniservo

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello from Main!")
    val server = new Amniservo
    server.add("1", "2")
    server.add("3", "4")
    val _: Unit = server.start()
  }
}
