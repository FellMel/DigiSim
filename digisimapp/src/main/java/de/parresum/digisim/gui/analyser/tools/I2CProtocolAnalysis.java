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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import de.parresum.digisim.gui.analyser.CapturedData;
import de.parresum.digisim.gui.analyser.Configurable;

/**
 * I2C Protocol analyzer
 *
 * @author Frank Kunz
 * @author Kai Uwe Bachmann
 *
 */
public class I2CProtocolAnalysis extends Base implements Tool, Configurable {

   /**
    * create constraints for GridBagLayout
    *
    * @param x  x grid position
    * @param y  y grid position
    * @param w  grid width
    * @param h  grid height
    * @param wx weighting for extra horizontal space
    * @param wy weighting for extra vertical space
    * @return constraints object
    */
   private static GridBagConstraints createConstraints(final int x, final int y, final int w, final int h,
         final double wx, final double wy) {
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH;
      gbc.insets = new Insets(4, 4, 4, 4);
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = w;
      gbc.gridheight = h;
      gbc.weightx = wx;
      gbc.weighty = wy;
      return (gbc);
   }

   /**
    * Class for I2C dataset
    *
    * @author Frank Kunz
    *
    *         An I2C dataset consists of a timestamp, a value, or it can have an I2C event. This class is used to store
    *         the decoded I2C data in a Vector.
    */
   private class I2CProtocolAnalysisDataSet {
      public I2CProtocolAnalysisDataSet(final long tm, final int val) {
         this.time = tm;
         this.value = val;
         this.event = null;
      }

      public I2CProtocolAnalysisDataSet(final long tm, final String ev) {
         this.time = tm;
         this.value = 0;
         this.event = new String(ev);
      }

      public boolean isEvent() {
         return (event != null);
      }

      public long time;
      public int value;
      public String event;
   }

   /**
    * The Dialog Class
    *
    * @author Frank Kunz
    *
    *         The dialog class draws the basic dialog with a grid layout. The dialog consists of three main parts. A
    *         settings panel, a table panel and three buttons.
    */
   private class I2CProtocolAnalysisDialog extends JDialog implements ActionListener, Runnable {
      public I2CProtocolAnalysisDialog(final Frame frame, final String name) {
         super(frame, name, true);
         setLayout(new GridBagLayout());
         getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

         decodedData = new Vector<I2CProtocolAnalysisDataSet>();
         startOfDecode = 0;

         /*
          * add protocol settings elements
          */
         final JPanel panSettings = new JPanel();
         panSettings.setLayout(new GridLayout(6, 2, 5, 5));
         panSettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Settings"),
               BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         final String channels[] = new String[32];
         for (int i = 0; i < 32; i++) {
            channels[i] = new String("Channel " + i);
         }
         panSettings.add(new JLabel("Line A"));
         lineA = new JComboBox<String>(channels);
         panSettings.add(lineA);
         panSettings.add(new JLabel("Line B"));
         lineB = new JComboBox<String>(channels);
         panSettings.add(lineB);
         detectSTART = new JCheckBox("Show START", true);
         panSettings.add(detectSTART);
         panSettings.add(new JLabel(""));
         detectSTOP = new JCheckBox("Show STOP", true);
         panSettings.add(detectSTOP);
         panSettings.add(new JLabel(""));
         detectACK = new JCheckBox("Show ACK", true);
         panSettings.add(detectACK);
         panSettings.add(new JLabel(""));
         detectNACK = new JCheckBox("Show NACK", true);
         panSettings.add(detectNACK);
         panSettings.add(new JLabel(""));
         add(panSettings, createConstraints(0, 0, 1, 1, 0, 0));

         /*
          * add bus configuration panel
          */
         final JPanel panBusConfig = new JPanel();
         panBusConfig.setLayout(new GridLayout(2, 2, 5, 5));
         panBusConfig.setBorder(BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder("Bus Configuration"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         panBusConfig.add(new JLabel("SCL :"));
         busSetSCL = new JLabel("<autodetect>");
         panBusConfig.add(busSetSCL);
         panBusConfig.add(new JLabel("SDA :"));
         busSetSDA = new JLabel("<autodetect>");
         panBusConfig.add(busSetSDA);
         add(panBusConfig, createConstraints(0, 1, 1, 1, 0, 0));

         /*
          * add an empty output view
          */
         final JPanel panTable = new JPanel();
         panTable.setLayout(new GridLayout(1, 1, 5, 5));
         panTable.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Results"),
               BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         outText = new JEditorPane("text/html", toHtmlPage(true));
         outText.setMargin(new Insets(5, 5, 5, 5));
         panTable.add(new JScrollPane(outText));
         add(panTable, createConstraints(1, 0, 3, 3, 1.0, 1.0));

         /*
          * add progress bar
          */
         final JPanel panProgress = new JPanel();
         panProgress.setLayout(new BorderLayout());
         panProgress.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Progress"),
               BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         progress = new JProgressBar(0, 100);
         progress.setMinimum(0);
         progress.setValue(0);
         progress.setMaximum(100);
         panProgress.add(progress, BorderLayout.CENTER);
         add(panProgress, createConstraints(0, 3, 4, 1, 1.0, 0));

         /*
          * add buttons
          */
         final JPanel panButton = new JPanel();
         // panButton.setLayout(new GridLayout(3,1,5,5));
         btnConvert = new JButton("Analyze");
         btnConvert.addActionListener(this);
         panButton.add(btnConvert);
         btnExport = new JButton("Export");
         btnExport.addActionListener(this);
         panButton.add(btnExport);
         btnCancel = new JButton("Close");
         btnCancel.addActionListener(this);
         panButton.add(btnCancel);
         add(panButton, createConstraints(3, 4, 1, 1, 0, 0));

         fileChooser = new JFileChooser();
         fileChooser.addChoosableFileFilter(new CSVFilter());
         fileChooser.addChoosableFileFilter(new HTMLFilter());

         // pack();
         setSize(900, 500);
         setResizable(false);
         runFlag = false;
         thrWorker = null;
      }

      /**
       * shows the dialog and sets the data to use
       *
       * @param data data to use for analysis
       */
      public void showDialog(final CapturedData data) {
         analysisData = data;
         setVisible(true);
      }

      /**
       * set the controls of the dialog enabled/disabled
       *
       * @param enable status of the controls
       */
      private void setControlsEnabled(final boolean enable) {
         lineA.setEnabled(enable);
         lineB.setEnabled(enable);
         detectSTART.setEnabled(enable);
         detectSTOP.setEnabled(enable);
         detectACK.setEnabled(enable);
         detectNACK.setEnabled(enable);
         btnExport.setEnabled(enable);
         btnCancel.setEnabled(enable);
      }

      @Override
      public void actionPerformed(final ActionEvent e) {
         if (e.getActionCommand().equals("Analyze")) {
            runFlag = true;
            thrWorker = new Thread(this);
            thrWorker.start();
         } else if (e.getActionCommand().equals("Close")) {
            setVisible(false);
         } else if (e.getActionCommand().equals("Export")) {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
               final File file = fileChooser.getSelectedFile();
               if (fileChooser.getFileFilter().getDescription().equals("Website (*.html)")) {
                  storeToHtmlFile(file);
               } else {
                  storeToCsvFile(file);
               }
            }
         } else if (e.getActionCommand().equals("Abort")) {
            runFlag = false;
         }
      }

      /**
       * calculate the time offset
       *
       * @param time absolute sample number
       * @return time relative to data
       */
      private long calculateTime(final long time) {
         if (analysisData.hasTriggerData()) {
            return time - analysisData.getTriggerPosition();
         }
         return time;
      }

      /**
       * This is the I2C protocol decoder core
       *
       * The decoder scans for a decode start event when one of the two lines is going low (start condition). After this
       * the decoder starts to decode the data.
       */
      private void decode() {
         // process the captured data and write to output
         int a, b, c, d;
         int sdaValue;
         int sdaMask, sclMask;

         // clear old data
         decodedData.clear();
         sdaMask = 0;
         sclMask = 0;
         statBusErrorCount = 0;
         statDecodedBytes = 0;

         /*
          * Build bitmasks based on the lineA, lineB pins pins.
          */
         final int lineAmask = (1 << lineA.getSelectedIndex());
         final int lineBmask = (1 << lineB.getSelectedIndex());

         System.out.println("lineAmask = 0x" + Integer.toHexString(lineAmask));
         System.out.println("lineBmask = 0x" + Integer.toHexString(lineBmask));

         progress.setValue(0);

         /*
          * first of all scan both lines until they are high (IDLE), then the first line that goes low is the SDA line
          * (START condition).
          */
         for (a = 0; a < analysisData.getValues().length; a++) {
            if ((analysisData.getValues()[a] & (lineAmask | lineBmask)) == (lineAmask | lineBmask)) {
               // IDLE found here
               break;
            }

            if (runFlag == false) {
               return;
            }
            progress.setValue(a * 100 / analysisData.getValues().length);
         }
         if (a == analysisData.getValues().length) {
            // no idle state could be found
            return;
         }
         // a is now the start of idle, now find the first start condition
         for (; a < analysisData.getValues().length; a++) {
            if (((analysisData.getValues()[a] & (lineAmask | lineBmask)) != (lineAmask | lineBmask))
                  && ((analysisData.getValues()[a] & (lineAmask | lineBmask)) != 0)) {
               // one line is low
               if ((analysisData.getValues()[a] & lineAmask) == 0) {
                  // lineA is low and lineB is high here: lineA = SDA, lineB = SCL
                  sdaMask = lineAmask;
                  sclMask = lineBmask;

                  busSetSCL.setText((String) lineB.getSelectedItem());
                  busSetSDA.setText((String) lineA.getSelectedItem());
               } else {
                  // lineB is low and lineA is high here: lineA = SCL, lineB = SDA
                  sdaMask = lineBmask;
                  sclMask = lineAmask;

                  busSetSCL.setText((String) lineA.getSelectedItem());
                  busSetSDA.setText((String) lineB.getSelectedItem());
               }
               break;
            }

            if (runFlag == false) {
               return;
            }
            progress.setValue(a * 100 / analysisData.getValues().length);
         }
         if (a == analysisData.getValues().length) {
            // no start condition could be found
            return;
         }

         /*
          * now it is clear what is SCL (sclMask) and what is SDA (sdaMask). Variable a points to the start condition.
          */
         if (detectSTART.isSelected()) {
            decodedData.addElement(new I2CProtocolAnalysisDataSet(a, "START"));
         }
         startOfDecode = a;
         endOfDecode = analysisData.getValues().length;
         if (analysisData.isCursorEnabled()) {
            startOfDecode = analysisData.getSampleIndex(analysisData.getCursorPositionA());
            endOfDecode = analysisData.getSampleIndex(analysisData.getCursorPositionB());
         }

         /*
          * Now decode the bytes, SDA may only change when SCL is low. Otherwise it may be a repeated start condition or
          * stop condition. If the start/stop condition is not at a byte boundary a bus error is detected. So we have to
          * scan for SCL rises and for SDA changes during SCL is high. Each byte is followed by a 9th bit (ACK/NACK).
          */
         b = analysisData.getValues()[a] & sclMask;
         c = analysisData.getValues()[a] & sdaMask;
         d = 8;
         sdaValue = 0;
         a = startOfDecode;
         while (a < endOfDecode - 1) {
            a++;

            // detect SCL rise
            if ((analysisData.getValues()[a] & sclMask) > b) {
               // SCL rises
               if ((analysisData.getValues()[a] & sdaMask) != c) {
                  // SDA changes too, bus error
                  decodedData.addElement(
                        new I2CProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[a]), "BUS-ERROR"));
                  statBusErrorCount++;
               } else // read SDA
               if (d == 0) {
                  // read the ACK/NACK state
                  if ((analysisData.getValues()[a] & sdaMask) != 0) {
                     // NACK
                     if (detectNACK.isSelected()) {
                        decodedData.addElement(
                              new I2CProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[a]), "NACK"));
                     }
                  } else // ACK
                  if (detectACK.isSelected()) {
                     decodedData.addElement(
                           new I2CProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[a]), "ACK"));
                  }
                  // next byte
                  d = 8;
               } else {
                  d--;
                  if ((analysisData.getValues()[a] & sdaMask) != 0) {
                     sdaValue |= (1 << d);
                  }
                  if (d == 0) {
                     // store decoded byte
                     decodedData.addElement(
                           new I2CProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[a]), sdaValue));
                     sdaValue = 0;
                     statDecodedBytes++;
                  }
               }
            }

            // detect SDA change when SCL high
            if (((analysisData.getValues()[a] & sclMask) == sclMask)
                  && ((analysisData.getValues()[a] & sdaMask) != c)) {
               // SDA changes here
               if (d < 7) {
                  // bus error, no complete byte detected
                  decodedData.addElement(
                        new I2CProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[a]), "BUS-ERROR"));
                  statBusErrorCount++;
               } else {
                  if ((analysisData.getValues()[a] & sdaMask) > c) {
                     // SDA rises, this is a stop condition
                     if (detectSTOP.isSelected()) {
                        decodedData.addElement(
                              new I2CProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[a]), "STOP"));
                     }
                  } else // SDA falls, this is a start condition
                  if (detectSTART.isSelected()) {
                     decodedData.addElement(
                           new I2CProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[a]), "START"));
                  }
                  // new byte
                  d = 8;
               }
            }

            b = analysisData.getValues()[a] & sclMask;
            c = analysisData.getValues()[a] & sdaMask;

            if (runFlag == false) {
               return;
            }
            progress.setValue((int) (analysisData.getTimestamps()[a] * 100 / (endOfDecode - startOfDecode)));
         }

         outText.setText(toHtmlPage(false));
         outText.setEditable(false);
      }

      /**
       * generate a HTML page
       *
       * @param empty if this is true an empty output is generated
       * @return String with HTML data
       */
      private String toHtmlPage(final boolean empty) {
         final Date now = new Date();
         final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);

         // generate html page header
         final String header = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
               + "<html>" + "  <head>" + "    <title></title>" + "    <meta content=\"\">" + "    <style>"
               + "			th { text-align:left;font-style:italic;font-weight:bold;font-size:medium;font-family:sans-serif;background-color:#C0C0FF; }"
               + "		</style>" + "  </head>" + "	<body>" + "		<H2>I2C Analysis Results</H2>" + "		<hr>"
               + "			<div style=\"text-align:right;font-size:x-small;\">" + df.format(now) + "           </div>"
               + "		<br>";

         // generate the statistics table
         String stats = "<table style=\"width:100%;\">";
         if (empty) {
            stats = stats.concat("<TR><TD style=\"width:30%;\">Decoded Bytes</TD><TD>-</TD></TR>"
                  + "<TR><TD style=\"width:30%;\">Detected Bus Errors</TD><TD>-</TD></TR>");
         } else {
            stats = stats.concat("<TR><TD style=\"width:30%;\">Decoded Bytes</TD><TD>" + statDecodedBytes + "</TD></TR>"
                  + "<TR><TD style=\"width:30%;\">Detected Bus Errors</TD><TD>" + statBusErrorCount + "</TD></TR>");
         }
         stats = stats.concat("</table>" + "<br>" + "<br>");

         // generate the data table
         String data = "<table style=\"font-family:monospace;width:100%;\">"
               + "<tr><th style=\"width:15%;\">Index</th><th style=\"width:15%;\">Time</th><th style=\"width:20%;\">Hex</th><th style=\"width:20%;\">Bin</th><th style=\"width:20%;\">Dec</th><th style=\"width:10%;\">ASCII</th></tr>";
         if (empty) {
         } else {
            I2CProtocolAnalysisDataSet ds;
            for (int i = 0; i < decodedData.size(); i++) {
               ds = decodedData.get(i);
               if (ds.isEvent()) {
                  // this is an event
                  if (ds.event.equals("START")) {
                     // start condition
                     data = data.concat("<tr style=\"background-color:#E0E0E0;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time) + "</td><td>START</td><td></td><td></td><td></td></tr>");
                  } else if (ds.event.equals("STOP")) {
                     // stop condition
                     data = data.concat("<tr style=\"background-color:#E0E0E0;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time) + "</td><td>STOP</td><td></td><td></td><td></td></tr>");
                  } else if (ds.event.equals("ACK")) {
                     // acknowledge
                     data = data.concat("<tr style=\"background-color:#C0FFC0;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time) + "</td><td>ACK</td><td></td><td></td><td></td></tr>");
                  } else if (ds.event.equals("NACK")) {
                     // no acknowledge
                     data = data.concat("<tr style=\"background-color:#FFC0C0;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time) + "</td><td>NACK</td><td></td><td></td><td></td></tr>");
                  } else if (ds.event.equals("BUS-ERROR")) {
                     // bus error
                     data = data.concat("<tr style=\"background-color:#FF8000;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time) + "</td><td>BUS-ERROR</td><td></td><td></td><td></td></tr>");
                  } else {
                     // unknown event
                     data = data.concat("<tr style=\"background-color:#FF8000;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time) + "</td><td>UNKNOWN</td><td></td><td></td><td></td></tr>");
                  }
               } else {
                  data = data.concat("<tr style=\"background-color:#FFFFFF;\"><td>" + i + "</td><td>"
                        + indexToTime(ds.time) + "</td><td>" + "0x" + integerToHexString(ds.value, 2) + "</td><td>"
                        + "0b" + integerToBinString(ds.value, 8) + "</td><td>" + ds.value + "</td><td>");
                  if (ds.value >= 32) {
                     data += (char) ds.value;
                  }
                  data = data.concat("</td></tr>");
               }
            }
         }
         data = data.concat("</table");

         // generate the footer table
         final String footer = "	</body>" + "</html>";

         return (header + stats + data + footer);
      }

      /**
       * exports the table data to a CSV file
       *
       * @param file File object
       */
      private void storeToCsvFile(final File file) {
         if (decodedData.size() > 0) {
            I2CProtocolAnalysisDataSet dSet;
            System.out.println("writing decoded data to " + file.getPath());
            try {
               final BufferedWriter bw = new BufferedWriter(new FileWriter(file));

               bw.write("\"" + "index" + "\",\"" + "time" + "\",\"" + "data or event" + "\"");
               bw.newLine();

               for (int i = 0; i < decodedData.size(); i++) {
                  dSet = decodedData.get(i);
                  if (dSet.isEvent()) {
                     bw.write("\"" + i + "\",\"" + indexToTime(dSet.time) + "\",\"" + dSet.event + "\"");
                  } else {
                     bw.write("\"" + i + "\",\"" + indexToTime(dSet.time) + "\",\"" + dSet.value + "\"");
                  }
                  bw.newLine();
               }
               bw.close();
            } catch (final Exception E) {
               E.printStackTrace(System.out);
            }
         }
      }

      /**
       * stores the data to a HTML file
       *
       * @param file file object
       */
      private void storeToHtmlFile(final File file) {
         if (decodedData.size() > 0) {
            System.out.println("writing decoded data to " + file.getPath());
            try {
               final BufferedWriter bw = new BufferedWriter(new FileWriter(file));

               // write the complete displayed html page to file
               bw.write(outText.getText());

               bw.close();
            } catch (final Exception E) {
               E.printStackTrace(System.out);
            }
         }
      }

      /**
       * Convert sample count to time string.
       *
       * @param count sample count (or index)
       * @return string containing time information
       */
      private String indexToTime(long count) {
         count -= startOfDecode;
         if (count < 0) {
            count = 0;
         }
         if (!analysisData.hasTimingData()) {
            return ("" + count);
         }
         final float time = (float) (count * (1.0 / analysisData.getRate()));
         if (time < 1.0e-6) {
            return (Math.rint(time * 1.0e9 * 100) / 100 + "ns");
         }
         if (time < 1.0e-3) {
            return (Math.rint(time * 1.0e6 * 100) / 100 + "µs");
         }
         if (time < 1.0) {
            return (Math.rint(time * 1.0e3 * 100) / 100 + "ms");
         }
         return (Math.rint(time * 100) / 100 + "s");
      }

      public void readProperties(final Properties properties) {
         selectByIndex(lineA, properties.getProperty("tools.I2CProtocolAnalysis.lineA"));
         selectByIndex(lineB, properties.getProperty("tools.I2CProtocolAnalysis.lineB"));
      }

      public void writeProperties(final Properties properties) {
         properties.setProperty("tools.I2CProtocolAnalysis.lineA", Integer.toString(lineA.getSelectedIndex()));
         properties.setProperty("tools.I2CProtocolAnalysis.lineB", Integer.toString(lineB.getSelectedIndex()));
      }

      /**
       * converts an integer to a hex string with leading zeros
       *
       * @param val        integer value for conversion
       * @param fieldWidth number of charakters in field
       * @return a nice string
       */
      private String integerToHexString(final int val, final int fieldWidth) {
         // first build a mask to cut off the signed extension
         int mask = (int) Math.pow(16.0, fieldWidth);
         mask--;
         final String str = Integer.toHexString(val & mask);
         int numberOfLeadingZeros = fieldWidth - str.length();
         if (numberOfLeadingZeros < 0) {
            numberOfLeadingZeros = 0;
         }
         if (numberOfLeadingZeros > fieldWidth) {
            numberOfLeadingZeros = fieldWidth;
         }
         final char zeros[] = new char[numberOfLeadingZeros];
         for (int i = 0; i < zeros.length; i++) {
            zeros[i] = '0';
         }
         final String ldz = new String(zeros);
         return (new String(ldz + str));
      }

      /**
       * converts an integer to a bin string with leading zeros
       *
       * @param val        integer value for conversion
       * @param fieldWidth number of charakters in field
       * @return a nice string
       */
      private String integerToBinString(final int val, final int fieldWidth) {
         // first build a mask to cut off the signed extension
         int mask = (int) Math.pow(2.0, (fieldWidth));
         mask--;
         final String str = Integer.toBinaryString(val & mask);
         int numberOfLeadingZeros = fieldWidth - str.length();
         if (numberOfLeadingZeros < 0) {
            numberOfLeadingZeros = 0;
         }
         if (numberOfLeadingZeros > fieldWidth) {
            numberOfLeadingZeros = fieldWidth;
         }
         final char zeros[] = new char[numberOfLeadingZeros];
         for (int i = 0; i < zeros.length; i++) {
            zeros[i] = '0';
         }
         final String ldz = new String(zeros);
         return (new String(ldz + str));
      }

      /**
       * runs the conversion when started
       */
      @Override
      public void run() {
         setControlsEnabled(false);
         btnConvert.setText("Abort");
         decode();
         setControlsEnabled(true);
         btnConvert.setText("Analyze");
      }

      private final JComboBox<String> lineA;
      private final JComboBox<String> lineB;
      private CapturedData analysisData;
      private final JEditorPane outText;
      private final Vector<I2CProtocolAnalysisDataSet> decodedData;
      private final JFileChooser fileChooser;
      private int startOfDecode;
      private int endOfDecode;
      private final JLabel busSetSCL;
      private final JLabel busSetSDA;
      private final JCheckBox detectSTART;
      private final JCheckBox detectSTOP;
      private final JCheckBox detectACK;
      private final JCheckBox detectNACK;
      private int statDecodedBytes;
      private int statBusErrorCount;

      private final JButton btnConvert;
      private final JButton btnExport;
      private final JButton btnCancel;

      private final JProgressBar progress;
      private boolean runFlag;

      private Thread thrWorker;

      private static final long serialVersionUID = 1L;
   }

   /**
    * Inner class defining a File Filter for CSV files.
    *
    */
   private class CSVFilter extends FileFilter {
      @Override
      public boolean accept(final File f) {
         return (f.isDirectory() || f.getName().toLowerCase().endsWith(".csv"));
      }

      @Override
      public String getDescription() {
         return ("Character sepatated Values (*.csv)");
      }
   }

   /**
    * Inner class defining a File Filter for HTML files.
    *
    */
   private class HTMLFilter extends FileFilter {
      @Override
      public boolean accept(final File f) {
         return (f.isDirectory() || f.getName().toLowerCase().endsWith(".html"));
      }

      @Override
      public String getDescription() {
         return ("Website (*.html)");
      }
   }

   public I2CProtocolAnalysis() {
   }

   @Override
   public void init(final Frame frame) {
      spad = new I2CProtocolAnalysisDialog(frame, getName());
   }

   /**
    * Returns the tools visible name.
    *
    * @return the tools visible name
    */
   @Override
   public String getName() {
      return ("I2C Protocol Analysis...");
   }

   /**
    * Convert captured data from timing data to state data using the given channel as clock.
    *
    * @param data - captured data to work on
    * @return always <code>null</code>
    */
   @Override
   public CapturedData process(final CapturedData data) {
      spad.showDialog(data);
      return (null);
   }

   /**
    * Reads dialog settings from given properties.
    *
    * @param properties Properties containing dialog settings
    */
   @Override
   public void readProperties(final Properties properties) {
      spad.readProperties(properties);
   }

   /**
    * Writes dialog settings to given properties.
    *
    * @param properties Properties where the settings are written to
    */
   @Override
   public void writeProperties(final Properties properties) {
      spad.writeProperties(properties);
   }

   private I2CProtocolAnalysisDialog spad;
}