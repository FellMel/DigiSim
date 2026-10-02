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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class ProjectCellRenderer extends DefaultTreeCellRenderer {
   private static final long serialVersionUID = -4111174665824688230L;
   private Icon icon;

   @Override
   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
         boolean leaf, int row, boolean hasFocus) {
      if (value instanceof INode) {
         icon = ((INode) value).getIcon();
      }
      JLabel comp = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

      return comp;
   }

   @Override
   public Icon getOpenIcon() {
      return icon;
   }

   @Override
   public Icon getClosedIcon() {
      return icon;
   }

   /**
    * Returns the icon used to represent leaf nodes.
    *
    * @return the icon used to represent leaf nodes
    */
   @Override
   public Icon getLeafIcon() {
      return icon;
   }

}
