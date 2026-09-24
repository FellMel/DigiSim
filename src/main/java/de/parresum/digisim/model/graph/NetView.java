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

import static de.parresum.digisim.model.ModelConstants.SOLID;
import static de.parresum.digisim.model.ModelConstants.UNIT_FACTOR;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.model.AbstractNetElement;
import de.parresum.digisim.model.NetJunction;
import de.parresum.digisim.model.NetPoint;
import de.parresum.digisim.model.NetWire;

/**
 * a graphical part representing a wire with all pathes
 *
 * @author Kai Uwe Bachmann
 */
public class NetView {
   private static final double RADIUS = 0.4 * UNIT_FACTOR;

   /** name of the network */
   private final String name;

   /** the graphical elements representing this network */
   private List<AbstractNetElement> elements = new ArrayList<>();

   /** the associated wire */
   private Wire wire;

   public NetView(String name, Wire wire) {
      this.name = name;
      this.wire = wire;
   }

   public void addElement(AbstractNetElement element) {
      this.elements.add(element);
   }

   public Wire getWire() {
      return wire;
   }

   public void setWire(Wire wire) {
      this.wire = wire;
   }

   public String getName() {
      return name;
   }

   public void paint(Graphics2D g2d) {
      Color col;
      switch (wire.get()) {
         case HIGH:
            col = Color.RED;
            break;
         case LOW:
            col = Color.GREEN;
            break;
         case OPEN:
            col = Color.BLUE;
            break;
         default:
            col = Color.BLACK;
            break;
      }
      g2d.setColor(col);
      g2d.setStroke(SOLID);

      for (AbstractNetElement element : elements) {
         switch (element) {
            case NetJunction j:
               paintJunction(g2d, j);
               break;
            case NetWire w:
               paintWire(g2d, w);
               break;
            default:
         }
      }

   }

   private void paintWire(Graphics2D g, NetWire wire) {
      NetPoint prev = null;
      for (NetPoint pt : wire.getPoints()) {
         if (prev != null) {
            g.draw(new Line2D.Double(prev.getX(), prev.getY(), pt.getX(), pt.getY()));
         }

         prev = pt;
      }
   }

   private void paintJunction(Graphics2D g, NetJunction junct) {
      NetPoint pt = junct.getPoints().getFirst();
      g.fill(new Ellipse2D.Double(pt.getX() - RADIUS, pt.getY() - RADIUS, RADIUS + RADIUS, RADIUS + RADIUS));
   }

   public Rectangle2D getBounding() {
      Rectangle2D bounding = null;
      for (AbstractNetElement element : elements) {
         switch (element) {
            case NetJunction j:
               bounding = boundJunction(bounding, j);
               break;
            case NetWire w:
               bounding = boundWire(bounding, w);
               break;
            default:
         }
      }

      return bounding;
   }

   private Rectangle2D boundWire(Rectangle2D bound, NetWire wire) {
      for (NetPoint pt : wire.getPoints()) {
         if (bound == null) {
            bound = new Rectangle2D.Double(pt.getX(), pt.getY(), 0, 0);
         } else {
            bound.add(pt.getX(), pt.getY());

         }
      }

      return bound;
   }

   private Rectangle2D boundJunction(Rectangle2D bound, NetJunction junct) {
      NetPoint pt = junct.getPoints().getFirst();
      if (bound == null) {
         bound = new Rectangle2D.Double(pt.getX() - RADIUS, pt.getY() - RADIUS, RADIUS + RADIUS, RADIUS + RADIUS);
      } else {
         bound.add(pt.getX() - RADIUS, pt.getY() - RADIUS);
         bound.add(pt.getX() + RADIUS, pt.getY() + RADIUS);
      }
      return bound;
   }
}
