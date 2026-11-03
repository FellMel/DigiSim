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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.kicad.parser.eescheme.Wire;
import de.parresum.kicad.parser.model.PointList;
import de.parresum.kicad.parser.model.Position;

/**
 * Extracted wire from Scheme
 *
 * @author Kai Uwe Bachmann
 */
public class NetWire extends AbstractNetElement {
   private final static Logger LOG = LogManager.getLogger(NetWire.class);

   /**
    * List of points, the wire is defined by
    */
   private List<NetPoint> points;

   /**
    * Creates a wire definition from KiCad element
    *
    * @param wire wire to create from
    */
   public NetWire(Wire wire) {
      super(wire.getUuid().getUuid());
      PointList pointList = wire.getPoints();
      points = new ArrayList<>(pointList.getPoints().size());
      for (Position pt : pointList.getPoints()) {
         points.add(new NetPoint(pt.getX() * UNIT_FACTOR, pt.getY() * UNIT_FACTOR));
      }
   }

   /**
    * checks whether the given point is part of the wire definition
    *
    * @return true, when the point is in the point list
    */
   @Override
   public boolean containsPoint(List<NetPoint> netPoints) {
      for (NetPoint myPt : this.points) {
         for (NetPoint netPt : netPoints) {
            if (myPt.equals(netPt)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * gets the point list
    */
   @Override
   public List<NetPoint> getPoints() {
      return points;
   }

   @Override
   public void paint(Graphics2D g) {
      NetPoint prev = null;
      for (NetPoint pt : points) {
         if (prev != null) {
            g.draw(new Line2D.Double(prev.getX(), prev.getY(), pt.getX(), pt.getY()));
         }

         prev = pt;
      }
   }

   @Override
   public Rectangle2D getBounding() {
      Rectangle2D bound = null;
      for (NetPoint pt : points) {
         if (bound == null) {
            bound = new Rectangle2D.Double(pt.getX(), pt.getY(), 0, 0);
         } else {
            bound.add(pt.getX(), pt.getY());
         }
      }

      return bound;
   }

   /**
    * for debugging, only
    */
   @Override
   public void print() {
      LOG.info(String.format("  wire (%.2f, %.2f) - (%.2f, %.2f)", points.get(0).getX(), points.get(0).getY(),
            points.get(1).getX(), points.get(1).getY()));
   }

}
