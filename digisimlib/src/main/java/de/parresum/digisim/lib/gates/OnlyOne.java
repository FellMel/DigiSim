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

import de.parresum.digisim.lib.Out;
import de.parresum.digisim.lib.State;
import de.parresum.digisim.lib.wire.Wire;

/**
 * Exactly one input is high
 *
 * @author Kai Uwe Bachmann
 */
public class OnlyOne extends AbstractGate {

   public OnlyOne(final String name, final Wire output, final Wire... inputs) {
      super(name, output, inputs);
   }

   public OnlyOne(final String name, final Out... inputs) {
      super(name, inputs);
   }

   public OnlyOne(final String name) {
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

      return (currentHigh == 1 ? State.HIGH : State.LOW);

   }

}
