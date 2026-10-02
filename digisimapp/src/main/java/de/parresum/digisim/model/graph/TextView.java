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

import static de.parresum.digisim.model.ModelConstants.UNIT_FACTOR;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import de.parresum.digisim.model.NetPoint;
import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.eescheme.shape.Text;
import de.parresum.kicad.parser.model.Justify;
import de.parresum.kicad.parser.model.Property;
import de.parresum.kicad.parser.model.Size;
import de.parresum.kicad.parser.model.TextEffects;

/**
 * A graphical text element
 *
 * @author Kai Uwe Bachmann
 */
public class TextView extends AbstractView {
   /** Position of the text */
   private final NetPoint pos;

   private NetPoint delta;

   /** rotation of the text */
   private final double angle;

   /** the text itself */
   private final String str;

   private Font font;
   private Color fontColor;

   private Shape shape;
   private Justify just;

   public TextView(Text text) {
      super(text, null);
      this.pos = new NetPoint(text.getPosition());
      angle = text.getPosition().getAngle();
      this.str = text.getText();

      readEffects(text.getTextEffects());
   }

   public TextView(Property prop) {
      super(null, null);
      this.pos = new NetPoint(prop.getAt());
      this.angle = prop.getAt().getAngle();
      this.str = prop.getValue();

      readEffects(prop.getEffects());
   }

   /**
    * @param item
    */
   public TextView(Pin pin, double dx, double dy) {
      super(null, null);
      this.pos = new NetPoint(pin.getPosition());
      this.angle = pin.getPosition().getAngle();
      this.str = pin.getName();
      this.delta = new NetPoint(dx, dy);

      readEffects(pin.getEffects());
   }

   private void readEffects(TextEffects stile) {
      de.parresum.kicad.parser.model.Font infont = stile.getFont();
      fontColor = createColor(infont.getColor());
      String face = infont.getFontFace();
      Size size = infont.getSize();
      Double thickness = infont.getThickness();

      // scale by 10 to avoid wrong spacing
      font = new Font(face, Font.PLAIN, (int) ((size.getHeight() * 1.4 * UNIT_FACTOR + .5)));

      just = stile.getJustify();

   }

   @Override
   protected void paintOutline(Graphics2D g2d) {
      paintOutline(g2d, 0.0);
   }

   public void paintOutline(Graphics2D g2d, double angle) {
      java.awt.Font oldfont = g2d.getFont();

      g2d.setFont(font);
      g2d.setColor(fontColor);

      AffineTransform origTransform = g2d.getTransform();

      // for text writing revert mirroring / flipping ...
      double mirrorX = origTransform.getScaleX() < 0.0 ? -1 : 1;
      double mirrorY = origTransform.getScaleY() < 0.0 ? -1 : 1;
      if ((origTransform.getType() & AffineTransform.TYPE_FLIP) != 0
            && (origTransform.getType() & AffineTransform.TYPE_QUADRANT_ROTATION) != 0) {
         mirrorY = -mirrorY; // TODO: check whether x oder y
      }
      g2d.scale(mirrorX, mirrorY);

      int width = g2d.getFontMetrics().stringWidth(str);
      int height = g2d.getFontMetrics().getHeight();
      int descent = g2d.getFontMetrics().getDescent();
      int ascent = g2d.getFontMetrics().getAscent();

      double posX = ((mirrorX * pos.getX()));
      double posY = ((mirrorY * pos.getY()));
      g2d.translate(posX, posY);

      double rotate = -(this.angle + angle) % 180.0;
      if (origTransform.getShearX() < 0 && origTransform.getShearY() > 0) {
         // type = 4b ????? oder 9
         rotate += 180.0;
      }
      g2d.rotate(Math.toRadians(rotate));
      if (delta != null) {
         if ((this.angle + angle) <= 90.0) {
            g2d.translate(-mirrorX * delta.getX(), mirrorY * delta.getY());

         } else {
            g2d.translate(mirrorX * delta.getX(), mirrorY * delta.getY());
         }

      }

      g2d.drawString(str, getXPos(width), getYPos(descent, ascent));

      g2d.setTransform(origTransform);
      g2d.setFont(oldfont);
   }

   public static Shape getTextShape(Graphics2D g2d, String text, double x, double y, Font font, boolean ltr) {
      AttributedString attstring = new AttributedString(text);
      attstring.addAttribute(TextAttribute.FONT, font);
      attstring.addAttribute(TextAttribute.RUN_DIRECTION,
            ltr ? TextAttribute.RUN_DIRECTION_LTR : TextAttribute.RUN_DIRECTION_RTL);
      FontRenderContext frc = g2d.getFontRenderContext();
      TextLayout t = new TextLayout(attstring.getIterator(), frc);
      AffineTransform transform = new AffineTransform();
      transform.translate(x, y);
      return t.getOutline(transform);
   }

   @Override
   protected void paintBackground(Graphics2D g2d) {
      // nothing to do, yet
   }

   private float getXPos(float width) {
      if (just != null && just.getHoritontalAlignment() != null) {
         switch (just.getHoritontalAlignment()) {
            case CENTER:
               return -width / 2.0f;
            case LEFT:
               return 0f;
            case RIGHT:
               return -width;
         }
      }
      return -width / 2.0f;
   }

   private float getYPos(float descent, float ascent) {
      if (just != null && just.getVerticalAlignment() != null) {
         switch (just.getVerticalAlignment()) {
            case CENTER:
               return ascent / 2.0f;
            case BOTTOM:
               return 0f;
            case TOP:
               return ascent;
         }
      }
      return ascent / 2.0f;
   }

   @Override
   public Rectangle2D getBounding() {
      // TODO: rotate
      FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
      Rectangle2D fontBound = font.getStringBounds(str, frc);

      TextLayout tl = new TextLayout(str, font, frc);
      float descent = tl.getDescent();
      float ascent = tl.getAscent();
      float height = ascent + descent;
      float width = (float) fontBound.getWidth();

      return new Rectangle2D.Double(pos.getX() + getXPos(width), pos.getY() + getYPos(descent, ascent),
            fontBound.getWidth(), fontBound.getHeight());
   }

}
