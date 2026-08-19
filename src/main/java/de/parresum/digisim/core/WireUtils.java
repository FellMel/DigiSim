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

import de.parresum.digisim.core.wire.TriStateWire;
import de.parresum.digisim.core.wire.Wire;

/**
 * some utils for wires
 *
 * @author Kai Uwe Bachmann
 */
public class WireUtils {
   /**
    * creates an address bus with 16 wires
    *
    * @param name name of the bus to create
    * @return the created wires
    */
   public static TriStateWire[] createAdrBus(final String name) {
      return createBusIO(name, 16);
   }

   /**
    * creates a half data bus with 4 wires
    *
    * @param name name of the bus to create
    * @return the created wires
    */
   public static Wire[] createHalfDB(final String name) {
      return createBus(name, 4);
   }

   /**
    * creates a tri state bus
    *
    * @param name name of the bus tu create
    * @param size width of the bus
    * @return the created wires
    */
   public static TriStateWire[] createBusIO(final String name, final int size) {
      final TriStateWire[] bus = new TriStateWire[size];
      for (int i = 0; i < bus.length; i++) {
         bus[i] = new TriStateWire(name + "." + i);
      }
      return bus;
   }

   /**
    * creates a bus
    *
    * @param name name of the bus
    * @param size width of the bus
    * @return the created wires
    */
   public static Wire[] createBus(final String name, final int size) {
      final Wire[] bus = new Wire[size];
      for (int i = 0; i < bus.length; i++) {
         bus[i] = new Wire(name + "." + i);
      }
      return bus;
   }
}
