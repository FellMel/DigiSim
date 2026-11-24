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

package de.parresum.digisim.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.api.wire.Wire;

/**
 * Base Element of most parts.
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractComponent implements CircuitComponent {
   /** Name of this part in the scheme */
   private final String name;

   /** name of the lib, the part is defined in */
   private String libName;

   // no global logger to select logging of particular gates
   protected final Logger log;

   public AbstractComponent(String name) {
      super();
      this.name = name;
      if (name != null && !name.isBlank()) {
         String logName = name.replace(" ", "_");
         log = LogManager.getLogger(logName);
      } else {
         log = LogManager.getLogger(this.getClass());
      }
   }

   @Override
   public String toString() {
      return name;
   }

   @Override
   public String getLibName() {
      return libName;
   }

   @Override
   public void setLibName(String libName) {
      this.libName = libName;
   }

   // ------------------------------- o -------------------------------
   protected boolean getState(Wire wire) {
      if (wire == null) {
         return false;
      }

      return wire.get() == State.HIGH;
   }

   protected boolean getStateOc(Wire wire) {
      if (wire == null) {
         return false;
      }

      return wire.get() == State.OPEN;
   }

   protected void setState(Wire wire, boolean val) {
      if (wire != null) {
         wire.set(val ? State.HIGH : State.LOW);
      }
   }

   protected void setState(Wire wire, boolean val, boolean oc) {
      if (wire != null) {
         wire.set(val ? (oc ? State.OPEN : State.HIGH) : State.LOW);
      }
   }

   protected void setOpen(Wire wire) {
      if (wire != null) {
         wire.set(State.OPEN);
      }
   }

   protected void setOpen(Wire[] wires) {
      for (Wire wire : wires) {
         if (wire != null) {
            wire.set(State.OPEN);
         }
      }
   }

   /**
    * reads the input wires as integer
    *
    * @param wires wires to read the value from
    * @return the read value
    */
   protected int wireToInt(Wire[] wires) {
      int val = 0;
      for (int i = wires.length - 1; i >= 0; i--) {
         val <<= 1;
         val |= getState(wires[i]) ? 1 : 0;
      }

      return val;
   }

   protected int wireToIntOc(Wire[] wires) {
      int val = 0;
      for (int i = wires.length - 1; i >= 0; i--) {
         val <<= 1;
         val |= getStateOc(wires[i]) ? 1 : 0;
      }

      return val;
   }

   /**
    * writes the value to the wires binary decoded
    *
    * @param val   value to write
    * @param wires wires to write to
    */
   protected void intToWire(int val, Wire[] wires) {
      intToWire(val, wires, false);
   }

   protected void intToWire(int val, Wire[] wires, boolean activeLow) {
      for (int i = 0; i < wires.length; i++) {
         setState(wires[i], ((val & 0x01) != 0) ^ activeLow);
         val >>= 1;
      }
   }

   protected void intToWireOc(int val, Wire[] wires) {
      intToWireOc(val, wires, false);
   }

   protected void intToWireOc(int val, Wire[] wires, boolean activeLow) {
      for (int i = 0; i < wires.length; i++) {
         setState(wires[i], ((val & 0x01) != 0) ^ activeLow, true);
         val >>= 1;
      }
   }

   /**
    * selects one wire
    *
    * @param val       number of the wire to select
    * @param wires     wires to select
    * @param activeLow true, when active is low
    */
   protected void selectWire(int val, Wire[] wires, boolean activeLow) {
      for (int i = 0; i < wires.length; i++) {
         setState(wires[i], (val == i) ^ activeLow);
      }
   }

   // ------------------------------- o -------------------------------
}
