/*
 * MIT License
 *
 * Copyright (c) 2023 gk646
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gk646.codestats.settings;

import com.gk646.codestats.CodeStatsWindow;
import com.gk646.codestats.ui.UIHelper;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;

public final class SettingsPanel implements Configurable {
    private static final JBCheckBox exclude_idea = new JBCheckBox("Exclude IDE configuration directories (.idea)");
    private static final JBTextField excludedFileTypesField = new JBTextField(10);
    private static final JBTextField includedFileTypesField = new JBTextField(10);
    private static final JBTextField separateTabsField = new JBTextField(10);
    private static final JBCheckBox exclude_npm = new JBCheckBox("Exclude compiler output dir (out|target|build|cmake)");
    private static final JBCheckBox exclude_compiler = new JBCheckBox("Exclude npm dir (node_modules)");
    private static final JBCheckBox exclude_git = new JBCheckBox("Exclude VCS directories (.git|.svn|.hg)");
    private static final JBCheckBox disableAutomaticUpdate = new JBCheckBox("Disable automatic update when opening CodeStats");
    public static final JBCheckBox disableTimeLine = new JBCheckBox("Disable TimeLine");
    private static final ComboBox<String> charsetMenu = UIHelper.getCharsetMenu();

    private DefaultListModel<String> excludedDirectoriesField;

    @Contract(pure = true)
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public @NotNull String getDisplayName() {
        return "CodeStats";
    }

    @Override
    public @NotNull JComponent createComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = initConstraints();

        addTextFieldWithLabel(panel, "Excluded File Types:", excludedFileTypesField, "(Example: ogg;mp3)", constraints);
        addTextFieldWithLabel(panel, "White-listed File Types:", includedFileTypesField, "(Example: txt;md)", constraints);
        addTextFieldWithLabel(panel, "Separate Tabs Types:", separateTabsField, "(Example: html;css)", constraints);

        addDirectoryListSection(panel, constraints);
        addCheckBoxes(panel, constraints);
        addCharsetSection(panel, constraints);
        addTimeLineSection(panel, constraints);


        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(panel, BorderLayout.NORTH);
        return wrapperPanel;
    }

    @Override
    public boolean isModified() {
        PersistentSave settings = PersistentSave.getInstance();
        return !excludedFileTypesField.getText().equals(settings.excludedFileTypes)
                || !includedFileTypesField.getText().equals(settings.includedFileTypes)
                || !separateTabsField.getText().equals(settings.separateTabsTypes)
                || exclude_idea.isSelected() != settings.excludeIdea
                || exclude_npm.isSelected() != settings.excludeNpm
                || exclude_compiler.isSelected() != settings.excludeCompiler
                || exclude_git.isSelected() != settings.excludeGit
                || disableAutomaticUpdate.isSelected() != settings.disableAutoUpdate
                || disableTimeLine.isSelected() != settings.disableTimeLine
                || !charsetMenu.getItemAt(charsetMenu.getSelectedIndex()).equals(settings.charSet)
                || !Collections.list(excludedDirectoriesField.elements()).equals(settings.excludedDirectories);
    }

    @Override
    public void reset() {
        PersistentSave settings = PersistentSave.getInstance();
        excludedFileTypesField.setText(settings.excludedFileTypes);
        includedFileTypesField.setText(settings.includedFileTypes);
        separateTabsField.setText(settings.separateTabsTypes);
        exclude_idea.setSelected(settings.excludeIdea);
        exclude_npm.setSelected(settings.excludeNpm);
        exclude_compiler.setSelected(settings.excludeCompiler);
        exclude_git.setSelected(settings.excludeGit);
        disableAutomaticUpdate.setSelected(settings.disableAutoUpdate);
        disableTimeLine.setSelected(settings.disableTimeLine);

        excludedDirectoriesField.clear();
        for (String dir : settings.excludedDirectories) {
            excludedDirectoriesField.addElement(dir);
        }

        charsetMenu.setSelectedItem(settings.charSet);
        excludedFileTypesField.setText(settings.excludedFileTypes);
        includedFileTypesField.setText(settings.includedFileTypes);
        separateTabsField.setText(settings.separateTabsTypes);
    }

    @Override
    public void apply() {
        PersistentSave settings = PersistentSave.getInstance();
        settings.excludedDirectories = Collections.list(excludedDirectoriesField.elements());
        settings.excludedFileTypes = excludedFileTypesField.getText();
        settings.includedFileTypes = includedFileTypesField.getText();
        settings.separateTabsTypes = separateTabsField.getText();
        settings.excludeIdea = exclude_idea.isSelected();
        settings.excludeNpm = exclude_npm.isSelected();
        settings.excludeCompiler = exclude_compiler.isSelected();
        settings.excludeGit = exclude_git.isSelected();
        settings.disableAutoUpdate = disableAutomaticUpdate.isSelected();
        settings.charSet = charsetMenu.getItemAt(charsetMenu.getSelectedIndex());
        settings.disableTimeLine = disableTimeLine.isSelected();
        CodeStatsWindow.PARSER.updateState();
    }

    private @NotNull GridBagConstraints initConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0;
        constraints.weighty = 0;
        return constraints;
    }

    private void addTextFieldWithLabel(@NotNull JPanel panel, String labelText, JBTextField textField, String exampleText, @NotNull GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(new JLabel(labelText), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(textField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        panel.add(new JLabel(exampleText), constraints);
    }

    private void addDirectoryListSection(@NotNull JPanel panel, @NotNull GridBagConstraints constraints) {
        excludedDirectoriesField = new DefaultListModel<>();
        var list = new JBList<>(excludedDirectoriesField);

        JScrollPane scrollPane = new JBScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(50, 120));

        //add button
        JButton addButton = new JButton("Add...");
        addButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            FileChooser.chooseFile(descriptor, CodeStatsWindow.project, null, file -> excludedDirectoriesField.addElement(file.getPath()));
        });

        //remove button
        var removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> {
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex != -1) {
                excludedDirectoriesField.remove(selectedIndex);
                list.clearSelection();
            }
        });

        list.addListSelectionListener(e -> removeButton.setEnabled(list.getSelectedIndex() != -1));

        constraints.gridx = 0;
        constraints.gridy++;
        panel.add(new JLabel("Excluded Directories:"), constraints);

        constraints.gridx = 1;
        panel.add(scrollPane, constraints);

        constraints.insets = JBUI.insets(1);
        constraints.gridx = 2;
        panel.add(addButton, constraints);

        constraints.insets = JBUI.insets(1);
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.gridx = 2;
        constraints.gridy++;
        panel.add(removeButton, constraints);
    }

    private void addCheckBoxes(@NotNull JPanel panel, @NotNull GridBagConstraints constraints) {
        constraints.gridx = 1;
        constraints.gridy++;
        panel.add(disableAutomaticUpdate, constraints);

        constraints.gridy++;
        panel.add(exclude_idea, constraints);

        constraints.gridy++;
        panel.add(exclude_compiler, constraints);

        constraints.gridy++;
        panel.add(exclude_npm, constraints);

        constraints.gridy++;
        panel.add(exclude_git, constraints);
    }

    private void addCharsetSection(@NotNull JPanel panel, @NotNull GridBagConstraints constraints) {
        constraints.gridy += 2;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(new JLabel("Charset:"), constraints);

        constraints.gridx = 1;
        panel.add(charsetMenu, constraints);
    }

    private void addTimeLineSection(@NotNull JPanel panel, @NotNull GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        JLabel timelineLabel = new JLabel("Timeline:");
        panel.add(timelineLabel, constraints);

        JButton clearTimeLine = new JButton("Clear TimeLine!");
        clearTimeLine.setPreferredSize(new Dimension(150, 25));

        clearTimeLine.addActionListener(e -> {
            String userInput = JOptionPane.showInputDialog(panel,
                    "To confirm clearing the timeline, please type 'reset':",
                    "Confirm Reset",
                    JOptionPane.WARNING_MESSAGE);

            if ("reset".equalsIgnoreCase(userInput)) {
                PersistentSave.clearPoints();
                JOptionPane.showMessageDialog(panel, "Timeline cleared!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else if (userInput != null) {
                JOptionPane.showMessageDialog(panel, "Invalid confirmation. Timeline was not cleared.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        constraints.gridy++;
        constraints.gridx = 1;
        panel.add(disableTimeLine, constraints);

        constraints.gridy++;
        panel.add(clearTimeLine, constraints);
    }
}
