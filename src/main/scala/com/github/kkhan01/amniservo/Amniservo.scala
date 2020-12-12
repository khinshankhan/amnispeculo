package com.github.kkhan01.amniservo

import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}

import com.github.kkhan01.amniservo.actors.ConnectionActor

object Amniservo {
  def server(): Unit = {
    val system = ActorSystem("ConnectionSystem")

    val PORT = 9999
    val bind: java.net.ServerSocket = new ServerSocket(PORT)

    while (true) {
      val connection: java.net.Socket = bind.accept()
      val actor = system.actorOf(Props[ConnectionActor])
      actor ! connection
    }
  }
}
