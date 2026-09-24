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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class ProjectSelectTree extends JTree {
   private final ProjectTreeModel treeModel;

   public ProjectSelectTree() {
      super();

      treeModel = new ProjectTreeModel();
      this.setModel(treeModel);

      setSelectionModel(new DefaultTreeSelectionModel());

      setCellRenderer(new ProjectCellRenderer());
      setToolTipText("");

   }

   @Override
   public String getToolTipText(MouseEvent e) {
      TreePath selPath = getPathForLocation(e.getX(), e.getY());
      if (selPath != null && selPath.getPathCount() > 0) {
         Object lp = selPath.getLastPathComponent();
         if (lp instanceof INode) {
            return ((INode) lp).getToolTipText();
         }
      }
      return null;
   }

   public void openProject(File file) throws IOException {
      treeModel.openProject(file);
   }

   public void closeProject() {
      treeModel.closeProject(null);
   }

   public boolean isProjectOpen() {
      return treeModel.isProjectOpen();
   }

   @Override
   public void addTreeSelectionListener(TreeSelectionListener listener) {
      getSelectionModel().addTreeSelectionListener(listener);
   }

   @Override
   public void removeTreeSelectionListener(TreeSelectionListener listener) {
      getSelectionModel().removeTreeSelectionListener(listener);
   }

   public void tmp() {
      addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            int selRow = getRowForLocation(e.getX(), e.getY());
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            if (selRow != -1 && e.getClickCount() == 2 && selPath != null) {
               Object selectedNode = selPath.getLastPathComponent();
               // do something else
            }
         }
      });
   }

   @Override
   public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
         boolean hasFocus) {
      if (value instanceof INode) {

         String sValue = ((INode) value).getName();
         if (sValue != null) {
            return sValue;
         }
      }
      return "";
   }

}
