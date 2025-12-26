import chisel3._
import chisel3.util._
import implicits._ 

object Fixed24_13 {
  val width = 24
  val binaryPoint = 11

  def apply(): Fixed24_13 = new Fixed24_13
  def apply(in: SInt): Fixed24_13 = { val r = Wire(new Fixed24_13); r.bits := (in << binaryPoint).asSInt; r }
  def apply(in: UInt): Fixed24_13 = { val r = Wire(new Fixed24_13); r.bits := (in.zext << binaryPoint).asSInt; r }
  def fromDouble(d: Double): Fixed24_13 = { val r = Wire(new Fixed24_13); r.bits := (d * (1 << binaryPoint)).toInt.S(width.W); r }
  def fromRaw(raw: SInt): Fixed24_13 = { val r = Wire(new Fixed24_13); r.bits := raw; r }

  implicit object FixedNumOps extends NumOps[Fixed24_13] {
    def add(a: Fixed24_13, b: Fixed24_13): Fixed24_13 = a + b
    def sub(a: Fixed24_13, b: Fixed24_13): Fixed24_13 = a - b
    def mul(a: Fixed24_13, b: Fixed24_13): Fixed24_13 = a * b
    def lt(a: Fixed24_13, b: Fixed24_13): Bool = a < b
    def gt(a: Fixed24_13, b: Fixed24_13): Bool = a > b
  }
}

class Fixed24_13 extends Bundle {
  val bits = SInt(Fixed24_13.width.W)
  private def bp = Fixed24_13.binaryPoint
  private def rawWidth = Fixed24_13.width

  def +(that: Fixed24_13): Fixed24_13 = { val r = Wire(new Fixed24_13); r.bits := this.bits + that.bits; r }
  def -(that: Fixed24_13): Fixed24_13 = { val r = Wire(new Fixed24_13); r.bits := this.bits - that.bits; r }
  def *(that: Fixed24_13): Fixed24_13 = { 
      val r = Wire(new Fixed24_13)
      val product = this.bits * that.bits
      r.bits := (product >> bp)(rawWidth - 1, 0).asSInt
      r
  }

  // Warning: Division is very resource-intensive and has poor timing in hardware.
  def /(that: Fixed24_13): Fixed24_13 = {
    val res = Wire(new Fixed24_13)
    val num = (this.bits << bp).asSInt 
    val quotient = num / that.bits
    res.bits := quotient(rawWidth - 1, 0).asSInt
    res
  }
  
  def >(that: Fixed24_13): Bool = this.bits > that.bits
  def <(that: Fixed24_13): Bool = this.bits < that.bits
  def >=(that: Fixed24_13): Bool = this.bits >= that.bits
  def <=(that: Fixed24_13): Bool = this.bits <= that.bits
  def ===(that: Fixed24_13): Bool = this.bits === that.bits
}