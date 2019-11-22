/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
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
package org.sleuthkit.autopsy.contentviewers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.directorytree.DirectoryTreeTopComponent;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_ASSOCIATED_OBJECT;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Displays additional context for the selected file, such as its source, and
 * usage, if known.
 *
 */
@ServiceProvider(service = DataContentViewer.class, position = 7)
@NbBundle.Messages({
    "ContextViewer.title=Context Viewer",
    "ContextViewer.toolTip=Displays context for selected file."
})
public final class ContextViewer extends javax.swing.JPanel implements DataContentViewer {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ContextViewer.class.getName());

    // defines a list of artifacts that provide context for a file
    private static final List<BlackboardArtifact.ARTIFACT_TYPE> SOURCE_CONTEXT_ARTIFACTS = new ArrayList<>();

    static {
        SOURCE_CONTEXT_ARTIFACTS.add(TSK_ASSOCIATED_OBJECT);
    }

    private BlackboardArtifact sourceContextArtifact;

    /**
     * Creates new form ContextViewer
     */
    public ContextViewer() {

        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSourceGoToResultButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jSourceNameLabel = new javax.swing.JLabel();
        jSourceTextLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jSourceGoToResultButton, org.openide.util.NbBundle.getMessage(ContextViewer.class, "ContextViewer.jSourceGoToResultButton.text")); // NOI18N
        jSourceGoToResultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSourceGoToResultButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ContextViewer.class, "ContextViewer.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jSourceNameLabel, org.openide.util.NbBundle.getMessage(ContextViewer.class, "ContextViewer.jSourceNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jSourceTextLabel, org.openide.util.NbBundle.getMessage(ContextViewer.class, "ContextViewer.jSourceTextLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jSourceNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSourceTextLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)))
                        .addGap(36, 36, 36))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSourceGoToResultButton)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSourceNameLabel)
                    .addComponent(jSourceTextLabel))
                .addGap(18, 18, 18)
                .addComponent(jSourceGoToResultButton)
                .addGap(0, 203, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSourceGoToResultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSourceGoToResultButtonActionPerformed

        final DirectoryTreeTopComponent dtc = DirectoryTreeTopComponent.findInstance();

        // Navigate to the source context artifact.
        if (sourceContextArtifact != null) {
            dtc.viewArtifact(sourceContextArtifact);
        }

    }//GEN-LAST:event_jSourceGoToResultButtonActionPerformed

    @Override
    public void setNode(Node selectedNode) {
        if ((selectedNode == null) || (!isSupported(selectedNode))) {
            resetComponent();
            return;
        }

        AbstractFile file = selectedNode.getLookup().lookup(AbstractFile.class);
        try {
            populateSourceContextData(file);
        } catch (NoCurrentCaseException | TskCoreException ex) {
            logger.log(Level.SEVERE, "Exception displaying context for file {0}", file); //NON-NLS
        }
    }

    @Override
    public String getTitle() {
        return Bundle.ContextViewer_title();
    }

    @Override
    public String getToolTip() {
        return Bundle.ContextViewer_toolTip();
    }

    @Override
    public DataContentViewer createInstance() {
        return new ContextViewer();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void resetComponent() {
        setSourceName("");
        setSourceText("");
    }

    @Override
    public boolean isSupported(Node node) {

        // check if the node has an abstract file and the file has any context defining artifacts.
        if (node.getLookup().lookup(AbstractFile.class) != null) {
            AbstractFile abstractFile = node.getLookup().lookup(AbstractFile.class);
            for (BlackboardArtifact.ARTIFACT_TYPE artifactType : SOURCE_CONTEXT_ARTIFACTS) {
                List<BlackboardArtifact> artifactsList;
                try {
                    artifactsList = abstractFile.getArtifacts(artifactType);
                    if (!artifactsList.isEmpty()) {
                        return true;
                    }
                } catch (TskCoreException ex) {
                    logger.log(Level.SEVERE, "Exception while looking up context artifacts for file {0}", abstractFile); //NON-NLS
                }
            }

        }

        return false;
    }

    @Override
    public int isPreferred(Node node) {
        return 1;
    }

    /**
     * Looks for context providing artifacts for the given file and populates
     * the source context.
     *
     * @param sourceFile File for which to show the context.
     *
     * @throws NoCurrentCaseException
     * @throws TskCoreException
     */
    private void populateSourceContextData(AbstractFile sourceFile) throws NoCurrentCaseException, TskCoreException {

        SleuthkitCase tskCase = Case.getCurrentCaseThrows().getSleuthkitCase();

        // Check for all context artifacts
        boolean foundASource = false;
        for (BlackboardArtifact.ARTIFACT_TYPE artifactType : SOURCE_CONTEXT_ARTIFACTS) {
            List<BlackboardArtifact> artifactsList = tskCase.getBlackboardArtifacts(artifactType, sourceFile.getId());
            if (!artifactsList.isEmpty()) {
                foundASource = true;
            }
            for (BlackboardArtifact contextArtifact : artifactsList) {
                addSourceEntry(contextArtifact);
            }
        }
        if (foundASource == false) {
            setSourceName("Unknown");
            showSourceText(false);
        }
    }

    @NbBundle.Messages({
        "ContextViewer.attachmentSource=Attached to: ",
        "ContextViewer.downloadSource=Downloaded from: "
    })

    /**
     * Adds a source context entry for the selected file based on the given context
     * providing artifact.
     *
     * @param artifact Artifact that may provide context.
     *
     * @throws NoCurrentCaseException
     * @throws TskCoreException
     */
    private void addSourceEntry(BlackboardArtifact artifact) throws TskCoreException {
        if (BlackboardArtifact.ARTIFACT_TYPE.TSK_ASSOCIATED_OBJECT.getTypeID() == artifact.getArtifactTypeID()) {
            BlackboardAttribute associatedArtifactAttribute = artifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ASSOCIATED_ARTIFACT));
            if (associatedArtifactAttribute != null) {
                long artifactId = associatedArtifactAttribute.getValueLong();
                BlackboardArtifact associatedArtifact = artifact.getSleuthkitCase().getBlackboardArtifact(artifactId);

                //save the artifact for "Go to Result" button
                sourceContextArtifact = associatedArtifact;

                setSourceFields(associatedArtifact);
            }
        }
    }

    /**
     * Sets the source label and text fields based on the given associated
     * artifact.
     *
     * @param associatedArtifact - associated artifact
     *
     * @throws TskCoreException
     */
    private void setSourceFields(BlackboardArtifact associatedArtifact) throws TskCoreException {
        if (BlackboardArtifact.ARTIFACT_TYPE.TSK_MESSAGE.getTypeID() == associatedArtifact.getArtifactTypeID()
                || BlackboardArtifact.ARTIFACT_TYPE.TSK_EMAIL_MSG.getTypeID() == associatedArtifact.getArtifactTypeID()) {

            setSourceName(Bundle.ContextViewer_attachmentSource());
            setSourceText(msgArtifactToAbbreiviatedString(associatedArtifact));

        } else if (BlackboardArtifact.ARTIFACT_TYPE.TSK_WEB_DOWNLOAD.getTypeID() == associatedArtifact.getArtifactTypeID()
                || BlackboardArtifact.ARTIFACT_TYPE.TSK_WEB_CACHE.getTypeID() == associatedArtifact.getArtifactTypeID()) {

            setSourceName(Bundle.ContextViewer_downloadSource());
            setSourceText(webDownloadArtifactToString(associatedArtifact));
        }
    }

    /**
     * Sets the source label string.
     *
     * @param nameLabel String value for source label.
     */
    private void setSourceName(String nameLabel) {
        jSourceNameLabel.setText(nameLabel);
    }

    /**
     * Sets the source text string.
     *
     * @param nameLabel String value for source text.
     */
    private void setSourceText(String text) {
        jSourceTextLabel.setText(text);
        showSourceText(true);
    }

    private void showSourceText(boolean isVisible) {
        jSourceTextLabel.setVisible(isVisible);
    }

    /**
     * Returns a display string with download source URL from the given
     * artifact.
     *
     * @param artifact artifact to get download source URL from.
     *
     * @return Display string with download URL and date/time.
     *
     * @throws TskCoreException
     */
    private String webDownloadArtifactToString(BlackboardArtifact artifact) throws TskCoreException {
        StringBuilder sb = new StringBuilder(1024);
        Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributesMap = getAttributesMap(artifact);

        if (BlackboardArtifact.ARTIFACT_TYPE.TSK_WEB_DOWNLOAD.getTypeID() == artifact.getArtifactTypeID()
                || BlackboardArtifact.ARTIFACT_TYPE.TSK_WEB_CACHE.getTypeID() == artifact.getArtifactTypeID()) {
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_URL, attributesMap, "URL");
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_CREATED, attributesMap, "On");
        }
        return sb.toString();
    }

    /**
     * Returns a abbreviated display string for a message artifact.
     *
     * @param artifact artifact to get download source URL from.
     *
     * @return Display string for message artifact.
     *
     * @throws TskCoreException
     */
    private String msgArtifactToAbbreiviatedString(BlackboardArtifact artifact) throws TskCoreException {

        StringBuilder sb = new StringBuilder(1024);
        Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributesMap = getAttributesMap(artifact);

        if (BlackboardArtifact.ARTIFACT_TYPE.TSK_MESSAGE.getTypeID() == artifact.getArtifactTypeID()) {
            sb.append("Message ");
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_FROM, attributesMap, "From");
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PHONE_NUMBER_TO, attributesMap, "To");
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME, attributesMap, "On");
        } else if (BlackboardArtifact.ARTIFACT_TYPE.TSK_EMAIL_MSG.getTypeID() == artifact.getArtifactTypeID()) {
            sb.append("Email ");
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_EMAIL_FROM, attributesMap, "From");
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_EMAIL_TO, attributesMap, "To");
            appendAttributeString(sb, BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_SENT, attributesMap, "On");
        }
        return sb.toString();
    }

    /**
     * Looks up specified attribute in the given map and, if found, appends its
     * value to the given string builder.
     *
     * @param sb String builder to append to.
     * @param attribType Attribute type to look for.
     * @param attributesMap Attributes map.
     * @param prependStr Optional string that is prepended before the attribute
     * value.
     */
    private void appendAttributeString(StringBuilder sb, BlackboardAttribute.ATTRIBUTE_TYPE attribType,
            Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributesMap, String prependStr) {

        BlackboardAttribute attribute = attributesMap.get(attribType);
        if (attribute != null) {
            String attrVal = attribute.getDisplayString();
            if (!StringUtils.isEmpty(attrVal)) {
                if (!StringUtils.isEmpty(prependStr)) {
                    sb.append(prependStr).append(' ');
                }
                sb.append(StringUtils.abbreviate(attrVal, 200)).append(' ');
            }
        }
    }

    /**
     * Gets all attributes for the given artifact, and returns a map of
     * attributes keyed by attribute type.
     *
     * @param artifact Artifact for which to get the attributes.
     *
     * @return Map of attribute type and value.
     *
     * @throws TskCoreException
     */
    private Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> getAttributesMap(BlackboardArtifact artifact) throws TskCoreException {
        Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributeMap = new HashMap<>();

        List<BlackboardAttribute> attributeList = artifact.getAttributes();
        for (BlackboardAttribute attribute : attributeList) {
            BlackboardAttribute.ATTRIBUTE_TYPE type = BlackboardAttribute.ATTRIBUTE_TYPE.fromID(attribute.getAttributeType().getTypeID());
            attributeMap.put(type, attribute);
        }

        return attributeMap;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jSourceGoToResultButton;
    private javax.swing.JLabel jSourceNameLabel;
    private javax.swing.JLabel jSourceTextLabel;
    // End of variables declaration//GEN-END:variables
}
