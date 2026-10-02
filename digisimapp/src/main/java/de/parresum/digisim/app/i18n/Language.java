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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Definition of a supported language
 *
 * @author Kai Uwe Bachmann
 */
public class Language {
   private final String name;
   private final String displayName;
   private final String filename;

   private ResourceBundle localeBundle;

   /**
    * Creates a new instance
    *
    * @param name the languages name
    */
   public Language(String name) {
      this(name, "", "");
   }

   /**
    * Creates a new instance with the current language
    */
   public Language() {
      this(Locale.getDefault().getLanguage());
   }

   /**
    * Creates new instance
    *
    * @param name        name, eq. "en" or "de"
    * @param displayName the name shown to the user
    * @param filename    a name that contains only ASCII characters
    */
   public Language(String name, String displayName, String filename) {
      this.name = name;
      this.displayName = displayName;
      this.filename = filename;

      Locale locale = Locale.of(name);
      localeBundle = ResourceBundle.getBundle("lang/digisim", locale);
   }

   @Override
   public String toString() {
      return displayName;
   }

   /**
    * returns the name
    *
    * @return the name
    */
   public String getName() {
      return name;
   }

   public String getString(String key) {
      if (localeBundle.containsKey(key)) {
         return localeBundle.getString(key);
      }
      return key;
   }

   public String getStringOrNull(String key) {
      if (localeBundle.containsKey(key)) {
         return localeBundle.getString(key);
      }
      return null;
   }
//   @Override
//   public int compareTo(Language o) {
//       return displayName.compareTo(o.displayName);
//   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      Language language = (Language) o;

      return name != null ? name.equals(language.name) : language.name == null;

   }

   @Override
   public int hashCode() {
      return name != null ? name.hashCode() : 0;
   }

   /**
    * @return a name that contains only ASCII characters
    */
   public String getFileName() {
      return filename;
   }
}
