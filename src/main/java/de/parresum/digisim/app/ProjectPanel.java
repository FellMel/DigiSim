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
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.parresum.digisim.app.project.ProjectSelectTree;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class ProjectPanel extends JPanel {
   private final ProjectSelectTree tree;

   public ProjectPanel() {
      super(new BorderLayout());

      tree = new ProjectSelectTree();
      this.add(new JScrollPane(tree));

   }

   public void openProject(File file) throws IOException {
      tree.openProject(file);
   }

   public void closeProject() {
      tree.closeProject();
   }
}
