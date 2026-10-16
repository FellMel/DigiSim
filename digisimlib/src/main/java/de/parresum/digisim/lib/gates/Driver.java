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

import de.parresum.digisim.api.AbstractComponent;
import de.parresum.digisim.api.State;
import de.parresum.digisim.api.StateListener;
import de.parresum.digisim.api.annotations.Component;
import de.parresum.digisim.api.annotations.Pin;
import de.parresum.digisim.api.annotations.PortType;
import de.parresum.digisim.api.wire.Wire;
import de.parresum.digisim.lib.Out;

/**
 * Tri-State Driver
 *
 * When enable is low, output will be active
 *
 * @author Kai Uwe Bachmann
 */
@Component("digisim:Driver")
public class Driver extends AbstractComponent implements StateListener, Out {

   // input
   private Wire in;

   // not Enable
   private Wire enable;

   // output
   private Wire out;

   // internal wire needed for tri-state handling
   private final Wire wire;

   private boolean inverting = false;

   public Driver(final String name) {
      super(name);
      wire = new Wire(name + ".LatchOut");
      AbstractGate.register("LATCH");
   }

   public Driver(final String name, final Wire out, final Wire in, final Wire enable) {
      this(name);
      setOut(out);
      setIn(in);
      setEnable(enable);
   }

   // ----------------------------------- o -----------------------------------
   public void setInverting(boolean inverting) {
      this.inverting = inverting;
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
   @Pin(value = "O", type = PortType.TRISTATE)
   public void setOut(final Wire out) {
      out.setTriState();
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

   // ----------------------------------- o -----------------------------------
   protected State calculate() {

      if (enable == null || enable.get() == State.HIGH) {
         return State.OPEN;
      } else {
         boolean input = getState(in);
         if (inverting) {
            input = !input;
         }

         return input ? State.HIGH : State.LOW;
      }
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

   @Override
   public void connected() {
      this.stateChanged(null, null, null);
   }
}
