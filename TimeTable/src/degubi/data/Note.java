package degubi.data;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import degubi.gui.NoteButton;
import degubi.gui.PopupGuis;
import degubi.tools.IPropertyObjectBuilder;
import degubi.tools.NIO;
import degubi.tools.PropertyFile;

public final class Note{
	public static final NoteBuilder builder = new NoteBuilder();
	
	public final String message;
	public final Color color;
	
	public Note(String message, Color color) {
		this.message = message;
		this.color = color;
	}
	
	public static void initNotes(JPanel notesPanel) {
		notesPanel.removeAll();
		NoteButton.yPos = 10;
		notesPanel.setPreferredSize(new Dimension(270, PropertyFile.notes.size() * 100 + 100));
		
		PropertyFile.notes.stream()
					.map(note -> new NoteButton(note, notesPanel))
					.forEach(notesPanel::add);
		
		JButton addNoteButton = new JButton(NIO.getIcon("addnote.png", 0));
		addNoteButton.setBackground(Color.GRAY);
		addNoteButton.setBounds(10, NoteButton.yPos + 10, 250, 80);
		addNoteButton.addActionListener(e -> addNewNote(notesPanel));
		notesPanel.add(addNoteButton);
	}
	
	private static void addNewNote(JPanel panel) {
		JDialog noteDialog = new JDialog((JFrame)null, "Új Jegyzet", true);
		noteDialog.setLayout(null);
		noteDialog.setResizable(false);
		
		JTextArea noteField = new JTextArea("Jegyzet szövege");
		noteField.setBounds(20, 20, 260, 100);
		
		List<JButton> buttons = new ArrayList<>();
		Consumer<JButton> listener = button -> {
			button.setBorder(new LineBorder(Color.RED, 4));
			buttons.stream()
			   	   .filter(passed -> !passed.getBackground().equals(button.getBackground()))
			   	   .forEach(passed -> passed.setBorder(null));
		};
		buttons.add(PopupGuis.newColorButton(20, 150, listener, Color.YELLOW));
		buttons.add(PopupGuis.newColorButton(80, 150, listener, Color.RED));
		buttons.add(PopupGuis.newColorButton(140, 150, listener, Color.GREEN));
		buttons.add(PopupGuis.newColorButton(200, 150, listener, Color.CYAN));
		
		JButton saveButton = new JButton("Mentés");
		saveButton.setBounds(100, 300, 120, 40);
		saveButton.setBackground(Color.LIGHT_GRAY);
		saveButton.addActionListener(e -> buttons.stream().filter(button -> button.getBorder() != null).findFirst()
				  .ifPresentOrElse(button -> {
					  PropertyFile.notes.add(new Note(noteField.getText(), button.getBackground()));
					  initNotes(panel);
					  PropertyFile.setObjectList("notes", builder, PropertyFile.notes);
					  noteDialog.dispose();
				  }, () -> JOptionPane.showMessageDialog(noteDialog, "Nincs szín beállítva a jegyzetnek!"))
		);
		
		buttons.forEach(noteDialog::add);
		noteDialog.add(noteField);
		noteDialog.add(saveButton);
		noteDialog.setBounds(0, 0, 300, 400);
		noteDialog.setLocationRelativeTo(null);
		noteDialog.setVisible(true);
	}
	
	private static final class NoteBuilder implements IPropertyObjectBuilder<Note>{

		@Override
		public String writeObject(Note note) {
			Color color = note.color;
			return note.message.replace(' ', '_') + " " + color.getRed() + " " + color.getGreen() + " " + color.getBlue();
		}

		@Override
		public Note readObject(String dataStr) {
			String[] split = dataStr.split(" ");
			return new Note(split[0].replace('_', ' '), new Color(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
		}
	}
}