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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JDialog;

/**
 * Base for analyzer dialogs
 *
 * @author Kai Uwe Bachmann
 */
public abstract class AbstractDialog extends JComponent {
   private static final long serialVersionUID = -2945437181329298674L;

   /** the user cancelled the dialog - all changes were discarded */
   public final static int CANCEL = 0;

   /** the user clicked ok - all changes were written to the settings */
   public final static int OK = 1;

   protected JDialog dialog;

   private int result;

   protected static GridBagConstraints createConstraints(int x, int y, int w, int h, double wx, double wy) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH;
      gbc.insets = new Insets(4, 4, 4, 4);
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = w;
      gbc.gridheight = h;
      gbc.weightx = wx;
      gbc.weighty = wy;
      return (gbc);
   }

   /**
    * Internal method that initializes a dialog and add this component to it.
    *
    * @param frame owner of the dialog
    */
   protected void initDialog(Frame frame) {
      // check if dialog exists with different owner and dispose if so
      if (dialog != null && dialog.getOwner() != frame) {
         dialog.dispose();
         dialog = null;
      }
      // if no valid dialog exists, create one
      if (dialog == null) {
         dialog = new JDialog(frame, getTitle(), true);
         dialog.getContentPane().add(this);
         dialog.pack();
         dialog.setResizable(false);
      }
   }

   /**
    * Display the settings dialog. If the user clicks ok, all changes are reflected in the properties of this object.
    * Otherwise changes are discarded.
    *
    * @param frame parent frame (needed for creating a modal dialog)
    * @return <code>OK</code> when user accepted changes, <code>CANCEL</code> otherwise
    */
   public int showDialog(Frame frame) {
      initDialog(frame);
      updateFields();
      result = CANCEL;
      dialog.setVisible(true);
      return (result);
   }

   protected void doOk() {
      readFields();
      result = OK;
      dialog.setVisible(false);
   }

   protected void doCancel() {
      result = CANCEL;
      dialog.setVisible(false);
   }

   protected abstract String getTitle();

   protected abstract void updateFields();

   protected abstract void readFields();

}
