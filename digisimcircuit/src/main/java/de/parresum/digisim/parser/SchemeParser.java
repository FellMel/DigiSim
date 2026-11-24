/*
 *     Copyright 2026 Parresum Soft @ http://parresum.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.parresum.digisim.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import de.parresum.digisim.api.CircuitComponent;
import de.parresum.digisim.api.spi.ComponentProvider;
import de.parresum.digisim.core.Circuit;
import de.parresum.digisim.model.AbstractNetElement;
import de.parresum.digisim.model.CircuitPin;
import de.parresum.digisim.model.LibPart;
import de.parresum.digisim.model.NetConnection;
import de.parresum.digisim.model.NetJunction;
import de.parresum.digisim.model.NetPart;
import de.parresum.digisim.model.NetPin;
import de.parresum.digisim.model.NetPoint;
import de.parresum.digisim.model.NetValue;
import de.parresum.digisim.model.NetWire;
import de.parresum.digisim.model.SheetPart;
import de.parresum.digisim.model.graph.ConnectionView;
import de.parresum.digisim.model.graph.GlobalConnectionView;
import de.parresum.digisim.model.graph.HierarchicalConnectionView;
import de.parresum.digisim.model.graph.NetView;
import de.parresum.digisim.model.graph.PartView;
import de.parresum.kicad.parser.eescheme.Junction;
import de.parresum.kicad.parser.eescheme.Schematic;
import de.parresum.kicad.parser.eescheme.Sheet;
import de.parresum.kicad.parser.eescheme.Symbol;
import de.parresum.kicad.parser.eescheme.Wire;
import de.parresum.kicad.parser.eescheme.label.GlobalLabel;
import de.parresum.kicad.parser.eescheme.label.HierarchicalLabel;
import de.parresum.kicad.parser.library.LibSymbol;
import de.parresum.kicad.parser.sexpr.SExpressionParser;

/**
 * Parser to read a scheme and simulate it
 *
 * @author Kai Uwe Bachmann
 */
public class SchemeParser {
   private final static Logger LOG = LogManager.getLogger(SchemeParser.class);

   public static Circuit readFile(File file) {
      try (FileReader infile = new FileReader(file)) {

         final Schematic result = SExpressionParser.parse(infile, new Schematic());

         return parseCircuit(file.getParentFile(), result);
      } catch (IOException e) {
         throw new IllegalStateException("Can't read input file", e);
      }
   }

   public static Circuit parseCircuit(File baseDir, Schematic scheme) {

      // get used Lib symbols ...
      Map<String, LibPart> lib = new HashMap<>();
      for (LibSymbol sym : scheme.getLibSymbols().getSymbols()) {
         LibPart part = new LibPart(sym);
         lib.put(part.getName(), part);
      }

      // first extract wires, pins and junctions
      LOG.info("collecting net elements ...");
      List<AbstractNetElement> elements = new ArrayList<AbstractNetElement>();
      for (Wire wire : scheme.getWires()) {
         elements.add(new NetWire(wire));
      }

      for (Junction junction : scheme.getJunctions()) {
         elements.add(new NetJunction(junction));
      }

      for (GlobalLabel label : scheme.getGlobalLabels()) {
         elements.add(new NetConnection(label));
      }

      for (HierarchicalLabel label : scheme.getHierarchicalLabels()) {
         elements.add(new NetConnection(label));
      }

      List<NetPart> parts = new ArrayList<NetPart>();
      for (Symbol symbol : scheme.getSymbols()) {
         String symbolName = symbol.getLibName();
         if (Strings.isEmpty(symbolName)) {
            symbolName = symbol.getLibraryIdentifier();
         }
         LibPart libPart = getLibSymbol(symbolName, lib);
         NetPart part = new NetPart(symbol, libPart);
         parts.add(part);

         for (CircuitPin item : part.getPins()) {
            elements.add((AbstractNetElement) item);
         }
      }

      List<SheetPart> sheets = new ArrayList<>();
      for (Sheet sheet : scheme.getSheets()) {
         SheetPart part = new SheetPart(sheet);
         sheets.add(part);
         for (CircuitPin item : part.getPins()) {
            elements.add((AbstractNetElement) item);
         }

      }
      LOG.info("Found " + elements.size() + " elements");

      // fixing Junctions / crossing wires
      // if netJunction in middle of NetWire, there is a connection
      for (AbstractNetElement item : elements) {
         if (item instanceof NetJunction) {
            for (AbstractNetElement second : elements) {
               if (second instanceof NetWire) {
                  ((NetWire) second).split(item.getPoints().getFirst());
               }
            }
         }
      }

      // now group them by netlists
      int netCnt = 1;
      LOG.info("grouping nets ...");
      Map<String, List<AbstractNetElement>> netLists = new HashMap<>();
      while (!elements.isEmpty()) {
         List<AbstractNetElement> netList = new ArrayList<>();
         AbstractNetElement root = elements.remove(0);

         netList.add(root);
         boolean found = false;
         do {
            found = false;
            Iterator<AbstractNetElement> it = elements.iterator();
            while (it.hasNext()) {
               AbstractNetElement srcitem = it.next();
               List<NetPoint> srcPoints = srcitem.getPoints();

               for (AbstractNetElement netItem : netList) {
                  if (netItem.containsPoint(srcPoints)) {
                     it.remove();
                     netList.add(srcitem);
                     found = true;
                     break;
                  }
               }
            }
         } while (found);

         if (netList.size() > 1) { // only, if there more than one participants. Else the pin is "Not Connected"
            netLists.put("net " + netCnt, netList);
         }
         netCnt++;
      }

      LOG.info("Found " + netLists.size() + " nets");

      // print result
      for (Entry<String, List<AbstractNetElement>> entry : netLists.entrySet()) {
         LOG.info("------------------------------------");
         LOG.info(entry.getKey());
         for (AbstractNetElement item : entry.getValue()) {
            item.print();
         }
         LOG.info("");
      }
      String title = "";
      if (scheme.getTitleBlock() != null) {
         title = scheme.getTitleBlock().getTitle();
      }
      return createCircuit(baseDir, title, lib, parts, sheets, netLists);
   }

   private static Circuit createCircuit(File baseDir, String name, Map<String, LibPart> lib, List<NetPart> parts,
         List<SheetPart> sheets, Map<String, List<AbstractNetElement>> netLists) {

      Circuit circuit = new Circuit(name);

      ServiceLoader<ComponentProvider> serviceLoader = ServiceLoader.load(ComponentProvider.class);
      try {
         Map<String, NetView> views = new HashMap<>();

         // first create wires
         for (Entry<String, List<AbstractNetElement>> net : netLists.entrySet()) {
            // TODO: handle special kind of wires (OC, Tri-State, ...)
            // TODO: handle bus
            de.parresum.digisim.api.wire.Wire wire = new de.parresum.digisim.api.wire.Wire(net.getKey());
            NetView view = new NetView(net.getKey(), wire);
            views.put(net.getKey(), view);
            circuit.addWire(view);
         }

         // now create connectors and pin-Lists
         // Map of <ElementName, Map<PinNumber, Wire>>
         Map<String, Map<String, de.parresum.digisim.api.wire.Wire>> pinMap = new HashMap<>();
         for (Entry<String, List<AbstractNetElement>> net : netLists.entrySet()) {
            NetView view = views.get(net.getKey());

            for (AbstractNetElement item : net.getValue()) {
               view.addElement(item);

               if (item instanceof NetConnection) {
                  NetConnection connection = (NetConnection) item;
                  String conName = connection.getName();

                  ConnectionView partConnection;
                  if (connection.isHierarchical()) {
                     partConnection = new HierarchicalConnectionView(connection);
                  } else {
                     partConnection = new GlobalConnectionView(connection);
                  }
                  partConnection.setWire(view.getWire());
                  circuit.addConnection(conName, partConnection);
               } else if (item.isPin()) {
                  NetPin pin = (NetPin) item;
                  String partName = pin.getPart();
                  String pinNumber = pin.getPinNr();

                  Map<String, de.parresum.digisim.api.wire.Wire> entry = pinMap.computeIfAbsent(partName,
                        _ -> new HashMap<>());
                  entry.put(pinNumber, view.getWire());
               }
            }
         }

         // create components, now
         for (NetPart part : parts) {
            CircuitComponent component = null;
            String libName = part.getLib();
            String elementName = part.getName();
            Map<String, de.parresum.digisim.api.wire.Wire> pins = pinMap.get(elementName);
            List<NetValue> values = part.getValues();
            Map<String, String> parameter = new HashMap<>();
            for (NetValue val : values) {
               parameter.put(val.getName(), val.getValue());
            }

            for (ComponentProvider provider : serviceLoader) {
               if (provider.isResponsible(libName)) {
                  component = provider.create(libName, elementName, pins, parameter);
                  break;
               }

            }
            if (component == null) {
               LOG.error("Can't create component {} - {}", libName, elementName);
            }
            PartView view = new PartView(part.getName(), component, part);
            circuit.addPart(part.getName(), view);
         }

         // finally create sub sheets
         for (SheetPart part : sheets) {
            String filename = part.getFilename();

            Circuit sub = readFile(new File(baseDir, filename));

            Map<String, de.parresum.digisim.api.wire.Wire> pins = pinMap.get(part.getName());
            de.parresum.digisim.core.SheetPart sp = new de.parresum.digisim.core.SheetPart(sub, pins);
            PartView view = new PartView(part.getName(), sp, part);
            circuit.addPart(part.getName(), view);
         }

      } catch (IllegalArgumentException e) {
         LOG.error("Error in imlementation ...", e);
         System.exit(-1);
      }
      return circuit;
   }

   private static LibPart getLibSymbol(String libSymbol, Map<String, LibPart> lib) {

      LibPart part = lib.get(libSymbol);
      if (part != null) {
         return part;
      }

      throw new IllegalStateException("Can't find lib entry for " + libSymbol);
   }

}
