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

import java.awt.Graphics2D;

import de.parresum.digisim.model.NetPoint;
import de.parresum.kicad.parser.eescheme.shape.Text;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class TextView extends AbstractView {
   private final NetPoint pos;
   private final String str;

   public TextView(Text text) {
      super(text, null);
      this.pos = new NetPoint(text.getPosition());
      this.str = text.getText();
   }

   @Override
   protected void paintOutline(Graphics2D g2d) {
      // TODO: Text-Effects

      g2d.drawString(str, (float) (pos.getX() * BASE_SCALE), (float) (pos.getY() * -BASE_SCALE));

   }

   @Override
   protected void paintBackground(Graphics2D g2d) {
      // nothing to do, yet
   }

}
