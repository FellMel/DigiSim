/**
 *
 */
module de.parresum.digisim.app {
   requires java.desktop;
   requires org.apache.logging.log4j;
   requires rxtx;
   requires de.parresum.kicad.parser;
   requires java.prefs;
   requires org.apache.commons.lang3;
   requires de.parresum.digisim.lib;

   opens icons;

   opens lang;
}