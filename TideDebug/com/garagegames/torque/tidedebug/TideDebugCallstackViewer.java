package com.garagegames.torque.tidedebug;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.Log;

import com.garagegames.torque.tide.Tide;


public class TideDebugCallstackViewer extends JPanel implements DefaultFocusComponent,
EBComponent {

	private ListHandler listHandler;
	private final JList list;
	private final JButton copy;
	private final JCheckBox tail;
	private boolean tailIsOn;
	public static final CallstackEntryListModel listModel;
	
	static
	{
		listModel = new CallstackEntryListModel();
	}
	   

	//{{{ TideDebugCallstackViewer constructor
	public TideDebugCallstackViewer() {
		super(new BorderLayout());

		JPanel caption = new JPanel();
		caption.setLayout(new BoxLayout(caption,BoxLayout.X_AXIS));
		caption.setBorder(new EmptyBorder(6,6,6,6));

		String settingsDirectory = jEdit.getSettingsDirectory();
		if(settingsDirectory != null)
		{
			String[] args = { "" };
			JLabel label = new JLabel(jEdit.getProperty(
								    "tide-callstack-viewer.caption",args));
			caption.add(label);
		}

		caption.add(Box.createHorizontalGlue());

		tailIsOn = jEdit.getBooleanProperty("tide-callstack-viewer.tail", false);
		tail = new JCheckBox(
				     jEdit.getProperty("tide-callstack-viewer.tail.label"),tailIsOn);
		tail.addActionListener(new ActionHandler());
		caption.add(tail);

		caption.add(Box.createHorizontalStrut(12));

		copy = new JButton(jEdit.getProperty("tide-callstack-viewer.copy"));
		copy.addActionListener(new ActionHandler());
		caption.add(copy);

		
		list = new JList(listModel);
		list.setCellRenderer(new TideDebugCallstackCellRenderer());
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setVisibleRowCount(Math.min(listModel.getSize(),4));
		list.addListSelectionListener(new MyListListener());
		listModel.addListDataListener(listHandler = new ListHandler());
		
		JScrollPane scroller = new JScrollPane(list,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		Dimension dim = scroller.getPreferredSize();
		dim.width = Math.min(600,dim.width);
		scroller.setPreferredSize(dim);
		add(BorderLayout.CENTER,scroller);

		propertiesChanged();

	} //}}}

	
	//{{{ getCallStackEntryListModel() method
	/**
	 * Returns the list model for viewing the log contents.
	 * @since jEdit 4.2pre1
	 */
	public static ListModel getCallstackEntryListModel()
	{
		return listModel;
	} //}}}
	


	public void setBounds(int x, int y, int width, int height)
	{
		list.setCellRenderer( new TideDebugCallstackCellRenderer() );
		super.setBounds(x, y, width, height);
		scrollLaterIfRequired();
	} //}}}

	public void focusOnDefaultComponent() {
		list.requestFocus();
	}

	public void handleMessage(EBMessage msg) {
		if(msg instanceof PropertiesChanged)
			propertiesChanged();
	}

	//{{{ propertiesChanged() method
	private void propertiesChanged()
	{
		list.setFont(jEdit.getFontProperty("view.font"));
		list.setFixedCellHeight(list.getFontMetrics(list.getFont())
					.getHeight());
	} //}}}

	//{{{ scrollToTail() method
	/** Scroll to the tail of the logs. */
	private void scrollToTail()
	{
		int index = list.getModel().getSize();
		if(index != 0)
			list.ensureIndexIsVisible(index - 1);
	} //}}}

	//{{{ scrollLaterIfRequired() method
	private void scrollLaterIfRequired()
	{
		if (tailIsOn)
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					scrollToTail();
				}
			});
	} //}}}

	public void addNotify()
	{
		super.addNotify();
		if(tailIsOn)
			scrollToTail();
		EditBus.addToBus(this);
	} //}}}

	//{{{ removeNotify() method
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		EditBus.removeFromBus(this);
	} //}}}
	
	//{{{ ActionHandler class
	private class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Object src = e.getSource();
			if(src == tail)
			{
				tailIsOn = !tailIsOn;
				jEdit.setBooleanProperty("tide-callstack-viewer.tail",tailIsOn);
				if(tailIsOn)
				{
					scrollToTail();
				}
			}
			else if(src == copy)
			{
				StringBuilder buf = new StringBuilder();
				Object[] selected = list.getSelectedValues();
				if(selected != null && selected.length != 0)
				{
					for(int i = 0; i < selected.length; i++)
					{
						buf.append(selected[i]);
						buf.append('\n');
					}
				}
				else
				{
					ListModel model = list.getModel();
					for(int i = 0; i < model.getSize(); i++)
					{
						if(model.getElementAt(i) != null)
						{
							if(model.getElementAt(i) != null)
							{
								buf.append(model.getElementAt(i));
								buf.append('\n');
							}
						}
					}
				}
				Registers.setRegister('$',buf.toString());
			}
		}
	} //}}}

	private class MyListListener implements ListSelectionListener 
	{
		public void valueChanged(ListSelectionEvent event) 
		{
			if (!event.getValueIsAdjusting() && list.getSelectedValue() != null) 
			{
				String selection = list.getSelectedValue().toString().trim();
				// get path and line and open file at pos
				if(selection.indexOf(":") > -1)
				{
					Tide tide = Tide.getInstance();
					String fileName = selection.substring(0, selection.indexOf(':'));
					String numStr = selection.substring(selection.lastIndexOf(" ")+1).trim();
					int lineNumber = new Integer(numStr).intValue();
					if(lineNumber > -1)
						tide.openAndScrollTo(fileName, lineNumber);
				}
			}
		}
	}
	
	private class ListHandler implements ListDataListener
	{
		public void intervalAdded(ListDataEvent e)
		{
			contentsChanged(e);
		}

		public void intervalRemoved(ListDataEvent e)
		{
			contentsChanged(e);
		}

		public void contentsChanged(ListDataEvent e)
		{
			scrollLaterIfRequired();
		}
	}
	
   //{{{ ErrorEntry class
	public static class CallstackEntry
	{
		String path;
		String functionName;
		int lineNum;
		String message;
		String[] messages;

		public CallstackEntry(String path, String functionName, int lineNum)
		{
			this.path = path;
			this.functionName = functionName;
			this.lineNum = lineNum;
			this.message = this.path + ": " + this.functionName + " - Line " + this.lineNum + "\n";
		}

		public boolean equals(Object o)
		{
			if(o instanceof CallstackEntry)
			{
				CallstackEntry e = (CallstackEntry)o;
				return e.path.equals(path);
			}
			else
				return false;
		}

		// This enables users to copy the error messages to
		// clipboard with Ctrl+C on Windows. But it works only
		// if the entry is selected by a mouse click.
		public String toString()
		{
			return message;
		}
	} //}}}
	
}
