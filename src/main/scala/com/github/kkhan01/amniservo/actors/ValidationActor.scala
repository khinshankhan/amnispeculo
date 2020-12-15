package com.github.kkhan01.amniservo.actors

import java.net.Socket
import java.io.{PrintStream, BufferedReader, InputStreamReader, BufferedSource}

import akka.actor.{Actor, PoisonPill, Props}

import com.github.kkhan01.amniservo.actors.ConnectionActor
import com.github.kkhan01.amniservo.util.Helpers

class ValidationActor(routes: Map[String, Map[String, (Map[String, String]) => String]]) extends Actor {
  override def preStart(): Unit = {
    println("Started validation.")
  }

  override def postStop(): Unit = {
    println("Ended validation.")
  }

  def receive = {
    case socket: Socket => {
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      val out = new PrintStream(socket.getOutputStream())
      var header = in.readLine()
      var(url, method, params) = Helpers.parseProtocol(header)

      var rawHeaders = ""
      var readingHeadersP = true
      while(readingHeadersP){
        var input = in.readLine()
        if(input == "") {
          readingHeadersP = false
        } else {
          rawHeaders += input + "\n"
          // println(input) // NOTE: sanity logging
        }
      }

      val headers = Helpers.getParams(rawHeaders, "\n", ": ")

      method match {
        case "GET" => {} // NOTE: everything is already done above
        case "POST" => {
          var data = ""
          // TODO: add more content types
          if(headers.contains("Content-Type") && headers("Content-Type").indexOf("multipart/form-data") >= 0){
            // TODO: allow more boundaries
            val boundary = headers("Content-Type").split("boundary=")(1).replaceAll("-", "")
            var rawData = ""
            var readingDataP = true
            while(readingDataP){
              var input = in.readLine()
              if(input == "") {
                readingDataP = false
              }
            }
            readingDataP = true
            while(readingDataP){
              var input = in.readLine()
              if(input.indexOf(boundary) > 0) {
                readingDataP = false
              } else {
                data += input + "\n"
              }
            }
          } else {
            while(in.ready()){
              data += in.read().asInstanceOf[Char]
            }
          }
          params = params + ("file" -> data)
        }
        case _ => url = "_"
      }

      val fn: (Map[String, String]) => String =
        if (routes.contains(url) && routes(url).contains(method)){
        routes(url)(method)
      } else if (routes.contains("_") && routes("_").contains(method)){
        routes("_")(method)
      } else {
        Helpers.invalid _
      }

      val actor = context.system.actorOf(Props(classOf[ConnectionActor], fn, params))
      actor ! socket

      self ! PoisonPill
    }
    case err: Throwable => {
      println("Debug: something went wrong, socket wasn't passed in:")
      println(err)
      self ! PoisonPill
    }
  }
}
