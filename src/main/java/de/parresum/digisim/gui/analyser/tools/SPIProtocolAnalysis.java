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
import java.awt.Container;
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
 * SPI Protocol analyzer
 *
 * @author Frank Kunz
 * @author Kai Uwe Bachmann
 *
 */
public class SPIProtocolAnalysis extends Base implements Tool, Configurable {

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
    * Class for SPI dataset
    *
    * @author Frank Kunz
    *
    *         A SPI dataset consists of a timestamp, MISO and MOSI values, or it can have an SPI event. This class is
    *         used to store the decoded SPI data in a Vector.
    */
   private class SPIProtocolAnalysisDataSet {
      public SPIProtocolAnalysisDataSet(final long tm, final int mo, final int mi) {
         this.time = tm;
         this.miso = mi;
         this.mosi = mo;
         this.event = null;
      }

      public SPIProtocolAnalysisDataSet(final long tm, final String ev) {
         this.time = tm;
         this.miso = 0;
         this.mosi = 0;
         this.event = new String(ev);
      }

      public boolean isEvent() {
         return (event != null);
      }

      public long time;
      public int miso;
      public int mosi;
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
   private class SPIProtocolAnalysisDialog extends JDialog implements ActionListener, Runnable {
      public SPIProtocolAnalysisDialog(final Frame frame, final String name) {
         super(frame, name, true);
         final Container pane = getContentPane();
         pane.setLayout(new GridBagLayout());
         getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

         decodedData = new Vector<SPIProtocolAnalysisDataSet>();
         startOfDecode = 0;

         /*
          * add protocol settings elements
          */
         final JPanel panSettings = new JPanel();
         panSettings.setLayout(new GridLayout(7, 2, 5, 5));
         panSettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Settings"),
               BorderFactory.createEmptyBorder(5, 5, 5, 5)));

         final String channels[] = new String[32];
         for (int i = 0; i < 32; i++) {
            channels[i] = new String("Channel " + i);
         }

         panSettings.add(new JLabel("SCK"));
         sck = new JComboBox<String>(channels);
         panSettings.add(sck);

         panSettings.add(new JLabel("MISO"));
         miso = new JComboBox<String>(channels);
         panSettings.add(miso);

         panSettings.add(new JLabel("MOSI"));
         mosi = new JComboBox<String>(channels);
         panSettings.add(mosi);

         panSettings.add(new JLabel("/CS"));
         cs = new JComboBox<String>(channels);
         panSettings.add(cs);

         panSettings.add(new JLabel("Mode"));
         modearray = new String[4];
         for (int i = 0; i < modearray.length; i++) {
            modearray[i] = new String("" + i);
         }
         mode = new JComboBox<String>(modearray);
         panSettings.add(mode);

         panSettings.add(new JLabel("Bits"));
         bitarray = new String[13];
         for (int i = 0; i < bitarray.length; i++) {
            bitarray[i] = new String("" + (i + 4));
         }
         bits = new JComboBox<String>(bitarray);
         bits.setSelectedItem("8");
         panSettings.add(bits);

         panSettings.add(new JLabel("Order"));
         orderarray = new String[2];
         orderarray[0] = new String("MSB first");
         orderarray[1] = new String("LSB first");
         order = new JComboBox<String>(orderarray);
         panSettings.add(order);
         pane.add(panSettings, createConstraints(0, 0, 1, 1, 0, 0));

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
         btnConvert = new JButton("Analyze");
         btnConvert.addActionListener(this);
         add(btnConvert, createConstraints(0, 4, 1, 1, 0.5, 0));
         btnExport = new JButton("Export");
         btnExport.addActionListener(this);
         add(btnExport, createConstraints(1, 4, 1, 1, 0.5, 0));
         btnCancel = new JButton("Close");
         btnCancel.addActionListener(this);
         add(btnCancel, createConstraints(2, 4, 1, 1, 0.5, 0));

         fileChooser = new JFileChooser();
         fileChooser.addChoosableFileFilter(new CSVFilter());
         fileChooser.addChoosableFileFilter(new HTMLFilter());

         setSize(1000, 500);
         setResizable(false);
         thrWorker = null;
         runFlag = false;
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
         sck.setEnabled(enable);
         miso.setEnabled(enable);
         mosi.setEnabled(enable);
         cs.setEnabled(enable);
         mode.setEnabled(enable);
         bits.setEnabled(enable);
         order.setEnabled(enable);
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
       * This is the SPI protocol decoder core
       *
       * The decoder scans for a decode start event like CS high to low edge or the trigger of the captured data. After
       * this the decoder starts to decode the data by the selected mode, number of bits and bit order. The decoded data
       * are put to a JTable object directly.
       */
      private void decode() {
         // process the captured data and write to output
         int a, c;
         int bitCount, mosivalue, misovalue, maxbits;

         // clear old data
         decodedData.clear();

         /*
          * Buid bitmasks based on the SCK, MISO, MOSI and CS pins.
          */
         final int csmask = (1 << cs.getSelectedIndex());
         final int sckmask = (1 << sck.getSelectedIndex());
         final int misomask = (1 << miso.getSelectedIndex());
         final int mosimask = (1 << mosi.getSelectedIndex());

         System.out.println("csmask   = 0x" + Integer.toHexString(csmask));
         System.out.println("sckmask  = 0x" + Integer.toHexString(sckmask));
         System.out.println("misomask = 0x" + Integer.toHexString(misomask));
         System.out.println("mosimask = 0x" + Integer.toHexString(mosimask));

         startOfDecode = 0;
         endOfDecode = analysisData.getValues().length;
         if (analysisData.isCursorEnabled()) {
            startOfDecode = analysisData.getSampleIndex(analysisData.getCursorPositionA());
            endOfDecode = analysisData.getSampleIndex(analysisData.getCursorPositionB() + 1);
         } else {
            /*
             * For analyze scan the CS line for a falling edge. If no edge could be found, the position of the trigger
             * is used for start of analysis. If no trigger and no edge is found the analysis fails.
             */
            a = analysisData.getValues()[0] & csmask;
            c = 0;
            for (int i = startOfDecode; i < endOfDecode; i++) {
               if (a > (analysisData.getValues()[i] & csmask)) {
                  // cs to low found here
                  startOfDecode = i;
                  c = 1;
                  System.out.println("CS found at " + i);
                  break;
               }
               a = analysisData.getValues()[i] & csmask;

               if (runFlag == false) {
                  return;
               }
               progress.setValue((int) (analysisData.getTimestamps()[i] * 100 / (endOfDecode - startOfDecode)));
            }
            if (c == 0) {
               // no CS edge found, look for trigger
               if (analysisData.hasTriggerData()) {
                  startOfDecode = analysisData.getSampleIndex(analysisData.getTriggerPosition());
               }
            }
            // now the trigger is in b, add trigger event to table
            decodedData.addElement(new SPIProtocolAnalysisDataSet(startOfDecode, "CSLOW"));
         }

         /*
          * Use the mode parameter to determine which edges are to detect. Mode 0 and mode 3 are sampling on the rising
          * clk edge, mode 2 and 4 are sampling on the falling edge. a is used for start of value, c is register for
          * detect line changes.
          */
         if ((mode.getSelectedItem().equals("0")) || (mode.getSelectedItem().equals("2"))) {
            // scanning for rising clk edges
            c = analysisData.getValues()[startOfDecode] & sckmask;
            a = analysisData.getValues()[startOfDecode] & csmask;
            bitCount = Integer.parseInt((String) bits.getSelectedItem()) - 1;
            maxbits = bitCount;
            misovalue = 0;
            mosivalue = 0;
            for (int i = startOfDecode; i < endOfDecode; i++) {
               if (c < (analysisData.getValues()[i] & sckmask)) {
                  // sample here
                  if (order.getSelectedItem().equals("MSB first")) {
                     if ((analysisData.getValues()[i] & misomask) == misomask) {
                        misovalue |= (1 << bitCount);
                     }
                     if ((analysisData.getValues()[i] & mosimask) == mosimask) {
                        mosivalue |= (1 << bitCount);
                     }
                  } else {
                     if ((analysisData.getValues()[i] & misomask) == misomask) {
                        misovalue |= (1 << (maxbits - bitCount));
                     }
                     if ((analysisData.getValues()[i] & mosimask) == mosimask) {
                        mosivalue |= (1 << (maxbits - bitCount));
                     }
                  }

                  if (bitCount > 0) {
                     bitCount--;
                  } else {
                     decodedData.addElement(new SPIProtocolAnalysisDataSet(
                           calculateTime(analysisData.getTimestamps()[i]), mosivalue, misovalue));

                     // System.out.println("MISO = 0x" + Integer.toHexString(misovalue));
                     // System.out.println("MOSI = 0x" + Integer.toHexString(mosivalue));
                     bitCount = Integer.parseInt((String) bits.getSelectedItem()) - 1;
                     misovalue = 0;
                     mosivalue = 0;

                  }
               }
               c = analysisData.getValues()[i] & sckmask;

               /* CS edge detection */
               if (a > (analysisData.getValues()[i] & csmask)) {
                  // falling edge
                  decodedData.addElement(
                        new SPIProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[i]), "CSLOW"));
               } else if (a < (analysisData.getValues()[i] & csmask)) {
                  // rising edge
                  decodedData.addElement(
                        new SPIProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[i]), "CSHIGH"));
               }
               a = analysisData.getValues()[i] & csmask;

               if (runFlag == false) {
                  return;
               }
               progress.setValue((int) (analysisData.getTimestamps()[i] * 100 / (endOfDecode - startOfDecode)));
            }
         } else {
            // scanning for falling clk edges
            c = analysisData.getValues()[startOfDecode] & sckmask;
            a = analysisData.getValues()[startOfDecode] & csmask;
            bitCount = Integer.parseInt((String) bits.getSelectedItem()) - 1;
            maxbits = bitCount;
            misovalue = 0;
            mosivalue = 0;
            for (int i = startOfDecode; i < endOfDecode; i++) {
               if (c > (analysisData.getValues()[i] & sckmask)) {
                  // sample here
                  if (order.getSelectedItem().equals("MSB first")) {
                     if ((analysisData.getValues()[i] & misomask) == misomask) {
                        misovalue |= (1 << bitCount);
                     }
                     if ((analysisData.getValues()[i] & mosimask) == mosimask) {
                        mosivalue |= (1 << bitCount);
                     }
                  } else {
                     if ((analysisData.getValues()[i] & misomask) == misomask) {
                        misovalue |= (1 << (maxbits - bitCount));
                     }
                     if ((analysisData.getValues()[i] & mosimask) == mosimask) {
                        mosivalue |= (1 << (maxbits - bitCount));
                     }
                  }

                  if (bitCount > 0) {
                     bitCount--;
                  } else {
                     decodedData.addElement(new SPIProtocolAnalysisDataSet(
                           calculateTime(analysisData.getTimestamps()[i]), mosivalue, misovalue));

                     // System.out.println("MISO = 0x" + Integer.toHexString(misovalue));
                     // System.out.println("MOSI = 0x" + Integer.toHexString(mosivalue));
                     bitCount = Integer.parseInt((String) bits.getSelectedItem()) - 1;
                     misovalue = 0;
                     mosivalue = 0;
                  }
               }
               c = analysisData.getValues()[i] & sckmask;

               /* CS edge detection */
               if (a > (analysisData.getValues()[i] & csmask)) {
                  // falling edge
                  decodedData.addElement(
                        new SPIProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[i]), "CSLOW"));
               } else if (a < (analysisData.getValues()[i] & csmask)) {
                  // rising edge
                  decodedData.addElement(
                        new SPIProtocolAnalysisDataSet(calculateTime(analysisData.getTimestamps()[i]), "CSHIGH"));
               }
               a = analysisData.getValues()[i] & csmask;

               if (runFlag == false) {
                  return;
               }
               progress.setValue((int) (analysisData.getTimestamps()[i] * 100 / (endOfDecode - startOfDecode)));
            }
         }

         outText.setText(toHtmlPage(false));
         outText.setEditable(false);
      }

      /**
       * exports the table data to a CSV file
       *
       * @param file File object
       */
      private void storeToCsvFile(final File file) {
         if (decodedData.size() > 0) {
            SPIProtocolAnalysisDataSet dSet;
            System.out.println("writing decoded data to " + file.getPath());
            try {
               final BufferedWriter bw = new BufferedWriter(new FileWriter(file));

               bw.write("\"" + "index" + "\",\"" + "time" + "\",\"" + "mosi data or event" + "\",\""
                     + "miso data or event" + "\"");
               bw.newLine();

               for (int i = 0; i < decodedData.size(); i++) {
                  dSet = decodedData.get(i);
                  if (dSet.isEvent()) {
                     bw.write("\"" + i + "\",\"" + indexToTime(dSet.time) + "\",\"" + dSet.event + "\",\"" + dSet.event
                           + "\"");
                  } else {
                     bw.write("\"" + i + "\",\"" + indexToTime(dSet.time) + "\",\"" + dSet.mosi + "\",\"" + dSet.miso
                           + "\"");
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

      /**
       * generate a HTML page
       *
       * @param empty if this is true an empty output is generated
       * @return String with HTML data
       */
      private String toHtmlPage(final boolean empty) {
         final Date now = new Date();
         final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
         final int bitCount = Integer.parseInt((String) bits.getSelectedItem());
         int bitAdder = 0;

         if (bitCount % 4 != 0) {
            bitAdder = 1;
         }

         // generate html page header
         final String header = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
               + "<html>" + "  <head>" + "    <title></title>" + "    <meta content=\"\">" + "    <style>"
               + "			th { text-align:left;font-style:italic;font-weight:bold;font-size:medium;font-family:sans-serif;background-color:#C0C0FF; }"
               + "		</style>" + "  </head>" + "	<body>" + "		<H2>SPI Analysis Results</H2>" + "		<hr>"
               + "			<div style=\"text-align:right;font-size:x-small;\">" + df.format(now) + "           </div>"
               + "		<br>";

         // generate the data table
         String data = "<table style=\"font-family:monospace;width:100%;\">"
               + "<tr><th style=\"width:15%;\">Index</th><th style=\"width:15%;\">Time</th><th style=\"width:10%;\">MOSI Hex</th><th style=\"width:10%;\">MOSI Bin</th><th style=\"width:8%;\">MOSI Dec</th><th style=\"width:7%;\">MOSI ASCII</th><th style=\"width:10%;\">MISO Hex</th><th style=\"width:10%;\">MISO Bin</th><th style=\"width:8%;\">MISO Dec</th><th style=\"width:7%;\">MISO ASCII</th></tr>";
         if (empty) {
         } else {
            SPIProtocolAnalysisDataSet ds;
            for (int i = 0; i < decodedData.size(); i++) {
               ds = decodedData.get(i);
               if (ds.isEvent()) {
                  // this is an event
                  if (ds.event.equals("CSLOW")) {
                     // start condition
                     data = data.concat("<tr style=\"background-color:#E0E0E0;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time)
                           + "</td><td>CSLOW</td><td></td><td></td><td></td><td>CSLOW</td><td></td><td></td><td></td></tr>");
                  } else if (ds.event.equals("CSHIGH")) {
                     // stop condition
                     data = data.concat("<tr style=\"background-color:#E0E0E0;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time)
                           + "</td><td>CSHIGH</td><td></td><td></td><td></td><td>CSHIGH</td><td></td><td></td><td></td></tr>");
                  } else {
                     // unknown event
                     data = data.concat("<tr style=\"background-color:#FF8000;\"><td>" + i + "</td><td>"
                           + indexToTime(ds.time)
                           + "</td><td>UNKNOWN</td><td></td><td></td><td></td><td>UNKNOWN</td><td></td><td></td><td></td></tr>");
                  }
               } else {
                  data = data
                        .concat("<tr style=\"background-color:#FFFFFF;\"><td>" + i + "</td><td>" + indexToTime(ds.time)
                              + "</td><td>" + "0x" + integerToHexString(ds.mosi, bitCount / 4 + bitAdder) + "</td><td>"
                              + "0b" + integerToBinString(ds.mosi, bitCount) + "</td><td>" + ds.mosi + "</td><td>");
                  if ((ds.mosi >= 32) && (bitCount == 8)) {
                     data += (char) ds.mosi;
                  }
                  data = data
                        .concat("</td><td>" + "0x" + integerToHexString(ds.miso, bitCount / 4 + bitAdder) + "</td><td>"
                              + "0b" + integerToBinString(ds.miso, bitCount) + "</td><td>" + ds.miso + "</td><td>");
                  if ((ds.miso >= 32) && (bitCount == 8)) {
                     data += (char) ds.miso;
                  }
                  data = data.concat("</td></tr>");
               }
            }
         }
         data = data.concat("</table");

         // generate the footer table
         final String footer = "	</body>" + "</html>";

         return (header + data + footer);
      }

      public void readProperties(final Properties properties) {
         selectByIndex(sck, properties.getProperty("tools.SPIProtocolAnalysis.sck"));
         selectByIndex(miso, properties.getProperty("tools.SPIProtocolAnalysis.miso"));
         selectByIndex(mosi, properties.getProperty("tools.SPIProtocolAnalysis.mosi"));
         selectByIndex(cs, properties.getProperty("tools.SPIProtocolAnalysis.cs"));
         selectByValue(mode, modearray, properties.getProperty("tools.SPIProtocolAnalysis.mode"));
         selectByValue(bits, bitarray, properties.getProperty("tools.SPIProtocolAnalysis.bits"));
         selectByValue(order, orderarray, properties.getProperty("tools.SPIProtocolAnalysis.order"));
      }

      public void writeProperties(final Properties properties) {
         properties.setProperty("tools.SPIProtocolAnalysis.sck", Integer.toString(sck.getSelectedIndex()));
         properties.setProperty("tools.SPIProtocolAnalysis.miso", Integer.toString(miso.getSelectedIndex()));
         properties.setProperty("tools.SPIProtocolAnalysis.mosi", Integer.toString(mosi.getSelectedIndex()));
         properties.setProperty("tools.SPIProtocolAnalysis.cs", Integer.toString(cs.getSelectedIndex()));
         properties.setProperty("tools.SPIProtocolAnalysis.mode", (String) mode.getSelectedItem());
         properties.setProperty("tools.SPIProtocolAnalysis.bits", (String) bits.getSelectedItem());
         properties.setProperty("tools.SPIProtocolAnalysis.order", (String) order.getSelectedItem());
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

      private final String[] modearray;
      private final String[] bitarray;
      private final String[] orderarray;

      private final JComboBox<String> sck;
      private final JComboBox<String> miso;
      private final JComboBox<String> mosi;
      private final JComboBox<String> cs;
      private final JComboBox<String> mode;
      private final JComboBox<String> bits;
      private CapturedData analysisData;
      private final JEditorPane outText;
      private final JComboBox<String> order;
      private final Vector<SPIProtocolAnalysisDataSet> decodedData;
      private final JFileChooser fileChooser;
      private int startOfDecode;
      private int endOfDecode;

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

   public SPIProtocolAnalysis() {
   }

   @Override
   public void init(final Frame frame) {
      spad = new SPIProtocolAnalysisDialog(frame, getName());
   }

   /**
    * Returns the tools visible name.
    *
    * @return the tools visible name
    */
   @Override
   public String getName() {
      return ("SPI Protocol Analysis...");
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

   private SPIProtocolAnalysisDialog spad;
}
