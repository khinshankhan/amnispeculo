package com.github.kkhan01.amniserver

import java.net._
import java.io._
import scala.io._

object Amniserver {
  def server(): Unit = {
    val PORT = 9999
    val bind = new ServerSocket(PORT)

    while (true) {
      val s = bind.accept()
      val in = new BufferedSource(s.getInputStream()).getLines()
      val out = new PrintStream(s.getOutputStream())

      val input = in.next()
      println(input)
      out.println(input)
      out.flush()
      s.close()
    }
  }
}
