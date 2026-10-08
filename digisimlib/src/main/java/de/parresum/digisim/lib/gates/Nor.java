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
 * NOR-Gate with 2-8 inputs
 *
 * @author Kai Uwe Bachmann
 */
@Component("NOR")
@Component("digisim:Nor 2")
@Component("digisim:Nor 3")
@Component("digisim:Nor 4")
@Component("digisim:Nor 5")
@Component("digisim:Nor 6")
@Component("digisim:Nor 7")
@Component("digisim:Nor 8")
public class Nor extends AbstractGate {

   public Nor(final String name, final Wire output, final Wire... inputs) {
      super(name, output, inputs);
      register("NOR " + inputs.length);
   }

   public Nor(final String name, final Out... inputs) {
      super(name, inputs);
      register("NOR " + inputs.length);
   }

   public Nor(final String name) {
      super(name);
   }

   public Nor(final String name, final boolean openCollector, final Wire output, final Wire... inputs) {
      super(name, openCollector, output, inputs);
      register("NOR " + inputs.length);
   }

   public Nor(final String name, final boolean openCollector, final Out... inputs) {
      super(name, openCollector, inputs);
      register("NOR " + inputs.length);
   }

   public Nor(final String name, final boolean openCollector) {
      super(name, openCollector);
   }

   @Override
   protected State calculate() {

      State current = State.HIGH;
      for (final Wire wire : inputs) {
         if (wire != null && wire.get() == State.HIGH) {
            current = State.LOW;
            break;
         }
      }
      return current;

   }

}
