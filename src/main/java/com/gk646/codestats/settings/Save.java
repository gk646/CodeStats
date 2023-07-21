package com.gk646.codestats.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(
        name = "com.gk646.codestats.settings.Save",
        storages = {@Storage("CodeStatsSettings.xml")}
)
public class Save implements PersistentStateComponent<Save> {
    public String excludedFileTypes = "ttf;sql;tmp;dmp;ico;dat;svg;class;svn-base;svn-work;Extra;gif;png;jpg;jpeg;bmp;tga;tiff;ear;war;zip;jar;iml;iws;ipr;bz2;gz;pyc";
    public String includedFileTypes = "";
    public String separateTabsTypes = "java;cpp;c;hpp;h;rs;css;html;js;txt;php;py";
    public boolean exclude_idea = true;

    public boolean exclude_npm = true;
    public boolean exclude_compiler = true;
    public boolean exclude_git = true;
    @XCollection(propertyElementName = "excludedDirs", elementTypes = String.class)

    public List<String> excludedDirectories = new ArrayList<>();


    public static Save getInstance() {
        return ApplicationManager.getApplication().getService(Save.class);
    }

    @Nullable
    @Override
    public Save getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Save state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
