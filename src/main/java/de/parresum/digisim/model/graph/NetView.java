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

import static de.parresum.digisim.model.graph.AbstractView.BASE_SCALE;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.model.AbstractNetElement;
import de.parresum.digisim.model.NetJunction;
import de.parresum.digisim.model.NetPin;
import de.parresum.digisim.model.NetPoint;
import de.parresum.digisim.model.NetWire;

/**
 * a graphical part representing a wire with all pathes
 *
 * @author Kai Uwe Bachmann
 */
public class NetView {
   private static final int RADIUS = 3;

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

   public void paint(Graphics g) {
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
      g.setColor(col);

      for (AbstractNetElement element : elements) {
         switch (element) {
            case NetJunction j:
               paintJunction(g, j);
               break;
            case NetPin p:
               paintPin(g, p);
               break;
            case NetWire w:
               paintWire(g, w);
               break;
            default:
         }
      }

   }

   private void paintWire(Graphics g, NetWire wire) {
      NetPoint prev = null;
      for (NetPoint pt : wire.getPoints()) {
         if (prev != null) {
            g.drawLine(zoom(prev.getX()), zoom(prev.getY()), zoom(pt.getX()), zoom(pt.getY()));
         }

         prev = pt;
      }
   }

   private void paintJunction(Graphics g, NetJunction junct) {
      NetPoint pt = junct.getPoints().getFirst();
      g.fillOval(zoom(pt.getX()) - RADIUS, zoom(pt.getY()) - RADIUS, RADIUS + RADIUS, RADIUS + RADIUS);
   }

   private void paintPin(Graphics g, NetPin pin) {
      // Pins will be drawn by parts ...
   }

   private int zoom(double val) {
      return (int) (val * BASE_SCALE);
   }
}
