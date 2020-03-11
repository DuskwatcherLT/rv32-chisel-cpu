package core
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class regfiletest(rf: regfile) extends PeekPokeTester(rf){

poke(rf.io.raddr1, 0)
   poke(rf.io.raddr2, 0)
   poke(rf.io.waddr, 0)
   step(1)
   expect(rf.io.rdata1, 0)
   expect(rf.io.rdata2, 0)

    poke(rf.io.waddr, 1)
   poke(rf.io.wdata, 11111)
   poke(rf.io.we,1)
   step(1)

    poke(rf.io.waddr, 2)
   poke(rf.io.wdata, 22222)
    poke(rf.io.we,1)
   step(1)

   poke(rf.io.waddr, 3)
   poke(rf.io.wdata, 33333)
    poke(rf.io.we,1)
   step(1)

   poke(rf.io.raddr1, 1)
   poke(rf.io.raddr2, 2)
   expect(rf.io.rdata1, 11111)
   expect(rf.io.rdata2, 22222)
   poke(rf.io.raddr1, 3)
   poke(rf.io.raddr2, 1)
   expect(rf.io.rdata1, 33333)
   expect(rf.io.rdata2, 11111)

    poke(rf.io.waddr, 31)
   poke(rf.io.wdata, 12345)
    poke(rf.io.we,1)
   step(1)
   poke(rf.io.raddr1, 31)
   expect(rf.io.rdata1, 12345)

}

class RegFileTester extends ChiselFlatSpec {
    val args = Array[String]()
    "RegFile module" should "pass test" in {
      iotesters.Driver.execute(args, () => new regfile()) {
        c => new regfiletest(c)
      } should be (true)
    }
}
