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

import java.util.Properties;

/**
 * This interface defines the methods required to make (UI) object states controllable by the project mechanism.
 * <p>
 * Its methods are called by {@@link Project} when storing or loading the project state. A project state is the
 * collection of states of all user configurable items.
 * <p>
 * Note: When defining property values it should be kept in mind that the project configuration file should be
 * understandable and editable by users. Use common sense to determine wheter a particular setting should be part of the
 * project configuration or not. For key naming conventions please look at an actual configuration file.
 *
 * @author Kai Uwe Bachmann
 */
public interface Configurable {
   /**
    * Reads configuration from given properties. UI element settings must be modified according to the properties found.
    *
    * @param properties properties to read configuration from
    */
   public void readProperties(Properties properties);

   /**
    * Writes configuration to given properties. Properties must be set according to the UI element settings.
    *
    * @param properties properties to write configuration to
    */
   public void writeProperties(Properties properties);
}
