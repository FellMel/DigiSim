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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.model.NetConnection;
import de.parresum.digisim.model.NetPoint;
import de.parresum.digisim.model.PinType;
import de.parresum.kicad.parser.model.Justify;

/**
 * Base for Connection views
 *
 * @author Kai Uwe Bachmann
 */
public abstract class ConnectionView {
   /** name of the connection */
   private final String name;

   /** Type of the connection */
   private final NetConnection connection;

   private Justify just;

   private Wire wire;

   public ConnectionView(NetConnection con) {
      name = con.getName();
      this.connection = con;
      just = con.getJust();
   }

   public Wire getWire() {
      return wire;
   }

   public void setWire(Wire wire) {
      this.wire = wire;
   }

   /**
    * @return the connection
    */
   public NetConnection getConnection() {
      return connection;
   }

   /**
    * @param wire
    */
   public void joinWire(Wire wire) {
      switch (getType()) {
         case INPUT:
            this.wire.join(wire);
            break;

         case OUTPUT:
            wire.join(this.wire);
            break;

         case OPEN_COLLECTOR:
         case TRI_STATE:
            // TODO: join special pin
         default:
      }

   }

   public PinType getType() {
      return connection.getType();
   }

   public String getName() {
      return name;
   }

   public abstract void paint(Graphics2D g2d);

   public abstract Rectangle2D getBounding();

   protected void setColor(Graphics2D g2d) {
      Color col;
      switch (getWire().get()) {
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
   }

   protected Dimension2D paintText(Graphics2D g2d, double dx, double dy) {
      java.awt.Font oldfont = g2d.getFont();
      g2d.setFont(getConnection().getFont());

      // calculate TextSize
      int width = g2d.getFontMetrics().stringWidth(getName());
      int height = g2d.getFontMetrics().getHeight();
      int ascent = g2d.getFontMetrics().getAscent();
      int descent = g2d.getFontMetrics().getDescent();
      double w = width + 2.0 * dx;
      double heightHalf = (height + dy) / 2.0;

      AffineTransform tmpTransform = g2d.getTransform();
      g2d.rotate(Math.toRadians(getConnection().getAngle() % 180.0));

      Point2D textPos = rotate(width, height, dx, dy, getConnection().getAngle());
      // Draw text
      // TODO: Hoch-/Tief-stellen, andere sonderstyles in Helper erledigen
      g2d.drawString(getConnection().getName(), (float) (textPos.getX()), (float) (textPos.getY() + height / 2.0 - dy));
      g2d.setTransform(tmpTransform);
      g2d.setFont(oldfont);

      return new DimensionDouble(w, height);
   }

   private Point2D rotate(int width, int height, double dx, double dy, double angle) {

      switch ((int) angle % 360) {
         case 0:
         case 270:
         default:
            return new Point2D.Double(0 + dx, 0);
         case 90:
         case 180:
            return new Point2D.Double(-width - dx, 0);
      }
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
            xmin = bound.getMinY();
            ymin = -bound.getMinX();
            xmax = bound.getMaxY();
            ymax = -bound.getMaxX();
            break;

         case 180:// cos = -1 ; sin = 0
            xmin = -bound.getMinX();
            ymin = -bound.getMinY();
            xmax = -bound.getMaxX();
            ymax = -bound.getMaxY();
            break;

         case 270:// cos = 0 ; sin = 1
            xmin = -bound.getMinY();
            ymin = bound.getMinX();
            xmax = -bound.getMaxY();
            ymax = bound.getMaxX();
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
