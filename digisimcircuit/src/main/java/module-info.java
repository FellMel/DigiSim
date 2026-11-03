/**
 *
 */
module de.parresum.digisim.circuit {
   requires java.desktop;
   requires org.apache.logging.log4j;
   requires transitive de.parresum.kicad.parser;
   requires org.apache.commons.lang3;
   requires transitive de.parresum.digisim.lib;
   requires transitive de.parresum.digisim.api;

   exports de.parresum.digisim.parser;
   exports de.parresum.digisim.core;
   exports de.parresum.digisim.model;
   exports de.parresum.digisim.model.graph;

   uses de.parresum.digisim.api.spi.ComponentProvider;
}