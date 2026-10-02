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

import de.parresum.digisim.annotations.PartConnector;
import de.parresum.digisim.lib.wire.Wire;

/**
 * Base for all parts which can be put to a circuit
 *
 * @author Kai Uwe Bachmann
 */
public interface CircuitPart {

   /**
    * gets the name of lib, where the part comes from
    *
    * @return name of the lib
    */
   public String getLibName();

   /**
    * sets the name of the lib, where the part comes from
    *
    * @param libName
    */
   public void setLibName(String libName);

   public void joinWire(Wire wire, String pinNumber, PartConnector connector);
}
