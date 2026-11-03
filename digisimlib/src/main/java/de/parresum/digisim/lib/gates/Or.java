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
package de.parresum.digisim.lib.gates;

import de.parresum.digisim.api.State;
import de.parresum.digisim.api.annotations.Component;
import de.parresum.digisim.api.wire.Wire;
import de.parresum.digisim.lib.Out;

/**
 * OR-Gate with 2-8 inputs
 *
 * @author Kai Uwe Bachmann
 */
@Component("OR")
@Component("Or 2")
@Component("Or 3")
@Component("Or 4")
@Component("Or 5")
@Component("Or 6")
@Component("Or 7")
@Component("Or 8")
public class Or extends AbstractGate {

   public Or(final String name, final Wire output, final Wire... inputs) {
      super(name, output, inputs);
      register("OR " + inputs.length);
   }

   public Or(final String name, final Out... inputs) {
      super(name, inputs);
      register("OR " + inputs.length);
   }

   public Or(final String name) {
      super(name);
   }

   @Override
   protected State calculate() {

      State current = State.LOW;
      for (final Wire wire : inputs) {
         if (wire != null && wire.get() == State.HIGH) {
            current = State.HIGH;
            break;
         }
      }
      return current;

   }

}
