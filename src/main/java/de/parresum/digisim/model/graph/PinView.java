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
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import de.parresum.digisim.model.NetPoint;
import de.parresum.kicad.parser.library.GraphPin;
import de.parresum.kicad.parser.library.PinShapeType;
import de.parresum.kicad.parser.model.PositionAt;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class PinView extends AbstractView {
   private final static double DIAMETER = 0.7;
   private final static double CLOCK_SIZE = 0.2;

   public PinView(GraphPin pin) {
      super(null, createShape(pin));
   }

   private static Shape createShape2(GraphPin pin) {
      PositionAt pos = pin.getPosition();
      return new Rectangle2D.Double(pos.getX() * BASE_SCALE, pos.getY() * -BASE_SCALE, 5, 5);
   }

   private static Shape createShape(GraphPin pin) {
      PinShapeType pinShape = pin.getGraphicPinShape();
      double length = pin.getLength();
      NetPoint pinPos = new NetPoint(pin.getPosition().getX(), pin.getPosition().getY());
      int angle = (int) pin.getPosition().getAngle();

      NetPoint pt = new NetPoint(length, 0);
      pt = pt.rotate(angle);
      pt = pt.add(pinPos.getX(), pinPos.getY());
      // pinPos = pinPos.rotate(angle);

      Path2D path = new Path2D.Double();
      path.moveTo(pinPos.getX() * BASE_SCALE, pinPos.getY() * -BASE_SCALE);

      switch (pinShape) {
         case CLOCK:
            createClock(path, pt, angle);
            break;
         case CLOCK_LOW:
            createClockLow(path, pt, angle);
            break;
         case EDGE_CLOCK_HIGH:
            createEdgeClockHigh(path, pt, angle);
            break;
         case INPUT_LOW:
            createInputLow(path, pt, angle);
            break;
         case INVERTED:
            createInverted(path, pt, angle);
            break;
         case INVERTED_CLOCK:
            createInvertedClock(path, pt, angle);
            break;
         case LINE:
            createLine(path, pt, angle);
            break;
         case NON_LOGIC:
            createNonLogic(path, pt, angle);
            break;
         case OUTPUT_LOW:
            createOutputLow(path, pt, angle);
            break;
         default:
            createLine(path, pt, angle);
            break;
      }

      return path;
   }

   private static void createClock(Path2D path, NetPoint to, int angle) {
      createLine(path, to, angle);
      createClockPart(path, to, angle);
   }

   private static void createClockLow(Path2D path, NetPoint to, int angle) {
      createClock(path, to, angle);
      createEdge(path, to, angle);
   }

   private static void createEdgeClockHigh(Path2D path, NetPoint to, int angle) {
      createClock(path, to, angle);
      createEdge(path, to, angle);
   }

   private static void createInputLow(Path2D path, NetPoint to, int angle) {
      createLine(path, to, angle);
      createEdge(path, to, angle);
   }

   private static void createInverted(Path2D path, NetPoint to, int angle) {
      switch (angle) {
         case 0:
            path.lineTo((to.getX() - DIAMETER) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.append(new Ellipse2D.Double(to.getX() * BASE_SCALE, (to.getY() + DIAMETER / 2.0) * -BASE_SCALE,
                  DIAMETER * BASE_SCALE, DIAMETER * BASE_SCALE), false);
            break;
         case 90:
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - DIAMETER) * -BASE_SCALE);
            path.append(new Ellipse2D.Double((to.getX() - DIAMETER / 2.0) * BASE_SCALE, to.getY() * -BASE_SCALE,
                  DIAMETER * BASE_SCALE, DIAMETER * BASE_SCALE), false);
            break;
         case 180: // checked
            path.lineTo((to.getX() + DIAMETER) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.append(new Ellipse2D.Double(to.getX() * BASE_SCALE, (to.getY() + DIAMETER / 2.0) * -BASE_SCALE,
                  DIAMETER * BASE_SCALE, DIAMETER * BASE_SCALE), false);
            break;
         case 270:
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - DIAMETER) * -BASE_SCALE);
            path.append(new Ellipse2D.Double((to.getX() - DIAMETER / 2.0) * BASE_SCALE, to.getY() * -BASE_SCALE,
                  DIAMETER * BASE_SCALE, DIAMETER * BASE_SCALE), false);
            break;
      }
   }

   private static void createInvertedClock(Path2D path, NetPoint to, int angle) {
      createInverted(path, to, angle);
      createClockPart(path, to, angle);
   }

   private static void createLine(Path2D path, NetPoint to, int angle) {
      path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
   }

   private static void createNonLogic(Path2D path, NetPoint to, int angle) {
      path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
   }

   private static void createOutputLow(Path2D path, NetPoint to, int angle) {
      createLine(path, to, angle);
      switch (angle) {
         case 0:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 90:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 180:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 270:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() + CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
      }
   }

   private static void createClockPart(Path2D path, NetPoint to, int angle) {
      switch (angle) {
         case 0:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() + CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 90:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() + CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 180:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 270:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() + CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
      }
   }

   private static void createEdge(Path2D path, NetPoint to, int angle) {
      switch (angle) {
         case 0:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 90:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 180:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, (to.getY() + CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo((to.getX() - CLOCK_SIZE) * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
         case 270:
            path.moveTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            path.lineTo((to.getX() + CLOCK_SIZE) * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, (to.getY() - CLOCK_SIZE) * -BASE_SCALE);
            path.lineTo(to.getX() * BASE_SCALE, to.getY() * -BASE_SCALE);
            break;
      }
   }
//   @Override
//   protected void paintOutline(Graphics2D g2d) {
//      // TODO: Text-Effects
//
//      g2d.drawString(str, (float) pos.getX(), (float) pos.getY());
//
//   }
//
//   @Override
//   protected void paintBackground(Graphics2D g2d) {
//      // nothing to do, yet
//   }

}
