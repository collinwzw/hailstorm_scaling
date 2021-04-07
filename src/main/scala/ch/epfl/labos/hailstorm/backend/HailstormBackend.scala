/*
 * Copyright (c) 2020 EPFL IC LABOS.
 *
 * This file is part of Hailstorm
 * (see https://labos.epfl.ch/hailstorm).
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ch.epfl.labos.hailstorm.backend

import akka.actor._
import akka.pattern._
import akka.util._
import better.files.File
import ch.epfl.labos.hailstorm._
import ch.epfl.labos.hailstorm.common._
import com.typesafe.config.ConfigValueFactory

import java.net.InetAddress
import scala.util.Failure
import scala.util.Success
import scala.collection.mutable.{Map => MMap}
import scala.concurrent.duration._
import scala.language.postfixOps

object HailstormBackendActor {

  val name: String = "master"

  def props(subDirectory: Option[String]): Props = Props(classOf[HailstormBackendActor], subDirectory)

}

class HailstormBackendActor(subDirectory: Option[String]) extends Actor with ActorLogging {

  import context.dispatcher

  implicit val timeout = Timeout(300 seconds)

  val rootFile = subDirectory match {
    case Some(sd) => File(Config.HailstormConfig.BackendConfig.DataConfig.dataDirectory.getAbsolutePath) / sd
    case _ => File(Config.HailstormConfig.BackendConfig.DataConfig.dataDirectory.getAbsolutePath)
  }

  val bags = MMap.empty[Bag, ActorRef]

  def receive = {
    case cmd: Command => (bagActor(cmd.bag) ? cmd) pipeTo sender
  }

  log.info(s"Started backend!")

  def bagActor(bag: Bag): ActorRef =
    bags getOrElseUpdate(bag, context.actorOf(HailstormIO.props(bag, rootFile)))

}

object HailstormBackend {
  case class System(system: ActorSystem, port: Int)
  var systems: List[System] = Nil
  def stop(port: Array[String]): Unit = {
    systems = systems.filter(system => if (port.contains(system.port.toString)) {
      system.system.log.debug("Terminating port:" + system.port)
      system.system.terminate()
      dirDel(File("data/"+system.port.toString))
      false
    } else {
      true
    })
  }


  def dirDel(path: File): Unit = {
    if (!path.exists()) {
      return
    }
    else if (!path.isDirectory) {
      path.delete()
      println(path + " deleted")
      return
    }

    val files: Array[File] = path.list.toArray
    for (file <- files) {
      dirDel(file)
    }
    path.delete()
    println(path + " deleted")
  }

  def startNewNode(hostname: String, port: Int): Unit = {
    var config = {
        Config.HailstormConfig.BackendConfig.backendConfig
          .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(hostname))
          .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(port))
          .withValue("akka.remote.netty.tcp.bind-hostname", ConfigValueFactory.fromAnyRef(hostname))
          .withValue("akka.remote.netty.tcp.bind-port", ConfigValueFactory.fromAnyRef(port))
          .withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(hostname))
          .withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(port))
          .withValue("akka.remote.artery.bind.hostname", ConfigValueFactory.fromAnyRef(hostname))
          .withValue("akka.remote.artery.bind.port", ConfigValueFactory.fromAnyRef(port))
      }

    val system = ActorSystem("HailstormBackend", config)
    systems = systems :+ System(system, port)
    ch.epfl.labos.hailstorm.frontend.Statistics.init(system.dispatchers.lookup("hailstorm.backend.statistics-dispatcher"))

    system.log.debug(Config.HailstormConfig.BackendConfig.NodesConfig.nodes.size.toString())
    Config.ModeConfig.mode match {
      case Config.ModeConfig.Dev =>
        system.actorOf(HailstormBackendActor.props(Some(port + "")), HailstormBackendActor.name)
      case Config.ModeConfig.Scl =>
        system.actorOf(HailstormBackendActor.props(Some(port + "")), HailstormBackendActor.name)
      case Config.ModeConfig.Prod =>
        system.actorOf(HailstormBackendActor.props(None), HailstormBackendActor.name)
    }
    system.log.debug("New node:" + hostname + ":" + port.toString)
    system.log.debug("Allocating buffers...")
    Config.HailstormConfig.BackendConfig.NodesConfig.addNewBackend(hostname, port)
    BackendChunkPool.init()

  }

  def start(cliArguments: CliArguments): Unit = {
    var config =
      if (cliArguments.me.isSupplied) {
        //Config.HailstormConfig.me = arguments.me()
        Config.HailstormConfig.BackendConfig.backendConfig
          .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).hostname))
          .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).port))
          .withValue("akka.remote.netty.tcp.bind-hostname", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).hostname))
          .withValue("akka.remote.netty.tcp.bind-port", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).port))
          .withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).hostname))
          .withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).port))
          .withValue("akka.remote.artery.bind.hostname", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).hostname))
          .withValue("akka.remote.artery.bind.port", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).port))
      } else {
        Config.HailstormConfig.BackendConfig.backendConfig
          .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.localNode.hostname))
          .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.localNode.port))
          .withValue("akka.remote.artery.canonical.hostname", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.localNode.hostname))
          .withValue("akka.remote.artery.canonical.port", ConfigValueFactory.fromAnyRef(Config.HailstormConfig.BackendConfig.NodesConfig.localNode.port))
      }
    if (cliArguments.verbose()) {
      config = config.withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("DEBUG"))
    }

    val system = ActorSystem("HailstormBackend", config)
    ch.epfl.labos.hailstorm.frontend.Statistics.init(system.dispatchers.lookup("hailstorm.backend.statistics-dispatcher"))

    val port =
      if (cliArguments.me.isSupplied) {
        Config.HailstormConfig.BackendConfig.NodesConfig.nodes(cliArguments.me()).port
      } else {
        Config.HailstormConfig.BackendConfig.NodesConfig.localNode.port
      }
    systems = systems :+ System(system, port)


    Config.ModeConfig.mode match {
      case Config.ModeConfig.Dev =>
        system.actorOf(HailstormBackendActor.props(Some(port + "")), HailstormBackendActor.name)
      case Config.ModeConfig.Scl =>
        system.actorOf(HailstormBackendActor.props(Some(port + "")), HailstormBackendActor.name)
      case Config.ModeConfig.Prod =>
        system.actorOf(HailstormBackendActor.props(None), HailstormBackendActor.name)
    }
    system.log.debug("Allocating buffers...")
    BackendChunkPool.init()


  }

}
