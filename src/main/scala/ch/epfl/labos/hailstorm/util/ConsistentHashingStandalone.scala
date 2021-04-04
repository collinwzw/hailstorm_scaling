import ConsistentHashingStandalone.main

import java.util.TreeMap
import java.net.InetAddress
import scala.collection.mutable.ArrayBuffer
import com.typesafe.config.ConfigFactory

import java.util

object ConsistentHashingStandalone {
  val ch = new ConsistentHashingStandalone()
  def main(args: Array[String]): Unit = {
  val portList = ch.getportlist(ch.localIpAddress)
  print(portList)
}

}

class ConsistentHashingStandalone(){
  val localhost: InetAddress = InetAddress.getLocalHost
  val localIpAddress: String = localhost.getHostAddress
  private val config = ConfigFactory.load()
  private val root = config.getConfig("hailstorm")
  private val backendConfig = root.getConfig("backend")
  val oldrealNodeList: List[String] = backendConfig.getList("nodes").toArray().toList.map(_.toString.slice(8,25).split(":")(0))
  val realNodeList:List[String]=addNode(oldrealNodeList,localIpAddress)
  var virtualpool:ArrayBuffer[String] = buildPortPool(virtualsize = 100)
  var nodemap:util.TreeMap[Int,String] = buildNodeMap(realNodeList)
  var hashring:util.TreeMap[String,String] = buildHashRing()
  val nodenumber:Int = realNodeList.length //number of nodes
  val virtualsize = 100// need user to input
//  var virtualpool = ArrayBuffer[String]()
//  val nodemap =  new util.TreeMap[Int, String]()
//  val hashring = new util.TreeMap[String,String]()

      //Initialize the port pool
      def buildPortPool(virtualsize:Int):ArrayBuffer[String]={
        val virtualpool = ArrayBuffer[String]()
        val port = 2500
        for (i <- 0 to 100) {
          val newport = port + i
          virtualpool.append(newport.toString)
        }
        virtualpool
    }
//
    def buildNodeMap(realnodelist:List[String]):util.TreeMap[Int,String]= {
      nodemap = new util.TreeMap[Int, String]()
      for (nodes <- realnodelist) {
        val hash = getHash(nodes)
        nodemap.put(hash, nodes)
      }
    nodemap
    }
    def addNode(realNodeList:List[String],newnode:String):List[String] ={
      val newlist = newnode +: realNodeList
      newlist
    }


  def buildHashRing(): TreeMap[String,String] ={
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

    def getportlist(realnode:String): ArrayBuffer[String] ={
        val portlist = new ArrayBuffer[String]()
        for (port <- virtualpool) {
          if (hashring.get(port) == realnode) {
            portlist.append(port)
          }
        }
        portlist
        }

  def backendNodelist():ArrayBuffer[String] ={
    val backendNode = ArrayBuffer[String]
    for(nodes <- realNodeList){
      val portlist = getportlist(nodes)
      for(port <- portlist){

      }
    }

  }
    }