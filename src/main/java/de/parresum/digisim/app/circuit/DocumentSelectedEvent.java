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

import java.util.EventObject;

import de.parresum.digisim.app.project.SchemeNode;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class DocumentSelectedEvent extends EventObject {

   private final SchemeNode oldNode;
   private final SchemeNode newNode;

   public DocumentSelectedEvent(Object source, SchemeNode oldNode, SchemeNode newNode) {
      super(source);
      this.oldNode = oldNode;
      this.newNode = newNode;
   }

   public SchemeNode getOldNode() {
      return oldNode;
   }

   public SchemeNode getNewNode() {
      return newNode;
   }

}
