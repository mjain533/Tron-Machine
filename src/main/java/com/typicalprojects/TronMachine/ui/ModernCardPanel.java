package com.typicalprojects.TronMachine.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ModernCardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private int cornerRadius = 12;

    private java.awt.Color backgroundColor =
            new java.awt.Color(40, 45, 55);

    private java.awt.Color borderColor =
            new java.awt.Color(58, 100, 160);

    public ModernCardPanel() {
        setOpaque(false);
        setBorder(new EmptyBorder(12, 12, 12, 12));
    }

    public ModernCardPanel(int padding) {
        setOpaque(false);
        setBorder(new EmptyBorder(padding, padding, padding, padding));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width - 1, height - 1,
                         cornerRadius, cornerRadius);

        // Border
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, width - 1, height - 1,
                         cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }

    /* -------------------------
     * Customization setters
     * ------------------------- */

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setBackgroundColor(java.awt.Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(java.awt.Color color) {
        this.borderColor = color;
        repaint();
    }
}
