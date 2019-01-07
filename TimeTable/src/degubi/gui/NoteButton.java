package degubi.gui;

import com.google.gson.*;
import degubi.tools.*;
import java.awt.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;

public final class NoteButton extends JButton{
	private static final ImageIcon deleteIcon = GuiTools.getIcon("delete.png", 28);
	public static int yPos = 10;
	
	public NoteButton(JsonObject note, JPanel panel) {
		super("<html>" + note.get("message").getAsString().replace("\n", "<br>"));
		
		var deleteButton = new JButton(deleteIcon);
		deleteButton.setBounds(227, yPos, 32, 32);
		deleteButton.setOpaque(false);
		deleteButton.addActionListener(e -> {
			Settings.notes.remove(note);
			initNotes(panel);
			panel.repaint();
			Settings.save();
		});
		panel.add(deleteButton);
		
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.TOP);
		setForeground(Color.BLACK);
		setBackground(Settings.parseFromString(note.get("color").getAsString()));
		setBounds(10, yPos, 250, 80);
		yPos += 85;
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		graphics.setColor(Color.BLACK);
		graphics.drawLine(10, 22, 230, 22);
		graphics.drawLine(10, 38, 230, 38);
		graphics.drawLine(10, 54, 230, 54);
	}
	
	public static void initNotes(JPanel notesPanel) {
		notesPanel.removeAll();
		NoteButton.yPos = 10;
		notesPanel.setPreferredSize(new Dimension(270, Settings.notes.size() * 100 + 100));
		
		Settings.stream(Settings.notes)
				.map(note -> new NoteButton(note, notesPanel))
				.forEach(notesPanel::add);
		
		var addNoteButton = new JButton(GuiTools.getIcon("addnote.png", 0));
		addNoteButton.setBackground(Color.GRAY);
		addNoteButton.setBounds(10, NoteButton.yPos + 10, 250, 80);
		addNoteButton.addActionListener(e -> addNewNote(notesPanel));
		notesPanel.add(addNoteButton);
	}
	
	private static void addNewNote(JPanel panel) {
		var noteDialog = new JDialog((JFrame)null, "�j Jegyzet", true);
		noteDialog.setLayout(null);
		noteDialog.setResizable(false);
		
		var noteField = new JTextArea("Jegyzet sz�vege");
		noteField.setBounds(20, 20, 260, 100);
		
		var buttons = new ArrayList<JButton>();
		Consumer<JButton> listener = button -> {
			button.setBorder(new LineBorder(Color.RED, 4));
			buttons.stream()
			   	   .filter(passed -> !passed.getBackground().equals(button.getBackground()))
			   	   .forEach(passed -> passed.setBorder(null));
		};
		buttons.add(GuiTools.newColorButton(20, 150, listener, Color.YELLOW));
		buttons.add(GuiTools.newColorButton(80, 150, listener, Color.RED));
		buttons.add(GuiTools.newColorButton(140, 150, listener, Color.GREEN));
		buttons.add(GuiTools.newColorButton(200, 150, listener, Color.CYAN));
		
		var saveButton = new JButton("Ment�s");
		saveButton.setBounds(100, 300, 120, 40);
		saveButton.setBackground(Color.LIGHT_GRAY);
		saveButton.addActionListener(e -> buttons.stream().filter(button -> button.getBorder() != null).findFirst()
				  .ifPresentOrElse(button -> {
					  Settings.notes.add(Settings.newNoteObject(noteField.getText(), button.getBackground()));
					  initNotes(panel);
					  noteDialog.dispose();
					  Settings.save();
				  }, () -> JOptionPane.showMessageDialog(noteDialog, "Nincs sz�n be�ll�tva a jegyzetnek!"))
		);
		
		buttons.forEach(noteDialog::add);
		noteDialog.add(noteField);
		noteDialog.add(saveButton);
		noteDialog.setBounds(0, 0, 300, 400);
		noteDialog.setLocationRelativeTo(null);
		noteDialog.setVisible(true);
	}
}