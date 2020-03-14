package core

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class IFTest(t: IF) extends PeekPokeTester(t){

  def reset_poke_init(): Unit = {
    reset(10) // press rst for more than a while pls
    poke(t.io.id.branch.valid, 0)
    poke(t.io.id.ready, 1)
    poke(t.io.rom.ready, 1)
  }
  val PC_BASE = 0x80000000L
  reset_poke_init()
  for (i <- 0 until 7) {
    val some_inst = i*0x11110000 + (i+1)*0x00001111
    poke(t.io.rom.rdata, some_inst)
    expect(t.io.id.pc, PC_BASE + i*4)
    expect(t.io.rom.addr, PC_BASE + i*4)
    //expect(t.io.rom.mode, RAMMode.LW)
    expect(t.io.id.inst, some_inst)
    step(1)
  }
}

class IFTester extends ChiselFlatSpec {
    val args = Array[String]()
    "IF module" should "not tested now " in {
      iotesters.Driver.execute(args, () => new IF()) {
        c => new IFTest(c)
      } should be (true)
    }
}
