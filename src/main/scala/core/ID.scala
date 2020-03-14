package core

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

    //connect with regfile

    io.reg.read1.addr :=    rs1
    io.reg.read2.addr :=    rs2
    io.csr.addr       :=    csr

    //get register data
    val rs1Data = PriorityMux(Seq(
        (rs1 === 0.U,               0.U) ,              //zero register
        (rs1 === io.exWrRegOp.addr, io.exWrRegOp.data), //connect with ex
        (rs1 === io.memWrRegOp.addr,io.memWrRegOp.data),//connect with men
        (true.B,                    io.reg.read1.data)  //connect wiht regfile
    ))

     val rs2Data = PriorityMux(Seq(
        (rs2 === 0.U,               0.U),                //zero register
        (rs2 === io.exWrRegOp.addr, io.exWrRegOp.data),  //connect with ex
        (rs2 === io.memWrRegOp.addr,io.memWrRegOp.data), //connect with men
        (true.B,                    io.reg.read2.data)   //connect wiht regfile
    ))

    val csrData =   PriorityMux(Seq(
        (csr === io.exWrCSROp.addr&&io.exWrCSROp.valid,   io.exWrCSROp.data),
        (csr === io.memWrCSROp.addr&&io.memWrCSROp.valid, io.memWrCSROp.data),
        (true.B,                                          io.csr.data)
    ))


    //initialization
    io.if_.branch := 0.U.asTypeOf(Valid(UInt(32.W)))
    io.ex.aluOp   := 0.U.asTypeOf(new ALUOp)
    io.ex.aluOp.op := decodeRes(DecoderTable.OPT) //DecoderTableList[1]
    io.ex.wrCSROp := 0.U.asTypeOf(new WrCSROp)
    io.ex.wrRegOp := 0.U.asTypeOf(new WrRegOp)
    io.ex.except := except
    io.ex.except.inst_valid := except.inst_valid && !stall
    io.ex.store_data := 0.U
    imm := 0.S


    //Instruction classification
    val rs1Eff = Seq(InstType.B, InstType.I, InstType.S ,InstType.IS, InstType.R)
    val rs2Eff = Seq(InstType.R, InstType.S, InstType.B)


    //when it is jump inst
    def checkAndJump(target: UInt): Unit = {
        when(target(1,0).orR) { // misaligned
             when(!except.valid) {
                io.ex.except.valid := true.B
                io.ex.except.value := target
                io.ex.except.code  := Const.InstAddressMis
            }
        }.otherwise {
            io.if_.branch.bits  := target
            io.if_.branch.valid := true.B
        }
    }

    //core
    when(stall){
        //clear
        io.ex.wrRegOp.addr   := 0.U  
        io.ex.aluOp.op       := OptCode.ADD
        io.if_.branch.valid  := false.B
        io.if_.ready         := false.B
    }.otherwise{
        io.if_.ready         := true.B
        switch(instType){
            is(InstType.R){
                io.ex.aluOp.rd1     := rs1Data
                io.ex.aluOp.rd2     := rs2Data
                io.ex.wrRegOp.addr  := rd
            }
            is(InstType.I){
                imm                 :=  inst(31,20).asSInt
                io.ex.aluOp.rd1     :=  rs1Data
                io.ex.aluOp.rd2     :=  imm.asUInt
                io.ex.wrRegOp.addr  :=  rd
                //i type has a different inst is jalr
                when(decodeRes(DecoderTable.OPT) === OptCode.JALR) {
                    //t =pc+4; pc=(x[rs1]+sext(offset))&~1; x[rd]=t
                    checkAndJump((imm.asUInt + rs1Data) & (~ 1.U(32.W)))
                    io.ex.aluOp.rd1     :=  pc
                    io.ex.aluOp.rd2     :=  4.U
                    io.ex.aluOp.op      :=  OptCode.ADD
                }

            }
            is(InstType.IS){
                imm                 :=  inst(24,20).asSInt
                io.ex.aluOp.rd1     :=  rs1Data
                io.ex.aluOp.rd2     :=  imm.asUInt
                io.ex.wrRegOp.addr  :=  rd
            }
            is(InstType.S){
                imm             :=  Cat(inst(31,25),inst(11,7)).asSInt
                io.ex.aluOp.rd1 :=  rs1Data
                io.ex.aluOp.rd2 :=  imm.asUInt
                io.ex.store_data:=  rs2Data
            }
            is(InstType.BAD) {
                when(!except.valid) {
                io.ex.except.valid := true.B
                io.ex.except.value := inst
                io.ex.except.code  := Const.IllegalInst
                }
            }
        }
    }

}