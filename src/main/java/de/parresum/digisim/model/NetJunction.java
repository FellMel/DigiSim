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

import static de.parresum.digisim.model.ModelConstants.UNIT_FACTOR;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.parresum.kicad.parser.eescheme.Junction;

/**
 * extracted junction point in scheme
 *
 * @author Kai Uwe Bachmann
 */
public class NetJunction extends AbstractNetElement {
   private static final double RADIUS = 0.4 * UNIT_FACTOR;

   /** point of the junction */
   private final NetPoint point;

   public NetJunction(Junction junction) {
      super(junction.getUuid().getUuid());

      point = new NetPoint(junction.getPosition().getX() * UNIT_FACTOR, junction.getPosition().getY() * UNIT_FACTOR);

   }

   @Override
   public boolean containsPoint(List<NetPoint> points) {
      for (NetPoint pt : points) {
         if (this.point.equals(pt)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public List<NetPoint> getPoints() {
      return List.of(point);
   }

   @Override
   public void paint(Graphics2D g) {
      g.fill(new Ellipse2D.Double(point.getX() - RADIUS, point.getY() - RADIUS, RADIUS + RADIUS, RADIUS + RADIUS));

   }

   @Override
   public Rectangle2D getBounding() {
      return new Rectangle2D.Double(point.getX() - RADIUS, point.getY() - RADIUS, RADIUS + RADIUS, RADIUS + RADIUS);
   }

   /**
    * for debugging, only
    */
   @Override
   public void print() {
//      System.out.println(String.format("  junction (%.2f, %.2f)", point.getX(), point.getY()));
      // nothing to do, yet

   }

}
