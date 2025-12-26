import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FixedMathTester extends Module {
  val io = IO(new Bundle {
    val op1 = Input(new Fixed24_13)
    val op2 = Input(new Fixed24_13)
    
    val outAdd = Output(new Fixed24_13)
    val outSub = Output(new Fixed24_13)
    val outMul = Output(new Fixed24_13)
    val outDiv = Output(new Fixed24_13)
    
    val rawIn = Input(SInt(24.W))
    val outFromRaw = Output(new Fixed24_13)
  })

  io.outAdd := io.op1 + io.op2
  io.outSub := io.op1 - io.op2
  io.outMul := io.op1 * io.op2
  io.outDiv := io.op1 / io.op2

  io.outFromRaw := Fixed24_13.fromRaw(io.rawIn)
}

class Fixed24_13Test extends AnyFlatSpec with ChiselScalatestTester {

  val bp = 11
  val scale = 1 << bp

  def toRaw(d: Double): Int = (d * scale).toInt

  def toDouble(raw: BigInt): Double = raw.toDouble / scale

  behavior of "Fixed24_13"

  it should "correctly handle fromRaw with Negative Numbers" in {
    test(new FixedMathTester) { c =>
      // -1 = 0xFFFFFF
      val negOne = -1
      
      c.io.rawIn.poke(negOne.S)
      c.clock.step()
      
      c.io.outFromRaw.bits.expect(negOne.S)
      
      val outVal = c.io.outFromRaw.bits.peek().litValue
      println(s"[fromRaw] In: -1, Out: $outVal")
      assert(outVal == BigInt(-1))
    }
  }

  it should "handle Multiplication with mixed signs" in {
    test(new FixedMathTester) { c =>
      val testCases = Seq(
        // (A, B, Expected)
        (-1.5,  2.0, -3.0),
        ( 1.5, -2.0, -3.0),
        (-1.5, -2.0,  3.0),
        (-0.5, -0.5,  0.25)
      )

      for ((a, b, expected) <- testCases) {
        c.io.op1.bits.poke(toRaw(a).S)
        c.io.op2.bits.poke(toRaw(b).S)
        c.clock.step()

        val rawRes = c.io.outMul.bits.peek().litValue
        val res = toDouble(rawRes)

        println(s"[Mul] $a * $b = $res (Raw: $rawRes)")
        
        // allow 1 LSB 
        assert(Math.abs(res - expected) < (1.0/scale), s"Failed: $a * $b")
      }
    }
  }

  it should "handle Division with mixed signs correctly" in {
    test(new FixedMathTester) { c =>
      val testCases = Seq(
        (-6.0,  2.0, -3.0),
        ( 6.0, -2.0, -3.0),
        (-6.0, -2.0,  3.0),
        
        (-1.0,  0.5, -2.0),  // -1 / 0.5 = -2
        (-1.0, -0.5,  2.0),  // -1 / -0.5 = 2

        (-1.0,  3.0, -0.333) 
      )

      for ((a, b, expected) <- testCases) {
        c.io.op1.bits.poke(toRaw(a).S)
        c.io.op2.bits.poke(toRaw(b).S)
        c.clock.step()

        val rawRes = c.io.outDiv.bits.peek().litValue
        val res = toDouble(rawRes)

        println(s"[Div] $a / $b = $res (Raw: $rawRes)")

        assert(Math.abs(res - expected) < 0.01, s"Failed: $a / $b got $res")
      }
    }
  }

  // Sanity Check
  it should "handle edge cases (Zero and One)" in {
    test(new FixedMathTester) { c =>
      // 0 / -5 = 0
      c.io.op1.bits.poke(toRaw(0.0).S)
      c.io.op2.bits.poke(toRaw(-5.0).S)
      c.clock.step()
      c.io.outDiv.bits.expect(0.S)

      // -5 / 1 = -5
      c.io.op1.bits.poke(toRaw(-5.0).S)
      c.io.op2.bits.poke(toRaw(1.0).S)
      c.clock.step()
      val res = toDouble(c.io.outDiv.bits.peek().litValue)
      assert(res == -5.0)
    }
  }
}
