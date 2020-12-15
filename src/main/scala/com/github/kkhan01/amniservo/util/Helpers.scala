package com.github.kkhan01.amniservo.util

object Helpers {
  def parseProtocol(protocol: String): (String, String, Map[String, String]) = {
    val protocolParts = protocol.split(" ")
    val method = protocolParts(0)

    val queryIndex = protocolParts(1).indexOf('?')
    if(queryIndex != -1){
      val url = protocolParts(1).substring(protocolParts(1).indexOf('/'), queryIndex)
      val queryString = protocolParts(1).substring(queryIndex + 1)
      (url, method, getParams(queryString, "&", "="))
    } else {
      (protocolParts(1).substring(protocolParts(1).indexOf('/')), method, Map[String, String]())
    }
  }

  def getParams(queryString: String, splitDelimiter: String, keyDelimiter: String): Map[String, String] = {
    var params = Map[String, String]()

    val nameValuePairs = queryString.split(splitDelimiter)
    nameValuePairs.foreach(
      kv => {
        val valueIndex = kv.indexOf(keyDelimiter)
        if (valueIndex != -1){
          val key = kv.substring(0, valueIndex)
          val value = kv.substring(valueIndex + 1)
          params = params + (key -> value)
        } else {
          val key = kv
          val value = ""
          params = params + (key -> value)
        }
      }
    )
    params
  }

  def invalid(params: Map[String, String]): String = "Invalid route."
}
