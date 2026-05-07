package com.mycompany.dss.gui;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField userField = new JTextField(20);
    private JPasswordField passField = new JPasswordField(20);
    private boolean ok = false;
    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        setLayout(new BorderLayout());
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.gridx=0; c.gridy=0; p.add(new JLabel("Username:"), c);
        c.gridx=1; p.add(userField, c);
        c.gridx=0; c.gridy=1; p.add(new JLabel("Password:"), c);
        c.gridx=1; p.add(passField, c);
        JButton login = new JButton("Login");
        JButton cancel = new JButton("Cancel");
        JPanel btns = new JPanel(new FlowLayout());
        btns.add(login); btns.add(cancel);
        login.addActionListener(e -> check());
        cancel.addActionListener(e -> { ok=false; dispose(); });
        add(p, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        pack(); setLocationRelativeTo(parent); setResizable(false);
    }
    private void check() {
        if (userField.getText().equals("admin") && new String(passField.getPassword()).equals("1234")) {
            ok = true; dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid", "Login Failed", JOptionPane.ERROR_MESSAGE);
            userField.setText(""); passField.setText("");
        }
    }
    public boolean isSucceeded() { return ok; }
}