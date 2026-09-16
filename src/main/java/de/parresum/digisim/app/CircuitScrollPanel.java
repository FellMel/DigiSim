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

package de.parresum.digisim.app;

import java.awt.BorderLayout;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

/**
 * A scroll panel used by the circuit component
 *
 * @author Kai Uwe Bachmann
 */
public class CircuitScrollPanel extends JPanel {
   // private static final int BORDER = SIZE * 10;
   private final CircuitComponent circuitComponent;
   private final JScrollBar horizontal;
   private final JScrollBar vertical;
//   private GraphicMinMax graphicMinMax;
   private AffineTransform transform;

   /**
    * Creates a new instance
    *
    * @param circuitComponent the circuit component to use
    */
   public CircuitScrollPanel(CircuitComponent circuitComponent) {
      super(new BorderLayout());
      horizontal = new JScrollBar(JScrollBar.HORIZONTAL);
      vertical = new JScrollBar(JScrollBar.VERTICAL);

      this.circuitComponent = circuitComponent;
      add(circuitComponent, BorderLayout.CENTER);
      add(horizontal, BorderLayout.SOUTH);
      add(vertical, BorderLayout.EAST);

      horizontal.addAdjustmentListener(adjustmentEvent -> {
         if (adjustmentEvent.getValueIsAdjusting() && transform != null) {
            // circuitComponent.translateCircuitToX(-adjustmentEvent.getValue() * transform.getScaleX());
         }
      });
      vertical.addAdjustmentListener(adjustmentEvent -> {
         if (adjustmentEvent.getValueIsAdjusting() && transform != null) {
            // circuitComponent.translateCircuitToY(-adjustmentEvent.getValue() * transform.getScaleY());
         }
      });

//      addComponentListener(new ComponentAdapter() {
//         @Override
//         public void componentResized(ComponentEvent componentEvent) {
//            if (transform != null) {
//               updateBars();
//            }
//         }
//      });
//
//      circuitComponent.setCircuitScrollPanel(this);
   }

//   private GraphicMinMax getCircuitSize() {
//      if (graphicMinMax == null) {
//         graphicMinMax = new GraphicMinMax();
//         circuitComponent.getCircuit().drawTo(graphicMinMax);
//      }
//      return graphicMinMax;
//   }
//
//   void sizeChanged() {
//      graphicMinMax = null;
//      if (transform != null) {
//         updateBars();
//      }
//   }

//   /**
//    * Updates the transformation
//    *
//    * @param transform the transform
//    */
//   void transformChanged(AffineTransform transform) {
//      this.transform = transform;
//      updateBars();
//   }

//   private void updateBars() {
//      GraphicMinMax gr = getCircuitSize();
//
//      if (gr.getMin() == null || gr.getMax() == null || !circuitComponent.isManualScale()) {
//         horizontal.setVisible(false);
//         vertical.setVisible(false);
//      } else {
//         Point2D min = new Point2D.Float();
//         Point2D max = new Point2D.Float();
//         try {
//            transform.inverseTransform(new Point2D.Float(0, 0), min);
//            transform.inverseTransform(new Point2D.Float(getWidth(), getHeight()), max);
//            setValues(horizontal, min.getX(), max.getX(), gr.getMin().x, gr.getMax().x);
//            setValues(vertical, min.getY(), max.getY(), gr.getMin().y, gr.getMax().y);
//         } catch (NoninvertibleTransformException e) {
//            // can not happen! Scaling is never zero!
//            e.printStackTrace();
//         }
//      }
//   }

//   private void setValues(JScrollBar bar, double viewMin, double viewMax, int circuitMin, int circuitMax) {
//      int border = Math.max(BORDER, (circuitMax - circuitMin) / 10);
//      circuitMin -= border;
//      circuitMax += border;
//      int extent = (int) (viewMax - viewMin);
//      bar.setValues((int) viewMin, extent, circuitMin, circuitMax);
//      bar.setVisible(viewMin > circuitMin || viewMax < circuitMax);
//   }

   /**
    * @return the width of the bars
    */
   int getBarWidth() {
      return vertical.getPreferredSize().width;
   }
}
