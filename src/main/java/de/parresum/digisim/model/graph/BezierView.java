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
import java.awt.geom.CubicCurve2D;

import de.parresum.kicad.parser.eescheme.shape.Bezier;
import de.parresum.kicad.parser.model.PointList;
import de.parresum.kicad.parser.model.Position;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class BezierView extends AbstractView {
   public BezierView(Bezier bezier) {
      super(bezier, createShape(bezier));
   }

   public static Shape createShape(Bezier bezier) {
      PointList points = bezier.getPoints();

      Position start = points.getPoints().get(0);
      Position ctl1 = points.getPoints().get(1);
      Position ctl2 = points.getPoints().get(2);
      Position end = points.getPoints().get(3);

      return new CubicCurve2D.Double(start.getX() * BASE_SCALE, start.getY() * -BASE_SCALE, //
            ctl1.getX() * BASE_SCALE, ctl1.getY() * -BASE_SCALE, //
            ctl2.getX() * BASE_SCALE, ctl2.getY() * -BASE_SCALE, //
            end.getX() * BASE_SCALE, end.getY() * -BASE_SCALE);

   }

}
