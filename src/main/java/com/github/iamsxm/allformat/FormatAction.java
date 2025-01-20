package com.github.iamsxm.allformat;

import com.github.iamsxm.allformat.ui.NewDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

public class FormatAction extends AnAction {
    public FormatAction() {
        super("AllFormat");
    }

    public void actionPerformed(AnActionEvent e) {
        NewDialog dialog = new NewDialog(!JBColor.isBright());
    }
}
