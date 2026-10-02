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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import de.parresum.digisim.gui.analyser.CapturedData;

// TODO: when the dialog is closed using the window decoration's close function, close() is not called

/**
 * GUI Component that allows the user to control the device and start captures.
 * <p>
 * Its modelled after JFileChooser and should allow for non-dialog implementations making it somewhat reusable.
 *
 * @version 0.7
 * @author Michael "Mr. Sump" Poppitz
 * @author Kai Uwe Bachmann
 *
 */
public class FpgaDeviceController extends JComponent implements DeviceController, Runnable {
   private Thread worker;
   private Timer timer;

   private final JComboBox<String> portSelect;
   private final JComboBox<String> portRateSelect;
   private final JComboBox<String> sourceSelect;
   private final JComboBox<String> speedSelect;
   private final JComboBox<String> sizeSelect;
   private final JComboBox<String> ratioSelect;
   private final JCheckBox filterEnable;
   private final JCheckBox rleEnable;
   private final JCheckBox triggerEnable;
   private final JComboBox<String> triggerTypeSelect;
   private final JTabbedPane triggerStageTabs;
   private final JComboBox<String>[] triggerLevel;
   private final JTextField[] triggerDelay;
   private final JComboBox<String>[] triggerMode;
   private final JComboBox<String>[] triggerChannel;
   private final JCheckBox[] triggerStart;
   private final JCheckBox[][] triggerMask;
   private final JCheckBox[][] triggerValue;
   private final JCheckBox[] channelGroup;
   private final JProgressBar progress;
   private final JButton captureButton;

   private JDialog dialog;
   private final FpgaDevice device;
   private CapturedData capturedData;

   private final int triggerStages;

   private DeviceState status;
   private String errorMessage;

   private static final long serialVersionUID = 1L;
   private static final String NAME = "FpgaDeviceController";

   /**
    * Creates an array of check boxes, adds it to the device controller and returns it.
    *
    * @param label label to use on device controller component
    * @return array of created check boxes
    */
   private JCheckBox[] createChannelList(final JPanel pane, final GridBagConstraints constraints) {
      final JCheckBox[] boxes = new JCheckBox[32];

      final Container container = new Container();
      container.setLayout(new GridLayout(1, 32));

      for (int col = 31; col >= 0; col--) {
         final JCheckBox box = new JCheckBox();
         box.setEnabled(false);
         container.add(box);
         if ((col % 8) == 0 && col > 0) {
            container.add(new JLabel());
         }
         boxes[col] = box;
      }

      pane.add(container, constraints);
      return (boxes);
   }

   private static GridBagConstraints createConstraints(final int x, final int y, final int w, final int h,
         final double wx, final double wy) {
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH;
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = w;
      gbc.gridheight = h;
      gbc.weightx = wx;
      gbc.weighty = wy;
      return (gbc);
   }

   /**
    * Constructs device controller component.
    *
    */
   public FpgaDeviceController() {
      super();
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      device = new FpgaDevice();

      // connection pane
      final JPanel connectionPane = new JPanel();
      connectionPane.setLayout(new GridLayout(6, 2, 5, 5));
      connectionPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Connection Settings"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
      final String[] ports = FpgaDevice.getPorts();
      portSelect = new JComboBox<String>(ports);
      connectionPane.add(new JLabel("Analyzer Port:"));
      connectionPane.add(portSelect);

      final String[] portRates = { "115200bps (LL)", "57600bps (LH)", "38400bps (HL)", "19200bps (HH)" };
      portRateSelect = new JComboBox<String>(portRates);
      connectionPane.add(new JLabel("Port Speed (SW1,SW0):"));
      connectionPane.add(portRateSelect);

      connectionPane.add(new JLabel());
      connectionPane.add(new JLabel());
      connectionPane.add(new JLabel());
      connectionPane.add(new JLabel());
      connectionPane.add(new JLabel());
      connectionPane.add(new JLabel());

      add(connectionPane, createConstraints(0, 0, 1, 1, 1.0, 0.5));

      // settings pane
      final JPanel settingsPane = new JPanel();
      settingsPane.setLayout(new GridLayout(6, 2, 5, 5));
      settingsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Analyzer Settings"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      final String[] sources = { "Internal", "External / Rising", "External / Falling" };
      sourceSelect = new JComboBox<String>(sources);
      sourceSelect.addActionListener(_ -> doSourceSelect());
      settingsPane.add(new JLabel("Sampling Clock:"));
      settingsPane.add(sourceSelect);

      final String[] speeds = { "200MHz", "100MHz", "50MHz", "20MHz", "10MHz", "5MHz", "2MHz", "1MHz", "500kHz",
            "200kHz", "100kHz", "50kHz", "20kHz", "10kHz", "1kHz", "500Hz", "200Hz", "100Hz", "50Hz", "20Hz", "10Hz" };
      speedSelect = new JComboBox<String>(speeds);
      speedSelect.setSelectedIndex(1);
      speedSelect.addActionListener(_ -> doSpeedSelect());
      settingsPane.add(new JLabel("Sampling Rate:"));
      settingsPane.add(speedSelect);

      final Container groups = new Container();
      groups.setLayout(new GridLayout(1, 4));
      channelGroup = new JCheckBox[4];
      for (int i = 0; i < channelGroup.length; i++) {
         channelGroup[i] = new JCheckBox(Integer.toString(i));
         channelGroup[i].setSelected(true);
         groups.add(channelGroup[i]);
      }
      settingsPane.add(new JLabel("Channel Groups:"));
      settingsPane.add(groups);

      final String[] sizes = { "256K", "128K", "64K", "32K", "16K", "8K", "4K", "2K", "1K", "512", "256", "128", "64" };
      sizeSelect = new JComboBox<String>(sizes);
      sizeSelect.setSelectedIndex(7);
      settingsPane.add(new JLabel("Recording Size:"));
      settingsPane.add(sizeSelect);

      add(settingsPane, createConstraints(1, 0, 1, 1, 1.0, 0.5));

      filterEnable = new JCheckBox("Enable");
      filterEnable.setSelected(true);
      filterEnable.setEnabled(false);
      settingsPane.add(new JLabel("Noise Filter: "));
      settingsPane.add(filterEnable);

      rleEnable = new JCheckBox("Enable");
      rleEnable.setSelected(false);
      rleEnable.setEnabled(true);
      settingsPane.add(new JLabel("RLE: "));
      settingsPane.add(rleEnable);

      // trigger pane
      final JPanel triggerPane = new JPanel();
      triggerPane.setLayout(new GridBagLayout());
      triggerPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Trigger Settings"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
      triggerEnable = new JCheckBox("Enable");
      triggerEnable.addActionListener(_ -> doTriggerEnable());
      triggerPane.add(new JLabel("Trigger: "), createConstraints(0, 0, 1, 1, 0.0, 1.0));
      triggerPane.add(triggerEnable, createConstraints(1, 0, 1, 1, 0.0, 1.0));
      triggerPane.add(new JLabel(), createConstraints(2, 0, 1, 1, 10.0, 1.0));

      final String[] ratios = { "0/100", "25/75", "50/50", "75/25", "100/0" };
      ratioSelect = new JComboBox<String>(ratios);
      ratioSelect.setSelectedIndex(2);
      triggerPane.add(new JLabel("Before/After Ratio: "), createConstraints(0, 1, 1, 1, 0.5, 1.0));
      triggerPane.add(ratioSelect, createConstraints(1, 1, 1, 1, 0.5, 1.0));

      final String[] types = { "Simple", "Complex" };
      triggerTypeSelect = new JComboBox<String>(types);
      triggerTypeSelect.addActionListener(_ -> updateFields());
      triggerPane.add(new JLabel("Type: "), createConstraints(0, 2, 1, 1, 0.5, 1.0));
      triggerPane.add(triggerTypeSelect, createConstraints(1, 2, 1, 1, 0.5, 1.0));

      triggerPane.add(new JLabel(" "), createConstraints(0, 3, 1, 1, 1.0, 1.0));

      triggerStageTabs = new JTabbedPane();
      triggerStages = device.getTriggerStageCount();
      triggerMask = new JCheckBox[triggerStages][];
      triggerValue = new JCheckBox[triggerStages][];
      triggerLevel = new JComboBox[triggerStages];
      triggerDelay = new JTextField[triggerStages];
      triggerMode = new JComboBox[triggerStages];
      triggerChannel = new JComboBox[triggerStages];
      triggerStart = new JCheckBox[triggerStages];
      for (int i = 0; i < triggerStages; i++) {
         final JPanel stagePane = new JPanel();
         stagePane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         stagePane.setLayout(new GridBagLayout());

         final String[] levels = { "Immediatly", "On Level 1", "On Level 2", "On Level 3" };
         triggerLevel[i] = new JComboBox<String>(levels);
         if (i > 0) {
            triggerLevel[i].setSelectedIndex(3);
         }
         stagePane.add(new JLabel("Arm:"), createConstraints(0, 0, 1, 1, 1.0, 1.0));
         stagePane.add(triggerLevel[i], createConstraints(1, 0, 1, 1, 0.5, 1.0));
         final String[] modes = { "Parallel", "Serial" };
         triggerMode[i] = new JComboBox<String>(modes);
         stagePane.add(new JLabel("Mode:", JLabel.RIGHT), createConstraints(2, 0, 1, 1, 0.5, 1.0));
         stagePane.add(triggerMode[i], createConstraints(3, 0, 1, 1, 0.5, 1.0));
         triggerMode[i].addActionListener(_ -> updateFields());
         final String[] channels = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
               "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" };
         triggerChannel[i] = new JComboBox<String>(channels);
         stagePane.add(new JLabel("Channel:", JLabel.RIGHT), createConstraints(4, 0, 1, 1, 0.5, 1.0));
         stagePane.add(triggerChannel[i], createConstraints(5, 0, 1, 1, 0.5, 1.0));

         stagePane.add(new JLabel("31"), createConstraints(1, 1, 1, 1, 1.0, 1.0));
         stagePane.add(new JLabel("0", JLabel.RIGHT), createConstraints(5, 1, 1, 1, 1.0, 1.0));
         stagePane.add(new JLabel("Mask:"), createConstraints(0, 2, 1, 1, 1.0, 1.0));
         triggerMask[i] = createChannelList(stagePane, createConstraints(1, 2, 5, 1, 1.0, 1.0));
         stagePane.add(new JLabel("Value:"), createConstraints(0, 3, 1, 1, 1.0, 1.0));
         triggerValue[i] = createChannelList(stagePane, createConstraints(1, 3, 5, 1, 1.0, 1.0));

         stagePane.add(new JLabel("Action:"), createConstraints(0, 4, 1, 1, 1.0, 1.0));
         triggerStart[i] = new JCheckBox("Start Capture    (otherwise trigger level will rise by one)");
         stagePane.add(triggerStart[i], createConstraints(1, 4, 3, 1, 1.0, 1.0));
         stagePane.add(new JLabel("Delay:", JLabel.RIGHT), createConstraints(4, 4, 1, 1, 0.5, 1.0));
         triggerDelay[i] = new JTextField("0");
         stagePane.add(triggerDelay[i], createConstraints(5, 4, 1, 1, 0.5, 1.0));
         triggerStageTabs.add("Stage " + i, stagePane);
      }
      triggerPane.add(triggerStageTabs, createConstraints(0, 4, 3, 1, 1.0, 1.0));
      add(triggerPane, createConstraints(0, 2, 3, 2, 1.0, 0.5));

      // progress pane
      final JPanel progressPane = new JPanel();
      progressPane.setLayout(new BorderLayout());
      progressPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Progress"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
      progress = new JProgressBar(0, 100);
      progressPane.add(progress, BorderLayout.CENTER);
      add(progressPane, createConstraints(0, 4, 3, 1, 1.0, 0));

      add(new JLabel(), createConstraints(0, 5, 1, 1, 0.5, 0));

      captureButton = new JButton("Capture");
      captureButton.addActionListener(_ -> doCapture());
      add(captureButton, createConstraints(1, 5, 1, 1, 0.5, 0));

      final JButton cancel = new JButton("Close");
      cancel.addActionListener(_ -> close());
      add(cancel, createConstraints(2, 5, 1, 1, 0.5, 0));

      capturedData = null;
      timer = null;
      worker = null;
      status = DeviceState.IDLE;
   }

   /**
    * Internal method that initializes a dialog and add this component to it.
    *
    * @param frame owner of the dialog
    */
   private void initDialog(final JFrame frame) {
      // check if dialog exists with different owner and dispose if so
      if (dialog != null && dialog.getOwner() != frame) {
         dialog.dispose();
         dialog = null;
      }
      // if no valid dialog exists, create one
      if (dialog == null) {
         dialog = new JDialog(frame, "Capture", true);
         dialog.getContentPane().add(this);
         dialog.setResizable(false);
         dialog.setSize(this.getPreferredSize());
         // dialog.pack();
      }
      // reset progress bar
      progress.setValue(0);

      // sync dialog status with device
      updateFields();
   }

   /**
    * Return the device data of the last successful run.
    *
    * @return device data
    */
   @Override
   public CapturedData getDeviceData(final Component parent) {
      return (capturedData);
   }

   /**
    * Extracts integers from strings regardless of trailing trash.
    *
    * @param s string to be parsed
    * @return integer value, 0 if parsing fails
    */
   private int smartParseInt(final String s) {
      int val = 0;

      try {
         for (int i = 1; i <= s.length(); i++) {
            val = Integer.parseInt(s.substring(0, i));
         }
      } catch (final NumberFormatException E) {
      }

      return (val);
   }

   /**
    * Sets the enabled state of all available trigger check boxes and the ratio select.
    *
    * @param enable <code>true</code> to enable trigger configuration fields, <code>false</code> to disable them
    */
   private void setTriggerEnabled(final boolean enable) {
      final int channels = device.getAvailableChannelCount();
      final boolean complex = "Complex".equals(triggerTypeSelect.getSelectedItem());
      if (!complex) {
         triggerStageTabs.setSelectedIndex(0);
      }
      triggerTypeSelect.setEnabled(enable);
      ratioSelect.setEnabled(enable);

      for (int stage = 0; stage < triggerStages; stage++) {
         for (int i = 0; i < channels; i++) {
            triggerMask[stage][i].setEnabled(enable);
            triggerValue[stage][i].setEnabled(enable);
         }
         for (int i = channels; i < 32; i++) {
            triggerMask[stage][i].setEnabled(false);
            triggerValue[stage][i].setEnabled(false);
         }
         triggerStageTabs.setEnabledAt(stage, enable && (stage == 0 || complex));
         triggerLevel[stage].setEnabled(enable && complex);
         triggerDelay[stage].setEnabled(enable);
         triggerMode[stage].setEnabled(enable);
         if (enable && triggerMode[stage].getSelectedIndex() == 1) {
            triggerChannel[stage].setEnabled(true);
         } else {
            triggerChannel[stage].setEnabled(false);
         }
         triggerStart[stage].setEnabled(enable && complex);
      }
   }

   /**
    * Sets the enabled state of all configuration components of the dialog.
    *
    * @param enable <code>true</code> to enable components, <code>false</code> to disable them
    */
   private void setDialogEnabled(final boolean enable) {
      triggerEnable.setEnabled(enable);
      captureButton.setEnabled(enable);
      portSelect.setEnabled(enable);
      portRateSelect.setEnabled(enable);
      speedSelect.setEnabled(enable);
      sizeSelect.setEnabled(enable);
      updateFields(enable);
   }

   /** activates / deactivates dialog options according to device status */
   @Override
   public void updateFields() {
      updateFields(true);
   }

   /** activates / deactivates dialog options according to device status */
   private void updateFields(final boolean enable) {
      triggerEnable.setSelected(device.isTriggerEnabled());
      setTriggerEnabled(device.isTriggerEnabled());
      filterEnable.setEnabled(device.isFilterAvailable() && enable);
      for (int i = 0; i < channelGroup.length; i++) {
         channelGroup[i].setEnabled(enable && (i < device.getAvailableChannelCount() / 8));
      }
      speedSelect.setEnabled(device.getClockSource() == FpgaDevice.CLOCK_INTERNAL);
   }

   /** writes the dialog settings to the device */
   private void updateDevice() {
      String value;

      // set clock source
      value = (String) sourceSelect.getSelectedItem();
      if (value.equals("Internal")) {
         device.setClockSource(FpgaDevice.CLOCK_INTERNAL);
      } else if (value.equals("External / Rising")) {
         device.setClockSource(FpgaDevice.CLOCK_EXTERNAL_RISING);
      } else {
         device.setClockSource(FpgaDevice.CLOCK_EXTERNAL_FALLING);
      }

      // set sample rate
      value = (String) speedSelect.getSelectedItem();
      int f = smartParseInt(value);
      if (value.indexOf("M") > 0) {
         f *= 1000000;
      } else if (value.indexOf("k") > 0) {
         f *= 1000;
      }
      device.setRate(f);

      // set sample count
      value = (String) sizeSelect.getSelectedItem();
      int s = smartParseInt(value);
      if (value.indexOf("K") > 0) {
         s *= 1024;
      }
      device.setSize(s);

      // set before / after ratio
      value = (String) ratioSelect.getSelectedItem();
      double r = 0.5;
      if (value.equals("100/0")) {
         r = 0;
      } else if (value.equals("25/75")) {
         r = 0.75;
      } else if (value.equals("50/50")) {
         r = 0.5;
      } else if (value.equals("75/25")) {
         r = 0.25;
      } else if (value.equals("0/100")) {
         r = 1;
      }
      device.setRatio(r);

      // set filter
      device.setFilterEnabled(filterEnable.isSelected());
      device.setRleEnabled(rleEnable.isSelected());

      // set trigger
      final boolean triggerEnabled = triggerEnable.isSelected();
      device.setTriggerEnabled(triggerEnabled);
      if (triggerEnabled) {
         final boolean complex = "Complex".equals(triggerTypeSelect.getSelectedItem());
         for (int stage = 0; stage < triggerStages; stage++) {
            int m = 0;
            int v = 0;
            for (int i = 0; i < 32; i++) {
               if (triggerMask[stage][i].isSelected()) {
                  m |= 1 << i;
               }
               if (triggerValue[stage][i].isSelected()) {
                  v |= 1 << i;
               }
            }
            final int level = triggerLevel[stage].getSelectedIndex();
            final int delay = smartParseInt(triggerDelay[stage].getText());
            final int channel = triggerChannel[stage].getSelectedIndex();
            final boolean startCapture = triggerStart[stage].isSelected();
            if (complex) {
               if (triggerMode[stage].getSelectedIndex() == 0) {
                  device.setParallelTrigger(stage, m, v, level, delay, startCapture);
               } else {
                  device.setSerialTrigger(stage, channel, m, v, level, delay, startCapture);
               }
            } else if (stage == 0) {
               if (triggerMode[stage].getSelectedIndex() == 0) {
                  device.setParallelTrigger(stage, m, v, 0, delay, true);
               } else {
                  device.setSerialTrigger(stage, channel, m, v, 0, delay, true);
               }
            } else {
               // make sure stages > 0 will not interfere
               device.setParallelTrigger(stage, 0, 0, 3, 0, false);
            }
         }
      }

      // set enabled channel groups
      int enabledChannels = 0;
      for (int i = 0; i < channelGroup.length; i++) {
         if (channelGroup[i].isSelected()) {
            enabledChannels |= 0xff << (8 * i);
         }
      }
      device.setEnabledChannels(enabledChannels);
   }

   /**
    * Starts capturing from device. Should not be called externally.
    */
   @Override
   public void run() {
      // TODO: need to check if attach was successful
      device.attach((String) portSelect.getSelectedItem(), smartParseInt((String) portRateSelect.getSelectedItem()));

      status = DeviceState.RUNNING;

      try {
         System.out.println("Run started");
         errorMessage = "";
         capturedData = device.run();
         System.out.println("Run completed");
         status = DeviceState.DONE;
      } catch (final Exception ex) {
         // TODO: could make sense to also return half read captures if array length is corrected
         capturedData = null;
         status = DeviceState.ABORTED;
         System.out.println("Run aborted");
         if (!(ex instanceof InterruptedException)) {
            errorMessage = ex.getMessage();
            ex.printStackTrace(System.out);
         }
      }
      device.detach();
   }

   /**
    * Properly closes the dialog. This method makes sure timer and worker thread are stopped before the dialog is
    * closed.
    *
    */
   private void close() {
      if (timer != null) {
         timer.stop();
         timer = null;
      }
      if (worker != null) {
         device.stop(); // lets hope no one gets here before device.run() is called
         worker.interrupt();
         worker = null;
      }
      dialog.setVisible(false);
   }

   /**
    * Starts the capture thread.
    */
   private void startCapture() {
      try {
         setDialogEnabled(false);
         timer = new Timer(100, _ -> doTimer());
         worker = new Thread(this);
         timer.start();
         worker.start();
      } catch (final Exception E) {
         E.printStackTrace(System.out);
      }
   }

   private void doSourceSelect() {
      updateDevice();
      updateFields();

   }

   private void doSpeedSelect() {
      updateDevice();
      updateFields();

   }

   private void doTriggerEnable() {
      updateDevice();
      updateFields();

   }

   private void doCapture() {
      updateDevice();
      startCapture();

   }

   private void doTimer() {
      if (status == DeviceState.DONE) {
         close();
      } else if (status == DeviceState.ABORTED) {
         timer.stop();
         JOptionPane.showMessageDialog(this,
               "Error while trying to communicate with device:\n\n" + "\"" + errorMessage + "\"\n\n"
                     + "Make sure the device is:\n" + " - connected to the specified port\n"
                     + " - turned on and properly programmed\n" + " - set to the selected transfer rate\n",
               "Communication Error", JOptionPane.ERROR_MESSAGE);
         setDialogEnabled(true);
      } else if (device.isRunning()) {
         progress.setValue(device.getPercentage());
      }

   }

   private void selectByValue(final JComboBox<String> box, final String value) {
      if (value != null) {
         for (int i = 0; i < box.getItemCount(); i++) {
            if (value.equals(box.getItemAt(i))) {
               box.setSelectedIndex(i);
            }
         }
      }
   }

   @Override
   public void readProperties(final Properties properties) {
      selectByValue(portSelect, properties.getProperty(NAME + ".port"));
      selectByValue(portRateSelect, properties.getProperty(NAME + ".portRate"));
      selectByValue(sourceSelect, properties.getProperty(NAME + ".source"));
      selectByValue(speedSelect, properties.getProperty(NAME + ".speed"));
      selectByValue(sizeSelect, properties.getProperty(NAME + ".size"));
      selectByValue(ratioSelect, properties.getProperty(NAME + ".ratio"));
      filterEnable.setSelected("true".equals(properties.getProperty(NAME + ".filter")));
      triggerEnable.setSelected("true".equals(properties.getProperty(NAME + ".trigger")));
      selectByValue(triggerTypeSelect, properties.getProperty(NAME + ".triggerType"));

      for (int stage = 0; stage < triggerStages; stage++) {
         selectByValue(triggerLevel[stage], properties.getProperty(NAME + ".triggerStage" + stage + "Level"));
         triggerDelay[stage].setText(properties.getProperty(NAME + ".triggerStage" + stage + "Delay"));
         selectByValue(triggerMode[stage], properties.getProperty(NAME + ".triggerStage" + stage + "Mode"));
         selectByValue(triggerChannel[stage], properties.getProperty(NAME + ".triggerStage" + stage + "Channel"));

         final String mask = properties.getProperty(NAME + ".triggerStage" + stage + "Mask");
         if (mask != null) {
            for (int i = 0; i < 32 && i < mask.length(); i++) {
               triggerMask[stage][i].setSelected(mask.charAt(i) == '1');
            }
         }

         final String value = properties.getProperty(NAME + ".triggerStage" + stage + "Value");
         if (value != null) {
            for (int i = 0; i < 32 && i < value.length(); i++) {
               triggerValue[stage][i].setSelected(value.charAt(i) == '1');
            }
         }

         triggerStart[stage]
               .setSelected("true".equals(properties.getProperty(NAME + ".triggerStage" + stage + "StartCapture")));
      }

      final String group = properties.getProperty(NAME + ".channelGroup");
      if (group != null) {
         for (int i = 0; i < 4 && i < group.length(); i++) {
            channelGroup[i].setSelected(group.charAt(i) == '1');
         }
      }

      updateDevice();
      updateFields();
   }

   @Override
   public void writeProperties(final Properties properties) {
      properties.setProperty(NAME + ".port", (String) portSelect.getSelectedItem());
      properties.setProperty(NAME + ".portRate", (String) portRateSelect.getSelectedItem());
      properties.setProperty(NAME + ".source", (String) sourceSelect.getSelectedItem());
      properties.setProperty(NAME + ".speed", (String) speedSelect.getSelectedItem());
      properties.setProperty(NAME + ".size", (String) sizeSelect.getSelectedItem());
      properties.setProperty(NAME + ".ratio", (String) ratioSelect.getSelectedItem());
      properties.setProperty(NAME + ".filter", filterEnable.isSelected() ? "true" : "false");
      properties.setProperty(NAME + ".trigger", triggerEnable.isSelected() ? "true" : "false");
      properties.setProperty(NAME + ".triggerType", (String) triggerTypeSelect.getSelectedItem());

      for (int stage = 0; stage < triggerStages; stage++) {
         properties.setProperty(NAME + ".triggerStage" + stage + "Level",
               (String) triggerLevel[stage].getSelectedItem());
         properties.setProperty(NAME + ".triggerStage" + stage + "Delay", triggerDelay[stage].getText());
         properties.setProperty(NAME + ".triggerStage" + stage + "Mode", (String) triggerMode[stage].getSelectedItem());
         properties.setProperty(NAME + ".triggerStage" + stage + "Channel",
               (String) triggerChannel[stage].getSelectedItem());

         final StringBuffer mask = new StringBuffer();
         for (int i = 0; i < 32; i++) {
            mask.append(triggerMask[stage][i].isSelected() ? "1" : "0");
         }
         properties.setProperty(NAME + ".triggerStage" + stage + "Mask", mask.toString());

         final StringBuffer value = new StringBuffer();
         for (int i = 0; i < 32; i++) {
            value.append(triggerValue[stage][i].isSelected() ? "1" : "0");
         }
         properties.setProperty(NAME + ".triggerStage" + stage + "Value", value.toString());

         properties.setProperty(NAME + ".triggerStage" + stage + "StartCapture",
               triggerStart[stage].isSelected() ? "true" : "false");
      }

      final StringBuffer group = new StringBuffer();
      for (int i = 0; i < 4; i++) {
         group.append(channelGroup[i].isSelected() ? "1" : "0");
      }
      properties.setProperty(NAME + ".channelGroup", group.toString());
   }

   @Override
   public String getControllerName() {
      return "FPGA Controller";
   }

   /**
    * Displays the device controller dialog with enabled configuration portion and waits for user input.
    *
    * @param frame parent frame of this dialog
    * @return status, which is either <code>ABORTED</code> or <code>DONE</code>
    * @throws Exception
    */
   @Override
   public DeviceState showCaptureDialog(final JFrame frame) throws Exception {
      status = DeviceState.IDLE;
      initDialog(frame);
      setDialogEnabled(true);
      dialog.setVisible(true);
      return status;
   }

   /**
    * Displays the device controller dialog with disabled configuration, starting capture immediately.
    *
    * @param frame parent frame of this dialog
    * @return status, which is either <code>ABORTED</code> or <code>DONE</code>
    * @throws Exception
    */
   @Override
   public DeviceState showCaptureProgress(final JFrame frame) throws Exception {
      status = DeviceState.IDLE;
      initDialog(frame);
      startCapture();
      dialog.setVisible(true);
      return status;
   }

}
