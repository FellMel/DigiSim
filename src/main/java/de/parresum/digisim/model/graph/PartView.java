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

package de.parresum.digisim.model.graph;

import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.model.AbstractCircuitPart;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class PartView {

   private final String name;
   private final CircuitPart part;

   private final AbstractCircuitPart netPart;

   public PartView(String name, CircuitPart part, AbstractCircuitPart netPart) {
      super();
      this.name = name;
      this.part = part;
      this.netPart = netPart;
   }

   public String getName() {
      return name;
   }

   public CircuitPart getPart() {
      return part;
   }

   public AbstractCircuitPart getNetPart() {
      return netPart;
   }

}
