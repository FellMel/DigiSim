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
 * XOR-Gate
 *
 * @author Kai Uwe Bachmann
 */
@Part("XOR")
@Part("digisim:Xor 2")
public class Xor extends AbstractGate {

   public Xor(final String name, final Wire output, final Wire... inputs) {
      super(name, output, inputs);
      register("XOR " + inputs.length);
   }

   public Xor(final String name, final Out... inputs) {
      super(name, inputs);
      register("XOR " + inputs.length);
   }

   public Xor(final String name) {
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

      return (currentHigh % 2 == 1 ? State.HIGH : State.LOW);

   }

}
