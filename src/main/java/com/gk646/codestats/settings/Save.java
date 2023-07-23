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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@State(
        name = "com.gk646.codestats.settings.Save",
        storages = {@Storage("CodeStatsSettings.xml")}
)
public final class Save implements PersistentStateComponent<Save> {
    public String excludedFileTypes = "wav;ttf;sql;tmp;dmp;ico;dat;svg;class;svn-base;svn-work;Extra;gif;png;jpg;jpeg;bmp;tga;tiff;ear;war;zip;jar;iml;iws;ipr;bz2;gz;pyc";
    public String includedFileTypes = "";
    public String separateTabsTypes = "java;cpp;c;hpp;h;rs;css;html;js;txt;php;py;cs;go;rb;swift;ts;kt;sql;pl;lua;groovy;asp;aspx;jsp;json;xml;scss;less;sass;yaml;yml;sh;bat;ps1;md;f;r;m;asm;ada;scala;dart;jsx;julia";
    public boolean exclude_idea = true;
    public boolean exclude_npm = true;
    public boolean exclude_compiler = true;
    public boolean exclude_git = true;
    public boolean disableAutoUpdate = false;
    @XCollection(propertyElementName = "excludedDirs", elementTypes = String.class)

    public List<String> excludedDirectories = new ArrayList<>();

    public static Save getInstance() {
        return ApplicationManager.getApplication().getService(Save.class);
    }

    @Override
    public Save getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Save state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
