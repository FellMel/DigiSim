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
package de.parresum.digisim.gui.analyser.devices;

import java.io.IOException;

import de.parresum.digisim.gui.analyser.CapturedData;
import de.parresum.digisim.lib.Starter;
import de.parresum.digisim.lib.State;
import de.parresum.digisim.lib.StateListener;
import de.parresum.digisim.lib.wire.Wire;

/**
 * Device to connect to wires
 *
 * @author Kai Uwe Bachmann
 */
public class WireDevice implements StateListener {
   private final String name;

   protected int enabledChannels;

   private final Wire[] wires = new Wire[32];
   private Wire clockWire;
   private Starter starter;

   private final Object wait = new Object();
   private int curSample = 0;
   private final int buffer[];

   private SampleSize size;

   protected boolean running;
   protected int percentageDone;

   public WireDevice(final String name) {
      this(name, SampleSize.S128);
   }

   public WireDevice(final String name, final SampleSize size) {
      this.name = name;
      this.size = size;

      buffer = new int[size.samples];

      percentageDone = -1;
      stop();

   }

   public String getName() {
      return name;
   }

   public Wire getClockWire() {
      return clockWire;
   }

   public void setClockWire(final Wire clockWire) {
      this.clockWire = clockWire;
      wires[0] = clockWire;
      enabledChannels &= 0x01;
   }

   public void setStarter(final Starter starter) {
      this.starter = starter;
   }

   public void setWire(final int pos, final Wire wire) {
      if (pos < 0 || pos >= 32) {
         throw new IllegalArgumentException("pos must be between 0 and 31");
      }
      wires[pos] = wire;

      final int mask = 0x01 << pos;
      if (wire != null) {
         enabledChannels |= mask;
      } else {
         enabledChannels &= mask;

      }
   }

   /**
    * Returns wether or not the trigger is enabled.
    *
    * @@return <code>true</code> when trigger is enabled, <code>false</code> otherwise
    */
   public boolean isTriggerEnabled() {
      return true;
   }

   /**
    * Returns wether or not the noise filter can be used in the current configuration.
    *
    * @@return <code>true</code> when noise filter is available, <code>false</code> otherwise
    */
   public boolean isFilterAvailable() {
      return true;
   }

   /**
    * Returns the number of available channels in current configuration.
    *
    * @@return number of available channels
    */
   public int getAvailableChannelCount() {
      return (32);
   }

   public SampleSize getSize() {
      return size;
   }

   public void setSize(final SampleSize size) {
      this.size = size;
   }

   /**
    * Returns the current clock source.
    *
    * @@return the clock source currently used as defined by the CLOCK_ properties
    */
   public int getClockSource() {
      return 0;
   }

   public boolean isRunning() {
      return (running);
   }

   /**
    * Returns the percentage of the expected data that has already been read. The return value is only valid when
    * <code>isRunning()</code> returns <code>true</code>.
    *
    * @@return percentage already read (0-100)
    */
   public int getPercentage() {
      return (percentageDone);
   }

   /**
    * Sends the configuration to the device, starts it, reads the captured data and returns a CapturedData object
    * containing the data read as well as device configuration information.
    *
    * @@return captured data
    * @@throws IOException          when writing to or reading from device fails
    * @@throws InterruptedException if a read time out occurs after trigger match or stop() was called before trigger
    *                               match
    */
   public CapturedData run() throws IOException, InterruptedException {

      clockWire.addStateListener(this);
      running = true;
      curSample = 0;

      if (starter != null) {
         starter.start();
      }
      synchronized (wait) {
         wait.wait();
      }
      // collect additional information for CapturedData
      final int pos = 0;
      final int rate = 2;

      final CapturedData data = new CapturedData(buffer, pos, rate, 32, enabledChannels);
      final String[] labels = new String[32];
      for (int i = 0; i < 32; i++) {
         final Wire wire = wires[i];
         if (wire != null) {
            labels[i] = wire.toString();
         }
      }

      data.setLabels(labels);
      return data;
   }

   /**
    * Informs the thread in run() that it is supposed to stop reading data and return.
    *
    */
   public void stop() {
      running = false;
      if (starter != null) {
         starter.stop();
      }
      if (clockWire != null) {
         clockWire.removeStateListener(this);
      }
      synchronized (wait) {
         wait.notifyAll();
      }
   }

   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {
      if (oldState != newState) {
         buffer[curSample] = readVal();
         percentageDone = (100 * curSample) / buffer.length;
         curSample++;

         if (curSample >= buffer.length) {
            stop();
         }
      }
   }

   private int readVal() {
      int val = 0;
      for (int i = 31; i >= 0; i--) {
         final Wire wire = wires[i];
         val <<= 1;

         if (wire != null) {
            if (wire.get() == State.HIGH) {
               val |= 1;
            }
         }
      }
      return val;
   }

}
