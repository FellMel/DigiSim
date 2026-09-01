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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import de.parresum.kicad.parser.eescheme.shape.AbstractShape;
import de.parresum.kicad.parser.model.FillType;

/**
 * Base of graphical elements
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractView {

   // Some line definitions
   private final static Stroke DASH = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
         new float[] { 5.0f }, 0.0f);
   private final static Stroke DASH_DOT = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
         new float[] { 5.0f, 1.0f, 1.0f, 1.0f }, 0.0f);
   private final static Stroke DASH_DOT_DOT = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
         new float[] { 5.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f }, 0.0f);
   private final static Stroke DOT = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
         new float[] { 1.0f }, 0.0f);
   private final static Stroke SOLID = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

   /** base scale factor */
   public final static double BASE_SCALE = 5.0;

   // Stroke definition
   private final Stroke stroke;
   private double lineWidth;
   private final Color lineColor;
//   private LineType lineType;

   // Fill definition
   private final Color fillColor;
   private final FillType fillType;

   // Shape for the element
   private final Shape shape;

   protected AbstractView(AbstractShape kiCadShape, Shape shape) {
      this.shape = shape;
      if (kiCadShape != null) {
         if (kiCadShape.getStroke() != null) {
            // TODO: lineWidth
            switch (kiCadShape.getStroke().getType()) {
               case DASH:
                  stroke = DASH;
                  break;
               case DASH_DOT:
                  stroke = DASH_DOT;
                  break;
               case DASH_DOT_DOT:
                  stroke = DASH_DOT_DOT;
                  break;
               case DOT:
                  stroke = DOT;
                  break;
               case SOLID:
                  stroke = SOLID;
                  break;
               default:
                  stroke = SOLID;
            }

            lineWidth = kiCadShape.getStroke().getWidth();
            lineColor = createColor(kiCadShape.getStroke().getColor());
         } else {
            stroke = SOLID;
            lineColor = Color.BLACK;
         }

         if (kiCadShape.getFill() != null) {
            fillColor = createColor(kiCadShape.getFill().getColor());
            fillType = kiCadShape.getFill().getType();
         } else {
            fillColor = null;
            fillType = FillType.NONE;
         }
      } else {
         stroke = SOLID;
         lineColor = Color.BLACK;
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
