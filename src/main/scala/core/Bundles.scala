
package core

import chisel3._
import chisel3.util.Valid


//regfile
class RegRead extends Bundle {
    val addr = Input(UInt(5.W))
    val data = Output(UInt(32.W))

}
class Reg_Rd extends Bundle {
    val read1 = new RegRead()
    val read2 = new RegRead()

}

//ram output full io interface

class RAMOutPut extends Bundle{
    val addr  = Output(UInt(32.W))
    val wdata = Output(UInt(32.W))
    val rdata = Input(UInt(32.W))
    //val valid =  Output(Bool())
    val ready = Input(Bool())
}

//if_id with branch ex

class Exception_Bus extends Bundle{
    val valid = Output(Bool())
    val code  = Output(UInt(32.W))
    val value = Output(UInt(32.W))
    val pc    = Output(UInt(32.W))

    val inst_valid = Output(Bool())
}
class IF_ID extends Bundle{
  val except = Output(new Exception_Bus)
  val branch = Input(Valid(UInt(32.W)))
  val ready  = Input(Bool())
  val inst   = Output(UInt(32.W))

  def pc = except.pc
}

//id 2 ex 
class ID_EX extends Bundle{
    val aluOp       = Output(new ALUOp)
    val wrRegOp     = Output(new WrRegOp)
    val wrCSROp     = Output(new WrCSROp)
    val store_data  = Output(UInt(32.W))
    val except      = Output(new Exception_Bus)
    
    val ready       = Input(Bool())
}
//id output to ex_alu
class ALUOp extends Bundle{

    val rd1     =   Output(UInt(32.W))
    val rd2     =   Output(UInt(32.W))
    val op      =   Output(UInt(5.W))
}
//id out to regfile
class WrRegOp extends Bundle{
    val addr    =   Output(UInt(5.W))
    val data    =   Output(UInt(32.W))

    val we      =   Output(Bool())
}
//id 2 CSR write
class WrCSROp extends Bundle{
    val addr    =   Output(UInt(12.W))
    val data    =   Output(UInt(32.W))

    val valid   =   Output(Bool())
}
//id 2 csr
class ID_CSR extends Bundle {
  val addr      =   Output(UInt(12.W))
  val data     =   Input(UInt(32.W))
  val pr        =   Input(UInt(2.W))
}

//EX 2 MEM
class EX_MEM extends Bundle {
    val ram     =   Output(new RAMOutPut)
    val wrRegOp =   Output(new WrRegOp)
    val wrCSROp =   Output(new WrCSROp)
    val except  =   new Exception_Bus

    val ready   =   Input(Bool())
}