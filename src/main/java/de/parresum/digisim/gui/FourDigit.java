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

/**
 * Display for 16 bit
 *
 * @author Kai Uwe Bachmann
 */
public class FourDigit extends Digit {

   private static final long serialVersionUID = -2753986327231432716L;
   private int value;

   public FourDigit() {
      super();
   }

   @Override
   public void setValue(final int val) {
      if (val < 0 || val >= 0x10000) {
         super.setText("-");
         throw new IllegalStateException("Ungültiger Wert");
      }

      this.value = val;
      super.setText(String.format("%04X", val));
   }
}