/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014-2018 Basis Technology Corp.
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

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.coreutils.Version;
import org.sleuthkit.autopsy.ingest.IngestModuleFactoryAdapter;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

/**
 * TODO TODO
 */
@ServiceProvider(service = IngestModuleFactory.class)
public class AlpineQuestIngestModuleFactory extends IngestModuleFactoryAdapter {

    @NbBundle.Messages({
        "AlpineQuestIngestModuleFactory.moduleName.text=AlpineQuest Parser"
    })
    static String getModuleName() {
        return Bundle.AlpineQuestIngestModuleFactory_moduleName_text();
    }    

    @Override
    public String getModuleDisplayName() {
        return getModuleName();
    }

    // TODO improve this message TODO TODO 
    @NbBundle.Messages({
        "AlpineQuestIngestModuleFactory.moduleDesc.text=Parses tracks, waypoints, other things created by the AlpineQuest app"
    })
    @Override
    public String getModuleDescription() {
        return Bundle.AlpineQuestIngestModuleFactory_moduleDesc_text();
    }

    @Override
    public String getModuleVersionNumber() {
        return Version.getVersion();
    }

    @Override
    public boolean isDataSourceIngestModuleFactory() {
        return true;
    }

    @Override
    public DataSourceIngestModule createDataSourceIngestModule(IngestModuleIngestJobSettings settings) {
        return new AlpineQuestIngestModule();
    }

    @Override
    public boolean hasIngestJobSettingsPanel() {
        return false;
    }
}
