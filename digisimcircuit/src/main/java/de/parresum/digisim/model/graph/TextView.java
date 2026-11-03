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

import java.awt.Graphics2D;

import de.parresum.digisim.model.NetPoint;
import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.eescheme.shape.Text;
import de.parresum.kicad.parser.model.Property;

/**
 * A graphical text element
 *
 * @author Kai Uwe Bachmann
 */
public class TextView extends AbstractTextView {

   private NetPoint delta;

   public TextView(Text text) {
      super(text);
   }

   public TextView(Property prop) {
      super(prop.getValue(), prop.getAt(), prop.getEffects());
   }

   /**
    * @param item
    */
   public TextView(Pin pin, double dx, double dy) {
      super(pin.getName(), pin.getPosition(), pin.getEffects());
      this.delta = new NetPoint(dx, dy);
   }

   @Override
   protected void paintBackground(Graphics2D g2d) {
      // nothing to do, yet
   }

   @Override
   protected float getXPos(float width) {
      if (just != null && just.getHoritontalAlignment() != null) {
         switch (just.getHoritontalAlignment()) {
            case CENTER:
               return -width / 2.0f;
            case LEFT:
               return 0f;
            case RIGHT:
               return -width;
         }
      }
      return -width / 2.0f;
   }

   @Override
   protected float getYPos(float descent, float ascent) {
      if (just != null && just.getVerticalAlignment() != null) {
         switch (just.getVerticalAlignment()) {
            case CENTER:
               return ascent / 2.0f;
            case BOTTOM:
               return 0f;
            case TOP:
               return ascent;
         }
      }
      return ascent / 2.0f;
   }

   @Override
   protected void translate(Graphics2D g2d, double mirrorX, double mirrorY) {
      if (delta != null) {
         if ((this.angle + angle) <= 90.0) {
            g2d.translate(-mirrorX * delta.getX(), mirrorY * delta.getY());
         } else {
            g2d.translate(mirrorX * delta.getX(), mirrorY * delta.getY());
         }
      }

   }

}
