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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import de.parresum.digisim.model.NetPoint;
import de.parresum.kicad.parser.library.GraphPin;
import de.parresum.kicad.parser.library.PinShapeType;

/**
 * A graphical pin element
 *
 * @author Kai Uwe Bachmann
 */
public class PinView extends AbstractView {
   /** diameter of the inverter circle */
   private final static double DIAMETER = 0.8 * UNIT_FACTOR;

   /** the size of the clock triangle */
   private final static double CLOCK_SIZE = 0.8 * UNIT_FACTOR;

   public PinView(GraphPin pin) {
      super(null, createShape(pin));
   }

   /**
    * Creates the shape of the pin
    *
    * @param pin the pin type
    * @return the shape
    */
   private static Shape createShape(GraphPin pin) {
      PinShapeType pinShape = pin.getGraphicPinShape();
      double length = pin.getLength() * UNIT_FACTOR;
      NetPoint pinPos = new NetPoint(pin.getPosition().getX() * UNIT_FACTOR, pin.getPosition().getY() * UNIT_FACTOR);
      int angle = (int) pin.getPosition().getAngle();

      NetPoint pt = new NetPoint(length, 0);
      pt = pt.rotate(angle);
      pt = pt.add(pinPos.getX(), pinPos.getY());
      // pinPos = pinPos.rotate(angle);

      Path2D path = new Path2D.Double();
      path.moveTo(pinPos.getX(), pinPos.getY());

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

   /**
    * creates a clock pin
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createClock(Path2D path, NetPoint to, int angle) {
      createLine(path, to, angle);
      createClockPart(path, to, angle);
   }

   /**
    * creates a clock pin with edge
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createClockLow(Path2D path, NetPoint to, int angle) {
      createClock(path, to, angle);
      createEdge(path, to, angle);
   }

   /**
    * creates a clock pin with edge
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createEdgeClockHigh(Path2D path, NetPoint to, int angle) {
      createClock(path, to, angle);
      createEdge(path, to, angle);
   }

   /**
    * creates an input pin with edge
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createInputLow(Path2D path, NetPoint to, int angle) {
      createLine(path, to, angle);
      createEdge(path, to, angle);
   }

   /**
    * creates an inverted pin
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createInverted(Path2D path, NetPoint to, int angle) {
      switch (angle) {
         case 0:
            path.lineTo((to.getX() - DIAMETER), -to.getY());
            path.append(new Ellipse2D.Double((to.getX() - DIAMETER), (to.getY() - DIAMETER / 2.0), DIAMETER, DIAMETER),
                  false);
            break;
         case 90:
            path.lineTo(to.getX(), -(to.getY() - DIAMETER));
            path.append(new Ellipse2D.Double((to.getX() - DIAMETER / 2.0), to.getY(), DIAMETER, DIAMETER), false);
            break;
         case 180: // checked
            path.lineTo((to.getX() + DIAMETER), -to.getY());
            path.append(new Ellipse2D.Double(to.getX(), (to.getY() - DIAMETER / 2.0), DIAMETER, DIAMETER), false);
            break;
         case 270:
            path.lineTo(to.getX(), -(to.getY() - DIAMETER));
            path.append(new Ellipse2D.Double((to.getX() - DIAMETER / 2.0), to.getY(), DIAMETER, DIAMETER), false);
            break;
      }
   }

   /**
    * creates an inverted clock pin
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createInvertedClock(Path2D path, NetPoint to, int angle) {
      createInverted(path, to, angle);
      createClockPart(path, to, angle);
   }

   /**
    * creates a normal pin
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createLine(Path2D path, NetPoint to, int angle) {
      path.lineTo(to.getX(), to.getY());
   }

   /**
    * creates a non logical pin
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createNonLogic(Path2D path, NetPoint to, int angle) {
      path.lineTo(to.getX(), to.getY());
   }

   /**
    * creates an output pin with edge
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createOutputLow(Path2D path, NetPoint to, int angle) {
      createLine(path, to, angle);
      switch (angle) {
         case 0:
            path.moveTo(to.getX(), to.getY());
            path.lineTo(to.getX(), (to.getY() - CLOCK_SIZE));
            path.lineTo((to.getX() - CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), -to.getY());
            break;
         case 90:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() - CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), -(to.getY() + CLOCK_SIZE));
            path.lineTo(to.getX(), to.getY());
            break;
         case 180:
            path.moveTo(to.getX(), to.getY());
            path.lineTo(to.getX(), (to.getY() + CLOCK_SIZE));
            path.lineTo((to.getX() + CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), to.getY());
            break;
         case 270:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() + CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), (to.getY() - CLOCK_SIZE));
            path.lineTo(to.getX(), to.getY());
            break;
      }
   }

   /**
    * creates the clock part (triangle) of a pin
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createClockPart(Path2D path, NetPoint to, int angle) {
      switch (angle) {
         case 0:
            path.moveTo(to.getX(), to.getY());
            path.lineTo(to.getX(), (to.getY() - CLOCK_SIZE));
            path.lineTo((to.getX() + CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), (to.getY() + CLOCK_SIZE));
            path.lineTo(to.getX(), to.getY());
            break;
         case 90:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() - CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), (to.getY() + CLOCK_SIZE));
            path.lineTo((to.getX() + CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), to.getY());
            break;
         case 180:
            path.moveTo(to.getX(), to.getY());
            path.lineTo(to.getX(), (to.getY() - CLOCK_SIZE));
            path.lineTo((to.getX() - CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), (to.getY() + CLOCK_SIZE));
            path.lineTo(to.getX(), to.getY());
            break;
         case 270:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() - CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), (to.getY() - CLOCK_SIZE));
            path.lineTo((to.getX() + CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), to.getY());
            break;
      }
   }

   /**
    * creates the edge part of a pin
    *
    * @param path
    * @param to
    * @param angle
    */
   private static void createEdge(Path2D path, NetPoint to, int angle) {
      switch (angle) {
         case 0:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() - CLOCK_SIZE), (to.getY() + CLOCK_SIZE));
            path.lineTo((to.getX() - CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), to.getY());
            break;
         case 90:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() - CLOCK_SIZE), (to.getY() - CLOCK_SIZE));
            path.lineTo(to.getX(), (to.getY() + CLOCK_SIZE));
            path.lineTo(to.getX(), to.getY());
            break;
         case 180:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() + CLOCK_SIZE), (to.getY() + CLOCK_SIZE));
            path.lineTo((to.getX() + CLOCK_SIZE), to.getY());
            path.lineTo(to.getX(), to.getY());
            break;
         case 270:
            path.moveTo(to.getX(), to.getY());
            path.lineTo((to.getX() + CLOCK_SIZE), (to.getY() + CLOCK_SIZE));
            path.lineTo(to.getX(), (to.getY() - CLOCK_SIZE));
            path.lineTo(to.getX(), to.getY());
            break;
      }
   }

}
