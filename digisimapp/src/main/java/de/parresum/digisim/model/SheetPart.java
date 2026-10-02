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

import static de.parresum.digisim.model.ModelConstants.UNIT_FACTOR;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import de.parresum.digisim.model.graph.AbstractView;
import de.parresum.digisim.model.graph.RectangleView;
import de.parresum.digisim.model.graph.SheetPinView;
import de.parresum.digisim.model.graph.TextView;
import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.eescheme.Sheet;
import de.parresum.kicad.parser.model.Property;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class SheetPart extends AbstractCircuitPart {
   /** the uuid of the part */
   private final String uuid;

   private final String filename;

   public SheetPart(Sheet sheet) {
      super(getProperty(sheet, "Sheetname"));
      this.filename = getProperty(sheet, "Sheetfile");
      this.uuid = sheet.getUuid().getUuid();

      graphic.add(new RectangleView(sheet.getAt(), sheet.getSize(), sheet.getStroke(), sheet.getFill()));
      if (sheet.getPins() != null) {
         for (Pin item : sheet.getPins()) {
            LibPin libPin = new LibPin(item);
            NetPin pin = new NetPin(item, name);
            pins.put(libPin.getNumber(), pin);
            graphic.add(new TextView(item, 1.27 * UNIT_FACTOR, 0));
            graphic.add(new SheetPinView(item));
         }
      }
   }

   private static String getProperty(Sheet sheet, String propName) {
      for (Property prop : sheet.getProperties()) {
         if (propName.equalsIgnoreCase(prop.getKey())) {
            return prop.getValue();
         }
      }
      // fallback ...
      return null;
   }

   @Override
   public CircuitPin getPin(String number) {
      return pins.get(number);
   }

   public Collection<CircuitPin> getPins() {
      return pins.values();
   }

   /**
    * @return the filename
    */
   public String getFilename() {
      return filename;
   }

   @Override
   public void paint(Graphics2D g) {
      for (AbstractView item : graphic) {
         item.paint(g);
      }
   }

   @Override
   public Rectangle2D getBounding() {

      Rectangle2D bounding = null;
      for (AbstractView item : graphic) {
         Rectangle2D bound = item.getBounding();
         if (bounding == null) {
            bounding = bound;
         } else {
            bounding.add(bound);
         }
      }
      return bounding;
   }

}
