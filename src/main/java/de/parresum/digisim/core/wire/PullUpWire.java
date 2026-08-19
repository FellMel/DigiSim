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
package de.parresum.digisim.core.wire;

import java.util.ArrayList;
import java.util.List;

import de.parresum.digisim.core.State;

/**
 * Wire with pull up.
 *
 * When all input wires are open (or high), this wire will be high. When one ore mire input wires are low, this wire
 * will be low
 *
 * @author Kai Uwe Bachmann
 */
public class PullUpWire extends Wire {
   /**
    * all connected input wires
    */
   private final List<Wire> inputs = new ArrayList<>();

   public PullUpWire(final String name) {
      super(name);
      current = State.HIGH;
   }

   @Override
   public State set(final State current) {
      throw new IllegalStateException("Set can't be called directly on PullUpWire");
   }

   @Override
   public void join(final Wire input) {
      inputs.add(input);
      super.join(input);
   }

   @Override
   public State get() {
      for (final Wire wire : inputs) {
         if (wire.get() == State.LOW) {
            return State.LOW;
         }
      }
      return State.HIGH;
   }
}
