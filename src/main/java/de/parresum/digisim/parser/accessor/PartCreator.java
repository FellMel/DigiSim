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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.model.NetPart;

/**
 * Creator to create a new instance of a circuit part
 *
 * @author Kai Uwe Bachmann
 */
public class PartCreator {
   /** constructor to call for creation */
   private final Constructor<? extends CircuitPart> ctor;

   public PartCreator(Class<? extends CircuitPart> clazz) {
      super();

      Constructor<? extends CircuitPart>[] ctors = (Constructor<? extends CircuitPart>[]) clazz.getConstructors();
      Constructor<? extends CircuitPart> ctor = null;
      for (Constructor<? extends CircuitPart> item : ctors) {
         if (item.getParameterCount() == 1 && item.getParameterTypes()[0] == String.class) {
            ctor = item;
            break;
         }
      }
      if (ctor == null) {
         throw new IllegalStateException("Missing Constructor with String argument for class " + clazz.getName());
      }

      this.ctor = ctor;
   }

   /**
    * creates and initialize a new circuit part
    *
    * @param netPart extracted definition of the part to create
    */
   public CircuitPart create(NetPart netPart)
         throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      CircuitPart part = ctor.newInstance(netPart.getName());
      part.setLibName(netPart.getLib());

      return part;
   }
}
