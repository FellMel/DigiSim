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

package de.parresum.digisim.app.circuit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import de.parresum.digisim.app.i18n.LanguageManager;
import de.parresum.digisim.app.project.SchemeNode;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class CircuitDocumentPanel extends JTabbedPane {

   private static final long serialVersionUID = 6485199639452006664L;
   private Set<DocumentSelectionListener> listener = new CopyOnWriteArraySet<>();
   private SchemeNode currentDocument;

   private JPopupMenu contextMenu;
   private int contextTabIndex = -1;

   public CircuitDocumentPanel() {
      super();
      init();
   }

   public CircuitDocumentPanel(int tabPlacement, int tabLayoutPolicy) {
      super(tabPlacement, tabLayoutPolicy);
      init();
   }

   public CircuitDocumentPanel(int tabPlacement) {
      super(tabPlacement);
      init();
   }

   private void init() {
      setPreferredSize(new Dimension(800, 600));
      addChangeListener(_ -> {
         int index = getSelectedIndex();
         SchemeNode oldSelected = currentDocument;

         SchemeNode newSelected = null;
         if (index >= 0) {
            newSelected = ((CircuitComponent) getComponentAt(index)).getDocument();
         }
         currentDocument = newSelected;

         DocumentSelectedEvent evt = new DocumentSelectedEvent(CircuitDocumentPanel.this, oldSelected, newSelected);
         fireSelectionChanged(evt);
      });

      contextMenu = new JPopupMenu();
      contextMenu.add(LanguageManager.getAction("document.tab.context.closeThis", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doCloseThis();
         }
      }));
      contextMenu.add(LanguageManager.getAction("document.tab.context.closeOther", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doCloseOther();
         }
      }));
      contextMenu.add(LanguageManager.getAction("document.tab.context.closeAll", new AbstractAction() {

         @Override
         public void actionPerformed(ActionEvent e) {
            doCloseAll();
         }
      }));
   }

   public void openDocument(File baseDir, SchemeNode scheme) {
      CircuitComponent view = new CircuitComponent(baseDir, scheme);
      String name = scheme.getName();
      int idx = name.lastIndexOf(".");
      if (idx > 0) {
         name = name.substring(0, idx);
      }

      int present = this.indexOfTab(name);
      if (present >= 0) {
         // When document is already open, bring it to front
         setSelectedIndex(present);

         return;
      }

      addTab(name, scheme.getIcon(), view, scheme.getToolTipText());
      present = this.indexOfTab(name);
      setTabComponentAt(present, new CloseButtonTab(view, name, scheme.getIcon()));
      setSelectedIndex(present);
   }

   public void closeDocument(SchemeNode scheme) {

   }

   public void closeCurrentDocument() {
      int index = getSelectedIndex();
      if (index >= 0) {
         removeTabAt(index);
         currentDocument = null;
      }
   }

   public void zoomIn() {
      int index = getSelectedIndex();

      if (index >= 0) {
         CircuitComponent view = ((CircuitComponent) getComponentAt(index));
         view.zoomIn();
      }
   }

   public void zoomOut() {
      int index = getSelectedIndex();

      if (index >= 0) {
         CircuitComponent view = ((CircuitComponent) getComponentAt(index));
         view.zoomOut();
      }
   }

   public void zoomFit() {
      int index = getSelectedIndex();

      if (index >= 0) {
         CircuitComponent view = ((CircuitComponent) getComponentAt(index));
         view.zoomFit();
      }
   }

   public boolean canZoomIn() {
      int index = getSelectedIndex();

      if (index >= 0) {
         CircuitComponent view = ((CircuitComponent) getComponentAt(index));
         return view.canZoomIn();
      }
      return false;
   }

   public boolean canZoomOut() {
      int index = getSelectedIndex();

      if (index >= 0) {
         CircuitComponent view = ((CircuitComponent) getComponentAt(index));
         return view.canZoomOut();
      }
      return false;
   }

   private void doCloseThis() {
      if (contextTabIndex >= 0) {
         removeTabAt(contextTabIndex);
      }

      contextMenu.setVisible(false);
      contextTabIndex = -1;
   }

   private void doCloseOther() {
      if (contextTabIndex >= 0) {
         for (int i = getTabCount() - 1; i >= 0; i--) {
            if (i != contextTabIndex) {
               removeTabAt(i);
            }
         }
      }

      contextMenu.setVisible(false);
      contextTabIndex = -1;
   }

   private void doCloseAll() {
      if (contextTabIndex >= 0) {
         removeAll();
      }

      contextMenu.setVisible(false);
      contextTabIndex = -1;
   }

   public SchemeNode getCurrentDocument() {
      int index = getSelectedIndex();

      if (index >= 0) {
         CircuitComponent view = ((CircuitComponent) getComponentAt(index));
         return view.getDocument();
      }
      return null;
   }

   public void addSelectionListener(DocumentSelectionListener l) {

      listener.add(l);
   }

   public void removeSelectionListener(DocumentSelectionListener l) {
      listener.remove(l);
   }

   private void fireSelectionChanged(DocumentSelectedEvent evt) {
      for (DocumentSelectionListener item : listener) {
         item.documentSelected(evt);
      }
   }

   private static class TabButton extends JButton {
      private static final long serialVersionUID = -797657062424315149L;

      public TabButton() {
         int size = 14;
         setPreferredSize(new Dimension(size, size));
         setToolTipText(LanguageManager.get("document.tab.close.tooltip"));
         // Make the button looks the same for all Laf's
         setUI(new BasicButtonUI());
         // Make it transparent
         setContentAreaFilled(false);
         // No need to be focusable
         setFocusable(false);
         setBorder(BorderFactory.createEtchedBorder());
         setBorderPainted(false);
         // Making nice rollover effect
         // we use the same listener for all buttons
         // addMouseListener(buttonMouseListener);
         setRolloverEnabled(true);
         // Close the proper tab by clicking the button
      }

      // we don't want to update UI for this button
      @Override
      public void updateUI() {
      }

      // paint the cross
      @Override
      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g.create();
         // shift the image for pressed buttons
         if (getModel().isPressed()) {
            g2.translate(1, 1);
         }
         g2.setStroke(new BasicStroke(2));
         g2.setColor(Color.BLACK);
         if (getModel().isRollover()) {
            g2.setColor(Color.RED);
         }
         int delta = 3;
         g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
         g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
         g2.dispose();
      }
   }

   private class CloseButtonTab extends JPanel {
      private static final long serialVersionUID = 7402941309504150883L;
      private Component tab;
      private JLabel label;
      private JButton closingButton;

      public CloseButtonTab(Component aTab, String aTitle, Icon aIcon) {
         tab = aTab;
         setOpaque(false);
         setLayout(new GridBagLayout());
         MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               int tabIndex = indexOfComponent(tab);
               CircuitDocumentPanel.this.setSelectedIndex(tabIndex);
            }

            @Override
            public void mousePressed(MouseEvent e) {
               if (e.isPopupTrigger()) {
                  contextTabIndex = indexOfComponent(tab);
                  contextMenu.show(e.getComponent(), e.getX(), e.getY());
               }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               if (e.isPopupTrigger()) {
                  contextTabIndex = indexOfComponent(tab);
                  contextMenu.show(e.getComponent(), e.getX(), e.getY());
               }
            }
         };

         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(0, 0, 0, 5);

         label = new JLabel(aTitle);
         label.setIcon(aIcon);
         label.addMouseListener(listener);

         add(label, gbc);
         closingButton = new TabButton();
         closingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               int tabIndex = indexOfComponent(tab);

               CircuitDocumentPanel.this.setSelectedIndex(tabIndex);
               closeCurrentDocument();
            }
         });

         gbc.insets = new Insets(0, 0, 0, 0);
         add(closingButton, gbc);

         this.addMouseListener(listener);
      }
   }

}
