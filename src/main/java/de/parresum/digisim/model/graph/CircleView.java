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
import java.awt.geom.Ellipse2D;

import de.parresum.kicad.parser.eescheme.shape.Circle;
import de.parresum.kicad.parser.model.Position;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class CircleView extends AbstractView {
   public CircleView(Circle circle) {
      super(circle, createShape(circle));
   }

   public static Shape createShape(Circle circle) {

      Position center = circle.getCenter();
      double radius = circle.getRadius();

      return new Ellipse2D.Double((center.getX() - radius) * BASE_SCALE, (center.getY() + radius) * -BASE_SCALE,
            radius * 2 * BASE_SCALE, radius * 2 * BASE_SCALE);

   }

}
