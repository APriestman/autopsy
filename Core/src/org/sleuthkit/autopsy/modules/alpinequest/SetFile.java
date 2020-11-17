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
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.modules.alpinequest.AlpineQuestStructures.*;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Represents the data contained in an AlpineQuest .set file (a collection of waypoints)
 */
class SetFile {
    private static final Logger logger = Logger.getLogger(SetFile.class.getName());
    private final AbstractFile abstractFile;
    private final int version;
    private final double firstLongitude;
    private final double firstLatitude;
    private final Metadata metadata;
    private final List<Waypoint> waypoints;
        
    private SetFile(AbstractFile abstractFile, int version, double firstLongitude, double firstLatitude, Metadata metadata, List<Waypoint> waypoints) {
        this.abstractFile = abstractFile;
        this.version = version;
        this.firstLongitude = firstLongitude;
        this.firstLatitude = firstLatitude;
        this.metadata = metadata;
        this.waypoints = waypoints;
    }
    
    /**
     * Parse a file to create a SetFile object.
     * 
     * @param file The file to parse.
     * 
     * @return The parsed file as a SetFile object.
     * 
     * @throws TskCoreException 
     */
    static SetFile createFromAbstractFile(AbstractFile file) throws TskCoreException {
        
        AlpineQuestFileReader fileReader = new AlpineQuestFileReader(file);
        int version = fileReader.readInt();
        fileReader.readInt();  // Header size
        fileReader.readInt();  // Number of waypoints
        double firstLong = fileReader.readCoordinate();
        double firstLat = fileReader.readCoordinate();
        Metadata metadata = fileReader.readMetadata();
        List<Waypoint> waypoints = fileReader.readWaypoints();

        return new SetFile(file, version, firstLong, firstLat,
            metadata, waypoints);
    }

    /**
     * Create artifacts from the set file.
     * 
     * @throws TskCoreException 
     */
    public void createArtifacts() throws TskCoreException {
        ArtifactCreationUtils.createBookmarkArtifactsFromWaypoints(waypoints, abstractFile);
    }
}
