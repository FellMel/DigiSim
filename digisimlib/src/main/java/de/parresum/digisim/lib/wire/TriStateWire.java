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
package de.parresum.digisim.lib.wire;

import java.util.ArrayList;
import java.util.List;

import de.parresum.digisim.lib.State;
import de.parresum.digisim.lib.StateListener;

/**
 * Tri-State wire.
 *
 * This wire can be open and connected to multiple tri-state outputs. The network will be realized by some internal
 * wires
 *
 * @author Kai Uwe Bachmann
 */
public class TriStateWire extends Wire implements StateListener {

   /** internal inputs */
   private final List<Wire> inputs = new ArrayList<>();

   public TriStateWire(final String name) {
      super(name);
      current = State.OPEN;
   }

   public void addWire(final Wire wire) {
      inputs.add(wire);
      wire.addStateListener(this);
   }

   public void removeWire(final Wire wire) {
      wire.removeStateListener(this);
      inputs.remove(wire);
   }

   @Override
   public State set(final State current) {
      throw new IllegalAccessError("TriState cant be set direct");
   }

   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {
      State current = State.OPEN;
      for (final Wire in : inputs) {
         log.debug("   Input {}: {}", in, in.get());
         if (in.get() != State.OPEN) {
            // TODO: Reactivate
//				if (current != State.OPEN) {
//					throw new IllegalStateException("Multiple outputs on same wire: " + toString());
//				}
            current = in.get();
         }
      }

      super.set(current);

   }

}
