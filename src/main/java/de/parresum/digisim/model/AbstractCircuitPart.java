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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.parresum.digisim.model.graph.AbstractView;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractCircuitPart {
   /** name of the symbol */
   protected final String name;

   /** pins of the symbol */
   protected final Map<String, CircuitPin> pins = new HashMap<>();

   protected final List<AbstractView> graphic = new ArrayList<>();

   public AbstractCircuitPart(String name) {
      super();
      this.name = name;
   }

   protected Rectangle2D translate(Rectangle2D bound, NetPoint position, int angle, boolean mirrorX, boolean mirrorY) {
      double xmin, ymin;
      double xmax, ymax;

      // rotate(Math.toRadians(-angle));
      // xnew = x * cos + y * sin
      // ynew = -x * sin + y * cos
      switch (angle) {
         case 0: // cos = 1 ; sin = 0
         default: // unknown angle is like 0 degree
            xmin = bound.getMinX();
            ymin = bound.getMinY();
            xmax = bound.getMaxX();
            ymax = bound.getMaxY();
            break;

         case 90:// cos = 0 ; sin = -1
            xmin = -bound.getMinY();
            ymin = bound.getMinX();
            xmax = -bound.getMaxY();
            ymax = bound.getMaxX();
            break;

         case 180:// cos = -1 ; sin = 0
            xmin = -bound.getMinX();
            ymin = -bound.getMinY();
            xmax = -bound.getMaxX();
            ymax = -bound.getMaxY();
            break;

         case 270:// cos = 0 ; sin = 1
            xmin = bound.getMinY();
            ymin = -bound.getMinX();
            xmax = bound.getMaxY();
            ymax = -bound.getMaxX();
            break;
      }

      if (mirrorX) {
         double tmp = ymin;
         ymin = ymax;
         ymax = tmp;
      } else if (mirrorY) {
         double tmp = xmin;
         xmin = xmax;
         xmax = tmp;
      }

      xmin += position.getX();
      ymin += position.getY();
      xmax += position.getX();
      ymax += position.getY();

      return new Rectangle2D.Double(//
            Math.min(xmin, xmax), //
            Math.min(ymin, ymax), //
            Math.abs(xmax - xmin), //
            Math.abs(ymax - ymin));
   }

   public abstract CircuitPin getPin(String number);

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   public void paint(Graphics2D g) {
      for (AbstractView item : graphic) {
         item.paint(g);
      }
   }

   public Rectangle2D getBounding() {

      Rectangle2D bounding = new Rectangle2D.Double(0, 0, 0, 0);
      for (AbstractView item : graphic) {
         Rectangle2D bound = item.getBounding();
         bounding.add(bound);
      }
      return bounding;
   }

}
