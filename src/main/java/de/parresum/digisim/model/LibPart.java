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

package de.parresum.digisim.model;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.parresum.digisim.model.graph.AbstractView;
import de.parresum.digisim.model.graph.ArcView;
import de.parresum.digisim.model.graph.BezierView;
import de.parresum.digisim.model.graph.CircleView;
import de.parresum.digisim.model.graph.PinView;
import de.parresum.digisim.model.graph.PolygonView;
import de.parresum.digisim.model.graph.RectangleView;
import de.parresum.digisim.model.graph.TextView;
import de.parresum.kicad.parser.eescheme.shape.Arc;
import de.parresum.kicad.parser.eescheme.shape.Bezier;
import de.parresum.kicad.parser.eescheme.shape.Circle;
import de.parresum.kicad.parser.eescheme.shape.Polyline;
import de.parresum.kicad.parser.eescheme.shape.Rectangle;
import de.parresum.kicad.parser.eescheme.shape.Text;
import de.parresum.kicad.parser.library.GraphPin;
import de.parresum.kicad.parser.library.GraphSymbol;
import de.parresum.kicad.parser.library.LibSymbol;

/**
 * Extracted symbol definition from lib
 *
 * @author Kai Uwe Bachmann
 */
public class LibPart {

   /** name of the symbol */
   private final String name;

   /** pins of the symbol */
   private final Map<String, LibPin> pins = new HashMap<>();

   private final List<AbstractView> graphic = new ArrayList<>();

   public LibPart(LibSymbol symbol) {
      name = symbol.getName();

      if (symbol.getSymbols() != null) {
         for (GraphSymbol sub : symbol.getSymbols()) {
            parsePins(sub);
            parseGraphics(sub);
         }
      }
   }

   private void parsePins(GraphSymbol symbol) {
      if (symbol.getPins() != null) {
         for (GraphPin item : symbol.getPins()) {
            LibPin libPin = new LibPin(item);
            pins.put(libPin.getNumber(), libPin);
         }
      }
   }

   private void parseGraphics(GraphSymbol symbol) {
      if (symbol.getPolylines() != null) {
         for (Polyline poly : symbol.getPolylines()) {
            graphic.add(new PolygonView(poly));
         }
      }

      if (symbol.getRectangles() != null) {
         for (Rectangle rect : symbol.getRectangles()) {
            graphic.add(new RectangleView(rect));
         }
      }

      if (symbol.getCircles() != null) {
         for (Circle circle : symbol.getCircles()) {
            graphic.add(new CircleView(circle));
         }
      }

      if (symbol.getArcs() != null) {
         for (Arc arc : symbol.getArcs()) {
            graphic.add(new ArcView(arc));
         }
      }

      if (symbol.getBeziers() != null) {
         for (Bezier bezier : symbol.getBeziers()) {
            graphic.add(new BezierView(bezier));
         }
      }

      if (symbol.getTexts() != null) {
         for (Text text : symbol.getTexts()) {
            graphic.add(new TextView(text));
         }
      }

      if (symbol.getPins() != null) {
         for (GraphPin pin : symbol.getPins()) {
            graphic.add(new PinView(pin));
         }
      }
   }

   public String getName() {
      return name;
   }

   public LibPin getPin(String number) {
      return pins.get(number);
   }

   public void paint(Graphics2D g, NetPoint position, int angle, boolean mirrorX, boolean mirrorY) {
      AffineTransform transform = transform(g, position, angle, mirrorX, mirrorY);
      for (AbstractView item : graphic) {
         item.paint(g);
      }
      g.setTransform(transform);
   }

   protected AffineTransform transform(Graphics2D g2d, NetPoint position, int angle, boolean mirrorX, boolean mirrorY) {
      AffineTransform oldTransform = g2d.getTransform();
      // must be in inverse order
      g2d.translate(position.getX() * AbstractView.BASE_SCALE, position.getY() * AbstractView.BASE_SCALE);
      g2d.rotate(Math.toRadians(-angle));
      // TODO: handle mirror
      // if (mirrorX) {
//    p = p.mirror(true);
// } else if (mirrorY) {
//    p = p.mirror(false);
// }
//
// return position.add(p);

      return oldTransform;
   }

}
