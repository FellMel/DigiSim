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

import de.parresum.digisim.gui.MainWindow;
import de.parresum.digisim.lib.gates.Not;
import de.parresum.digisim.lib.io.Input;
import de.parresum.digisim.lib.io.Output;
import de.parresum.digisim.lib.wire.Wire;

public class NotExample extends MainWindow {

   private static final long serialVersionUID = -6984052904055636465L;

   public NotExample() throws HeadlessException {
      super("NOT-Example");
   }

   @Override
   protected void setup() {
      final Wire a = new Wire("a");
      final Wire out = new Wire("out");

      final Input i1 = new Input(a);

      final Output o = new Output(out);

      final Not gate = new Not(null, out, a);

      this.input.addInput(i1);
      this.output.addOutput(o);
   }

   public static void main(final String... args) {
      new NotExample();
   }
}
