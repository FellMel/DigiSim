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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.parresum.digisim.model.graph.AbstractView;
import de.parresum.digisim.model.graph.TextView;
import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.eescheme.Sheet;
import de.parresum.kicad.parser.eescheme.Symbol;
import de.parresum.kicad.parser.model.MirrorType;
import de.parresum.kicad.parser.model.PositionAt;
import de.parresum.kicad.parser.model.Property;

/**
 * Part extracted from scheme
 *
 * @author Kai Uwe Bachmann
 */
public class NetPart extends AbstractCircuitPart {
   /** the uuid of the part */
   private final String uuid;

   /** the mid point of the part */
   private final NetPoint point;

   /** angle of the part in scheme */
   private final int angle;

   /** name of the symbol in library */
   private final String lib;

   /** extracted symbol definition in lib */
   private final AbstractCircuitPart circuitPart;

//   /** pins of the part */
//   private final List<NetPin> pins = new ArrayList<NetPin>();
//
   /** values of the part */
   private final List<NetValue> values = new ArrayList<>();

   private final List<TextView> properties = new ArrayList<>();

   private final boolean mirrorX;
   private final boolean mirrorY;

   public NetPart(Symbol symbol, LibPart libPart) {
      super(getSymbolName(symbol));
      this.uuid = symbol.getUuid().getUuid();
      this.circuitPart = libPart;
      this.lib = symbol.getLibraryIdentifier();
      this.point = new NetPoint(symbol.getPosition());
      this.angle = (int) Math.round(symbol.getPosition().getAngle());
      if (symbol.getMirror() == null) {
         mirrorX = false;
         mirrorY = false;
      } else if (symbol.getMirror() == MirrorType.X) {
         mirrorX = true;
         mirrorY = false;
      } else {
         mirrorX = false;
         mirrorY = true;
      }

      parsePins(symbol);
      parseValues(symbol.getProperties());
   }

   public NetPart(Sheet sheet) {
      super(getSymbolName(sheet));
      this.uuid = sheet.getUuid().getUuid();
      this.circuitPart = new SheetPart(sheet);
      this.lib = null; // symbol.getLibraryIdentifier();
      this.point = new NetPoint(sheet.getAt());
      this.angle = (int) Math.round(sheet.getAt().getAngle());
      mirrorX = false;
      mirrorY = false;

      parsePins(sheet);
      parseValues(sheet.getProperties());
   }

   private static String getSymbolName(Symbol symbol) {
      for (Property prop : symbol.getProperties()) {
         if ("Reference".equalsIgnoreCase(prop.getKey())) {
            return prop.getValue();
         }
      }
      // fallback ...
      return symbol.getLibName();
   }

   private static String getSymbolName(Sheet sheet) {
      for (Property prop : sheet.getProperties()) {
         if ("Sheetname".equalsIgnoreCase(prop.getKey())) {
            return prop.getValue();
         }
      }
      // fallback ...
      return null;
   }

   private void parsePins(Symbol symbol) {
      for (Pin pin : symbol.getPins()) {
         NetPoint point = getPinPoint(symbol, pin);
         int angle = getPinAngle(symbol, pin);

         NetPin partPin = new NetPin(pin, name, point, angle);

         pins.put(partPin.getPinNr(), partPin);
      }
   }

   private void parsePins(Sheet sheet) {
      for (Pin pin : sheet.getPins()) {
         NetPoint point = new NetPoint(pin.getPosition());
         int angle = (int) pin.getPosition().getAngle();

         NetPin partPin = new NetPin(pin, name, point, angle);

         pins.put(partPin.getPinNr(), partPin);
      }
   }

   private void parseValues(List<Property> inProperties) {
      for (Property prop : inProperties) {
         if (prop.getValue() != null && !prop.getValue().isBlank()) {
            values.add(new NetValue(prop.getKey(), prop.getValue(), name));
            if (!prop.isHide()) {
               properties.add(new TextView(prop));
            }
         }
      }
   }

   private NetPoint getPinPoint(Symbol symbol, Pin pin) {
      PositionAt origin = symbol.getPosition();
      String pinName = pin.getName();
      CircuitPin libPin = circuitPart.getPin(pinName);
      if (libPin == null) {
         throw new IllegalStateException("Can't find pin entry for " + pinName);
      }

      NetPoint pinPos = libPin.getPosition();
      if (angle != 0) {
         pinPos = pinPos.rotate(angle);
      }
      if (mirrorX) {
         pinPos = pinPos.mirror(true);
      }
      if (mirrorY) {
         pinPos = pinPos.mirror(false);
      }

      // TODO: was ist mit gedrehten / gespiegelten Symbolen?
      // Y wird negativ gezählt ??? Warum ?
      NetPoint pt = new NetPoint(origin.getX() * UNIT_FACTOR + pinPos.getX() * UNIT_FACTOR,
            origin.getY() * UNIT_FACTOR - pinPos.getY() * UNIT_FACTOR);

      return pt;

   }

   private int getPinAngle(Symbol symbol, Pin pin) {
      String pinName = pin.getName();
      CircuitPin libPin = circuitPart.getPin(pinName);
      if (libPin == null) {
         throw new IllegalStateException("Can't find pin entry for " + pinName);
      }

      return (int) (libPin.getAngle() + symbol.getPosition().getAngle());

   }

   public Collection<CircuitPin> getPins() {
      return pins.values();
   }

   public List<NetValue> getValues() {
      return values;
   }

   public String getUuid() {
      return uuid;
   }

   public NetPoint getPoint() {
      return point;
   }

   public int getAngle() {
      return angle;
   }

   @Override
   public String getName() {
      return name;
   }

   public String getLib() {
      return lib;
   }

   @Override
   public void paint(Graphics2D g) {
      g.setColor(Color.BLACK);

      // Lib is mirrored on axis x
      AffineTransform oldTransform = transform(g, point, angle, !mirrorX, mirrorY);
      circuitPart.paint(g);
      g.setTransform(oldTransform);

      paintProps(g);
   }

   public void paintProps(Graphics2D g) {
      // TODO: rotate
      for (TextView item : properties) {
         item.paintOutline(g, angle);
      }

   }

   protected AffineTransform transform(Graphics2D g2d, NetPoint position, int angle, boolean mirrorX, boolean mirrorY) {
      AffineTransform oldTransform = g2d.getTransform();
      // must be in inverse order
      g2d.translate(position.getX(), position.getY());
      if (mirrorX) {
         g2d.scale(1, -1);
      } else if (mirrorY) {
         g2d.scale(-1, 1);
      }
      g2d.rotate(Math.toRadians(angle));

      return oldTransform;
   }

   @Override
   public Rectangle2D getBounding() {
      Rectangle2D bound = circuitPart.getBounding();

      bound = translate(bound, point, angle, mirrorX, mirrorY);

      for (AbstractView item : properties) {
         bound.add(item.getBounding());
      }

      return bound;
   }

   @Override
   public CircuitPin getPin(String number) {
      return pins.get(number);
   }
}
