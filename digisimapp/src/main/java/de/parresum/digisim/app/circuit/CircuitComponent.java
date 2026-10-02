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

package de.parresum.digisim.app.circuit;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.Strings;

import de.parresum.digisim.app.project.SchemeNode;
import de.parresum.digisim.core.Circuit;
import de.parresum.digisim.gui.InputPanel;
import de.parresum.digisim.gui.OutputPanel;
import de.parresum.digisim.lib.InputPart;
import de.parresum.digisim.lib.OutputPart;
import de.parresum.digisim.lib.io.Input;
import de.parresum.digisim.lib.io.Output;
import de.parresum.digisim.parser.Parser;

/**
 * Component which shows the circuit.
 *
 * @author Kai Uwe Bachmann
 */
public class CircuitComponent extends JPanel {

   private static final long serialVersionUID = -3535918475429640972L;
   private final SchemeNode document;
   private final Circuit circuit;

   private SchemePanel schemaPanel;

   /** Panel holding input elements */
   private final InputPanel input = new InputPanel();

   /** Panel holding output elements */
   private final OutputPanel output = new OutputPanel();

   public CircuitComponent(File baseDir, SchemeNode document) {
      super(new BorderLayout());
      circuit = Parser.parseCircuit(baseDir, document.getScheme());
      this.document = document;
      // add(new JLabel(document.getName()));
      init();
   }

   private void init() {
      this.add(input, BorderLayout.NORTH);
      this.add(output, BorderLayout.SOUTH);
      List<InputPart> in = circuit.getInputs();
      Collections.sort(in, (a, b) -> {
         return Strings.CI.compare(a.getName(), b.getName());
      });
      for (InputPart input : in) {
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
      this.schemaPanel = new SchemePanel(circuit);
      this.add(new JScrollPane(schemaPanel), BorderLayout.CENTER);
   }

   public SchemeNode getDocument() {
      return document;
   }

   /**
    *
    */
   public void zoomIn() {
      schemaPanel.zoomIn();
   }

   /**
    *
    */
   public void zoomOut() {
      schemaPanel.zoomOut();
   }

   /**
    *
    */
   public void zoomFit() {
      schemaPanel.zoomFit();

   }

   public boolean canZoomIn() {
      return schemaPanel.canZoomIn();
   }

   public boolean canZoomOut() {
      return schemaPanel.canZoomOut();
   }

}
