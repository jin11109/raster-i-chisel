import chisel3._

trait NumOps[T] {
  def add(a: T, b: T): T
  def sub(a: T, b: T): T
  def mul(a: T, b: T): T
  def lt(a: T, b: T): Bool
  def gt(a: T, b: T): Bool
}

object implicits {
  implicit class NumOpsSyntax[T](val a: T)(implicit ops: NumOps[T]) {
    def +(b: T): T = ops.add(a, b)
    def -(b: T): T = ops.sub(a, b)
    def *(b: T): T = ops.mul(a, b)
    def <(b: T): Bool = ops.lt(a, b)
    def >(b: T): Bool = ops.gt(a, b)
  }

  implicit object SIntNumOps extends NumOps[SInt] {
    def add(a: SInt, b: SInt): SInt = a + b
    def sub(a: SInt, b: SInt): SInt = a - b
    def mul(a: SInt, b: SInt): SInt = a * b
    def lt(a: SInt, b: SInt): Bool = a < b
    def gt(a: SInt, b: SInt): Bool = a > b
  }
}