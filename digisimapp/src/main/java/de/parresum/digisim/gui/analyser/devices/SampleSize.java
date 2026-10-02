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
package de.parresum.digisim.gui.analyser.devices;

/**
 * Known sample sizes
 *
 * @author Kai Uwe Bachmann
 */
public enum SampleSize {
   S32("32", 0x20), //
   S64("64", 0x40), //
   S128("128", 0x80), //
   S256("256", 0x100), //
   S512("512", 0x200), //
   S1K("1K", 0x400), //
   S2K("2K", 0x800), //
   S4K("4K", 0x1000), //
   S8K("8K", 0x2000), //
   S16K("16K", 0x4000), //
   S32K("32K", 0x8000), //
   S64K("64K", 0x10000), //
   S128K("128K", 0x20000), //
   S256K("256K", 0x40000); //

   public final String name;
   public final int samples;

   SampleSize(final String name, final int samples) {
      this.name = name;
      this.samples = samples;
   }

   @Override
   public String toString() {
      return name;
   }
}
