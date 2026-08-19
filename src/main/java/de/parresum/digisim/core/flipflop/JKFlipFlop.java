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
package de.parresum.digisim.core.flipflop;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.wire.Wire;

/**
 * JK-Flip-Flop
 *
 * @author Kai Uwe Bachmann
 */
@Part("JKFF")
public class JKFlipFlop extends AbstractFlipFlop {
   // J-Input
   private Wire j;

   // K-Input
   private Wire k;

   public JKFlipFlop(final String name) {
      super(name);
   }

   @Override
   protected String getSymbolName() {
      return "JK-FlipFlop";
   }

   @Pin("J")
   public void setJ(final Wire j) {
      this.j = j;
   }

   @Pin("K")
   public void setK(final Wire k) {
      this.k = k;
   }

   @Override
   protected State flipFlop(final State cur) {

      if (j.get() == State.HIGH && k.get() == State.HIGH) {
         return cur.not();
      }
      if (j.get() == State.HIGH) {
         return State.HIGH;

      }
      if (k.get() == State.HIGH) {
         return State.LOW;

      }
      return cur;
   }

}
