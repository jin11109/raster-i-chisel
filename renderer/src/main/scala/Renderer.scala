package renderer

import chisel3._
import chisel3.util._

class Renderer extends Module {
  val io = IO(new Bundle {
    val start     = Input(Bool())
    val angle     = Input(UInt(9.W))
    val fb_id     = Input(UInt(1.W))
    val done      = Output(Bool())
    
    // For test
    val mem_we    = Output(Bool())
    val mem_addr  = Output(UInt(32.W))
    val mem_wdata = Output(UInt(128.W))
  })

  // Dummy Logic for testing pipeline
  // Just writes a specific color to a specific address to prove it works
  
  val counter = RegInit(0.U(32.W))
  val active  = RegInit(false.B)

  io.done := false.B
  io.mem_we := false.B
  io.mem_addr := 0.U
  io.mem_wdata := 0.U

  when(io.start) {
    active := true.B
    counter := 0.U
  }

  when(active) {
    counter := counter + 1.U
    
    // Write white color to first 100 pixels
    io.mem_we := true.B
    io.mem_addr := counter
    // RGBA: FFFFFFFF repeated 4 times for 128 bit
    io.mem_wdata := "hFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF".U(128.W) 

    when (counter > 100.U) {
      active := false.B
      io.done := true.B
    }
  }
}

object Renderer extends App {
  emitVerilog(new Renderer, Array("--target-dir", "generated"))
}