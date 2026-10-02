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

package de.parresum.digisim.gui;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import de.parresum.digisim.app.circuit.SchemePanel;
import de.parresum.digisim.core.Circuit;
import de.parresum.digisim.lib.InputPart;
import de.parresum.digisim.lib.OutputPart;
import de.parresum.digisim.lib.io.Input;
import de.parresum.digisim.lib.io.Output;

/**
 * General window for simulation
 *
 * @author Kai Uwe Bachmann
 */
public class SimulationWindow extends MainWindow {

   private static final long serialVersionUID = 1092172064186888787L;

   /** Simulated Circuit */
   private final Circuit circuit;

   public SimulationWindow(Circuit circuit) {
      this.circuit = circuit;
      super(circuit.getName());
   }

   @Override
   protected void setup() {
      List<InputPart> in = circuit.getInputs();
//      Collections.sort(in, (a, b) -> {
//         return a.getName().compareToIgnoreCase(b.getName());
//      });
      for (InputPart input : circuit.getInputs()) {
         if (input instanceof JComponent) {
            this.input.add((JComponent) input);
         } else {
            this.input.add(new Input(input.getName(), input.getOutput()));
         }
      }

      Collections.sort(circuit.getOutputs(), (a, b) -> {
         return a.getName().compareToIgnoreCase(b.getName());

      });
      for (OutputPart output : circuit.getOutputs()) {
         if (output instanceof JComponent) {
            this.output.add((JComponent) output);
         } else {
            this.output.add(new Output(output.getName(), output.getInput()));
         }
      }
      this.add(new SchemePanel(circuit), BorderLayout.CENTER);
//      circuit.start();
   }
}
