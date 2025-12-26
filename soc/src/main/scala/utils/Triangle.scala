import chisel3._
import chisel3.util._
import implicits._

object Triangle2 {
  def apply[T <: Data](gen: T)(implicit ops: NumOps[T]): Triangle2[T] = new Triangle2(gen)

  def apply[T <: Data](a: NumVec[T], b: NumVec[T], c: NumVec[T])(implicit ops: NumOps[T]): Triangle2[T] = {
    val gen = a.components(0).cloneType.asInstanceOf[T]
    val wire = Wire(new Triangle2(gen))
    wire.vertices(0) := a
    wire.vertices(1) := b
    wire.vertices(2) := c
    wire
  }
}

class Triangle2[T <: Data](gen: T)(implicit ops: NumOps[T]) extends Bundle {
  val vertices = Vec(3, NumVec(2, gen))

  def aabb(): Aabb2[T] = {
    def minOp(a: T, b: T): T = Mux(a < b, a, b)
    def maxOp(a: T, b: T): T = Mux(a > b, a, b)

    val minX = Seq(vertices(0)(0), vertices(1)(0), vertices(2)(0)).reduce(minOp)
    val minY = Seq(vertices(0)(1), vertices(1)(1), vertices(2)(1)).reduce(minOp)
    val maxX = Seq(vertices(0)(0), vertices(1)(0), vertices(2)(0)).reduce(maxOp)
    val maxY = Seq(vertices(0)(1), vertices(1)(1), vertices(2)(1)).reduce(maxOp)

    Aabb2(NumVec(minX, minY), NumVec(maxX, maxY))
  }

  def apply(idx: Int) = vertices(idx)
  def apply(idx: UInt) = vertices(idx)
}