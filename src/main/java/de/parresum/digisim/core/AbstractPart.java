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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base Element of most parts.
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractPart implements CircuitPart {
   /** Name of this part in the scheme */
   private final String name;

   /** name of the lib, the part is defined in */
   private String libName;

   // no global logger to select logging of particular gates
   protected final Logger log;

   public AbstractPart(String name) {
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

}
