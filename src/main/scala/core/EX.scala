package core

import chisel3._
import chisel3.util._

class EX extends Module {
    val io = IO(new Bundle{
        val id      = Flipped(new ID_EX)
        val mem     = new EX_MEM

        val flush   = Input(Bool())
    })

    // alu input locked
    //when !rest dealy one cycle and make alu = 0.U.asTypeOf(new ALUOp)
    //otherwise,dealy one cycle and make alu = io.id.aluOp
    val alu = RegNext(io.id.aluOp, init=0.U.asTypeOf(new ALUOp))

    val in1 =   alu.rd1
    val in2 =   alu.rd2
    val op  =   alu.op

    val Out = MuxLookup(op, in1+in2, 
        Seq(
            OptCode.ADD     -> (in1 + in2),
            OptCode.SUB     -> (in1 - in2),
            OptCode.SLT     -> Mux(in1.asSInt < in2.asSInt, 1.U, 0.U),
            OptCode.SLTU    -> Mux(in1 < in2, 1.U, 0.U),
            OptCode.XOR     -> (in1 ^ in2),
            OptCode.OR      -> (in1 | in2),
            OptCode.AND     -> (in1 & in2),
            OptCode.SLL     -> (in1 << in2),
            OptCode.SRL     -> (in1 >> in2),
            OptCode.SRA     -> (in1.asSInt >> in2).asUInt,

            OptCode.MUL     -> (in1 * in2)(31,0),
            OptCode.MULH    ->( in1.asSInt * in2.asSInt)(63,32).asUInt,
            OptCode.MULHU   -> (in1 * in2)(63,32),
            OptCode.MULHSU  -> (in1.asSInt * in2)(63,32).asUInt,
            OptCode.DIV     -> (in1.asSInt / in2.asSInt).asUInt,
            OptCode.DIVU    -> in1 / in2,
            OptCode.REM     -> (in1.asSInt % in2.asSInt).asUInt,
            OptCode.REMU    -> in1 % in2
            
        )
    )


    //memory access

    //csr

}