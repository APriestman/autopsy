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
package org.sleuthkit.autopsy.texttranslation.ui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import org.sleuthkit.autopsy.texttranslation.NoServiceProviderException;
import org.sleuthkit.autopsy.texttranslation.TextTranslationService;

public class TranslationOptionsPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private final TranslationOptionsPanelController controller;
    private String currentSelection = "";

    /**
     * Creates new form TranslationOptionsPanel
     */
    public TranslationOptionsPanel(TranslationOptionsPanelController theController) {
        initComponents();
        controller = theController;
        TextTranslationService.getInstance().getTranslators().forEach((translator) -> {
            translatorComboBox.addItem(translator.getName());
        });
        translatorComboBox.setEnabled(translatorComboBox.getItemCount() > 0);
        updatePanel();
    }

    private void updatePanel() {
        translationServicePanel.removeAll();
        String selectedItem = translatorComboBox.getSelectedItem().toString();
        if (translatorComboBox.getSelectedItem() != null && !currentSelection.equals(selectedItem)) {
            try {
                TextTranslationService.getInstance().setSelectedTranslator(translatorComboBox.getSelectedItem().toString());
                Component panel = TextTranslationService.getInstance().getSelectedTranslator().getComponent();
                panel.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                       controller.changed();
                    }
                });
                translationServicePanel.add(panel);
                currentSelection = selectedItem;
            } catch (NoServiceProviderException ex) {
                translationServicePanel.add(new JLabel("No Text Translators available"));
            }
        } else {
            translationServicePanel.add(new JLabel("No Translators selected"));
        }
        revalidate();
        repaint();
    }

    void load() {
        updatePanel();
        controller.changed();
    }

    void store() {

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        translatorComboBox = new javax.swing.JComboBox<>();
        translationServiceLabel = new javax.swing.JLabel();
        translationServicePanel = new javax.swing.JPanel();

        translatorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translatorComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(translationServiceLabel, org.openide.util.NbBundle.getMessage(TranslationOptionsPanel.class, "TranslationOptionsPanel.translationServiceLabel.text")); // NOI18N

        translationServicePanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(translationServiceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(translatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 152, Short.MAX_VALUE))
                    .addComponent(translationServicePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(translatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(translationServiceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(translationServicePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void translatorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_translatorComboBoxActionPerformed
        updatePanel();
    }//GEN-LAST:event_translatorComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel translationServiceLabel;
    private javax.swing.JPanel translationServicePanel;
    private javax.swing.JComboBox<String> translatorComboBox;
    // End of variables declaration//GEN-END:variables

}
