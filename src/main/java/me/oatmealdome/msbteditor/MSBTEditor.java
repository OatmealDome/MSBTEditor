// Licensed under WTFPL (Version 2)
// Refer to the license.txt attached.

package me.oatmealdome.msbteditor;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.DatatypeConverter;

import me.oatmealdome.util.AssortedUtil;

/**
 * The main GUI class of the MSBTEditor application.
 * 
 * @author OatmealDome
 *
 */
public class MSBTEditor {

	private JFrame frame;
	private JList<String> list;
	private JTextArea textArea;
	private JTextArea atrArea;
	private JFileChooser chooser;
	private MSBTFile msbtFile = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MSBTEditor window = new MSBTEditor();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MSBTEditor() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 726, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JToolBar toolBar = new JToolBar();
		toolBar.setBounds(0, 0, 704, 23);
		frame.getContentPane().add(toolBar);

		JButton btnSomething = new JButton("Open");
		btnSomething.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Load MSBT
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					loadMSBT(chooser.getSelectedFile().getAbsolutePath());
					textArea.setText("");
					atrArea.setText("");
				}
			}
		});
		toolBar.add(btnSomething);

		DefaultListModel<String> listModel = new DefaultListModel<String>();

		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (msbtFile != null && list.getSelectedIndex() != -1) {
						textArea.setText(msbtFile.text[list.getSelectedIndex()]);
						textArea.setCaretPosition(0);
						String atr1 = DatatypeConverter.printHexBinary(msbtFile.atrData[list.getSelectedIndex()]);
						atr1 = atr1.replaceAll("(.{20})", "$1\n"); // Thanks npinti on StackOverflow!
						atrArea.setText(atr1);
						atrArea.setCaretPosition(0);
					}
				}
			}
		});

		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBounds(10, 27, 337, 423);
		frame.getContentPane().add(listScrollPane);

		textArea = new JTextArea(" No MSBT loaded.");
		textArea.setEditable(false);

		JScrollPane textScrollPane = new JScrollPane(textArea);
		textScrollPane.setBounds(354, 27, 345, 211);
		frame.getContentPane().add(textScrollPane);

		atrArea = new JTextArea();
		atrArea.setEditable(false);
		atrArea.setLineWrap(true);

		JScrollPane atrScrollPane = new JScrollPane(atrArea);
		atrScrollPane.setBounds(354, 241, 345, 209);
		frame.getContentPane().add(atrScrollPane);

		chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("MSBT Files", "msbt");
		chooser.setFileFilter(filter);

	}

	private void loadMSBT(String file) {
		RandomAccessFile ras = null;
		msbtFile = null;
		try {
			ras = new RandomAccessFile(file, "r");
			msbtFile = MSBTReader.readMSBT(ras);
		} catch (Exception e) {
			e.printStackTrace();
			showException(e);
			return;
		} finally {
			if (ras == null)
				return;
			try {
				ras.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				showException(ioe);
				return;
			}
		}

		DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (int i = 0; i < msbtFile.text.length; i++) {
			listModel.addElement(msbtFile.labels[i]);
		}
		list.setModel(listModel);
	}
	
	private void showException(Throwable t) {
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		listModel.addElement("An error has occurred.");
		list.setModel(listModel);
		textArea.setText(AssortedUtil.stackTraceAsString(t));
		textArea.setCaretPosition(0);
		atrArea.setText("");
	}

}
