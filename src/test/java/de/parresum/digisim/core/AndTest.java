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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.parresum.digisim.core.gates.And;
import de.parresum.digisim.core.wire.Wire;

public class AndTest {

   @Test
   public void gateTest2() {
      final Wire out = new Wire("out");
      final Wire a = new Wire("a");
      final Wire b = new Wire("b");
      final And and = new And("test", out, a, b);

      a.set(State.LOW);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "low,low");

      // -------------
      a.set(State.LOW);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "low,high");

      // -------------
      a.set(State.HIGH);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "high,low");

      // -------------
      a.set(State.HIGH);
      b.set(State.HIGH);

      assertEquals(State.HIGH, out.get(), "high,high");

   }

   @Test
   public void gateTest4() {
      final Wire out = new Wire("out");
      final Wire a = new Wire("a");
      final Wire b = new Wire("b");
      final Wire c = new Wire("c");
      final Wire d = new Wire("d");
      final And and = new And("test", out, a, b, c, d);

      // -------------
      a.set(State.LOW);
      b.set(State.LOW);
      a.set(State.LOW);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "0");

      // -------------
      a.set(State.LOW);
      b.set(State.LOW);
      a.set(State.LOW);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "1");
      // -------------
      a.set(State.LOW);
      b.set(State.LOW);
      a.set(State.HIGH);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "2");
      // -------------
      a.set(State.LOW);
      b.set(State.LOW);
      a.set(State.HIGH);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "3");
      // -------------
      a.set(State.LOW);
      b.set(State.HIGH);
      a.set(State.LOW);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "4");
      // -------------
      a.set(State.LOW);
      b.set(State.HIGH);
      a.set(State.LOW);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "5");
      // -------------
      a.set(State.LOW);
      b.set(State.HIGH);
      a.set(State.HIGH);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "6");
      // -------------
      a.set(State.LOW);
      b.set(State.HIGH);
      a.set(State.HIGH);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "7");
      // -------------
      a.set(State.HIGH);
      b.set(State.LOW);
      a.set(State.LOW);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "8");
      // -------------
      a.set(State.HIGH);
      b.set(State.LOW);
      a.set(State.LOW);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "9");
      // -------------
      a.set(State.HIGH);
      b.set(State.LOW);
      a.set(State.HIGH);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "A");
      // -------------
      a.set(State.HIGH);
      b.set(State.LOW);
      a.set(State.HIGH);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "B");
      // -------------
      a.set(State.HIGH);
      b.set(State.HIGH);
      a.set(State.LOW);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "C");
      // -------------
      a.set(State.HIGH);
      b.set(State.HIGH);
      a.set(State.LOW);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "D");
      // -------------
      a.set(State.HIGH);
      b.set(State.HIGH);
      a.set(State.HIGH);
      b.set(State.LOW);

      assertEquals(State.LOW, out.get(), "E");
      // -------------
      a.set(State.HIGH);
      b.set(State.HIGH);
      a.set(State.HIGH);
      b.set(State.HIGH);

      assertEquals(State.LOW, out.get(), "F");
      // -------------

   }

}
