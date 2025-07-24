import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class ResetPasswordUI {

    public static JPanel content;
    private static JLabel statusLabel;
    private static JButton nextButton;
    private static JTextField nIDField;

    public static void show(JFrame parent) {
        parent.setTitle("Reset Password");
        parent.getContentPane().removeAll();
        parent.getContentPane().setBackground(Color.WHITE);
        parent.setLayout(new BorderLayout());

        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Reset Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Are You?");
        roleLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JRadioButton studentBtn = new JRadioButton("Student");
        JRadioButton orgBtn = new JRadioButton("Institution");
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(studentBtn);
        roleGroup.add(orgBtn);
        studentBtn.setSelected(true);

        // Set font for radio buttons
        studentBtn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        orgBtn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        radioPanel.setBackground(Color.WHITE);
        radioPanel.add(roleLabel);
        radioPanel.add(studentBtn);
        radioPanel.add(orgBtn);

        JLabel nIDLabel = new JLabel("Nex ID *");
        nIDLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        nIDLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nIDLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel nIDPanel = new JPanel();
        nIDPanel.setLayout(new BoxLayout(nIDPanel, BoxLayout.X_AXIS));
        nIDPanel.setBackground(Color.WHITE);
        nIDPanel.setMaximumSize(new Dimension(400, 40));

        // Create a fixed prefix label
        JLabel prefixLabel = new JLabel("NX");
        prefixLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        prefixLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));

        nIDField = new JTextField("S");
        nIDField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        nIDField.setHorizontalAlignment(JTextField.LEFT);
        nIDField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        nextButton = new JButton("→");
        styleButton(nextButton);
        nextButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));

        nIDPanel.add(prefixLabel);
        nIDPanel.add(nIDField);
        nIDPanel.add(nextButton);

        JLabel nIDErrorLabel = new JLabel("", SwingConstants.CENTER);
        nIDErrorLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        nIDErrorLabel.setForeground(Color.RED);
        nIDErrorLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        nIDErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel otpLabel = new JLabel("Enter Your OTP", SwingConstants.CENTER);
        otpLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        otpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        otpLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        codePanel.setBackground(Color.WHITE);
        JTextField[] codeFields = new JTextField[6];
        for (int i = 0; i < 6; i++) {
            JTextField codeField = new JTextField();
            codeField.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            codeField.setHorizontalAlignment(JTextField.CENTER);
            codeField.setPreferredSize(new Dimension(42, 42));
            codeField.setDocument(new javax.swing.text.PlainDocument() {
                public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (str == null || getLength() >= 1 || !str.matches("\\d")) return;
                    super.insertString(offs, str, a);
                }
            });
            final int index = i;
            codeField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        if (codeField.getText().isEmpty() && index > 0) codeFields[index - 1].requestFocus();
                    } else {
                        if (!codeField.getText().isEmpty() && index < 5) codeFields[index + 1].requestFocus();
                    }
                }
            });
            codeField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
            codeField.setEnabled(false);
            codeFields[i] = codeField;
            codePanel.add(codeField);
        }

        JLabel passwordLabel = new JLabel("New Password (min 6 characters)", SwingConstants.CENTER);
        passwordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setEnabled(false);
        passwordField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JButton showPasswordButton = new JButton("👁");
        styleButton(showPasswordButton);
        showPasswordButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == '\u2022') {
                passwordField.setEchoChar((char) 0);
                showPasswordButton.setText("👁");
            } else {
                passwordField.setEchoChar('\u2022');
                showPasswordButton.setText("👁");
            }
        });

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setMaximumSize(new Dimension(352, 52));
        passwordPanel.setPreferredSize(new Dimension(352, 52));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordButton, BorderLayout.EAST);

        JButton resetButton = new JButton("Reset password");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        for (JButton button : new JButton[]{resetButton, cancelButton}) {
            styleButton(button);
            button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        }
        
        resetButton.setForeground(new Color(0, 120, 215));
        cancelButton.setForeground(new Color(80, 80, 80));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cancelButton.addActionListener(e -> LoginRegisterUI.show(parent));

        resetButton.addActionListener(e -> {
            StringBuilder otp = new StringBuilder();
            for (JTextField tf : codeFields) {
                otp.append(tf.getText());
            }
            
            String newPassword = new String(passwordField.getPassword());
            String nexID = "NX" + nIDField.getText().trim().toUpperCase();
            
            if (otp.length() != 6) {
                showStatus("Please enter a valid 6-digit OTP", Color.RED);
                return;
            }
            
            if (newPassword.length() < 6) {
                showStatus("Password must be at least 6 characters", Color.RED);
                return;
            }
            
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin");
                PreparedStatement ps = con.prepareStatement("UPDATE register SET Password=? WHERE nID=?");
                ps.setString(1, newPassword);
                ps.setString(2, nexID);
                int updated = ps.executeUpdate();
                con.close();
                
                if (updated > 0) {
                    showStatus("Password updated successfully! Redirecting to login...", new Color(0, 150, 0));
                    
                    // Timer to redirect after 3 seconds
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> LoginRegisterUI.show(parent));
                        }
                    }, 3000);
                } else {
                    showStatus("Failed to update password. Please try again.", Color.RED);
                }
            } catch (Exception ex) {
                showStatus("Error: " + ex.getMessage(), Color.RED);
                ex.printStackTrace();
            }
        });

        nextButton.addActionListener(e -> {
            String inputID = nIDField.getText().trim().toUpperCase();
            if (inputID.length() < 1) {
                nIDErrorLabel.setText("Please enter your ID");
                nIDErrorLabel.setVisible(true);
                for (JTextField tf : codeFields) tf.setEnabled(false);
                passwordField.setEnabled(false);
                return;
            }
            
            String fullID = "NX" + inputID;
            
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub", "root", "admin");
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT nID FROM register WHERE nID = '" + fullID + "'");
                boolean found = rs.next();
                con.close();
                
                if (found) {
                    for (JTextField tf : codeFields) {
                        tf.setEnabled(true);
                        tf.setText("");
                    }
                    passwordField.setEnabled(false);
                    passwordField.setText("");
                    nIDErrorLabel.setText("");
                    nIDErrorLabel.setVisible(false);
                    codeFields[0].requestFocus();
                } else {
                    for (JTextField tf : codeFields) tf.setEnabled(false);
                    passwordField.setEnabled(false);
                    nIDErrorLabel.setText("Invalid Nex ID");
                    nIDErrorLabel.setVisible(true);
                }
            } catch (Exception ex) {
                nIDErrorLabel.setText("Database error");
                nIDErrorLabel.setVisible(true);
                ex.printStackTrace();
            }
        });

        nIDField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                e.setKeyChar(Character.toUpperCase(e.getKeyChar()));
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    nextButton.doClick();
                }
            }
        });

        studentBtn.addActionListener(e -> {
            nIDField.setText("S");
        });

        orgBtn.addActionListener(e -> {
            nIDField.setText("I");
        });

        // Add key listener to last OTP field to enable password field when OTP is complete
        codeFields[5].addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                boolean otpComplete = true;
                for (JTextField tf : codeFields) {
                    if (tf.getText().isEmpty()) {
                        otpComplete = false;
                        break;
                    }
                }
                passwordField.setEnabled(otpComplete);
                if (otpComplete) {
                    passwordField.requestFocus();
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 80, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(resetButton);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(radioPanel);
        content.add(nIDLabel);
        content.add(nIDPanel);
        content.add(nIDErrorLabel);
        content.add(otpLabel);
        content.add(codePanel);
        content.add(Box.createVerticalStrut(10));
        content.add(passwordLabel);
        content.add(passwordPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonPanel);
        content.add(statusLabel);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.add(content);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        parent.getContentPane().removeAll();
        parent.getContentPane().setLayout(new BorderLayout());
        parent.getContentPane().add(scrollPane, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private static void styleButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setBackground(new Color(240, 240, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
            }
        });
    }

    private static void showStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(message);
    }
}