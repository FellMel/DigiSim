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

import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.annotations.PortType;
import de.parresum.digisim.core.AbstractPart;
import de.parresum.digisim.core.Out;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.StateListener;
import de.parresum.digisim.core.gates.AbstractGate;
import de.parresum.digisim.core.wire.Wire;

/**
 * Base implementation of Flip-Flops
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractFlipFlop extends AbstractPart implements StateListener, Out {
   /** normaler output */
   private Wire output;

   /** inverted output */
   private Wire outputn;

   /** actual state */
   private State state = State.LOW;

   /** Clock input */
   private Wire clk;

   /** preset input */
   private Wire prn;

   /** clear input */
   private Wire clrn;

   public AbstractFlipFlop(final String name) {
      super(name);
      AbstractGate.register(getSymbolName());
   }

   protected abstract String getSymbolName();

   /**
    * connect the clock input
    *
    * @param clk wire to connect
    */
   @Pin("CLK")
   public void setClk(final Wire clk) {
      if (this.clk != null) {
         this.clk.removeStateListener(this);
      }
      this.clk = clk;

      // clock signal needs a second notification for output delay
      clk.setDoubleSignal(true);
      this.clk.addStateListener(this);
   }

   /**
    * connect the PRESET input
    *
    * @param prn wire to connect
    */
   @Pin("/PR")
   public void setPrn(final Wire prn) {
      if (this.prn != null) {
         // TODO: disconnect old wire
      }
      this.prn = prn;
      this.prn.addStateListener((src, o, n) -> pr(n));
   }

   /**
    * connect the CLEAR input
    *
    * @param clrn wire to connect
    */
   @Pin("/CLR")
   public void setClrn(final Wire clrn) {
      if (this.clrn != null) {
         // TODO: disconnect old wire
      }
      this.clrn = clrn;
      this.clrn.addStateListener((src, o, n) -> clr(n));
   }

   /**
    * connect the output
    *
    * @param output wire to connect
    */
   @Pin(value = "Q", type = PortType.OUTPUT)
   public void setOutput(final Wire output) {
      if (this.output != null) {
         throw new IllegalStateException("Double output on wire");
      }
      this.output = output;
   }

   /**
    * connect the inverted output
    *
    * @param outputn wire to connect
    */
   @Pin(value = "/Q", type = PortType.OUTPUT)
   public void setOutputn(final Wire outputn) {
      if (this.outputn != null) {
         throw new IllegalStateException("Double outputn on wire");
      }
      this.outputn = outputn;
   }

   /**
    * gets the wire connected to the output pin
    *
    * @return the connected wire or null if not connected
    */
   @Override
   public Wire getOutput() {
      return output;
   }

   /**
    * do the preset
    *
    * @param state state on the preset pin
    */
   private void pr(final State state) {
      if (state == State.LOW) {
         this.state = (State.HIGH);
         outputState();
      }
      log.debug("Preset {}: Output now {}", state, this.state);
   }

   /**
    * do the clear
    *
    * @param state state on the clear pin
    */
   private void clr(final State state) {
      if (state == State.LOW) {
         this.state = (State.LOW);
         outputState();
      }
      log.debug("Clear {}: Output now {}", state, this.state);

   }

   /**
    * set the flipflop to high
    */
   public void set() {
      state = State.HIGH;
      outputState();
   }

   /**
    * set the flipflop to low
    */
   public void clear() {
      state = State.LOW;
      outputState();

   }

   /**
    * writes the pin states to log file
    */
   protected void logPins() {
      log.debug("    clrn: {}: {}", clrn, clrn != null ? clrn.get() : null);
      log.debug("    prn: {}: {}", prn, prn != null ? prn.get() : null);
   }

   /**
    * calculates the new state of the flipflop
    *
    * @param cur old state
    * @return new state
    */
   protected abstract State flipFlop(State cur);

   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {

      final State previous = this.state;
      // preset has precedence
      if (this.prn != null && this.prn.get() == State.LOW) {
         state = State.HIGH;
         outputState();
         log.debug("Change through Wire {} from {} to {}. previous {}, now {}", src, oldState, newState, previous,
               state);
         return;
      }

      // second precedence is set
      if (this.clrn != null && this.clrn.get() == State.LOW) {
         state = State.LOW;
         outputState();
         log.debug("Change through Wire {} from {} to {}. previous {}, now {}", src, oldState, newState, previous,
               state);
         return;
      }

      // now normal FF-Handling
      if (oldState == State.LOW && newState == State.HIGH) {
         state = flipFlop(state);
         log.debug("Change through Wire {} from {} to {}. previous {}, now {}", src, oldState, newState, previous,
               state);
         if (log.isDebugEnabled()) {
            logPins();
         }
      }

      // output is delayed
      if (oldState == State.HIGH && newState == State.HIGH) {
         outputState();
         log.debug("Change through Wire {} from {} to {}. previous {}, now {}", src, oldState, newState, previous,
               state);
      }

   }

   /**
    * initializes the state
    */
   public void initState() {
      state = State.LOW;
      outputState();
   }

   /**
    * iniilaizes the state
    *
    * @param init state to initialize to
    */
   public void initState(final State init) {
      state = init;
      outputState();
   }

   /**
    * propagates the state to output pins
    */
   protected void outputState() {
      if (output != null) {
         output.set(state);
      }

      if (outputn != null) {
         outputn.set(state.not());
      }
   }

}
