import com.formdev.flatlaf.FlatLightLaf;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.awt.*;
import java.util.Properties;
import java.util.Random;

public class OTPVerificationApp {
    private static String generatedOTP;
    private static String recipientEmail;
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    public static void main(String[] args) {
        // Set up FlatLaf for modern UI
        FlatLightLaf.setup();

        // Create main frame
        JFrame frame = new JFrame("OTP Verification System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        // Main panel with card layout for switching between screens
        JPanel mainPanel = new JPanel(new CardLayout());
        
        // Panel 1: Email Input
        JPanel emailPanel = createEmailPanel(mainPanel, frame);
        
        // Panel 2: OTP Verification
        JPanel otpPanel = createOTPPanel(mainPanel, frame);
        
        mainPanel.add(emailPanel, "EMAIL");
        mainPanel.add(otpPanel, "OTP");
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static JPanel createEmailPanel(JPanel mainPanel, JFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Enter Your Email for OTP Verification");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(titleLabel, gbc);

        JLabel emailLabel = new JLabel("Email Address:");
        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(250, 30));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.add(emailLabel);
        inputPanel.add(emailField);
        panel.add(inputPanel, gbc);

        JButton sendButton = new JButton("Send OTP");
        sendButton.setPreferredSize(new Dimension(150, 40));
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(new Color(59, 89, 182));

        sendButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (isValidEmail(email)) {
                recipientEmail = email;
                statusLabel.setText("Sending OTP...");
                
                // Generate and send OTP in a separate thread to avoid UI freeze
                new Thread(() -> {
                    try {
                        generatedOTP = generateOTP();
                        sendOTPEmail(recipientEmail, generatedOTP);
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("OTP sent successfully!");
                            CardLayout cl = (CardLayout) mainPanel.getLayout();
                            cl.show(mainPanel, "OTP");
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Failed to send OTP: " + ex.getMessage());
                            JOptionPane.showMessageDialog(frame, 
                                "Error sending OTP: " + ex.getMessage(), 
                                "Error", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            } else {
                statusLabel.setText("Please enter a valid email address");
            }
        });

        panel.add(sendButton, gbc);
        panel.add(statusLabel, gbc);
        
        return panel;
    }

    private static JPanel createOTPPanel(JPanel mainPanel, JFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Verify OTP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(titleLabel, gbc);

        JLabel instructionLabel = new JLabel("Enter the OTP sent to " + (recipientEmail != null ? recipientEmail : "your email"));
        panel.add(instructionLabel, gbc);

        JLabel otpLabel = new JLabel("OTP:");
        JTextField otpField = new JTextField(10);
        otpField.setPreferredSize(new Dimension(150, 30));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.add(otpLabel);
        inputPanel.add(otpField);
        panel.add(inputPanel, gbc);

        JButton verifyButton = new JButton("Verify OTP");
        verifyButton.setPreferredSize(new Dimension(150, 40));
        verifyButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(150, 40));
        
        JLabel statusLabel = new JLabel("");
        statusLabel.setForeground(new Color(59, 89, 182));

        verifyButton.addActionListener(e -> {
            String enteredOTP = otpField.getText().trim();
            if (enteredOTP.equals(generatedOTP)) {
                statusLabel.setText("OTP verified successfully!");
                JOptionPane.showMessageDialog(frame, 
                    "OTP Verification Successful!\nYou can now proceed.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusLabel.setText("Invalid OTP. Please try again.");
            }
        });

        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) mainPanel.getLayout();
            cl.show(mainPanel, "EMAIL");
            otpField.setText("");
            statusLabel.setText("");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(verifyButton);
        buttonPanel.add(backButton);
        
        panel.add(buttonPanel, gbc);
        panel.add(statusLabel, gbc);
        
        return panel;
    }

    private static String generateOTP() {
        // Generate a 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private static void sendOTPEmail(String toEmail, String otp) throws Exception {
        // Get sender email and password from user input
        String senderEmail = JOptionPane.showInputDialog("Enter your Gmail address:");
        if (senderEmail == null || senderEmail.trim().isEmpty()) {
            throw new Exception("Sender email is required");
        }
        
        String password = JOptionPane.showInputDialog("Enter your Gmail password/app password:");
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Password is required");
        }

        // Setup mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Create session with authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, password);
            }
        });

        try {
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your OTP for Verification");
            message.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 10 minutes.");

            // Send email
            Transport.send(message);
        } catch (MessagingException e) {
            throw new Exception("Failed to send email: " + e.getMessage());
        }
    }

    private static boolean isValidEmail(String email) {
        // Simple email validation regex
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email != null && email.matches(emailRegex);
    }
}