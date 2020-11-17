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
import org.sleuthkit.autopsy.modules.alpinequest.AlpineQuestStructures.*;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Represents the data contained in an AlpineQuest .rte file.
 */
class RouteFile {
    
    private final AbstractFile abstractFile;
    private final int version;
    private final double firstLongitude;
    private final double firstLatitude;
    private final Long firstTimestamp;
    private final double totalLength;
    private final double totalLengthWithElevation;
    private final double totalElevationGain;
    private final long totalTime;
    private final Metadata metadata;
    private final List<Waypoint> waypoints;
        
    private RouteFile(AbstractFile abstractFile, int version, double firstLongitude, double firstLatitude, Long firstTimestamp,
            double totalLength, double totalLengthWithElevation, double totalElevationGain, long totalTime,
            Metadata metadata, List<Waypoint> waypoints) {
        this.abstractFile = abstractFile;
        this.version = version;
        this.firstLongitude = firstLongitude;
        this.firstLatitude = firstLatitude;
        this.firstTimestamp = firstTimestamp;
        this.totalLength = totalLength;
        this.totalLengthWithElevation = totalLengthWithElevation;
        this.totalElevationGain = totalElevationGain;
        this.totalTime = totalTime;
        this.metadata = metadata;
        this.waypoints = waypoints;
    }
    
    Long getFirstTimestamp() {
        return firstTimestamp;
    }
    
    Metadata getMetadata() {
        return metadata;
    } 
    
    List<Waypoint> getWaypoints() {
        return waypoints;
    }
    
    /**
     * Parse a file to create a RouteFile object.
     * 
     * @param file The file to parse.
     * 
     * @return The parsed file as a RouteFile object.
     * 
     * @throws TskCoreException 
     */
    static RouteFile createFromAbstractFile(AbstractFile file) throws TskCoreException {
        
        AlpineQuestFileReader fileReader = new AlpineQuestFileReader(file);
        int version = fileReader.readInt();

        fileReader.readInt(); // Header size
        fileReader.readInt(); // Number of waypoints
        double firstLong = fileReader.readCoordinate();
        double firstLat = fileReader.readCoordinate();
        Long firstTimestamp = fileReader.readTimestamp();
        double trackLen = fileReader.readDouble();
        double trackLenWithElevation = fileReader.readDouble();
        double elevationGain = fileReader.readDouble();
        long totalTime = fileReader.readLong();
        Metadata metadata = fileReader.readMetadata();
        List<Waypoint> waypoints = fileReader.readWaypoints();

        return new RouteFile(file, version, firstLong, firstLat, firstTimestamp,
            trackLen, trackLenWithElevation, elevationGain, totalTime,
            metadata, waypoints);
    }

    /**
     * Create an artifact from the route file.
     * 
     * @throws TskCoreException 
     */
    public void createArtifacts() throws TskCoreException {
        ArtifactCreationUtils.createRouteArtifactFromRouteFile(this, abstractFile);
    }
}
