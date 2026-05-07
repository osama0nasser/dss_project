package com.mycompany.dss.gui;

import javax.swing.*;
import java.awt.*;

public class DarkModeManager {
    private static boolean darkMode = false;
    public static void toggle(JFrame frame) { darkMode = !darkMode; applyTheme(frame); }
    private static void applyTheme(JFrame frame) {
        Color bg = darkMode ? new Color(43,43,43) : Color.WHITE;
        Color fg = darkMode ? Color.WHITE : Color.BLACK;
        Color panel = darkMode ? new Color(60,63,65) : new Color(240,240,240);
        frame.getContentPane().setBackground(bg);
        for (Component c : frame.getContentPane().getComponents()) setCompColor(c, bg, fg, panel);
        frame.repaint();
    }
    private static void setCompColor(Component c, Color bg, Color fg, Color panel) {
        if (c instanceof JComponent) { c.setBackground(panel); c.setForeground(fg); }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) setCompColor(child, bg, fg, panel);
        }
    }
}