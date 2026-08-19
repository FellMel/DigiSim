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

import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Stores the diagram labels and provides a dialog to change them.
 *
 * @version 0.7
 * @author Frank Kunz
 * @author Kai Uwe Bachmann
 *
 */
public class DiagramLabels extends AbstractDialog {
   private static final long serialVersionUID = 1L;

   private String[] diagramLabels;
   private JTextField[] labelFields;

   /**
    * Constructs diagram labels component.
    */
   public DiagramLabels() {
      super();
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      JPanel modePane = new JPanel();
      modePane.setLayout(new GridBagLayout());
      modePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Diagram Labels"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      labelFields = new JTextField[32];
      diagramLabels = new String[32];
      for (int col = 0; col < 2; col++) {
         for (int row = 0; row < 16; row++) {
            int num = 16 * col + row;
            modePane.add(new JLabel("Channel " + num + ": "), createConstraints(2 * col, row, 1, 1, 0, 0));
            labelFields[num] = new JTextField(20);
            modePane.add(labelFields[num], createConstraints(2 * col + 1, row, 1, 1, 0, 0));
            diagramLabels[num] = new String();
         }
      }
      add(modePane, createConstraints(0, 0, 5, 1, 0, 0));

      // TODO: separated panel
      JButton ok = new JButton("Ok");
      ok.addActionListener(_ -> doOk());
      add(ok, createConstraints(0, 1, 1, 1, 0.34, 0));

      JButton cancel = new JButton("Cancel");
      cancel.addActionListener(_ -> doCancel());
      add(cancel, createConstraints(1, 1, 1, 1, 0.33, 0));

      JButton clear = new JButton("Clear");
      clear.addActionListener(_ -> doClear());
      add(clear, createConstraints(2, 1, 1, 1, 0.33, 0));
   }

   @Override
   protected String getTitle() {
      return "Diagram Labels";
   }

   public String[] getDiagramLabels() {
      return diagramLabels;
   }

   @Override
   protected void updateFields() {
      for (int i = 0; i < 32; i++) {
         labelFields[i].setText(diagramLabels[i]);
      }
   }

   @Override
   protected void readFields() {
      for (int i = 0; i < 32; i++) {
         diagramLabels[i] = new String(labelFields[i].getText());
      }

   }

   private void doClear() {
      for (int i = 0; i < 32; i++) {
         labelFields[i].setText("");
      }

   }

   /**
    * Reads user settings from given properties. Uses the property prefix "DiagramLabels".
    *
    * @param properties properties to read settings from
    */
   public void readProperties(Properties properties) {
      for (int i = 0; i < 32; i++) {
         if (properties.containsKey("DiagramLabels.channel" + i)) {
            diagramLabels[i] = properties.getProperty("DiagramLabels.channel" + i);
         }
      }
   }

   /**
    * Writes user settings to given properties. Uses the property prefix "DiagramLabels".
    *
    * @param properties properties to write settings to
    */
   public void writeProperties(Properties properties) {
      for (int i = 0; i < 32; i++) {
         properties.setProperty("DiagramLabels.channel" + i, diagramLabels[i]);
      }
   }

}
