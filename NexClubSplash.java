import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.formdev.flatlaf.FlatLightLaf;
import java.sql.*;
import java.time.LocalDateTime;

public class NexClubSplash extends JWindow {

    private JLabel[] dots;
    private Timer animationTimer;
    private Timer fadeTimer;
    private int step = 0;
    private float opacity = 1.0f;

    private static Connection connection;

    public NexClubSplash() {
        int w = 500, h = 300;
        setSize(w, h);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        ImageIcon rawIcon = new ImageIcon("1.png");
        Image scaledImg = rawIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        ImageIcon logo = new ImageIcon(scaledImg);

        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());

        JLabel img = new JLabel(logo);
        img.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(img, BorderLayout.CENTER);

        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        dotsPanel.setOpaque(false);
        dotsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        dots = new JLabel[3];

        for (int i = 0; i < 3; i++) {
            dots[i] = new JLabel("●");
            dots[i].setForeground(Color.BLACK);
            dots[i].setFont(new Font("Dialog", Font.BOLD, 8));
            dotsPanel.add(dots[i]);
        }

        panel.add(dotsPanel, BorderLayout.SOUTH);
        setContentPane(panel);

        animationTimer = new Timer(300, e -> {
            for (int i = 0; i < dots.length; i++) {
                dots[i].setFont(new Font("Dialog", i == step % 3 ? Font.BOLD : Font.PLAIN, i == step % 3 ? 9 : 8));
            }
            step++;
        });
    }

    public void showSplash() {
        setOpacity(opacity);
        setVisible(true);
        animationTimer.start();

        new Timer(7000, e -> {
            ((Timer) e.getSource()).stop();
            startFadeOut();
        }).start();
    }

    private void startFadeOut() {
        fadeTimer = new Timer(40, e -> {
            opacity -= 0.10f;
            if (opacity <= 0f) {
                fadeTimer.stop();
                animationTimer.stop();
                setVisible(false);
                dispose();
                showMainApp();
            } else {
                setOpacity(opacity);
            }
        });
        fadeTimer.start();
    }

    private void showMainApp() {
        initializeDatabase();

        JFrame main = new JFrame("NexClub App");
        main.setIconImage(new ImageIcon("l2.png").getImage());
        main.setSize(1024, 576);
        main.setLocationRelativeTo(null);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.getContentPane().setBackground(Color.WHITE);

        LoginRegisterUI.show(main);
        main.setVisible(true);
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection rootConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false", "root", "admin");
            Statement stmt = rootConn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS nexclub");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/nexclub?useSSL=false", "root", "admin");

            Statement s = connection.createStatement();

            // Create login table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS login (" +
                "nID VARCHAR(30) PRIMARY KEY, " +
                "Password VARCHAR(100), " +
                "login_time DATETIME, " +
                "login_count INT)");

            // Create register table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS register (" +
                "nID VARCHAR(30) PRIMARY KEY, " +
                "email VARCHAR(100) UNIQUE, " +
                "Password VARCHAR(100))");

            s.close();
            rootConn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Database initialization failed: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println("FlatLaf failed to load");
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        NexClubSplash splash = new NexClubSplash();
        splash.showSplash();
    }
}
