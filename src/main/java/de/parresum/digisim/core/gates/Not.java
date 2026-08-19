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
package de.parresum.digisim.core.gates;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.core.Out;
import de.parresum.digisim.core.State;
import de.parresum.digisim.core.wire.Wire;

/**
 * NOT-Gate
 *
 * @author Kai Uwe Bachmann
 */
@Part("NOT")
@Part("digisim:Not")
public class Not extends AbstractGate {

   public static final String TYPE_NAME = "NOT";

   public Not(final String name, final Wire output, final Wire inputs) {
      super(name, output, inputs);
      register("NOT");
   }

   public Not(final String name, final Out gate) {
      super(name, gate.getOutput());
      register("NOT");
   }

   public Not(final String name) {
      super(name);
   }

   @Override
   public void addInput(final Wire input) {
      if (!inputs.isEmpty()) {
         throw new IllegalStateException("Multiple inputs for Not");
      }
      super.addInput(input);
   }

   @Override
   protected State calculate() {

      final State current = inputs.getFirst().get();
      return current.not();

   }

}
