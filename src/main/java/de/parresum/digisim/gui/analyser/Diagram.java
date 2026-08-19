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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * This component displays a diagram which is obtained from a {@link CapturedData} object. The settings for the diagram
 * are obtained from the embedded {@link DiagramSettings} and {@link DiagramLabels} objects. Look there for an overview
 * of ways to display data.
 * <p>
 * Component size changes with the size of the diagram. Therefore it should only be used from within a JScrollPane.
 *
 *
 * @author Kai Uwe Bachmann
 */
public class Diagram extends JComponent implements Configurable {
   private static final int CHANNEL_HIGHT = 20;
   private static final int SCOPE_HIGHT = 133;
   private static final int BYTE_HIGHT = CHANNEL_HIGHT;
   private static final int TEXT_GAP = 5;
   private static final int TEXT_MIN = 15;

   // Höhe einer Kurve
   private final static int LINE_HEIGHT = 14;

   private CapturedData capturedData;
   private final DiagramSettings settings;
   private final DiagramLabels labels;
   private long unitFactor;
   private String unitName;

   private int offsetX;
   private final int offsetY;
   private int mouseX;
   private int mouseY;
   private int mouseDragX;
   private StatusChangeListener statusChangeListener;

   private double scale;
   private final double maxScale;
   private int timeDivider;
   private int currentPage;
   private int maxPages;
   private int pageLen;

   private final Color signal = new Color(0, 0, 196);
   private final Color trigger = new Color(196, 255, 196);
   private final Color grid = Color.LIGHT_GRAY;
   private final Color text = new Color(0, 0, 0);
   private final Color time = Color.BLACK;
   private final Color groupBackground = new Color(242, 242, 242);
   private final Color background = Color.WHITE;
   private final Color cursorA = new Color(190, 120, 0);
   private final Color cursorB = new Color(190, 120, 0);

   private int draggedCursor;
   private final Cursor cursorDefault;
   private final Cursor cursorDrag;
   private final Vector<DiagramCursorChangeListener> curListners;

   private Point contextMenuPosition;
   private JPopupMenu contextMenu;

   private final Dimension size;

   private static final long serialVersionUID = 1L;

   /*
    * TODO: Optimization: drawEdge is called many times with data containing many signal transitions. - Optimize 1: draw
    * a rectangle instead of the edges when zoomed to fit - Optimize 2: enable double buffering could increase
    * performance
    */

   /**
    * Create a new empty diagram to be placed in a container.
    *
    */
   public Diagram() {
      super();

      this.size = new Dimension(25, 1);

      this.offsetX = TEXT_MIN + 2 * TEXT_GAP;
      this.offsetY = 18;

      zoomDefault();
      setBackground(background);

      this.addMouseListener(new MouseAdapter() {

         /**
          * Handles mouse button events for context menu
          */
         @Override
         public void mousePressed(final MouseEvent event) {
            contextMenuPosition.x = event.getX();
            contextMenuPosition.y = event.getY();
            if ((event.getButton() == MouseEvent.BUTTON2) && (capturedData != null) && (getCursorMode())) {
               contextMenu.show(event.getComponent(), contextMenuPosition.x, contextMenuPosition.y);
            }
         }

      });
      this.addMouseMotionListener(new MouseMotionAdapter() {
         /**
          * Handles mouse dragged events and produces status change "events" accordingly.
          */
         @Override
         public void mouseDragged(final MouseEvent event) {
            mouseDragX = event.getX();
            updateCursors(true);
            updateStatus(true);
         }

         /**
          * Handles mouse moved events and produces status change "events" accordingly.
          */
         @Override
         public void mouseMoved(final MouseEvent event) {
            mouseX = event.getX();
            mouseY = event.getY();
            updateCursors(false);
            updateStatus(false);
         }

      });

      this.contextMenuPosition = new Point();
      this.contextMenu = new JPopupMenu();
      final JMenuItem gotoA = new JMenuItem("Set Cursor A");
      gotoA.addActionListener(_ -> doSetCursorA());
      this.contextMenu.add(gotoA);

      final JMenuItem gotoB = new JMenuItem("Set Cursor B");
      gotoB.addActionListener(_ -> doSetCursorB());
      this.contextMenu.add(gotoB);

      this.settings = new DiagramSettings();
      this.capturedData = null;

      this.labels = new DiagramLabels();

      this.cursorDefault = this.getCursor();
      this.cursorDrag = new Cursor(Cursor.MOVE_CURSOR);
      this.draggedCursor = 0;
      this.curListners = new Vector<DiagramCursorChangeListener>();

      this.maxScale = 10.0;
      this.timeDivider = 1;
      this.maxPages = 1;
      this.currentPage = 0;
      this.pageLen = 0;
   }

   public void addCursorChangeListener(final DiagramCursorChangeListener listener) {
      this.curListners.add(listener);
   }

   /**
    * Resizes the diagram as required by available data and scaling factor.
    *
    */
   private void resize() {
      if (capturedData == null) {
         return;
      }

      int height = 20;
      for (int group = 0; group < capturedData.getChannels() / 8 && group < 4; group++) {
         if (((capturedData.getEnabledChannels() >> (8 * group)) & 0xff) != 0) {
            if ((settings.getGroupSettings()[group] & DiagramSettings.DISPLAY_CHANNELS) > 0) {
               height += CHANNEL_HIGHT * 8;
            }
            if ((settings.getGroupSettings()[group] & DiagramSettings.DISPLAY_SCOPE) > 0) {
               height += SCOPE_HIGHT;
            }
            if ((settings.getGroupSettings()[group] & DiagramSettings.DISPLAY_BYTE) > 0) {
               height += BYTE_HIGHT;
            }
         }
      }

      long width = (long) (scale * capturedData.getAbsoluteLength());
      width /= maxPages;
      width += TEXT_MIN + 2 * TEXT_GAP;

      final Rectangle rect = getBounds();
      rect.setSize((int) width, height);
      setBounds(rect);
      size.width = (int) width;
      size.height = height;

      this.repaint();
   }

   /**
    * Sets the captured data object to use for drawing the diagram.
    *
    * @param capturedData captured data to base diagram on
    */
   public void setCapturedData(final CapturedData capturedData) {
      this.capturedData = capturedData;

      // reset zoom, etc.
      scale = maxScale;
      zoomDefault();
      timeDivider = 1;
      maxPages = 1;
      currentPage = 0;
      pageLen = 0;

      // show data
      calculateUnits();
      resize();
   }

   private void calculateUnits() {
      if (capturedData != null && capturedData.hasTimingData()) {
         final double step = (1000.0 / scale) / capturedData.getRate();

         unitFactor = 1;
         unitName = "s";
         if (step < 0.000001) {
            unitFactor = 1000000000;
            unitName = "ns";
         } else if (step < 0.001) {
            unitFactor = 1000000;
            unitName = "�s";
         } else if (step < 1) {
            unitFactor = 1000;
            unitName = "ms";
         }
      } else {
         unitFactor = 1;
         unitName = "";
      }
   }

   /**
    * Returns the captured data object currently displayed in the diagram.
    *
    * @return diagram's current captured data
    */
   public CapturedData getCapturedData() {
      return (capturedData);
   }

   /**
    * Returns wheter or not the diagram has any data.
    *
    * @return <code>true</code> if captured data exists, <code>false</code> otherwise
    */
   public boolean hasCapturedData() {
      return (capturedData != null);
   }

   // -----------------------------------------------------------------------------------
   // Zoom
   // -----------------------------------------------------------------------------------
   /**
    * Zooms in by factor 2 and resizes the component accordingly.
    *
    */
   public void zoomIn() {
      if (scale < maxScale) {
         scale = scale * 2;
         if (scale > maxScale) {
            scale = maxScale;
         }
         calculatePages();
         calculateUnits();
         resize();
      }
   }

   /**
    * Zooms out by factor 2 and resizes the component accordingly.
    *
    */
   public void zoomOut() {
      scale = scale / 2;
      calculatePages();
      calculateUnits();
      resize();
   }

   /**
    * Reverts back to the standard zoom level.
    *
    */
   public void zoomDefault() {
      scale = maxScale;
      calculatePages();
      calculateUnits();
      resize();
   }

   /**
    * Zooms to fitting the view on Display.
    *
    */
   public void zoomFit(int width) {
      // reverse the scaling
      width -= (TEXT_MIN + 2 * TEXT_GAP);
      if (width < 1) {
         width = 1;
      }

      // avoid null pointer exception when no data available
      if (capturedData == null) {
         return;
      }

      if (capturedData.getAbsoluteLength() > 0) {
         scale = width / (double) capturedData.getAbsoluteLength();
      } else {
         scale = maxScale;
      }

      calculatePages();
      calculateUnits();
      resize();
   }

   // -----------------------------------------------------------------------------------
   // Pages
   // -----------------------------------------------------------------------------------
   /**
    * calculate number and size of pages for various zoom levels.
    */
   private void calculatePages() {
      if (capturedData != null) {
         final double maxAvailableWidth = Integer.MAX_VALUE - 100;
         final double currentScaledSize = (long) (scale * capturedData.getAbsoluteLength());
         if (currentScaledSize > maxAvailableWidth) {
            maxPages = (int) Math.ceil(currentScaledSize / maxAvailableWidth);
            pageLen = (int) (scale * capturedData.getAbsoluteLength() / maxPages);
         } else {
            maxPages = 1;
            pageLen = (int) (scale * capturedData.getAbsoluteLength());
         }
      } else {
         maxPages = 1;
         pageLen = 0;
      }
   }

   /**
    * calulate the position within a window (pane) based on current page and zoom settings
    *
    * @param width window width
    * @param pos   sample position
    * @return current position within window
    */
   public int getTargetPosition(final int width, long pos) {
      pos -= getPageOffset();
      if (pos < 0) {
         pos = 0;
      }
      return (int) ((double) pos * (double) width * scale / pageLen);
   }

   /**
    * @param page set the current page
    */
   public void setCurrentPage(final int page) {
      currentPage = page;
      resize();
   }

   /**
    * get the page number based on a sample position
    *
    * @param pos sample position
    * @return page number
    */
   public int getPage(final long pos) {
      if (capturedData != null) {
         return (int) (pos * maxPages / capturedData.getAbsoluteLength());
      }
      return 0;
   }

   /**
    * @return the maxPages
    */
   public int getMaxPages() {
      return maxPages;
   }

   /**
    * @return the sample offset of the current page
    */
   public long getPageOffset() {
      return (currentPage * capturedData.getAbsoluteLength() / maxPages);
   }

   // -----------------------------------------------------------------------------------
   //
   // -----------------------------------------------------------------------------------
   /**
    * Display the diagram settings dialog. Will block until the dialog is closed again.
    *
    */
   public void showSettingsDialog(final Frame frame) {
      if (settings.showDialog(frame) == DiagramSettings.OK) {
         resize();
      }
   }

   /**
    * Display the diagram labels dialog. Will block until the dialog is closed again.
    *
    */
   public void showLabelsDialog(final Frame frame) {
      if (labels.showDialog(frame) == DiagramLabels.OK) {
         resize();
      }
   }

   /**
    * Gets the dimensions of the full diagram. Used to inform the container (preferrably a JScrollPane) about the size.
    */
   @Override
   public Dimension getPreferredSize() {
      return (size);
   }

   /**
    * Gets the dimensions of the full diagram. Used to inform the container (preferrably a JScrollPane) about the size.
    */
   @Override
   public Dimension getMinimumSize() {
      return (size);
   }

   /**
    * Enable/Disable diagram cursors
    */
   public void setCursorMode(final boolean enabled) {
      if (this.hasCapturedData()) {
         capturedData.setCursorEnabled(enabled);
      }
      updateStatus(false);
      resize();
   }

   /**
    * get current cursor mode
    */
   public boolean getCursorMode() {
      if (this.hasCapturedData()) {
         return capturedData.isCursorEnabled();
      }
      return false;
   }

   // -----------------------------------------------------------------------------------
   // Drawing
   // -----------------------------------------------------------------------------------
   private void drawEdge(final Graphics g, final int x, final int y, final boolean falling, final boolean rising) {
      if (scale <= 1) {
         g.drawLine(x, y, x, y + LINE_HEIGHT);
      } else {
         int edgeX = x;
         if (scale >= 5) {
            edgeX += (int) (scale * 0.4);
         }

         if (rising) {
            g.drawLine(x, y + LINE_HEIGHT, edgeX, y);
            g.drawLine(edgeX, y, x + (int) scale, y);
         }
         if (falling) {
            g.drawLine(x, y, edgeX, y + LINE_HEIGHT);
            g.drawLine(edgeX, y + LINE_HEIGHT, x + (int) scale, y + LINE_HEIGHT);
         }
      }
   }

   /**
    * Draws a channel.
    *
    * @param g    graphics context to draw on
    * @param x    x offset
    * @param y    y offset
    * @param data array containing the sampled data
    * @param n    number of channel to display
    * @param from index of first sample to display
    * @param to   index of last sample to display
    */
   private void drawChannel(final Graphics g, final int x, final int y, final int[] data, final long[] time,
         final int n, long from, long to) {
      int dataIndex = 0;

      from /= timeDivider;
      to /= timeDivider;

      do {
         if ((time[dataIndex] / timeDivider) > from) {
            break;
         }
         dataIndex++;
      } while (dataIndex < time.length);
      if (dataIndex > 0) {
         dataIndex--;
      }

      for (long current = from; current < to;) {
         int currentX = (int) ((current - getPageOffset()) * scale * timeDivider);
         final int currentV = (data[dataIndex] >> n) & 0x01;
         int nextV = currentV;
         long next = current;

         // here is a transition
         dataIndex++;
         if (dataIndex < data.length) {
            nextV = (data[dataIndex] >> n) & 0x01;
            next = time[dataIndex] / timeDivider;
         } else {
            next = to;
         }
         if (next >= to) {
            next = to + 1;
         }

         if (currentX < 0) {
            currentX = 0;
         }
         currentX += x;

         final int currentEndX = currentX + (int) (scale * (next - current - 1) * timeDivider);

         // draw straight line up to the point of change and a edge if not at end
         if (currentV == nextV) {
            g.drawLine(currentX, y + LINE_HEIGHT * (1 - currentV), currentEndX + (int) (scale * timeDivider),
                  y + 14 * (1 - currentV));
         } else {
            g.drawLine(currentX, y + LINE_HEIGHT * (1 - currentV), currentEndX, y + LINE_HEIGHT * (1 - currentV));
            if (currentV > nextV) {
               drawEdge(g, currentEndX, y, true, false);
            } else if (currentV < nextV) {
               drawEdge(g, currentEndX, y, false, true);
            }
         }
         current = next;
      }
   }

   private void drawGridLine(final Graphics g, final Rectangle clipArea, final int y) {
      g.setColor(grid);
      g.drawLine(clipArea.x, y, clipArea.x + clipArea.width, y);
   }

   /**
    * Draws a byte bar.
    *
    * @param g    graphics context to draw on
    * @param x    x offset
    * @param y    y offset
    * @param data array containing the sampled data
    * @param n    number of group to display (0-3 for 32 channels)
    * @param from index of first sample to display
    * @param to   index of last sample to display
    */
   private int drawGroupByte(final Graphics g, final int x, final int y, final int[] data, final long[] time,
         final Rectangle clipArea, final int n, long from, long to) {

      int dataIndex = 0;
      // find the time index one before "from"

      from /= timeDivider;
      to /= timeDivider;

      do {
         if ((time[dataIndex] / timeDivider) > from) {
            break;
         }
         dataIndex++;
      } while (dataIndex < time.length);
      if (dataIndex > 0) {
         dataIndex--;
      }

      // draw background
      g.setColor(groupBackground);
      g.fillRect(clipArea.x, y, clipArea.width, BYTE_HIGHT - 1);
      g.setColor(text);
      g.drawString("B" + n, TEXT_GAP, y + LINE_HEIGHT);
      // draw bottom grid line
      drawGridLine(g, clipArea, y + BYTE_HIGHT - 1);

      g.setColor(signal);

      final int yOfs = y + 2;
      final int h = LINE_HEIGHT;

      for (long current = from; current < to;) {
         int currentX = (int) ((current - getPageOffset()) * scale * timeDivider);
         final int currentXSpace = (int) (x + (current - 1) * scale * timeDivider);
         final int currentV = (data[dataIndex] >> (8 * n)) & 0xff;
         int nextV = currentV;
         long next = current;

         // here is a transition
         dataIndex++;
         if (dataIndex < data.length) {
            nextV = (data[dataIndex] >> n) & 0x01;
            next = time[dataIndex] / timeDivider;
         } else {
            next = to;
         }
         if (next >= to) {
            next = to + 1;
         }

         if (currentX < 0) {
            currentX = 0;
         }
         currentX += x;

         final int currentEndX = currentX + (int) (scale * (next - current - 1) * timeDivider);

         // draw straight lines up to the point of change and a edge if not at end
         if (currentV == nextV) {
            g.drawLine(currentX, yOfs + h, currentEndX + (int) scale * timeDivider, yOfs + h);
            g.drawLine(currentX, yOfs, currentEndX + (int) scale * timeDivider, yOfs);
         } else {
            g.drawLine(currentX, yOfs + h, currentEndX, yOfs + h);
            g.drawLine(currentX, yOfs, currentEndX, yOfs);
            drawEdge(g, currentEndX, yOfs, true, true);
         }

         // if steady long enough, add hex value
         if (currentEndX - currentXSpace > 15) {
            if (currentV >= 0x10) {
               g.drawString(Integer.toString(currentV, 16), (currentXSpace + currentEndX) / 2 - 2, y + LINE_HEIGHT);
            } else {
               g.drawString("0" + Integer.toString(currentV, 16), (currentXSpace + currentEndX) / 2 - 2,
                     y + LINE_HEIGHT);
            }

         }

         current = next;
      }
      return BYTE_HIGHT;
   }

   private int drawGroupAnalyzer(final Graphics g, final int xofs, final int yofs, final int data[], final long[] time,
         final Rectangle clipArea, final int n, final long from, final long to, final String labels[]) {
      // draw channel separators
      for (int bit = 0; bit < 8; bit++) {
         String tmp = "" + (bit + n * 8);
         if (labels[bit + n * 8] != null) {
            tmp = labels[bit + n * 8];
         }
         g.setColor(grid);
         g.drawLine(clipArea.x, CHANNEL_HIGHT * bit + yofs + CHANNEL_HIGHT - 1, clipArea.x + clipArea.width,
               CHANNEL_HIGHT * bit + yofs + CHANNEL_HIGHT - 1);
         g.setColor(text);
         g.drawString(tmp, TEXT_GAP, CHANNEL_HIGHT * bit + yofs + LINE_HEIGHT);
      }

      // draw actual data
      g.setColor(signal);
      for (int bit = 0; bit < 8; bit++) {
         drawChannel(g, xofs, yofs + 20 * bit + 2, data, time, 8 * n + bit, from, to);
      }

      return (CHANNEL_HIGHT * 8);
   }

   private int drawGroupScope(final Graphics g, final int x, final int y, final int data[], final long[] time,
         final Rectangle clipArea, final int n, long from, long to) {
      int dataIndex = 0;
      // find the time index one before "from"

      from /= timeDivider;
      to /= timeDivider;

      do {
         if ((time[dataIndex] / timeDivider) > from) {
            break;
         }
         dataIndex++;
      } while (dataIndex < time.length);
      if (dataIndex > 0) {
         dataIndex--;
      }

      // draw label
      g.setColor(text);
      g.drawString("S" + n, TEXT_GAP, y + 70);

      // draw actual data
      g.setColor(signal);
      int last = (255 - ((data[dataIndex] >> (n * 8)) & 0xff)) / 2;
      int val = (255 - ((data[dataIndex] >> (n * 8)) & 0xff)) / 2;
      int oldPosTmp = calcTmpPos(from);
      oldPosTmp += x;
      int posTmp;
      for (long pos = from; pos < to;) {
         final long oldPos = pos;
         pos = time[dataIndex] / timeDivider;
         if (pos > oldPos) {
            val = (255 - ((data[dataIndex] >> (n * 8)) & 0xff)) / 2;

            oldPosTmp = calcTmpPos(oldPos);
            oldPosTmp += x;

            posTmp = calcTmpPos(pos);
            posTmp += x;

            g.drawLine(oldPosTmp, y + 2 + last, posTmp, y + 2 + val);

            last = val;
         }
         dataIndex++;
         if (dataIndex >= time.length) {
            break;
         }
      }
      posTmp = calcTmpPos(to);
      posTmp += x;

      g.drawLine(oldPosTmp, y + 2 + last, posTmp, y + 2 + val);

      // draw bottom grid line
      drawGridLine(g, clipArea, y + SCOPE_HIGHT - 1);

      return (SCOPE_HIGHT);
   }

   private int calcTmpPos(final long pos) {
      long lval = (long) ((pos * timeDivider - getPageOffset()) * scale);
      if (lval >= Integer.MAX_VALUE) {
         lval = Integer.MAX_VALUE - 100;
      }
      if (lval < 0) {
         lval = 0;
      }
      return (int) lval;
   }

   /**
    * Paints the diagram to the extend necessary.
    */
   @Override
   public void paintComponent(final Graphics g) {
      if (capturedData == null) {
         return;
      }

      // calculate width for all labels
      String tmpLabels[] = labels.getDiagramLabels();
      if (capturedData.getLabels() != null) {
         tmpLabels = capturedData.getLabels();
      }

      int offX = TEXT_MIN;
      final FontMetrics fm = g.getFontMetrics();
      for (int i = 0; i < 32; i++) {
         if (tmpLabels[i] != null) {
            final int width = fm.stringWidth(tmpLabels[i]);
            offX = Math.max(offX, width);
         }
      }
      offsetX = offX + +2 * TEXT_GAP;

      final boolean hasTiming = capturedData.hasTimingData();
      final boolean hasTrigger = capturedData.hasTriggerData();
      final int channels = capturedData.getChannels();
      final int enabled = capturedData.getEnabledChannels();
      long triggerPosition = capturedData.getTriggerPosition();
      if (!hasTrigger) {
         triggerPosition = 0;
      }
      int rate = capturedData.getRate();
      if (!hasTiming) { // value of rate is only valid if timing data exists
         rate = 1;
      }

      final int xofs = offsetX;
      final int yofs = offsetY + 2;

      // obtain portion of graphics that needs to be drawn
      final Rectangle clipArea = g.getClipBounds();

      // find index of first row that needs drawing
      final long firstRow = xToIndex(clipArea.x);

      // find index of last row that needs drawing
      final long lastRow = xToIndex(clipArea.x + clipArea.width) + 1;

      // calculate time divider for samplecount > 2^31-1
      final long visibleSamples = lastRow - firstRow;
      timeDivider = 1;
      while ((visibleSamples / timeDivider) >= Integer.MAX_VALUE) {
         timeDivider++;
      }

      // paint portion of background that needs drawing
      g.setColor(background);
      g.fillRect(clipArea.x, clipArea.y, clipArea.width, clipArea.height);

      // draw trigger if existing and visible
      if (hasTrigger && triggerPosition >= firstRow && triggerPosition <= lastRow) {
         g.setColor(trigger);
         g.fillRect(xofs + (int) ((triggerPosition - getPageOffset()) * scale) - 1, 0, (int) (scale) + 2,
               yofs + 36 * 20);
      }

      // draw time line
      int rowInc = (int) (10 / scale);
      if (rowInc <= 0) {
         rowInc = 1;
      }
      final int timeLineShift = (int) (triggerPosition % rowInc);
      g.setColor(time);
      for (long row = (firstRow / rowInc) * rowInc + timeLineShift; row < lastRow; row += rowInc) {
         int pos = (int) (scale * (row - getPageOffset()));
         if (pos < 0) {
            pos = 0;
         }
         pos += xofs;
         if (((row - triggerPosition) / rowInc) % CHANNEL_HIGHT == 0) {
            g.drawLine(pos, 1, pos, 15);
            if (hasTiming) {
               final NumberFormat nf = NumberFormat.getInstance();
               nf.setMaximumFractionDigits(15);
               nf.setMinimumFractionDigits(1);
               g.drawString(nf.format((double) (row - triggerPosition) / (double) rate) + " sec", pos + TEXT_GAP, 10);
            } else {
               g.drawString(Long.toString(row - triggerPosition), pos + TEXT_GAP, 10);
            }
         } else {
            g.drawLine(pos, 12, pos, 15);
         }
      }

      // draw groups
      int bofs = yofs;
      drawGridLine(g, clipArea, bofs++);

      for (int block = 0; block < channels / 8; block++) {
         if (((enabled >> (8 * block)) & 0xff) != 0) {
            if (block < 4 && (settings.getGroupSettings()[block] & DiagramSettings.DISPLAY_CHANNELS) > 0) {
               bofs += drawGroupAnalyzer(g, xofs, bofs, capturedData.getValues(), capturedData.getTimestamps(),
                     clipArea, block, firstRow, lastRow, tmpLabels);
            }
            if (block < 4 && (settings.getGroupSettings()[block] & DiagramSettings.DISPLAY_SCOPE) > 0) {
               bofs += drawGroupScope(g, xofs, bofs, capturedData.getValues(), capturedData.getTimestamps(), clipArea,
                     block, firstRow, lastRow);
            }
            if (block < 4 && (settings.getGroupSettings()[block] & DiagramSettings.DISPLAY_BYTE) > 0) {
               bofs += drawGroupByte(g, xofs, bofs, capturedData.getValues(), capturedData.getTimestamps(), clipArea,
                     block, firstRow, lastRow);
            }
         }
      }

      // draw cursors if enabled
      if (capturedData.isCursorEnabled()) {
         // draw cursor B first (lower priority)
         if (capturedData.getCursorPositionB() >= firstRow && capturedData.getCursorPositionB() <= lastRow) {
            g.setColor(background);
            g.fillRect(xofs + (int) ((capturedData.getCursorPositionB() - getPageOffset()) * scale), 0, 8, 12);
            g.setColor(cursorB);
            g.drawRect(xofs + (int) ((capturedData.getCursorPositionB() - getPageOffset()) * scale), 0, 8, 12);
            g.drawLine(xofs + (int) ((capturedData.getCursorPositionB() - getPageOffset()) * scale), 0,
                  xofs + (int) ((capturedData.getCursorPositionB() - getPageOffset()) * scale), yofs + 36 * BYTE_HIGHT);
            g.drawString("B", xofs + (int) ((capturedData.getCursorPositionB() - getPageOffset()) * scale) + 1, 11);
         }
         // draw cursor A last (higher priority)
         if (capturedData.getCursorPositionA() >= firstRow && capturedData.getCursorPositionA() <= lastRow) {
            g.setColor(background);
            g.fillRect(xofs + (int) ((capturedData.getCursorPositionA() - getPageOffset()) * scale), 0, 8, 12);
            g.setColor(cursorA);
            g.drawRect(xofs + (int) ((capturedData.getCursorPositionA() - getPageOffset()) * scale), 0, 8, 12);
            g.drawLine(xofs + (int) ((capturedData.getCursorPositionA() - getPageOffset()) * scale), 0,
                  xofs + (int) ((capturedData.getCursorPositionA() - getPageOffset()) * scale), yofs + 36 * BYTE_HIGHT);
            g.drawString("A", xofs + (int) ((capturedData.getCursorPositionA() - getPageOffset()) * scale), 11);
         }
      }
   }

   /**
    * Convert x position to sample index.
    *
    * @param x horizontal position in pixels
    * @return sample index
    */
   private long xToIndex(final int x) {
      long index = (long) ((x - offsetX) / scale);
      index += getPageOffset();
      if (index < 0) {
         index = 0;
      }
      if (index >= capturedData.getAbsoluteLength()) {
         index = capturedData.getAbsoluteLength() - 1;
      }
      return (index);
   }

   /**
    * Convert sample count to time string.
    *
    * @param count sample count (or index)
    * @return string containing time information
    */
   private String indexToTime(final long count) {
      final double time = (((double) count * (double) unitFactor) / capturedData.getRate());
      return (String.format("%.3f", time) + unitName);
   }

   /**
    * Update status information. Notifies {@link StatusChangeListener}.
    *
    * @param dragging <code>true</code> indicates that dragging information should be added
    */
   private void updateStatus(final boolean dragging) {
      if (capturedData == null || statusChangeListener == null) {
         return;
      }

      final StringBuilder sb = new StringBuilder(" ");

      final int row = (mouseY - offsetY) / CHANNEL_HIGHT;
      if (row <= capturedData.getChannels() + (capturedData.getChannels() / 9)) {
         if (row % 9 == 8) {
            sb.append("Byte " + (row / 9));
         } else {
            sb.append("Channel " + (row - (row / 9)));
         }
         sb.append(" | ");
      }

      if (capturedData.isCursorEnabled()) {
         // print cursor data to status line
         if (!capturedData.hasTimingData()) {
            sb.append("Sample@A=" + (capturedData.getCursorPositionA() - capturedData.getTriggerPosition()));
            sb.append(" | Sample@B=" + (capturedData.getCursorPositionB() - capturedData.getTriggerPosition()));
            sb.append(" | Distance(A,B)=" + (capturedData.getCursorPositionB() - capturedData.getCursorPositionA()));
         } else {
            float frequency = 0;
            if (capturedData.getCursorPositionA() == capturedData.getCursorPositionB()) {
               // no difference between cursors --> infinite frequency
            } else {
               frequency = Math.abs((float) capturedData.getRate()
                     / (float) (capturedData.getCursorPositionA() - capturedData.getCursorPositionB()));
            }
            String unit;
            int div;
            if (frequency >= 1000000) {
               unit = "MHz";
               div = 1000000;
            } else if (frequency >= 1000) {
               unit = "kHz";
               div = 1000;
            } else {
               unit = "Hz";
               div = 1;
            }
            sb.append("Time@A=" + indexToTime(capturedData.getCursorPositionA() - capturedData.getTriggerPosition()));
            sb.append(
                  " | Time@B=" + indexToTime(capturedData.getCursorPositionB() - capturedData.getTriggerPosition()));
            sb.append(" (Duration "
                  + indexToTime(Math.abs(capturedData.getCursorPositionA() - capturedData.getCursorPositionB()))
                  + ", ");
            if (frequency != 0) {
               sb.append("Frequency " + (frequency / div) + unit + ")");
            } else {
               sb.append("Frequency undefined)");
            }
         }
      } else // print origin status when no cursors used
      if (dragging && xToIndex(mouseDragX) != xToIndex(mouseX)) {
         final long index = xToIndex(mouseDragX);

         if (!capturedData.hasTimingData()) {
            sb.append("Sample " + (index - capturedData.getTriggerPosition()));
            sb.append(" (Distance " + (index - xToIndex(mouseX)) + ")");
         } else {
            final float frequency = Math.abs((float) capturedData.getRate() / (index - xToIndex(mouseX)));
            String unit;
            int div;
            if (frequency >= 1_000_000) {
               unit = "MHz";
               div = 1_000_000;
            } else if (frequency >= 1_000) {
               unit = "kHz";
               div = 1_000;
            } else {
               unit = "Hz";
               div = 1;
            }
            sb.append("Time " + indexToTime(index - capturedData.getTriggerPosition()));
            sb.append(" (Duration " + indexToTime(index - xToIndex(mouseX)) + ", ");
            sb.append("Frequency " + (frequency / div) + unit + ")");
         }
      } else if (!capturedData.hasTimingData()) {
         sb.append("Sample " + (xToIndex(mouseX) - capturedData.getTriggerPosition()));
      } else {
         sb.append("Time " + indexToTime(xToIndex(mouseX) - capturedData.getTriggerPosition()));
      }
      statusChangeListener.statusChanged(sb.toString());
   }

   private void updateCursors(final boolean dragged) {
      if (capturedData == null) {
         return;
      }

      long index;

      if (capturedData.isCursorEnabled()) {
         if (dragged) {
            // drag cursor only when mouse is near by
            switch (draggedCursor) {
               case 1:
                  // cursor A is dragged
                  index = xToIndex(mouseDragX);
                  capturedData.setCursorPositionA(index);
                  // notify cursor change listeners
                  if (index > 0 && index < (capturedData.getAbsoluteLength() - 1)) {
                     for (int i = 0; i < curListners.size(); i++) {
                        curListners.get(i).onCursorChanged(mouseDragX);
                     }
                  }
                  break;
               case 2:
                  // cursor B is dragged
                  index = xToIndex(mouseDragX);
                  capturedData.setCursorPositionB(index);
                  // notify cursor change listeners
                  if (index > 0 && index < (capturedData.getAbsoluteLength() - 1)) {
                     for (int i = 0; i < curListners.size(); i++) {
                        curListners.get(i).onCursorChanged(mouseDragX);
                     }
                  }
                  break;
               default:
                  break;
            }
            this.repaint();
         } else // not dragged, just check if the cursor is near by a trigger
         if (Math.abs(xToIndex(mouseX) - (capturedData.getCursorPositionA())) < (5 / scale)) {
            this.setCursor(cursorDrag);
            this.draggedCursor = 1;
         } else if (Math.abs(xToIndex(mouseX) - (capturedData.getCursorPositionB())) < (5 / scale)) {
            this.setCursor(cursorDrag);
            this.draggedCursor = 2;
         } else {
            this.setCursor(cursorDefault);
            this.draggedCursor = 0;
         }
      }
   }

   private void doSetCursorA() {
      capturedData.setCursorPositionA(xToIndex(contextMenuPosition.x));
      this.repaint();
   }

   private void doSetCursorB() {
      capturedData.setCursorPositionB(xToIndex(contextMenuPosition.x));
      this.repaint();
   }

   /**
    * Adds a status change listener for this diagram, Simple implementation that will only call the last added listener
    * on status change.
    */
   public void addStatusChangeListener(final StatusChangeListener listener) {
      statusChangeListener = listener;
   }

   @Override
   public void readProperties(final Properties properties) {
      settings.readProperties(properties);
      labels.readProperties(properties);
      resize();
   }

   @Override
   public void writeProperties(final Properties properties) {
      settings.writeProperties(properties);
      labels.writeProperties(properties);
   }

}
