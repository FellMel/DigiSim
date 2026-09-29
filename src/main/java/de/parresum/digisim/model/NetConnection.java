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

import static de.parresum.digisim.model.ModelConstants.UNIT_FACTOR;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.parresum.kicad.parser.eescheme.ShapeType;
import de.parresum.kicad.parser.eescheme.label.AbstractLabel;
import de.parresum.kicad.parser.eescheme.label.GlobalLabel;
import de.parresum.kicad.parser.eescheme.label.HierarchicalLabel;
import de.parresum.kicad.parser.model.Justify;
import de.parresum.kicad.parser.model.Size;
import de.parresum.kicad.parser.model.TextEffects;

/**
 * Connection to outer (upper) circuit parts.
 *
 * Acts also as Input / Output
 *
 * @author Kai Uwe Bachmann
 */
public class NetConnection extends AbstractNetElement {
   /** point of the connection */
   private final NetPoint point;

   private final double angle;

   /** type of the connection */
   private final PinType type;

   /** Label of the connection */
   private final String name;

   private Font font;
   private Justify just;
   private final boolean isHierarchical;

   public NetConnection(GlobalLabel label) {
      this(label, false, label.getShape());
   }

   public NetConnection(HierarchicalLabel label) {
      this(label, true, label.getShape());
   }

   private NetConnection(AbstractLabel label, boolean hierarchical, ShapeType shape) {
      super(label.getUuid().getUuid());
      point = new NetPoint(label.getPosition().getX() * UNIT_FACTOR, label.getPosition().getY() * UNIT_FACTOR);
      angle = label.getPosition().getAngle();

      name = label.getText();
      isHierarchical = hierarchical;
      switch (shape) {
         case BIDIRECTIONAL:
            type = PinType.TRI_STATE;
            break;
         case INPUT:
            type = PinType.INPUT;
            break;
         case OUTPUT:
            type = PinType.OUTPUT;
            break;
         case TRI_STATE:
            type = PinType.TRI_STATE;
            break;
         case PASSIVE:
            type = PinType.OPEN_COLLECTOR;
            break;
         case ROUND:
            type = PinType.UNKNOWN;
            break;
         default:
            type = PinType.UNKNOWN;
            break;

      }
      readEffects(label.getTextEffects());
   }

   private void readEffects(TextEffects stile) {
      de.parresum.kicad.parser.model.Font infont = stile.getFont();
      // fontColor = createColor(infont.getColor());
      String face = infont.getFontFace();
      Size size = infont.getSize();
      Double thick = infont.getThickness();

      font = new Font(face, Font.PLAIN, (int) ((size.getHeight() * UNIT_FACTOR + .5)));

      just = stile.getJustify();

   }

   public PinType getType() {
      return type;
   }

   public String getName() {
      return name;
   }

   public NetPoint getPoint() {
      return point;
   }

   public double getAngle() {
      return angle;
   }

   public Font getFont() {
      return font;
   }

   public void setFont(Font font) {
      this.font = font;
   }

   /**
    * @return the just
    */
   public Justify getJust() {
      return just;
   }

   /**
    * @return the isHierarchical
    */
   public boolean isHierarchical() {
      return isHierarchical;
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
   public void paint(Graphics2D g) {
      // System.out.println(String.format("Con %s:%s - %s", name, type, getUuid()));
      // TODO Auto-generated method stub
      // nothing to paint, yet

   }

   @Override
   public Rectangle2D getBounding() {
      return new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);
   }

   /**
    * for debugging, only
    */
   @Override
   public void print() {
//      System.out.println(String.format("  connection (%.2f, %.2f)", point.getX(), point.getY()));
      // nothing to do, yet

   }

}
