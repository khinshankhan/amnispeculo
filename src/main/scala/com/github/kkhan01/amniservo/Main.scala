package com.github.kkhan01.amniserver

import com.github.kkhan01.amniserver.Amniserver

object Main {
    def main(args: Array[String]): Unit = {
      println("Hello from Main!")
      val _: Unit = Amniserver.server()
    }
}
