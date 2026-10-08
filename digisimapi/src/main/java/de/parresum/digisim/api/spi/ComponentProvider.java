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

package de.parresum.digisim.api.spi;

import java.io.IOException;
import java.util.Map;

import de.parresum.digisim.api.CircuitComponent;
import de.parresum.digisim.api.wire.Wire;

/**
 * A provider for creating and handling library components
 *
 *
 * @author Kai Uwe Bachmann
 */
public interface ComponentProvider {
   /**
    * Gets the name of the library associated with this provider
    *
    * @return the library name
    */
   String getLibName();

   /**
    * Gets the KiCad library file associated with the components supported by this provider
    *
    * @return the library file
    * @throws IOException when an error occours
    */
   String getKiCadLibFile() throws IOException;

   /**
    * checks whether this provider is responsible for the component type
    *
    * @param name name of the component to check
    * @return true, when responsible
    */
   boolean isResponsible(String name);

   /**
    * Creates a new circuit element and configures it
    *
    * @param libName       library element to create
    * @param componentName component name to create
    * @param wires         map of wires to connect to the component
    * @param parameter     additional parameter to configure the component
    * @return the created component
    */
   CircuitComponent create(String libName, String componentName, Map<String, Wire> wires,
         Map<String, String> parameter);

}
