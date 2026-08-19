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

package de.parresum.digisim.parser.accessor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.core.wire.Wire;

/**
 * Helper to access wires of parts
 *
 * @author Kai Uwe Bachmann
 */
public class PinAccessor {
   /** Pin number, the accessor is for */
   protected final String pinNumber;

   /** Class, the accessor is for */
   protected final Class<? extends CircuitPart> part;

   /** property descriptor defining the wire within the part class */
   protected final PropertyDescriptor prop;

   public PinAccessor(String pinNumber, Class<? extends CircuitPart> part, PropertyDescriptor prop) {
      super();
      this.pinNumber = pinNumber;
      this.part = part;
      this.prop = prop;
   }

   public void setWire(CircuitPart part, Wire wire) throws IllegalAccessException, InvocationTargetException {
      // TODO: check part is of correct type ...

      prop.getWriteMethod().invoke(part, wire);
   }
}
