package de.parresum.digisim.api.wire;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.api.State;
import de.parresum.digisim.api.StateListener;

/**
 * General wire to connect parts
 *
 * @author Kai Uwe Bachmann
 */
public class Wire implements StateListener {
   /**
    * Wire with constant low
    */
   public final static Wire GND = new Wire("GND") {
      @Override
      public State get() {
         return State.LOW;
      }

   };
   /**
    * Wire with constant high
    */
   public final static Wire VCC = new Wire("VCC") {

      @Override
      public State get() {
         return State.HIGH;
      }

   };

   // ------------------------------- o -------------------------------
   // local logger to enable each wire separated
   protected final Logger log;

   // name of the wire / net
   private final String name;

   // current state of the wire
   protected State current = State.LOW;

   // creates a double change signal, needeed by flipflops
   private boolean doubleSignal = false;

   private boolean triState = false;
   /** internal inputs for tri state */
   private List<Wire> inputs;

   // Listeners on this wire
   private final Set<StateListener> listeners = new CopyOnWriteArraySet<>();

   // ------------------------------- o -------------------------------
   /**
    * Creates a new wire
    *
    * @param name name of this wire
    */
   public Wire(final String name) {
      super();
      this.name = name;
      if (name != null && !name.isBlank()) {
         String logName = name.replace(" ", "_");
         log = LogManager.getLogger(logName);
      } else {
         log = LogManager.getLogger(this.getClass());
      }
   }

   // ------------------------------- o -------------------------------
   /**
    * gets the name of the wire
    *
    * @return name of the wire
    */
   public String getName() {
      return name;
   }

   /**
    * Gets the actual state of the wire
    *
    * @return
    */
   public State get() {
      return current;
   }

   /**
    * Connects wires
    *
    * This wire takes the state of input wire
    *
    * @param input
    */
   public void join(final Wire input) {
      input.addStateListener((_, o, n) -> {
         this.current = n;
         fireStateChange(o, n);
      });
      input.fireStateChange(input.current.not(), input.current);
   }

   /**
    * change the state of this wire
    *
    * @param current new state
    * @return previous state
    */
   public State set(final State current) {
      if (triState) {
         throw new IllegalAccessError("TriState cant be set direct");
      }
      return setInternal(current);
   }

   private State setInternal(State current) {
      final State old = get();
      this.current = current;
      final State tmp = get();
      fireStateChange(old, tmp);
      return old;

   }

   // ------------------------------- o -------------------------------
   /**
    * checks whether this wire create a double signal
    *
    * @return true on double enabled
    */
   public boolean isDoubleSignal() {
      return doubleSignal;
   }

   /**
    * sets the double signal state
    *
    * @param doubleSignal double signal state
    */
   public void setDoubleSignal(final boolean doubleSignal) {
      this.doubleSignal = doubleSignal;
   }

   // ------------------------------- o -------------------------------
   /**
    * switches the wire to Tri-State A Tri-State-Wire can't be switched back to normal
    *
    * This wire can be open and connected to multiple tri-state outputs. The network will be realized by some internal
    * wires
    *
    */
   public void setTriState() {
      if (this.triState) {
         return;
      }

      this.triState = true;
      inputs = new ArrayList<Wire>();
   }

   /**
    * @return the triState
    */
   public boolean isTriState() {
      return triState;
   }

   public void addWire(final Wire wire) {
      if (triState) {
         inputs.add(wire);
         wire.addStateListener(this);
      }
   }

   public void removeWire(final Wire wire) {
      if (triState) {
         wire.removeStateListener(this);
         inputs.remove(wire);
      }
   }

   // ------------------------------- o -------------------------------
   /**
    * adds a listener to this wire
    *
    * @param listener the listener to add
    */
   public void addStateListener(final StateListener listener) {
      this.listeners.add(listener);
   }

   /**
    * removes a listener from this wire
    *
    * @param listener listener to remove
    */
   public void removeStateListener(final StateListener listener) {
      this.listeners.remove(listener);
   }

   /**
    * propagates a state change to all listener
    *
    * @param oldState old stage
    * @param newState new state
    */
   protected void fireStateChange(final State oldState, final State newState) {
      if (oldState != newState) {
         log.debug("Change {} from {} to {}. {} ", name, oldState, newState, this.getClass().getSimpleName());
         for (final StateListener listener : listeners) {
            listener.stateChanged(this, oldState, newState);
         }
         if (doubleSignal) {
            for (final StateListener listener : listeners) {
               listener.stateChanged(this, newState, newState);
            }
         }
      }
   }

   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {
      State current = State.OPEN;
      for (final Wire in : inputs) {
         log.debug("   Input {}: {}", in, in.get());
         if (in.get() != State.OPEN) {
            // TODO: Reactivate
//              if (current != State.OPEN) {
//                  throw new IllegalStateException("Multiple outputs on same wire: " + toString());
//              }
            current = in.get();
         }
      }

      setInternal(current);

   }

   // ------------------------------- o -------------------------------
   @Override
   public String toString() {
      return name + ": " + get();
   }

   // ------------------------------- o -------------------------------
}
