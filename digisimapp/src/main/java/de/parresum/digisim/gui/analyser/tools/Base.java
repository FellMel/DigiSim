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
package de.parresum.digisim.gui.analyser.tools;

import javax.swing.JComboBox;

import de.parresum.digisim.gui.analyser.CapturedData;

/**
 * Abstract base class that may be used for tools.
 * <p>
 * This class is provided for convenience to ease implementing new tools and reduce the redundancy for commonly used
 * methods.
 * <p>
 * For details about the methods required for tools, see {@link Tool} interface.
 *
 * @author Kai Uwe Bachmann
 */
public abstract class Base implements Tool {

   /**
    * Calls <code>process(CapturedData data)</code>.
    */
   @Override
   public CapturedData process(CapturedData data, int group, int channel, int position) {
      return (process(data));
   }

   /**
    * Selects the item of a combo box whose index corresponds to a string array index matching the given value.
    *
    * @param box     combo box where the entry should be selected
    * @param entries list of strings corresponding to combo box entries
    * @param value   value to be searched for in string array and highlighted in combo box
    */
   public void selectByValue(JComboBox<String> box, String[] entries, String value) {
      if (value != null) {
         for (int i = 0; i < entries.length; i++) {
            if (value.equals(entries[i])) {
               box.setSelectedIndex(i);
            }
         }
      }
   }

   /**
    * Selects the first item of the combo box that matches the given string value.
    *
    * @param box   combo box where the entry should be selected
    * @param value value to be searched for in string array and highlighted in combo box
    */
   public void selectByValue(JComboBox<String> box, String value) {
      if (value != null) {
         for (int i = 0; i < box.getItemCount(); i++) {
            if (value.equals(box.getItemAt(i))) {
               box.setSelectedIndex(i);
            }
         }
      }
   }

   /**
    * Selects the item of a combo box at the given index.
    *
    * @param box   combo box where the entry should be selected
    * @param index string containing integer to be used as index for selected item in combo box
    */
   public void selectByIndex(JComboBox<String> box, String index) {
      try {
         box.setSelectedIndex(Integer.parseInt(index));
      } catch (Exception e) {
         /* don't care */
      }
   }
}
