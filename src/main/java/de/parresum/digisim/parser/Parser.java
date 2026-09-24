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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.core.Circuit;
import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.gui.SimulationWindow;
import de.parresum.digisim.model.AbstractNetElement;
import de.parresum.digisim.model.LibPart;
import de.parresum.digisim.model.NetConnection;
import de.parresum.digisim.model.NetJunction;
import de.parresum.digisim.model.NetPart;
import de.parresum.digisim.model.NetPin;
import de.parresum.digisim.model.NetPoint;
import de.parresum.digisim.model.NetWire;
import de.parresum.digisim.model.graph.ConnectionView;
import de.parresum.digisim.model.graph.NetView;
import de.parresum.digisim.model.graph.PartView;
import de.parresum.kicad.parser.eescheme.Junction;
import de.parresum.kicad.parser.eescheme.Schematic;
import de.parresum.kicad.parser.eescheme.Symbol;
import de.parresum.kicad.parser.eescheme.Wire;
import de.parresum.kicad.parser.eescheme.label.GlobalLabel;
import de.parresum.kicad.parser.library.LibSymbol;
import de.parresum.kicad.parser.sexpr.SExpressionParser;

/**
 * Parser to read a scheme and simulate it
 *
 * @author Kai Uwe Bachmann
 */
public class Parser {
   private final static Logger LOG = LogManager.getLogger(Parser.class);

   public static void main(String[] args) {
      LOG.info("Start parsing");
      File file = null;

      if (args.length > 0) {
         String filename = args[0];
         file = new File(filename);
      } else {
         JFileChooser fileChooser = new JFileChooser();

         // Optional: Startverzeichnis festlegen (z. B. Benutzerordner)
         fileChooser.setCurrentDirectory(new File("src/main/resources/kicad/DigiSim"));
         fileChooser.setAcceptAllFileFilterUsed(false);
         fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
               return "KiCad Scheme (*.kicad_sch)";
            }

            @Override
            public boolean accept(File f) {
               if (f.isDirectory()) {
                  return true;
               } else {
                  return f.getName().toLowerCase().endsWith(".kicad_sch");
               }
            }
         });

         // 2. Dialog anzeigen (parent ist z. B. ein JFrame oder null)
         int result = fileChooser.showOpenDialog(null);

         // 3. Ergebnis auswerten
         if (result != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
         }
         file = fileChooser.getSelectedFile();
      }

      Circuit circuit = readFile(file);

      // Create test window with inputs and outputs
      createSimWindow(circuit);

   }

   private static Circuit readFile(File file) {
      try (FileReader infile = new FileReader(file)) {

         final Schematic result = SExpressionParser.parse(infile, new Schematic());

         return parseCircuit(result);
      } catch (IOException e) {
         throw new IllegalStateException("Can't read input file", e);
      }
   }

   public static Circuit parseCircuit(Schematic scheme) {

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

      List<NetPart> parts = new ArrayList<NetPart>();
      for (Symbol symbol : scheme.getSymbols()) {
         LibPart libPart = getLibSymbol(symbol.getLibraryIdentifier(), lib);
         NetPart part = new NetPart(symbol, libPart);
         parts.add(part);

         elements.addAll(part.getPins());
      }
      LOG.info("Found " + elements.size() + " elements");

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
      return createCircuit(scheme.getTitleBlock().getTitle(), lib, parts, netLists);

   }

   private static Circuit createCircuit(String name, Map<String, LibPart> lib, List<NetPart> parts,
         Map<String, List<AbstractNetElement>> netLists) {

      Circuit circuit = new Circuit(name);

      // first create elements
      try {
         for (NetPart part : parts) {
            CircuitPart sp = PartHelper.createPart(part);
            PartView view = new PartView(part.getName(), sp, part);
            circuit.addPart(part.getName(), view);
         }

         // now create wires and connect them to elements
         for (Entry<String, List<AbstractNetElement>> net : netLists.entrySet()) {
            de.parresum.digisim.core.wire.Wire wire = new de.parresum.digisim.core.wire.Wire(net.getKey());
            NetView view = new NetView(net.getKey(), wire);
            circuit.addWire(view);
            for (AbstractNetElement item : net.getValue()) {
               view.addElement(item);
               if (item.isPin()) {
                  NetPin pin = (NetPin) item;
                  String partName = pin.getPart();
                  String pinNumber = pin.getPinNr();

                  PartView part = circuit.getPart(partName);
                  if (part == null) {
                     throw new IllegalStateException("Unknown part with name " + partName);
                  }

                  PartHelper.join(wire, part.getPart(), pinNumber);
               } else if (item instanceof NetConnection) {
                  NetConnection connection = (NetConnection) item;
                  String conName = connection.getName();

                  ConnectionView partConnection = new ConnectionView(connection);
                  partConnection.setWire(wire);
                  circuit.addConnection(conName, partConnection);
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

   private static LibPart getLibSymbol(String libSymbol, Map<String, LibPart> lib) {

      LibPart part = lib.get(libSymbol);
      if (part != null) {
         return part;
      }

      throw new IllegalStateException("Can't find lib entry for " + libSymbol);

   }

}
