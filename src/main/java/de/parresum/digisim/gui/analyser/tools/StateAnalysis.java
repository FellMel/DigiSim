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
package de.parresum.digisim.gui.analyser.tools;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import de.parresum.digisim.gui.analyser.CapturedData;
import de.parresum.digisim.gui.analyser.Configurable;

/**
 * Tool to convert captured data for state analysis using a user selected channel as clock. Whether sampling should be
 * performed on rising or falling edge can be selected too.
 *
 * @author Kai Uwe Bachmann
 */
public class StateAnalysis extends Base implements Tool, Configurable {

   private StateAnalysisDialog sad;

   public StateAnalysis() {
   }

   @Override
   public void init(Frame frame) {
      sad = new StateAnalysisDialog(frame, getName());
   }

   /**
    * Returns the tools visible name.
    *
    * @return the tools visible name
    */
   @Override
   public String getName() {
      return ("State Analysis...");
   }

   /**
    * Convert captured data from timing data to state data using the given channel as clock.
    *
    * @param data - captured data to work on
    */
   @Override
   public CapturedData process(CapturedData data) {
      // if no data exists or init has has not been called, return null
      if (data == null || sad == null) {
         return (null);
      }

      // if function has been cancelled by the user, return null
      if (sad.showDialog() == StateAnalysisDialog.CANCEL) {
         return (null);
      }

      // obtain user choices
      int number = sad.channel;
      int level = (sad.edge == StateAnalysisDialog.RISING ? 0 : 1); // this seems overly complicated right now, but
                                                                    // RISING might change

      // obtain data from captured data
      int[] values = data.getValues();
      long triggerPosition = data.getTriggerPosition();

      // calculate new sample array size
      int last = values[0] & 1 << number;
      int size = 0;
      for (int i = 0; i < values.length; i++) {
         int current = values[i] & 1 << number;
         if (last == level && current != level) {
            size++;
         }
         last = current;
      }

      // convert captured data
      last = values[0] & 1 << number;
      int pos = 0;
      int newTrigger = -1;
      int[] newValues = new int[size];
      for (int i = 0; i < values.length; i++) {
         int current = values[i] & 1 << number;
         if (last == level && current != level) {
            newValues[pos++] = values[i - 1];
         }
         if (triggerPosition == i) {
            newTrigger = pos;
         }
         last = current;
      }

      // return new data
      return (new CapturedData(newValues, newTrigger, CapturedData.NOT_AVAILABLE, data.getChannels(),
            data.getEnabledChannels()));
   }

   /**
    * Reads dialog settings from given properties.
    *
    * @param properties Properties containing dialog settings
    */
   @Override
   public void readProperties(Properties properties) {
      sad.readProperties(properties);
   }

   /**
    * Writes dialog settings to given properties.
    *
    * @param properties Properties where the settings are written to
    */
   @Override
   public void writeProperties(Properties properties) {
      sad.writeProperties(properties);
   }

   private class StateAnalysisDialog extends JDialog {
      private static final long serialVersionUID = 1L;
      public final static int CANCEL = 0;
      public final static int OK = 1;

      public final static int RISING = 0;
      public final static int FALLING = 1;

      public int channel;
      public int edge;

      private JComboBox<String> edgeSelect;
      private JComboBox<String> channelSelect;
      private String[] edges;
      private String[] channels;
      private int result;

      public StateAnalysisDialog(Frame frame, String name) {
         super(frame, name, true);
         Container pane = getContentPane();
         pane.setLayout(new GridLayout(3, 2, 5, 5));
         getRootPane().setBorder(BorderFactory.createLineBorder(getBackground(), 5));

         // TODO: use cb-model
         channels = new String[32];
         for (int i = 0; i < channels.length; i++) {
            channels[i] = Integer.toString(i);
         }
         channelSelect = new JComboBox<>(channels);
         channelSelect.addItemListener(_ -> doChannelSelected());
         pane.add(new JLabel("Clock Channel:"));
         pane.add(channelSelect);

         // TODO: use cb-model
         String[] tmp = { "Rising", "Falling" };
         edges = tmp;
         edgeSelect = new JComboBox<>(edges);
         edgeSelect.addItemListener(_ -> doEdgeSelected());
         pane.add(new JLabel("Clock Edge:"));
         pane.add(edgeSelect);

         JButton convert = new JButton("Convert");
         convert.addActionListener(_ -> doConvert());
         pane.add(convert);

         JButton cancel = new JButton("Cancel");
         cancel.addActionListener(_ -> doCancel());
         pane.add(cancel);

         pack();
         setResizable(false);
         result = CANCEL;
      }

      public int showDialog() {
         setVisible(true);
         return (result);
      }

      private void doConvert() {
         result = OK;
         setVisible(false);

      }

      private void doCancel() {
         result = CANCEL;
         setVisible(false);

      }

      private void doEdgeSelected() {
         if (((String) edgeSelect.getSelectedItem()).equals("Rising")) {
            edge = RISING;
         } else {
            edge = FALLING;
         }
      }

      private void doChannelSelected() {
         channel = Integer.parseInt((String) channelSelect.getSelectedItem());

      }

      public void readProperties(Properties properties) {
         selectByValue(edgeSelect, edges, properties.getProperty("tools.StateAnalysis.edge"));
         selectByValue(channelSelect, channels, properties.getProperty("tools.StateAnalysis.channel"));
      }

      public void writeProperties(Properties properties) {
         properties.setProperty("tools.StateAnalysis.channel", (String) channelSelect.getSelectedItem());
         properties.setProperty("tools.StateAnalysis.edge", (String) edgeSelect.getSelectedItem());
      }
   }

}
