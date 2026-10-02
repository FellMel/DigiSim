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

import java.awt.Component;

import javax.swing.JFrame;

import de.parresum.digisim.gui.analyser.CapturedData;
import de.parresum.digisim.gui.analyser.Configurable;

/**
 * Interface for implementing device controllers. Each device controller must implement at least this interface.
 *
 * @author Frank Kunz
 * @author Kai Uwe Bachmann
 *
 */
public interface DeviceController extends Configurable {

   public static enum DeviceState {
      /** dialog showing and waiting for user action */
      IDLE,
      /** capture currently running */
      RUNNING,
      /** capture / dialog aborted by user */
      ABORTED,
      /** capture finished */
      DONE;
   }

   /**
    * read the captured device data
    *
    * @param parent parent component that requests the data (null if none)
    * @return the captured data or null if no data available
    */
   public CapturedData getDeviceData(Component parent);

   /**
    * update the status of the device controller GUI input fields
    */
   public void updateFields();

   /**
    * shows the device controllers GUI
    *
    * @param frame parent frame
    * @return dialog status
    * @throws Exception when the dialog can not be shown
    */
   public DeviceState showCaptureDialog(JFrame frame) throws Exception;

   /**
    * shows the device controllers GUI and starts the capture with the current settings
    *
    * @param frame parent frame
    * @return dialog status
    * @throws Exception when the dialog can not be shown
    */
   public DeviceState showCaptureProgress(JFrame frame) throws Exception;

   /**
    * get the device controller identification string
    *
    * @return name of the controller
    */
   public String getControllerName();
}
