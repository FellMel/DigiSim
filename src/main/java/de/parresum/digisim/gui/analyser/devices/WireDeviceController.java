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

import static de.parresum.digisim.gui.analyser.devices.DeviceController.DeviceState.ABORTED;
import static de.parresum.digisim.gui.analyser.devices.DeviceController.DeviceState.DONE;
import static de.parresum.digisim.gui.analyser.devices.DeviceController.DeviceState.IDLE;
import static de.parresum.digisim.gui.analyser.devices.DeviceController.DeviceState.RUNNING;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.Timer;

import de.parresum.digisim.gui.analyser.CapturedData;

/**
 * Device controller for using in simulation
 *
 * @author Kai Uwe Bachmann
 */
public class WireDeviceController extends JComponent implements DeviceController, Runnable {
   private static final String NAME = "WireDeviceController";
   private static List<WireDevice> devices = new ArrayList<>();

   private Thread worker;
   private Timer timer;

   private final JComboBox<SampleSize> sizeSelect;
   private final JCheckBox filterEnable;
   private final JCheckBox[] channelGroup;
   private final JProgressBar progress;
   private final JButton captureButton;

   private JDialog dialog;
   private WireDevice device;
   private CapturedData capturedData;

   private DeviceState status;
   private String errorMessage;

   private static final long serialVersionUID = 1L;

   public static void addDevice(final WireDevice device) {
      devices.add(device);
   }

   /**
    * Constructs device controller component.
    *
    */
   public WireDeviceController() {
      super();
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      if (!devices.isEmpty()) {
         device = devices.get(0);
      } else {
         device = new TestDevice();
      }

      // connection pane
      final JPanel connectionPane = new JPanel();
      connectionPane.setLayout(new GridLayout(6, 2, 5, 5));

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

      sizeSelect = new JComboBox<SampleSize>(SampleSize.values());
      sizeSelect.setSelectedItem(device.getSize());
      settingsPane.add(new JLabel("Recording Size:"));
      settingsPane.add(sizeSelect);

      add(settingsPane, createConstraints(1, 0, 1, 1, 1.0, 0.5));

      filterEnable = new JCheckBox("Enable");
      filterEnable.setSelected(true);
      filterEnable.setEnabled(false);
      settingsPane.add(new JLabel("Noise Filter: "));
      settingsPane.add(filterEnable);

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
      captureButton.addActionListener(a -> {
         updateDevice();
         startCapture();
      });
      add(captureButton, createConstraints(1, 5, 1, 1, 0.5, 0));

      final JButton cancel = new JButton("Close");
      cancel.addActionListener(a -> {
         close();
      });
      add(cancel, createConstraints(2, 5, 1, 1, 0.5, 0));

      capturedData = null;
      timer = null;
      worker = null;
      status = IDLE;
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

   // #############

   @Override
   public void readProperties(final Properties properties) {
      // TODO Auto-generated method stub

   }

   @Override
   public void writeProperties(final Properties properties) {
      // TODO Auto-generated method stub

   }

   @Override
   public void run() {

      status = RUNNING;

      try {
         System.out.println("Run started");
         errorMessage = "";
         capturedData = device.run();
         System.out.println("Run completed");
         status = DONE;
      } catch (final Exception ex) {
         // TODO: could make sense to also return half read captures if array length is corrected
         capturedData = null;
         status = ABORTED;
         System.out.println("Run aborted");
         if (!(ex instanceof InterruptedException)) {
            errorMessage = ex.getMessage();
            ex.printStackTrace(System.out);
         }
      }

   }

   /** writes the dialog settings to the device */
   private void updateDevice() {

      // set sample count
      final SampleSize sizeValue = (SampleSize) sizeSelect.getSelectedItem();
      device.setSize(sizeValue);

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

   @Override
   public CapturedData getDeviceData(final Component parent) {
      return (capturedData);
   }

   @Override
   public void updateFields() {
      updateFields(true);

   }

   @Override
   public DeviceState showCaptureDialog(final JFrame frame) throws Exception {
      status = IDLE;
      initDialog(frame);
      setDialogEnabled(true);
      dialog.setVisible(true);
      return status;
   }

   @Override
   public DeviceState showCaptureProgress(final JFrame frame) throws Exception {
      status = IDLE;
      initDialog(frame);
      startCapture();
      dialog.setVisible(true);
      return status;
   }

   @Override
   public String getControllerName() {
      return "Wire Controller";
   }

   // -------------------------------------------------------------
   /**
    * Starts the capture thread.
    */
   private void startCapture() {
      try {
         setDialogEnabled(false);
         timer = new Timer(100, a -> {
            if (status == DONE) {
               close();
            } else if (status == ABORTED) {
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

         });
         worker = new Thread(this);
         timer.start();
         worker.start();
      } catch (final Exception E) {
         E.printStackTrace(System.out);
      }
   }

   // -------------------------------------------------------------
   /**
    * Internal method that initializes a dialog and add this component to it.
    *
    * @@param frame owner of the dialog
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
    * Sets the enabled state of all configuration components of the dialog.
    *
    * @@param enable <code>true</code> to enable components, <code>false</code> to disable them
    */
   private void setDialogEnabled(final boolean enable) {
      captureButton.setEnabled(enable);
      sizeSelect.setEnabled(enable);
      updateFields(enable);
   }

   // -------------------------------------------------------------
   /** activates / deactivates dialog options according to device status */
   private void updateFields(final boolean enable) {
      setTriggerEnabled(device.isTriggerEnabled());
      filterEnable.setEnabled(device.isFilterAvailable() && enable);
      for (int i = 0; i < channelGroup.length; i++) {
         channelGroup[i].setEnabled(enable && (i < device.getAvailableChannelCount() / 8));
      }
   }

   /**
    * Sets the enabled state of all available trigger check boxes and the ratio select.
    *
    * @@param enable <code>true</code> to enable trigger configuration fields, <code>false</code> to disable them
    */
   private void setTriggerEnabled(final boolean enable) {
      final int channels = device.getAvailableChannelCount();
   }

}
