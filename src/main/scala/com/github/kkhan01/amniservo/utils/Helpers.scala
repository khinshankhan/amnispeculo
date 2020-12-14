package com.github.kkhan01.amniservo.utils

object Helpers {
  def parseHeader(header: String): (String, Map[String, String]) = {
    var queryParams = Map[String, String]()

    println(header)
    val protocol = header.split(" ")
    // TODO: allow other types of methods
    protocol(0) match {
      case "GET" => {
        val queryIndex = protocol(1).indexOf('?')
        if(queryIndex != -1){
          val url = protocol(1).substring(protocol(1).indexOf('/'), queryIndex)
          val queryString = protocol(1).substring(queryIndex + 1)
          queryParams = getParams(queryString)
          return(url, queryParams)
        } else {
          return (protocol(1).substring(protocol(1).indexOf('/')), queryParams)
        }
      }
      case "POST" => {
        println("POSTING!")
        return ("_", queryParams)
      }
      case _ => return ("_", queryParams)
    }
  }

  def getParams(queryString: String): Map[String, String] = {
    var queryParams = scala.collection.mutable.Map[String, String]()

    val nameValuePairs = queryString.split("&")
    nameValuePairs.foreach(kv => {
                             val valueIndex = kv.indexOf('=')
                             if (valueIndex != -1){
                               val key = kv.substring(0, valueIndex)
                               val value = kv.substring(valueIndex + 1)
                               queryParams = queryParams + (key -> value)
                             } else {
                               val key = kv
                               val value = ""
                               queryParams += (key -> value)
                             }
                           })
    return collection.immutable.HashMap() ++ queryParams
  }
}
