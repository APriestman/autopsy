/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014 Basis Technology Corp.
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
package org.sleuthkit.autopsy.ingest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.examples.SampleExecutableIngestModuleFactory;
import org.sleuthkit.autopsy.examples.SampleIngestModuleFactory;
import org.sleuthkit.autopsy.modules.android.AndroidModuleFactory;
import org.sleuthkit.autopsy.modules.e01verify.E01VerifierModuleFactory;
import org.sleuthkit.autopsy.modules.exif.ExifParserModuleFactory;
import org.sleuthkit.autopsy.modules.fileextmismatch.FileExtMismatchDetectorModuleFactory;
import org.sleuthkit.autopsy.modules.filetypeid.FileTypeIdModuleFactory;
import org.sleuthkit.autopsy.modules.hashdatabase.HashLookupModuleFactory;
import org.sleuthkit.autopsy.modules.sevenzip.ArchiveFileExtractorModuleFactory;
import org.sleuthkit.autopsy.python.JythonModuleLoader;

/**
 * Discovers ingest module factories implemented in Java or Jython.
 */
final class IngestModuleFactoryLoader {

    private static final Logger logger = Logger.getLogger(IngestModuleFactoryLoader.class.getName());
    private static final ArrayList<String> coreModuleOrdering = new ArrayList<String>() {
        {
            // The ordering of Core module factories is hard-coded. 
            add("org.sleuthkit.autopsy.recentactivity.RecentActivityExtracterModuleFactory"); //NON-NLS
            add(HashLookupModuleFactory.class.getCanonicalName());
            add(FileTypeIdModuleFactory.class.getCanonicalName());
            add(ArchiveFileExtractorModuleFactory.class.getCanonicalName());
            add(ExifParserModuleFactory.class.getCanonicalName());
            add("org.sleuthkit.autopsy.keywordsearch.KeywordSearchModuleFactory"); //NON-NLS
            add("org.sleuthkit.autopsy.thunderbirdparser.EmailParserModuleFactory"); //NON-NLS
            add(FileExtMismatchDetectorModuleFactory.class.getCanonicalName());
            add(E01VerifierModuleFactory.class.getCanonicalName());
            add(AndroidModuleFactory.class.getCanonicalName());
        }
    };

    /**
     * Get the currently available set of ingest module factories. The factories
     * are not cached between calls since NetBeans modules with classes labeled
     * as IngestModuleFactory service providers and/or Python scripts defining
     * classes derived from IngestModuleFactory may be added or removed between
     * invocations.
     *
     * @return A list of objects that implement the IngestModuleFactory
     * interface.
     */
    static List<IngestModuleFactory> getIngestModuleFactories() {
        // Discover the ingest module factories implemented using Java, making a
        // hash map of display names to discovered factories and a hash set of
        // display names.
        HashSet<String> moduleDisplayNames = new HashSet<>();
        HashMap<String, IngestModuleFactory> javaFactoriesByClass = new HashMap<>();
        for (IngestModuleFactory factory : Lookup.getDefault().lookupAll(IngestModuleFactory.class)) {
            if (!moduleDisplayNames.contains(factory.getModuleDisplayName())) {
                moduleDisplayNames.add(factory.getModuleDisplayName());
                javaFactoriesByClass.put(factory.getClass().getCanonicalName(), factory);
                logger.log(Level.INFO, "Found ingest module factory: name = {0}, version = {1}", new Object[]{factory.getModuleDisplayName(), factory.getModuleVersionNumber()}); //NON-NLS
            } else {
                logger.log(Level.SEVERE, "Found duplicate ingest module display name (name = {0})", factory.getModuleDisplayName()); //NON-NLS
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(IngestModuleFactoryLoader.class, "IngestModuleFactoryLoader.errorMessages.duplicateDisplayName", factory.getModuleDisplayName()),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }

        // Kick out the sample ingest module factories implemented in Java.
        javaFactoriesByClass.remove(SampleIngestModuleFactory.class.getCanonicalName());
        javaFactoriesByClass.remove(SampleExecutableIngestModuleFactory.class.getCanonicalName());

        // Add the core ingest module factories in the desired order, removing
        // the core factories from the map so that the map will only contain 
        // non-core modules after this loop.
        List<IngestModuleFactory> factories = new ArrayList<>();
        for (String className : coreModuleOrdering) {
            IngestModuleFactory coreFactory = javaFactoriesByClass.remove(className);
            if (coreFactory != null) {
                factories.add(coreFactory);
            } else {
                logger.log(Level.SEVERE, "Core factory {0} not loaded", coreFactory);
            }
        }

        // Add any remaining non-core factories discovered. Order is not 
        // guaranteed!
        factories.addAll(javaFactoriesByClass.values());

        // Add any ingest module factories implemented using Jython. Order is 
        // not guaranteed! 
        for (IngestModuleFactory factory : JythonModuleLoader.getIngestModuleFactories()) {
            if (!moduleDisplayNames.contains(factory.getModuleDisplayName())) {
                moduleDisplayNames.add(factory.getModuleDisplayName());
                factories.add(factory);
                logger.log(Level.INFO, "Found ingest module factory: name = {0}, version = {1}", new Object[]{factory.getModuleDisplayName(), factory.getModuleVersionNumber()}); //NON-NLS
            } else {
                logger.log(Level.SEVERE, "Found duplicate ingest module display name (name = {0})", factory.getModuleDisplayName()); //NON-NLS
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(IngestModuleFactoryLoader.class, "IngestModuleFactoryLoader.errorMessages.duplicateDisplayName", factory.getModuleDisplayName()),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }

        return factories;
    }
}
