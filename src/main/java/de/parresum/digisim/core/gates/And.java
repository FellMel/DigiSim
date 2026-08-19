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
package de.parresum.digisim.core.gates;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.core.Out;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.wire.Wire;

/**
 * AND-Gate with 2-8 inputs
 *
 * @author Kai Uwe Bachmann
 */
@Part("AND")
@Part("digisim:And 2")
@Part("digisim:And 3")
@Part("digisim:And 4")
@Part("digisim:And 5")
@Part("digisim:And 6")
@Part("digisim:And 7")
@Part("digisim:And 8")
public class And extends AbstractGate {

   public And(final String name, final Wire output, final Wire... inputs) {
      super(name, output, inputs);
      register("AND " + inputs.length);
   }

   public And(final String name, final Out... inputs) {
      super(name, inputs);
      register("AND " + inputs.length);
   }

   public And(final String name) {
      super(name);
   }

   @Override
   protected State calculate() {

      State current = State.HIGH;
      for (final Wire wire : inputs) {
         if (wire != null && wire.get() != State.HIGH) {
            current = State.LOW;
            break;
         }
      }
      return current;

   }

}
