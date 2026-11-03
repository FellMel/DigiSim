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

package de.parresum.digisim.lib.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.api.CircuitComponent;
import de.parresum.digisim.api.spi.ComponentBinder;
import de.parresum.digisim.api.spi.ComponentProvider;
import de.parresum.digisim.api.wire.Wire;

/**
 * Service Provider for standard digisim library
 *
 * @author Kai Uwe Bachmann
 */
public class LibComponentProviderImpl implements ComponentProvider {
   private final static Logger LOG = LogManager.getLogger(LibComponentProviderImpl.class);

   private final static String LIB_NAME = "digisim";

   private final ComponentBinder binder;

   public LibComponentProviderImpl() {
      binder = new ComponentBinder("de.parresum.digisim.lib", this.getClass());
   }

   @Override
   public String getLibName() {
      return LIB_NAME;
   }

   @Override
   public String getKiCadLibFile() throws IOException {
      InputStream infile = this.getClass().getClassLoader().getResourceAsStream("/kicad/" + LIB_NAME + ".kicad_sym");

      if (infile == null) {
         throw new FileNotFoundException("Can't find library file");
      }
      return IOUtils.toString(infile, StandardCharsets.UTF_8);
   }

   @Override
   public boolean isResponsible(String name) {
      return name.startsWith(LIB_NAME + ":");
   }

   @Override
   public CircuitComponent create(String libName, String componentName, Map<String, Wire> wires,
         Map<String, String> parameter) {
      String baseName = libName;
      if (libName.startsWith(LIB_NAME + ":")) {
         baseName = libName.substring(LIB_NAME.length() + 1);
      }

      try {
         CircuitComponent component = binder.createComponent(baseName, componentName, wires, parameter);
         component.setLibName(libName);
         return component;
      } catch (ReflectiveOperationException e) {
         LOG.error("can't create component of type {}", libName, e);
         throw new IllegalStateException("can't create component of type " + libName, e);
      }
   }

}
