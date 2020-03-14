package core
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class RegfileTest(rf: Regfile) extends PeekPokeTester(rf){

   poke(rf.io.id.read1.addr, 0)
   poke(rf.io.id.read2.addr, 0)
   poke(rf.io.wrReg.addr, 0)
   step(1)
   expect(rf.io.id.read1.data, 0)
   expect(rf.io.id.read2.data, 0)

   poke(rf.io.wrReg.addr, 1)
   poke(rf.io.wrReg.data, 11111)
   poke(rf.io.wrReg.we,1)
   step(1)

   poke(rf.io.wrReg.addr, 2)
   poke(rf.io.wrReg.data, 22222)
   poke(rf.io.wrReg.we,1)
   step(1)

   poke(rf.io.wrReg.addr, 3)
   poke(rf.io.wrReg.data, 33333)
   poke(rf.io.wrReg.we,1)
   step(1)

   poke(rf.io.id.read1.addr, 1)
   poke(rf.io.id.read2.addr, 2)
   expect(rf.io.id.read1.data, 11111)
   expect(rf.io.id.read2.data, 22222)
   poke(rf.io.id.read1.addr, 3)
   poke(rf.io.id.read2.addr, 1)
   expect(rf.io.id.read1.data, 33333)
   expect(rf.io.id.read2.data, 11111)

   poke(rf.io.wrReg.addr, 31)
   poke(rf.io.wrReg.data, 12345)
   poke(rf.io.wrReg.we,1)
   step(1)
   poke(rf.io.id.read1.addr, 31)
   expect(rf.io.id.read1.data, 12345)

}

class RegFileTester extends ChiselFlatSpec {
    val args = Array[String]()
    "RegFile module" should "pass test" in {
      iotesters.Driver.execute(args, () => new Regfile()) {
        c => new RegfileTest(c)
      } should be (true)
    }
}
