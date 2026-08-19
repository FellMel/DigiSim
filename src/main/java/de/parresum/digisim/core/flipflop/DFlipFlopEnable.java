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
 * D-Flip-Flop mit Enable.
 *
 * Data will be taken from D-pin by rising clock if enable is set
 *
 * @author Kai Uwe Bachmann
 */
@Part("DFFE")
public class DFlipFlopEnable extends DFlipFlop {
   /**
    * Enable input
    */
   private Wire ena;

   public DFlipFlopEnable(final String name) {
      super(name);
   }

   @Override
   protected String getSymbolName() {
      return "D-FlipFlop Enable";
   }

   /**
    * set enable input
    *
    * @param ena
    */
   @Pin("ENA")
   public void setEna(final Wire ena) {
      this.ena = ena;
   }

   @Override
   protected void logPins() {
      log.debug("    ena: {}: {}", ena, ena.get());
      super.logPins();
   }

   @Override
   protected State flipFlop(final State cur) {
      if (ena.get() == State.HIGH) {
         return super.flipFlop(cur);
      }
      return cur;
   }

}
