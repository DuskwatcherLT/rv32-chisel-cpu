package core

import chisel3._
import chisel3.util._


class IF extends Module{ 
    val io = IO(new Bundle{
        val id = new IF_ID
        val rom = new RAMOutPut
    })


    val pc       = RegInit(PCConst.PC_INIT)
    val branch   = RegInit(0.U.asTypeOf(Valid(UInt(32.W))))
    val inst     = RegInit(0.U.asTypeOf(Valid(UInt(32.W))))

    val rom_inst = Wire(Valid(UInt(32.W)))
    rom_inst.valid := io.rom.ready
    rom_inst.bits := io.rom.rdata

    val cu_branch = Mux(io.id.branch.valid, io.id.branch, branch)
    val cu_inst   = Mux(inst.valid, inst ,rom_inst)

    //branch & inst
    when(io.id.branch.valid){
        branch := io.id.branch
    }
    when(!inst.valid && io.rom.ready){
        inst.valid := true.B
        inst.bits := io.rom.rdata
    }
    
    val dealy = !cu_inst.valid || !io.id.ready
    //when rom.ready status change
    when(!dealy){
        pc := PriorityMux(Seq((cu_branch.valid, cu_branch.bits),(true.B, pc + 4.U)))
        branch := 0.U.asTypeOf(Valid(UInt(32.W))) //clear
        inst := 0.U.asTypeOf(Valid(UInt(32.W))) //clear
    }
    //fetch 
    io.rom.addr := pc  
    io.rom.wdata := 0.U

    //default

    io.id.inst := PCConst.NOP_INST
    io.id.except := 0.U.asTypeOf(new Exception_Bus)

    when(!(dealy || cu_branch.valid)){
        io.id.inst := cu_inst.bits
        io.id.except.pc := pc
        io.id.except.inst_valid := true.B
    }
}
