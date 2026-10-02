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
package de.parresum.digisim.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.parresum.digisim.lib.CircuitPart;
import de.parresum.digisim.lib.InputPart;
import de.parresum.digisim.lib.OutputPart;
import de.parresum.digisim.lib.io.Input;
import de.parresum.digisim.lib.io.Output;
import de.parresum.digisim.model.graph.ConnectionView;
import de.parresum.digisim.model.graph.NetView;
import de.parresum.digisim.model.graph.PartView;

/**
 * A circuit to simulate
 *
 * @author Kai Uwe Bachmann
 */
public class Circuit {
   /** Name of the circuit */
   private final String name;

   /** Map of all parts within the circuit */
   private final Map<String, PartView> parts = new HashMap<>();

   /** Map of all wires within the circuit */
   private final Map<String, NetView> wires = new HashMap<>();

   private final Map<String, ConnectionView> connections = new HashMap<>();

   /** List of input components to show in the simulation GUI */
   private final List<InputPart> inputs = new ArrayList<>();

   /** List of output components to show in the simulation GUI */
   private final List<OutputPart> outputs = new ArrayList<>();

   /**
    * Creates a new circuit
    *
    * @param name name of the circuit
    */
   public Circuit(String name) {
      super();
      this.name = name;
   }

   /**
    * gets the name of the circuit
    *
    * @return name of the circuit
    */
   public String getName() {
      return name;
   }

   /**
    * Adds a part to the circuit
    *
    * @param name name of the part to add
    * @param part part to add
    */
   public void addPart(String name, PartView part) {
      parts.put(name, part);
      CircuitPart element = part.getPart();
      if (element instanceof OutputPart) {
         outputs.add((OutputPart) element);
      } else if (element instanceof InputPart) {
         inputs.add((InputPart) element);
      }
   }

   public void addConnection(String name, ConnectionView connection) {
      connections.put(name, connection);

      switch (connection.getType()) {
         case INPUT:
            inputs.add(new Input(formatConnectionName(connection.getName()), connection.getWire()));
            break;
         case OUTPUT:
            outputs.add(new Output(formatConnectionName(connection.getName()), connection.getWire()));
            break;
         case OPEN_COLLECTOR:
         case TRI_STATE:
            // TODO: TriState-Input
            inputs.add(new Input(formatConnectionName(connection.getName()), connection.getWire()));
            outputs.add(new Output(formatConnectionName(connection.getName()), connection.getWire()));
            break;
         case UNKNOWN:
         default:
            // TODO: what kind of connection is this ????

      }

   }

   private String formatConnectionName(String name) {
      name = name.replace("{", "").replace("}", "");
      name = name.replace("_", "").replace("^", "");
      return name;
   }

   /**
    * Gets a dedicated part of the circuit
    *
    * @param name name of the part to get
    * @return the part or null, if not available
    */
   public PartView getPart(String name) {
      return parts.get(name);
   }

   public Collection<PartView> getParts() {
      return parts.values();
   }

   /**
    * Adds a wire to the circuit
    *
    * @param wire wire to add
    */
   public void addWire(NetView wire) {
      wires.put(wire.getName(), wire);

   }

   public Collection<NetView> getViews() {
      return wires.values();
   }

   public Collection<ConnectionView> getConnections() {
      return connections.values();
   }

   public ConnectionView getConection(String name) {
      return connections.get(name);
   }

   /**
    * Gets the list of input elements
    *
    * @return input elements
    */
   public List<InputPart> getInputs() {
      return inputs;
   }

   /**
    * Gets the list of output elements
    *
    * @return output elements
    */
   public List<OutputPart> getOutputs() {
      return outputs;
   }

//   public void start() {
//      for (Wire w : wires.values()) {
//         w.set(State.HIGH);
//         w.set(State.LOW);
//      }
//   }

}
