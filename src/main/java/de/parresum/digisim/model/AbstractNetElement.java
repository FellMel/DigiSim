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
package de.parresum.digisim.model;

import java.util.List;

/**
 * Base of net elements
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractNetElement {

   /** uuid of the element */
   private final String uuid;

   public AbstractNetElement(String uuid) {
      super();
      this.uuid = uuid;
   }

   /**
    * gets the uuid of the element
    *
    * @return
    */
   public String getUuid() {
      return uuid;
   }

   /**
    * checks, whether one of the given points is matched by the net element
    *
    * @param points points to check
    * @return true if at least one oint is identical
    */
   public abstract boolean containsPoint(List<NetPoint> points);

   /**
    * gets the list of points, defining the net element
    *
    * @return list of points
    */
   public abstract List<NetPoint> getPoints();

   /**
    * for debugging, only
    */
   public abstract void print();

   public boolean isPin() {
      return false;
   }
}
