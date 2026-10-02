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
package de.parresum.digisim.core;

import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.annotations.PortType;
import de.parresum.digisim.core.wire.Wire;

/**
 * A clock generator
 *
 * @author Kai Uwe Bachmann
 */
@Part("CLK")
public class Clock extends AbstractPart implements Out, Starter {

   // no global logger to select logging of particular gates
   private static final Logger LOG = LogManager.getLogger(Clock.class);
   /**
    * clock frequency of the generator in ms
    */
   private final int delay;

   /**
    * default output wire
    */
   private Wire output = new Wire("clk");

   /**
    * half clock output
    */
   private Wire output2 = new Wire("clk/2");

   /**
    * quarter clock output
    */
   private Wire output3 = new Wire("clk/4");

   /**
    * Timer to trigger the next change
    */
   private Timer timer;

   /** Whether the generator is working */
   private volatile boolean running = false;

   /**
    * Creates a default clock generator with a frequency of 5Hz
    *
    * @param name name of the generator
    */
   public Clock(String name) {
      super(name);
      this.delay = 100;
   }

   /**
    * Creates a clock generator
    *
    * @param name  name of the generator
    * @param delay periodic width in ms
    */
   public Clock(String name, final int delay) {
      super(name);
      this.delay = delay;
   }

   @Pin(value = "CLK", type = PortType.OUTPUT)
   public void setOutput(final Wire output2) {
      this.output2 = output2;
   }

   @Override
   public Wire getOutput() {
      return output;
   }

   @Pin(value = "CLK2", type = PortType.OUTPUT)
   public void setOutput2(final Wire output2) {
      this.output2 = output2;
   }

   public Wire getOutput2() {
      return output2;
   }

   @Pin(value = "CLK4", type = PortType.OUTPUT)
   public void setOutput3(final Wire output3) {
      this.output3 = output3;
   }

   public Wire getOutput3() {
      return output3;
   }

   /**
    * starts the generator
    */
   @Override
   public void start() {
      try {
         running = true;
         timer = new Timer(delay / 2, _ -> doTimer());
         timer.start();
      } catch (final Exception e) {
         LOG.error("Can't start timer", e);
      }
   }

   /**
    * stops the generator
    */
   @Override
   public void stop() {
      running = false;
      timer.stop();

   }

   /** timer callback */

   private void doTimer() {
      if (running) {
         output.set(output.get() == State.HIGH ? State.LOW : State.HIGH);
      }
      if (output.get() == State.HIGH) {

         output2.set(output2.get() == State.HIGH ? State.LOW : State.HIGH);
         if (output2.get() == State.HIGH) {

            output3.set(output3.get() == State.HIGH ? State.LOW : State.HIGH);
         }
      }
   }

}
