package core

import chisel3._

 
class regfile extends Module{
    val io = IO(new Bundle{
        val raddr1 = Input(UInt(5.W))
        val rdata1 = Output(UInt(32.W))
        val raddr2 = Input(UInt(5.W))
        val rdata2 = Output(UInt(32.W))
        
        val we = Input(UInt(1.W))  //enable
        val waddr = Input(UInt(5.W)) 
        val wdata = Input(UInt(32.W))   
        val debug = Output(Vec(32, UInt(32.W)))
    })

    val regs = Mem(32, UInt(32.W))
    regs(0.U) := 0.U //zero regs

    //read 
    io.rdata1 := Mux(io.raddr1.orR, regs(io.raddr1), 0.U)
    io.rdata2 := Mux(io.raddr2.orR, regs(io.raddr2), 0.U)
    //write
    when(io.waddr.orR&&io.we.orR){
        regs(io.waddr) := io.wdata
    }
    //debug 
    for (i <- 0 until 32)
        io.debug(i) := regs(i.U)
}   