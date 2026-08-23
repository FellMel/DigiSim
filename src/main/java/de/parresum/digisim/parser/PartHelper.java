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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.annotations.Part;
import de.parresum.digisim.core.CircuitPart;
import de.parresum.digisim.core.wire.Wire;
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
      } catch (ClassNotFoundException | IntrospectionException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * gets a list of all CircuitPart classes within tge given package and its subpackages
    *
    * @param pckgname package to parse
    * @return list of classes found
    * @throws ClassNotFoundException when an error occours
    */
   private static List<Class<? extends CircuitPart>> getClasses(String pckgname) throws ClassNotFoundException {

      ArrayList<Class<? extends CircuitPart>> classes = new ArrayList<>();
      // Get a File object for the package
      File directory = null;
      try {
         ClassLoader cld = Thread.currentThread().getContextClassLoader();
         if (cld == null) {
            throw new ClassNotFoundException("Can't get class loader.");
         }

         String path = pckgname.replace('.', '/');

         URL resource = cld.getResource(path);
         if (resource == null) {
            throw new ClassNotFoundException("No resource for " + path);
         }

         directory = new File(resource.getFile());
      } catch (NullPointerException x) {
         throw new ClassNotFoundException(pckgname + " (" + directory + ") does not appear to be a valid package");
      }

      if (directory.exists()) {
         // Get the list of the files contained in the package
         File[] files = directory.listFiles();
         for (File file : files) {
            if (file.isFile()) {
               String fileName = file.getName();
               // we are only interested in .class files
               if (fileName.endsWith(".class")) {
                  // removes the .class extension
                  Class<?> candidate = Class.forName(pckgname + '.' + fileName.substring(0, fileName.length() - 6));
                  if (CircuitPart.class.isAssignableFrom(candidate)) {
                     classes.add((Class<? extends CircuitPart>) candidate);
                  }
               }
            } else if (file.isDirectory()) {
               classes.addAll(getClasses(pckgname + '.' + file.getName()));
            }
         }

      } else {
         throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
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
