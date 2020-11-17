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

import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Represents the data contained in an AlpineQuest .wpt file.
 */
class WaypointFile {
    
    private final AbstractFile abstractFile;
    private final AlpineQuestStructures.Waypoint waypoint;

    private WaypointFile(AbstractFile abstractFile, AlpineQuestStructures.Waypoint waypoint) {
        this.abstractFile = abstractFile;
        this.waypoint = waypoint;
    }
    
    /**
     * Parse a file to create a WaypointFile object.
     * 
     * @param file The file to parse.
     * 
     * @return The parsed file as a WaypointFile object.
     * 
     * @throws TskCoreException 
     */
    static WaypointFile createFromAbstractFile(AbstractFile file) throws TskCoreException {
        
        AlpineQuestFileReader fileReader = new AlpineQuestFileReader(file);
        fileReader.readInt(); // Version
            
        // There's no information on what the "header" contains. In sample data the size is zero.
        int headerSize = fileReader.readInt();
        if (headerSize != 0) {
            fileReader.skipBytes(headerSize);
        }
        
        // Read the waypoint
        AlpineQuestStructures.Waypoint waypoint = fileReader.readWaypoint();

        return new WaypointFile(file, waypoint);
    }

    /**
     * Create an artifact from the waypoint file.
     * 
     * @throws TskCoreException 
     */
    public void createArtifacts() throws TskCoreException {
        ArtifactCreationUtils.createBookmarkArtifactFromWaypoint(waypoint, abstractFile);
    }
}
