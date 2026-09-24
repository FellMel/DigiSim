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
import java.awt.geom.Rectangle2D;

import de.parresum.kicad.parser.eescheme.shape.Rectangle;
import de.parresum.kicad.parser.model.Position;

/**
 * A graphical rectangle element
 *
 * @author Kai Uwe Bachmann
 */
public class RectangleView extends AbstractView {

   private final Rectangle rect;

   public RectangleView(Rectangle rect) {
      super(rect, createShape(rect));
      this.rect = rect;
   }

   public static Shape createShape(Rectangle rect) {
      Position start = rect.getStartPosition();
      Position end = rect.getEndPosition();

      double x = Math.min(start.getX() * UNIT_FACTOR, end.getX() * UNIT_FACTOR);
      double y = Math.min(start.getY() * UNIT_FACTOR, end.getY() * UNIT_FACTOR);
      double w = Math.abs(end.getX() * UNIT_FACTOR - start.getX() * UNIT_FACTOR);
      double h = Math.abs(end.getY() * UNIT_FACTOR - start.getY() * UNIT_FACTOR);

      return new Rectangle2D.Double(x, y, w, h);

   }

}
