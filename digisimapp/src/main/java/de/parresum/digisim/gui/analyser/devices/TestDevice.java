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

public class TestDevice extends WireDevice {

   public TestDevice() {
      super("Test Device");
      enabledChannels = 1;
   }

   /**
    * Sends the configuration to the device, starts it, reads the captured data and returns a CapturedData object
    * containing the data read as well as device configuration information.
    *
    * @return captured data
    * @throws IOException          when writing to or reading from device fails
    * @throws InterruptedException if a read time out occurs after trigger match or stop() was called before trigger
    *                              match
    */
   @Override
   public CapturedData run() throws IOException, InterruptedException {

      running = true;
      int[] buffer = new int[8129];
      int channels = 32;
      int samples = 8129;

      // wait for first byte forever (trigger could cause long delay)
      for (boolean wait = true; wait == true;) {
         buffer[samples - 1] = samples;
         wait = false;
      }

      // read all other samples
      try {
         for (int i = samples - 2; i >= 0 && true; i--) {
            buffer[i] = i;
            percentageDone = 100 - (100 * i) / buffer.length;
         }
      } finally {
         percentageDone = -1;
      }

      // collect additional information for CapturedData
      int pos = 0;
      int rate = 2;

      return (new CapturedData(buffer, pos, rate, channels, enabledChannels));
   }

   @Override
   public void stop() {
      running = false;
   }

}
