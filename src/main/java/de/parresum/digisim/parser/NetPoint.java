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
package de.parresum.digisim.parser;

import java.util.Objects;

/**
 * An extracted Point from scheme
 *
 * @author Kai Uwe Bachmann
 */
public class NetPoint {
   /** Precission for comparison of points */
   private static final double PRECISSION = 0.01;

   /** x coordinate */
   private final double x;

   /** y cordinate */
   private final double y;

   public NetPoint(double x, double y) {
      super();
      this.x = x;
      this.y = y;
   }

   public double getX() {
      return x;
   }

   public double getY() {
      return y;
   }

   public NetPoint rotate(int angle) {
      switch (angle) {
         case 0:
            return this;

         case 90:
            return new NetPoint(-y, x);

         case 180:
            return new NetPoint(-x, -y);

         case 270:
            return new NetPoint(y, -x);

         default:
            return this;
      }
   }

   public NetPoint mirror(boolean xAxis) {
      if (xAxis) {
         return new NetPoint(x, -y);
      } else {
         return new NetPoint(-x, y);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(x, y);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      NetPoint other = (NetPoint) obj;

      return Math.abs(this.x - other.x) < PRECISSION && Math.abs(this.y - other.y) < PRECISSION;

   }

   /**
    * for debugging, only
    */
   @Override
   public String toString() {
      return String.format("NetPoint [x=%.2f, y=%.2f]", x, y);
   }

}
