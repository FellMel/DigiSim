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

import java.util.prefs.Preferences;

/**
 * Handler to manage global application settings
 *
 * @author Kai Uwe Bachmann
 */
public class AppPrefferences {
   private static final Preferences PREFS = Preferences.userRoot().node("de/parresum/digiSim");

   public static void setPref(String key, String value) {
      PREFS.put(key, value);
   }

   public static String getPref(String key, String def) {
      return PREFS.get(key, def);
   }
}
