// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT
//
// Modifications (c) 2025 jin11109
// Licensed under MIT License

import chisel3._
import chisel3.util._
import implicits._

class UVec2(val xWidth: Int, val yWidth: Int) extends Bundle {
  val x = UInt(xWidth.W)
  val y = UInt(yWidth.W)
}

class UVec2Factory(val xWidth: Int, val yWidth: Int) {
  def apply() = new UVec2(xWidth, yWidth)
  def apply(x: Int, y: Int) = {
    val res = Wire(new UVec2(xWidth, yWidth))
    res.x := x.U; res.y := y.U; res
  }
}

class NumVec[T <: Data](val len: Int, gen: T)(implicit ops: NumOps[T]) extends Bundle {
  val components = Vec(len, gen)

  def +(that: NumVec[T]): NumVec[T] = {
    val result = Wire(new NumVec(len, gen))
    for (i <- 0 until len) {
      result.components(i) := this.components(i) + that.components(i)
    }
    result
  }

  def -(that: NumVec[T]): NumVec[T] = {
    val result = Wire(new NumVec(len, gen))
    for (i <- 0 until len) {
      result.components(i) := this.components(i) - that.components(i)
    }
    result
  }

  def *(scalar: T): NumVec[T] = {
    val result = Wire(new NumVec(len, gen))
    for (i <- 0 until len) {
      result.components(i) := this.components(i) * scalar
    }
    result
  }

  def dot(that: NumVec[T]): T = {
    (this.components zip that.components).map { case (a, b) => a * b }.reduce(_ + _)
  }
  
  def cross(that: NumVec[T]): NumVec[T] = {
    require(this.len == 3 && that.len == 3)
    
    val term0 = this(1) * that(2) - this(2) * that(1)
    val newGen = term0.cloneType.asInstanceOf[T]

    val result = Wire(new NumVec(3, newGen)) 

    result.components(0) := term0
    result.components(1) := this(2) * that(0) - this(0) * that(2)
    result.components(2) := this(0) * that(1) - this(1) * that(0)
    result
  }

  def apply(idx: Int) = components(idx)
  def apply(idx: UInt) = components(idx)
}

object NumVec {
  def apply[T <: Data](vals: T*)(implicit ops: NumOps[T]): NumVec[T] = {
    require(vals.nonEmpty)
    val len = vals.length
    val gen = vals.head.cloneType
    val wire = Wire(new NumVec(len, gen))
    for ((valData, index) <- vals.zipWithIndex) {
      wire.components(index) := valData
    }
    wire
  }

  def apply[T <: Data](len: Int, gen: T)(implicit ops: NumOps[T]): NumVec[T] = {
    new NumVec(len, gen)
  }
}