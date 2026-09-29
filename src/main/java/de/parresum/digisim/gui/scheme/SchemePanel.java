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

package de.parresum.digisim.gui.scheme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import de.parresum.digisim.core.Circuit;
import de.parresum.digisim.model.graph.ConnectionView;
import de.parresum.digisim.model.graph.NetView;
import de.parresum.digisim.model.graph.PartView;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class SchemePanel extends JPanel {
   private final static int BORDER = 20;
   private final static double[] ZOOM_FACTORS = { 0.1, 0.2, 0.5, 1, 2, 5, 10, 20 };
   private Circuit circuit;

   private int zoom = 2;

   private Rectangle2D bounding;
   private Dimension size;

   public SchemePanel(Circuit circuit) {
      super();
      this.circuit = circuit;

      for (NetView wire : circuit.getViews()) {
         wire.getWire().addStateListener((_, _, _) -> repaint());
      }
      init();

      setZoom();
   }

   private void init() {

      for (PartView part : circuit.getParts()) {
         extendBounding(part.getNetPart().getBounding());
      }
      for (NetView view : circuit.getViews()) {
         extendBounding(view.getBounding());
      }
      for (ConnectionView con : circuit.getConnections()) {
         extendBounding(con.getBounding());
      }

      bounding = new Rectangle2D.Double(bounding.getX() - BORDER, bounding.getY() - BORDER,
            bounding.getWidth() + BORDER + BORDER, bounding.getHeight() + BORDER + BORDER);
   }

   private void extendBounding(Rectangle2D bound) {
      if (bounding == null) {
         bounding = bound;
      } else {
         bounding.add(bound);
      }
   }

   @Override
   protected void paintComponent(Graphics g) {

      Graphics2D g2d = (Graphics2D) g;
      // paint background
      g.setColor(Color.WHITE);
      Dimension size = this.getSize();
      g.fillRect(0, 0, size.width, size.height);

      g2d.scale(ZOOM_FACTORS[zoom], ZOOM_FACTORS[zoom]);
      g2d.translate(-bounding.getMinX(), -(bounding.getMinY() /* + bounding.getHeight() */));

      // paint elements
      paintParts(g2d);
      paintWires(g2d);
      paintConnectors(g2d);
   }

   private void paintParts(Graphics2D g) {
      for (PartView part : circuit.getParts()) {
         part.getNetPart().paint(g);
      }
   }

   private void paintWires(Graphics2D g) {
      for (NetView view : circuit.getViews()) {
         view.paint(g);
      }
   }

   private void paintConnectors(Graphics2D g) {
      for (ConnectionView con : circuit.getConnections()) {
         con.paint(g);
      }
   }

   public void zoomIn() {
      zoom++;
      if (zoom >= ZOOM_FACTORS.length) {
         zoom = ZOOM_FACTORS.length - 1;
      }
      setZoom();
   }

   public void zoomOut() {
      zoom--;
      if (zoom < 0) {
         zoom = 0;
      }
      setZoom();
   }

   public void zoomFit() {
      // TODO: implement
   }

   public boolean canZoomIn() {
      return zoom < ZOOM_FACTORS.length - 1;
   }

   public boolean canZoomOut() {
      return zoom > 0;
   }

   private void setZoom() {
      setPreferredSize(new Dimension((int) (bounding.getWidth() * ZOOM_FACTORS[zoom]),
            (int) (bounding.getHeight() * ZOOM_FACTORS[zoom])));
      setSize(new Dimension((int) (bounding.getWidth() * ZOOM_FACTORS[zoom]),
            (int) (bounding.getHeight() * ZOOM_FACTORS[zoom])));

   }
}
