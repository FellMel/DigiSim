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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

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

   public TextView(Text text) {
      super(text, null);
      this.pos = new NetPoint(text.getPosition());
      this.str = text.getText();

      readEffects(text.getTextEffects());
   }

   public TextView(Property prop) {
      super(null, null);
      this.pos = new NetPoint(prop.getAt().getX(), -prop.getAt().getY());
      this.str = prop.getValue();

      readEffects(prop.getEffects());
   }

   private void readEffects(TextEffects stile) {
      de.parresum.kicad.parser.model.Font infont = stile.getFont();
      fontColor = createColor(infont.getColor());
      String face = infont.getFontFace();
      Size size = infont.getSize();
      Double thick = infont.getThickness();

      font = new Font(face, Font.PLAIN, (int) ((size.getHeight() + .5) * BASE_SCALE));

      Justify just = stile.getJustify();

   }

   @Override
   protected void paintOutline(Graphics2D g2d) {
      // TODO: Text-Effects
      java.awt.Font oldfont = g2d.getFont();

      g2d.setFont(font);
      g2d.setColor(fontColor);
      int width = g2d.getFontMetrics().stringWidth(str);
      int height = g2d.getFontMetrics().getHeight();
      g2d.drawString(str, (float) ((pos.getX()) * BASE_SCALE - width / 2.0),
            (float) ((pos.getY()) * -BASE_SCALE + height / 2.0));

      g2d.setFont(oldfont);
   }

   @Override
   protected void paintBackground(Graphics2D g2d) {
      // nothing to do, yet
   }

}
