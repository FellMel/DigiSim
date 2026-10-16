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
package de.parresum.digisim.lib.io;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import de.parresum.digisim.api.CircuitComponent;
import de.parresum.digisim.api.State;
import de.parresum.digisim.api.StateListener;
import de.parresum.digisim.api.annotations.Component;
import de.parresum.digisim.api.annotations.Pin;
import de.parresum.digisim.api.annotations.PortType;
import de.parresum.digisim.api.annotations.Value;
import de.parresum.digisim.api.wire.Wire;
import de.parresum.digisim.lib.OutputPart;

/**
 * GUI-Element of an output wire / LED
 *
 * @author Kai Uwe Bachmann
 */
@Component("digisim:LED")
public class Output extends Box implements CircuitComponent, StateListener, OutputPart {

   private static final long serialVersionUID = 5797566509949991774L;

   /** Name of the element */
   private final String name;

   /** Display name of the element */
   private String description;

   /** name of the defining lib */
   private String libName;

   /** input wire */
   private Wire input;

   /** GUI element for output */
   private final Led output = new Led();

   /** GUI-Label */
   private final JLabel label;

   /**
    * Creates an output element
    *
    * @param name name of the element
    */
   public Output(final String name) {
      super(BoxLayout.Y_AXIS);
      this.name = name;

      label = new JLabel(name);
      label.setAlignmentX(CENTER_ALIGNMENT);
      this.add(label);
      this.add(output);
      output.setAlignmentX(CENTER_ALIGNMENT);
   }

   /**
    * Creates an output element
    *
    * @param name  name of the element
    * @param input wire connected with this element
    */
   public Output(final String name, final Wire input) {
      this(name);

      addInput(input);
      this.setBorder(new EmptyBorder(3, 3, 3, 3));
   }

   /**
    * Creates an output element
    *
    * @param input wire connected with this element
    */
   public Output(final Wire input) {
      this(input.toString(), input);
   }

   // ----------------------------------- o -----------------------------------
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

   @Override
   public String getName() {
      return name;
   }

   @Override
   public String getLibName() {
      return libName;
   }

   @Override
   public void setLibName(String libName) {
      this.libName = libName;
   }

   /**
    * adds a wire to this element
    *
    * @param input the wire to connect
    */
   public void addInput(final Wire input) {
      if (this.input != null) {
         throw new IllegalStateException("Multiple inputs for LED");
      }
      this.input = input;
      input.addStateListener(this);
      stateChanged(input, null, null);
   }

   /**
    * sets the input wire
    *
    * @param wire wire to connect
    */
   @Pin(value = "I", type = PortType.INPUT)
   public void setInput(Wire wire) {
      addInput(wire);
   }

   /**
    * gets the wire connected to this element
    *
    * @return the connected wire
    */
   @Override
   public Wire getInput() {
      return input;
   }

   // ----------------------------------- o -----------------------------------
   @Override
   public void stateChanged(final Wire src, final State oldState, final State newState) {
      if (input != null) {
         final State current = input.get();
         output.setValue(current);
      }
   }

   @Override
   public void connected() {
      this.stateChanged(null, null, null);
   }

   // ----------------------------------- o -----------------------------------
   @Override
   public String toString() {
      return name;
   }

}
