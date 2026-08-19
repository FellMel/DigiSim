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

import java.text.ParseException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

import de.parresum.digisim.annotations.Value;
import de.parresum.digisim.core.InputPart;
import de.parresum.digisim.core.OutputPart;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.wire.TriStateWire;
import de.parresum.digisim.core.wire.Wire;

/**
 * Input-Output-Element for tri state wires
 *
 * @author Kai Uwe Bachmann
 */
public class BusInOut extends Box implements ChangeListener, InputPart, OutputPart {

   private static final long serialVersionUID = 500826478704230350L;

   /** Name of the element */
   private final String name;

   /** Display name of the element */
   private String description;

   /** Connected tri state wires */
   private TriStateWire[] bus;

   /** internal bus for handling */
   private final Wire[] myBus;

   /** enable wire */
   private final Wire enable;

   /** GUI input element */
   private final JSpinner spnNumber;

   /** GUI Input for enable */
   private final JCheckBox input = new JCheckBox();

   /** GUI-Label */
   private final JLabel label;

   public BusInOut(final String name, final TriStateWire[] output, final Wire enable) {
      super(BoxLayout.Y_AXIS);
      this.name = name;
      this.enable = enable;

      myBus = new Wire[output.length];
      int maxValue = 1;
      for (int i = 0; i < output.length; i++) {
         maxValue <<= 1;
         maxValue |= 0x01;

         myBus[i] = new Wire("");
         output[i].addWire(myBus[i]);
         output[i].addStateListener((s, o, n) -> busChanged());
      }

      spnNumber = new JSpinner(new SpinnerNumberModel(0, 0, maxValue, 1));
      final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spnNumber.getEditor();
      final JFormattedTextField tf = editor.getTextField();
      tf.setFormatterFactory(new MyFormatterFactory());

      label = new JLabel(name);
      this.add(label);
      label.setAlignmentX(CENTER_ALIGNMENT);
      this.add(spnNumber);
      spnNumber.setAlignmentX(CENTER_ALIGNMENT);
      spnNumber.addChangeListener(this);

      this.add(input);
      input.setAlignmentX(CENTER_ALIGNMENT);
      input.addChangeListener(l -> enableChanged());
      setOutput(output);
      this.setBorder(new EmptyBorder(3, 3, 3, 3));
      enableChanged();
   }

   /**
    * gets the display name of this element
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

   /**
    * connects the bus to this element
    *
    * @param output wires to connect
    */
   public void setOutput(final TriStateWire[] output) {
      if (this.bus != null) {
         throw new IllegalStateException("Double output on wire");
      }

      this.bus = output;
      stateChanged(null);
   }

   @Override
   public Wire getOutput() {
      return bus[0];
   }

   @Override
   public Wire getInput() {
      return bus[0];
   }

   /**
    * reacts on change of enable
    */
   protected void enableChanged() {
      final State current = input.isSelected() ? State.HIGH : State.LOW;
      enable.set(current);

      if (current == State.HIGH) {
         spnNumber.setEnabled(true);
         stateChanged(null);

      } else {
         spnNumber.setEnabled(false);
         for (final Wire w : myBus) {
            w.set(State.OPEN);
         }

      }

   }

   protected void busChanged() {
      final State current = input.isSelected() ? State.HIGH : State.LOW;
      if (current == State.LOW) {
         int val = 0;
         for (int i = bus.length - 1; i >= 0; i--) {
            val <<= 1;
            if (bus[i].get() == State.HIGH) {
               val |= 0x01;
            }
         }
         spnNumber.getModel().setValue(val);
      }
   }

   @Override
   public void stateChanged(final ChangeEvent e) {
      int val = ((Number) spnNumber.getModel().getValue()).intValue();
      final State current = input.isSelected() ? State.HIGH : State.LOW;
      if (current == State.HIGH) {
         for (int i = 0; i < myBus.length; i++) {
            if ((val & 0x01) == 0) {
               myBus[i].set(State.LOW);
            } else {
               myBus[i].set(State.HIGH);

            }
            val >>= 1;
         }
      }

   }

   private static class MyFormatterFactory extends DefaultFormatterFactory {
      private static final long serialVersionUID = 8140552094425475520L;

      @Override
      public AbstractFormatter getDefaultFormatter() {
         return new HexFormatter();
      }
   }

   private static class HexFormatter extends DefaultFormatter {
      private static final long serialVersionUID = -1573486840620279177L;

      @Override
      public Object stringToValue(final String text) throws ParseException {
         try {
            return Integer.valueOf(text, 16);
         } catch (final NumberFormatException nfe) {
            throw new ParseException(text, 0);
         }
      }

      @Override
      public String valueToString(final Object value) throws ParseException {
         return Integer.toHexString(((Number) value).intValue()).toUpperCase();
      }
   }

}
