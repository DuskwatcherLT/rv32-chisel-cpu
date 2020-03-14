package core

import chisel3._
import chisel3.util._

object PCConst{
    val PC_INIT = "h_8000_0000".U(32.W)
    val NOP_INST = "h_8000_0013".U(32.W)
}   