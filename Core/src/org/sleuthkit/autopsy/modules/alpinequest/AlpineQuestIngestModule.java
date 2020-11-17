/*
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.sleuthkit.autopsy.modules.alpinequest;

import java.util.List;
import java.util.logging.Level;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Creates artifacts from AlpineQuest data.
 */
class AlpineQuestIngestModule implements DataSourceIngestModule {
    private static final Logger logger = Logger.getLogger(AlpineQuestIngestModule.class.getName());
    
    // The actual folder names are psyberia.alpinequest.free and psyberia.alpinequest.full
    private static final String ALPINE_QUEST_PATH = "psyberia.alpinequest";
    private static final String WAYPOINT_EXT = "wpt";
    private static final String TRACK_EXT = "trk";
    private static final String ROUTE_EXT = "rte";
    private static final String SET_EXT = "set";
    private static final String AREA_EXT = "are";
    private IngestJobContext context;
    private SleuthkitCase skCase;
    
    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {

        try {
            // Get all the AlpineQuest files
            List<AbstractFile> alpineQuestFiles = getAlpineQuestFiles();
            
            if (context.dataSourceIngestIsCancelled()) {
                return ProcessResult.OK;
            }
            
            // Set the progress bar for the total number of files
            int totalFiles = alpineQuestFiles.size();
            progressBar.switchToDeterminate(totalFiles);
            
            // Process the files
            logger.log(Level.INFO, "Processing {0} AlpineQuest files", alpineQuestFiles.size());
            for (AbstractFile file : alpineQuestFiles) {
                if (context.dataSourceIngestIsCancelled()) {
                    return ProcessResult.OK;
                }
                
                processFile(file);
                progressBar.progress(1);
            }   
            
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error processing Alpine Quest data", ex);
        }
        
        return ProcessResult.OK;
    }
    
    /**
     * Process an AlpineQuest file. Type is determined by extension.
     * 
     * @param file The AlpineQuest file.
     * 
     * @throws TskCoreException 
     */
    private void processFile(AbstractFile file) throws TskCoreException {
        if (context.dataSourceIngestIsCancelled()) {
            return;
        }
        
        if (WAYPOINT_EXT.equalsIgnoreCase(file.getNameExtension())) {
            WaypointFile waypointFile = WaypointFile.createFromAbstractFile(file);
            waypointFile.createArtifacts();
        } else if (TRACK_EXT.equalsIgnoreCase(file.getNameExtension())) {
            TrackFile trackFile = TrackFile.createFromAbstractFile(file);
            trackFile.createArtifacts();
        } else if (ROUTE_EXT.equalsIgnoreCase(file.getNameExtension())) {
            RouteFile routeFile = RouteFile.createFromAbstractFile(file);
            routeFile.createArtifacts();
        } else if (SET_EXT.equalsIgnoreCase(file.getNameExtension())) {
            SetFile setFile = SetFile.createFromAbstractFile(file);
            setFile.createArtifacts();
        } else if (AREA_EXT.equalsIgnoreCase(file.getNameExtension())) {
            AreaFile areaFile = AreaFile.createFromAbstractFile(file);
            areaFile.createArtifacts();
        }
    }
    
    /**
     * Find all GPS files under the AlpineQuest folder.
     * 
     * @return The list of files to process.
     * 
     * @throws TskCoreException 
     */
    private List<AbstractFile> getAlpineQuestFiles() throws TskCoreException {
        String whereClause = "data_source_obj_id = " + context.getDataSource().getId()
                + " AND ("
                + " extension = \"" + WAYPOINT_EXT + "\""
                + " OR extension = \"" + TRACK_EXT + "\""
                + " OR extension = \"" + ROUTE_EXT + "\""
                + " OR extension = \"" + SET_EXT + "\""
                + " OR extension = \"" + AREA_EXT + "\""
                + ") "
                + " AND parent_path LIKE \"%" + ALPINE_QUEST_PATH + "%\"";
        return skCase.findAllFilesWhere(whereClause);
    }
    
    /**
     * Get the program name.
     * 
     * @return The program name.
     */
    static String getProgramName() {
        return "AlpineQuest"; // NON-NLS
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        this.context = context;
        this.skCase = Case.getCurrentCase().getSleuthkitCase();
    }
    
}
