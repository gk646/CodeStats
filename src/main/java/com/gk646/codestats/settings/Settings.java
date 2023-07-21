package com.gk646.codestats.settings;

import com.gk646.codestats.CodeStatsWindow;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;

public class Settings implements Configurable {
    private JBTextField excludedFileTypesField;
    private JBTextField includedFileTypesField;
    private JBTextField separateTabsField;
    private JBCheckBox exclude_idea;
    private JBCheckBox exclude_npm;
    private JBCheckBox exclude_compiler;
    private JBCheckBox exclude_git;

    private DefaultListModel<String> excludedDirectoriesField;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Code Stats";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.weightx = 0;
        constraints.weighty = 0;

        //excluded file types
        constraints.gridy = 0;
        constraints.gridx = 0;
        panel.add(new JLabel("Excluded File Types:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        excludedFileTypesField = new JBTextField(20);
        panel.add(excludedFileTypesField, constraints);

        constraints.weightx = 0;
        constraints.gridx = 2;
        panel.add(new JLabel("(Example: txt;mp3"), constraints);

        //included files types
        constraints.gridy = 1;
        constraints.gridx = 0;
        panel.add(new JLabel("Included File Types:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        includedFileTypesField = new JBTextField(20);
        panel.add(includedFileTypesField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        panel.add(new JLabel("(Example: txt;md)"), constraints);

        constraints.gridy = 2;
        constraints.gridx = 0;
        panel.add(new JLabel("Separate Tabs Types:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        separateTabsField = new JBTextField(20);
        panel.add(separateTabsField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        panel.add(new JLabel("(Example: html;css)"), constraints);


        excludedDirectoriesField = new DefaultListModel<>();

        JList<String> list = new JBList<>(excludedDirectoriesField);

        JScrollPane scrollPane = new JBScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(150, 120));

        JButton addButton = new JButton("Add...");
        addButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            FileChooser.chooseFile(descriptor, null, null, file -> {
                excludedDirectoriesField.addElement(file.getPath());
            });
        });


        JButton removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> {
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex != -1) {
                excludedDirectoriesField.remove(selectedIndex);
                list.clearSelection();
            }
        });

        list.addListSelectionListener(e -> {
            removeButton.setEnabled(list.getSelectedIndex() != -1);
        });


        // Add the components to the panel
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(new JLabel("Excluded Directories:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(scrollPane, constraints);

        constraints.insets = JBUI.insets(1);
        constraints.gridx = 2;
        constraints.gridy = 3;
        panel.add(addButton, constraints);

        constraints.insets = JBUI.insets(1);
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.gridx = 2;
        constraints.gridy = 4;
        panel.add(removeButton, constraints);


        constraints.gridx = 1;

        //checkboxes
        constraints.gridy++;
        exclude_idea = new JBCheckBox("Exclude IDE configuration directories (.idea)");
        panel.add(exclude_idea, constraints);

        constraints.gridy++;
        exclude_compiler = new JBCheckBox("Exclude compiler output dir (out)");
        panel.add(exclude_compiler, constraints);

        constraints.gridy++;
        exclude_npm = new JBCheckBox("Exclude npm dir (node_modules)");
        panel.add(exclude_npm, constraints);

        constraints.gridy++;
        exclude_git = new JBCheckBox("Exclude Git directory (.git)");
        panel.add(exclude_git, constraints);


        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(panel, BorderLayout.NORTH);

        return wrapperPanel;
    }

    @Override
    public boolean isModified() {
        Save settings = Save.getInstance();
        return !excludedFileTypesField.getText().equals(settings.excludedFileTypes)
                || !includedFileTypesField.getText().equals(settings.includedFileTypes)
                || !separateTabsField.getText().equals(settings.separateTabsTypes)
                || exclude_idea.isSelected() != settings.exclude_idea
                || exclude_npm.isSelected() != settings.exclude_npm
                || exclude_compiler.isSelected() != settings.exclude_compiler
                || exclude_git.isSelected() != settings.exclude_git
                || !Collections.list(excludedDirectoriesField.elements()).equals(settings.excludedDirectories);
    }


    @Override
    public void reset() {
        Save settings = Save.getInstance();
        excludedFileTypesField.setText(settings.excludedFileTypes);
        includedFileTypesField.setText(settings.includedFileTypes);
        separateTabsField.setText(settings.separateTabsTypes);
        exclude_idea.setSelected(settings.exclude_idea);
        exclude_npm.setSelected(settings.exclude_npm);
        exclude_compiler.setSelected(settings.exclude_compiler);
        exclude_git.setSelected(settings.exclude_git);

        excludedDirectoriesField.clear();
        for (String dir : settings.excludedDirectories) {
            excludedDirectoriesField.addElement(dir);
        }
        excludedFileTypesField.setText(settings.excludedFileTypes);
        includedFileTypesField.setText(settings.includedFileTypes);
        separateTabsField.setText(settings.separateTabsTypes);
    }

    @Override
    public void apply() {
        Save settings = Save.getInstance();
        settings.excludedDirectories = Collections.list(excludedDirectoriesField.elements());
        settings.excludedFileTypes = excludedFileTypesField.getText();
        settings.includedFileTypes = includedFileTypesField.getText();
        settings.separateTabsTypes = separateTabsField.getText();
        settings.exclude_idea = exclude_idea.isSelected();
        settings.exclude_npm = exclude_npm.isSelected();
        settings.exclude_compiler = exclude_compiler.isSelected();
        settings.exclude_git = exclude_git.isSelected();

        CodeStatsWindow.parser.updateState();
    }
}
