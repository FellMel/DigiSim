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

package de.parresum.digisim.app.i18n;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.parresum.digisim.app.AppPrefferences;

/**
 *
 *
 * @author Kai Uwe Bachmann
 */
public class LanguageManager {
   private static final Logger LOG = LogManager.getLogger(LanguageManager.class);

   private static final String LANGUAGE = "language";

   private static Language CURRENT = new Language();

   private static final String OS = System.getProperty("os.name").toLowerCase();
   private static final boolean IS_MAC = OS.contains("mac");

   static {
      AppPrefferences.setPref(LANGUAGE, "default");

   }

   /**
    * Sets the GUI language
    *
    * @param language the language
    */
   public static void setLanguage(Language language) {
      AppPrefferences.setPref(LANGUAGE, language.getName());
   }

   /**
    * gets an internationalized string
    *
    * @param key    the key
    * @param params optional parameters
    * @return the internationalized string of key if no translation present
    */
   public static String get(String key) {
      return CURRENT.getString(key);
   }

   public static String get(String key, Object... params) {
      String text = CURRENT.getString(key);
      return String.format(text, params);
   }

   public static String getOrNull(String key) {
      return CURRENT.getString(key);
   }

   public static String getOrNull(String key, Object... params) {
      String text = CURRENT.getString(key);
      if (text == null) {
         return null;
      }
      return String.format(text, params);
   }

   public static Action getAction(String key, Action action) {
      String text = CURRENT.getString(key + ".name");
      action.putValue(Action.NAME, text);

      String tooltip = CURRENT.getStringOrNull(key + ".tooltip");
      if (tooltip != null) {
         action.putValue(Action.SHORT_DESCRIPTION, tooltip);
      }

      String description = CURRENT.getStringOrNull(key + ".description");
      if (description != null) {
         action.putValue(Action.LONG_DESCRIPTION, description);
      }

      String icon = CURRENT.getStringOrNull(key + ".icon");
      if (icon != null && !icon.isBlank()) {
         action.putValue(Action.SMALL_ICON, icon);
      }

      String accelerator = CURRENT.getStringOrNull(key + ".accelerator");
      if (accelerator != null && !accelerator.isBlank()) {
         accelerator = translateAccelerator(accelerator);
         action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
      }

      String mnemonic = CURRENT.getStringOrNull(key + ".mnemonic");
      if (mnemonic != null && !mnemonic.isBlank()) {

         action.putValue(Action.MNEMONIC_KEY, stringToKeyEvent(mnemonic));
      }

      return action;
   }

   private static String translateAccelerator(String keycode) {
      if (IS_MAC) {
         keycode = keycode.replace("control", "meta");
      }
      return keycode;
   }

   private static Integer stringToKeyEvent(String name) {
      try {
         Field field = KeyEvent.class.getDeclaredField(name);
         if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
            field.setAccessible(true);
            return field.getInt(null);
         }
      } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
         LOG.error("Can't convert {} to KeyEvent", name, e);
      }

      return null;
   }

   /**
    * @return the current language
    */
   public static Language currentLanguage() {
      return CURRENT;
   }

}
