/**
 * Copyright Copyright 2010-15 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.BamQC.Dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.ac.babraham.BamQC.BamQCApplication;
import uk.ac.babraham.BamQC.DataTypes.Genome.Genome;
import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;

/**
 * A Dialog to allow the viewing and editing of all BamQC preferences.
 */
public class EditPreferencesDialog extends JDialog implements ActionListener,ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9120924960274548107L;

	/** The genome base. */
	private JTextField genomeBase;
	
	/** The data location. */
	private JTextField dataLocation;
	
	/** The save location. */
	private JTextField saveLocation;
	
	/** The proxy host. */
	private JTextField proxyHost;
	
	/** The proxy port. */
	private JTextField proxyPort;
	
	/** The download location. */
	private JTextField downloadLocation;
	
	/** The ignored features. */
	private JList ignoredFeatures;
	
	/** The temp directory. */
	private JTextField tempDirectory;
	
	/** The ignored features model. */
	private DefaultListModel ignoredFeaturesModel;
	
	/** The add. */
	private JButton add;
	
	/** The remove. */
	private JButton remove;
	
	private Genome genome;
		
	/**
	 * Instantiates a new edits the preferences dialog.
	 * 
	 * @param application the application
	 */
	public EditPreferencesDialog (Genome genome) {
		super(BamQCApplication.getInstance(),"Edit Preferences...");
		this.genome = genome;
		setSize(600,280);
		setLocationRelativeTo(BamQCApplication.getInstance());
		setModal(true);
		BamQCPreferences p = BamQCPreferences.getInstance();
		
		JTabbedPane tabs = new JTabbedPane();

		JPanel filePanel = new JPanel();
		filePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		filePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0.1;
		c.weighty=0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		filePanel.add(new JLabel("Genome Base Location"),c);
		c.gridx=1;
		c.weightx=0.5;
		genomeBase = new JTextField();
		try {
			genomeBase.setText(p.getGenomeBase().getAbsolutePath());
		}
		catch (FileNotFoundException e){
			JOptionPane.showMessageDialog(this, "Couldn't find the folder which was supposed to hold the genomes", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		genomeBase.setEditable(false);
		filePanel.add(genomeBase,c);
		c.gridx=2;
		c.weightx=0.1;
		JButton genomeButton = new JButton("Browse");
		genomeButton.setActionCommand("genomeBase");
		genomeButton.addActionListener(this);
		filePanel.add(genomeButton,c);
		
		c.gridx=0;
		c.gridy++;
		c.weightx=0.1;
		filePanel.add(new JLabel("Default Data Location"),c);
		c.gridx=1;
		c.weightx=0.5;
		dataLocation = new JTextField(p.getDataLocationPreference().getAbsolutePath());
		dataLocation.setEditable(false);
		filePanel.add(dataLocation,c);
		c.gridx=2;
		c.weightx=0.1;
		JButton dataButton = new JButton("Browse");
		dataButton.setActionCommand("dataLocation");
		dataButton.addActionListener(this);
		filePanel.add(dataButton,c);

		c.gridx=0;
		c.gridy++;
		c.weightx=0.1;
		filePanel.add(new JLabel("Default Save Location"),c);
		c.gridx=1;
		c.weightx=0.5;
		saveLocation = new JTextField(p.getSaveLocationPreference().getAbsolutePath());
		saveLocation.setEditable(false);
		filePanel.add(saveLocation,c);
		c.gridx=2;
		c.weightx=0.1;
		JButton saveLocationButton = new JButton("Browse");
		saveLocationButton.setActionCommand("saveLocation");
		saveLocationButton.addActionListener(this);
		filePanel.add(saveLocationButton,c);

		tabs.addTab("Files",filePanel);

		
		c.gridx=1;
		c.weightx=0.5;
		JPanel tempDirPanel = new JPanel();
		tempDirPanel.setLayout(new BorderLayout());

		tempDirectory = new JTextField();
		if (p.tempDirectory() != null) {
			tempDirectory.setText(p.tempDirectory().getAbsolutePath());
		}
		tempDirectory.setEditable(false);
		tempDirPanel.add(tempDirectory,BorderLayout.CENTER);
		JButton tempDirBrowseButton = new JButton("Browse");
		tempDirBrowseButton.setActionCommand("tempDir");
		tempDirBrowseButton.addActionListener(this);
		tempDirPanel.add(tempDirBrowseButton,BorderLayout.EAST);
		
	
		c.gridx=0;
		c.gridy++;
		
		JPanel featureTypesPanel = new JPanel();
		featureTypesPanel.setLayout(new BorderLayout());
		ignoredFeaturesModel = new DefaultListModel();
		String [] f = p.getIgnoredFeatures();
		for (int i=0;i<f.length;i++) {
//			System.out.println("Added "+f[i]);
			ignoredFeaturesModel.addElement(f[i]);
		}
		ignoredFeatures = new JList(ignoredFeaturesModel);
		ignoredFeatures.addListSelectionListener(this);
		featureTypesPanel.add(new JScrollPane(ignoredFeatures),BorderLayout.CENTER);
		JPanel featureTypesButtons = new JPanel();
		featureTypesButtons.setLayout(new GridLayout(2,1));
		add = new JButton("Add");
		add.setActionCommand("addFeature");
		add.addActionListener(this);
		featureTypesButtons.add(add);
		remove = new JButton("Remove");
		remove.setActionCommand("removeFeature");
		remove.addActionListener(this);
		remove.setEnabled(false);
		featureTypesButtons.add(remove);
		featureTypesPanel.add(featureTypesButtons,BorderLayout.EAST);
			
		
		JPanel networkPanel = new JPanel();
		networkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		
		networkPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0.1;
		c.weighty=0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		networkPanel.add(new JLabel("HTTP Proxy server"),c);
		c.gridx=1;
		c.weightx=0.5;
		proxyHost = new JTextField(p.proxyHost());
		networkPanel.add(proxyHost,c);

		c.gridx=0;
		c.gridy++;
		c.weightx=0.1;
		networkPanel.add(new JLabel("HTTP Proxy port"),c);
		c.gridx=1;
		c.weightx=0.5;
		proxyPort = new JTextField(""+p.proxyPort());
		networkPanel.add(proxyPort,c);
		
		c.gridx=0;
		c.gridy++;
		c.weightx=0.1;
		networkPanel.add(new JLabel("Genome Download URL"),c);
		c.gridx=1;
		c.weightx=0.5;
		downloadLocation = new JTextField(p.getGenomeDownloadLocation());
		networkPanel.add(downloadLocation,c);
		tabs.addTab("Network",networkPanel);
		
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabs, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		
		JButton okButton = new JButton("Save");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		
		getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		setVisible(true);
	}

	/**
	 * Launches a file browser to select a directory
	 * 
	 * @param f the TextFild from which to take the starting directory
	 * @return the selected directory
	 */
	private void getDir (JTextField f) {
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new File(f.getText()));
	    chooser.setDialogTitle("Select Directory");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	f.setText(chooser.getSelectedFile().getAbsolutePath());
	    }
	}

	private void getFile (JTextField f) {
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new File(f.getText()));
	    chooser.setDialogTitle("Select File");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	f.setText(chooser.getSelectedFile().getAbsolutePath());
	    }
	}

	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		String c = ae.getActionCommand();
		
		if (c.equals("genomeBase")) {
			getDir(genomeBase);
		}
		else if (c.equals("dataLocation")) {
			getDir(dataLocation);
		}
		else if (c.equals("saveLocation")) {
			getDir(saveLocation);
		}
		else if (c.equals("tempDir")) {
			getDir(tempDirectory);
		}
		else if (c.equals("removeFeature")) {
			Object [] o = ignoredFeatures.getSelectedValues();
			for (int i=0;i<o.length;i++) {
				ignoredFeaturesModel.removeElement(o[i]);
			}
		}
		else if (c.equals("addFeature")) {
			String featureName=null;
			FeatureSelector ufs = new FeatureSelector(this);
			while (true) {
				featureName = ufs.getFeatureName();
				if (featureName == null)
					return;  // They cancelled
					
					
				if (featureName.length() == 0)
					continue; // Try again
				
				break;
			}
			ufs.dispose();
			ignoredFeaturesModel.addElement(featureName);
		}
		else if (c.equals("cancel")) {
			setVisible(false);
			dispose();
		}
		
		else if (c.equals("ok")) {
			File genomeBaseFile = new File(genomeBase.getText());
			if (genomeBaseFile == null || (! genomeBaseFile.exists())) {
				JOptionPane.showMessageDialog(this,"Invalid genome base location","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			File dataLocationFile = new File(dataLocation.getText());
			if (dataLocationFile == null || (! dataLocationFile.exists())) {
				JOptionPane.showMessageDialog(this,"Invalid data location","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			File saveLocationFile = new File(saveLocation.getText());
			if (saveLocationFile == null || (! saveLocationFile.exists())) {
				JOptionPane.showMessageDialog(this,"Invalid save location","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			File tempDirFile = null;
			if (tempDirectory.getText().length()>0) {
				tempDirFile = new File(tempDirectory.getText());
				if (! tempDirFile.exists()) {
					JOptionPane.showMessageDialog(this,"Invalid temp dir","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(this,"No temp dir specified","Error",JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String proxyHostValue = proxyHost.getText();
			int proxyPortValue = 0;
			if (proxyPort.getText().length()>0) {
				try {
					proxyPortValue = Integer.parseInt(proxyPort.getText());
				}
				catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(this,"Proxy port number was not an integer","Error",JOptionPane.ERROR_MESSAGE);
					return;				
				}
			}
			
			if (proxyHostValue.length()>0 && proxyPort.getText().length() == 0){
				JOptionPane.showMessageDialog(this,"You specified a proxy server address, but did not provide the port number (default is usually 80 or 8080)","Error",JOptionPane.ERROR_MESSAGE);
				return;								
			}
			
			
			// OK that's everything which could have gone wrong.  Let's save it
			// to the preferences file
			
			BamQCPreferences p = BamQCPreferences.getInstance();
			
			p.setDataLocation(dataLocationFile);
			p.setSaveLocation(saveLocationFile);
			p.setGenomeBase(genomeBaseFile);
			p.setProxy(proxyHostValue,proxyPortValue);
			p.setGenomeDownloadLocation(downloadLocation.getText());
			p.setTempDirectory(tempDirFile);
			Object [] o = ignoredFeaturesModel.toArray();
			String [] s = new String[o.length];
			for (int i=0;i<s.length;i++) {
				s[i] = (String)o[i];
			}
			p.setIgnoredFeatures(s);
			
			try {
				p.savePreferences();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setVisible(false);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if (ignoredFeatures.getSelectedIndices().length>0) {
			remove.setEnabled(true);
		}
		else {
			remove.setEnabled(false);
		}
	}
	
	/**
	 * The Class FeatureSelector.
	 */
	private class FeatureSelector extends JDialog implements ListSelectionListener, ActionListener {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2655517264575721041L;

		/** The cancelled. */
		private boolean cancelled = true;
		
		/** The input. */
		private JTextField input;
		
		/** The list. */
		private JList list;
		
		/**
		 * Instantiates a new feature selector.
		 * 
		 * @param c the c
		 */
		public FeatureSelector (Dialog c) {
			super(c);
			setTitle("Select feature name");
			setSize(250,250);
			setModal(true);
			setLocationRelativeTo(c);
			
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			input = new JTextField("[Enter feature name]");
			topPanel.add(input,BorderLayout.CENTER);
			topPanel.add(new JLabel("Currently loaded features"),BorderLayout.SOUTH);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(topPanel,BorderLayout.NORTH);

			if (genome != null) {
				list = new JList (genome.annotationSet().getAvailableFeatureTypes());
			}
			else {
				list = new JList ();
			}
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(this);
			getContentPane().add(new JScrollPane(list),BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel();

			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(this);
			buttonPanel.add(cancelButton);
			
			JButton okButton = new JButton("OK");
			okButton.setActionCommand("ok");
			okButton.addActionListener(this);
			buttonPanel.add(okButton);
			
			getContentPane().add(buttonPanel,BorderLayout.SOUTH);
			
			
		}
		
		/**
		 * Gets the feature name.
		 * 
		 * @return the feature name
		 */
		public String getFeatureName (){
			cancelled = true;
			input.setText("[Enter feature name]");
			setVisible(true);
			if (cancelled) {
				return null;
			}
			return input.getText();
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			String s = (String)list.getSelectedValue();
			if (s!=null) {
				input.setText(s);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("ok")) {
				cancelled = false;
			}
			if (input.getText().equals("[Enter feature name]")){
				input.setText("");
			}
			
			setVisible(false);
		}
	}
	
}
