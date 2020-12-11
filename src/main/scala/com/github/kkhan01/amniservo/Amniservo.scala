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
      try {
        while(true){
          val in = new BufferedSource(s.getInputStream()).getLines()
          val out = new PrintStream(s.getOutputStream())

          val input = in.next()
          println(input)
          out.println(input)
          out.flush()
        }
      } catch {
        case err: java.util.NoSuchElementException => s.close()
        case err: Throwable =>{
          s.close()
          println("Debug: unknown error:")
          println(err)
        }
      }
    }
  }
}
