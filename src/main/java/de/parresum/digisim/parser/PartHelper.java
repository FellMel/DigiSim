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

package de.parresum.digisim.parser;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.core.wire.Wire;
import de.parresum.digisim.model.NetPart;
import de.parresum.digisim.model.NetValue;
import de.parresum.digisim.parser.accessor.PartCreator;
import de.parresum.digisim.parser.accessor.PartDescriptor;
import de.parresum.digisim.parser.accessor.PinAccessor;
import de.parresum.digisim.parser.accessor.ValueAccessor;

/**
 * Helper to handle circuit Parts
 *
 * @author Kai Uwe Bachmann
 */
public class PartHelper {
   private final static Logger LOG = LogManager.getLogger(PartHelper.class);

   /** map to store known part descriptors */
   private final static Map<String, PartDescriptor> descriptors = new HashMap<>();
   static {
      try {
         LOG.info("Start searching parts ...");
         // TODO: implement a configuration service to extend with new parts
         List<Class<? extends CircuitPart>> pkg = getClasses("de.parresum.digisim.core");
         for (Class<? extends CircuitPart> cls : pkg) {
            Part[] parts = (Part[]) cls.getAnnotationsByType(Part.class);
            if (parts.length > 0) {
               PartDescriptor desc = new PartDescriptor(cls);
               for (Part part : parts) {
                  descriptors.put(part.value(), desc);
                  LOG.info("Found part {} in class {}", part.value(), cls.getName());

               }
               for (String e : desc.getPins().keySet()) {
                  LOG.info("   Pin {}", e);
               }
            }
         }
      } catch (ClassNotFoundException | IntrospectionException | IOException | URISyntaxException e) {
         LOG.error("Error while parsing classes", e);
      }
   }

   /**
    * gets a list of all CircuitPart classes within tge given package and its subpackages
    *
    * @param pckgname package to parse
    * @return list of classes found
    * @throws ClassNotFoundException when an error occours
    * @throws IOException
    * @throws URISyntaxException
    */
   private static List<Class<? extends CircuitPart>> getClasses(String pckgname)
         throws ClassNotFoundException, IOException, URISyntaxException {

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      ArrayList<String> names = new ArrayList<String>();

      String path = pckgname.replace(".", "/");
      URL packageURL = classLoader.getResource(path);

      if (packageURL.getProtocol().equals("jar")) {
         ArrayList<Class<? extends CircuitPart>> classes = new ArrayList<>();

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
                  if (CircuitPart.class.isAssignableFrom(candidate)) {
                     classes.add((Class<? extends CircuitPart>) candidate);
                  }
               }
            }
         }
         return classes;

      } else {
         // loop through files in classpath
         URI uri = new URI(packageURL.toString());
         File folder = new File(uri.getPath());
         return getFileClasses(pckgname, folder);
      }
   }

   private static List<Class<? extends CircuitPart>> getFileClasses(String pckgname, File folder)
         throws ClassNotFoundException {
      ArrayList<Class<? extends CircuitPart>> classes = new ArrayList<>();
      File[] content = folder.listFiles();
      for (File actual : content) {
         if (actual.isDirectory()) {
            classes.addAll(getFileClasses(pckgname + "." + actual.getName(), actual));
         } else {
            String entryName = actual.getName();
            // we are only interested in .class files
            if (entryName.endsWith(".class")) {
               // removes the .class extension
               Class<?> candidate = Class.forName(pckgname + '.' + entryName.substring(0, entryName.length() - 6));
               if (CircuitPart.class.isAssignableFrom(candidate)) {
                  classes.add((Class<? extends CircuitPart>) candidate);
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
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws IllegalArgumentException
    * @throws InvocationTargetException
    */
   public static CircuitPart createPart(NetPart netPart)
         throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      PartDescriptor desc = descriptors.get(netPart.getLib());
      if (desc == null) {
         throw new IllegalArgumentException("Unknown Lib Element: " + netPart.getLib());
      }

      PartCreator creator = desc.getCreator();
      CircuitPart part = creator.create(netPart);

      // initialize values
      for (NetValue val : netPart.getValues()) {
         ValueAccessor accessor = desc.getValues().get(val.getName());
         if (accessor != null) {
            accessor.setValue(part, val.getValue());
         }
      }

      return part;
   }

   /**
    * connects a wire to a circuit part
    *
    * @param wire      wire to connect
    * @param part      part to connect the wire to
    * @param pinNumber pin name to connect the wire to
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   public static void join(Wire wire, CircuitPart part, String pinNumber)
         throws IllegalAccessException, InvocationTargetException {
      PartDescriptor desc = descriptors.get(part.getLibName());

      PinAccessor pinAccess = desc.getPins().get(pinNumber);
      LOG.info("  connection {} on {} : {}", wire.getName(), part.getLibName(), pinNumber);
      pinAccess.setWire(part, wire);
   }

}
