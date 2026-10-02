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
package de.parresum.digisim.gui.analyser.tools;

import java.awt.Frame;

import de.parresum.digisim.gui.analyser.CapturedData;

/**
 * Interface for pluggable tools.
 * <p>
 * All tools implementing this interface that are added to the tools class list will be automatically added to the tools
 * menu in the client.
 *
 * @author Kai Uwe Bachmann
 */
public interface Tool {

   /**
    * Is called to get the name for the menu entry. The name must be unique among all tools. Should end in "..." if it
    * opens a dialog window.
    *
    * @return name for this tool
    */
   public String getName();

   /**
    * Performs tool initialization. This method should also prepare the dialog, if one is needed
    *
    * @param frame main window's frame (needed for modal dialogs)
    */
   public void init(Frame frame);

   /**
    * This method is invoked when the tool is selected from the Tools menu. It should request any missing information
    * using a dialog and perform the tool's actual task.
    *
    * @param data currently displayed captured data
    * @return new <code>CapturedData</code> if provided data has been altered or <code>null</code> otherwise
    */
   public CapturedData process(CapturedData data);

   /**
    * This method is invoked when the tool is selected from the context menu after right clicking someplace in the
    * diagram. It should request any missing information using a dialog and perform the tool's actual task.
    *
    * @param data     currently displayed captured data
    * @param group    channel group at the mouse position where the menu was openend
    * @param channel  number of channel at the mouse position where the menu was openend
    * @param position number of sample at the mouse position where the menu was openend
    * @return new <code>CapturedData</code> if provided data has been altered or <code>null</code> otherwise
    */
   public CapturedData process(CapturedData data, int group, int channel, int position);
}
