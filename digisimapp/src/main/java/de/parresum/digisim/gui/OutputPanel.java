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
package de.parresum.digisim.gui;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import de.parresum.digisim.lib.io.Output;
import de.parresum.digisim.lib.wire.Wire;

/**
 * Panel to capture all output elements
 *
 * @author Kai Uwe Bachmann
 */
public class OutputPanel extends JPanel {

   private static final long serialVersionUID = -3731621811547417367L;

   public OutputPanel() {
      super();
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setBorder(new LineBorder(Color.BLACK));
   }

   public OutputPanel(final boolean vertical) {
      super();
      this.setLayout(new BoxLayout(this, vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
      setBorder(new LineBorder(Color.BLACK));
   }

   /**
    * add an output component to the panel
    *
    * @param output the component to add
    */
   public void addOutput(final JComponent output) {
      this.add(output);
   }

   /**
    * adds a wire to the panel. The wire will be represented by standard Output
    *
    * @param wire the wire to add
    */
   public void addWire(final Wire wire) {
      final Output out = new Output(wire);
      addOutput(out);
   }

}
