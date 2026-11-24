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

package de.parresum.digisim.lib;

import de.parresum.digisim.api.AbstractComponent;
import de.parresum.digisim.api.State;
import de.parresum.digisim.api.annotations.Component;
import de.parresum.digisim.api.annotations.Pin;
import de.parresum.digisim.api.annotations.PortType;
import de.parresum.digisim.api.wire.PullUpWire;
import de.parresum.digisim.api.wire.Wire;

/**
 * Simple HIGH-Output component
 *
 * @author Kai Uwe Bachmann
 */
@Component("VCC")
public class Vcc extends AbstractComponent implements Out {
   private Wire output;

   public Vcc(final String name) {
      super(name);
   }

   @Pin(value = "1", type = PortType.OUTPUT)
   public void setOutput(final Wire output) {
      if (this.output != null) {
         throw new IllegalStateException("Double output on wire");
      }
      if (output instanceof PullUpWire) {
         this.output = new Wire(output.toString());
         output.join(this.output);
      } else {
         this.output = output;
      }
      this.output.set(State.HIGH);

   }

   @Override
   public Wire getOutput() {
      if (this.output == null) {
         setOutput(new Wire(this + "-out"));
      }
      return output;
   }

   @Override
   public void connected() {
      getOutput().set(State.HIGH);
   }

}
