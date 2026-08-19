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
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import de.parresum.digisim.core.State;

/**
 * A GUI LED element
 *
 * @author Kai Uwe Bachmann
 */
public class Led extends JPanel {

   private static final long serialVersionUID = -5842285200312398365L;

   /**
    * creates the LED
    */
   public Led() {
      super();

      setBorder(new LineBorder(Color.BLACK, 2));
      setSize(16, 16);
      setMaximumSize(new Dimension(16, 16));
      setMaximumSize(new Dimension(16, 16));
      setBackground(Color.WHITE);
   }

   /**
    * Sets the state shown by the LED
    *
    * @param val new state
    */
   public void setValue(final State val) {
      switch (val) {
         case HIGH:
            setBackground(Color.RED);
            break;
         case LOW:
            setBackground(Color.GREEN);
            break;
         case OPEN:
            setBackground(Color.WHITE);
            break;
      }
   }
}
