package ch.epfl.labos.hailstorm.util


import java.util.TreeMap
import java.net.InetAddress
import scala.collection.mutable.ArrayBuffer
import com.typesafe.config.ConfigFactory

import java.util

object RemoveNode {
  val rn = new RemoveNode()
  def main(args: Array[String]): Unit = {
    val portList = rn.getmaplist(rn.localIpAddress,rn.oldhashring)
    val address = rn.getAddress(rn.myport,rn.hashring)
    print(address)
  }

}

class RemoveNode(){
  val localhost: InetAddress = InetAddress.getLocalHost
  val localIpAddress: String = localhost.getHostAddress
  private val config = ConfigFactory.load()
  private val root = config.getConfig("hailstorm")
  private val backendConfig = root.getConfig("backend")
  val oldrealNodeList: List[String] = backendConfig.getList("nodes").toArray().toList.map(_.toString.slice(8,28).split(":")(0)).distinct
  val Nodes:List[String] = backendConfig.getList("nodes").toArray().toList.map(_.toString.slice(8,25))
  val realNodeList:List[String]=removeMe(oldrealNodeList,localIpAddress)
  var virtualpool:List[String] = backendConfig.getList("nodes").toArray().toList.map(_.toString.slice(10,28).split(":")(1).slice(0,4))
  var nodemap:util.TreeMap[Int,String] = buildNodeMap(realNodeList)
  var oldnodemap:util.TreeMap[Int,String] = buildNodeMap(oldrealNodeList)
  var hashring:util.TreeMap[String,String] = buildHashRing(nodemap)
  var oldhashring:util.TreeMap[String,String] = buildHashRing(oldnodemap)
  val myport:ArrayBuffer[String] = getmaplist(localIpAddress,oldhashring)
  val nodenumber:Int = realNodeList.length //number of nodes
  val virtualsize:Int = backendConfig.getList("nodes").toArray().toList.size// need user to input


  //Initialize the port pool
  def buildPortPool(virturalNodeSize:Int):ArrayBuffer[String]={
    val virtualpool = ArrayBuffer[String]()
    val port = 2500
    for (i <- 0 to virturalNodeSize) {
      val newport = port + i
      virtualpool.append(newport.toString)
    }
    virtualpool
  }

  //
  def buildNodeMap(realnodelist:List[String]):util.TreeMap[Int,String]= {
    val mymap = new util.TreeMap[Int, String]()
    for (nodes <- realnodelist) {
      val hash = getHash(nodes)
      mymap.put(hash, nodes)
    }
    mymap
  }
  def removeMe(realNodeList:List[String],myaddress:String):List[String] ={
    val newlist = realNodeList.filterNot(x=> {x == myaddress})
    newlist
  }

  def buildHashRing(nodemap:util.TreeMap[Int,String]): util.TreeMap[String,String] ={
    val hashring = new util.TreeMap[String,String]()
    for (port <- virtualpool) {
      val porthash = getHash(port)
      val node = nodemap.tailMap(porthash) match {
        case tailMap if (tailMap.isEmpty) => nodemap.get(nodemap.firstKey)
        case tailMap => nodemap.get(tailMap.firstKey)
      }
      hashring.put(port, node)
    }
    hashring
  }

  //Hash function
  def getHash(server: String): Int = {
    import scala.util.hashing.MurmurHash3
    Math.abs(MurmurHash3.stringHash(server))
  }

  def getmaplist(myIP:String,hashring:util.TreeMap[String,String]): ArrayBuffer[String] ={
    val portlist = new ArrayBuffer[String]()
    for (port <- virtualpool) {
      if (hashring.get(port) == myIP) {
        portlist.append(port)
      }
    }
    portlist
  }

  def getAddress(myport:ArrayBuffer[String],hashring:util.TreeMap[String,String]): String = {
    val addresslist = new ArrayBuffer[String]()
    for(port <- myport){
      val address = hashring.get(port) + ":" + port
      addresslist.append(address)
    }
    val result = addresslist.toString()
    result.slice(12,result.length-1)
  }


}
