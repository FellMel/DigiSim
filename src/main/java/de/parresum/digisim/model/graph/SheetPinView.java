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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import de.parresum.digisim.model.NetPoint;
import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.library.PinType;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class SheetPinView extends AbstractView {
   /** diameter of the inverter circle */
   private final static double WIDTH = 1.27 * UNIT_FACTOR;

   /** the size of the clock triangle */
   private final static double HEIGHT = 1.27 * UNIT_FACTOR;
   private NetPoint pinPos;
   private int angle;

   public SheetPinView(Pin pin) {
      super(null, createShape(pin));
      pinPos = new NetPoint(pin.getPosition());
      angle = (int) pin.getPosition().getAngle();

   }

   /**
    * Creates the shape of the pin
    *
    * @param pin the pin type
    * @return the shape
    */
   private static Shape createShape(Pin pin) {
      PinType pinType = pin.getElectricalPinType();

      Path2D path = new Path2D.Double();
      path.moveTo(0, 0);

      switch (pinType) {
         case INPUT:
            createInput(path);
            break;
         case OUTPUT:
            createOutput(path);
            break;

         case BIDIRECTIONAL:
         case TRI_STATE:
            createBiDirectional(path);
            break;

         case PASSIVE:
         default:
            createPassive(path);
            break;

      }

      return path;
   }

   /**
    * @param path
    */
   private static void createInput(Path2D path) {
      path.lineTo(0, HEIGHT / 2.0);
      path.lineTo(-WIDTH / 2.0, HEIGHT / 2.0);
      path.lineTo(-WIDTH, 0);
      path.lineTo(-WIDTH / 2.0, -HEIGHT / 2.0);
      path.lineTo(0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   /**
    * @param path
    */
   private static void createOutput(Path2D path) {
      path.lineTo(-WIDTH / 2.0, HEIGHT / 2.0);
      path.lineTo(-WIDTH, HEIGHT / 2.0);
      path.lineTo(-WIDTH, -HEIGHT / 2.0);
      path.lineTo(-WIDTH / 2.0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   /**
    * @param path
    */
   private static void createBiDirectional(Path2D path) {
      path.lineTo(-WIDTH / 2.0, HEIGHT / 2.0);
      path.lineTo(-WIDTH, 0);
      path.lineTo(-WIDTH / 2.0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   /**
    * @param path
    */
   private static void createPassive(Path2D path) {
      path.lineTo(0, HEIGHT / 2.0);
      path.lineTo(-WIDTH, HEIGHT / 2.0);
      path.lineTo(-WIDTH, -HEIGHT / 2.0);
      path.lineTo(0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   @Override
   public void paint(Graphics2D g2d) {
      AffineTransform oldTransform = g2d.getTransform();
      g2d.translate(pinPos.getX(), pinPos.getY());
      g2d.rotate(Math.toRadians(-angle));
      super.paint(g2d);

      g2d.setTransform(oldTransform);
   }

}
