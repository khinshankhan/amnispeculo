package com.github.kkhan01.amniservo

import com.github.kkhan01.amniservo.Amniservo

object Main {
  def reverse(x: String) = x.reverse

  def main(args: Array[String]): Unit = {
    println("Hello from Main!")
    val server = new Amniservo
    server.add("1", reverse)
    val _: Unit = server.start()
  }
}
