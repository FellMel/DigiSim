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

package de.parresum.digisim.model.graph;

import static de.parresum.digisim.model.ModelConstants.UNIT_FACTOR;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;

import de.parresum.kicad.parser.eescheme.shape.Polyline;
import de.parresum.kicad.parser.model.Position;

/**
 * A graphical polygon element
 *
 * @author Kai Uwe Bachmann
 */
public class PolygonView extends AbstractView {
   private final Polyline polyline;

   public PolygonView(Polyline polyline) {
      super(polyline, createShape(polyline));
      this.polyline = polyline;
   }

   public static Shape createShape(Polyline polyline) {
      polyline.getPoints();

      Double path = new Path2D.Double();
      boolean first = true;
      for (Position pt : polyline.getPoints().getPoints()) {
         if (first) {
            path.moveTo(pt.getX() * UNIT_FACTOR, pt.getY() * UNIT_FACTOR);
            first = false;
         } else {
            path.lineTo(pt.getX() * UNIT_FACTOR, pt.getY() * UNIT_FACTOR);
         }
      }

      return path;
   }

}
