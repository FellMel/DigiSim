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

/**
 * States, a wire can be in.
 *
 * @author Kai Uwe Bachmann
 */
public enum State {
   /** Low state */
   LOW,

   /** High state */
   HIGH,

   /** Open state of a tri-state wire or an OC-wire */
   OPEN;

   /**
    * inverts the current state
    *
    * @return the inverted state
    */
   public State not() {
      switch (this) {
         case HIGH:
            return LOW;
         case LOW:
            return HIGH;
         case OPEN:
         default:
            return OPEN;
      }
   }
}
