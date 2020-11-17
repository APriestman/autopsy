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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.sleuthkit.autopsy.modules.alpinequest.AlpineQuestStructures.*;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.ReadContentInputStream;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Class to read AlpineQuest structures and primitives.
 */
class AlpineQuestFileReader {
    
    private final ReadContentInputStream stream;
    
    /**
     * Create the reader from an abstract file.
     * 
     * @param file The AlpineQuest file to parse.
     * 
     * @throws TskCoreException 
     */
    AlpineQuestFileReader(AbstractFile file) throws TskCoreException {
        stream = new ReadContentInputStream(file);
    }
    
    /**
     * Read and parse a list of waypoints.
     * 
     * @return The list of parsed waypoints.
     * 
     * @throws TskCoreException 
     */
    List<Waypoint> readWaypoints() throws TskCoreException {
        int numWaypoints = readInt();
        List<Waypoint> waypoints = new ArrayList<>();
        for (int i = 0;i < numWaypoints;i++) {
            waypoints.add(readWaypoint());
        }
        return waypoints;
    }
    
    /**
     * Read and parse a waypoint.
     * 
     * @return The parsed waypoint.
     * 
     * @throws TskCoreException 
     */
    Waypoint readWaypoint() throws TskCoreException {
        Metadata metadata = readMetadata();
        Location location = readLocation();
        
        return new Waypoint(metadata, location);
    }
    
    /**
     * Read and parse a Metadata object.
     * 
     * @return The parsed Metadata object.
     * 
     * @throws TskCoreException 
     */
    Metadata readMetadata() throws TskCoreException {
        MetadataContent content = readMetadataContent();
        List<MetadataContentExt> metadataContentExtList = readMetadataContentExtList();
        
        return new Metadata(content, metadataContentExtList);
    }
    
    /**
     * Read and parse extended metadata content.
     * 
     * @return The list of parsed extended metadata content objects.
     * 
     * @throws TskCoreException 
     */
    List<MetadataContentExt> readMetadataContentExtList() throws TskCoreException {
        int extContentCount = readInt();
        List<MetadataContentExt> metadataContentExtList = new ArrayList<>();
        for (int i = 0;i < extContentCount;i++) {
            String name = readString();
            MetadataContent metadataContent = readMetadataContent();
            metadataContentExtList.add(new MetadataContentExt(name, metadataContent));
        }
        return metadataContentExtList;
    }
    
    /**
     * Read and parse metadata content and entries.
     * 
     * @return The parsed MetadataContent.
     * 
     * @throws TskCoreException 
     */
    MetadataContent readMetadataContent() throws TskCoreException {
        int numEntries = readInt();
        List<MetadataContentEntry> entries = new ArrayList<>();
        for (int i = 0;i < numEntries;i++) {
            String name = readString();
            
            // This is the type of data if less than zero, or indicates the length of string-type data
            int typeOrStringLen = readInt();
            if (typeOrStringLen == MetadataContentEntryType.BOOLEAN.getValue()) {
                entries.add(new MetadataContentEntry(name, MetadataContentEntryType.BOOLEAN, readBoolean()));
            } else if (typeOrStringLen == MetadataContentEntryType.LONG.getValue()) {
                entries.add(new MetadataContentEntry(name, MetadataContentEntryType.LONG, readLong()));
            } else if (typeOrStringLen == MetadataContentEntryType.DOUBLE.getValue()) {
                entries.add(new MetadataContentEntry(name, MetadataContentEntryType.DOUBLE, readDouble()));
            } else if (typeOrStringLen == MetadataContentEntryType.RAW.getValue()) {
                int rawDataLen = readInt();
                entries.add(new MetadataContentEntry(name, MetadataContentEntryType.RAW, readRaw(rawDataLen)));
            } else {
                entries.add(new MetadataContentEntry(name, MetadataContentEntryType.STRING, readString(typeOrStringLen)));
            }
        }
        return new MetadataContent(entries);
    }    
    
    /**
     * Read and parse a list of locations.
     * 
     * @return The list of locations.
     * 
     * @throws TskCoreException 
     */
    List<Location> readLocations() throws TskCoreException {
        int numLocations = readInt();
        List<Location> locations = new ArrayList<>();
        for (int i = 0;i < numLocations;i++) {
            locations.add(readLocation());
        }
        return locations;
    }
    
    /**
     * Read and parse a location.
     * 
     * @return The parsed location.
     * 
     * @throws TskCoreException 
     */
    Location readLocation() throws TskCoreException {
        int entryLen = readInt();
        double longitude = readCoordinate();
        double latitude = readCoordinate();
        double height = readHeight();
        Long timestamp = readTimestamp();
        
        // See if there are optional entries for accuracy and pressure. 
        // We don't store these so skip over the bytes.
        // Header length to this point is 20 bytes (long = 4, lat = 4, height = 4, timestamp = 8)
        int bytesLeft = entryLen - 20;
        if (bytesLeft > 0) {
            skipBytes(bytesLeft);
        }
        
        return new Location(longitude, latitude, height, timestamp);
    }
    
    /**
     * Read and parse a list of segments.
     * 
     * @return The list of segments.
     * 
     * @throws TskCoreException 
     */
    List<Segment> readSegments() throws TskCoreException {
        int numSegments = readInt();
        List<Segment> segments = new ArrayList<>();
        for (int i = 0;i < numSegments;i++) {
            segments.add(readSegment());
        }
        return segments;
    }
    
    /**
     * Read a parse a segment.
     * 
     * @return The parsed segment.
     * 
     * @throws TskCoreException 
     */
    Segment readSegment() throws TskCoreException {
        Metadata metadata = readMetadata();
        int numLocations = readInt();
        List<Location> locations = new ArrayList<>();
        for (int i = 0;i < numLocations;i++) {
            locations.add(readLocation());
        }
        return new Segment(metadata, locations);
    }
    
    /**
     * Read from the stream into a byte buffer.
     * 
     * @return The byte buffer.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    byte[] readRaw(int bytesToRead) throws TskCoreException {
        byte[] bytes = new byte[bytesToRead];
        try {
            int read = stream.read(bytes);
            if (read != bytesToRead) {
                throw new TskCoreException("Ran out of data while reading file");
            }
        } catch (ReadContentInputStream.ReadContentInputStreamException ex) {
            throw new TskCoreException("Error reading " + bytesToRead + " bytes from stream");
        }
        
        return bytes;
    }
    
    /**
     * Read four bytes from the stream as an integer.
     * 
     * @return The four bytes as an int.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    int readInt() throws TskCoreException {
        return ByteBuffer.wrap(readRaw(4)).getInt();
    }
    
    /**
     * Read eight bytes from the stream as a long.
     * 
     * @return The eight bytes as a long.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    long readLong() throws TskCoreException {
        return ByteBuffer.wrap(readRaw(8)).getLong();
    }
    
    /**
     * Read eight bytes from the stream as a double.
     * 
     * @return The eight bytes as a double.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    double readDouble() throws TskCoreException {
        return ByteBuffer.wrap(readRaw(8)).getDouble();
    }    
    
    /**
     * Read 1 byte from the stream as a boolean.
     * 
     * @return The byte as a boolean.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    boolean readBoolean() throws TskCoreException {
        byte[] bytes = readRaw(1);
        return (bytes[0] != 0);
    }
    
     /**
     * Read a string from the stream including the length.
     * First four bytes give length. String is UTF8 encoded.
     * 
     * @return The string.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    String readString() throws TskCoreException {
        return readString(ByteBuffer.wrap(readRaw(4)).getInt());
    }
    
     /**
     * Read a string from the stream with the given length.
     * First four bytes give length. String is UTF8 encoded.
     * 
     * @return The string
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    String readString(int length) throws TskCoreException {
        if (length <= 0) {
            return "";
        }
        
        byte[] stringBytes = readRaw(length);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }    
    
    /**
     * Read a coordinate from the file.
     * Coordinates are stored as four-byte integers and must be divided by 10^7 to
     * get the valude in degrees.
     * 
     * @return The coordinate in degrees.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    double readCoordinate() throws TskCoreException {
        int intCoordVal = ByteBuffer.wrap(readRaw(4)).getInt();
        return (double)intCoordVal / 10000000;
    }
    
    /**
     * Read a height from the file.
     * Heights are stored as four-byte integers and must be divided by 10^3 to
     * get the value in meters.
     * 
     * @return The height in meters.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    double readHeight() throws TskCoreException {
        int intHeightVal = ByteBuffer.wrap(readRaw(4)).getInt();
        if (intHeightVal == -999999999) {
            // Flag for no value
            return 0;
        }
        return (double)intHeightVal / 1000;
    }
    
    /**
     * Read a timestamp from the file.
     * Timestamps are stored in milliseconds and will be converted to seconds.
     * 
     * @return The timestamp or null if the timestamp was all zero.
     * 
     * @throws TskCoreException If an error occurred while reading or ran out of data.
     */
    Long readTimestamp() throws TskCoreException {
        long timestampInMillis = readLong();
        if (timestampInMillis == 0) {
            return null;
        }
        return timestampInMillis / 1000;
    }       
    
    /**
     * Skip over some bytes in the file.
     * 
     * @throws TskCoreException 
     */
    void skipBytes(int bytesToSkip) throws TskCoreException {
        try {
            stream.skip(bytesToSkip);
        } catch (IOException ex) {
            throw new TskCoreException("Error skipping " + bytesToSkip + " bytes");
        }
    }
}
