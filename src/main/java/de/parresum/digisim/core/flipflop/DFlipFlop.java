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
 * D-Flip-Flop.
 *
 * Data will be taken from D-pin by rising clock
 *
 * @author Kai Uwe Bachmann
 */
@Part("DFF")
public class DFlipFlop extends AbstractFlipFlop {

   // D-input
   private Wire d;

   public DFlipFlop(final String name) {
      super(name);
   }

   @Override
   protected String getSymbolName() {
      return "D-FlipFlop";
   }

   /**
    * set the d input
    *
    * @param d
    */
   @Pin("D")
   public void setD(final Wire d) {
      this.d = d;
   }

   @Override
   protected State flipFlop(final State cur) {

      return d.get();

   }

   @Override
   protected void logPins() {
      log.debug("    d: {}: {}", d, d.get());
      super.logPins();
   }

}
