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
 * Represents the data contained in an AlpineQuest .trk file.
 */
class TrackFile {
    private static final Logger logger = Logger.getLogger(TrackFile.class.getName());
    private final AbstractFile abstractFile;
    private final double totalLength;
    private final double totalLengthWithElevation;
    private final double totalElevationGain;
    private final long totalTime;
    private final AlpineQuestStructures.Metadata metadata;
    private final List<AlpineQuestStructures.Waypoint> waypoints;
    private final List<AlpineQuestStructures.Segment> segments;
        
    private TrackFile(AbstractFile abstractFile,
            double totalLength, double totalLengthWithElevation, double totalElevationGain, long totalTime,
            AlpineQuestStructures.Metadata metadata, List<AlpineQuestStructures.Waypoint> waypoints, List<AlpineQuestStructures.Segment> segments) {
        this.abstractFile = abstractFile;
        this.totalLength = totalLength;
        this.totalLengthWithElevation = totalLengthWithElevation;
        this.totalElevationGain = totalElevationGain;
        this.totalTime = totalTime;
        this.metadata = metadata;
        this.waypoints = waypoints;
        this.segments = segments;
    }
    
    /**
     * Parse a file to create a TrackFile object.
     * 
     * @param file The file to parse.
     * 
     * @return The parsed file as a TrackFile object.
     * 
     * @throws TskCoreException 
     */
    static TrackFile createFromAbstractFile(AbstractFile file) throws TskCoreException {
        
        AlpineQuestFileReader fileReader = new AlpineQuestFileReader(file);
        fileReader.readInt();   // Version
        fileReader.readInt();   // Header size
        fileReader.readInt();   // Number of locations
        fileReader.readInt();   // Number of segments
        fileReader.readInt();   // Number of waypoints
        fileReader.readCoordinate();  // First longitude
        fileReader.readCoordinate();  // First latitude
        fileReader.readTimestamp();   // First timestamp
        double trackLen = fileReader.readDouble();
        double trackLenWithElevation = fileReader.readDouble();
        double elevationGain = fileReader.readDouble();
        long totalTime = fileReader.readLong();
        Metadata metadata = fileReader.readMetadata();
        List<Waypoint> waypoints = fileReader.readWaypoints();
        List<Segment> segments = fileReader.readSegments();

        return new TrackFile(file,
            trackLen, trackLenWithElevation, elevationGain, totalTime,
            metadata, waypoints, segments);
    }

    /**
     * Create an artifact from the track file.
     * 
     * @throws TskCoreException 
     */
    public void createArtifacts() throws TskCoreException {
        // Create the track artifacts
        ArtifactCreationUtils.createTrackArtifactsFromSegments(segments, metadata.getName(), abstractFile);
        
        // Also add waypoints that were added while recording the track. It does not appear that these waypoints
        // are saved elsewhere.
        ArtifactCreationUtils.createBookmarkArtifactsFromWaypoints(waypoints, abstractFile);
    }
}
