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
import java.awt.geom.Rectangle2D;

import de.parresum.digisim.model.graph.AbstractView;
import de.parresum.digisim.model.graph.ArcView;
import de.parresum.digisim.model.graph.BezierView;
import de.parresum.digisim.model.graph.CircleView;
import de.parresum.digisim.model.graph.PinTextView;
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
import de.parresum.kicad.parser.library.PinNames;
import de.parresum.kicad.parser.library.PinNumber;

/**
 * Extracted symbol definition from library
 *
 * @author Kai Uwe Bachmann
 */
public class LibPart extends AbstractCircuitPart {

   public LibPart(LibSymbol symbol) {
      super(symbol.getName());

      if (symbol.getSymbols() != null) {
         for (GraphSymbol sub : symbol.getSymbols()) {
            // TODO: Unit ...
            parsePins(sub);
            parseGraphics(symbol, sub);
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

   private void parseGraphics(LibSymbol libSymbol, GraphSymbol symbol) {
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

            PinNames names = libSymbol.getPinNames();
            if (names == null || !names.isHide()) {

//               if (pin.getEffects() != null && pin.getEffects().isHide()) {
//                  continue;
//               }
               Double offset = null;
               if (names != null) {
                  offset = names.getOffset();
               }

               boolean inside = true;
               if (offset == null) {
                  offset = 0.508;
               }
               if (offset == 0) {
                  inside = false;
               }
               graphic.add(new PinTextView(pin, inside, offset));
            }

            PinNumber numbers = libSymbol.getPinNumbers();
            if (numbers == null || !numbers.isHide()) {
               graphic.add(new PinTextView(pin));
            }
         }
      }
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public CircuitPin getPin(String number) {
      return pins.get(number);
   }

   @Override
   public void paint(Graphics2D g) {
      for (AbstractView item : graphic) {
         item.paint(g);
      }
   }

   @Override
   public Rectangle2D getBounding() {

      Rectangle2D bounding = new Rectangle2D.Double(0, 0, 0, 0);
      for (AbstractView item : graphic) {
         Rectangle2D bound = item.getBounding();
         bounding.add(bound);
      }
      return bounding;
   }

}
