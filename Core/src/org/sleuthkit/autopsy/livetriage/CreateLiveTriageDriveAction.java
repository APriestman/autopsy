/*
 * Autopsy Forensic Browser
 *
 * Copyright 2018 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this abstractFile except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.livetriage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.util.logging.Level;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JOptionPane;
import java.awt.Frame;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.core.UserPreferences;
import org.sleuthkit.autopsy.coreutils.FileUtil;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.progress.ModalDialogProgressIndicator;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.SpecialDirectory;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;

@ActionID(category = "Tools", id = "org.sleuthkit.autopsy.livetriage.CreateLiveTriageDriveAction")
@ActionReference(path = "Menu/Tools", position = 1850, separatorBefore = 1849)
@ActionRegistration(displayName = "#CTL_CreateLiveTriageDriveAction", lazy = false)
@NbBundle.Messages({"CTL_CreateLiveTriageDriveAction=Make Live Triage Drive"})
public final class CreateLiveTriageDriveAction extends CallableSystemAction implements PropertyChangeListener {

    private static final String DISPLAY_NAME = Bundle.CTL_CreateLiveTriageDriveAction();
    private ModalDialogProgressIndicator progressIndicator = null;
    private String drivePath = "";
    private CopyFilesWorker worker;
    private final Map<String, SpecialDirectory> extractedFileDirMap = new HashMap<>();

    @Override
    public boolean isEnabled() {
        return true;
    }

    @NbBundle.Messages({"CreateLiveTriageDriveAction.error.title=Error creating live triage disk",
        "CreateLiveTriageDriveAction.exenotfound.message=Executable could not be found",
        "CreateLiveTriageDriveAction.batchFileError.message=Error creating batch file",
        "CreateLiveTriageDriveAction.appPathError.message=Could not location application directory",
        "CreateLiveTriageDriveAction.copyError.message=Could not copy application. Only works on installed version.",
        "CreateLiveTriageDriveAction.success.title=Success",
        "CreateLiveTriageDriveAction.success.message=Live triage drive created. Use RunFromUSB.bat to run the application"
    })
    @Override
    @SuppressWarnings("fallthrough")
    public void performAction() {
        
                        // OK HERE WE GO
        try{ 
            System.out.println("\n\n##### Trying to make portable case!");
            
            // Fake it
            String autFileName = "PortableCaseTest.aut";
            String reportDir = Case.getCurrentCase().getReportDirectory();
            String dbFilePath = Paths.get(reportDir, "autopsy.db").toString();
            PrintWriter writer = new PrintWriter(Paths.get(reportDir, autFileName).toString(), "UTF-8");
            String data = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + 
                "<AutopsyCase>" + 
                "  <SchemaVersion>4.0</SchemaVersion>" + 
                "  <CreatedDate>2019/01/08 15:13:03 (EST)</CreatedDate>" + 
                "  <ModifiedDate>2019/01/08 15:13:05 (EST)</ModifiedDate>" + 
                "  <CreatedByAutopsyVersion>4.10.0</CreatedByAutopsyVersion>" + 
                "  <SavedByAutopsyVersion>4.10.0</SavedByAutopsyVersion>" + 
                "  <Case>" + 
                "    <Name>PortableCaseTest1_20190108_151303</Name>" + 
                "    <DisplayName>PortableCaseTest</DisplayName>" + 
                "    <Number/>" + 
                "    <Examiner/>" + 
                "    <ExaminerPhone/>" + 
                "    <ExaminerEmail/>" + 
                "    <CaseNotes/>" + 
                "    <CaseType>Single-user case</CaseType>" + 
                "    <Database/>" + 
                "    <CaseDatabase>autopsy.db</CaseDatabase>" + 
                "    <TextIndex/>" + 
                "  </Case>" + 
                "</AutopsyCase>";
            writer.println(data);
            writer.close();
            
            SleuthkitCase skCase = SleuthkitCase.newCase(dbFilePath);
            Content dataSource;
            
            SleuthkitCase.CaseDbTransaction trans = null;
            try {
                trans = skCase.beginTransaction();
                dataSource = skCase.addLocalFilesDataSource("12345", "PortableLocalFilesSet", "", trans);
                trans.commit();
            } catch (Exception ex) {
                trans.rollback();
                ex.printStackTrace();
                return;
            }
            
            List<AbstractFile> files = Case.getCurrentCase().getSleuthkitCase().findAllFilesWhere("obj_id > 1");
            System.out.println("### Going to copy " + files.size() + " files");
            
            for (AbstractFile abstractFile:files) {
                
                if ( ! abstractFile.getName().contains("t")) {
                    continue;
                }
                
                File localFile= new File(reportDir, abstractFile.getId() + "-" + FileUtil.escapeFileName(abstractFile.getName()));
                System.out.println("###   Copying to file " + localFile.getAbsolutePath());
                ContentUtils.writeToFile(abstractFile, localFile);
                
                trans = null;
                try {
                    trans = skCase.beginTransaction();
                    AbstractFile file;

                    file = skCase.addLocalFile(localFile.getName(), localFile.getAbsolutePath(), abstractFile.getSize(),
                            abstractFile.getCtime(), abstractFile.getCrtime(), abstractFile.getAtime(), abstractFile.getMtime(),
                            true, TskData.EncodingType.NONE, 
                            getExtractedFileDir((new File(abstractFile.getParentPath())), skCase, dataSource), trans);

                    trans.commit();
                } catch (TskCoreException ex) {
                    if (null != trans) {
                        try {
                            trans.rollback();
                        } catch (TskCoreException ex2) {
                            //logger.log(Level.SEVERE, String.format("Failed to rollback transaction after exception: %s", ex.getMessage()), ex2);
                        }
                    }
                    ex.printStackTrace();
                    return;
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        System.out.println("#######\n");

        Frame mainWindow = WindowManager.getDefault().getMainWindow();

        // If this is an installed version, there should be an <appName>64.exe abstractFile in the bin folder
        String appName = UserPreferences.getAppName();
        String exeName = appName + "64.exe";
        String installPath = PlatformUtil.getInstallPath();

        Path exePath = Paths.get(installPath, "bin", exeName);

        if (!exePath.toFile().exists()) {
            JOptionPane.showMessageDialog(mainWindow,
                    Bundle.CreateLiveTriageDriveAction_exenotfound_message(),
                    Bundle.CreateLiveTriageDriveAction_error_title(),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Path applicationBasePath;
        try {
            applicationBasePath = exePath.getParent().getParent();
        } catch (InvalidPathException ex) {
            JOptionPane.showMessageDialog(mainWindow,
                    Bundle.CreateLiveTriageDriveAction_appPathError_message(),
                    Bundle.CreateLiveTriageDriveAction_error_title(),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        SelectDriveDialog driveDialog = new SelectDriveDialog(mainWindow, true);
        driveDialog.display();

        if (!driveDialog.getSelectedDrive().isEmpty()) {
            drivePath = driveDialog.getSelectedDrive();
            if (drivePath.startsWith("\\\\.\\")) {
                drivePath = drivePath.substring(4);
            }

            worker = new CopyFilesWorker(applicationBasePath, drivePath, appName);
            worker.addPropertyChangeListener(this);
            worker.execute();
        }
    }
    
    private SpecialDirectory getExtractedFileDir(File file, SleuthkitCase skCase, Content dataSource) throws TskCoreException {
        if ((file == null) || file.getPath().isEmpty()) {
            throw new TskCoreException("Can not create directory from null path");
        }

        if (extractedFileDirMap.containsKey(file.toString())) {
            return extractedFileDirMap.get(file.toString());
        }

        File parent = file.getParentFile();
        if (parent == null) {
            // This is the root of the path and it isn't in the map, so create it
            SpecialDirectory dir = createdExtractedFilesDir(dataSource.getId(), file.getName(), skCase);
            extractedFileDirMap.put(file.getName(), dir);
            return dir;

        } else {
            // Create everything above this in the tree, and then add the parent folder
            SpecialDirectory parentDir = getExtractedFileDir(parent, skCase, dataSource);
            SpecialDirectory dir = createdExtractedFilesDir(parentDir.getId(), file.getName(), skCase);
            extractedFileDirMap.put(file.getPath(), dir);
            return dir;
        }
    }  
    
    private SpecialDirectory createdExtractedFilesDir(long parentId, String name, SleuthkitCase sleuthkitCase) throws TskCoreException {
        SleuthkitCase.CaseDbTransaction trans = null;

        try {
            trans = sleuthkitCase.beginTransaction();
            SpecialDirectory dir;

            dir = sleuthkitCase.addLocalDirectory(parentId, name, trans);

            trans.commit();
            return dir;
        } catch (TskCoreException ex) {
            if (null != trans) {
                try {
                    trans.rollback();
                } catch (TskCoreException ex2) {
                    //logger.log(Level.SEVERE, String.format("Failed to rollback transaction after exception: %s", ex.getMessage()), ex2);
                }
            }
            throw ex;
        }
    }    

    @NbBundle.Messages({"# {0} - drivePath",
        "CreateLiveTriageDriveAction.progressBar.text=Copying live triage files to {0}",
        "CreateLiveTriageDriveAction.progressBar.title=Please wait"})
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if ("state".equals(evt.getPropertyName())
                && (SwingWorker.StateValue.STARTED.equals(evt.getNewValue()))) {

            // Setup progress bar.
            String displayStr = NbBundle.getMessage(this.getClass(), "CreateLiveTriageDriveAction.progressBar.text",
                    drivePath);

            progressIndicator = new ModalDialogProgressIndicator(WindowManager.getDefault().getMainWindow(),
                    NbBundle.getMessage(this.getClass(), "CreateLiveTriageDriveAction.progressBar.title"));
            progressIndicator.start(displayStr);

        } else if ("state".equals(evt.getPropertyName())
                && (SwingWorker.StateValue.DONE.equals(evt.getNewValue()))) {
            if (progressIndicator != null) {
                progressIndicator.finish();
            }

            if (worker.hadError()) {
                MessageNotifyUtil.Message.error(NbBundle.getMessage(CopyFilesWorker.class, "CopyFilesWorker.error.text"));
            } else {
                MessageNotifyUtil.Message.info(NbBundle.getMessage(CopyFilesWorker.class, "CopyFilesWorker.done.text"));
            }
        }
    }

    private class CopyFilesWorker extends SwingWorker<Void, Void> {

        private final Path sourceFolder;
        private final String drivePath;
        private final String appName;
        private boolean error = false;

        CopyFilesWorker(Path sourceFolder, String drivePath, String appName) {
            this.sourceFolder = sourceFolder;
            this.drivePath = drivePath;
            this.appName = appName;
        }

        boolean hadError() {
            return error;
        }

        @Override
        protected Void doInBackground() throws Exception {

            copyBatchFile(drivePath, appName);
            copyApplication(sourceFolder, drivePath, appName);

            return null;
        }

        @NbBundle.Messages({"CopyFilesWorker.error.text=Error copying live triage files",
            "CopyFilesWorker.done.text=Finished creating live triage disk"})
        @Override
        protected void done() {
            try {
                super.get();
            } catch (Exception ex) {
                error = true;
                Logger.getLogger(CreateLiveTriageDriveAction.class.getName()).log(Level.SEVERE, "Fatal error during live triage drive creation", ex); //NON-NLS                
            }
        }
    }

    private void copyApplication(Path sourceFolder, String destBaseFolder, String appName) throws IOException {

        // Create an appName folder in the destination 
        Path destAppFolder = Paths.get(destBaseFolder, appName);
        if (!destAppFolder.toFile().exists()) {
            if (!destAppFolder.toFile().mkdirs()) {
                throw new IOException("Failed to create directory " + destAppFolder.toString());
            }
        }

        // Now copy the files
        FileUtils.copyDirectory(sourceFolder.toFile(), destAppFolder.toFile());
    }

    private void copyBatchFile(String destPath, String appName) throws IOException, InvalidPathException {
        Path batchFilePath = Paths.get(destPath, "RunFromUSB.bat");
        FileUtils.writeStringToFile(batchFilePath.toFile(), getBatchFileContents(appName), "UTF-8");

    }

    private String getBatchFileContents(String appName) {

        String batchFile
                = "@echo off\n"
                + "\n"
                + "REM This restores the working directory when using 'Run as administrator'"
                + "@setlocal enableextensions\n"
                + "@cd /d \"%~dp0\""
                + "\n"
                + "SET appName=\"" + appName + "\"\n"
                + "\n"
                + "REM Create the configData directory. Exit if it does not exist after attempting to create it\n"
                + "if not exist configData mkdir configData\n"
                + "if not exist configData (\n"
                + "        echo Error creating directory configData\n"
                + "        goto end\n"
                + ")\n"
                + "\n"
                + "REM Create the userdir sub directory. Exit if it does not exist after attempting to create it\n"
                + "if not exist configData\\userdir mkdir configData\\userdir\n"
                + "if not exist configData\\userdir (\n"
                + "        echo Error creating directory configData\\userdir\n"
                + "        goto end\n"
                + ")\n"
                + "\n"
                + "REM Create the cachedir sub directory. Exit if it does not exist after attempting to create it\n"
                + "REM If it exists to start with, delete it to clear out old data\n"
                + "if exist configData\\cachedir rd /s /q configData\\cachedir\n"
                + "mkdir configData\\cachedir\n"
                + "if not exist configData\\cachedir (\n"
                + "        echo Error creating directory configData\\cachedir\n"
                + "        goto end\n"
                + ")\n"
                + "\n"
                + "REM Create the temp sub directory. Exit if it does not exist after attempting to create it\n"
                + "REM If it exists to start with, delete it to clear out old data\n"
                + "if exist configData\\temp rd /s /q configData\\temp\n"
                + "mkdir configData\\temp\n"
                + "if not exist configData\\temp (\n"
                + "        echo Error creating directory configData\\temp\n"
                + "        goto end\n"
                + ")\n"
                + "\n"
                + "REM Create the cases directory. It's ok if this fails.\n"
                + "if not exist cases mkdir cases\n"
                + "\n"
                + "if exist %appName% (\n"
                + "        if not exist %appName%\\bin\\%appName%64.exe (\n"
                + "                echo %appName%\\bin\\%appName%64.exe does not exist\n"
                + "                goto end\n"
                + "        )\n"
                + "        %appName%\\bin\\%appName%64.exe --userdir ..\\configData\\userdir --cachedir ..\\configData\\cachedir -J-Djava.io.tmpdir=..\\configData\\temp --liveAutopsy\n"
                + ") else (\n"
                + "        echo Could not find %appName% directory\n"
                + "        goto end\n"
                + ")\n"
                + "\n"
                + ":end\n"
                + "\n"
                + "REM Keep the cmd window open in case there was an error\n"
                + "@pause\n";
        return batchFile;
    }

    @Override
    public String getName() {
        return DISPLAY_NAME;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean asynchronous() {
        return false; // run on edt
    }
}
