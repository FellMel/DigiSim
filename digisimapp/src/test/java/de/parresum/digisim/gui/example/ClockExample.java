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
package de.parresum.digisim.gui.example;

import javax.swing.SwingUtilities;

import de.parresum.digisim.gui.analyser.AnalyserWindow;
import de.parresum.digisim.gui.analyser.devices.WireDevice;
import de.parresum.digisim.gui.analyser.devices.WireDeviceController;
import de.parresum.digisim.lib.Clock;

public class ClockExample {

   public ClockExample() {

      Clock clock = new Clock("clock", 100);
      WireDevice device = new WireDevice("Clock test");
      device.setClockWire(clock.getOutput());
      device.setWire(1, clock.getOutput3());
      device.setWire(2, clock.getOutput2());
      device.setStarter(clock);
      WireDeviceController.addDevice(device);

      AnalyserWindow w = new AnalyserWindow();

      try {
         SwingUtilities.invokeAndWait(w);
      } catch (Exception e) {
         System.out.println("Error while invoking application: " + e.getMessage());
         System.exit(-1);
      }

   }

   public static void main(String[] args) {
      new ClockExample();
   }
}
