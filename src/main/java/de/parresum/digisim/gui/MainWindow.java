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
package de.parresum.digisim.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Base Window for simulation
 *
 * @author Kai Uwe Bachmann
 */
public abstract class MainWindow extends JFrame {

   private static final long serialVersionUID = 6981044264914065260L;

   /** Panel holding input elements */
   protected final InputPanel input = new InputPanel();

   /** Panel holding output elements */
   protected final OutputPanel output = new OutputPanel();

   /**
    * Creates the window
    *
    * @param title title of the window
    * @throws HeadlessException
    */
   public MainWindow(final String title) throws HeadlessException {
      super(title);

      setSize(400, 400);
      setLayout(new BorderLayout());

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(final WindowEvent windowEvent) {
            System.exit(0);
         }
      });

      this.add(input, BorderLayout.NORTH);
      this.add(output, BorderLayout.SOUTH);

      setup();

      setVisible(true);
   }

   /**
    * creates the content of the window
    */
   protected abstract void setup();

}
