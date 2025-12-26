// Original code (c) 2023 Alan Jian
// Licensed under MIT License
//
// Modifications (c) 2025 jin11109
// Licensed under MIT License

import chisel3._
import chisel3.util._

class Renderer extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool())
    val fbId = Input(UInt(Fb.idWidth.W))
    // Uses the Flat Uppercase AXI interface to match Vram/Trinity
    val vram = Flipped(new WrAxiExtUpper(Vram.addrWidth, Vram.dataWidth))
  })

  val fbWriter = Module(new FbWriter)
  fbWriter.io.fbId := io.fbId

  /* Manually connect Flat AXI (io.vram) to Structured AXI (fbWriter) */
  /* Write Address Channel (AW) */
  io.vram.AWID    := fbWriter.io.vram.addr.bits.id
  io.vram.AWADDR  := fbWriter.io.vram.addr.bits.addr
  io.vram.AWLEN   := fbWriter.io.vram.addr.bits.len
  io.vram.AWSIZE  := fbWriter.io.vram.addr.bits.size
  io.vram.AWBURST := fbWriter.io.vram.addr.bits.burst
  io.vram.AWVALID := fbWriter.io.vram.addr.valid
  fbWriter.io.vram.addr.ready := io.vram.AWREADY

  // Drive defaults for AXI signals that FbWriter doesn't use
  io.vram.AWLOCK   := false.B
  io.vram.AWCACHE  := "b0011".U
  io.vram.AWPROT   := "b000".U
  io.vram.AWQOS    := 0.U
  io.vram.AWREGION := 0.U

  /* Write Data Channel (W) */
  io.vram.WDATA   := fbWriter.io.vram.data.bits.data
  io.vram.WSTRB   := fbWriter.io.vram.data.bits.strb
  io.vram.WLAST   := fbWriter.io.vram.data.bits.last
  io.vram.WVALID  := fbWriter.io.vram.data.valid
  fbWriter.io.vram.data.ready := io.vram.WREADY

  /* Write Response Channel (B) */
  fbWriter.io.vram.resp.bits.id   := io.vram.BID
  fbWriter.io.vram.resp.bits.resp := io.vram.BRESP
  fbWriter.io.vram.resp.valid     := io.vram.BVALID
  io.vram.BREADY := fbWriter.io.vram.resp.ready

  // Write data, Purple Pixel, as temporarily tesing way
  val purple = FbRGB(255.U, 0.U, 255.U)
  val purplePixels = VecInit(Seq.fill(Fb.nrBanks)(purple))

  // Intermidiate memory buffer  
  val memVertex = Module(new GenericRam(new ScreenVertex, 2048))
  val memPos    = Module(new GenericRam(new TransFormedPos, 2048))
  val memNorm   = Module(new GenericRam(new TransFormedNorm, 4096))
  val memBB     = Module(new GenericRam(new BoundingBox, 4096))
  // Prevent optimize out these buffer
  /* TODO: Remove these if don't need */
  dontTouch(memVertex.io)
  dontTouch(memPos.io)
  dontTouch(memNorm.io)
  dontTouch(memBB.io)

  // Geometry Unit
  val geometry = Module(new Geometry)
  geometry.io.angle := 0.U 
  
  // Connect Memory Interfaces: Write Ports
  memVertex.io.writer <> geometry.io.vtxWritePort
  memPos.io.writer    <> geometry.io.posWritePort
  memNorm.io.writer   <> geometry.io.normWritePort
  memBB.io.writer     <> geometry.io.bbWritePort
  // Connect Memory Interfaces: Read Ports
  memVertex.io.reader <> geometry.io.VtxReadPort

  // Unused read ports
  memPos.io.reader.req.valid := false.B; memPos.io.reader.req.bits := 0.U
  memNorm.io.reader.req.valid := false.B; memNorm.io.reader.req.bits := 0.U
  memBB.io.reader.req.valid := false.B; memBB.io.reader.req.bits := 0.U

  // State Machine
  val sIdle :: sRunning :: sWaitFbWriter :: Nil = Enum(3)
  val state = RegInit(sRunning)
  val fbIdReg = RegNext(io.fbId, 1.U)
  val start   = (io.fbId =/= fbIdReg)

  switch (state) {
    is (sIdle) {
      when (start) {
        state := sRunning
      }
    }
    is (sRunning) {
      when (geometry.io.done) {
        state := sWaitFbWriter
      }
    }
    
    is (sWaitFbWriter) {
      // If don't use !RegNext(fbWriter.io.done), the current donesignal
      // remains at true(Level-sensitive), causing the second frame to read the
      // old High signal at the very beginning.
      when (fbWriter.io.done && !RegNext(fbWriter.io.done)) {
        state := sIdle
      }
    }
  }

  // Drive FbWriter request
  fbWriter.io.req.valid := (state === sWaitFbWriter)
  fbWriter.io.req.bits.pix := purplePixels

  // Output done signal
  io.done := (state === sIdle)
}