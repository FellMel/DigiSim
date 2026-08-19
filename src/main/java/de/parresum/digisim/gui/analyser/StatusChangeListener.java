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

/**
 * Simple interface for a "event" mechanism that allows the diagram to update the main windows status bar.
 *
 * @author Kai Uwe Bachmann
 */
public interface StatusChangeListener {
   /**
    * Called when the status bar text needs to change.
    *
    * @param status new status bar text
    */
   public void statusChanged(String status);
}
