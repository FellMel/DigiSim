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

package de.parresum.digisim.app;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility to handle Icons
 *
 * @author Kai Uwe Bachmann
 */
public class IconCreator {
   private static final Logger LOG = LogManager.getLogger(IconCreator.class);

   private static final Map<String, Image> CACHE = new HashMap<>();

   private IconCreator() {
   }

   /**
    * Creates an icon from a resource
    *
    * @param name name of the resource
    * @return the icon
    */
   public static Icon create(String name) {
      return new ImageIcon(createImage(name));
   }

   /**
    * Creates an image from a resource
    *
    * @param name name of the resource
    * @return the image
    */
   public static Image createImage(String name) {
      return CACHE.computeIfAbsent(name, (n) -> getImage(n));
   }

   private static BufferedImage getImage(String name) {
      try {
         BufferedImage image = getImageOrNull(name);
         if (image == null) {
            throw new NullPointerException("resource " + name + " not found!");
         }
         return image;
      } catch (IOException e) {
         LOG.error("Can't load image {}", name, e);
         throw new RuntimeException("Image " + name + " not found", e);
      }
   }

   private static BufferedImage getImageOrNull(String name) throws IOException {
      URL systemResource = Thread.currentThread().getContextClassLoader().getResource("icons/" + name);
      if (systemResource == null) {
         return null;
      }
      return ImageIO.read(systemResource);
   }

   /**
    * Creates an image list from a resource
    *
    * @param names names of the resource
    * @return the image
    */
   public static ArrayList<Image> createImages(String... names) {
      ArrayList<Image> list = new ArrayList<Image>(names.length);
      for (String name : names) {
         list.add(createImage(name));
      }
      return list;
   }
}
