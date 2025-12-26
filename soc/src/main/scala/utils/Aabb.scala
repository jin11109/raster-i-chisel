import chisel3._
import chisel3.util._
import implicits._

object Aabb2 {
  def apply[T <: Data](gen: T)(implicit ops: NumOps[T]): Aabb2[T] = new Aabb2(gen)

  def apply[T <: Data](min: NumVec[T], max: NumVec[T])(implicit ops: NumOps[T]): Aabb2[T] = {
    require(max.len == 2, "[Error] Min vector must have length 2")
    val gen = min.components(0).cloneType.asInstanceOf[T]
    
    val wire = Wire(new Aabb2(gen))
    wire.min := min
    wire.max := max
    wire
  }
}

class Aabb2[T <: Data](gen: T)(implicit ops: NumOps[T]) extends Bundle {
  val min = new NumVec(2, gen)
  val max = new NumVec(2, gen)

  def overlap(that: Aabb2[T]): Bool = {
    !(min(0) > that.max(0) || min(1) > that.max(1) || max(0) < that.min(0) || max(1) < that.min(1))
  }
}
