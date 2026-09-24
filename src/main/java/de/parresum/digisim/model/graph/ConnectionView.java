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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.model.NetConnection;
import de.parresum.digisim.model.NetPoint;
import de.parresum.digisim.model.PinType;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class ConnectionView {
   private final static double BORDER = 0.4 * UNIT_FACTOR;
   private final static double DEPTH = 1.4 * UNIT_FACTOR;
   private final String name;
   private final NetConnection connection;

   private Wire wire;

   public ConnectionView(NetConnection con) {
      name = con.getName();
      this.connection = con;
   }

   public Wire getWire() {
      return wire;
   }

   public void setWire(Wire wire) {
      this.wire = wire;
   }

   public PinType getType() {
      return connection.getType();
   }

   public String getName() {
      return name;
   }

   public void paint(Graphics2D g2d) {

      Color col;
      switch (wire.get()) {
         case HIGH:
            col = Color.RED;
            break;
         case LOW:
            col = Color.GREEN;
            break;
         case OPEN:
            col = Color.BLUE;
            break;
         default:
            col = Color.BLACK;
            break;
      }
      g2d.setColor(col);

      AffineTransform oldTransform = g2d.getTransform();
      java.awt.Font oldfont = g2d.getFont();
      g2d.setFont(connection.getFont());

      // calculate TextSize
      int width = g2d.getFontMetrics().stringWidth(name);
      int height = g2d.getFontMetrics().getHeight();
      double w = width + 2.0 * DEPTH;
      double heightHalf = (height + BORDER) / 2.0;

      g2d.translate(connection.getPoint().getX(), connection.getPoint().getY());
      g2d.rotate(Math.toRadians(connection.getAngle()));

      // Draw text
      // TODO: Hoch-/Tief-stellen, andere sonderstyles in Helper erledigen
      // TODO: Rotate erst nach Text, Textpos in helper rotieren
      g2d.drawString(connection.getName(), (int) (DEPTH), (int) ((heightHalf - BORDER)));

      // Draw Box
      g2d.drawLine((int) ((0 + DEPTH)), (int) (-heightHalf), //
            (int) ((w - DEPTH)), (int) (-heightHalf));
      g2d.drawLine((int) ((0 + DEPTH)), (int) (heightHalf), //
            (int) ((w - DEPTH)), (int) (heightHalf));

      switch (connection.getType()) {
         case INPUT:
            paintInput(g2d, w, heightHalf);
            paintNoOutput(g2d, w, heightHalf);
            break;

         case OUTPUT:
            paintNoInput(g2d, w, heightHalf);
            paintOutput(g2d, w, heightHalf);
            break;

         case OPEN_COLLECTOR:
         case TRI_STATE:
            paintInput(g2d, w, heightHalf);
            paintOutput(g2d, w, heightHalf);
            break;

         case UNKNOWN:
         default:
            paintNoInput(g2d, w, heightHalf);
            paintNoOutput(g2d, w, heightHalf);
            break;
      }

      g2d.setFont(oldfont);
      g2d.setTransform(oldTransform);
   }

   private void paintInput(Graphics2D g2d, double w, double heightHalf) {
      g2d.drawLine(0, 0, (int) (DEPTH), (int) (heightHalf));
      g2d.drawLine(0, 0, (int) (DEPTH), (int) (-heightHalf));
   }

   private void paintOutput(Graphics2D g2d, double w, double heightHalf) {
      g2d.drawLine((int) (w), 0, (int) ((w - DEPTH)), (int) (heightHalf));
      g2d.drawLine((int) (w), 0, (int) ((w - DEPTH)), (int) (-heightHalf));

   }

   private void paintNoInput(Graphics2D g2d, double w, double heightHalf) {
      g2d.drawLine(0, (int) (heightHalf), 0, (int) (-heightHalf));
      g2d.drawLine(0, (int) (heightHalf), (int) (DEPTH), (int) (heightHalf));
      g2d.drawLine(0, (int) (-heightHalf), (int) (DEPTH), (int) (-heightHalf));

   }

   private void paintNoOutput(Graphics2D g2d, double w, double heightHalf) {
      g2d.drawLine((int) (w), (int) (heightHalf), (int) (w), (int) (-heightHalf));
      g2d.drawLine((int) (w), (int) (heightHalf), (int) ((w - DEPTH)), (int) (heightHalf));
      g2d.drawLine((int) (w), (int) (-heightHalf), (int) ((w - DEPTH)), (int) (-heightHalf));

   }

   public Rectangle2D getBounding() {

      // calculate TextSize
      Rectangle2D fontBound = connection.getFont().getStringBounds(name,
            new FontRenderContext(new AffineTransform(), false, false));
      double width = fontBound.getWidth();
      double height = fontBound.getHeight();
      double w = width + 2.0 * DEPTH;
      double heightHalf = (height + BORDER) / 2.0;

      Rectangle2D bounding = new Rectangle2D.Double(0, -heightHalf, 0, 0);
      bounding.add(w, -heightHalf);
      bounding.add(0, heightHalf);
      bounding.add(w, heightHalf);

      bounding = translate(bounding, connection.getPoint(), (int) connection.getAngle(), false, false);

      return bounding;
   }

   // TODO: mit AbstractCirclePart zusammen führen
   protected Rectangle2D translate(Rectangle2D bound, NetPoint position, int angle, boolean mirrorX, boolean mirrorY) {
      double xmin, ymin;
      double xmax, ymax;

      // rotate(Math.toRadians(-angle));
      // xnew = x * cos + y * sin
      // ynew = -x * sin + y * cos
      switch (angle) {
         case 0: // cos = 1 ; sin = 0
         default: // unknown angle is like 0 degree
            xmin = bound.getMinX();
            ymin = bound.getMinY();
            xmax = bound.getMaxX();
            ymax = bound.getMaxY();
            break;

         case 90:// cos = 0 ; sin = -1
            xmin = -bound.getMinY();
            ymin = bound.getMinX();
            xmax = -bound.getMaxY();
            ymax = bound.getMaxX();
            break;

         case 180:// cos = -1 ; sin = 0
            xmin = -bound.getMinX();
            ymin = -bound.getMinY();
            xmax = -bound.getMaxX();
            ymax = -bound.getMaxY();
            break;

         case 270:// cos = 0 ; sin = 1
            xmin = bound.getMinY();
            ymin = -bound.getMinX();
            xmax = bound.getMaxY();
            ymax = -bound.getMaxX();
            break;
      }

      if (mirrorX) {
         double tmp = ymin;
         ymin = ymax;
         ymax = tmp;
      } else if (mirrorY) {
         double tmp = xmin;
         xmin = xmax;
         xmax = tmp;
      }

      xmin += position.getX();
      ymin += position.getY();
      xmax += position.getX();
      ymax += position.getY();

      return new Rectangle2D.Double(//
            Math.min(xmin, xmax), //
            Math.min(ymin, ymax), //
            Math.abs(xmax - xmin), //
            Math.abs(ymax - ymin));
   }

}
