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
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.wire.Wire;

/**
 * Input for Bus (multiple wires)
 *
 * @author Kai Uwe Bachmann
 */
public class BusInput extends Box implements ChangeListener, InputPart {

   private static final long serialVersionUID = 1072165416851193426L;

   /** Name of the element */
   private final String name;

   /** Display name of the element */
   private String description;

   /** connected wires */
   private Wire[] output;

   /** GUI element for input */
   private final JSpinner spnNumber;

   /** GUI-Label */
   private final JLabel label;

   public BusInput(final String name, final Wire[] output) {
      super(BoxLayout.Y_AXIS);
      this.name = name;

      int maxValue = 1;
      for (final Wire w : output) {
         maxValue <<= 1;
         maxValue |= 0x01;
      }

      spnNumber = new JSpinner(new SpinnerNumberModel(0, 0, maxValue, 1));
      final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spnNumber.getEditor();
      final JFormattedTextField tf = editor.getTextField();
      tf.setFormatterFactory(new MyFormatterFactory());

      this.add(spnNumber);
      label = new JLabel(name);
      label.setAlignmentX(CENTER_ALIGNMENT);
      this.add(label);
      spnNumber.addChangeListener(this);

      setOutput(output);
      this.setBorder(new EmptyBorder(3, 3, 3, 3));
   }

   /**
    * gets the display name of this input element
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

   @Override
   public Wire getOutput() {
      return output[0];
   }

   /**
    * sets the input wires
    *
    * @param output wires to connect
    */
   public void setOutput(final Wire[] output) {
      if (this.output != null) {
         throw new IllegalStateException("Double output on Bus " + name);
      }

      this.output = output;
      stateChanged(null);
   }

   @Override
   public void stateChanged(final ChangeEvent e) {
      int val = ((Number) spnNumber.getModel().getValue()).intValue();

      for (int i = 0; i < output.length; i++) {
         if ((val & 0x01) == 0) {
            output[i].set(State.LOW);
         } else {
            output[i].set(State.HIGH);

         }
         val >>= 1;
      }

   }

   private static class MyFormatterFactory extends DefaultFormatterFactory {
      private static final long serialVersionUID = 5907289550992086897L;

      @Override
      public AbstractFormatter getDefaultFormatter() {
         return new HexFormatter();
      }
   }

   private static class HexFormatter extends DefaultFormatter {
      private static final long serialVersionUID = 3211417872436787661L;

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
