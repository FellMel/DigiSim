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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.kicad.parser.eescheme.Pin;

/**
 * Pin extracted from scheme
 *
 * @author Kai Uwe Bachmann
 */
public class NetPin extends AbstractNetElement {
   private final static Logger LOG = LogManager.getLogger(NetPin.class);

   /** Position of the pin in scheme */
   private final NetPoint point;
   private final int angle;
//   private final double length;

   /** Name of the pin */
   private final String pinNr;

   /** Name of the part, the pin is for */
   private final String part;

   // private final PinShapeView pinShape;

   public NetPin(Pin pin, String part, NetPoint point, int angle) {
      super(pin.getUuid().getUuid());
      this.part = part;
      this.pinNr = pin.getName();
      this.point = point;
      this.angle = angle;
      // this.length = pin.getLength();
      // this.pinShape = PinShapeView.from(pin.getGraphicPinShape());
   }

   @Override
   public boolean containsPoint(List<NetPoint> points) {
      for (NetPoint pt : points) {
         if (this.point.equals(pt)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public List<NetPoint> getPoints() {
      return List.of(point);
   }

   @Override
   public boolean isPin() {
      return true;
   }

   public String getPinNr() {
      return pinNr;
   }

   public String getPart() {
      return part;
   }

   public int getAngle() {
      return angle;
   }

//   public double getLength() {
//      return length;
//   }
//
//   public PinShapeView getPinShape() {
//      return pinShape;
//   }

   /**
    * for debugging, only
    */
   @Override
   public void print() {
      LOG.info(String.format("  %s, %s (%s)", part, pinNr, point));

   }

}
