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
package de.parresum.digisim.core;

import static de.parresum.digisim.lib.State.HIGH;
import static de.parresum.digisim.lib.State.LOW;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.parresum.digisim.lib.State;
import de.parresum.digisim.lib.gates.Nand;
import de.parresum.digisim.lib.wire.PullUpWire;
import de.parresum.digisim.lib.wire.Wire;

public class OcTest {

   @Test
   public void ocTest() {

      final Wire a = new Wire("a");
      final Wire b = new Wire("b");
      final Wire c = new Wire("c");
      final Wire d = new Wire("d");

      final PullUpWire out = new PullUpWire("out");

      new Nand("Test", true, out, a, b);
      new Nand("Test", true, out, c, d);

      final State matrix[][] = { //
            { HIGH, LOW, LOW, LOW, LOW }, //
            { HIGH, LOW, LOW, LOW, HIGH }, //
            { HIGH, LOW, LOW, HIGH, LOW }, //
            { LOW, LOW, LOW, HIGH, HIGH }, //

            { HIGH, LOW, HIGH, LOW, LOW }, //
            { HIGH, LOW, HIGH, LOW, HIGH }, //
            { HIGH, LOW, HIGH, HIGH, LOW }, //
            { LOW, LOW, HIGH, HIGH, HIGH }, //

            { HIGH, HIGH, LOW, LOW, LOW }, //
            { HIGH, HIGH, LOW, LOW, HIGH }, //
            { HIGH, HIGH, LOW, HIGH, LOW }, //
            { LOW, HIGH, LOW, HIGH, HIGH }, //

            { LOW, HIGH, HIGH, LOW, LOW }, //
            { LOW, HIGH, HIGH, LOW, HIGH }, //
            { LOW, HIGH, HIGH, HIGH, LOW }, //
            { LOW, HIGH, HIGH, HIGH, HIGH },//
      };

      for (int line = 0; line < matrix.length; line++) {
         a.set(matrix[line][1]);
         b.set(matrix[line][2]);
         c.set(matrix[line][3]);
         d.set(matrix[line][4]);
         assertEquals(matrix[line][0], out.get(), "Line " + line);
      }
   }
}
