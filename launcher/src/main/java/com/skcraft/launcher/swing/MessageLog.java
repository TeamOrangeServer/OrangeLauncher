/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import static org.apache.commons.io.IOUtils.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.google.common.collect.Lists;
import com.skcraft.launcher.util.LimitLinesDocumentListener;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SimpleLogFormatter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.teamfruit.skcraft.launcher.swing.AttributeLog;

/**
 * A simple message log.
 */
public class MessageLog extends JTabbedPane implements AttributeLog {

    private static final Logger rootLogger = Logger.getLogger("");

    private final int numLines;
    private final boolean colorEnabled;

    @Getter @Setter protected JCheckBox seeLastCheckbox;
    @Getter protected MessagePanel message;

    private Handler loggerHandler;
    protected final SimpleAttributeSet defaultAttributes = new SimpleAttributeSet();
    protected final SimpleAttributeSet highlightedAttributes;
    protected final SimpleAttributeSet errorAttributes;
    protected final SimpleAttributeSet infoAttributes;
    protected final SimpleAttributeSet debugAttributes;

    public class MessagePanel extends JPanel {
	    protected JTextComponent textComponent;
	    protected JScrollPane scrollText;
	    protected Document document;

    	public MessagePanel() {
            setLayout(new BorderLayout());

			initComponents();
		}

        private void initComponents() {
            if (colorEnabled) {
                JTextPane text = new JTextPane() {
                    @Override
                    public boolean getScrollableTracksViewportWidth() {
                        return true;
                    }
                };
                this.textComponent = text;
            } else {
                JTextArea text = new JTextArea();
                this.textComponent = text;
                text.setLineWrap(true);
                text.setWrapStyleWord(true);
            }

            textComponent.setFont(new JLabel().getFont());
            textComponent.setEditable(false);
            textComponent.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
            DefaultCaret caret = (DefaultCaret) textComponent.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            document = textComponent.getDocument();
            document.addDocumentListener(new LimitLinesDocumentListener(numLines, true));

            scrollText = new JScrollPane(textComponent);
            scrollText.setBorder(null);
            scrollText.setVerticalScrollBarPolicy(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scrollText.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            MouseAdapter mouseAdapter = new MouseAdapter() {
            	@Override
            	public void mousePressed(MouseEvent e) {
            		seeLastCheckbox.setSelected(false);
            	}

            	@Override
            	public void mouseWheelMoved(MouseWheelEvent e) {
            		if(e.getWheelRotation()<0)
            			seeLastCheckbox.setSelected(false);
            	}
    		};
            scrollText.getVerticalScrollBar().addMouseListener(mouseAdapter);
            scrollText.addMouseWheelListener(mouseAdapter);
            final JScrollBar scrollBar = scrollText.getVerticalScrollBar();
            scrollBar.addAdjustmentListener(new AdjustmentListener(){
                @Override
                public void adjustmentValueChanged(AdjustmentEvent arg0) {
                    int pos = scrollBar.getValue();
                    int height = scrollBar.getHeight();
                    if(scrollBar.getMaximum() == pos + height){
                        seeLastCheckbox.setSelected(true);
                    }
                }
            });

            add(scrollText, BorderLayout.CENTER);
        }

        public String getPastableText() {
            String text = textComponent.getText().replaceAll("[\r\n]+", "\n");
            text = text.replaceAll("Session ID is [A-Fa-f0-9]+", "Session ID is [redacted]");
            return text;
        }

        public void clear() {
            textComponent.setText("");
        }

        public void log(final String line, AttributeSet attributes) {
        	final Document d = document;
        	final JTextComponent t = textComponent;

            if (colorEnabled) {
                if (line.startsWith("(!!)")) {
                    attributes = highlightedAttributes;
                }
            }
            final AttributeSet a = attributes;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        int offset = d.getLength();
                        d.insertString(offset, line,
                                (a != null && colorEnabled) ? a : defaultAttributes);
                        if (seeLastCheckbox.isSelected())
                        	t.setCaretPosition(d.getLength());
                    } catch (BadLocationException ble) {

                    }
                }
            });
        }

	    /**
		 * Register a global logger listener.
		 */
		public void registerLoggerHandler() {
		    loggerHandler = new ConsoleLoggerHandler();
		    rootLogger.addHandler(loggerHandler);
		}

		/**
		 * Used to send logger messages to the console.
		 */
		private class ConsoleLoggerHandler extends Handler {
		    private final SimpleLogFormatter formatter = new SimpleLogFormatter();

		    @Override
		    public void publish(LogRecord record) {
		        Level level = record.getLevel();
		        /*Throwable t = */record.getThrown();
		        AttributeSet attributes = defaultAttributes;

		        if (level.intValue() >= Level.WARNING.intValue()) {
		            attributes = errorAttributes;
		        } else if (level.intValue() < Level.INFO.intValue()) {
		            attributes = debugAttributes;
		        }

		        log(formatter.format(record), attributes);
		    }

		    @Override
		    public void flush() {
		    }

		    @Override
		    public void close() throws SecurityException {
		    }
		}
    }


	public MessageLog(int numLines, boolean colorEnabled) {
        this.numLines = numLines;
        this.colorEnabled = colorEnabled;

        this.highlightedAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(highlightedAttributes, new Color(0xFF7F00));

        this.errorAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(errorAttributes, new Color(0xFF0000));
        this.infoAttributes = new SimpleAttributeSet();
        this.debugAttributes = new SimpleAttributeSet();

        initComponents();
    }

    private void initComponents() {
        seeLastCheckbox = new JCheckBox(SharedLocale.tr("console.seeLast"), true);
    	message = new MessagePanel();

    	initTabs();
    }

    protected void initTabs() {
    	addTab(SharedLocale.tr("console.launcherTab"), message);
    }

    /**
     * Log a message given the {@link javax.swing.text.AttributeSet}.
     *
     * @param line line
     * @param attributes attribute set, or null for none
     */
    public void log(final String line, AttributeSet attributes) {
    	for (Component message: getComponents())
    		if (message instanceof MessagePanel)
    			((MessagePanel) message).log(line, attributes);
    }

    public void clear() {
    	for (Component message: getComponents())
    		if (message instanceof MessagePanel)
				((MessagePanel) message).textComponent.setText("");
    }

    /**
     * Get an output stream that can be written to.
     *
     * @return output stream
     */
    public ConsoleOutputStream getOutputStream() {
        return getOutputStream((AttributeSet) null);
    }

    /**
     * Get an output stream with the given attribute set.
     *
     * @param attributes attributes
     * @return output stream
     */
    public ConsoleOutputStream getOutputStream(AttributeSet attributes) {
        return new ConsoleOutputStream(attributes);
    }

    /**
     * Get an output stream using the give color.
     *
     * @param color color to use
     * @return output stream
     */
    public ConsoleOutputStream getOutputStream(Color color) {
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, color);
        return getOutputStream(attributes);
    }

    /**
     * Consume an input stream and print it to the dialog. The consumer
     * will be in a separate daemon thread.
     *
     * @param from stream to read
     */
    public void consume(ConsumableInputStream from) {
        consume(from, getOutputStream());
    }

    /**
     * Consume an input stream and print it to the dialog. The consumer
     * will be in a separate daemon thread.
     *
     * @param from stream to read
     * @param color color to use
     */
    public void consume(ConsumableInputStream from, Color color) {
        consume(from, getOutputStream(color));
    }

    /**
     * Consume an input stream and print it to the dialog. The consumer
     * will be in a separate daemon thread.
     *
     * @param from stream to read
     * @param attributes attributes
     */
    public void consume(ConsumableInputStream from, AttributeSet attributes) {
        consume(from, getOutputStream(attributes));
    }

    /**
     * Internal method to consume a stream.
     *
     * @param from stream to consume
     * @param outputStream console stream to write to
     */
    private void consume(ConsumableInputStream from, ConsoleOutputStream outputStream) {
        from.consume(outputStream);
    }

    /**
     * Detach the handler on the global logger.
     */
    public void detachGlobalHandler() {
        if (loggerHandler != null) {
            rootLogger.removeHandler(loggerHandler);
            loggerHandler = null;
        }
    }

    public SimpleAttributeSet asDefault() {
        return defaultAttributes;
    }

    public SimpleAttributeSet asHighlighted() {
        return highlightedAttributes;
    }

    public SimpleAttributeSet asError() {
        return errorAttributes;
    }

    public SimpleAttributeSet asInfo() {
        return infoAttributes;
    }

    public SimpleAttributeSet asDebug() {
        return debugAttributes;
    }

    public static class ConsumableInputStream {
    	private final InputStream from;
    	private final @Getter(lazy=true, value=AccessLevel.PRIVATE) Thread thread = initListener();
    	private List<PrintWriter> outs = Lists.newCopyOnWriteArrayList();

    	public ConsumableInputStream(InputStream from) {
			this.from = from;
		}

        /**
         * Internal method to consume a stream.
         *
         * @param from stream to consume
         * @param outputStream console stream to write to
         */
        void consume(ConsoleOutputStream outputStream) {
            outs.add(new PrintWriter(outputStream, true));
            getThread();
        }

        private Thread initListener() {
        	final InputStream in = from;
        	Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    try {
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            String s = new String(buffer, 0, len);
                            System.out.print(s);
                            for (PrintWriter out: outs) {
                                out.append(s);
                                out.flush();
                            }
                        }
                    } catch (IOException e) {
                    } finally {
                        closeQuietly(in);
                        for (PrintWriter out: outs)
                        	closeQuietly(out);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
            return thread;
        }
    }


    /**
     * Used to send console messages to the console.
     */
    private class ConsoleOutputStream extends ByteArrayOutputStream {
        private AttributeSet attributes;

        private ConsoleOutputStream(AttributeSet attributes) {
            this.attributes = attributes;
        }

        @Override
        public void flush() throws UnsupportedEncodingException {
            String data = toString();
            print(data);
        }

        public void print(String data) {
            if (data.length() == 0) return;
            log(data, attributes);
            reset();
        }
    }

}
