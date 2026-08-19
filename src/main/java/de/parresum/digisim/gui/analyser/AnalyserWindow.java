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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.filechooser.FileFilter;

import de.parresum.digisim.gui.analyser.devices.DeviceController;
import de.parresum.digisim.gui.analyser.devices.DeviceController.DeviceState;
import de.parresum.digisim.gui.analyser.devices.FpgaDeviceController;
import de.parresum.digisim.gui.analyser.devices.WireDeviceController;
import de.parresum.digisim.gui.analyser.tools.StateAnalysis;
import de.parresum.digisim.gui.analyser.tools.Tool;

/**
 * Main frame and starter for Logic Analyzer Client.
 * <p>
 * This class only provides a simple end-user frontend and no functionality to be used by other code.
 *
 * @author Kai Uwe Bachmann
 */
public final class AnalyserWindow extends WindowAdapter
      implements Runnable, WindowListener, DiagramCursorChangeListener {

   private JMenu toolMenu;
   private JMenu diagramMenu;

   private JFileChooser fileChooser;
   private JFileChooser projectChooser;
   private DeviceController[] controllers;
   private int currentController;
   private Diagram diagram;
   private JScrollPane diagramPane;
   private Project project;
   private JLabel status;
   private Tool[] tools;
   private JCheckBoxMenuItem cursorsEnabledMenuItem;
   private JComboBox<String> currentDisplayPage;
   private JLabel maxDisplayPage;

   private JFrame frame;

   private static final String APP_NAME = "Logic Analyzer Client";

   /**
    * Creates a JMenu containing items as specified. If an item name is empty, a separator will be added in its place.
    *
    * @param name    Menu name
    * @param entries array of menu item names.
    * @return created menu
    */
   private JMenu createMenu(String name, AbstractAction[] entries) {
      JMenu menu = new JMenu(name);
      if (entries != null) {
         for (int i = 0; i < entries.length; i++) {
            if (entries[i] != null) {
               JMenuItem item = new JMenuItem(entries[i]);
               // item.addActionListener(this);
               menu.add(item);
            } else {
               menu.add(new JSeparator());
            }
         }
      }
      return (menu);
   }

   /**
    * Creates tool icons and adds them the the given tool bar.
    *
    * @param tools        tool bar to add icons to
    * @param files        array of icon file names
    * @param descriptions array of icon descriptions
    */
   private void createTools(JToolBar tools, AbstractAction[] actions) {

      for (int i = 0; i < actions.length; i++) {
         JButton b = new JButton(actions[i]);
         b.setMargin(new Insets(0, 0, 0, 0));
         tools.add(b);
      }
   }

   /**
    * Enables or disables functions that can only operate when captured data has been added to the diagram.
    *
    * @param enable set <code>true</code> to enable these functions, <code>false</code> to disable them
    */
   private void enableDataDependingFunctions(boolean enable) {
      diagramMenu.setEnabled(enable);
      toolMenu.setEnabled(enable);
   }

   /**
    * Inner class defining a File Filter for SLA files.
    *
    */
   private class SLAFilter extends FileFilter {
      public static final String FILE_EXTENSION = ".sla";

      @Override
      public boolean accept(File f) {
         return (f.isDirectory() || f.getName().toLowerCase().endsWith(FILE_EXTENSION));
      }

      @Override
      public String getDescription() {
         return ("Sump's Logic Analyzer Files (*" + FILE_EXTENSION + ")");
      }

   }

   /**
    * Inner class defining a File Filter for SLP files.
    *
    */
   private class SLPFilter extends FileFilter {

      public static final String FILE_EXTENSION = ".slp";

      @Override
      public boolean accept(File f) {
         return (f.isDirectory() || f.getName().toLowerCase().endsWith(FILE_EXTENSION));
      }

      @Override
      public String getDescription() {
         return ("Sump's Logic Analyzer Project Files (*" + FILE_EXTENSION + ")");
      }
   }

   /**
    * Default constructor.
    *
    */
   public AnalyserWindow() {
      super();
      project = new Project();
   }

   /**
    * Creates the GUI.
    *
    */
   void createGUI() {

      frame = new JFrame(APP_NAME);
      frame.setIconImage((new ImageIcon("icons/la.png")).getImage());
      Container contentPane = frame.getContentPane();
      contentPane.setLayout(new BorderLayout());

      JMenuBar mb = new JMenuBar();

      // file menu
      AbstractAction[] fileEntries = { //
            new AbstractAction("Open...") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doOpen();
               }

            }, //
            new AbstractAction("Save as...") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doSave();
               }

            }, //
            null, new AbstractAction("Exit") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doExit();
               }

            }//
      };
      JMenu fileMenu = createMenu("File", fileEntries);
      mb.add(fileMenu);

      // project menu
      AbstractAction[] projectEntries = { //
            new AbstractAction("Open Project...") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doOpenProject(); // +#####
               }

            }, //
            new AbstractAction("Save Project as...") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doSaveProjectAs();
               }

            }//
      };

      JMenu projectMenu = createMenu("Project", projectEntries);
      mb.add(projectMenu);

      // device menu
      LinkedList<DeviceController> loadedControllers = new LinkedList<DeviceController>();
      loadedControllers.add(new WireDeviceController());

      currentController = -1;
      if (loadedControllers.size() > 0) {
         currentController = 0;
         controllers = new DeviceController[loadedControllers.size()];
         for (int i = 0; i < loadedControllers.size(); i++) {
            if (loadedControllers.get(i) instanceof FpgaDeviceController) {
               currentController = i;
            }
            controllers[i] = loadedControllers.get(i);
            if (loadedControllers.get(i) instanceof Configurable) {
               project.addConfigurable(loadedControllers.get(i));
            }
         }

         // device controller menu is only added when at least one controller is available
         JMenu deviceMenu = null;
         if (controllers.length > 1) {
            // when more than one controller then add a controller seclection to menue
            AbstractAction[] deviceEntries = { //
                  new AbstractAction("Controller...") {

                     @Override
                     public void actionPerformed(ActionEvent e) {
                        doController();
                     }

                  }, //
                  new AbstractAction("Capture...") {

                     @Override
                     public void actionPerformed(ActionEvent e) {
                        doCapture();
                     }

                  }, //
                  new AbstractAction("Repeat Capture") {

                     @Override
                     public void actionPerformed(ActionEvent e) {
                        doRepeatCapture();
                     }

                  }//
            };
            deviceMenu = createMenu("Device", deviceEntries);
         } else {
            AbstractAction[] deviceEntries = { //
                  new AbstractAction("Capture...") {

                     @Override
                     public void actionPerformed(ActionEvent e) {
                        doCapture();
                     }

                  }, //
                  new AbstractAction("Repeat Capture") {

                     @Override
                     public void actionPerformed(ActionEvent e) {
                        doRepeatCapture();
                     }

                  }//
            };
            deviceMenu = createMenu("Device", deviceEntries);
         }
         mb.add(deviceMenu);

         System.out.println("Device Controller = " + controllers[currentController].getControllerName());
      }

      // diagram menu

      AbstractAction[] diagramEntries = { //
            new AbstractAction("Zoom In") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doZoomIn();
               }

            }, //
            new AbstractAction("Zoom Out") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doZoomOut();
               }

            }, //
            new AbstractAction("Default Zoom") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doDefaultZoom();
               }

            }, //
            new AbstractAction("Zoom Fit") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doZoomFit();
               }

            }, //
            null, new AbstractAction("Goto Trigger") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doGotoTrigger();
               }

            }, //
            new AbstractAction("Goto A") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doGotoA();
               }

            }, //
            new AbstractAction("Goto B") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doGotoB();
               }

            }, //
            null, new AbstractAction("Diagram Settings...") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doDiagramSettings();
               }

            }, //
            new AbstractAction("Labels...") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doLabels();
               }

            }//
      };
      diagramMenu = createMenu("Diagram", diagramEntries);
      cursorsEnabledMenuItem = new JCheckBoxMenuItem("Cursors");
      cursorsEnabledMenuItem.addActionListener(l -> doCursors(l));
      diagramMenu.add(cursorsEnabledMenuItem);
      mb.add(diagramMenu);

      // tools menu
      LinkedList<Tool> loadedTools = new LinkedList<Tool>();
      loadedTools.add(new StateAnalysis());

      if (loadedTools.size() > 0) {
         tools = new Tool[loadedTools.size()];
         Iterator<Tool> test = loadedTools.iterator();
         for (int i = 0; test.hasNext(); i++) {
            tools[i] = test.next();
         }

         AbstractAction[] toolEntries = new AbstractAction[tools.length];

         for (int i = 0; i < tools.length; i++) {
            final Tool tool = tools[i];
            tool.init(frame);
            toolEntries[i] = new AbstractAction(tool.getName()) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doTools(tool);

               }
            };
         }
         toolMenu = createMenu("Tools", toolEntries);
      } else {
         toolMenu = createMenu("Tools", null);
      }
      mb.add(toolMenu);

      // help menu
      AbstractAction[] helpEntries = { //
            new AbstractAction("About") {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doAbout();
               }

            } //
      };
      JMenu helpMenu = createMenu("Help", helpEntries);
      mb.add(helpMenu);

      frame.setJMenuBar(mb);

      JToolBar tools = new JToolBar();
      tools.setRollover(true);
      tools.setFloatable(false);

      AbstractAction[] fileToolsD = { //
            new AbstractAction("Open...", loadIcon("fileopen.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doOpen();

               }
            }, //
            new AbstractAction("Save as...", loadIcon("filesaveas.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doSave();

               }
            } //
      };

      createTools(tools, fileToolsD);
      tools.addSeparator();

      AbstractAction[] deviceToolsD = { //
            new AbstractAction("Capture...", loadIcon("launch.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doCapture();
               }
            }, //
            new AbstractAction("Repeat Capture", loadIcon("reload.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doRepeatCapture();
               }
            } //
      };
      createTools(tools, deviceToolsD);
      tools.addSeparator();

      AbstractAction[] diagramToolsD = { //
            new AbstractAction("Zoom In", loadIcon("viewmag+.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doZoomIn();
               }
            }, //
            new AbstractAction("Zoom Out", loadIcon("viewmag-.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doZoomOut();
               }
            }, //
            new AbstractAction("Default Zoom", loadIcon("viewmag1.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doDefaultZoom();
                  ;
               }
            }, //
            new AbstractAction("Zoom Fit", loadIcon("viewmag_fit.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doZoomFit();
               }
            }, //
            new AbstractAction("Goto Trigger", loadIcon("viewmag_cursor.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doGotoTrigger();
               }
            }, //
            new AbstractAction("Goto A", loadIcon("viewmag_a.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doGotoA();
               }
            }, //
            new AbstractAction("Goto B", loadIcon("viewmag_b.png")) {

               @Override
               public void actionPerformed(ActionEvent e) {
                  doGotoB();
               }
            } //
      };
      createTools(tools, diagramToolsD);
      tools.addSeparator();

      tools.add(new JLabel("Page "));
      String[] pages = { "1" };
      currentDisplayPage = new JComboBox<>(pages);
      currentDisplayPage.addActionListener(_ -> doComboBoxChanged());
      currentDisplayPage.setMaximumSize(new Dimension(60, currentDisplayPage.getMaximumSize().height));
      tools.add(currentDisplayPage);
      tools.add(new JLabel(" of "));
      maxDisplayPage = new JLabel("1");
      tools.add(maxDisplayPage);

      contentPane.add(tools, BorderLayout.NORTH);

      status = new JLabel(" ");
      contentPane.add(status, BorderLayout.SOUTH);

      diagram = new Diagram();
      project.addConfigurable(diagram);
      diagram.addStatusChangeListener(s -> statusChanged(s));
      diagram.setPreferredSize(contentPane.getSize());
      diagramPane = new JScrollPane(diagram);
      JScrollBar srb = diagramPane.getHorizontalScrollBar();
      srb.setUnitIncrement(10);
      srb.setBlockIncrement(30);
      contentPane.add(diagramPane, BorderLayout.CENTER);

      enableDataDependingFunctions(false);

      frame.setSize(1000, 700);
      frame.addWindowListener(this);
      frame.setVisible(true);

      fileChooser = new JFileChooser();
      fileChooser.addChoosableFileFilter(new SLAFilter());

      projectChooser = new JFileChooser();
      projectChooser.addChoosableFileFilter(new SLPFilter());

      diagram.addCursorChangeListener(this);
   }

   private Icon loadIcon(String name) {
      URL u = AnalyserWindow.class.getResource("/icons/" + name);
      return new ImageIcon(u);
   }

   private void updatePageInfo() {
      maxDisplayPage.setText("" + diagram.getMaxPages());
      currentDisplayPage.removeAllItems();
      for (int i = 0; i < diagram.getMaxPages(); i++) {
         currentDisplayPage.addItem(Integer.toString(i + 1));
      }
   }

   private void doOpen() {
      try {
         if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.isFile()) {
               loadData(file);
               frame.setTitle(APP_NAME + " - " + file.getName());
            }
            cursorsEnabledMenuItem.setSelected(diagram.getCursorMode());
            Container contentPane = frame.getContentPane();
            diagram.zoomFit((contentPane.getSize().width * 95) / 100);
            updatePageInfo();
         }
         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doSave() {
      try {
         if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(SLAFilter.FILE_EXTENSION)) {
               file = new File(file.getAbsolutePath() + SLAFilter.FILE_EXTENSION);
            }
            boolean writefile = true;
            if (file.exists() && (JOptionPane.showConfirmDialog(frame,
                  "The file " + file.getName() + " already exists. Overwrite it?", "Overwrite File",
                  JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.NO_OPTION)) {
               writefile = false;
            }
            if (writefile) {
               System.out.println("Saving: " + file.getName());
               diagram.getCapturedData().writeToFile(file);
               frame.setTitle(APP_NAME + " - " + file.getName());
            }
         }
         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doOpenProject() {
      try {
         if (projectChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = projectChooser.getSelectedFile();
            if (file.isFile()) {
               loadProject(file);
            }
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doSaveProjectAs() {
      try {
         if (projectChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = projectChooser.getSelectedFile();
            if (!file.getName().endsWith(SLPFilter.FILE_EXTENSION)) {
               file = new File(file.getAbsolutePath() + SLPFilter.FILE_EXTENSION);
            }
            boolean writefile = true;
            if (file.exists() && (JOptionPane.showConfirmDialog(frame,
                  "The file " + file.getName() + " already exists. Overwrite it?", "Overwrite File",
                  JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.NO_OPTION)) {
               writefile = false;
            }
            if (writefile) {
               System.out.println("Saving Project: " + file.getName());
               project.store(file);
            }
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doCapture() {
      try {
         if (currentController < 0) {
            return;
         }
         if (controllers[currentController].showCaptureDialog(frame) == DeviceState.DONE) {
            diagram.setCapturedData(controllers[currentController].getDeviceData(frame));
            Container contentPane = frame.getContentPane();
            diagram.zoomFit((contentPane.getSize().width * 95) / 100);
            cursorsEnabledMenuItem.setSelected(diagram.getCursorMode());
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doRepeatCapture() {
      try {
         if (currentController < 0) {
            return;
         }
         if (controllers[currentController].showCaptureProgress(frame) == DeviceState.DONE) {
            diagram.setCapturedData(controllers[currentController].getDeviceData(frame));
            Container contentPane = frame.getContentPane();
            diagram.zoomFit((contentPane.getSize().width * 95) / 100);
            cursorsEnabledMenuItem.setSelected(diagram.getCursorMode());
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doExit() {
      try {
         exit();

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doZoomIn() {
      try {
         diagram.zoomIn();
         updatePageInfo();

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doZoomOut() {
      try {
         diagram.zoomOut();
         updatePageInfo();

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doDefaultZoom() {
      try {
         diagram.zoomDefault();
         updatePageInfo();

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doZoomFit() {
      try {
         Container contentPane = frame.getContentPane();
         diagram.zoomFit((contentPane.getSize().width * 95) / 100);
         updatePageInfo();

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doGotoTrigger() {
      try {
         // do this only with data and trigger available
         if (diagram.hasCapturedData() && diagram.getCapturedData().hasTriggerData()) {
            gotoPosition(diagram.getCapturedData().getTriggerPosition());
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doGotoA() {
      try {
         if (diagram.getCursorMode()) {
            gotoPosition(diagram.getCapturedData().getCursorPositionA());
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doGotoB() {
      try {
         if (diagram.getCursorMode()) {
            gotoPosition(diagram.getCapturedData().getCursorPositionB());
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doDiagramSettings() {
      try {
         diagram.showSettingsDialog(frame);

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doLabels() {
      try {
         diagram.showLabelsDialog(frame);

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doCursors(ActionEvent event) {
      try {
         diagram.setCursorMode(((JCheckBoxMenuItem) event.getSource()).getState());

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doAbout() {
      try {
         JOptionPane.showMessageDialog(null,
               "Sump's Logic Analyzer Client\n" + "\n" + "Copyright 2006 Michael Poppitz\n"
                     + "This software is released under the GNU GPL.\n" + "\n" + "Version: cvs_03062010\n" + "\n"
                     + "For more information see:\n" + "http://www.sump.org/projects/analyzer/",
               "About", JOptionPane.INFORMATION_MESSAGE);

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doController() {
      try {
         String[] possibleControllers = new String[controllers.length];
         for (int i = 0; i < controllers.length; i++) {
            possibleControllers[i] = new String(controllers[i].getControllerName());
         }
         Object selectedController = JOptionPane.showInputDialog(null, "Select a Device Controller",
               "Device Controller", JOptionPane.INFORMATION_MESSAGE, null, possibleControllers,
               possibleControllers[currentController]);
         for (int i = 0; i < controllers.length; i++) {
            if (controllers[i].getControllerName().equals(selectedController)) {
               currentController = i;
            }
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doComboBoxChanged() {
      try {
         Object current = currentDisplayPage.getSelectedItem();
         if (current != null) {
            int page = Integer.parseInt((String) current);
            page--;
            diagramPane.getViewport().setViewPosition(new Point(0, 0));
            diagram.setCurrentPage(page);
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   private void doTools(Tool tool) {
      try {
         // check if a tool has been selected and if so, process captured data by tool
         for (int i = 0; i < tools.length; i++) {
            CapturedData newData = tool.process(diagram.getCapturedData());
            if (newData != null) {
               diagram.setCapturedData(newData);
            }
         }

         enableDataDependingFunctions(diagram.hasCapturedData());
      } catch (Exception E) {
         E.printStackTrace(System.out);
      }

   }

   /**
    * Diagram Cursor position changed notification
    */
   @Override
   public void onCursorChanged(int mousePos) {
      Point currentViewPos = diagramPane.getViewport().getViewPosition();
      int vpMin = currentViewPos.x;
      int vpMax = diagramPane.getViewport().getSize().width + currentViewPos.x;

      /*
       * The step size to left/right depends on the distance of the current mouse position to the right/left viewport
       * border. So when the mouse cursor is far outside the diagram scrolls faster.
       */
      int stepSizeLeft = Math.abs(mousePos - vpMin);
      int stepSizeRight = Math.abs(mousePos - vpMax);

      if (mousePos < vpMin) {
         currentViewPos.x -= stepSizeLeft;
         diagramPane.getViewport().setViewPosition(currentViewPos);
      }
      if (mousePos > vpMax) {
         currentViewPos.x += stepSizeRight;
         diagramPane.getViewport().setViewPosition(currentViewPos);
      }
   }

   /**
    * set Diagramm viewport position
    *
    * @param samplePos sample position
    */
   private void gotoPosition(long samplePos) {
      Dimension dim = diagram.getPreferredSize();
      JViewport vp = diagramPane.getViewport();

      // do nothing if the zoom factor is nearly the viewport size
      if (dim.width < vp.getWidth() * 2) {
         return;
      }

      currentDisplayPage.setSelectedIndex(diagram.getPage(samplePos));
      int pos = diagram.getTargetPosition(dim.width, samplePos) - 20;
      if (pos < 0) {
         pos = 0;
      }

      if (samplePos == 0) {
         vp.setViewPosition(new Point(0, 0));
      } else {
         vp.setViewPosition(new Point(pos, 0));
      }
   }

   /**
    * Handles status change requests.
    */
   public void statusChanged(String s) {
      status.setText(s);
   }

   /**
    * Handles window close requests.
    */
   @Override
   public void windowClosing(WindowEvent event) {
      exit();
   }

   /**
    * Load the given file as data.
    *
    * @param file file to be loaded as data
    * @throws IOException when an IO error occurs
    */
   public void loadData(File file) throws IOException {
      System.out.println("Opening: " + file.getName());
      diagram.setCapturedData(new CapturedData(file));
   }

   /**
    * Load the given file as project.
    *
    * @param file file to be loaded as projects
    * @throws IOException when an IO error occurs
    */
   public void loadProject(File file) throws IOException {
      System.out.println("Opening Project: " + file.getName());
      project.load(file);
   }

   /**
    * Starts GUI creation and displays it. Must be called be Swing event dispatcher thread.
    */
   @Override
   public void run() {
      createGUI();
   }

   /**
    * Tells the main thread to exit. This will stop the VM.
    */
   public void exit() {
      System.exit(0);
   }

}
