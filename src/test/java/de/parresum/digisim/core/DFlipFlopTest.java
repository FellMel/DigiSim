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

import static de.parresum.digisim.core.State.HIGH;
import static de.parresum.digisim.core.State.LOW;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.parresum.digisim.core.flipflop.DFlipFlop;
import de.parresum.digisim.core.wire.Wire;

public class DFlipFlopTest {

   private final DFlipFlop dff = new DFlipFlop("");
   private final Wire clk = new Wire("clk");
   private final Wire d = new Wire("d");
   private final Wire pr = new Wire("pr");
   private final Wire clr = new Wire("clr");

   private final Wire out = new Wire("out");
   private final Wire nout = new Wire("/out");

   @BeforeEach
   public void init() {
      pr.set(HIGH);
      clr.set(HIGH);

      dff.setOutput(out);
      dff.setOutputn(nout);

      dff.setD(d);
      dff.setClk(clk);
      dff.setClrn(clr);
      dff.setPrn(pr);

      dff.initState();

   }

   @org.junit.jupiter.api.Test
   public void normalTest() {
      // -------------

      d.set(HIGH);
      assertEquals(LOW, out.get());
      assertEquals(HIGH, nout.get());

      clk.set(HIGH);
      assertEquals(HIGH, out.get());
      assertEquals(LOW, nout.get());
      clk.set(LOW);

      d.set(LOW);
      assertEquals(HIGH, out.get());
      assertEquals(LOW, nout.get());

      clk.set(HIGH);
      assertEquals(LOW, out.get());
      assertEquals(HIGH, nout.get());
      clk.set(LOW);
   }

   @Test
   public void setResetTest() {
      // -------------
      pr.set(LOW);
      assertEquals(HIGH, out.get());
      assertEquals(LOW, nout.get());

      d.set(HIGH);
      assertEquals(HIGH, out.get());
      assertEquals(LOW, nout.get());

      clk.set(HIGH);
      assertEquals(HIGH, out.get());
      assertEquals(LOW, nout.get());
      clk.set(LOW);

      d.set(LOW);
      assertEquals(HIGH, out.get());
      assertEquals(LOW, nout.get());

      clk.set(HIGH);
      assertEquals(HIGH, out.get());
      assertEquals(LOW, nout.get());
      clk.set(LOW);
      pr.set(HIGH);

      // -------------
      clr.set(LOW);
      assertEquals(LOW, out.get());
      assertEquals(HIGH, nout.get());

      d.set(HIGH);
      assertEquals(LOW, out.get());
      assertEquals(HIGH, nout.get());

      clk.set(HIGH);
      assertEquals(LOW, out.get());
      assertEquals(HIGH, nout.get());
      clk.set(LOW);

      d.set(LOW);
      assertEquals(LOW, out.get());
      assertEquals(HIGH, nout.get());

      clk.set(HIGH);
      assertEquals(LOW, out.get());
      assertEquals(HIGH, nout.get());
      clk.set(LOW);
      clr.set(LOW);
   }

}
