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
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import de.parresum.digisim.model.NetConnection;
import de.parresum.digisim.model.PinType;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class HierarchicalConnectionView extends ConnectionView {
   /** diameter of the inverter circle */
   private final static double WIDTH = 1.27 * UNIT_FACTOR;

   /** the size of the clock triangle */
   private final static double HEIGHT = 1.27 * UNIT_FACTOR;

   private Shape shape;

   public HierarchicalConnectionView(NetConnection con) {
      super(con);
      shape = createShape(con.getType());
   }

   /**
    * Creates the shape of the pin
    *
    * @param pin the pin type
    * @return the shape
    */
   private Shape createShape(PinType pinType) {
      Path2D path = new Path2D.Double();
      path.moveTo(0, 0);

      switch (pinType) {
         case INPUT:
            createInput(path);
            break;
         case OUTPUT:
            createOutput(path);
            break;

         case OPEN_COLLECTOR:
         case TRI_STATE:
            createBiDirectional(path);
            break;

         case UNKNOWN:
         default:
            createPassive(path);
            break;

      }

      return path;
   }

   /**
    * @param path
    */
   private void createInput(Path2D path) {
      path.lineTo(WIDTH / 2.0, HEIGHT / 2.0);
      path.lineTo(WIDTH, HEIGHT / 2.0);
      path.lineTo(WIDTH, -HEIGHT / 2.0);
      path.lineTo(WIDTH / 2.0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   /**
    * @param path
    */
   private void createOutput(Path2D path) {
      path.lineTo(0, HEIGHT / 2.0);
      path.lineTo(WIDTH / 2.0, HEIGHT / 2.0);
      path.lineTo(WIDTH, 0);
      path.lineTo(WIDTH / 2.0, -HEIGHT / 2.0);
      path.lineTo(0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   /**
    * @param path
    */
   private void createBiDirectional(Path2D path) {
      path.lineTo(WIDTH / 2.0, HEIGHT / 2.0);
      path.lineTo(WIDTH, 0);
      path.lineTo(WIDTH / 2.0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   /**
    * @param path
    */
   private void createPassive(Path2D path) {
      path.lineTo(0, HEIGHT / 2.0);
      path.lineTo(WIDTH, HEIGHT / 2.0);
      path.lineTo(WIDTH, -HEIGHT / 2.0);
      path.lineTo(0, -HEIGHT / 2.0);
      path.lineTo(0, 0);
   }

   @Override
   public void paint(Graphics2D g2d) {
      setColor(g2d);

      AffineTransform oldTransform = g2d.getTransform();

      g2d.translate(getConnection().getPoint().getX(), getConnection().getPoint().getY());
      AffineTransform tmp = g2d.getTransform();

      g2d.rotate(Math.toRadians(getConnection().getAngle()));
      g2d.draw(shape);
      g2d.setTransform(tmp);

      Dimension2D textSize = paintText(g2d, WIDTH, HEIGHT / 2.0);

      g2d.setTransform(oldTransform);

   }

   @Override
   public Rectangle2D getBounding() {
      // calculate TextSize
      Rectangle2D fontBound = getConnection().getFont().getStringBounds(getName(),
            new FontRenderContext(new AffineTransform(), false, false));
      double width = fontBound.getWidth();
      double height = fontBound.getHeight();

      double w = width + 2.0 * WIDTH;
      double heightHalf = Math.max(height, HEIGHT) / 2.0;

      Rectangle2D bounding = new Rectangle2D.Double(0, -heightHalf, 0, 0);
      bounding.add(w, -heightHalf);
      bounding.add(0, heightHalf);
      bounding.add(w, heightHalf);

      bounding = translate(bounding, getConnection().getPoint(), (int) getConnection().getAngle(), false, false);

      return bounding;
   }

}
