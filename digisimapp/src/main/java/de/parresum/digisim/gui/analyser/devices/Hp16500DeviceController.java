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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import de.parresum.digisim.gui.analyser.CapturedData;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

/**
 * This is a device controller for basic communication with a HP16500 mainframe. It just supports a readout of the
 * available cards in the cardcage and a selection of one card that can be read out. All trigger, sample rate, etc.
 * settings must be done directly with the analyzer controls.
 *
 * The current implementation supports only a HP16550A logic analyzer card. It reads out the first pod of the selected
 * analyzer and decodes the data.
 *
 * @author Frank Kunz
 * @author Kai Uwe Bachmann
 */
public class Hp16500DeviceController extends JComponent implements Runnable, DeviceController {
   private Thread worker;
   private Timer timer;

   private final JComboBox<String> portSelect;
   private final JComboBox<String> portRateSelect;
   private final JComboBox<String> portParitySelect;
   private final JComboBox<String> portStopSelect;

   private final JComboBox<String> sourceSelect;
   private final JComboBox<String> analyzerSelect;

   private final JProgressBar progress;
   private final JButton captureButton;
   private final JButton configButton;

   private JDialog dialog;
   private DeviceState status;
   private final Hp16500Device device;
   private String errorMessage;

   private CapturedData capturedData;
   private byte[] rawData;

   private static final long serialVersionUID = 1L;
   private static final String NAME = "Hp16500DeviceController";

   private boolean debug;
   private boolean rawStore;

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
    * create a new HP16500 device controller object
    */
   public Hp16500DeviceController() {
      super();
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      debug = false;
      rawStore = false;

      device = new Hp16500Device();

      // connection pane
      final JPanel connectionPane = new JPanel();
      connectionPane.setLayout(new GridLayout(4, 2, 5, 5));
      connectionPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Connection Settings"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
      final String[] ports = Hp16500Device.getPorts();
      portSelect = new JComboBox<String>(ports);
      connectionPane.add(new JLabel("Analyzer Port:"));
      connectionPane.add(portSelect);

      final String[] portRates = { "19200bps", "9600bps", "4800bps", "2400bps", "1200bps", "600bps", "300bps",
            "110bps", };
      portRateSelect = new JComboBox<String>(portRates);
      connectionPane.add(new JLabel("Port Speed:"));
      connectionPane.add(portRateSelect);

      final String[] portParities = { "none", "odd", "even" };
      portParitySelect = new JComboBox<String>(portParities);
      connectionPane.add(new JLabel("Port Parity:"));
      connectionPane.add(portParitySelect);

      final String[] portStopBits = { "1", "1.5", "2" };
      portStopSelect = new JComboBox<String>(portStopBits);
      connectionPane.add(new JLabel("Port Stopbits:"));
      connectionPane.add(portStopSelect);

      add(connectionPane, createConstraints(0, 0, 1, 1, 0, 0));

      // settings pane
      final JPanel settingsPane = new JPanel();
      settingsPane.setLayout(new GridLayout(4, 2, 5, 5));
      settingsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Analyzer Settings"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      final String[] sourceAvailable = { "get config first" };
      sourceSelect = new JComboBox<String>(sourceAvailable);
      settingsPane.add(new JLabel("Card:"));
      settingsPane.add(sourceSelect);

      final String[] analyzerAvailable = { "1", "2" };
      analyzerSelect = new JComboBox<String>(analyzerAvailable);
      settingsPane.add(new JLabel("Analyzer:"));
      settingsPane.add(analyzerSelect);

      settingsPane.add(new JLabel());
      settingsPane.add(new JLabel());
      settingsPane.add(new JLabel());
      settingsPane.add(new JLabel());

      add(settingsPane, createConstraints(1, 0, 2, 1, 0, 0));

      // progress pane
      final JPanel progressPane = new JPanel();
      progressPane.setLayout(new BorderLayout());
      progressPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Progress"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
      progress = new JProgressBar(0, 100);
      progressPane.add(progress, BorderLayout.CENTER);
      add(progressPane, createConstraints(0, 4, 3, 1, 0, 0));

      captureButton = new JButton("Capture");
      captureButton.addActionListener(_ -> doCapture());
      add(captureButton, createConstraints(0, 5, 1, 1, 0, 0));

      configButton = new JButton("Get Config");
      configButton.addActionListener(_ -> doGetConfig());
      add(configButton, createConstraints(1, 5, 1, 1, 0, 0));

      final JButton cancel = new JButton("Close");
      cancel.addActionListener(_ -> close());
      add(cancel, createConstraints(2, 5, 1, 1, 0, 0));

      rawData = null;
      capturedData = null;
      timer = null;
      worker = null;
      status = DeviceState.IDLE;
   }

   private void doCapture() {
      if (!debug && (sourceSelect.getSelectedItem().equals("get config first")
            || sourceSelect.getSelectedItem().equals(""))) {
         JOptionPane.showMessageDialog(null, "use \"Get Config\" first to select analyzer card", "Error",
               JOptionPane.ERROR_MESSAGE);
      } else {
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
   }

   private void doGetConfig() {
      try {
         final int portRate = smartParseInt((String) portRateSelect.getSelectedItem());
         int portParity = SerialPort.PARITY_NONE;
         if (portParitySelect.getSelectedItem().equals("odd")) {
            portParity = SerialPort.PARITY_ODD;
         } else if (portParitySelect.getSelectedItem().equals("even")) {
            portParity = SerialPort.PARITY_EVEN;
         }
         int portStopbits = SerialPort.STOPBITS_1;
         if (portStopSelect.getSelectedItem().equals("1.5")) {
            portStopbits = SerialPort.STOPBITS_1_5;
         } else if (portStopSelect.getSelectedItem().equals("2")) {
            portStopbits = SerialPort.STOPBITS_2;
         }

         device.open((String) portSelect.getSelectedItem(), portRate, portParity, portStopbits);
         final int[] cards = device.getCardCage();

         System.out.println("Analyzer:" + device.getAnalyzerId());

         System.out.println("Cards:");
         String cardString;
         sourceSelect.removeAllItems();
         for (int i = 0; i < cards.length; i++) {
            System.out.println("\t" + cards[i]);
            if (cards[i] != -1) {
               cardString = device.getCardString(cards[i]);

               // only allow 16550 Analyzers
               if (cardString.contains("16550")) {
                  sourceSelect.addItem(cardString.substring(0, cardString.indexOf(' ')));
               }
               if (cardString.contains("16542")) {
                  sourceSelect.addItem(cardString.substring(0, cardString.indexOf(' ')));
               }
            }
         }
         dialog.pack();

      } catch (final PortInUseException | IOException e1) {
         JOptionPane.showMessageDialog(null, e1.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      } finally {
         device.close();
      }
   }

   private void doTimer() {
      if (status == DeviceState.DONE) {
         close();
      } else if (status == DeviceState.ABORTED) {
         timer.stop();
         JOptionPane.showMessageDialog(this,
               "Error while trying to communicate with device:\n\n" + "\"" + errorMessage + "\"\n\n"
                     + "Make sure the device is:\n" + " - connected to the specified port\n"
                     + " - turned on and properly configured\n" + " - set to the selected transfer rate\n",
               "Communication Error", JOptionPane.ERROR_MESSAGE);
         setDialogEnabled(true);
      } else if (device.isRunning()) {
         progress.setValue(device.getPercentage());
      }
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
    * Starts capturing from device. Should not be called externally.
    */
   @Override
   public void run() {
      final int portRate = smartParseInt((String) portRateSelect.getSelectedItem());

      int portParity = SerialPort.PARITY_NONE;
      if (portParitySelect.getSelectedItem().equals("odd")) {
         portParity = SerialPort.PARITY_ODD;
      } else if (portParitySelect.getSelectedItem().equals("even")) {
         portParity = SerialPort.PARITY_EVEN;
      }

      int portStopbits = SerialPort.STOPBITS_1;
      if (portStopSelect.getSelectedItem().equals("1.5")) {
         portStopbits = SerialPort.STOPBITS_1_5;
      } else if (portStopSelect.getSelectedItem().equals("2")) {
         portStopbits = SerialPort.STOPBITS_2;
      }

      byte[] data = null;
      Hp16550DeviceDecoder hp16550Data = null;
      Hp16542DeviceDecoder hp16542Data = null;
      try {
         status = DeviceState.RUNNING;
         errorMessage = "";
         if (!debug) {
            // load from Device
            final int selectedCardPos = device.getCardInCage(device.getCardId((String) sourceSelect.getSelectedItem()));
            device.open((String) portSelect.getSelectedItem(), portRate, portParity, portStopbits);
            data = device.getData(selectedCardPos);
            System.out.println("Databytes: " + data.length);
            device.close();
         } else {
            // load from file
            final JFileChooser inDumpFileChooser = new JFileChooser();
            if (inDumpFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
               final File inf = inDumpFileChooser.getSelectedFile();

               if (inf.exists() && inf.isFile()) {
                  try {
                     final FileInputStream is = new FileInputStream(inf);
                     data = new byte[is.available()];
                     is.read(data);
                     is.close();
                  } catch (final IOException e1) {
                     e1.printStackTrace();
                  }
               } else {
                  System.out.println("File not found");
               }
            }
         }

         try {
            hp16550Data = new Hp16550DeviceDecoder(data);
            capturedData = hp16550Data.getCapturedData(analyzerSelect.getSelectedIndex());
         } catch (final IOException e) {
            hp16550Data = null;
         }
         try {
            hp16542Data = new Hp16542DeviceDecoder(data);
            capturedData = hp16542Data.getCapturedData();
         } catch (final IOException e) {
            hp16542Data = null;
         }

         if (((data != null) && (hp16550Data == null) && (hp16542Data == null)) || (rawStore == true)) {
            // there were some data but no valid decoder was found
            rawData = new byte[data.length];
            for (int i = 0; i < rawData.length; i++) {
               rawData[i] = data[i];
            }
         } else {
            rawData = null;
         }

         status = DeviceState.DONE;
      } catch (final Exception ex) {
         status = DeviceState.ABORTED;
         progress.setValue(0);
         System.out.println("Run aborted");
         if (!(ex instanceof InterruptedException)) {
            errorMessage = ex.getMessage();
            ex.printStackTrace(System.out);
         }
      }
   }

   /**
    * Properly closes the dialog. This method makes sure timer and worker thread are stopped before the dialog is
    * closed.
    */
   private void close() {
      if (timer != null) {
         timer.stop();
         timer = null;
      }
      if (worker != null) {
         worker.interrupt();
         worker = null;
      }
      dialog.setVisible(false);
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

   /*
    * (non-Javadoc)
    *
    * @see org.sump.analyzer.Configurable#readProperties(org.sump.util.Properties)
    */
   @Override
   public void readProperties(final Properties properties) {
      selectByValue(portSelect, properties.getProperty(NAME + ".port"));
      selectByValue(portRateSelect, properties.getProperty(NAME + ".portRate"));
      selectByValue(portParitySelect, properties.getProperty(NAME + ".portParity"));
      selectByValue(portStopSelect, properties.getProperty(NAME + ".portStop"));

      // selectByValue(sourceSelect, properties.getProperty(NAME + ".source"));
      selectByValue(analyzerSelect, properties.getProperty(NAME + ".analyzer"));

      // hidden parameter for debug mode enabled
      debug = Boolean.parseBoolean(properties.getProperty(NAME + ".debug"));
      if (debug) {
         System.out.println(NAME + "-> debug enabled");
      }

      // hidden parameter for raw data store enabled
      rawStore = Boolean.parseBoolean(properties.getProperty(NAME + ".raw"));
      if (rawStore) {
         System.out.println(NAME + "-> raw data store enabled");
      }
   }

   /*
    * (non-Javadoc)
    *
    * @see org.sump.analyzer.Configurable#writeProperties(org.sump.util.Properties)
    */
   @Override
   public void writeProperties(final Properties properties) {
      properties.setProperty(NAME + ".port", (String) portSelect.getSelectedItem());
      properties.setProperty(NAME + ".portRate", (String) portRateSelect.getSelectedItem());
      properties.setProperty(NAME + ".portParity", (String) portParitySelect.getSelectedItem());
      properties.setProperty(NAME + ".portStop", (String) portStopSelect.getSelectedItem());

      // properties.setProperty(NAME + ".source", (String)sourceSelect.getSelectedItem());
      properties.setProperty(NAME + ".analyzer", (String) analyzerSelect.getSelectedItem());
   }

   /**
    * Return the device data of the last successful run.
    *
    * @@return device data
    */
   @Override
   public CapturedData getDeviceData(final Component parent) {
      System.out.println("getDeviceData");
      if ((rawData != null) && (parent != null)) {
         // some data were captured but not decoded

         // ask for store a raw dump file
         if (JOptionPane.showConfirmDialog(parent, "store a RAW dump file instead ?", "No decoder found!",
               JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
            final JFileChooser dumpFileChooser = new JFileChooser();
            if (dumpFileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
               final File file = dumpFileChooser.getSelectedFile();
               System.out.println("Saving: " + file.getName() + ".");
               try {
                  final FileOutputStream os = new FileOutputStream(file);
                  os.write(rawData);
                  os.flush();
                  os.close();
               } catch (final Exception ex) {
                  ex.printStackTrace();
               }
            }
         }
         rawData = null;
      }
      return capturedData;
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
         dialog.pack();
      }
      // reset progress bar
      progress.setValue(0);

      // sync dialog status with device
      updateFields();
   }

   /**
    * Sets the enabled state of all configuration components of the dialog.
    *
    * @param enable <code>true</code> to enable components, <code>false</code> to disable them
    */
   private void setDialogEnabled(final boolean enable) {
      captureButton.setEnabled(enable);
      configButton.setEnabled(enable);
      portSelect.setEnabled(enable);
      portRateSelect.setEnabled(enable);
      portParitySelect.setEnabled(enable);
      portStopSelect.setEnabled(enable);
      sourceSelect.setEnabled(enable);
      analyzerSelect.setEnabled(enable);
   }

   /**
    * Displays the device controller dialog with enabled configuration portion and waits for user input.
    *
    * @param frame parent frame of this dialog
    * @return status, which is either <code>ABORTED</code> or <code>DONE</code>
    * @throws Exception
    */
   @Override
   public DeviceState showCaptureDialog(final JFrame frame) {
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
   public DeviceState showCaptureProgress(final JFrame frame) {
      try {
         if (device.getCardCage() != null) {
            status = DeviceState.IDLE;
            initDialog(frame);
            setDialogEnabled(false);
            timer = new Timer(100, _ -> doTimer());
            worker = new Thread(this);
            timer.start();
            worker.start();
            dialog.setVisible(true);
         } else {
            status = showCaptureDialog(frame);
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }
      return status;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.sump.analyzer.DeviceController#updateFields()
    */
   @Override
   public void updateFields() {
   }

   /**
    * return the name of the device controller
    */
   @Override
   public String getControllerName() {
      return "HP 16500 Controller";
   }

   /**
    * inner class for decoding HP16500 Data
    *
    * @author Frank Kunz
    */
   private class Hp16550DeviceDecoder {
      String sectionName;
      int sectionSize;
      int numberOfAquisitionChips;
      Hp16550Analyzer[] analyzers;
      Hp16550Chip[] chips;

      /**
       * class constructor
       *
       * @@param data raw block data read out from analyzer
       * @@throws IOException when the data array contains wrong data
       */
      public Hp16550DeviceDecoder(final byte[] data) throws IOException {
         int offs;
         final int sampleRowSize = 14; // row size is 14 for 6 pods and 26 for 12 pods
         chips = new Hp16550Chip[3]; // 6 pods only one card configuration allowed thats 3 chips

         // decode header 16 bytes
         sectionName = new String(data, 0, 10);
         sectionName = sectionName.trim();
         if ((data[11] & 0xFF) != 32) {
            throw new IOException("Invalid Module ID " + data[11]);
         }

         sectionSize = 0;
         for (int i = 0; i < 4; i++) {
            sectionSize += (data[12 + i] & 0xFF) << ((3 - i) * 8);
         }

         // decode preamble 160 bytes
         numberOfAquisitionChips = data[19] & 0xFF;

         analyzers = new Hp16550Analyzer[2];
         analyzers[0] = new Hp16550Analyzer(data, 20);

         analyzers[1] = new Hp16550Analyzer(data, 60);

         // decode data
         offs = 100 + 2 + 12; // skip 6 pods = 12 bytes
         int currentChip;
         int currentPod;
         for (int i = 0; i < chips.length; i++) {
            currentChip = chips.length - i - 1;
            chips[currentChip] = new Hp16550Chip(((data[offs + i * 4] & 0xFF) << 8) + (data[offs + 1 + i * 4] & 0xFF));
            for (int j = 0; j < chips[currentChip].pods.length; j++) {
               currentPod = chips[currentChip].pods.length - 1 - j;
               chips[currentChip].pods[currentPod].triggerPos = (((data[offs + 26 + i * 4 + 2 * j] & 0xFF) << 8)
                     + (data[offs + 26 + 1 + i * 4 + 2 * j] & 0xFF));
            }
         }
         if (data.length > 176) {
            // data available fill pod structures with sample data
            offs = 176;
            int maxPodLen = 0;
            for (int i = 0; i < chips.length; i++) {
               currentChip = chips.length - 1 - i;
               for (int j = 0; j < chips[currentChip].pods.length; j++) {
                  currentPod = chips[currentChip].pods.length - 1 - j;
                  if (chips[currentChip].pods[currentPod].sampleData.length > 0) {
                     for (int k = 0; k < chips[currentChip].pods[currentPod].sampleData.length; k++) {
                        chips[currentChip].pods[currentPod].sampleData[k] = ((data[offs + 2 + (i * 4) + (j * 2)
                              + sampleRowSize * k] & 0xFF) << 8)
                              + (data[offs + 2 + (i * 4) + (j * 2) + 1 + sampleRowSize * k] & 0xFF);
                     }
                     if (chips[currentChip].pods[currentPod].sampleData.length > maxPodLen) {
                        maxPodLen = chips[currentChip].pods[currentPod].sampleData.length;
                     }
                  }
               }
            }
            // get timing data
            offs = 176 + maxPodLen * sampleRowSize;
            long timeStamp = 0;
            for (int i = 0; i < chips.length; i++) {
               for (int j = 0; j < chips[i].timingData.length; j++) {
                  timeStamp = 0;
                  for (int k = 0; k < 8; k++) {
                     timeStamp += (long) (data[offs + k] & 0xFF) << ((7 - k) * 8);
                  }
                  chips[i].timingData[j] = timeStamp;
                  offs += 8;
               }
            }
         }

         if (debug) {
            System.out.println("Analyzer Chips:");
            for (int i = 0; i < chips.length; i++) {
               System.out.println("\tchip[" + i + "] size=" + chips[i].timingData.length);
               if (chips[i].timingData.length > 0) {
                  for (int j = 0; j < chips[i].pods.length; j++) {
                     long trgPos = 0;
                     if (chips[i].pods[j].triggerPos > 0) {
                        long trgOffs = chips[i].timingData[0];
                        if (trgOffs < 0) {
                           trgOffs *= -1;
                        }
                        trgPos = chips[i].timingData[chips[i].pods[j].triggerPos - 1] + trgOffs;
                     }
                     System.out.println(
                           "\t\tpod[" + j + "] trigIdx=" + chips[i].pods[j].triggerPos + " (" + trgPos + "ps)");
                  }
               }
            }
         }
      }

      /**
       * get the sampled data in a usable format for GUI
       *
       * @param analyzer number of analyzer for data read out
       * @return analyzer data
       * @throws IllegalArgumentException when an unavailable analyzer is selected
       * @throws IOException              when the selected analyzer has no data available
       */
      public CapturedData getCapturedData(final int analyzer) throws IllegalArgumentException, IOException {
         CapturedData capturedData = null;
         // check if analyzer is available
         if ((analyzer < 0) || (analyzer > 1)) {
            throw new IllegalArgumentException("Analyzer " + analyzer + " is out of range");
         }

         int currentChip = -1;
         int podCount = 2;

         // check if analyzer has data
         if ((analyzers[analyzer].listOfPods & 0x6) == 0x6) {
            // the first pair of pods is assigned
            currentChip = 0;
         } else if ((analyzers[analyzer].listOfPods & 0x18) == 0x18) {
            // the second pair of pods is assigned
            currentChip = 2;
         } else if ((analyzers[analyzer].listOfPods & 0x60) == 0x60) {
            // the third pair of pods is assigned
            currentChip = 4;
         } else if ((analyzers[analyzer].listOfPods & 0x2) == 0x2) {
            // the first pod is assigned
            currentChip = 0;
            podCount = 1;
         } else if ((analyzers[analyzer].listOfPods & 0x8) == 0x8) {
            // the third pod is assigned
            currentChip = 2;
            podCount = 1;
         } else if ((analyzers[analyzer].listOfPods & 0x20) == 0x20) {
            // the fifth pod is assigned
            currentChip = 4;
            podCount = 1;
         } else {
            throw new IOException("Analyzer " + analyzer + " has no data");
         }

         final int arraylen = Math.max(chips[currentChip].pods[0].sampleData.length,
               chips[currentChip].pods[1].sampleData.length);

         // prepare data array
         final int[] data = new int[arraylen];
         for (int i = 0; i < chips[currentChip].pods[0].sampleData.length; i++) {
            data[i] = chips[currentChip].pods[0].sampleData[i];
         }
         if (podCount == 2) {
            for (int i = 0; i < chips[currentChip].pods[1].sampleData.length; i++) {
               data[i] += (chips[currentChip].pods[1].sampleData[i] << 16);
            }
         }

         int enabledChannels = 0xFFFFFFFF;
         if (podCount == 1) {
            enabledChannels &= 0xFFFF;
         }

         switch (analyzers[analyzer].machineDataMode) {
            case 11:
            case 14:
               // transitional data mode
               final long[] timing = new long[arraylen];
               for (int i = 0; i < chips[currentChip].timingData.length; i++) {
                  timing[i] = chips[currentChip].timingData[i] / analyzers[analyzer].samplePeriod;
               }

               // correct time offset
               long timeOffset = timing[0];
               if (timeOffset < 0) {
                  timeOffset *= -1;
                  for (int i = 0; i < timing.length; i++) {
                     timing[i] += timeOffset;
                  }
               }

               final long absoluteLength = (chips[currentChip].timingData[chips[currentChip].timingData.length - 1]
                     - chips[currentChip].timingData[0]) / analyzers[analyzer].samplePeriod;

               // get trigger position
               long triggerPos = 0;
               if (chips[currentChip].pods[0].triggerPos > 0) {
                  triggerPos = timing[chips[currentChip].pods[0].triggerPos - 1];
               }

               capturedData = new CapturedData(data, timing, triggerPos,
                     (int) (1000000000000L / analyzers[analyzer].samplePeriod), podCount * 16, enabledChannels,
                     absoluteLength);
               break;
            case 1:
            case 2:
            case 4:
            case 8:
               // state data mode
            case 10:
            case 13:
               // conventional data mode
               capturedData = new CapturedData(data, chips[currentChip].pods[0].triggerPos,
                     (int) (1000000000000L / analyzers[analyzer].samplePeriod), podCount * 16, enabledChannels);
               break;
            case 12:
               // glitch data mode
               break;
            case -1:
            default:
               // no data
               break;
         }
         return capturedData;
      }

   }

   /**
    * inner class for decode an analyzer header
    *
    * @@author Frank Kunz
    */
   private class Hp16550Analyzer {
      /**
       * class contructor
       *
       * @param data   raw data block from analyzer
       * @param offset byte offset in data for analyzer header postion
       */
      public Hp16550Analyzer(final byte[] data, final int offset) {
         machineDataMode = data[offset + 0];
         listOfPods = ((data[offset + 2] & 0xFF) << 8) + (data[offset + 3] & 0xFF);
         timeStateTagChip = data[offset + 4];
         masterChip = data[offset + 5];
         samplePeriod = 0;
         for (int i = 0; i < 8; i++) {
            samplePeriod += (data[offset + 12 + i] & 0xFF) << ((7 - i) * 8);
         }
         tagType = data[offset + 28] & 0xFF;
         timeOffset = 0;
         for (int i = 0; i < 8; i++) {
            timeOffset += (data[offset + 30 + i] & 0xFF) << ((7 - i) * 8);
         }

         if (debug) {
            System.out.println("Analyzer Info:");
            System.out.println("\tmachineDataMode=" + machineDataMode);
            System.out.println("\tlistOfPods=" + listOfPods);
            System.out.println("\ttimeStateTagChip=" + timeStateTagChip);
            System.out.println("\tmasterChip=" + masterChip);
            System.out.println("\tsamplePeriod=" + samplePeriod + "ps");
            System.out.println("\ttagType=" + tagType);
            System.out.println("\ttimeOffset=" + timeOffset + "ps");
         }
      }

      int machineDataMode;
      int listOfPods;
      int timeStateTagChip;
      int masterChip;
      long samplePeriod;
      int tagType;
      long timeOffset;
   }

   /**
    * inner class for decoding pod data
    *
    * @author Frank Kunz
    */
   private class Hp16550Pod {
      /**
       * class contructor for a pod (16bit)
       *
       * @@param samples number of samples for this pod
       */
      public Hp16550Pod(final int samples) {
         sampleData = new int[samples];
         triggerPos = 0;
      }

      int[] sampleData;
      int triggerPos;
   }

   /**
    * inner class for encapsulate a aquisition chip
    *
    * @author Frank Kunz
    */
   private class Hp16550Chip {
      public Hp16550Chip(final int samples) {
         pods = new Hp16550Pod[2];
         pods[0] = new Hp16550Pod(samples);
         pods[1] = new Hp16550Pod(samples);
         timingData = new long[samples];
      }

      Hp16550Pod pods[];
      long[] timingData;
   }

   /**
    * inner class for decoding HP16500 Data
    *
    * @@author Frank Kunz
    */
   private class Hp16542DeviceDecoder {
      String sectionName;
      int sectionSize;
      int dataAquisitionMode;
      int totalAquisitionStates;
      int prestoreAquisitionStates;
      int poststoreAquisitionStates;
      long samplePeriod;
      int systemWidth;
      int[] samples;

      /**
       * class constructor
       *
       * @param data raw block data read out from analyzer
       * @throws IOException when the data array contains wrong data
       */
      public Hp16542DeviceDecoder(final byte[] data) throws IOException {
         int offs;

         // decode header 16 bytes
         sectionName = new String(data, 0, 10);
         sectionName = sectionName.trim();
         if ((data[11] & 0xFF) != 42) {
            throw new IOException("Invalid Module ID " + data[11]);
         }

         sectionSize = 0;
         for (int i = 0; i < 4; i++) {
            sectionSize += (data[12 + i] & 0xFF) << ((3 - i) * 8);
         }

         // decode preamble 156 bytes
         offs = 0 + 16;
         final int instrumentId = ((data[offs] & 0xFF) << 8) + (data[offs + 1] & 0xFF);
         offs = 2 + 16;
         final int revisionCode = ((data[offs] & 0xFF) << 8) + (data[offs + 1] & 0xFF);
         offs = 4 + 16;
         dataAquisitionMode = (data[offs] & 0xFF);
         offs = 5 + 16;
         final int numberAquisitionPods = (data[offs] & 0xFF);

         if ((dataAquisitionMode < 1) || (dataAquisitionMode > 2)) {
            throw new IOException("Unknown Aquisition Mode " + dataAquisitionMode);
         }

         offs = 26 + 16;
         samplePeriod = 0;
         for (int i = 0; i < 8; i++) {
            samplePeriod += (data[offs + i] & 0xFF) << ((7 - i) * 8);
         }

         offs = 56 + 16;
         totalAquisitionStates = 0;
         for (int i = 0; i < 4; i++) {
            totalAquisitionStates += (data[offs + i] & 0xFF) << ((3 - i) * 8);
         }
         offs = 60 + 16;
         prestoreAquisitionStates = 0;
         for (int i = 0; i < 4; i++) {
            prestoreAquisitionStates += (data[offs + i] & 0xFF) << ((3 - i) * 8);
         }
         offs = 64 + 16;
         poststoreAquisitionStates = 0;
         for (int i = 0; i < 4; i++) {
            poststoreAquisitionStates += (data[offs + i] & 0xFF) << ((3 - i) * 8);
         }

         offs = 68 + 16;
         final int analyzerConfig = ((data[offs] & 0xFF) << 8) + (data[offs + 1] & 0xFF);
         offs = 70 + 16;
         final int aquisitionDataValid = (data[offs] & 0xFF);
         offs = 71 + 16;
         final int tracePointFound = (data[offs] & 0xFF);
         offs = 72 + 16;
         final int recordMode = ((data[offs] & 0xFF) << 8) + (data[offs + 1] & 0xFF);

         offs = 74 + 16;
         int memoryLength = 0;
         for (int i = 0; i < 4; i++) {
            memoryLength += (data[offs + i] & 0xFF) << ((3 - i) * 8);
         }
         offs = 78 + 16;
         int numberOfRecords = 0;
         for (int i = 0; i < 4; i++) {
            numberOfRecords += (data[offs + i] & 0xFF) << ((3 - i) * 8);
         }
         offs = 82 + 16;
         int recordLength = 0;
         for (int i = 0; i < 4; i++) {
            recordLength += (data[offs + i] & 0xFF) << ((3 - i) * 8);
         }

         offs = 86 + 16;
         final int percentPreStore = ((data[offs] & 0xFF) << 8) + (data[offs + 1] & 0xFF);

         systemWidth = 0;
         switch (analyzerConfig) {
            default:
               throw new IOException("Invalid Analyzer Config " + analyzerConfig);
            // break;
            case 0:
            case 4:
               systemWidth = 2 * numberAquisitionPods;
               break;
            case 1:
            case 5:
               systemWidth = numberAquisitionPods;
               break;
            case 2:
            case 6:
               systemWidth = 2;
               break;
            case 3:
            case 7:
               systemWidth = 1;
               break;
         }

         if (debug) {
            System.out.println("instrumentId=" + instrumentId);
            System.out.println("revisionCode=" + revisionCode);
            System.out.println("dataAquisitionMode=" + dataAquisitionMode);
            System.out.println("numberAquisitionPods=" + numberAquisitionPods);
            System.out.println("samplePeriod=" + samplePeriod);
            System.out.println("totalAquisitionStates=" + totalAquisitionStates);
            System.out.println("prestoreAquisitionStates=" + prestoreAquisitionStates);
            System.out.println("poststoreAquisitionStates=" + poststoreAquisitionStates);
            System.out.println("analyzerConfig=" + analyzerConfig);
            System.out.println("aquisitionDataValid=" + aquisitionDataValid);
            System.out.println("tracePointFound=" + tracePointFound);
            System.out.println("recordMode=" + recordMode);
            System.out.println("memoryLength=" + memoryLength);
            System.out.println("numberOfRecords=" + numberOfRecords);
            System.out.println("recordLength=" + recordLength);
            System.out.println("percentPreStore=" + percentPreStore);
            System.out.println("systemWidth=" + systemWidth);
            System.out.println();
         }

         offs = 156 + 16;
         samples = new int[totalAquisitionStates];
         int index = 0;
         int shift = 0;
         for (int i = 0; i < samples.length; i++) {
            samples[i] = 0;
            for (int j = 0; j < systemWidth; j++) {
               shift = (((systemWidth - 1) - j) * 8);
               index = systemWidth * i + j;
               samples[i] |= ((data[offs + index] & 0xFF) << shift);
            }
         }
      }

      /**
       * get the sampled data in a usable format for GUI
       *
       * @@return analyzer data
       */
      public CapturedData getCapturedData() {
         CapturedData capturedData = null;
         int enabledChannels = 0xFFFF;
         if (systemWidth == 1) {
            enabledChannels &= 0xFF;
         }

         if (dataAquisitionMode == 1) {
            // timing data
            capturedData = new CapturedData(samples, prestoreAquisitionStates, (int) (1000000000000L / samplePeriod),
                  systemWidth * 8, enabledChannels);
         } else {
            // state data
            final long timeDummy[] = new long[totalAquisitionStates];
            for (int i = 0; i < timeDummy.length; i++) {
               timeDummy[i] = i;
            }
            capturedData = new CapturedData(samples, timeDummy, prestoreAquisitionStates, CapturedData.NOT_AVAILABLE,
                  systemWidth * 8, enabledChannels, totalAquisitionStates);
         }

         return capturedData;
      }

   }
}