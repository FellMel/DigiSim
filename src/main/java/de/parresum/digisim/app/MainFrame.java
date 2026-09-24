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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.SystemProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.app.circuit.CircuitDocumentPanel;
import de.parresum.digisim.app.i18n.LanguageManager;
import de.parresum.digisim.app.project.INode;
import de.parresum.digisim.app.project.ProjectPanel;
import de.parresum.digisim.app.project.SchemeNode;

/**
 * Main Application window
 *
 * @author Kai Uwe Bachmann
 */
public class MainFrame extends JFrame {
   private static final Logger LOG = LogManager.getLogger(MainFrame.class);

   // private final WindowPosManager windowPosManager;
//   private CircuitComponent circuitComponent;
//   private CircuitScrollPanel circuitScrollPanel;
   private final JLabel statusLabel;

   private Action openAction;
   private Action closeAction;

   private Action openScheme;
   private List<Action> schemeOpenedActions = new ArrayList<>();

   private Action zoomIn;
   private Action zoomOut;
   private Action zoomFit;

   private ProjectPanel projectPanel;
   private CircuitDocumentPanel circuitPanel;

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

      createPanes();
      createMainMenu();

      validate();
      repaint();
      pack();
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

   // ---------------------------------------- o ----------------------------------------
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

   // ---------------------------------------- o ----------------------------------------
   private void createEditMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   // ---------------------------------------- o ----------------------------------------
   private void createViewMenu(JMenuBar mainMenu, JToolBar toolbar) {
      JMenu view =

            new JMenu(LanguageManager.getAction("menu.view", new AbstractAction() {

               @Override
               public void actionPerformed(ActionEvent e) {

               }
            }));

      openScheme = LanguageManager.getAction("menu.view.openScheme", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doOpenScheme();

         }
      });
      view.add(openScheme);

      view.addSeparator();
      Action closeScheme = LanguageManager.getAction("menu.view.closeScheme", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doCloseScheme();

         }
      });
      view.add(closeScheme);
      schemeOpenedActions.add(closeScheme);

      view.addSeparator();
      zoomIn = LanguageManager.getAction("menu.view.zoomIn", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doZoomIn();

         }
      });
      view.add(zoomIn);
      schemeOpenedActions.add(zoomIn);

      zoomOut = LanguageManager.getAction("menu.view.zoomOut", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doZoomOut();

         }
      });
      view.add(zoomOut);
      schemeOpenedActions.add(zoomOut);

      zoomFit = LanguageManager.getAction("menu.view.zoomFit", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doZoomFit();

         }
      });
      view.add(zoomFit);
      schemeOpenedActions.add(zoomFit);

      adjustViewActions();

      mainMenu.add(view);

   }

   protected void adjustViewActions() {
      INode node = projectPanel.getSelectedNode();
      if (node instanceof SchemeNode) {
         openScheme.setEnabled(true);
      } else {
         openScheme.setEnabled(false);
      }

      INode current = circuitPanel.getCurrentDocument();
      for (Action a : schemeOpenedActions) {
         a.setEnabled(current != null);
      }
   }

   // ---------------------------------------- o ----------------------------------------
   private void createSimulateMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   // ---------------------------------------- o ----------------------------------------
   private void createAnalyseMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   // ---------------------------------------- o ----------------------------------------
   private void createWindowMenu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   // ---------------------------------------- o ----------------------------------------
   private void createHelpMemu(JMenuBar mainMenu, JToolBar toolbar) {

   }

   // ---------------------------------------- o ----------------------------------------
   private void createPanes() {
      JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

      createProjectView();
      createSchemeView();

      split.setLeftComponent(projectPanel);
      split.setRightComponent(circuitPanel);
      split.setDividerLocation(200);

      getContentPane().add(split);

      circuitPanel.addChangeListener(_ -> {
         documentChanged();
      });

   }

   private void createProjectView() {
      projectPanel = new ProjectPanel();
      projectPanel.addTreeSelectionListener(l -> {
         if (l.isAddedPath()) {
            System.out.println("Path " + l.getPath().toString() + " selected");
         } else {
            System.out.println("Path " + l.getPath().toString() + " deselected");
         }
         adjustViewActions();
      });
      projectPanel.addDoubleClickListener(l -> {
         System.out.println("Project " + l.getNode().getName() + " opened");
         doOpenCircuit(l.getNode());
      });

   }

   private void createSchemeView() {
      circuitPanel = new CircuitDocumentPanel(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
      circuitPanel.addSelectionListener(l -> adjustViewActions());
//      circuitComponent = new CircuitComponent();
//      circuitScrollPanel = new CircuitScrollPanel(circuitComponent);

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

   // ---------------------------------------- o ----------------------------------------

   /**
   *
   */
   protected void doOpenScheme() {
      INode node = projectPanel.getSelectedNode();
      if (node instanceof SchemeNode) {
         doOpenCircuit((SchemeNode) node);
      }
   }

   private void doOpenCircuit(SchemeNode node) {
      circuitPanel.openDocument(node);
   }

   /**
    *
    */
   protected void doCloseScheme() {
      circuitPanel.closeCurrentDocument();
   }

   /**
    *
    */
   protected void doZoomIn() {
      circuitPanel.zoomIn();
      documentChanged();
   }

   /**
    *
    */
   protected void doZoomOut() {
      circuitPanel.zoomOut();
      documentChanged();
   }

   /**
    *
    */
   protected void doZoomFit() {
      circuitPanel.zoomFit();
      documentChanged();
   }

   public void documentChanged() {
      zoomIn.setEnabled(circuitPanel.canZoomIn());
      zoomOut.setEnabled(circuitPanel.canZoomOut());
      zoomFit.setEnabled(circuitPanel.getSelectedIndex() >= 0);
   }

   // ---------------------------------------- o ----------------------------------------
   private void doError(Exception ex, String key, Object... params) {
      LOG.error("Error occoured", ex);

      String msg = LanguageManager.get(key + ".message", params);
      String title = LanguageManager.get(key + ".title");
      JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);

   }
}
