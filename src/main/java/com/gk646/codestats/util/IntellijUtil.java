/*
 * MIT License
 *
 * Copyright (c) 2024 gk646
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

package com.gk646.codestats.util;

import com.gk646.codestats.CodeStatsWindow;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collection;

public class IntellijUtil {
    /**
     * Displays a popup to select a file from multiple matching files.
     *
     * @param files     The collection of VirtualFile objects.
     * @param project   The current IntelliJ Project.
     * @param component The component in relation to which the popup is shown.
     */
    public static void showFileSelectionPopup(Collection<VirtualFile> files, Project project, JComponent component) {
        BaseListPopupStep<VirtualFile> step = new BaseListPopupStep<>("Multiple matching files! Select one:", new ArrayList<>(files)) {
            @Override
            public @Nullable PopupStep<?> onChosen(VirtualFile selectedValue, boolean finalChoice) {
                openFileInEditor(selectedValue, project);
                return FINAL_CHOICE;
            }

            @Override
            public @NotNull String getTextFor(VirtualFile value) {
                return value.getPath(); // Display the full path in the popup
            }
        };

        ListPopup popup = JBPopupFactory.getInstance().createListPopup(step);
        popup.showCenteredInCurrentWindow(project);
    }

    /**
     * Opens a file in the editor.
     *
     * @param file    The VirtualFile to open.
     * @param project The current IntelliJ Project.
     */
    private static void openFileInEditor(VirtualFile file, Project project) {
        FileEditorManager.getInstance(project).openFile(file, true);
    }

    /**
     * Handles the opening of a file. If multiple files match the filename, it prompts the user to select.
     *
     * @param fileName  The name of the file to open.
     * @param component The UI component from where the file open is initiated.
     */
    public static void openFileInEditor(String fileName, JComponent component) {
        Project project = CodeStatsWindow.project;
        Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.projectScope(project));

        if (!files.isEmpty()) {
            if (files.size() > 1) {
                showFileSelectionPopup(files, project, component);
            } else {
                VirtualFile fileToOpen = files.iterator().next();
                if (fileToOpen != null) {
                    FileEditorManager.getInstance(project).openFile(fileToOpen, true);
                }
            }
        } else {
            JOptionPane.showMessageDialog(component, "File not found: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
