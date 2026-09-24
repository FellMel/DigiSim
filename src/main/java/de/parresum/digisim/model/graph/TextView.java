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

   /** the text itself */
   private final String str;

   private Font font;
   private Color fontColor;

   private Shape shape;

   public TextView(Text text) {
      super(text, null);
      this.pos = new NetPoint(text.getPosition());
      this.str = text.getText();

      readEffects(text.getTextEffects());
   }

   public TextView(Property prop) {
      super(null, null);
      this.pos = new NetPoint(prop.getAt().getX() * UNIT_FACTOR, prop.getAt().getY() * UNIT_FACTOR);
      this.str = prop.getValue();

      readEffects(prop.getEffects());
   }

   private void readEffects(TextEffects stile) {
      de.parresum.kicad.parser.model.Font infont = stile.getFont();
      fontColor = createColor(infont.getColor());
      String face = infont.getFontFace();
      Size size = infont.getSize();
      Double thick = infont.getThickness();

      // scale by 10 to avoid wrong spacing
      font = new Font(face, Font.PLAIN, (int) ((size.getHeight() * 1.4 * UNIT_FACTOR + .5)));

      Justify just = stile.getJustify();

   }

   @Override
   protected void paintOutline(Graphics2D g2d) {
      java.awt.Font oldfont = g2d.getFont();

      g2d.setFont(font);
      g2d.setColor(fontColor);

      AffineTransform origTransform = g2d.getTransform();

      // for text writing revert mirroring ...
      double mirrorX = origTransform.getScaleX() < 0.0 ? -1 : 1;
      double mirrorY = origTransform.getScaleY() < 0.0 ? -1 : 1;
      g2d.scale(mirrorX, mirrorY);

      int width = g2d.getFontMetrics().stringWidth(str);
      int height = g2d.getFontMetrics().getHeight();
      int descent = g2d.getFontMetrics().getDescent();
      int ascent = g2d.getFontMetrics().getAscent();

      float posX = (float) ((mirrorX * pos.getX() - width / 2.0));
      float posY = (float) ((mirrorY * pos.getY() + ascent / 2.0));
      g2d.drawString(str, posX, posY);

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

   @Override
   public Rectangle2D getBounding() {
      // TODO: rotate
      Rectangle2D fontBound = font.getStringBounds(str, new FontRenderContext(new AffineTransform(), false, false));
      return new Rectangle2D.Double(pos.getX(), pos.getY() + fontBound.getHeight() / 2.0, fontBound.getWidth(),
            fontBound.getHeight());
   }

}
