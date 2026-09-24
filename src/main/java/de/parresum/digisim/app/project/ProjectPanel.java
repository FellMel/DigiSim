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

package de.parresum.digisim.app.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class ProjectPanel extends JPanel {
   private final ProjectSelectTree tree;

   private Set<SchemeDoubleClickListener> listener = new CopyOnWriteArraySet<>();

   public ProjectPanel() {
      super(new BorderLayout());
      setPreferredSize(new Dimension(200, 200));

      tree = new ProjectSelectTree();
      this.add(new JScrollPane(tree));

      tree.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if (selRow != -1 && e.getClickCount() == 2 && selPath != null) {
               Object selectedNode = selPath.getLastPathComponent();
               if (selectedNode instanceof SchemeNode) {
                  SchemeSelectionEvent evt = new SchemeSelectionEvent(this, selPath, (SchemeNode) selectedNode);
                  fireDoubleClick(evt);
               }
            }
         }
      });

   }

   public void addTreeSelectionListener(TreeSelectionListener listener) {
      tree.addTreeSelectionListener(listener);
   }

   public void removeTreeSelectionListener(TreeSelectionListener listener) {
      tree.removeTreeSelectionListener(listener);
   }

   public void addDoubleClickListener(SchemeDoubleClickListener l) {

      listener.add(l);
   }

   public void removeDoubleClickListener(SchemeDoubleClickListener l) {
      listener.remove(l);
   }

   private void fireDoubleClick(SchemeSelectionEvent evt) {
      for (SchemeDoubleClickListener item : listener) {
         item.projectDoubleclicked(evt);
      }
   }

   public void openProject(File file) throws IOException {
      tree.openProject(file);
   }

   public void closeProject() {
      tree.closeProject();
   }

   public INode getSelectedNode() {
      TreePath path = tree.getSelectionPath();
      if (path == null) {
         return null;
      }
      return (INode) path.getLastPathComponent();
   }
}
