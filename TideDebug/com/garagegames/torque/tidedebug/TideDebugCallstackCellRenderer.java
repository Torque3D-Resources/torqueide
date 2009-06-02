package com.garagegames.torque.tidedebug;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class TideDebugCallstackCellRenderer  extends JComponent implements ListCellRenderer
{
	//{{{ ErrorListCellRenderer constructor
	TideDebugCallstackCellRenderer()
	{
		plainFont = new JLabel().getFont();
		boldFont = new Font(plainFont.getName(),Font.BOLD,plainFont.getSize());
		plainFM = getFontMetrics(plainFont);
		boldFM = getFontMetrics(boldFont);

		setBorder(new EmptyBorder(2,2,2,2));
	} //}}}

	//{{{ getListCellRendererComponent() method
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		TideDebugCallstackViewer.CallstackEntry entry = (TideDebugCallstackViewer.CallstackEntry)value;
		this.path = entry.path + ":";
		this.message = entry.message;
		this.selected = isSelected;
		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else
		{
			setBackground(list.getBackground());
			Color color = list.getForeground();
			setForeground(color);
		}

		setEnabled( list.isEnabled() );
		setFont( list.getFont() );
		setOpaque( true );
		
		return this;
	} //}}}

	//{{{ getPreferredSize() method
	public Dimension getPreferredSize()
	{
		int width = boldFM.stringWidth(path);
		int height = boldFM.getHeight();
		width = Math.max(plainFM.stringWidth(message),width);
		height += plainFM.getHeight();
		
		Insets insets = getBorder().getBorderInsets(this);
		width += insets.left + insets.right;
		height += insets.top + insets.bottom;

		return new Dimension(width,height);
	} //}}}

	//{{{ paintComponent() method
	public void paintComponent(Graphics g)
	{
		Insets insets = getBorder().getBorderInsets(this);
		g.setFont(boldFont);	
		if(selected)
			g.setColor(Color.BLUE);
		else
			g.setColor(Color.BLACK);
		g.drawString(message,insets.left,insets.top + boldFM.getAscent());
		int y = insets.top + boldFM.getHeight() + 2;
		g.setFont(plainFont);
	} //}}}

	//{{{ Instance variables
	private String path;
	private String message;
	private Font plainFont;
	private Font boldFont;
	private FontMetrics plainFM;
	private FontMetrics boldFM;
	private boolean selected;
	//}}}
}

