import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;

public class ForgotPassword extends JFrame {
    private int randomCode;
    private String user;

    // UI elements
    private JTextField txtEmail = new JTextField(20);
    private JTextField txtVer = new JTextField(20);
    private JTextField txtResetPass = new JTextField(20);
    private JTextField txtVerResetPass = new JTextField(20);
    private JButton btnSendCode = new JButton("Send Code");
    private JButton btnVerify = new JButton("Verify");
    private JButton btnReset = new JButton("Reset Password");

    public ForgotPassword() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Password Recovery");
        setLayout(new GridLayout(5, 2, 10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new JLabel("Email:"));
        add(txtEmail);
        add(new JLabel("Verification Code:"));
        add(txtVer);
        add(new JLabel("New Password:"));
        add(txtResetPass);
        add(new JLabel("Confirm Password:"));
        add(txtVerResetPass);
        add(btnSendCode);
        add(btnVerify);
        add(btnReset);

        btnSendCode.addActionListener(e -> sendCode());
        btnVerify.addActionListener(e -> verifyCode());
        btnReset.addActionListener(e -> resetPassword());

        pack();
        setLocationRelativeTo(null);
    }

    private void sendCode() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Random rand = new Random();
            randomCode = 100000 + rand.nextInt(900000);  // 6-digit code

            String host = "smtp.gmail.com";
            String from = "your.email@gmail.com"; // Replace with your email
            String password = "your_app_password"; // Use app password for Gmail
            String subject = "Password Reset Code";
            String messageText = "Your password reset code is: " + randomCode;

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);
            JOptionPane.showMessageDialog(this, "Verification code sent to your email", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to send email: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void verifyCode() {
        try {
            int enteredCode = Integer.parseInt(txtVer.getText());
            if (enteredCode == randomCode) {
                JOptionPane.showMessageDialog(this, "Verification successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtResetPass.setEditable(true);
                txtVerResetPass.setEditable(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid verification code", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid code", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetPassword() {
        String newPass = txtResetPass.getText();
        String verPass = txtVerResetPass.getText();

        if (!newPass.equals(verPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Here you would typically update the password in your database
        // For example:
        // updatePasswordInDatabase(txtEmail.getText(), newPass);
        
        JOptionPane.showMessageDialog(this, "Password reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ForgotPassword().setVisible(true));
    }
}