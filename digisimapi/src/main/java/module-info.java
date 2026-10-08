/**
 *
 */
module de.parresum.digisim.api {
   // requires java.desktop;
   requires org.apache.logging.log4j;
   requires java.desktop;

   exports de.parresum.digisim.api;
   exports de.parresum.digisim.api.spi;
   exports de.parresum.digisim.api.annotations;
   exports de.parresum.digisim.api.wire;

}