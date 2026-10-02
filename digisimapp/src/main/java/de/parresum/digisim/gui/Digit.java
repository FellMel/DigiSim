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
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.border.LineBorder;

/**
 * An output element for 4 bit
 *
 * @author Kai Uwe Bachmann
 */
public class Digit extends JLabel {

   private static final long serialVersionUID = -1994324882043952592L;
   /** value mapping */
   private final String VALUES[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

   /** current value */
   private int value;

   /**
    * Creates a digit
    */
   public Digit() {
      super();
      setBorder(new LineBorder(Color.BLUE));
      setFont(new Font("Courier", Font.PLAIN, 24));
      setValue(0);
   }

   /**
    * Sets the value to display
    *
    * @param val new value
    */
   public void setValue(final int val) {
      if (val < 0 || val >= 16) {
         super.setText("-");
         throw new IllegalStateException("Ungültiger Wert");
      }

      this.value = val;
      super.setText(VALUES[val]);
   }
}
