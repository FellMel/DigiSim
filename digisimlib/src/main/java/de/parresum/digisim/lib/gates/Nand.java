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

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.lib.Out;
import de.parresum.digisim.lib.State;
import de.parresum.digisim.lib.wire.Wire;

/**
 * NAND-Gate with 2-8 inputs
 *
 * @author Kai Uwe Bachmann
 */
@Part("NAND")
@Part("digisim:Nand 2")
@Part("digisim:Nand 3")
@Part("digisim:Nand 4")
@Part("digisim:Nand 5")
@Part("digisim:Nand 6")
@Part("digisim:Nand 7")
@Part("digisim:Nand 8")
public class Nand extends AbstractGate {

   public Nand(final String name, final Wire output, final Wire... inputs) {
      super(name, output, inputs);
      register("NAND " + inputs.length);
   }

   public Nand(final String name, final Out... inputs) {
      super(name, inputs);
      register("NAND " + inputs.length);
   }

   public Nand(final String name) {
      super(name);
   }

   public Nand(final String name, final boolean openCollector, final Out... inputs) {
      super(name, openCollector, inputs);
      register("NAND " + (openCollector ? "(OC) " : "") + inputs.length);
   }

   public Nand(final String name, final boolean openCollector, final Wire output, final Wire... inputs) {
      super(name, openCollector, output, inputs);
      register("NAND " + (openCollector ? "(OC) " : "") + inputs.length);
   }

   @Override
   protected State calculate() {

      State current = State.LOW;
      for (final Wire wire : inputs) {
         if (wire != null && wire.get() != State.HIGH) {
            current = State.HIGH;
            break;
         }
      }
      return current;

   }

}
