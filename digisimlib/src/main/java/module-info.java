/**
 *
 */
module de.parresum.digisim.lib {
   requires java.desktop;
   requires org.apache.commons.io;
   requires org.apache.logging.log4j;
   requires transitive de.parresum.digisim.api;

   exports de.parresum.digisim.lib;
   exports de.parresum.digisim.lib.flipflop;
   exports de.parresum.digisim.lib.gates;
   exports de.parresum.digisim.lib.io;

   opens kicad;

   provides de.parresum.digisim.api.spi.ComponentProvider with de.parresum.digisim.lib.impl.LibComponentProviderImpl;
}