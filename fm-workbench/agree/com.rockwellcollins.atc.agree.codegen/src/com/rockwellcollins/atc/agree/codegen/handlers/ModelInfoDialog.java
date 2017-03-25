package com.rockwellcollins.atc.agree.codegen.handlers;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ModelInfoDialog extends TitleAreaDialog{
	private final class DirChooserListner implements Listener {
		public void handleEvent(Event event) {
			// Get saved or entered output directory
			String existingDir = outputText.getText();
			// Prompt for updated output directory
			DirectoryDialog directoryChooser = new DirectoryDialog(parentShell, SWT.SAVE);
			directoryChooser.setMessage("Choose Output Directory");
			if (!existingDir.equals("")) {
				directoryChooser.setFilterPath(existingDir);
			}
			String updatedDir = directoryChooser.open();
			if (updatedDir != null) {
				outputText.setText(updatedDir);
			}
		}
	}

	private final class OriginalMdlChooserListner implements Listener {
		public void handleEvent(Event event) {
			// Get saved or entered file path
			String defaultPath = originalText.getText();
			// Prompt for updated output directory
			FileDialog fileChooser = new FileDialog(parentShell, SWT.OPEN);
			fileChooser.setFilterNames(new String[] { "Simulink Models (*.slx)" });
			fileChooser.setFilterExtensions(new String[] { "*.slx" });
			if (!defaultPath.equals("")) {
				fileChooser.setFileName(defaultPath);
			} else if (!outputText.getText().equals("")) {
				fileChooser.setFilterPath(outputText.getText());
			}
			String updatedFile = fileChooser.open();
			if (updatedFile != null) {
				originalText.setText(updatedFile);
			}
		}
	}

	private final Shell parentShell;
	private final ModelInfo savedInfo;
	private ModelInfo updatedInfo;

	private Text outputText;
	private ControlDecoration outputTextError;

	private Text originalText;
	private ControlDecoration originalTextError;

	private Text updatedText;
	private ControlDecoration updatedTextError;

	private Text subsystemText;
	
	private StringBuffer buffer = new StringBuffer();

	@Override
	public String toString() {
		return buffer.toString();
	}

	protected void write(Object o) {
		buffer.append(o);
	}

	private static final String seperator = System.getProperty("line.separator");

	private void newline() {
		write(seperator);
	}

	protected ModelInfoDialog(Shell parentShell, ModelInfo savedInfo) {
		super(parentShell);
		this.parentShell = parentShell;
		this.savedInfo = savedInfo;
	}

	public ModelInfo getModelInfo() {
		return updatedInfo;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Model Info for Inserting Simulink Observer (Script Generator)");
	}

	@Override
	protected boolean isResizable() {
		return true;

	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);

		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_ERROR);
		Image errorImage = fieldDecoration.getImage();

		GridLayout layout = new GridLayout(3, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);

		Label outputLabel = new Label(container, SWT.NONE);
		outputLabel.setText("Output Directory Path:");
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		outputLabel.setLayoutData(gridData);

		outputText = new Text(container, SWT.BORDER);
		outputText.setText(savedInfo.outputDirPath);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 450;
		gridData.horizontalIndent = 10;
		outputText.setLayoutData(gridData);
		outputText.addModifyListener(e -> validate());
		
		outputTextError = new ControlDecoration(outputText, SWT.TOP | SWT.LEFT);
		outputTextError.setImage(errorImage);
		outputTextError.hide();

		Button browseOutputButton = new Button(container, SWT.PUSH);
		browseOutputButton.setText("Browse");
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 100;
		browseOutputButton.setLayoutData(gridData);
		browseOutputButton.addListener(SWT.Selection, new DirChooserListner());

		Label originalLabel = new Label(container, SWT.NONE);
		originalLabel.setText("Original Model Path:");
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		originalLabel.setLayoutData(gridData);

		originalText = new Text(container, SWT.BORDER);
		originalText.setText(savedInfo.originalModelName);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 450;
		gridData.horizontalIndent = 10;
		originalText.setLayoutData(gridData);
		originalText.addModifyListener(e -> validate());

		originalTextError = new ControlDecoration(originalText, SWT.TOP | SWT.LEFT);
		originalTextError.setImage(errorImage);
		originalTextError.hide();

		Button browseOriginalButton = new Button(container, SWT.PUSH);
		browseOriginalButton.setText("Browse");
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 100;
		browseOriginalButton.setLayoutData(gridData);
		browseOriginalButton.addListener(SWT.Selection, new OriginalMdlChooserListner());

		Label updatedLabel = new Label(container, SWT.NONE);
		updatedLabel.setText("Model to Insert Observer:");
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 180;
		updatedLabel.setLayoutData(gridData);

		updatedText = new Text(container, SWT.BORDER);
		updatedText.setText(savedInfo.updatedModelName);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = 10;
		updatedText.setLayoutData(gridData);
		updatedText.addModifyListener(e -> validate());

		updatedTextError = new ControlDecoration(updatedText, SWT.TOP | SWT.LEFT);
		updatedTextError.setImage(errorImage);
		updatedTextError.hide();

		Label subsystemLabel = new Label(container, SWT.NONE);
		subsystemLabel.setText("Subsystem to Verify:");
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 150;
		subsystemLabel.setLayoutData(gridData);

		subsystemText = new Text(container, SWT.BORDER);
		subsystemText.setText(savedInfo.subsystemName);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = 10;
		subsystemText.setLayoutData(gridData);
		subsystemText.addModifyListener(e -> validate());

		return container;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ModelInfoDialogConstants.EXPORT_SCRIPT_ID,
				ModelInfoDialogConstants.EXPORT_LABEL, true);
		createButton(parent, ModelInfoDialogConstants.GENERATE_SUBSYSTEM_ID,
				ModelInfoDialogConstants.GENERATE_LABEL, true);
		createButton(parent, ModelInfoDialogConstants.UPDATE_MODEL_ID,
				ModelInfoDialogConstants.UPDATE_LABEL, true);
		createButton(parent, ModelInfoDialogConstants.VERIFY_SUBSYSTEM_ID,
				ModelInfoDialogConstants.VERIFY_LABEL, true);		
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}
	

	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		validate();
		return result;
	}

	protected void validate() {
		boolean exportButtonEnabled = false;
		boolean generateButtonEnabled = false;
		boolean updateButtonEnabled = false;
		boolean verifyButtonEnabled = false;		
		boolean outputDirError = false;
		boolean originalMdlError = false;
		boolean originalMdlEmpty = true;
		boolean updatedMdlError = false;
		boolean updatedMdlEmpty = true;
		buffer.setLength(0);

		if (outputText.getText().equals("")) {
			write("Must fill out the output directory");
			newline();
			outputDirError = true;
		} else if (!new File(outputText.getText()).exists()) {
			write("The output directory does not exist");
			newline();
			outputDirError = true;
		} else {
			exportButtonEnabled = true;
		}

		//not flagging an error if the originalText is empty
		if(!originalText.getText().equals("")){
			originalMdlEmpty = false;
			if (!new File(originalText.getText()).exists()) {
				write("Original model file does not exist");
				newline();
				originalMdlError = true;
			} else if (!originalText.getText().endsWith(".slx")) {
				write("Original model must have .slx extension");
				newline();
				originalMdlError = true;
			}	
		}
		
		//not flagging an error if the updatedText is empty
		if(!updatedText.getText().equals("")){
			updatedMdlEmpty = false;
			if (!updatedText.getText().endsWith(".slx")) {
				write("Updated model name must have .slx extension");
				newline();
				updatedMdlError = true;
			}
		}

		//not flagging an error if the subsystemText is empty
		if (!subsystemText.getText().equals("")) {
			if(!updatedMdlEmpty && !updatedMdlError){
				generateButtonEnabled = true;
			}
			if(exportButtonEnabled && !originalMdlEmpty && !originalMdlError && 
			   !updatedMdlEmpty && !updatedMdlError){
				updateButtonEnabled = true;
				verifyButtonEnabled = true;
			}	
		}

		setExportEnabled(exportButtonEnabled);
		setGenerateEnabled(generateButtonEnabled);
		setUpdateEnabled(updateButtonEnabled);
		setVerifyEnabled(verifyButtonEnabled);
		
		if(!outputDirError && !originalMdlError && !updatedMdlError){
			setErrorMessage(null);
		}
		else{
			setErrorMessage(buffer.toString());
		}
		
		if(outputDirError)
			outputTextError.show();
		else
			outputTextError.hide();
		if(originalMdlError)
			originalTextError.show();
		else
			originalTextError.hide();
		if(updatedMdlError)
			updatedTextError.show();
		else
			updatedTextError.hide();
	}

	protected void buttonPressed(int buttonId) {
		if (ModelInfoDialogConstants.EXPORT_SCRIPT_ID == buttonId) {
			exportPressed();
		} else if (ModelInfoDialogConstants.GENERATE_SUBSYSTEM_ID == buttonId) {
			generatePressed();
		} else if (ModelInfoDialogConstants.UPDATE_MODEL_ID == buttonId) {
			updatePressed();
		} else if (ModelInfoDialogConstants.VERIFY_SUBSYSTEM_ID == buttonId) {
			verifyPressed();	
		} else if (IDialogConstants.CANCEL_ID == buttonId) {
			cancelPressed();
		}
	}
	
	private void setExportEnabled(boolean enabled) {
		Button export = getButton(ModelInfoDialogConstants.EXPORT_SCRIPT_ID);
		if (export != null) {
			export.setEnabled(enabled);
		}
	}
	
	private void setGenerateEnabled(boolean enabled) {
		Button generate = getButton(ModelInfoDialogConstants.GENERATE_SUBSYSTEM_ID);
		if (generate != null) {
			generate.setEnabled(enabled);
		}
	}
	
	private void setUpdateEnabled(boolean enabled) {
		Button update = getButton(ModelInfoDialogConstants.UPDATE_MODEL_ID);
		if (update != null) {
			update.setEnabled(enabled);
		}
	}
	
	private void setVerifyEnabled(boolean enabled) {
		Button verify = getButton(ModelInfoDialogConstants.VERIFY_SUBSYSTEM_ID);
		if (verify != null) {
			verify.setEnabled(enabled);
		}
	}

	protected void exportPressed() {
		// for source text property saved in AADL, need to update the separator in the path string
		updatedInfo = new ModelInfo(outputText.getText(), originalText.getText(), updatedText.getText(),
				subsystemText.getText(), true, false, false, false);
		super.okPressed();
	}
	
	protected void generatePressed() {
		// for source text property saved in AADL, need to update the separator in the path string
		updatedInfo = new ModelInfo(outputText.getText(), originalText.getText(), updatedText.getText(),
				subsystemText.getText(), false, true, false, false);
		super.okPressed();
	}
	
	
	protected void updatePressed() {
		// for source text property saved in AADL, need to update the separator in the path string
		updatedInfo = new ModelInfo(outputText.getText(), originalText.getText(), updatedText.getText(),
				subsystemText.getText(), false, false, true, false);
		super.okPressed();
	}
	
	protected void verifyPressed() {
		// for source text property saved in AADL, need to update the separator in the path string
		updatedInfo = new ModelInfo(outputText.getText(), originalText.getText(), updatedText.getText(),
				subsystemText.getText(), false, false, false, true);
		super.okPressed();
	}

}
