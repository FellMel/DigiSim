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

import de.parresum.digisim.lib.wire.Wire;

/**
 * Listener to react on state changes of a wire
 *
 * @author Kai Uwe Bachmann
 */
public interface StateListener {
   /**
    * State of a wire is changed
    *
    * @param src      wire triggering the change
    * @param oldState old state
    * @param newState new state
    */
   public void stateChanged(Wire src, State oldState, State newState);
}
