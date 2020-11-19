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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.modules.alpinequest.AlpineQuestStructures.*;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.blackboardutils.GeoArtifactsHelper;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoTrackPoints;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoWaypoints;

/**
 * Utility class to create Blackboard artifacts from AlpineQuest data.
 */
class ArtifactCreationUtils {
    
    private static final Logger logger = Logger.getLogger(ArtifactCreationUtils.class.getName());
    
    /**
     * Create an artifact from a single waypoint.
     * 
     * @param waypoint     The waypoint.
     * @param abstractFile The AbstractFile associated with the waypoint.
     * 
     * @throws TskCoreException 
     */
    static void createBookmarkArtifactFromWaypoint(Waypoint waypoint, AbstractFile abstractFile) throws TskCoreException {
        BlackboardArtifact artifact = abstractFile.newArtifact(BlackboardArtifact.ARTIFACT_TYPE.TSK_GPS_BOOKMARK);
        
        List<BlackboardAttribute> attrs = new ArrayList<>();
        String moduleName = AlpineQuestIngestModuleFactory.getModuleName();
        attrs.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE, moduleName, waypoint.getLatitude()));
        attrs.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE, moduleName, waypoint.getLongitude()));
        attrs.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_ALTITUDE, moduleName, waypoint.getElevation()));
        if (waypoint.getTimestamp() != null) {
            attrs.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME, moduleName, waypoint.getTimestamp()));
        }
        if (waypoint.getName() != null) {
            attrs.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME, moduleName, waypoint.getName()));
        }
        attrs.add(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PROG_NAME, moduleName, AlpineQuestIngestModule.getProgramName()));
        artifact.addAttributes(attrs);
        
        try {
            // Post the new waypoint artifacts.
            abstractFile.getSleuthkitCase().getBlackboard().postArtifact(artifact, AlpineQuestIngestModuleFactory.getModuleName());
        } catch (Blackboard.BlackboardException ex) {
            logger.log(Level.WARNING, "Failed to post artifacts", ex);
        }
    }
    
    /**
     * Create artifacts from a list of waypoints.
     * 
     * @param waypoints    The list of waypoints.
     * @param abstractFile The AbstractFile associated with the waypoints.
     * @throws TskCoreException 
     */
    static void createBookmarkArtifactsFromWaypoints(List<Waypoint> waypoints, AbstractFile abstractFile) throws TskCoreException {
        for(Waypoint waypoint : waypoints) {
            createBookmarkArtifactFromWaypoint(waypoint, abstractFile);
        }
    }
    
    /**
     * Create artifacts from a list of segments making up a track.
     * 
     * @param segments     The list of segments.
     * @param name         The name of this track.
     * @param abstractFile The AbstractFile associated with the segments.
     * 
     * @throws TskCoreException 
     */
    @NbBundle.Messages({
        "# {0} - numSegments",
        "AlpineQuestIngestModuleFactory.segmentNumber.text= (Part {0})"
    })
    static void createTrackArtifactsFromSegments(List<Segment> segments, String name, AbstractFile abstractFile) throws TskCoreException {
        List<Location> fullTrack = new ArrayList<>();
        for (Segment segment : segments) {
            fullTrack.addAll(segment.getLocations());
        }
        createTrackArtifactFromLocations(fullTrack, name, abstractFile);
    }
    
    static void createTrackArtifactFromLocations(List<Location> locations, String name, AbstractFile abstractFile) throws TskCoreException {
        // Convert Locations into GeoTrackPoints
        GeoTrackPoints geoTrackPoints = new GeoTrackPoints();
        for (AlpineQuestStructures.Location location : locations) {
            geoTrackPoints.addPoint(new GeoTrackPoints.TrackPoint(location.getLatitude(), location.getLongitude(), location.getElevation(),
                null, null, null, null, location.getTimestamp()));
        }

        // Create the artifact
        if (!geoTrackPoints.isEmpty()) {
            try {
                (new GeoArtifactsHelper(abstractFile.getSleuthkitCase(), AlpineQuestIngestModuleFactory.getModuleName(), 
                    AlpineQuestIngestModule.getProgramName(), abstractFile)).addTrack(name, geoTrackPoints, null);
            }  catch (Blackboard.BlackboardException ex) {
                logger.log(Level.WARNING, "Failed to post artifacts", ex);
            }
        }
    }
    
    /**
     * Create artifact from a route.
     * 
     * @param routeFile    The parsed route file.
     * @param abstractFile The AbstractFile associated with the route.
     * 
     * @throws TskCoreException 
     */
    static void createRouteArtifactFromRouteFile(RouteFile routeFile, AbstractFile abstractFile) throws TskCoreException {
        // Convert the waypoints into GeoWaypoints
        GeoWaypoints geoWaypoints = new GeoWaypoints();
        for (Waypoint waypoint : routeFile.getWaypoints()) {
            geoWaypoints.addPoint(
                new GeoWaypoints.Waypoint(waypoint.getLatitude(), waypoint.getLongitude(), waypoint.getElevation(), waypoint.getName()));
        }
        
        // Create the artifact
        if(!geoWaypoints.isEmpty()) {
            try {
                // If there's no timestamp set for the route, check if any of they waypoints have it set.
                Long timestamp = routeFile.getFirstTimestamp();
                if (timestamp == null) {
                    for (Waypoint waypoint: routeFile.getWaypoints()) {
                        if(waypoint.getTimestamp() != null) {
                            timestamp = waypoint.getTimestamp();
                            break;
                        }
                    }
                }
                
                // Create the route
                (new GeoArtifactsHelper(abstractFile.getSleuthkitCase(), AlpineQuestIngestModuleFactory.getModuleName(), 
                    AlpineQuestIngestModule.getProgramName(), abstractFile)).addRoute(routeFile.getMetadata().getName(), timestamp, 
                    geoWaypoints, null);
            }  catch (Blackboard.BlackboardException ex) {
                logger.log(Level.WARNING, "Failed to post artifacts", ex);
            }
        }
    }
}
