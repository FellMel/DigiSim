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

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.api.CircuitComponent;
import de.parresum.digisim.api.annotations.Component;
import de.parresum.digisim.api.spi.accessor.ComponentCreator;
import de.parresum.digisim.api.spi.accessor.ComponentDescriptor;
import de.parresum.digisim.api.spi.accessor.PinAccessor;
import de.parresum.digisim.api.spi.accessor.ValueAccessor;
import de.parresum.digisim.api.wire.Wire;

/**
 * Helper to handle circuit components
 *
 * @author Kai Uwe Bachmann
 */
public class ComponentBinder {
   private final static Logger LOG = LogManager.getLogger(ComponentBinder.class);

   /** map to store known part descriptors */
   private final Map<String, ComponentDescriptor> descriptors = new HashMap<>();

   public ComponentBinder(String basepath) {
      this(basepath, null);
   }

   /**
    * Creates a new binder
    *
    * @param basepath path to search for components in
    * @param anchor   anchor class to find the right jar-file / class-folder
    */
   public ComponentBinder(String basepath, Class<?> anchor) {
      try {
         LOG.info("Start searching parts at {} ...", basepath);
         List<Class<? extends CircuitComponent>> pkg = getClasses(basepath, anchor);
         for (Class<? extends CircuitComponent> cls : pkg) {
            Component[] parts = (Component[]) cls.getAnnotationsByType(Component.class);
            if (parts.length > 0) {
               for (Component part : parts) {
                  ComponentDescriptor desc = new ComponentDescriptor(cls, part.value());
                  descriptors.put(part.value(), desc);
                  LOG.info("Found part {} in class {}", part.value(), cls.getName());
                  LOG.info("Component {}", part.value());
                  for (String e : desc.getPins().keySet()) {
                     LOG.info("   Pin {}", e);
                  }

               }
            }
         }
      } catch (ClassNotFoundException | IntrospectionException | IOException | URISyntaxException e) {
         LOG.error("Error while parsing classes", e);
      }
   }

   /**
    * gets a list of all CircuitPart classes within the given package and its sub packages
    *
    * @param pckgname package to parse
    * @return list of classes found
    * @throws ClassNotFoundException when an error occurs
    * @throws IOException
    * @throws URISyntaxException
    */
   private List<Class<? extends CircuitComponent>> getClasses(String pckgname, Class<?> anchor)
         throws ClassNotFoundException, IOException, URISyntaxException {

      LOG.debug("parsing path {}", pckgname);

      List<Class<? extends CircuitComponent>> result = new ArrayList<>();

      String path = pckgname.replace(".", "/");
      URL packageURL;
      if (anchor != null) {
         // little hack.
         // without the anchor we get some random URL, when the path is in more than one JAR-File/Folder (e.g. test
         // folder)
         String baseName = anchor.getCanonicalName().replace(".", "/");
         ClassLoader classLoader = anchor.getClassLoader();
         packageURL = classLoader.getResource(baseName + ".class");
      } else {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         packageURL = classLoader.getResource(path);
      }
      LOG.debug("  found: {}", packageURL);

      if (packageURL.getProtocol().equals("jar")) {

         // build jar file name, then loop through zipped entries
         String jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
         jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));

         try (JarFile jf = new JarFile(jarFileName)) {
            Enumeration<JarEntry> jarEntries = jf.entries();

            while (jarEntries.hasMoreElements()) {
               String entryName = jarEntries.nextElement().getName();
               if (entryName.startsWith(path) && entryName.endsWith(".class")) {

                  entryName = entryName.substring(0, entryName.lastIndexOf('.'));
                  // we are only interested in .class files
                  // removes the .class extension
                  Class<?> candidate = Class.forName(entryName.replace("/", "."));
                  if (CircuitComponent.class.isAssignableFrom(candidate)) {
                     result.add((Class<? extends CircuitComponent>) candidate);
                  }
               }
            }
         }
      } else {
         // loop through files in classpath
         URI uri = new URI(packageURL.toString());
         File folder = new File(uri.getPath());

         // When URL is recognized by anchor, we need to reduce to basepath ...
         while (!folder.getAbsolutePath().endsWith(path)) {
            folder = folder.getParentFile();
         }

         result.addAll(getFileClasses(pckgname, folder));
      }
      return result;
   }

   private List<Class<? extends CircuitComponent>> getFileClasses(String pckgname, File folder)
         throws ClassNotFoundException {
      ArrayList<Class<? extends CircuitComponent>> classes = new ArrayList<>();
      LOG.debug("parsing file system {}", folder);
      File[] content = folder.listFiles();
      LOG.debug("found {} entries", content.length);

      for (File actual : content) {
         if (actual.isDirectory()) {
            LOG.debug("  parsing folder {}", actual.getName());
            classes.addAll(getFileClasses(pckgname + "." + actual.getName(), actual));
         } else {
            LOG.debug("  parsing file {}", actual.getName());
            String entryName = actual.getName();
            // we are only interested in .class files
            if (entryName.endsWith(".class")) {
               // removes the .class extension
               Class<?> candidate = Class.forName(pckgname + '.' + entryName.substring(0, entryName.length() - 6));
               if (CircuitComponent.class.isAssignableFrom(candidate)) {
                  classes.add((Class<? extends CircuitComponent>) candidate);
               }
            }
         }
      }
      return classes;
   }

   /**
    * Creates a new CircuitPart by its net definition
    *
    * @param netPart definition to create the part from
    * @return the created part
    * @throws ReflectiveOperationException
    * @throws IllegalArgumentException
    */
   public CircuitComponent createComponent(String libElement, String elementName, Map<String, Wire> wires,
         Map<String, String> parameter) throws ReflectiveOperationException {
      ComponentDescriptor desc = descriptors.get(libElement);
      if (desc == null) {
         throw new IllegalArgumentException("Unknown Lib Element: " + libElement);
      }

      ComponentCreator creator = desc.getCreator();
      CircuitComponent part = creator.create(elementName);

      for (Entry<String, Wire> wire : wires.entrySet()) {
         PinAccessor accessor = desc.getPin(wire.getKey());
         // TODO: check for special wire types ...

         if (accessor == null) {
            LOG.error("Unknown pin {} for element {} ({}). Ignoring", wire.getKey(), elementName, libElement);
         } else {
            accessor.setWire(part, wire.getValue());
         }
      }

      // initialize values
      if (parameter != null) {
         for (Entry<String, String> param : parameter.entrySet()) {
            ValueAccessor accessor = desc.getValues().get(param.getKey());
            if (accessor != null) {
               accessor.setValue(part, param.getValue());
            } else {
               LOG.warn("Unknown value {} for element {} ({}). Ignoring", param.getKey(), elementName, libElement);
            }
         }
      }

      part.connected();

      return part;
   }

}
