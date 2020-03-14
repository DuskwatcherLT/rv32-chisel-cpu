package core_

import chisel3._
import chisel3.util._


class ID extends Module{
    val io = IO(new Bundle {
        val if_  = Flipped(new IF_ID)
        val reg = Flipped(new Reg_Rd)
        val ex  =   new ID_EX
        val csr =   new ID_CSR

        val flush   = Input(Bool())

        //connect

        val exWrRegOp   =   Flipped(new WrRegOp)
        val memWrRegOp  =   Flipped(new WrRegOp)

        val exWrCSROp   =   Flipped(new WrCSROp)
        val memWrCSROp  =   Flipped(new WrCSROp)
    })
    //
    val inst    =   RegInit(PCConst.NOP_INST)
    val except  =   RegInit(0.U.asTypeOf(new Exception_Bus))
    val pc      =   except.pc

    //when id  is stalling, this cycle does not executed instruction
    //so , on the next cycle ,id has same instruction.
    //and ,if get a nop_inst when it sees  stall
    //id will not undata itself

    val stall   =   Wire(Bool())
    val flush   =   io.flush
    when(flush){
        inst    :=  PCConst.NOP_INST
        except  :=  0.U.asTypeOf(new Exception_Bus)
    }.elsewhen(stall){
        inst    :=  inst
        except  :=  except
    }.otherwise{
        inst    :=  io.if_.inst
        except  :=  io.if_.except
    }

    //decode
    val decodeRes   =   ListLookup(inst, DecoderTable.defaultDec, DecoderTable.decMap)
    val instType    =   decodeRes(DecoderTable.TYPE) //Map[0]
    val rs1         =   inst(19, 15)
    val rs2         =   inst(24, 20)
    val rd          =   inst(11, 7)
    val csr         =   inst(31, 20)
    val imm         =   Wire(SInt(32.W))

    

}