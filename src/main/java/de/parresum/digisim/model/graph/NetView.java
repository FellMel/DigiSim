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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.model.AbstractNetElement;

/**
 * a graphical part representing a wire with all paths
 *
 * @author Kai Uwe Bachmann
 */
public class NetView {

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
         element.paint(g2d);
      }
   }

   public Rectangle2D getBounding() {
      Rectangle2D bounding = null;
      for (AbstractNetElement element : elements) {
         Rectangle2D tmp = element.getBounding();
         if (bounding == null) {
            bounding = tmp;
         } else {
            bounding.add(tmp);
         }
      }

      return bounding;
   }

}
