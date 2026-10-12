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
 * XNOR-Gate
 *
 * @author Kai Uwe Bachmann
 */
@Component("XNOR")
@Component("digisim:Xnor 2")
public class Xnor extends AbstractGate {

   public Xnor(final String name, final Wire output, final Wire... inputs) {
      super(name, output, inputs);
      register("XNOR " + inputs.length);
   }

   public Xnor(final String name, final Out... inputs) {
      super(name, inputs);
      register("XNOR " + inputs.length);
   }

   public Xnor(final String name) {
      super(name);
   }

   @Override
   protected State calculate() {
      int currentHigh = 0;
      for (final Wire wire : inputs) {
         if (wire != null && wire.get() == State.HIGH) {
            currentHigh++;
         }
      }

      return (currentHigh % 2 == 0 ? State.HIGH : State.LOW);

   }

}
