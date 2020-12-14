package com.github.kkhan01.amniservo.actors

import java.net.Socket

import java.io.{PrintStream, PrintWriter, StringWriter}

import scala.io.BufferedSource

import akka.actor.{Actor, PoisonPill}

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}

class ConnectionActor(methods: Map[String, (Map[String, String]) => String]) extends Actor {
  override def preStart(): Unit = {
    println("Started validation.")
  }

  override def postStop(): Unit = {
    println("Ended validation.")
  }

  def getParams(queryString: String, splitDelimiter: String, keyDelimiter: String): Map[String, String] = {
    var params = scala.collection.mutable.Map[String, String]()

    val nameValuePairs = queryString.split(splitDelimiter)
    nameValuePairs.foreach(kv => {
                             val valueIndex = kv.indexOf(keyDelimiter)
                             if (valueIndex != -1){
                               val key = kv.substring(0, valueIndex)
                               val value = kv.substring(valueIndex + 1)
                               params = params + (key -> value)
                             } else {
                               val key = kv
                               val value = ""
                               params += (key -> value)
                             }
                           })
    return collection.immutable.HashMap() ++ params
  }

  // TODO: implement real 500 http error
  def invalid(params: Map[String, String]): String = "Invalid route."

  def send(s: String): String = "HTTP/1.0 200 OK\n\r\n" + s

  def receive = {
    case socket: java.net.Socket => {
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      val out = new PrintStream(socket.getOutputStream())

      var header = in.readLine()
      // println(header) // NOTE: sanity logging
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

      val headers = getParams(rawHeaders, "\n", ": ")
      println(headers)

      val protocol = header.split(" ")
      var params = Map[String, String]()
      // TODO: allow other types of methods
      val url = protocol(0) match {
        case "GET" => {
          val queryIndex = protocol(1).indexOf('?')
          if(queryIndex != -1){
            val url = protocol(1).substring(protocol(1).indexOf('/'), queryIndex)
            val queryString = protocol(1).substring(queryIndex + 1)
            params = getParams(queryString, "&", "=")
            url
          } else {
            protocol(1).substring(protocol(1).indexOf('/'))
          }
        }
        case "POST" => {
          var data = ""
          while(in.ready()){
            data += in.read().asInstanceOf[Char]
          }
          print("DATA FROM POST:")
          println(data) // NOTE: sanity logging
          params = getParams(data, "&", "=")
          protocol(1).substring(protocol(1).indexOf('/'))
        }
        case _ => "_"
      }

      if (methods.contains(url)){
        out.println(send(methods(url)(params)))
      } else if (methods.contains("_")){
        out.println(send(methods("_")(params)))
      } else {
        out.println(send(invalid(params)))
      }

      socket.close()
      self ! PoisonPill
    }
    case err: Throwable => {
      println("Debug: something went wrong, socket wasn't passed in.")
      println(err)
    }
  }
}
