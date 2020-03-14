package core

import chisel3._

 
class Regfile extends Module{
    val io = IO(new Bundle{
        val id = new Reg_Rd()
        /*val we = Input(UInt(1.W))  //enable
        val waddr = Input(UInt(5.W)) 
        val wdata = Input(UInt(32.W))   */
        val wrReg = Flipped(new WrRegOp)
        val debug = Output(Vec(32, UInt(32.W)))
    })

    val regs = Mem(32, UInt(32.W))
    regs(0.U) := 0.U //zero regs

    io.id.read1.data := Mux(io.id.read1.addr.orR, regs(io.id.read1.addr), 0.U)
    io.id.read2.data := Mux(io.id.read2.addr.orR, regs(io.id.read2.addr), 0.U)
    when(io.wrReg.addr.orR&&io.wrReg.we.orR){
        regs(io.wrReg.addr) := io.wrReg.data
    }
    //debug 
    for (i <- 0 until 32)
        io.debug(i) := regs(i.U)
}   