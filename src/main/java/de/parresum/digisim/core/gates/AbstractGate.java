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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.annotations.PortType;
import de.parresum.digisim.core.AbstractPart;
import de.parresum.digisim.core.Out;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.StateListener;
import de.parresum.digisim.core.wire.PullUpWire;
import de.parresum.digisim.core.wire.Wire;

/**
 * Base of all gate implementations
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractGate extends AbstractPart implements StateListener, Out {
   /** Counter of instances for reporting */
   private static final Map<String, Integer> instances = new HashMap<>();

   /** output of the gate */
   private Wire output;

   /** whether the output is open collector */
   private boolean openCollector;

   /** input pins of the gate */
   protected final List<Wire> inputs = new ArrayList<>();

   /** trace mode for extended logging */
   private boolean trace = false;

   /**
    * Creates a gate for generic schemes
    *
    * @param name name of the gate
    */
   public AbstractGate(final String name) {
      super(name);
   }

   /**
    * Creates a new gate and connects the pins
    *
    * @param name   name of the gate
    * @param output output wire
    * @param inputs input wires
    */
   public AbstractGate(final String name, final Wire output, final Wire... inputs) {
      this(name);
      setOutput(output);
      addInputs(inputs);
   }

   /**
    * Creates a new gate with open collector output
    *
    * @param name          name of the gate
    * @param openCollector whether the gate is OC
    * @param output        output wire
    * @param inputs        input wires
    */
   public AbstractGate(final String name, final boolean openCollector, final Wire output, final Wire... inputs) {
      this(name);
      this.openCollector = openCollector;
      setOutput(output);
      addInputs(inputs);
   }

   /**
    * Creates a new Gate and connects the input pins. the output wire will be created automatically.
    *
    * @param name   name of the gate
    * @param inputs input wires
    */
   public AbstractGate(final String name, final Out... inputs) {
      this(name);
      setOutput(new Wire(""));
      addInputs(inputs);
   }

   /**
    * Creates a new Gate and connects the input pins. the output wire will be created automatically.
    *
    * @param name          name of the gate
    * @param openCollector whether the gate is OC
    * @param inputs        input wires
    */
   public AbstractGate(final String name, final boolean openCollector, final Out... inputs) {
      this(name);
      this.openCollector = openCollector;
      setOutput(new Wire(""));
      addInputs(inputs);
   }

   // ----------------------------------- o -----------------------------------
   public boolean isOpenCollector() {
      return openCollector;
   }

   public void setOpenCollector(final boolean openCollector) {
      this.openCollector = openCollector;
   }

   public void setTrace() {
      this.trace = true;
   }

   // ----------------------------------- o -----------------------------------
   // Outputs
   // ----------------------------------- o -----------------------------------
   /**
    * set the output port
    *
    * @param output output wire to connect
    */
   @Pin(value = "O", type = PortType.OUTPUT)
   public void setOutput(final Wire output) {
      if (this.output != null) {
         throw new IllegalStateException("Double output on wire");
      }
      if (output instanceof PullUpWire) {
         this.output = new Wire(output.toString());
         output.join(this.output);
      } else {

         this.output = output;
      }

   }

   /**
    * gets the output wire
    *
    * @return the connected wire or null if not connected
    */
   @Override
   public Wire getOutput() {
      return output;
   }

   // ----------------------------------- o -----------------------------------
   // Input
   // ----------------------------------- o -----------------------------------
   /**
    * Adds an input wire
    *
    * @param input wire to connect
    */
   public void addInput(final Wire input) {
      if (!inputs.contains(input)) {
         inputs.add(input);
         input.addStateListener(this);
         stateChanged(null, null, null);
      }
   }

   /**
    * joins another gate with an input of this gate
    *
    * @param input gate to connect to this
    */
   public void addInput(final Out input) {
      final Wire wire = input.getOutput();
      if (!inputs.contains(wire)) {
         inputs.add(wire);
         wire.addStateListener(this);
         stateChanged(null, null, null);
      }
   }

   /**
    * Connects multiple wires to this gate
    *
    * @param inputs wires to connect
    */
   public void addInputs(final Out... inputs) {
      for (final Out input : inputs) {
         addInput(input);
      }
   }

   /**
    * Connects multiple wires to this gate
    *
    * @param inputs wires to connect
    */
   public void addInputs(final Wire... inputs) {
      for (final Wire input : inputs) {
         addInput(input);
      }
   }

   /**
    * connects a wire to a particular pin
    *
    * @param pos  pin number to connect to
    * @param wire wire to connect
    * @return previously connected wire on this pin
    */
   protected Wire setInput(int pos, Wire wire) {
      while (inputs.size() <= pos) {
         inputs.add(null);
      }

      Wire old = inputs.get(pos);
      if (old != null) {
         old.removeStateListener(this);
      }

      inputs.set(pos, wire);
      wire.addStateListener(this);
      stateChanged(null, null, null);

      return old;
   }

   protected Wire getInput(int pos) {
      if (inputs.size() <= pos) {
         return null;
      }
      return inputs.get(pos);
   }

   @Pin(value = "A", type = PortType.INPUT)
   public void setA(Wire wire) {
      setInput(0, wire);
   }

   @Pin(value = "B", type = PortType.INPUT)
   public void setB(Wire wire) {
      setInput(1, wire);
   }

   @Pin(value = "C", type = PortType.INPUT)
   public void setC(Wire wire) {
      setInput(2, wire);
   }

   @Pin(value = "D", type = PortType.INPUT)
   public void setD(Wire wire) {
      setInput(3, wire);
   }

   @Pin(value = "E", type = PortType.INPUT)
   public void setE(Wire wire) {
      setInput(4, wire);
   }

   @Pin(value = "F", type = PortType.INPUT)
   public void setF(Wire wire) {
      setInput(5, wire);
   }

   @Pin(value = "G", type = PortType.INPUT)
   public void setG(Wire wire) {
      setInput(6, wire);
   }

   @Pin(value = "H", type = PortType.INPUT)
   public void setH(Wire wire) {
      setInput(7, wire);
   }
   // ----------------------------------- o -----------------------------------
   // General pins
   // ----------------------------------- o -----------------------------------

   // ----------------------------------- o -----------------------------------
   /**
    * Calculates the gate state
    *
    * @return new state of the gate
    */
   protected abstract State calculate();

   /**
    * Listener for changes on input wires
    *
    */
   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {
      if (output == null) {
         return;
//         throw new IllegalStateException("Output not connected");
      }
      State current = calculate();
      if (openCollector && current == State.HIGH) {
         current = State.OPEN;
      }
      // TODO: remove open
      if ((current != output.get() && current != State.OPEN) || trace) {
         log.debug("Change {} through Wire {} from {} to {}. previous {}", output, src, oldState, newState,
               output.get());
         if (log.isDebugEnabled()) {
            for (final Wire wire : inputs) {
               log.debug("   Wire {}: {}", wire, wire != null ? wire.get() : null);
            }
         }
         log.debug("  now: {}", current);
      }
      output.set(current);

   }

   // ----------------------------------- o -----------------------------------
   public static void register(final String name) {
      Integer cur = instances.get(name);
      if (cur == null) {
         cur = 1;
      } else {
         cur++;
      }
      instances.put(name, cur);
   }

   public static void printInstances() {
      System.out.println("Registered Gates");
      System.out.println("================");
      int total = 0;
      for (final Entry<String, Integer> e : instances.entrySet()) {
         total += e.getValue();
         System.out.println("  " + e.getKey() + " : " + e.getValue());
      }
      System.out.println("----------------");
      System.out.println("  " + total);

   }
}
