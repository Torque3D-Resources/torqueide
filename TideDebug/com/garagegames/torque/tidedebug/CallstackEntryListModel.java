package com.garagegames.torque.tidedebug;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;

public class CallstackEntryListModel extends AbstractListModel {

	private Vector<TideDebugCallstackViewer.CallstackEntry> callstackEntries;

	final List<ListDataListener> listeners = new ArrayList<ListDataListener>();

	public CallstackEntryListModel()
	{
		super();
		callstackEntries = new Vector<TideDebugCallstackViewer.CallstackEntry>();
	}
	
	public CallstackEntryListModel(Vector<TideDebugCallstackViewer.CallstackEntry> entries)
	{
		this.callstackEntries = entries;
	}

	//{{{ addListDataListener() method
	public void addListDataListener(ListDataListener listener)
	{
		listeners.add(listener);
	} //}}}

	//{{{ removeListDataListener() method
	public void removeListDataListener(ListDataListener listener)
	{
		listeners.remove(listener);
	} //}}}
	
	public Object getElementAt(int index) {
		return callstackEntries.elementAt(index);
	}
	
	public void addElement(TideDebugCallstackViewer.CallstackEntry entry)
	{
		callstackEntries.add(entry);
		fireIntervalAdded(this, getSize(), getSize());
	}

	public void removeAll()
	{
		callstackEntries.clear();
	}
	
	public int getSize() {
		return callstackEntries.size();
	}

}
