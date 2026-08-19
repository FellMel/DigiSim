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
package de.parresum.digisim.gui.analyser;

import java.io.File;

import javax.swing.SwingUtilities;

import de.parresum.digisim.core.Clock;
import de.parresum.digisim.gui.analyser.devices.WireDevice;
import de.parresum.digisim.gui.analyser.devices.WireDeviceController;

/**
 * Loader for the Logic Analyzer Client.
 * <p>
 * Processes command arguments and starts the UI. After the UI is closed it terminates the VM.
 * <p>
 * See description for {@@link Loader#main(String[])} for details on supported arguments.
 *
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 * @author Kai Uwe Bachmann
 *
 */
public class Loader {

   /**
    * Constructs a new loader.
    */
   public Loader() {
   }

   /**
    * Starts up the logic analyzer client. Project ("*.slp") and data ("*.sla") files can be supplied as arguments. The
    * files will then be loaded automatically. If a file cannot be read, the client will exit.
    *
    * @param args arguments
    */
   public static void main(String[] args) {
      Clock clock = new Clock("clk", 100);
      WireDevice device = new WireDevice("Clock test");
      device.setClockWire(clock.getOutput());
      device.setWire(1, clock.getOutput3());
      device.setWire(2, clock.getOutput2());
      device.setStarter(clock);
      WireDeviceController.addDevice(device);

      AnalyserWindow w = new AnalyserWindow();

      for (int i = 0; i < args.length; i++) {
         String arg = args[i];

         // handle options (there aren't any yet)
         if (arg.startsWith("-")) {
            System.out.println();
            System.out.println("Sumps Logic Analyzer Client");
            System.out.println("Copyright (C) 2006 Michael Poppitz");
            System.out.println("This software is released under the GNU GPL.");
            System.out.println();
            System.out.println("Usage: run [<project file>] [<data file>]");
            System.out.println();
            System.out.println("	<project file> is a saved project with file extension \".slp\"");
            System.out.println("	<data file> is saved data with file extension \".sla\"");
            System.out.println();
            System.exit(0);

            // handle file arguments
         } else {
            try {
               File f = new File(arg);
               if (!f.isFile()) {
                  System.out.println("Error: File does not exist: " + arg);
                  System.exit(-1);
               }
               if (arg.toLowerCase().endsWith(".slp")) {
                  w.loadProject(f);
               } else if (arg.toLowerCase().endsWith(".sla")) {
                  w.loadData(f);
               } else {
                  System.out.println("Error: Unknown file type in argument: " + arg);
                  System.exit(-1);
               }
            } catch (Exception e) {
               System.out.println("Error: Exception occured while reading file: " + e.getMessage());
               System.exit(-1);
            }
         }
      }

      try {
         SwingUtilities.invokeAndWait(w);
      } catch (Exception e) {
         System.out.println("Error while invoking application: " + e.getMessage());
         System.exit(-1);
      }
   }
}
