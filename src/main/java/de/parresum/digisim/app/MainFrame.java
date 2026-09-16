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

package de.parresum.digisim.app;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.SystemProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.app.i18n.LanguageManager;

/**
 * Main Application window
 *
 * @author Kai Uwe Bachmann
 */
public class MainFrame extends JFrame {
   private static final Logger LOG = LogManager.getLogger(MainFrame.class);

   // private final WindowPosManager windowPosManager;
   private CircuitComponent circuitComponent;
   private CircuitScrollPanel circuitScrollPanel;
   private final JLabel statusLabel;

   private Action openAction;
   private Action closeAction;

   private ProjectPanel projectPanel;

   public static void main(String... args) {
      MainFrame mainframe = new MainFrame();

      mainframe.setVisible(true);

   }

   public MainFrame() {
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter() {

         @Override
         public void windowClosing(WindowEvent e) {
            doQuit();

         }
      });

      ArrayList<Image> images = IconCreator.createImages("icon32.png", "icon64.png", "icon128.png");
      setIconImages(images);
      Taskbar.getTaskbar().setIconImage(images.getLast());

      setTitle(LanguageManager.get("title"));

      // windowPosManager = new WindowPosManager(this);

      statusLabel = new JLabel(" ");
      // statusLabel.setBorder(BorderFactory.createEmptyBorder(0, Screen.getInstance().getFontSize() * 2 / 3, 0, 0));
      getContentPane().add(statusLabel, BorderLayout.SOUTH);

      createMainMenu();

      createPanes();

      validate();
      repaint();
      setSize(400, 300);
   }

   private void createMainMenu() {
      JMenuBar mainMenu = new JMenuBar();
      JToolBar toolBar = new JToolBar();

      createFileMenu(mainMenu, toolBar);
      createEditMenu(mainMenu, toolBar);
      createViewMenu(mainMenu, toolBar);
      createSimulateMenu(mainMenu, toolBar);
      createAnalyseMenu(mainMenu, toolBar);
      createWindowMenu(mainMenu, toolBar);
      createHelpMemu(mainMenu, toolBar);

      setJMenuBar(mainMenu);
      getContentPane().add(toolBar, BorderLayout.NORTH);
   }

   private void createFileMenu(JMenuBar mainMenu, JToolBar toolbar) {
      JMenu file =

            new JMenu(LanguageManager.getAction("menu.file", new AbstractAction() {

               @Override
               public void actionPerformed(ActionEvent e) {

               }
            }));

      openAction = LanguageManager.getAction("menu.file.open", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doOpenProject();

         }
      });
      file.add(openAction);

      file.add(LanguageManager.getAction("menu.file.openNewWindow", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doOpenProjectNewWindow();

         }
      }));

      file.addSeparator();
      closeAction = LanguageManager.getAction("menu.file.closeProject", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doCloseProject();

         }
      });
      file.add(closeAction);

      file.addSeparator();
      file.add(LanguageManager.getAction("menu.file.quit", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doQuit();

         }
      }));
      closeAction.setEnabled(false);

      mainMenu.add(file);
   }

   private void createEditMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   private void createViewMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   private void createSimulateMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   private void createAnalyseMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   private void createWindowMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   private void createHelpMemu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   // ---------------------------------------- o ----------------------------------------
   private void createPanes() {
      JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

      createProjectView();
      createSchemeView();

      split.setLeftComponent(projectPanel);
      split.setRightComponent(circuitScrollPanel);

      getContentPane().add(split);

   }

   private void createProjectView() {
      projectPanel = new ProjectPanel();

   }

   private void createSchemeView() {
      circuitComponent = new CircuitComponent();
      circuitScrollPanel = new CircuitScrollPanel(circuitComponent);

      // circuitComponent = new CircuitComponent(/* this, library, shapeFactory */);
//      circuitComponent.addListener(this);
//      if (builder.circuit != null) {
//         LOGGER.debug("create with given circuit: " + builder.circuit.getOrigin());
//         SwingUtilities.invokeLater(() -> circuitComponent.setCircuit(builder.circuit));
//         setFilename(builder.fileToOpen, false);
//      } else {
//         if (builder.fileToOpen != null) {
//            LOGGER.debug("create with given file " + builder.fileToOpen);
//            SwingUtilities
//                  .invokeLater(() -> loadFile(builder.fileToOpen, builder.library == null, builder.library == null));
//         } else {
//            File name = fileHistory.getMostRecent();
//            LOGGER.debug("create with history file " + name);
//            if (name != null) {
//               SwingUtilities.invokeLater(() -> loadFile(name, true, false));
//            }
//         }
//      }
      // circuitScrollPanel = new CircuitScrollPanel(circuitComponent);
//      JComponent panel = new JLabel("Hello World");
//      getContentPane().add(panel, BorderLayout.CENTER);

   }

   // ---------------------------------------- o ----------------------------------------
   // Action methods
   // ---------------------------------------- o ----------------------------------------

   private void doOpenProject() {

      // File select project file
      JFileChooser fileChooser = new JFileChooser();

      String currentDir = AppPrefferences.getPref("currentDir", SystemProperties.getUserHome());

      // Optional: Startverzeichnis festlegen (z. B. Benutzerordner)
      fileChooser.setCurrentDirectory(new File(currentDir));
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.addChoosableFileFilter(new FileFilter() {
         @Override
         public String getDescription() {
            return "KiCad Project (*.kicad_pro)";
         }

         @Override
         public boolean accept(File f) {
            if (f.isDirectory()) {
               return true;
            } else {
               return f.getName().toLowerCase().endsWith(".kicad_pro");
            }
         }
      });

      // 2. Dialog anzeigen (parent ist z. B. ein JFrame oder null)
      int result = fileChooser.showOpenDialog(null);

      // 3. Ergebnis auswerten
      if (result != JFileChooser.APPROVE_OPTION) {
         return;
      }
      File file = fileChooser.getSelectedFile();
      AppPrefferences.setPref("currentDir", file.getParent());
      try {
         projectPanel.openProject(file);
         openAction.setEnabled(false);
         closeAction.setEnabled(true);
      } catch (IOException ex) {
         doError(ex, "main.error.openFile", file);
      }
      // open file in project view
   }

   private void doOpenProjectNewWindow() {
      System.out.println("open project in new window");

   }

   private void doCloseProject() {
      // TODO: Check whether a project is open
      // Ask for closing current project

      projectPanel.closeProject();
      openAction.setEnabled(true);
      closeAction.setEnabled(false);

   }

   private void doQuit() {
      String[] options = { LanguageManager.get("button.ok"), LanguageManager.get("button.cancel") };

      int confirm = JOptionPane.showOptionDialog(this, //
            LanguageManager.get("main.quit.confirm.msg"), //
            LanguageManager.get("main.quit.confirm.title"), //
            JOptionPane.DEFAULT_OPTION, //
            JOptionPane.WARNING_MESSAGE, //
            null, options, options[1]);

      if (confirm == JOptionPane.OK_OPTION) {
         System.exit(0);
      }
   }

   private void doError(Exception ex, String key, Object... params) {
      LOG.error("Error occoured", ex);

      String msg = LanguageManager.get(key + ".message", params);
      String title = LanguageManager.get(key + ".title");
      JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);

   }
}
