import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class DataLoaderTest extends AnyFlatSpec with ChiselScalatestTester {
  def hex24SignExtend(hex: Int): chisel3.SInt = {
    val shift = 32 - 24
    ((hex << shift) >> shift).S
  }

  "DataLoader" should "read correct values from Hex files" in {
      test(new DataLoader){ dut =>
          
      // Test MESH_INDICES[0] values 000000010093
      dut.io.geo.idxAddr.poke(0.U)
      dut.clock.step(1)
      dut.io.geo.idxData(0).expect(0x93.S)
      dut.io.geo.idxData(1).expect(0x1.S)
      dut.io.geo.idxData(2).expect(0x0.S)
      // Test MESH_INDICES[100] values 00ba00bb00bd
      dut.io.geo.idxAddr.poke(100.U)
      dut.clock.step(1)
      dut.io.geo.idxData(0).expect(0xbd.S)
      dut.io.geo.idxData(1).expect(0xbb.S)
      dut.io.geo.idxData(2).expect(0xba.S)

      // Test MESH_VERTICES[0] values FFFD95FFFDF7FFFFF9
      dut.io.geo.vtxAddr.poke(0.U)
      dut.clock.step(1)
      dut.io.geo.vtxData(0).bits.expect(hex24SignExtend(0xFFFFF9))
      dut.io.geo.vtxData(1).bits.expect(hex24SignExtend(0xFFFDF7))
      dut.io.geo.vtxData(2).bits.expect(hex24SignExtend(0xFFFD95))
      // Test MESH_VERTICES[100] values FFFF91FFF76200014D
      dut.io.geo.vtxAddr.poke(100.U)
      dut.clock.step(1)
      dut.io.geo.vtxData(0).bits.expect(hex24SignExtend(0x00014D))
      dut.io.geo.vtxData(1).bits.expect(hex24SignExtend(0xFFF762))
      dut.io.geo.vtxData(2).bits.expect(hex24SignExtend(0xFFFF91))

      // Test MESH_NORMALS[0] values 00024C0000C300079F
      dut.io.geo.normAddr.poke(0.U)
      dut.clock.step(1)
      dut.io.geo.normData(0).bits.expect(hex24SignExtend(0x00079F))
      dut.io.geo.normData(1).bits.expect(hex24SignExtend(0x0000C3))
      dut.io.geo.normData(2).bits.expect(hex24SignExtend(0x00024C))
      // Test MESH_NORMALS[100] values 0005610002AE000546
      dut.io.geo.normAddr.poke(100.U)
      dut.clock.step(1)
      dut.io.geo.normData(0).bits.expect(hex24SignExtend(0x000546))
      dut.io.geo.normData(1).bits.expect(hex24SignExtend(0x0002AE))
      dut.io.geo.normData(2).bits.expect(hex24SignExtend(0x000561))

      // Test SINE[0] values 000000
      dut.io.math.angle.poke(0.U)
      dut.clock.step(1)
      dut.io.math.sine.bits.expect(hex24SignExtend(0x000000))
      // Test SINE[100] values 0007e0
      dut.io.math.angle.poke(100.U)
      dut.clock.step(1)
      dut.io.math.sine.bits.expect(hex24SignExtend(0x0007e0))
      
      // Test SINE[0] values 000000
      dut.io.math.angle.poke(0.U)
      dut.clock.step(1)
      dut.io.math.cosine.bits.expect(hex24SignExtend(0x000800))
      // Test SINE[100] values fffe9c
      dut.io.math.angle.poke(100.U)
      dut.clock.step(1)
      dut.io.math.cosine.bits.expect(hex24SignExtend(0xfffe9c))
      
    }
  }
}
