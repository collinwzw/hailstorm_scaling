import java.util.TreeMap
import com.typesafe.config._

import java.util
import scala.+:
import scala.collection.mutable.ArrayBuffer

object ConsistentHashing {

}

class ConsistentHashing {
  val realnodelist = Config.HailstormConfig.BackendConfig.NodesConfig.nodes //get the physical node ip list from Config
  val nodenumber = realnodelist.size //number of nodes
  val virtualsize = 100// need user to input
  var virtualpool = ArrayBuffer[String]()
  val nodemap =  new util.TreeMap[Int, String]()
  val hashring = new util.TreeMap[String,String]()

  //Initialize the Hashing map


  def initialize() {
    val port = 2500
    for (i <- 0 to 100) {
      val newport = port+i
      virtualpool.append(newport.toString)

    }

    for (i <- 0 to nodenumber) {
      val hash = getHash(realnodelist[i])
      nodemap.put(hash, realnodelist[i])
    }

    for (port <= virtualpool) {
      var porthash = getHash(port)
      var node = nodemap.tailMap(porthash) match {
        case tailMap if (tailMap.isEmpty) => Some(nodemap.get(nodemap.firstKey))
        case tailMap => Some(nodemap.get(tailMap.firstKey))
      }
      hashring.put(port, node)
      }
    updateHashring()
    }


  //Hashring function
  def getHash(server: String): Int = {
    import scala.util.hashing.MurmurHash3
    Math.abs(MurmurHash3.stringHash(server))
  }
  def addRealnode(server:String): Unit ={
    val hash = getHash(server)
    nodemap.put(hash,server)
  }

  def updateHashring(): Unit ={
    for (port <- virtualpool) {
      var porthash = getHash(port)
      var node = nodemap.tailMap(porthash) match {
        case tailMap if (tailMap.isEmpty) => Some(nodemap.get(nodemap.firstKey))
        case tailMap => Some(nodemap.get(tailMap.firstKey))
      }
      hashring.put(port, node)
    }
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
  }