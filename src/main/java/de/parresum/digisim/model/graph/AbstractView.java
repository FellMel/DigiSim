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

import static de.parresum.digisim.model.ModelConstants.DASH;
import static de.parresum.digisim.model.ModelConstants.DASH_DOT;
import static de.parresum.digisim.model.ModelConstants.DASH_DOT_DOT;
import static de.parresum.digisim.model.ModelConstants.DOT;
import static de.parresum.digisim.model.ModelConstants.SOLID;
import static de.parresum.digisim.model.ModelConstants.UNIT_FACTOR;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import de.parresum.kicad.parser.eescheme.shape.AbstractShape;
import de.parresum.kicad.parser.model.Fill;
import de.parresum.kicad.parser.model.FillType;

/**
 * Base of graphical elements
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractView {

   // Stroke definition
   private final Stroke stroke;
   private double lineWidth;
   private final Color lineColor;

   // Fill definition
   private final Color fillColor;
   private final FillType fillType;

   // Shape for the element
   private final Shape shape;

   protected AbstractView(AbstractShape kiCadShape, Shape shape) {
      this((kiCadShape != null) ? kiCadShape.getStroke() : null, //
            (kiCadShape != null) ? kiCadShape.getFill() : null, //
            shape);
   }

   /**
    * @param stroke
    * @param fill
    * @param shape
    */
   public AbstractView(de.parresum.kicad.parser.model.Stroke stroke, Fill fill, Shape shape) {
      this.shape = shape;
      if (stroke != null) {
         BasicStroke tmp;
         lineWidth = stroke.getWidth() * UNIT_FACTOR;

         switch (stroke.getType()) {
            case DASH:
               tmp = DASH;
               break;
            case DASH_DOT:
               tmp = DASH_DOT;
               break;
            case DASH_DOT_DOT:
               tmp = DASH_DOT_DOT;
               break;
            case DOT:
               tmp = DOT;
               break;
            case SOLID:
               tmp = SOLID;
               break;
            default:
               tmp = SOLID;
         }
         if (lineWidth > 0.0) {
            tmp = new BasicStroke((float) lineWidth, tmp.getEndCap(), tmp.getLineJoin(), tmp.getMiterLimit(),
                  tmp.getDashArray(), tmp.getDashPhase());
         }

         this.stroke = tmp;
         lineColor = createColor(stroke.getColor());
      } else {
         this.stroke = SOLID;
         lineColor = Color.BLACK;
      }

      if (fill != null) {
         fillColor = createColor(fill.getColor());
         fillType = (fill.getType() != null) ? fill.getType() : FillType.NONE;
      } else {
         fillColor = null;
         fillType = FillType.NONE;
      }
   }

   /**
    * paints the element
    *
    * @param g2d
    */
   public void paint(Graphics2D g2d) {

      paintBackground(g2d);
      paintOutline(g2d);
   }

   /**
    * paints the outline
    *
    * @param g2d
    */
   protected void paintOutline(Graphics2D g2d) {
      g2d.setColor(lineColor);
      g2d.setStroke(stroke);
      g2d.draw(shape);
   }

   /**
    * paints the filled content of the shape
    *
    * @param g2d
    */
   protected void paintBackground(Graphics2D g2d) {
      switch (fillType) {
         case NONE:
            return;
         case OUTLINE:
            g2d.setColor(lineColor);
            break;
         case BACKGROUND:
         case SOLID:
            g2d.setColor(fillColor);
            break;
      }
      g2d.fill(shape);
   }

   public Rectangle2D getBounding() {
      return shape.getBounds2D();
   }

   /**
    * creates a color from model
    *
    * @param col
    * @return
    */
   protected Color createColor(de.parresum.kicad.parser.model.Color col) {
      if (col == null) {
         return Color.BLACK;
      }
      if (col.getAlpha() != 0.0) {
         return new Color(col.getRed(), col.getGreen(), col.getBlue(), (int) (col.getAlpha() * 255));
      }
      return new Color(col.getRed(), col.getGreen(), col.getBlue());
   }
}
