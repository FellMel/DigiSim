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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.annotations.Pin;
import de.parresum.digisim.annotations.PortType;
import de.parresum.digisim.annotations.Value;
import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.core.InputPart;
import de.parresum.digisim.core.Out;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.wire.TriStateWire;
import de.parresum.digisim.core.wire.Wire;

/**
 * GUI-Input element / switch
 *
 * @author Kai Uwe Bachmann
 */
@Part("digisim:Switch")
public class Input extends Box implements CircuitPart, ChangeListener, Out, InputPart {

   private static final long serialVersionUID = 3660162041124702476L;
   /** Name of the element */
   private final String name;

   /** Display name of the element */
   private String description;

   /** name of the defining lib */
   private String libName;

   /** output wire */
   private Wire output;

   /** GUI element for intput */
   private final JCheckBox input = new JCheckBox();

   /** GUI-Label */
   private final JLabel label;

   /**
    * Creates an input element
    *
    * @param name name of the element
    */
   public Input(final String name) {
      super(BoxLayout.Y_AXIS);
      this.name = name;

      label = new JLabel(name);
      this.add(input);
      this.add(label);
      input.addChangeListener(this);

      this.setBorder(new EmptyBorder(3, 3, 3, 3));
      if (!name.isEmpty() && name.charAt(0) == '/') {
         input.setSelected(true);
      }

   }

   /**
    * Creates an input element
    *
    * @param name name of the element
    * @param wire output connected with this element
    */
   public Input(final String name, final Wire output) {
      this(name);
      setOutput(output);
   }

   /**
    * Creates an input element
    *
    * @param output wire connected with this element
    */
   public Input(final Wire output) {
      this(output.toString(), output);
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
   public String getLibName() {
      return libName;
   }

   @Override
   public void setLibName(String libName) {
      this.libName = libName;
   }

   @Pin(value = "O", type = PortType.OUTPUT)
   public void setOutput(Wire output) {
      if (this.output != null) {
         throw new IllegalStateException("Double output on switch");
      }

      if (output instanceof TriStateWire) {
         final Wire tmp = new Wire("tmp");
         ((TriStateWire) output).addWire(tmp);
         output = tmp;
      }
      this.output = output;
      stateChanged(null);
   }

   @Override
   public Wire getOutput() {
      return output;
   }

   @Override
   public void stateChanged(final ChangeEvent e) {
      final State current = input.isSelected() ? State.HIGH : State.LOW;
      output.set(current);

   }

}
