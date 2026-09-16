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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import de.parresum.digisim.app.IconCreator;
import de.parresum.kicad.parser.eescheme.SchemaParser;
import de.parresum.kicad.parser.eescheme.Schematic;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class SchemeNode implements TreeNode, INode {
   private final String uuid;
   private final String name;
   private final File file;
   private final String toolTipText;
   private final Icon icon;
   private final Schematic scheme;
   private final ProjectNode parent;
   private List<SchemeNode> children = new ArrayList<>();
   private SchemeNode parentScheme;
   private boolean isSubnode;

   public SchemeNode(File file, ProjectNode project) throws IOException {
      this.file = file;
      this.parent = project;
      toolTipText = file.getAbsolutePath();

      scheme = SchemaParser.readSchema(file);

      this.uuid = scheme.getUuid().getUuid();
//      String tmp = scheme.getTitleBlock().getTitle();
//      if (tmp == null || tmp.isBlank()) {
      this.name = file.getName();
//      } else {
//         this.name = tmp;
//      }

      icon = IconCreator.create("icon_eeschema_24_16.png");
   }

   public void addChild(SchemeNode child) {
      this.children.add(child);
      child.isSubnode = true;
      child.parentScheme = this;
   }

   public boolean isSubnode() {
      return isSubnode;
   }

   public String getUuid() {
      return uuid;
   }

   @Override
   public String getName() {
      return name;
   }

   public File getFile() {
      return file;
   }

   @Override
   public String getToolTipText() {
      return toolTipText;
   }

   @Override
   public Icon getIcon() {
      return icon;
   }

   public Schematic getScheme() {
      return scheme;
   }

   @Override
   public TreeNode getParent() {
      return parent;
   }

   @Override
   public TreeNode getChildAt(int childIndex) {
      return children.get(childIndex);
   }

   @Override
   public int getChildCount() {
      return children.size();
   }

   @Override
   public int getIndex(TreeNode node) {
      return children.indexOf(node);
   }

   @Override
   public boolean getAllowsChildren() {
      return true;
   }

   @Override
   public boolean isLeaf() {
      return children.isEmpty();
   }

   @Override
   public Enumeration<? extends TreeNode> children() {
      return Collections.enumeration(children);
   }

}
