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

/**
 * Accessor to set a value within a part
 *
 * @author Kai Uwe Bachmann
 */
public class ValueAccessor {
   /** name of the value */
   protected final String value;

   /** part, the value is for */
   protected final Class<? extends CircuitPart> part;

   /** property descriptor defining the value within the part class */
   protected final PropertyDescriptor prop;

   public ValueAccessor(String value, Class<? extends CircuitPart> part, PropertyDescriptor prop) {
      super();
      this.value = value;
      this.part = part;
      this.prop = prop;
   }

   /**
    * seets the value within the part
    *
    * @param part  pat to set the value in
    * @param value value to set
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   public void setValue(CircuitPart part, String value) throws IllegalAccessException, InvocationTargetException {
      // TODO: check part is of correct type ...

      prop.getWriteMethod().invoke(part, value);
   }

}
