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
 * T-Flip-Flop
 *
 * output will be changed, when T is high on raising clk
 *
 * @author Kai Uwe Bachmann
 */
@Part("TFF")
public class TFlipFlop extends AbstractFlipFlop {
   // T-Input
   private Wire t;

   public TFlipFlop(final String name) {
      super(name);
   }

   @Override
   protected String getSymbolName() {
      return "T-FlipFlop";
   }

   /**
    * Setzt den T-Eingang
    *
    * @param t
    */
   @Pin("T")
   public void setT(final Wire t) {
      this.t = t;
   }

   @Override
   protected State flipFlop(final State cur) {
      if (t.get() == State.HIGH) {
         return cur.not();
      }

      return cur;
   }

}
