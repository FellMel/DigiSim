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

import java.awt.Graphics2D;

import de.parresum.kicad.parser.library.GraphPin;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class PinTextView extends AbstractTextView {
   private final double offset;
   private final double length;
   private final boolean inside;
   private final boolean isName;

   /**
    * @param item
    */
   public PinTextView(GraphPin pin, boolean inside, double offset) {
      super(pin.getPinName().getName(), pin.getPosition(), pin.getPinName().getEffects());
      isName = true;
      this.offset = offset * UNIT_FACTOR;
      this.inside = inside;
      this.length = pin.getLength() * UNIT_FACTOR;

   }

   public PinTextView(GraphPin pin) {
      super(pin.getPinNumber().getName(), pin.getPosition(), pin.getPinNumber().getEffects());
      isName = false;
      this.offset = 0;
      this.inside = false;
      this.length = pin.getLength() * UNIT_FACTOR;

   }

   @Override
   protected float getXPos(float width) {
      float xPos = 0.0f;
      if (inside && isName) {
         if (angle == 0) {
            xPos = (float) (length + offset);
         } else if (angle == 90) {
            xPos = (float) (length + offset);
         } else if (angle == 180) {
            xPos = (float) -(length + offset + width);
         } else if (angle == 270) {
            xPos = (float) -(length + offset + width);
         }
      } else {
         if (angle == 0) {
            xPos = (float) (length - width) / 2;
         } else if (angle == 90) {
            xPos = (float) (length - width) / 2;
         } else if (angle == 180) {
            xPos = (float) (-length - width) / 2;
         } else if (angle == 270) {
            xPos = (float) (-length - width) / 2;
         } else {
            xPos = (float) (length + width) / 2;
         }
      }
      return xPos;

   }

   @Override
   protected float getYPos(float descent, float ascent) {
      if (inside) {
         return (descent);
      }
      if (isName) {
         return -descent;
      } else {
         return ascent;
      }

   }

   @Override
   protected void translate(Graphics2D g2d, double mirrorX, double mirrorY) {
      // nothing to do, yet
   }

}
