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
import java.awt.geom.Rectangle2D;

import de.parresum.kicad.parser.eescheme.shape.Rectangle;
import de.parresum.kicad.parser.model.Position;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class RectangleView extends AbstractView {

   public RectangleView(Rectangle rect) {
      super(rect, createShape(rect));
   }

   public static Shape createShape(Rectangle rect) {
      Position start = rect.getStartPosition();
      Position end = rect.getEndPosition();

      return new Rectangle2D.Double(start.getX() * BASE_SCALE, start.getY() * -BASE_SCALE, //
            (end.getX() - start.getX()) * BASE_SCALE, (end.getY() - start.getY()) * -BASE_SCALE);

   }
}
