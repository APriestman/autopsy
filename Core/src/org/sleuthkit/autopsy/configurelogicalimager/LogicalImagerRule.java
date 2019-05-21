/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.configurelogicalimager;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * The class definition for the Logical Imager Rule.
 */
public class LogicalImagerRule {

    private final boolean shouldAlert;
    private final boolean shouldSave;
    private final String description;
    private Set<String> extensions = new HashSet<>();
    private Set<String> filenames = new HashSet<>();
    private Set<String> paths = new HashSet<>();
    private Set<String> fullPaths = new HashSet<>();
    private int minFileSize = 0;
    private int maxFileSize = 0;
    private int minDays = 0;
    private String minDate;
    private String maxDate;
    
    LogicalImagerRule(boolean shouldAlert, boolean shouldSave, String description,
            Set<String> extensions,
            Set<String> filenames,
            Set<String> paths,
            Set<String> fullPaths,
            int minFileSize,
            int maxFileSize,
            int minDays,
            String minDate,
            String maxDate
    ) {
        this.shouldAlert = shouldAlert;
        this.shouldSave = shouldSave;
        this.description = description;
        this.extensions = extensions;
        this.filenames = filenames;
        this.paths = paths;
        this.fullPaths = fullPaths;
        this.minFileSize = minFileSize;
        this.maxFileSize = maxFileSize;
        this.minDays = minDays;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public boolean isShouldAlert() {
        return shouldAlert;
    }

    public boolean isShouldSave() {
        return shouldSave;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    public Set<String> getFilenames() {
        return filenames;
    }

    public Set<String> getPaths() {
        return paths;
    }

    public Set<String> getFullPaths() {
        return fullPaths;
    }

    public int getMinFileSize() {
        return minFileSize;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public int getMinDays() {
        return minDays;
    }

    public String getMinDate() {
        return minDate;
    }

    public String getMaxDate() {
        return maxDate;
    }

    public boolean validatePath(String path) {
        return !path.contains("\\");
    }

    public static class Builder {
        private boolean shouldAlert;
        private boolean shouldSave;
        private String description;
        private Set<String> extensions = new HashSet<>();        
        private Set<String> filenames = new HashSet<>();        
        private Set<String> paths = new HashSet<>();        
        private Set<String> fullPaths = new HashSet<>();        
        private int minFileSize = 0;
        private int maxFileSize = 0;
        private int minDays = 0;
        private String minDate;
        private String maxDate;

        public Builder() {}
        
        public Builder shouldAlert(boolean shouldAlert) {
            this.shouldAlert = shouldAlert;
            return this;
        }
        
        public Builder shouldSave(boolean shouldSave) {
            this.shouldSave = shouldSave;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder extensions(Set<String> extensions) {
            this.extensions = extensions;
            return this;
        }
        
        public Builder filenames(Set<String> filenames) {
            this.filenames = filenames;
            return this;
        }
        
        public Builder paths(Set<String> paths) {
            this.paths = paths;
            return this;
        }
        
        public Builder fullPaths(Set<String> fullPaths) {
            this.fullPaths = fullPaths;
            return this;
        }
        
        public Builder minFileSize(int minFileSize) {
            this.minFileSize = minFileSize;
            return this;
        }
        
        public Builder maxFileSize(int maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }
        
        public Builder minDays(int minDays) {
            this.minDays = minDays;
            return this;
        }
        
        public Builder minDate(String minDate) {
            this.minDate = minDate;
            return this;
        }
        
        public Builder maxDate(String maxDate) {
            this.maxDate = maxDate;
            return this;
        }
        
        public LogicalImagerRule build() {
            return new LogicalImagerRule(shouldAlert, shouldSave, description, 
                    extensions, filenames, paths, fullPaths,
                    minFileSize, maxFileSize,
                    minDays, minDate, maxDate
            );
        }
    }
}
