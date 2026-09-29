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

package de.parresum.digisim.model.graph;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.Objects;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class DimensionDouble extends Dimension2D implements java.io.Serializable {

   /**
    * The width dimension; negative values can be used.
    *
    * @serial
    * @see #getSize
    * @see #setSize
    * @since 1.0
    */
   public double width;

   /**
    * The height dimension; negative values can be used.
    *
    * @serial
    * @see #getSize
    * @see #setSize
    * @since 1.0
    */
   public double height;

   /**
    * Creates an instance of {@code Dimension} with a width of zero and a height of zero.
    */
   public DimensionDouble() {
      this(0, 0);
   }

   /**
    * Creates an instance of {@code Dimension} whose width and height are the same as for the specified dimension.
    *
    * @param d the specified dimension for the {@code width} and {@code height} values
    */
   public DimensionDouble(Dimension d) {
      this(d.width, d.height);
   }

   /**
    * Constructs a {@code Dimension} and initializes it to the specified width and specified height.
    *
    * @param width  the specified width
    * @param height the specified height
    */
   public DimensionDouble(double width, double height) {
      this.width = width;
      this.height = height;
   }

   /**
    * {@inheritDoc}
    *
    * @since 1.2
    */
   @Override
   public double getWidth() {
      return width;
   }

   /**
    * {@inheritDoc}
    *
    * @since 1.2
    */
   @Override
   public double getHeight() {
      return height;
   }

   /**
    * Sets the size of this {@code Dimension} object to the specified width and height in double precision. Note that if
    * {@code width} or {@code height} are larger than {@code Integer.MAX_VALUE}, they will be reset to
    * {@code Integer.MAX_VALUE}.
    *
    * @param width  the new width for the {@code Dimension} object
    * @param height the new height for the {@code Dimension} object
    * @since 1.2
    */
   @Override
   public void setSize(double width, double height) {
      this.width = width;
      this.height = height;
   }

   /**
    * Sets the size of this {@code Dimension} object to the specified size. This method is included for completeness, to
    * parallel the {@code setSize} method defined by {@code Component}.
    *
    * @param d the new size for this {@code Dimension} object
    * @see java.awt.Dimension#getSize
    * @see java.awt.Component#setSize
    * @since 1.1
    */
   @Override
   public void setSize(Dimension2D d) {
      setSize(d.getWidth(), d.getHeight());
   }

   /**
    * Sets the size of this {@code Dimension} object to the specified width and height. This method is included for
    * completeness, to parallel the {@code setSize} method defined by {@code Component}.
    *
    * @param width  the new width for this {@code Dimension} object
    * @param height the new height for this {@code Dimension} object
    * @see java.awt.Dimension#getSize
    * @see java.awt.Component#setSize
    * @since 1.1
    */
   public void setSize(int width, int height) {
      this.width = width;
      this.height = height;
   }

   @Override
   public int hashCode() {
      return Objects.hash(height, width);
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
      DimensionDouble other = (DimensionDouble) obj;
      return Double.doubleToLongBits(height) == Double.doubleToLongBits(other.height)
            && Double.doubleToLongBits(width) == Double.doubleToLongBits(other.width);
   }

   /**
    * Returns a string representation of the values of this {@code Dimension} object's {@code height} and {@code width}
    * fields. This method is intended to be used only for debugging purposes, and the content and format of the returned
    * string may vary between implementations. The returned string may be empty but may not be {@code null}.
    *
    * @return a string representation of this {@code Dimension} object
    */
   @Override
   public String toString() {
      return getClass().getName() + "[width=" + width + ",height=" + height + "]";
   }
}
