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

import static de.parresum.digisim.model.graph.AbstractView.BASE_SCALE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.model.NetConnection;
import de.parresum.digisim.model.PinType;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class ConnectionView {
   private final static double BORDER = 0.4;
   private final static double DEPTH = 1.4;
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
      double w = width / BASE_SCALE + 2.0 * DEPTH;
      double heightHalf = (height / BASE_SCALE + BORDER) / 2.0;

      g2d.translate(connection.getPoint().getX() * BASE_SCALE, connection.getPoint().getY() * BASE_SCALE);
      g2d.rotate(Math.toRadians(connection.getAngle()));

      // Draw text
      // TODO: Hoch-/Tief-stellen, andere sonderstyles in Helper erledigen
      // TODO: Rotate erst nach Text, Textpos in helper rotieren
      g2d.drawString(connection.getName(), (int) (DEPTH * BASE_SCALE), (int) ((heightHalf - BORDER) * BASE_SCALE));

      // Draw Box
      g2d.drawLine((int) ((0 + DEPTH) * BASE_SCALE), (int) (-heightHalf * BASE_SCALE), //
            (int) ((w - DEPTH) * BASE_SCALE), (int) (-heightHalf * BASE_SCALE));
      g2d.drawLine((int) ((0 + DEPTH) * BASE_SCALE), (int) (heightHalf * BASE_SCALE), //
            (int) ((w - DEPTH) * BASE_SCALE), (int) (heightHalf * BASE_SCALE));

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
      g2d.drawLine(0, 0, (int) (DEPTH * BASE_SCALE), (int) (heightHalf * BASE_SCALE));
      g2d.drawLine(0, 0, (int) (DEPTH * BASE_SCALE), (int) (-heightHalf * BASE_SCALE));
   }

   private void paintOutput(Graphics2D g2d, double w, double heightHalf) {
      g2d.drawLine((int) (w * BASE_SCALE), 0, (int) ((w - DEPTH) * BASE_SCALE), (int) (heightHalf * BASE_SCALE));
      g2d.drawLine((int) (w * BASE_SCALE), 0, (int) ((w - DEPTH) * BASE_SCALE), (int) (-heightHalf * BASE_SCALE));

   }

   private void paintNoInput(Graphics2D g2d, double w, double heightHalf) {
      g2d.drawLine(0, (int) (heightHalf * BASE_SCALE), 0, (int) (-heightHalf * BASE_SCALE));
      g2d.drawLine(0, (int) (heightHalf * BASE_SCALE), (int) (DEPTH * BASE_SCALE), (int) (heightHalf * BASE_SCALE));
      g2d.drawLine(0, (int) (-heightHalf * BASE_SCALE), (int) (DEPTH * BASE_SCALE), (int) (-heightHalf * BASE_SCALE));

   }

   private void paintNoOutput(Graphics2D g2d, double w, double heightHalf) {
      g2d.drawLine((int) (w * BASE_SCALE), (int) (heightHalf * BASE_SCALE), (int) (w * BASE_SCALE),
            (int) (-heightHalf * BASE_SCALE));
      g2d.drawLine((int) (w * BASE_SCALE), (int) (heightHalf * BASE_SCALE), (int) ((w - DEPTH) * BASE_SCALE),
            (int) (heightHalf * BASE_SCALE));
      g2d.drawLine((int) (w * BASE_SCALE), (int) (-heightHalf * BASE_SCALE), (int) ((w - DEPTH) * BASE_SCALE),
            (int) (-heightHalf * BASE_SCALE));

   }

}
