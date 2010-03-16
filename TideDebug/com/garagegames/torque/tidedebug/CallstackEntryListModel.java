package com.garagegames.torque.tidedebug;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.gjt.sp.util.Log;

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
		if(index < callstackEntries.size())
			return callstackEntries.elementAt(index);
		return null;
	}
	
	public void addElement(TideDebugCallstackViewer.CallstackEntry entry)
	{
		callstackEntries.add(entry);
		Log.log(Log.DEBUG, this, "Size of vector: " + getSize());
		this.fireIntervalAdded(getSize(), getSize());
	}

	//{{{ fireIntervalAdded() method
	private void fireIntervalAdded(int index1, int index2)
	{
		for(int i = 0; i < listeners.size(); i++)
		{
			ListDataListener listener = listeners.get(i);
			listener.intervalAdded(new ListDataEvent(this,
				ListDataEvent.INTERVAL_ADDED,
				index1,index2));
		}
	} //}}}

	//{{{ fireIntervalRemoved() method
	private void fireIntervalRemoved(int index1, int index2)
	{
		for(int i = 0; i < listeners.size(); i++)
		{
			ListDataListener listener = listeners.get(i);
			listener.intervalRemoved(new ListDataEvent(this,
				ListDataEvent.INTERVAL_REMOVED,
				index1,index2));
		}
	} //}}}
	
	
	public void removeAll()
	{
		callstackEntries.clear();
		this.fireIntervalRemoved(getSize(), getSize());
	}
	
	public int getSize() {
		return callstackEntries.size();
	}

}
