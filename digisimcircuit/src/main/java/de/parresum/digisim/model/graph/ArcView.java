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
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import de.parresum.kicad.parser.eescheme.shape.Arc;
import de.parresum.kicad.parser.eescheme.shape.Radius;
import de.parresum.kicad.parser.model.Position;

/**
 * a graphical arc element
 *
 * @author Kai Uwe Bachmann
 */
public class ArcView extends AbstractView {

   public ArcView(Arc arc) {
      super(arc, createShape(arc));
   }

   public static Shape createShape(Arc arc) {

      Position start = arc.getStartPosition();
      Position mid = arc.getMidPosition();
      Position end = arc.getEndPosition();

      Radius radius = arc.getRadius();

      if (radius != null) {
         // start / end / radius.pos, radius.length, radius.angle
         // (arc
         // (start 0.0254 -5.0546)
         // (end 0.0254 -2.54)
         // (radius
         // (at 0 -3.81) --> center
         // (length 1.27) --> radius
         // (angles -88.9 88.9))) --> start / end
         // warum hier auch start / end ?????
         Position center = radius.getAt();
         double r = radius.getLength();
         Position angles = radius.getAngles();
         double angleStart = angles.getX() * UNIT_FACTOR;
         double angleDelta = angles.getY() * UNIT_FACTOR - angles.getX() * UNIT_FACTOR;
         if (angleDelta < 0.0) {
            angleDelta += 360.0;
         }

         return new Arc2D.Double(center.getX() * UNIT_FACTOR - r, center.getY() * UNIT_FACTOR - r,
               center.getX() * UNIT_FACTOR + r, center.getY() * UNIT_FACTOR + r, angleStart, angleDelta, Arc2D.OPEN);

      } else {
         // 1. Calculate the circle center (h, k) and radius r
         double d = 2 * (start.getX() * UNIT_FACTOR * (mid.getY() * UNIT_FACTOR - end.getY() * UNIT_FACTOR)
               + mid.getX() * UNIT_FACTOR * (end.getY() * UNIT_FACTOR - start.getY() * UNIT_FACTOR)
               + end.getX() * UNIT_FACTOR * (start.getY() * UNIT_FACTOR - mid.getY() * UNIT_FACTOR));

         // Handle collinear points (straight line segment instead of an arc)
         if (Math.abs(d) < 1e-9) {
            double minX = Math.min(start.getX() * UNIT_FACTOR,
                  Math.min(mid.getX() * UNIT_FACTOR, end.getX() * UNIT_FACTOR));
            double maxX = Math.max(start.getX() * UNIT_FACTOR,
                  Math.max(mid.getX() * UNIT_FACTOR, end.getX() * UNIT_FACTOR));
            double minY = Math.min(start.getY() * UNIT_FACTOR,
                  Math.min(mid.getY() * UNIT_FACTOR, end.getY() * UNIT_FACTOR));
            double maxY = Math.max(start.getY() * UNIT_FACTOR,
                  Math.max(mid.getY() * UNIT_FACTOR, end.getY() * UNIT_FACTOR));
            return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
         }

         double startSq = start.getX() * UNIT_FACTOR * start.getX() * UNIT_FACTOR
               + start.getY() * UNIT_FACTOR * start.getY() * UNIT_FACTOR;
         double midSq = mid.getX() * UNIT_FACTOR * mid.getX() * UNIT_FACTOR
               + mid.getY() * UNIT_FACTOR * mid.getY() * UNIT_FACTOR;
         double endSq = end.getX() * UNIT_FACTOR * end.getX() * UNIT_FACTOR
               + end.getY() * UNIT_FACTOR * end.getY() * UNIT_FACTOR;

         double h = (startSq * (mid.getY() * UNIT_FACTOR - end.getY() * UNIT_FACTOR)
               + midSq * (end.getY() * UNIT_FACTOR - start.getY() * UNIT_FACTOR)
               + endSq * (start.getY() * UNIT_FACTOR - mid.getY() * UNIT_FACTOR)) / d;

         double k = (startSq * (end.getX() * UNIT_FACTOR - mid.getX() * UNIT_FACTOR)
               + midSq * (start.getX() * UNIT_FACTOR - end.getX() * UNIT_FACTOR)
               + endSq * (mid.getX() * UNIT_FACTOR - start.getX() * UNIT_FACTOR)) / d;

         double r = Math.hypot(start.getX() * UNIT_FACTOR - h, start.getY() * UNIT_FACTOR - k);

         // 2. Calculate angles in radians (-PI to PI)
         double aStart = Math.atan2(start.getY() * UNIT_FACTOR - k, start.getX() * UNIT_FACTOR - h);
         double aMid = Math.atan2(mid.getY() * UNIT_FACTOR - k, mid.getX() * UNIT_FACTOR - h);
         double aEnd = Math.atan2(end.getY() * UNIT_FACTOR - k, end.getX() * UNIT_FACTOR - h);

         double angleStart = aStart / Math.PI * 180.0;
         double angleDelta = (aEnd - aStart) / Math.PI * 180.0;
         if (angleDelta < 0.0) {
            angleDelta += 360.0;
         }

         return new Arc2D.Double((k - r), (h - r), //
               (k + r), (h + r), //
               angleStart, angleDelta, Arc2D.OPEN);
      }

   }

}
