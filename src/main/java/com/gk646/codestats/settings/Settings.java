package com.gk646.codestats.settings;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
public class Settings implements  Configurable{

    String[] excludedFileTypes = new String[]{"class", "svn-base", "svn-work", "Extra", "gif", "png", "jpg", "jpeg", "bmp", "tga", "tiff", "ear", "war", "zip", "jar", "iml", "iws", "ipr", "bz2", "gz", "pyc"};

    String[] separateTabs = new String[]{};

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Code Stats";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        // Create the settings page UI. This could be a JPanel containing various controls.
        JPanel panel = new JPanel();
        // Add controls to the panel...
        return panel;
    }

    @Override
    public boolean isModified() {
        // Return true if the settings have been modified.
        return false;
    }

    @Override
    public void apply() {
        // Save the settings.
    }

    @Override
    public void reset() {
        // Reset the settings to their default values.
    }
}
