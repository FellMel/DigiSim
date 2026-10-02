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
import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.annotations.PortType;
import de.parresum.digisim.core.AbstractPart;
import de.parresum.digisim.core.Out;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.StateListener;
import de.parresum.digisim.core.wire.TriStateWire;
import de.parresum.digisim.core.wire.Wire;

/**
 * Tri-State-Latch
 *
 * When enable is low, output will be active
 *
 * @author Kai Uwe Bachmann
 */
@Part("digisim:Driver")
public class Latch extends AbstractPart implements StateListener, Out {

   // input
   private Wire in;

   // Enable
   private Wire enable;

   // output
   private TriStateWire out;

   // internal wire needed for tri-state handling
   private final Wire wire;

   public Latch(final String name) {
      super(name);
      wire = new Wire(name + ".LatchOut");
      AbstractGate.register("LATCH");
   }

   public Latch(final String name, final TriStateWire out, final Wire in, final Wire enable) {
      this(name);
      setOut(out);
      setIn(in);
      setEnable(enable);
   }

   /**
    * set the input wire
    *
    * @param in
    */
   @Pin("I")
   public void setIn(final Wire in) {
      this.in = in;
      in.addStateListener(this);
   }

   /**
    * set the enable wire
    *
    * @param enable
    */
   @Pin("E")
   public void setEnable(final Wire enable) {
      this.enable = enable;
      enable.addStateListener(this);
   }

   /**
    * set the output wire
    *
    * @param out
    */
   @Pin(value = "O", type = PortType.BIDIRECTIONAL)
   public void setOut(final TriStateWire out) {
      if (this.out != null) {
         this.out.removeWire(wire);
      }

      this.out = out;
      this.out.addWire(wire);

   }

   @Override
   public Wire getOutput() {
      return this.out;
   }

   protected State calculate() {

      if (enable == null || enable.get() == State.LOW) {
         wire.set(State.OPEN);
      } else {
         wire.set(in.get());
      }
      return wire.get();

   }

   /**
    * Listener for changes on input wires
    *
    */
   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {
      State current = calculate();
      // TODO: remove open
      if ((current != out.get() && current != State.OPEN)) {
         log.debug("Change {} through Wire {} from {} to {}. previous {}", wire, src, oldState, newState, out.get());
         log.debug("  now: {}", current);
      }
      wire.set(current);

   }
}
