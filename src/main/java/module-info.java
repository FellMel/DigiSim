/**
 *
 */
module digisim {
   requires java.desktop;
   requires org.apache.logging.log4j;
   requires rxtx;
   requires de.parresum.kicad.parser;
   requires java.prefs;
   requires org.apache.commons.lang3;

   opens icons;

   opens lang;
}