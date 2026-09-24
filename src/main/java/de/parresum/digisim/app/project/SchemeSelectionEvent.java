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

import java.util.EventObject;

import javax.swing.tree.TreePath;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class SchemeSelectionEvent extends EventObject {
   private final TreePath path;
   private final SchemeNode node;

   public SchemeSelectionEvent(Object source, TreePath path, SchemeNode node) {
      super(source);
      this.path = path;
      this.node = node;
   }

   public TreePath getPath() {
      return path;
   }

   public SchemeNode getNode() {
      return node;
   }

}
