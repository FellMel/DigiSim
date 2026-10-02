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
package de.parresum.digisim.lib.flipflop;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.lib.State;
import de.parresum.digisim.lib.wire.Wire;

/**
 * SR-Flip-Flop
 *
 * @author Kai Uwe Bachmann
 */
@Part("SRFF")
public class SRFlipFlop extends AbstractFlipFlop {
   // Set-Input
   private Wire s;

   // Reset-Input
   private Wire r;

   public SRFlipFlop(final String name) {
      super(name);
   }

   @Override
   protected String getSymbolName() {
      return "RS-FlipFlop";
   }

   @Pin("S")
   public void setS(final Wire s) {
      this.s = s;
   }

   @Pin("R")
   public void setR(final Wire r) {
      this.r = r;
   }

   @Override
   protected State flipFlop(final State cur) {

      if (s.get() == State.HIGH && r.get() == State.HIGH) {
         // TODO: IllegalStateException
      } else if (s.get() == State.HIGH) {
         return State.HIGH;

      } else if (r.get() == State.HIGH) {
         return State.LOW;

      } else {
         // Nothing to do, yet
      }
      return cur;
   }

}
