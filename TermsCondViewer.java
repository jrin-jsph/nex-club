import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TermsCondViewer {

    public static void showTerms(JFrame parent, Runnable onAccept, Runnable onDecline) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            try {
                createUI(parent, onAccept, onDecline);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Failed to load terms: " + e.getMessage());
            }
        });
    }

    private static void createUI(JFrame parent, Runnable onAccept, Runnable onDecline) throws IOException {
        JFrame frame = new JFrame("Terms and Conditions");
        frame.setUndecorated(true);
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(parent);
        frame.setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 20, 20));

        // Make window draggable
        final Point[] dragOffset = new Point[1];
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset[0] = new Point(e.getX(), e.getY());
            }
        });
        frame.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset[0] != null) {
                    Point newLocation = frame.getLocation();
                    newLocation.x += e.getX() - dragOffset[0].x;
                    newLocation.y += e.getY() - dragOffset[0].y;
                    frame.setLocation(newLocation);
                }
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(Color.WHITE);
        frame.setContentPane(root);

        String rawMarkdown = Files.readString(Paths.get("nexclub_terms.md"));
        JTextPane textPane = createMarkdownPane(rawMarkdown);

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.setOpaque(false);

        JButton acceptBtn = createFlatButton("Accept");
        JButton declineBtn = createFlatButton("Decline");
        JToggleButton agreeToggle = createFlatToggle("I agree to the terms");

        acceptBtn.setVisible(false);
        declineBtn.setVisible(false);

        agreeToggle.addItemListener(e -> {
            boolean agreed = agreeToggle.isSelected();
            acceptBtn.setVisible(agreed);
            declineBtn.setVisible(agreed);
        });

        acceptBtn.addActionListener(e -> {
            frame.dispose();
            onAccept.run();
        });

        declineBtn.addActionListener(e -> {
            // Disable button during delay
            declineBtn.setEnabled(false);
            
            // Create 3-second delay timer
            Timer timer = new Timer(3000, ev -> {
                frame.dispose();
                onDecline.run();
            });
            timer.setRepeats(false); // Only fire once
            timer.start();
        });

        buttonPanel.add(agreeToggle);
        buttonPanel.add(acceptBtn);
        buttonPanel.add(declineBtn);
        root.add(buttonPanel, BorderLayout.SOUTH);

        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            boolean shown = false;

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                if (!shown && bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - 10) {
                    agreeToggle.setVisible(true);
                    shown = true;
                }
            }
        });

        agreeToggle.setVisible(false);

        frame.setVisible(true);
    }

    private static JTextPane createMarkdownPane(String markdown) {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setBackground(Color.WHITE);
        pane.setBorder(null);

        StyledDocument doc = pane.getStyledDocument();
        Style defaultStyle = doc.addStyle("default", null);
        StyleConstants.setFontFamily(defaultStyle, "Segoe UI");
        StyleConstants.setFontSize(defaultStyle, 14);

        Style headingStyle = doc.addStyle("heading", null);
        StyleConstants.setFontSize(headingStyle, 18);
        StyleConstants.setBold(headingStyle, true);
        StyleConstants.setSpaceAbove(headingStyle, 10);
        StyleConstants.setSpaceBelow(headingStyle, 6);

        Style bulletStyle = doc.addStyle("bullet", null);
        StyleConstants.setFontSize(bulletStyle, 14);
        StyleConstants.setLeftIndent(bulletStyle, 16);
        StyleConstants.setFirstLineIndent(bulletStyle, -10);

        Style boldStyle = doc.addStyle("bold", null);
        StyleConstants.setBold(boldStyle, true);
        StyleConstants.setFontSize(boldStyle, 14);
        StyleConstants.setFontFamily(boldStyle, "Segoe UI");

        for (String line : markdown.split("\n")) {
            try {
                if (line.startsWith("# ")) {
                    doc.insertString(doc.getLength(), line.substring(2) + "\n", headingStyle);
                } else if (line.startsWith("## ")) {
                    doc.insertString(doc.getLength(), line.substring(3) + "\n", headingStyle);
                } else if (line.startsWith("* ") || line.startsWith("- ")) {
                    doc.insertString(doc.getLength(), "• " + line.substring(2) + "\n", bulletStyle);
                } else {
                    String parsedLine = line;
                    int boldStart, boldEnd;
                    while ((boldStart = parsedLine.indexOf("**")) != -1 &&
                           (boldEnd = parsedLine.indexOf("**", boldStart + 2)) != -1) {
                        doc.insertString(doc.getLength(), parsedLine.substring(0, boldStart), defaultStyle);
                        doc.insertString(doc.getLength(), parsedLine.substring(boldStart + 2, boldEnd), boldStyle);
                        parsedLine = parsedLine.substring(boldEnd + 2);
                    }
                    doc.insertString(doc.getLength(), parsedLine + "\n", defaultStyle);
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        pane.setCaret(new DefaultCaret() {
            public void paint(Graphics g) {}
        });

        return pane;
    }

    private static JButton createFlatButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JToggleButton createFlatToggle(String text) {
        JToggleButton toggle = new JToggleButton(text);
        toggle.setFocusPainted(false);
        toggle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        toggle.setContentAreaFilled(false);
        toggle.setOpaque(false);
        toggle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggle.setForeground(Color.BLACK);
        return toggle;
    }
}