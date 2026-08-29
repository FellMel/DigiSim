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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.eescheme.Symbol;
import de.parresum.kicad.parser.model.MirrorType;
import de.parresum.kicad.parser.model.PositionAt;
import de.parresum.kicad.parser.model.Property;

/**
 * Part extracted from scheme
 *
 * @author Kai Uwe Bachmann
 */
public class NetPart {
   /** the uuid of the part */
   private final String uuid;

   /** the mid point of the part */
   private final NetPoint point;

   /** angle of the part in scheme */
   private final int angle;

   /** name of the part in scheme */
   private final String name;

   /** name of the symbol in library */
   private final String lib;

   /** extracted symbol definition in lib */
   private final LibPart libPart;

   /** pins of the part */
   private final List<NetPin> pins = new ArrayList<NetPin>();

   /** values of the part */
   private final List<NetValue> values = new ArrayList<>();

   private final boolean mirrorX;
   private final boolean mirrorY;

   public NetPart(Symbol symbol, LibPart libPart) {
      this.uuid = symbol.getUuid().getUuid();
      this.name = getSymbolName(symbol);
      this.libPart = libPart;
      this.lib = symbol.getLibraryIdentifier();
      this.point = new NetPoint(symbol.getPosition().getX(), symbol.getPosition().getY());
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
      parseValues(symbol);
   }

   private String getSymbolName(Symbol symbol) {
      for (Property prop : symbol.getProperties()) {
         if ("Reference".equalsIgnoreCase(prop.getKey())) {
            return prop.getValue();
         }
      }
      // fallback ...
      return symbol.getLibName();
   }

   private void parsePins(Symbol symbol) {
      for (Pin pin : symbol.getPins()) {
         NetPoint point = getPinPoint(symbol, pin);
         int angle = getPinAngle(symbol, pin);

         NetPin partPin = new NetPin(pin, name, point, angle);

         pins.add(partPin);
      }
   }

   private void parseValues(Symbol symbol) {
      for (Property prop : symbol.getProperties()) {
         if (prop.getValue() != null && !prop.getValue().isBlank()) {
            values.add(new NetValue(prop.getKey(), prop.getValue(), name));
         }
      }
   }

   private NetPoint getPinPoint(Symbol symbol, Pin pin) {
      PositionAt origin = symbol.getPosition();
      String pinName = pin.getName();
      LibPin libPin = libPart.getPin(pinName);
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
      NetPoint pt = new NetPoint(origin.getX() + pinPos.getX(), origin.getY() - pinPos.getY());

      return pt;

   }

   private int getPinAngle(Symbol symbol, Pin pin) {
      PositionAt origin = symbol.getPosition();
      String pinName = pin.getName();
      LibPin libPin = libPart.getPin(pinName);
      if (libPin == null) {
         throw new IllegalStateException("Can't find pin entry for " + pinName);
      }

      return (int) (libPin.getAngle() + symbol.getPosition().getAngle());

   }

   private int getPinLength(Symbol symbol, Pin pin) {
      PositionAt origin = symbol.getPosition();
      String pinName = pin.getName();
      LibPin libPin = libPart.getPin(pinName);
      if (libPin == null) {
         throw new IllegalStateException("Can't find pin entry for " + pinName);
      }

      return (int) (libPin.getAngle() + symbol.getPosition().getAngle());

   }

   public List<NetPin> getPins() {
      return pins;
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

   public String getName() {
      return name;
   }

   public String getLib() {
      return lib;
   }

   public void paint(Graphics2D g) {
      g.setColor(Color.BLACK);

      libPart.paint(g, point, angle, mirrorX, mirrorY);
   }
}
