package degubi.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.google.gson.JsonObject;

import degubi.tools.GuiTools;
import degubi.tools.Settings;

public final class NoteButton extends JButton{
	private static final ImageIcon deleteIcon = GuiTools.getIcon("delete.png", 28);
	public static int yPos = 10;
	
	public NoteButton(JsonObject note, JPanel panel) {
		super("<html>" + note.get("message").getAsString().replace("\n", "<br>"));
		
		JButton deleteButton = new JButton(deleteIcon);
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
		
		StreamSupport.stream(Settings.notes.spliterator(), false)
					 .map(note -> new NoteButton(note.getAsJsonObject(), notesPanel))
					 .forEach(notesPanel::add);
		
		JButton addNoteButton = new JButton(GuiTools.getIcon("addnote.png", 0));
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
		buttons.add(GuiTools.newColorButton(20, 150, listener, Color.YELLOW));
		buttons.add(GuiTools.newColorButton(80, 150, listener, Color.RED));
		buttons.add(GuiTools.newColorButton(140, 150, listener, Color.GREEN));
		buttons.add(GuiTools.newColorButton(200, 150, listener, Color.CYAN));
		
		JButton saveButton = new JButton("Mentés");
		saveButton.setBounds(100, 300, 120, 40);
		saveButton.setBackground(Color.LIGHT_GRAY);
		saveButton.addActionListener(e -> buttons.stream().filter(button -> button.getBorder() != null).findFirst()
				  .ifPresentOrElse(button -> {
					  Settings.notes.add(Settings.newNoteObject(noteField.getText(), button.getBackground()));
					  initNotes(panel);
					  noteDialog.dispose();
					  Settings.save();
				  }, () -> JOptionPane.showMessageDialog(noteDialog, "Nincs szín beállítva a jegyzetnek!"))
		);
		
		buttons.forEach(noteDialog::add);
		noteDialog.add(noteField);
		noteDialog.add(saveButton);
		noteDialog.setBounds(0, 0, 300, 400);
		noteDialog.setLocationRelativeTo(null);
		noteDialog.setVisible(true);
	}
}