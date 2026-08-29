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

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import de.parresum.kicad.parser.eescheme.shape.Arc;
import de.parresum.kicad.parser.eescheme.shape.Radius;
import de.parresum.kicad.parser.model.Position;

/**
 *
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
         double angleStart = angles.getX();
         double angleDelta = angles.getY() - angles.getX();
         if (angleDelta < 0.0) {
            angleDelta += 360.0;
         }

         return new Arc2D.Double(center.getX() - r, center.getY() - r, center.getX() + r, center.getY() + r, angleStart,
               angleDelta, Arc2D.OPEN);

      } else {
         // 1. Calculate the circle center (h, k) and radius r
         double d = 2 * (start.getX() * (mid.getY() - end.getY()) + mid.getX() * (end.getY() - start.getY())
               + end.getX() * (start.getY() - mid.getY()));

         // Handle collinear points (straight line segment instead of an arc)
         if (Math.abs(d) < 1e-9) {
            double minX = Math.min(start.getX(), Math.min(mid.getX(), end.getX()));
            double maxX = Math.max(start.getX(), Math.max(mid.getX(), end.getX()));
            double minY = Math.min(start.getY(), Math.min(mid.getY(), end.getY()));
            double maxY = Math.max(start.getY(), Math.max(mid.getY(), end.getY()));
            return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
         }

         double startSq = start.getX() * start.getX() + start.getY() * start.getY();
         double midSq = mid.getX() * mid.getX() + mid.getY() * mid.getY();
         double endSq = end.getX() * end.getX() + end.getY() * end.getY();

         double h = (startSq * (mid.getY() - end.getY()) + midSq * (end.getY() - start.getY())
               + endSq * (start.getY() - mid.getY())) / d;

         double k = (startSq * (end.getX() - mid.getX()) + midSq * (start.getX() - end.getX())
               + endSq * (mid.getX() - start.getX())) / d;

         double r = Math.hypot(start.getX() - h, start.getY() - k);

         // 2. Calculate angles in radians (-PI to PI)
         double aStart = Math.atan2(start.getY() - k, start.getX() - h);
         double aMid = Math.atan2(mid.getY() - k, mid.getX() - h);
         double aEnd = Math.atan2(end.getY() - k, end.getX() - h);

         double angleStart = aStart / Math.PI * 180.0;
         double angleDelta = (aEnd - aStart) / Math.PI * 180.0;
         if (angleDelta < 0.0) {
            angleDelta += 360.0;
         }

         return new Arc2D.Double((k - r) * BASE_SCALE, (h - r) * -BASE_SCALE, //
               (k + r) * BASE_SCALE, (h + r) * -BASE_SCALE, //
               angleStart, angleDelta, Arc2D.OPEN);
      }

   }

}
