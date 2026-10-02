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

import de.parresum.digisim.annotations.PartConnector;
import de.parresum.digisim.lib.CircuitPart;
import de.parresum.digisim.lib.wire.Wire;
import de.parresum.digisim.model.graph.ConnectionView;

/**
 * Hierarchical Sheet within a circuit
 *
 * @author Kai Uwe Bachmann
 */
public class SheetPart implements CircuitPart {
   /** Name of this part in the scheme */
   private final String name;
   private final Circuit circuit;

   /**
    * @param name
    */
   public SheetPart(Circuit circuit) {
      name = circuit.getName();
      this.circuit = circuit;
   }

   @Override
   public String getLibName() {
      return null;
   }

   @Override
   public void setLibName(String libName) {

   }

   @Override
   public void joinWire(Wire wire, String pinNumber, PartConnector connector) {
      ConnectionView con = circuit.getConection(pinNumber);
      if (con == null) {
         System.err.println("Pin not found: " + pinNumber + " on Part " + name);
         // TODO: report problem
         return;
      }

      con.joinWire(wire);
   }

}
