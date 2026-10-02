/**
 *
 */
module de.parresum.digisim.lib {
   requires java.desktop;
   requires org.apache.logging.log4j;

   exports de.parresum.digisim.annotations;
   exports de.parresum.digisim.lib;
   exports de.parresum.digisim.lib.flipflop;
   exports de.parresum.digisim.lib.gates;
   exports de.parresum.digisim.lib.io;
   exports de.parresum.digisim.lib.wire;

}