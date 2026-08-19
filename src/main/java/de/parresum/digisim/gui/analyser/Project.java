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
package de.parresum.digisim.gui.analyser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Project maintains a global properties list for all registered objects implementing {@link Configurable}. It also
 * provides methods for loading and storing these properties from and to project configuration files. This allows to
 * keep multiple sets of user settings across multiple instance lifecycles.
 *
 * @author Kai Uwe Bachmann
 */
public class Project {
   private Properties properties;
   private List<Configurable> configurableObjectList;

   /**
    * Constructs a new project with an empty set of properties and configurable objects.
    */
   public Project() {
      this.properties = new Properties();
      this.configurableObjectList = new LinkedList<Configurable>();
   }

   /**
    * Adds a configurable object to the project. The given objects properties will be read and written whenever load and
    * store operations take place.
    *
    * @param configurable configurable object
    */
   public void addConfigurable(Configurable configurable) {
      this.configurableObjectList.add(configurable);
   }

   /**
    * Gets all currently defined properties for this project.
    *
    * @return project properties
    */
   public Properties getProperties() {
      for (Configurable conf : configurableObjectList) {
         conf.writeProperties(properties);
      }
      return (properties);
   }

   /**
    * Loads properties from the given file and notifies all registered configurable objects.
    *
    * @param file file to read properties from
    * @throws IOException when IO operation failes
    */
   public void load(File file) throws IOException {
      InputStream stream = new FileInputStream(file);
      properties.load(stream);
      for (Configurable conf : configurableObjectList) {
         conf.readProperties(properties);
      }
   }

   /**
    * Stores properties fetched from all registered configurable objects in the given file.
    *
    * @param file file to store properties in
    * @throws IOException when IO operation failes
    */
   public void store(File file) throws IOException {
      // creating new properties object will remove alien properties read from broken / old project files
      properties = new Properties();
      for (Configurable conf : configurableObjectList) {
         conf.writeProperties(properties);
      }
      OutputStream stream = new FileOutputStream(file);
      properties.store(stream, "UnitedSoft Analyser Project File");
   }

}
