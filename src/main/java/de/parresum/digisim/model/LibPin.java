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

import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.library.GraphPin;

/**
 * Extracted pin definition of lib symbol
 *
 * @author Kai Uwe Bachmann
 */
public class LibPin implements CircuitPin {
   /** name of the pin */
   private final String name;

   /** number of the pin */
   private final String number;

   /** position of the pin */
   private final NetPoint position;

   /** angle of the pin */
   private final int angle;

   /** type of the pin */
   private final PinType type;

   public LibPin(GraphPin pin) {
      name = pin.getPinName().getName();
      number = pin.getPinNumber().getName();
      position = new NetPoint(pin.getPosition().getX(), pin.getPosition().getY());
      angle = (int) Math.round(pin.getPosition().getAngle());
      switch (pin.getElectricalPinType()) {
         case INPUT:
            type = PinType.INPUT;
            break;
         case OUTPUT:
            type = PinType.OUTPUT;
            break;
         case TRI_STATE:
            type = PinType.TRI_STATE;
            break;
         case OPEN_COLLECTOR:
            type = PinType.OPEN_COLLECTOR;
            break;
         default:
            type = PinType.UNKNOWN;
            break;
      }
   }

   public LibPin(Pin pin) {
      name = pin.getName();
      number = pin.getName();
      position = new NetPoint(pin.getPosition().getX(), pin.getPosition().getY());
      angle = (int) Math.round(pin.getPosition().getAngle());
      switch (pin.getElectricalPinType()) {
         case INPUT:
            type = PinType.INPUT;
            break;
         case OUTPUT:
            type = PinType.OUTPUT;
            break;
         case TRI_STATE:
            type = PinType.TRI_STATE;
            break;
         case OPEN_COLLECTOR:
            type = PinType.OPEN_COLLECTOR;
            break;
         default:
            type = PinType.UNKNOWN;
            break;
      }
   }

   public String getName() {
      return name;
   }

   public String getNumber() {
      return number;
   }

   public NetPoint getPosition() {
      return position;
   }

   public int getAngle() {
      return angle;
   }

   public PinType getType() {
      return type;
   }

}
