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

package de.parresum.digisim.parser;

import java.util.HashMap;
import java.util.Map;

import de.parresum.kicad.parser.eescheme.Pin;
import de.parresum.kicad.parser.eescheme.Symbol;

/**
 * Extracted symbol definition from lib
 *
 * @author Kai Uwe Bachmann
 */
public class LibPart {

   /** name of the symbol */
   private final String name;

   /** pins of the symbol */
   private final Map<String, LibPin> pins = new HashMap<>();

   public LibPart(Symbol symbol) {
      name = symbol.getName();

      parsePins(symbol);
   }

   private void parsePins(Symbol symbol) {
      if (symbol.getPins() != null) {
         for (Pin item : symbol.getPins()) {
            LibPin libPin = new LibPin(item);
            pins.put(libPin.getNumber(), libPin);
         }
      }

      if (symbol.getSymbols() != null) {
         for (Symbol sub : symbol.getSymbols()) {
            parsePins(sub);
         }
      }

   }

   public String getName() {
      return name;
   }

   public LibPin getPin(String number) {
      return pins.get(number);
   }

}
