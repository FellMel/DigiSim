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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import de.parresum.digisim.app.IconCreator;
import de.parresum.kicad.parser.eescheme.Sheet;
import de.parresum.kicad.parser.model.Property;
import de.parresum.kicad.parser.project.Project;
import de.parresum.kicad.parser.project.ProjectParser;
import de.parresum.kicad.parser.project.TopLevelSheet;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class ProjectNode implements TreeNode, INode {

   private final List<SchemeNode> children = new ArrayList<>();
   private final String name;
   private final File file;
   private String toolTipText;
   private Icon icon;
   private Project project;

   /**
    * Creates a new leaf
    *
    * @param file the file containing the leaf
    * @throws IOException
    */
   public ProjectNode(File file) throws IOException {
      this.file = file;
      name = file.getName();
      if (name.toLowerCase().endsWith("kicad_pro")) {
         readProject();
      } else {
         throw new IllegalArgumentException("Not a project file: " + name);
      }

      readSchemes();
   }

   private void readProject() throws IOException {
      this.project = ProjectParser.readProject(file);
      toolTipText = file.getAbsolutePath();
      icon = IconCreator.create("project_16.png");
   }

   private void readSchemes() throws IOException {
      File dir = file.getParentFile();
      File[] files = dir.listFiles(new FilenameFilter() {

         @Override
         public boolean accept(File dir, String name) {
            return name.endsWith(".kicad_sch");
         }
      });

      // first read all files
      Map<String, SchemeNode> schemas = new HashMap<>();
      for (File in : files) {
         SchemeNode node = new SchemeNode(in, this);
         schemas.put(in.getName(), node);
      }

      // now create hierarchy
      for (TopLevelSheet sheet : project.getSchematic().getTopLevelSheets()) {
         String filename = sheet.getFilename();

         SchemeNode schema = schemas.get(filename);
         if (schema != null) {
            children.add(schema);
            registerScheme(schema, schemas);
         }
      }

      // add unassigned schemes
      if (!schemas.isEmpty()) {
         for (SchemeNode item : schemas.values()) {
            children.add(item);
         }
      }
   }

   /**
    * removes the current schema from the list and registers sub schemas
    *
    * @param schema
    * @param schemas
    */
   private void registerScheme(SchemeNode schema, Map<String, SchemeNode> schemas) {
      schemas.remove(schema.getFile().getName());

      for (Sheet subsheet : schema.getScheme().getSheets()) {
         String subfilename = null;
         for (Property prop : subsheet.getProperties()) {
            if ("Sheetfile".equals(prop.getKey())) {
               subfilename = prop.getValue();
            }
         }
         if (subfilename != null) {

            SchemeNode subschema = schemas.get(subfilename);
            if (subschema != null) {
               schema.addChild(subschema);
               registerScheme(subschema, schemas);
            }
         }
      }
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public String getToolTipText() {
      return toolTipText;
   }

   @Override
   public Icon getIcon() {
      return icon;
   }

   @Override
   public TreeNode getParent() {
      return null;
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

   @Override
   public String toString() {
      return "ProjectNode [" + name + "]";
   }

}
