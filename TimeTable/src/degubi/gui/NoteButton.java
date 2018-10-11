package degubi.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import degubi.data.Note;
import degubi.tools.NIO;
import degubi.tools.PropertyFile;

public final class NoteButton extends JButton{
	private static final ImageIcon deleteIcon = NIO.getIcon("delete.png", 28);
	public static int yPos = 10;
	
	public NoteButton(Note note, JPanel panel) {
		super(note.message);
		
		JButton deleteButton = new JButton(deleteIcon);
		deleteButton.setBounds(227, yPos, 32, 32);
		deleteButton.setOpaque(false);
		deleteButton.addActionListener(e -> {
			PropertyFile.notes.remove(note);
			Note.initNotes(panel);
			PropertyFile.setObjectList("notes", Note.builder, PropertyFile.notes);
			panel.repaint();
		});
		panel.add(deleteButton);
		
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.TOP);
		setForeground(Color.BLACK);
		setBackground(note.color);
		setBounds(10, yPos, 250, 80);
		yPos += 85;
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		graphics.setColor(Color.BLACK);
		graphics.drawLine(10, 25, 230, 25);
		graphics.drawLine(10, 60, 230, 60);
	}
}