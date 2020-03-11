
package core

import chisel3._
import chisel3.util.Valid

class RegRead extends Bundle {
    val addr = Input(UInt(5.W))
    val data = Output(UInt(32.W))

}
class Reg_Rd extends Bundle {
    val read1 = new RegRead()
    val read2 = new RegRead()

}