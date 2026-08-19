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
package de.parresum.digisim.core.io;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import de.parresum.digisim.annotations.Value;
import de.parresum.digisim.core.OutputPart;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.StateListener;
import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.gui.Digit;
import de.parresum.digisim.gui.FourDigit;
import de.parresum.digisim.gui.TwoDigit;

/**
 * Display for bus outputs
 *
 * @author Kai Uwe Bachmann
 */
public class BusOutput extends Box implements StateListener, OutputPart {

   private static final long serialVersionUID = 6887653683070383604L;

   /** Name of the element */
   private final String name;

   /** Display name of the element */
   private String description;

   /**
    * connected wires
    */
   private final List<Wire> inputs = new ArrayList<>();

   /**
    * GUI-Digit-Display
    */
   private final Digit output;

   /** GUI-Label */
   private final JLabel label;

   public BusOutput(final String name, final Wire[] wire) {
      super(BoxLayout.Y_AXIS);
      this.name = name;
      switch (wire.length) {
         case 1, 2, 3, 4:
            output = new Digit();
            break;
         case 5, 6, 7, 8:
            output = new TwoDigit();
            break;
         case 9, 10, 11, 12, 13, 14, 15, 16:
            output = new FourDigit();
            break;
         default:
            throw new IllegalArgumentException("Unsupported wire size");
      }

      label = new JLabel(name);
      label.setAlignmentX(CENTER_ALIGNMENT);
      this.add(label);
      this.add(output);
      output.setAlignmentX(CENTER_ALIGNMENT);

      addInput(wire);
      this.setBorder(new EmptyBorder(3, 3, 3, 3));
   }

   /**
    * gets the display name of this output element
    *
    * @return name of this element
    */
   public String getDescription() {
      return description;
   }

   /**
    * sets the display name of this element
    *
    * @param description name to display
    */
   @Value("Description")
   public void setDescription(String description) {
      this.description = description;
      label.setText(description);
   }

   public void addInput(final Wire[] input) {
      if (!inputs.isEmpty()) {
         throw new IllegalStateException("Multiple inputs for Not");
      }
      for (final Wire w : input) {
         inputs.add(w);
         w.addStateListener(this);

      }
      stateChanged(null, null, null);
   }

   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {
      int mask = 1;
      int current = 0;
      for (final Wire w : inputs) {
         if (w.get() == State.HIGH) {
            current |= mask;
         }
         mask <<= 1;
      }
      output.setValue(current);
   }

   @Override
   public Wire getInput() {
      return inputs.get(0);
   }

}
