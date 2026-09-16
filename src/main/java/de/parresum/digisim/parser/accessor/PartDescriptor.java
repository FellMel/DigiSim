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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.annotations.Value;
import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.core.wire.Wire;

/**
 * Descriptor holding informations to create the part and access its elements
 *
 * @author Kai Uwe Bachmann
 */
public class PartDescriptor {

   /** Creator to create new instances */
   private final PartCreator creator;

   /** known pins of the part and theire accessors */
   private final Map<String, PinAccessor> pins = new HashMap<>();

   /** additional values of the part */
   private final Map<String, ValueAccessor> values = new HashMap<>();

   /**
    * Creates the descriptor
    *
    * @param clazz clazz to create the descriptor for
    * @throws IntrospectionException when an error occours
    */
   public PartDescriptor(Class<? extends CircuitPart> clazz) throws IntrospectionException {

      creator = new PartCreator(clazz);

      BeanInfo bean = Introspector.getBeanInfo(clazz);

      for (PropertyDescriptor prop : bean.getPropertyDescriptors()) {
         if (prop.getWriteMethod() != null) {
            Pin pin = prop.getWriteMethod().getAnnotation(Pin.class);
            if (pin != null) {
               if (prop.getWriteMethod().getParameterCount() != 1
                     || !Wire.class.isAssignableFrom(prop.getWriteMethod().getParameters()[0].getType())) {
                  System.err.println("Illegal Wire setter. Need one argument of typ Wire");
               } else {
                  pins.put(pin.value(), new PinAccessor(pin.value(), clazz, prop));
               }
            }
            Value value = prop.getWriteMethod().getAnnotation(Value.class);
            if (value != null) {
               if (prop.getWriteMethod().getParameterCount() != 1
                     || !prop.getWriteMethod().getParameters()[0].getType().isAssignableFrom(String.class)) {
                  System.err.println("Illegal Value setter. Need one argument of typ String");
               } else {
                  values.put(value.value(), new ValueAccessor(value.value(), clazz, prop));
               }
            }
         }
      }
   }

   /**
    * Gets the creator to create a new Part
    *
    * @return
    */
   public PartCreator getCreator() {
      return creator;
   }

   /**
    * Gets the known pins
    *
    * @return the pin accessors
    */
   public Map<String, PinAccessor> getPins() {
      return pins;
   }

   /**
    * Gets the known values
    *
    * @return the value accessors
    */
   public Map<String, ValueAccessor> getValues() {
      return values;
   }

}
