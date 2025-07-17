import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;

public class sProfileUI {
    public static void show(JFrame parent) {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel title = new JLabel("🎓 Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        mainPanel.add(title, gbc);
        gbc.gridwidth = 1;

        String[] labels = {
            "Full Name", "Email ID", "Phone Number", "Date of Birth",
            "Gender", "Address", "Nationality", "Current College Name",
            "University", "Degree Program", "Branch / Department",
            "Year of Study", "Roll Number", "Admission Year",
            "Expected Graduation Year", "CGPA / Percentage", "Achievements / Awards"
        };

        for (String label : labels) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.3;
            mainPanel.add(new JLabel(label + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            mainPanel.add(new JTextField(20), gbc);

            row++;
        }

        // Profile Picture Upload
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JButton uploadPic = new JButton("Upload Profile Picture");
        mainPanel.add(uploadPic, gbc);

        // Replace current frame content
        parent.getContentPane().removeAll();
        parent.getContentPane().add(mainPanel);
        parent.revalidate();
        parent.repaint();
    }

    // Test standalone (optional)
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("Student Profile");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        sProfileUI.show(frame);
        frame.setVisible(true);
    }
}
