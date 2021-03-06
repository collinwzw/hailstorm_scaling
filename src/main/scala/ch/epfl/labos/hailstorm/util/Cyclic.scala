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
package ch.epfl.labos.hailstorm.util

import akka.actor.ActorRef
import ch.epfl.labos.hailstorm.Config

import scala.collection.immutable.Seq
import scala.util.Random

object Cyclic {

  def apply[A](seed: Int, nodes: Seq[ActorRef]): Cyclic[A] =
    new Cyclic[A](seed, nodes)
}

class Cyclic[A](seed: Int, nodes: Seq[ActorRef]) {
  val refsOrder: List[Int] = {
    val res = nodes.indices.toList
    new Random(3 * seed).shuffle(new Random(seed).shuffle(res))
  } // XXX: need to shuffle twice with this stupid RNG

  var size = nodes.size

  private var i = 0

  def permutation(): Seq[ActorRef] = {
    val seq: Seq[ActorRef] = refsOrder.map(i => Config.HailstormConfig.BackendConfig.NodesConfig.backendRefs(i))
    size = seq.size
    seq
  }

  def next: ActorRef = {
    val ret = permutation()(i)
    i = (i + 1) % size
    ret
  }

}
