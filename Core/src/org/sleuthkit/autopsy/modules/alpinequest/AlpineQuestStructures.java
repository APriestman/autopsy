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

/**
 * Utility class containing the objects that make up AlpineQuest files.
 */
class AlpineQuestStructures {  
    
    /**
     * Class representing an AlpineQuest Waypoint.
     */
    static class Waypoint {
        private Metadata metadata;
        private Location location;
        
        Waypoint(Metadata metadata, Location location) {
            this.metadata = metadata;
            this.location = location;
        }
        
        double getLongitude() {
            return location.getLongitude();
        }
        
        double getLatitude() {
            return location.getLatitude();
        }
        
        double getElevation() {
            return location.getElevation();
        }
        
        Long getTimestamp() {
            return location.getTimestamp();
        }
        
        /**
         * Attempts to find the name of this waypoint in the metadata.
         * 
         * @return The name if found, null otherwise.
         */
        String getName() {
            return metadata.getName();
        }
    }
    
    /**
     * Class representing an AlpineQuest Metadata object.
     */
    static class Metadata {
        private MetadataContent metadataContent;
        private List<MetadataContentExt> metadataContentExtList;
        
        Metadata(MetadataContent metadataContent, List<MetadataContentExt> metadataContentExtList) {
            this.metadataContent = metadataContent;
            this.metadataContentExtList = metadataContentExtList;
        }
        
        /**
         * Attempts to find a name-type entry and returns the value.
         * At present, checks for "name" and "file_desc" fields.
         * 
         * @return A name-type value or null if none were found.
         */
        String getName() {
            String nameValue = metadataContent.getMetadataValueAsString("name");
            if (nameValue != null) {
                return nameValue;
            }
            
            return metadataContent.getMetadataValueAsString("file_desc");
        }
    }
    
    /**
     * Class representing an AlpineQuest Extended Metadata Content object.
     */
    static class MetadataContentExt {
        private final String name;
        private final MetadataContent metadataContent;
        
        MetadataContentExt(String name, MetadataContent metadataContent) {
            this.name = name;
            this.metadataContent = metadataContent;
        }
    }
    
    /**
     * Class representing an AlpineQuest Metadata Content object.
     */    
    static class MetadataContent {
        private final List<MetadataContentEntry> entries;
        
        MetadataContent(List<MetadataContentEntry> entries) {
            this.entries = entries;
        }
        
        /**
         * Look for a given field in the metadata and return the value.
         * 
         * @return The value if found or null if not.
         */
        String getMetadataValueAsString(String fieldName) {
            for (MetadataContentEntry entry : entries) {
                if (entry.getName().equals(fieldName)) {
                    return entry.getValueAsString();
                }
            }
            return null;
        }
    }
    
    /**
     * Enum for the types of data stored in a MetadataContentEntry.
     * Note that in the data, the STRING type is represented by any non-negative value.
     */
    static enum MetadataContentEntryType {
        BOOLEAN(-1),
        LONG(-2),
        DOUBLE(-3),
        RAW(-4),
        STRING(0);
        
        int value;
        
        MetadataContentEntryType(int value) {
            this.value = value;
        }
        
        int getValue() {
            return value;
        }
    }
    
    /**
     * Class representing an AlpineQuest Metadata Content Entry object.
     * These entries are name/value pairs.
     */ 
    static class MetadataContentEntry {
        private final String name;
        private final MetadataContentEntryType type;
        
        // Only one of these should be set for each entry
        private boolean booleanData;
        private long longData;
        private double doubleData;
        private byte[] rawData;
        private String stringData;
        
        MetadataContentEntry(String name, MetadataContentEntryType type, boolean booleanData) {
            this.name = name;
            this.type = type;
            this.booleanData = booleanData;
        }
        
        MetadataContentEntry(String name, MetadataContentEntryType type, long longData) {
            this.name = name;
            this.type = type;
            this.longData = longData;
        }
        
        MetadataContentEntry(String name, MetadataContentEntryType type, double doubleData) {
            this.name = name;
            this.type = type;
            this.doubleData = doubleData;
        }
        
        MetadataContentEntry(String name, MetadataContentEntryType type, byte[] rawData) {
            this.name = name;
            this.type = type;
            this.rawData = rawData;
        }
        
        MetadataContentEntry(String name, MetadataContentEntryType type, String stringData) {
            this.name = name;
            this.type = type;
            this.stringData = stringData;
        }
        
        String getName() {
            return name;
        }
        
        /**
         * Get the value as a string.
         *
         * @return Value as string.
         */
        String getValueAsString() {
            switch(type) {
                case BOOLEAN:
                    return Boolean.toString(booleanData);
                case LONG:
                    return Long.toString(longData);
                case DOUBLE:
                    return Double.toString(doubleData);
                case RAW:
                    char[] hexArray = "0123456789ABCDEF".toCharArray();
                    char[] hexChars = new char[rawData.length * 2];
                    for (int j = 0; j < rawData.length; j++) {
                        int v = rawData[j] & 0xFF;
                        hexChars[j * 2] = hexArray[v >>> 4];
                        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
                    }
                    return new String(hexChars);
                case STRING:
                    return stringData;
                default:
                    return "";
            }
        }
    }
    
    /**
     * Class representing an AlpineQuest Location object.
     */ 
    static class Location {
        private final double longitude;
        private final double latitude;
        private final double elevation;
        private final Long timestamp;
        
        Location(double longitude, double latitude, double elevation, Long timestamp) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.elevation = elevation;
            this.timestamp = timestamp;
        }
        
        double getLongitude() {
            return longitude;
        }
        
        double getLatitude() {
            return latitude;
        }
        
        double getElevation() {
            return elevation;
        }
        
        Long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Class representing an AlpineQuest Segment object.
     * A segment is a piece of a recorded track.
     */ 
    static class Segment {
        private final Metadata metadata;
        private final List<Location> locations;
        
        Segment(Metadata metadata, List<Location> locations) {
            this.metadata = metadata;
            this.locations = locations;
        }
        
        List<Location> getLocations() {
            return locations;
        }
    }

    private AlpineQuestStructures() {  
        // Class should not be instantiated
    }
}
