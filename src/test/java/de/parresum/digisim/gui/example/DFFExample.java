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
package de.parresum.digisim.gui.example;

import java.awt.HeadlessException;

import de.parresum.digisim.core.flipflop.DFlipFlop;
import de.parresum.digisim.core.io.Input;
import de.parresum.digisim.core.io.Output;
import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.gui.MainWindow;

public class DFFExample extends MainWindow {

   private static final long serialVersionUID = -8782067128027113096L;

   public DFFExample() throws HeadlessException {
      super("DFF-Example");
   }

   @Override
   protected void setup() {
      final Wire wset = new Wire("Set");
      final Wire wreset = new Wire("Clear");
      final Wire wd = new Wire("d");
      final Wire wclk = new Wire("clk");
      final Wire out = new Wire("out");

      final Input set = new Input("Set", wset);
      final Input reset = new Input("Reset", wreset);
      final Input d = new Input("D", wd);
      final Input clk = new Input("Clk", wclk);

      final Output o = new Output("out", out);

      final DFlipFlop gate = new DFlipFlop("dff");

      gate.setPrn(wset);
      gate.setClrn(wreset);
      gate.setD(wd);
      gate.setClk(wclk);

      gate.setOutput(out);

      this.input.addInput(reset);
      this.input.addInput(set);
      this.input.addInput(clk);
      this.input.addInput(d);
      this.output.addOutput(o);
   }

   public static void main(final String... args) {
      new DFFExample();
   }
}
