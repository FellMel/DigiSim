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

import java.util.Map;
import java.util.Map.Entry;

import de.parresum.digisim.api.CircuitComponent;
import de.parresum.digisim.api.wire.Wire;
import de.parresum.digisim.model.graph.ConnectionView;

/**
 * Hierarchical Sheet within a circuit
 *
 * @author Kai Uwe Bachmann
 */
public class SheetPart implements CircuitComponent {
   /** Name of this part in the scheme */
   private final String name;
   private final Circuit circuit;

   /**
    * @param name
    */
   public SheetPart(Circuit circuit, Map<String, Wire> pins) {
      name = circuit.getName();
      this.circuit = circuit;

      for (Entry<String, Wire> entry : pins.entrySet()) {
         ConnectionView con = circuit.getConection(entry.getKey());
         if (con == null) {
            System.err.println("Pin not found: " + entry.getKey() + " on Part " + name);
            // TODO: report problem
            return;
         }

         con.joinWire(entry.getValue());
      }
   }

   @Override
   public String getLibName() {
      return null;
   }

   @Override
   public void setLibName(String libName) {

   }

}
