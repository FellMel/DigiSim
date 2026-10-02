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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Stores diagram display settings and provides a dialog for changing them.
 *
 *
 * @author Kai Uwe Bachmann
 */
public class DiagramSettings extends AbstractDialog {

   private static final long serialVersionUID = 1L;

   /** display a group in 8 channel logic level view (used in <code>groupSettings</code>) */
   public final static int DISPLAY_CHANNELS = 1;
   /** display a group in a 8bit resolution scope view (used in <code>groupSettings</code>) */
   public final static int DISPLAY_SCOPE = 2;
   /** display a group in a 8bit hex value view (used in <code>groupSettings</code>) */
   public final static int DISPLAY_BYTE = 4;

   /**
    * Display settings for each group. Can be any combinations (ored) of the defined MODE_* values.
    */
   private int[] groupSettings;

   private JCheckBox[][] groupSettingBoxes;

   /**
    * Constructs diagram settings component.
    */
   public DiagramSettings() {
      super();
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      JPanel modePane = new JPanel();
      modePane.setLayout(new GridBagLayout());
      modePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Group Display Settings"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      groupSettingBoxes = new JCheckBox[4][3];
      for (int i = 0; i < 4; i++) {
         modePane.add(new JLabel("Group " + i + ": "), createConstraints(0, i, 1, 1, 0, 0));
         groupSettingBoxes[i][0] = new JCheckBox();
         modePane.add(groupSettingBoxes[i][0], createConstraints(1, i, 1, 1, 0, 0));
         modePane.add(new JLabel("Channels"), createConstraints(2, i, 1, 1, 0, 0));
         groupSettingBoxes[i][1] = new JCheckBox();
         modePane.add(groupSettingBoxes[i][1], createConstraints(3, i, 1, 1, 0, 0));
         modePane.add(new JLabel("Scope"), createConstraints(4, i, 1, 1, 0, 0));
         groupSettingBoxes[i][2] = new JCheckBox();
         modePane.add(groupSettingBoxes[i][2], createConstraints(5, i, 1, 1, 0, 0));
         modePane.add(new JLabel("Byte Value"), createConstraints(6, i, 1, 1, 0, 0));
      }

      add(modePane, createConstraints(0, 0, 2, 1, 0, 0));

      // TODO: separated panel
      JButton ok = new JButton("Ok");
      ok.addActionListener(l -> doOk());
      add(ok, createConstraints(0, 1, 1, 1, 0.5, 0));

      JButton cancel = new JButton("Cancel");
      cancel.addActionListener(l -> doCancel());
      add(cancel, createConstraints(1, 1, 1, 1, 0.5, 0));

      groupSettings = new int[4];
      for (int i = 0; i < groupSettings.length; i++) {
         groupSettings[i] = DISPLAY_CHANNELS | DISPLAY_BYTE;
      }
   }

   @Override
   protected String getTitle() {
      return "Diagram Settings";
   }

   public int[] getGroupSettings() {
      return groupSettings;
   }

   @Override
   protected void updateFields() {
      for (int i = 0; i < 4; i++) {
         for (int j = 0; j < 3; j++) {
            groupSettingBoxes[i][j].setSelected((groupSettings[i] & (1 << j)) > 0);
         }
      }
   }

   @Override
   protected void readFields() {
      for (int i = 0; i < 4; i++) {
         groupSettings[i] = 0;
         for (int j = 0; j < 3; j++) {
            if (groupSettingBoxes[i][j].isSelected()) {
               groupSettings[i] |= 1 << j;
            }
         }
      }
   }

   public void readProperties(Properties properties) {
      String value;

      for (int i = 0; i < 4; i++) {
         value = properties.getProperty("DiagramSettings.group" + i);
         if (value != null) {
            groupSettings[i] = 0;
            if (value.indexOf("channels") >= 0) {
               groupSettings[i] |= DISPLAY_CHANNELS;
            }
            if (value.indexOf("scope") >= 0) {
               groupSettings[i] |= DISPLAY_SCOPE;
            }
            if (value.indexOf("byte") >= 0) {
               groupSettings[i] |= DISPLAY_BYTE;
            }
         }
      }
      updateFields();
   }

   public void writeProperties(Properties properties) {
      for (int i = 0; i < 4; i++) {
         StringBuffer value = new StringBuffer();
         if ((groupSettings[i] & DISPLAY_CHANNELS) != 0) {
            value.append("channels ");
         }
         if ((groupSettings[i] & DISPLAY_SCOPE) != 0) {
            value.append("scope ");
         }
         if ((groupSettings[i] & DISPLAY_BYTE) != 0) {
            value.append("byte ");
         }
         properties.setProperty("DiagramSettings.group" + i, value.toString());
      }
   }

}
