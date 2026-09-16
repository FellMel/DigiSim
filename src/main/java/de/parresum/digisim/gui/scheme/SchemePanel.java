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
   private Circuit circuit;

   private double zoom = 1.0;

   public SchemePanel(Circuit circuit) {
      super();
      this.circuit = circuit;

      for (NetView wire : circuit.getViews()) {
         wire.getWire().addStateListener((_, _, _) -> repaint());
      }
   }

   @Override
   protected void paintComponent(Graphics g) {

      Graphics2D g2d = (Graphics2D) g;
      // g2d.scale(3.0, 3.0);
      // paint background
      g.setColor(Color.WHITE);
      Dimension size = this.getSize();
      g.fillRect(0, 0, size.width, size.height);

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

}
