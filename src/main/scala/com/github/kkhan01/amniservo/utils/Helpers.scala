package com.github.kkhan01.amniservo.utils

object Helpers {
  def parseHeader(header: String): (String, Map[String, String]) = {
    var queryParams = Map[String, String]()

    println(header)
    val protocol = header.split(" ")
    // TODO: allow other types of methods
    if(protocol(0) == "GET"){
      val queryIndex = protocol(1).indexOf('?')
      if(queryIndex != -1){
        val url = protocol(1).substring(protocol(1).indexOf('/'), queryIndex)
        val nameValuePairs = protocol(1)
          .substring(queryIndex + 1)
          .split("&")
        nameValuePairs.foreach(kv => {
                                 val valueIndex = kv.indexOf('=')
                                 if (valueIndex != -1){
                                   val key = kv.substring(0, valueIndex)
                                   val value = kv.substring(valueIndex + 1)
                                   queryParams = queryParams + (key -> value)
                                 } else {
                                   val key = kv
                                   val value = ""
                                   queryParams = queryParams + (key -> value)
                                 }
                               })
        return(url, queryParams)
      } else {
        return (protocol(1).substring(protocol(1).indexOf('/')), queryParams)
      }
    }
    return ("_", queryParams)
  }
}
