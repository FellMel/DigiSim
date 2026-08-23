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

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.core.Circuit;
import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.gui.SimulationWindow;
import de.parresum.kicad.parser.eescheme.Junction;
import de.parresum.kicad.parser.eescheme.Schematic;
import de.parresum.kicad.parser.eescheme.Symbol;
import de.parresum.kicad.parser.eescheme.Wire;
import de.parresum.kicad.parser.sexpr.SExpressionParser;

/**
 * Parser to read a scheme and simulate it
 *
 * @author Kai Uwe Bachmann
 */
public class Parser {
   private final static Logger LOG = LogManager.getLogger(Parser.class);

   private static Map<String, LibPart> lib = new HashMap<>();
   private static List<NetPart> parts = new ArrayList<NetPart>();
   private static Map<String, List<AbstractNetElement>> netLists = new HashMap<>();

   public static void main(String[] args) {
      LOG.info("Start parsing");
      // TODO: switch to file chooser
//      String filename = "src/main/resources/kicad/DigiSim/DigiSim.kicad_sch";
//      String filename = "src/main/resources/kicad/DigiSim/d-flipflop.kicad_sch";
      String filename = "src/main/resources/kicad/DigiSim/t-flipflop.kicad_sch";

      String circuitName = extractElements(filename);
      Circuit circuit = createCircuit(circuitName);

      // Create test window with inputs and outputs
      createSimWindow(circuit);

   }

   private static String extractElements(String filename) {
      try (FileReader infile = new FileReader(filename)) {

         final Schematic result = SExpressionParser.parse(infile, new Schematic());

         // get used Lib symbols ...
         for (Symbol sym : result.getLibSymbols().getSymbols()) {
            LibPart part = new LibPart(sym);
            lib.put(part.getName(), part);
         }

         // first extract wires, pins and junctions
         LOG.info("collecting net elements ...");
         List<AbstractNetElement> elements = new ArrayList<AbstractNetElement>();
         for (Wire wire : result.getWires()) {
            elements.add(new NetWire(wire));
         }

         for (Junction junction : result.getJunctions()) {
            elements.add(new NetJunction(junction));
         }

         for (Symbol symbol : result.getSymbols()) {
            LibPart libPart = getLibSymbol(symbol.getLibraryIdentifier());
            NetPart part = new NetPart(symbol, libPart);
            parts.add(part);

            elements.addAll(part.getPins());
         }
         LOG.info("Found " + elements.size() + " elements");

         // now group them by netlists
         int netCnt = 1;
         LOG.info("grouping nets ...");
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

            netLists.put("net " + netCnt, netList);
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
         return result.getTitleBlock().getTitle();

      } catch (IOException e) {
         throw new IllegalStateException("Can't read input file", e);
      }

   }

   private static Circuit createCircuit(String name) {

      Circuit circuit = new Circuit(name);

      // first create elements
      try {
         for (NetPart part : parts) {
            CircuitPart sp = PartHelper.createPart(part);
            circuit.addPart(part.getName(), sp);
         }

         // now create wires and connect them to elements
         for (Entry<String, List<AbstractNetElement>> net : netLists.entrySet()) {
            de.parresum.digisim.core.wire.Wire wire = new de.parresum.digisim.core.wire.Wire(net.getKey());
            circuit.addWire(wire);
            for (AbstractNetElement item : net.getValue()) {
               if (item.isPin()) {
                  NetPin pin = (NetPin) item;
                  String partName = pin.getPart();
                  String pinNumber = pin.getPinNr();

                  CircuitPart part = circuit.getPart(partName);
                  if (part == null) {
                     throw new IllegalStateException("Unknown part with name " + partName);
                  }

                  PartHelper.join(wire, part, pinNumber);
               }
            }
         }
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
         LOG.error("Error in imlementation ...", e);
         System.exit(-1);
      }
      return circuit;
   }

   private static void createSimWindow(Circuit circuit) {
      SimulationWindow wnd = new SimulationWindow(circuit);
   }

   private static LibPart getLibSymbol(String libSymbol) {

      LibPart part = lib.get(libSymbol);
      if (part != null) {
         return part;
      }

      throw new IllegalStateException("Can't find lib entry for " + libSymbol);

   }

}
