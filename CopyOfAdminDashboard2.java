import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

/**
 * This class builds the Admin Dashboard UI panel and contains the main entry point for the application.
 */
public class CopyOfAdminDashboard2 {
    private CardLayout cardLayout;
    private JPanel contentCards;
    private JButton[] navButtons;
    private String[] navNames = {"Dashboard", "Manage Clubs", "Manage Events"};
    private Color accentBlue = new Color(40, 104, 255);
    private Color pageBg = Color.WHITE;
    private Color cardBorder = new Color(220, 220, 220);
    private int sidebarWidth = 220;
    private final String PREFERRED_FONT = "Microsoft JhengHei";
    private JPanel indicator;
    private int indicatorTargetY = 0;
    private Timer indicatorTimer;
    private List<Club> clubs = new ArrayList<>();
    private List<EventItem> events = new ArrayList<>();
    private Connection dbConn = null;

    private JLabel statusLabel;
    private Timer statusTimer;
    private JPanel glass;
    private JPanel popupPanel;
    private int currentCardIndex = 0;

    private String collegeName = "Default College";
    private JLabel dashboardTitleLabel;

    private JFrame parentFrame; // Reference to the main application window
    private JPanel dashboardContentPane; // The main panel for this view

    public CopyOfAdminDashboard2(JFrame parentFrame) { 
        this.parentFrame = parentFrame;
        buildContentPane();
    }
    
    // Method to get the fully constructed content pane for this view
    public Container getContentPane() {
        return this.dashboardContentPane;
    }

    private void buildContentPane() {
        dashboardContentPane = new JPanel(new BorderLayout());
        dashboardContentPane.setBackground(pageBg);
        
        JPanel topBar = createTopBar();
        dashboardContentPane.add(topBar, BorderLayout.NORTH);
        
        connectDatabase();
        if (dbConn == null) {
            showStatusMessage("DB connection failed.", true);
        }
        
        prepareTables();
        loadDataFromDB();
        fetchCollegeName(); 

        dashboardTitleLabel.setText("Admin Control Panel - " + collegeName);
        
        JPanel sidebar = createSidebar();
        dashboardContentPane.add(sidebar, BorderLayout.WEST);
        
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(pageBg);
        contentCards.add(createDashboardPanel(), "Dashboard");
        contentCards.add(createManageClubsPanel(), "Manage Clubs");
        contentCards.add(createManageEventsPanel(), "Manage Events");
        dashboardContentPane.add(contentCards, BorderLayout.CENTER);

        createGlassPane();
        
        SwingUtilities.invokeLater(() -> animateIndicatorTo(0));
    }


    private void connectDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/nexclub?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            dbConn = DriverManager.getConnection(url, "root", "admin");
        } catch (Exception e) {
            dbConn = null;
        }
    }

    private void prepareTables() {
        if (dbConn == null) return;
        try (Statement st = dbConn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS clubs (id INT AUTO_INCREMENT PRIMARY KEY, nid VARCHAR(255), abbreviation VARCHAR(10), club_name VARCHAR(255), leaders TEXT, description TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS events (id INT AUTO_INCREMENT PRIMARY KEY, nid VARCHAR(255), abbreviation VARCHAR(10), event_name VARCHAR(255), club_name VARCHAR(255), leaders TEXT, venue VARCHAR(255), date VARCHAR(50), time VARCHAR(50))");
            //st.execute("CREATE TABLE IF NOT EXISTS login (login_time DATETIME, nid VARCHAR(255))");
            st.execute("CREATE TABLE IF NOT EXISTS colleges (abbreviation VARCHAR(10) PRIMARY KEY, full_name VARCHAR(255))");
        } catch (SQLException e) {
            showStatusMessage("Failed to prepare DB tables.", true);
        }
    }

    private String getLatestNid() {
        if (dbConn == null) return null;
        try (Statement st = dbConn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT nid FROM login ORDER BY login_time DESC LIMIT 1");
            if (rs.next()) {
                return rs.getString("nid");
            }
        } catch (SQLException e) {
            showStatusMessage("Could not fetch NID.", true);
        }
        return null;
    }

    private void fetchCollegeName() {
        if (dbConn == null) return;
        String latestNid = getLatestNid();
        if (latestNid != null && latestNid.length() >= 6) {
            try (Statement st = dbConn.createStatement()) {
                String abbreviation = latestNid.substring(3, 6).toUpperCase();
                ResultSet rsCollege = st.executeQuery("SELECT name FROM colleges WHERE abbreviation = '" + abbreviation + "'");
                if (rsCollege.next()) {
                    collegeName = rsCollege.getString("name");
                }
                rsCollege.close();
            } catch (SQLException e) {
                showStatusMessage("Failed to fetch college name.", true);
            }
        }
    }

    private void loadDataFromDB() {
        clubs.clear(); events.clear();
        if (dbConn == null) {
            showStatusMessage("Cannot load data; DB not connected.", true);
            return;
        }
        try (Statement st = dbConn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM clubs ORDER BY id DESC");
            while (rs.next()) {
                clubs.add(new Club(rs.getInt("id"), rs.getString("nid"), rs.getString("abbreviation"), rs.getString("club_name"), rs.getString("description"), split(rs.getString("leaders"))));
            }
            rs = st.executeQuery("SELECT * FROM events ORDER BY id DESC");
            while (rs.next()) {
                events.add(new EventItem(rs.getInt("id"), rs.getString("nid"), rs.getString("abbreviation"), rs.getString("event_name"), rs.getString("club_name"), split(rs.getString("leaders")), rs.getString("venue"), rs.getString("date"), rs.getString("time")));
            }
        } catch (SQLException e) {
            showStatusMessage("Failed to load data.", true);
        }
    }
    
    private void refreshAllPanels() {
        loadDataFromDB();
        String currentCard = getCurrentVisibleCard();
        contentCards.removeAll();
        contentCards.add(createDashboardPanel(), "Dashboard");
        contentCards.add(createManageClubsPanel(), "Manage Clubs");
        contentCards.add(createManageEventsPanel(), "Manage Events");
        cardLayout.show(contentCards, currentCard);
        contentCards.revalidate();
        contentCards.repaint();
    }
    
    private String[] split(String s) {
        if (s == null || s.trim().isEmpty()) return new String[0];
        return s.split(",");
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(new Color(248,249,251));
        side.setPreferredSize(new Dimension(sidebarWidth, 0));
        side.setBorder(new EmptyBorder(18, 12, 18, 12));
        JPanel topContent = new JPanel(null);
        topContent.setOpaque(false);
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBounds(10, 30, sidebarWidth - 20, 300);
        navButtons = new JButton[navNames.length];
        for (int i = 0; i < navNames.length; i++) {
            JButton b = new JButton(navNames[i]);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFocusPainted(false);
            b.setBackground(new Color(248,249,251));
            b.setBorder(new EmptyBorder(10, 12, 10, 12));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            applyAppFont(b, Font.PLAIN, 14);
            int idx = i;
            b.addActionListener(e -> {
                cardLayout.show(contentCards, navNames[idx]);
                animateIndicatorTo(idx);
            });
            b.addMouseListener(new SidebarHoverAdapter(b));
            navPanel.add(b);
            navPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            navButtons[i] = b;
        }
        topContent.add(navPanel);
        indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(233,241,255));
                g2.fillRoundRect(4, 0, 6, 44, 6, 6);
                g2.dispose();
            }
        };
        indicator.setOpaque(false);
        indicator.setSize(14, 44);
        indicator.setLocation(0, 30);
        topContent.add(indicator);
        side.add(topContent, BorderLayout.CENTER);
        
        JButton logout = new JButton("Logout");
        makeButtonTransparent(logout);
        applyAppFont(logout, Font.BOLD, 13);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10, 2, 10, 2));
        logout.addActionListener(e -> performLogout());
        side.add(logout, BorderLayout.SOUTH);

        indicatorTimer = new Timer(8, null);
        indicatorTimer.addActionListener(e -> {
            Point loc = indicator.getLocation();
            int y = loc.y;
            if (y == indicatorTargetY) {
                indicatorTimer.stop();
            } else {
                int dir = indicatorTargetY > y ? 1 : -1;
                int step = Math.max(1, Math.abs(indicatorTargetY - y) / 6);
                y += dir * step;
                indicator.setLocation(indicator.getX(), y);
                topContent.repaint();
            }
        });
        return side;
    }

    private void animateIndicatorTo(int index) {
        if (index < 0 || index >= navButtons.length) return;
        this.currentCardIndex = index;
        int gap = 6;
        int yBase = 30;
        int itemHeight = 44 + gap;
        indicatorTargetY = yBase + index * itemHeight;
        for (int i = 0; i < navNames.length; i++) {
            JButton b = navButtons[i];
            if (i == index) {
                b.setBackground(new Color(233,241,255));
                b.setForeground(accentBlue);
                applyAppFont(b, Font.BOLD, 14);
            } else {
                b.setBackground(new Color(248,249,251));
                b.setForeground(Color.BLACK);
                applyAppFont(b, Font.PLAIN, 14);
            }
        }
        indicatorTimer.start();
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(pageBg);
        top.setBorder(new EmptyBorder(12, 12, 12, 12));
        dashboardTitleLabel = new JLabel("Admin Control Panel");
        applyAppFont(dashboardTitleLabel, Font.BOLD, 16);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(dashboardTitleLabel);
        top.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        statusLabel = new JLabel(" ");
        applyAppFont(statusLabel, Font.BOLD, 13);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusLabel.setVisible(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        right.add(statusLabel, gbc);
        JLabel name = new JLabel("Admin User");
        applyAppFont(name, Font.PLAIN, 13);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        right.add(name, gbc);
        top.add(right, BorderLayout.EAST);
        statusTimer = new Timer(3000, e -> statusLabel.setVisible(false));
        statusTimer.setRepeats(false);
        return top;
    }

    private void showStatusMessage(String message, boolean isError) {
        if (statusTimer != null && statusTimer.isRunning()) statusTimer.stop();
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setBackground(isError ? new Color(255, 220, 220) : new Color(220, 255, 220));
            statusLabel.setForeground(isError ? Color.RED.darker() : Color.GREEN.darker());
            statusLabel.setVisible(true);
            statusTimer.start();
        }
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(pageBg);
        panel.setBorder(new EmptyBorder(16,16,16,16));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel welcome = new JLabel("<html><div style='font-size:20px;'><b>Admin Dashboard</b></div><div style='color:#6b7280;'>Manage clubs, events, and data.</div></html>");
        applyAppFont(welcome, Font.PLAIN, 14);
        header.add(welcome, BorderLayout.WEST);
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(12, 0, 12, 0));
        RoundedPanel totalClubs = createStatCard(String.valueOf(clubs.size()), "Total Clubs", new Color(58,78,237));
        RoundedPanel totalEvents = createStatCard(String.valueOf(events.size()), "Total Events", new Color(118,81,255));
        stats.add(totalClubs); 
        stats.add(totalEvents);
        header.add(stats, BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);
        JPanel recentActivityPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        recentActivityPanel.setOpaque(false);
        recentActivityPanel.setBorder(BorderFactory.createTitledBorder("Recently Added"));
        JPanel recentClubsGrid = new JPanel();
        recentClubsGrid.setLayout(new BoxLayout(recentClubsGrid, BoxLayout.Y_AXIS));
        recentClubsGrid.setOpaque(false);
        for (int i = 0; i < Math.min(4, clubs.size()); i++) {
            recentClubsGrid.add(createClubCardForList(clubs.get(i)));
            if (i < Math.min(4, clubs.size()) - 1) recentClubsGrid.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        if (clubs.isEmpty()) recentClubsGrid.add(new JLabel("No clubs added yet.", SwingConstants.CENTER));
        JScrollPane recentClubsScroll = new JScrollPane(recentClubsGrid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recentClubsScroll.getViewport().setBackground(pageBg);
        recentClubsScroll.setBorder(null);
        recentActivityPanel.add(recentClubsScroll);
        JPanel recentEventsGrid = new JPanel();
        recentEventsGrid.setLayout(new BoxLayout(recentEventsGrid, BoxLayout.Y_AXIS));
        recentEventsGrid.setOpaque(false);
        for (int i = 0; i < Math.min(4, events.size()); i++) {
            recentEventsGrid.add(createEventCardForList(events.get(i)));
            if (i < Math.min(4, events.size()) - 1) recentEventsGrid.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        if (events.isEmpty()) recentEventsGrid.add(new JLabel("No events added yet.", SwingConstants.CENTER));
        JScrollPane recentEventsScroll = new JScrollPane(recentEventsGrid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recentEventsScroll.getViewport().setBackground(pageBg);
        recentEventsScroll.setBorder(null);
        recentActivityPanel.add(recentEventsScroll);
        panel.add(recentActivityPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createManageClubsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(pageBg);
        panel.setBorder(new EmptyBorder(16,16,16,16));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Club"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField leadersField = new JTextField(20);
        JTextArea descArea = new JTextArea(4, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        Dimension fieldSize = new Dimension(280, 35);
        nameField.setPreferredSize(fieldSize);
        leadersField.setPreferredSize(fieldSize);
        descArea.setPreferredSize(new Dimension(fieldSize.width, fieldSize.height * 2));
        
        JLabel nameLabel = new JLabel("Club Name:");
        applyAppFont(nameLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        JLabel leadersLabel = new JLabel("Leaders (comma-sep):");
        applyAppFont(leadersLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(leadersLabel, gbc);
        gbc.gridx = 1; formPanel.add(leadersField, gbc);
        JLabel descLabel = new JLabel("Description:");
        applyAppFont(descLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(descLabel, gbc);
        gbc.gridx = 1; formPanel.add(descScroll, gbc);

        JButton addButton = new JButton("Add Club");
        styleBorderlessButton(addButton);
        applyAppFont(addButton, Font.BOLD, 14);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(addButton, gbc);
        panel.add(formPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setOpaque(false);
        for (Club c : clubs) {
            JPanel cardWrapper = new JPanel();
            cardWrapper.setOpaque(false);
            cardWrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
            cardWrapper.add(createClickableClubCard(c));
            grid.add(cardWrapper);
        }
        JScrollPane gridScroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.getViewport().setBackground(pageBg);
        gridScroll.setBorder(null);
        panel.add(gridScroll, BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            String clubName = nameField.getText();
            String leaders = leadersField.getText();
            String description = descArea.getText();
            if (clubName.isEmpty() || description.isEmpty()) {
                showStatusMessage("Name and description are required.", true);
                return;
            }
            if (dbConn != null) {
                String nid = getLatestNid();
                if (nid == null) {
                    showStatusMessage("Error: Could not find any user in the login table.", true);
                    return;
                }
                String abbreviation = (nid.length() >= 6) ? nid.substring(3, 6) : "def";
                try (PreparedStatement ps = dbConn.prepareStatement("INSERT INTO clubs(nid, abbreviation, club_name, leaders, description) VALUES(?,?,?,?,?)")) {
                    ps.setString(1, nid);
                    ps.setString(2, abbreviation);
                    ps.setString(3, clubName);
                    ps.setString(4, leaders);
                    ps.setString(5, description);
                    ps.executeUpdate();
                    showStatusMessage("Club added successfully!", false);
                    nameField.setText(""); leadersField.setText(""); descArea.setText("");
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error adding club: " + ex.getMessage(), true);
                }
            } else {
                showStatusMessage("Database not connected.", true);
            }
        });

        return panel;
    }

    private JPanel createManageEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(pageBg);
        panel.setBorder(new EmptyBorder(16,16,16,16));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Event"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField eventNameField = new JTextField(20);
        JTextField clubNameField = new JTextField(20);
        JTextField leadersField = new JTextField(20);
        JTextField venueField = new JTextField(20);
        JTextField dateField = new JTextField(20);
        JTextField timeField = new JTextField(20);
        Dimension fieldSize = new Dimension(280, 35);
        eventNameField.setPreferredSize(fieldSize);
        clubNameField.setPreferredSize(fieldSize);
        leadersField.setPreferredSize(fieldSize);
        venueField.setPreferredSize(fieldSize);
        dateField.setPreferredSize(fieldSize);
        timeField.setPreferredSize(fieldSize);

        JLabel eventNameLabel = new JLabel("Event Name:");
        applyAppFont(eventNameLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(eventNameLabel, gbc);
        gbc.gridx = 1; formPanel.add(eventNameField, gbc);
        JLabel clubLabel = new JLabel("Club Name:");
        applyAppFont(clubLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(clubLabel, gbc);
        gbc.gridx = 1; formPanel.add(clubNameField, gbc);
        JLabel leadersLabel = new JLabel("Leaders (comma-sep):");
        applyAppFont(leadersLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(leadersLabel, gbc);
        gbc.gridx = 1; formPanel.add(leadersField, gbc);
        JLabel venueLabel = new JLabel("Venue:");
        applyAppFont(venueLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(venueLabel, gbc);
        gbc.gridx = 1; formPanel.add(venueField, gbc);
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        applyAppFont(dateLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(dateLabel, gbc);
        gbc.gridx = 1; formPanel.add(dateField, gbc);
        JLabel timeLabel = new JLabel("Time (HH:MM AM/PM):");
        applyAppFont(timeLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(timeLabel, gbc);
        gbc.gridx = 1; formPanel.add(timeField, gbc);
        
        JButton addButton = new JButton("Add Event");
        styleBorderlessButton(addButton);
        applyAppFont(addButton, Font.BOLD, 14);
        gbc.gridx = 1; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(addButton, gbc);
        panel.add(formPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setOpaque(false);
        for (EventItem ev : events) {
            JPanel cardWrapper = new JPanel();
            cardWrapper.setOpaque(false);
            cardWrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
            cardWrapper.add(createClickableEventCard(ev));
            grid.add(cardWrapper);
        }
        JScrollPane scroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(pageBg);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            if (eventNameField.getText().isEmpty()) {
                showStatusMessage("Event name is required.", true);
                return;
            }
            if (dbConn != null) {
                String nid = getLatestNid();
                if (nid == null) {
                    showStatusMessage("Error: Could not find any user in the login table.", true);
                    return;
                }
                String abbreviation = (nid.length() >= 6) ? nid.substring(3, 6) : "def";
                try (PreparedStatement ps = dbConn.prepareStatement("INSERT INTO events(nid, abbreviation, event_name, club_name, leaders, venue, date, time) VALUES(?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, nid);
                    ps.setString(2, abbreviation);
                    ps.setString(3, eventNameField.getText());
                    ps.setString(4, clubNameField.getText());
                    ps.setString(5, leadersField.getText());
                    ps.setString(6, venueField.getText());
                    ps.setString(7, dateField.getText());
                    ps.setString(8, timeField.getText());
                    ps.executeUpdate();
                    showStatusMessage("Event added successfully!", false);
                    eventNameField.setText(""); clubNameField.setText(""); leadersField.setText(""); venueField.setText(""); dateField.setText(""); timeField.setText("");
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error adding event: " + ex.getMessage(), true);
                }
            } else {
                showStatusMessage("Database not connected.", true);
            }
        });

        return panel;
    }

    private RoundedPanel createStatCard(String big, String small, Color color) {
        RoundedPanel card = new RoundedPanel(10, pageBg, false);
        card.setPreferredSize(new Dimension(180, 70));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,14,10,14));
        JLabel bigLbl = new JLabel(big);
        applyAppFont(bigLbl, Font.BOLD, 22);
        bigLbl.setForeground(color);
        JLabel lbl = new JLabel(small);
        applyAppFont(lbl, Font.PLAIN, 12);
        lbl.setForeground(Color.GRAY);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(bigLbl, BorderLayout.NORTH);
        left.add(lbl, BorderLayout.SOUTH);
        card.add(left, BorderLayout.WEST);
        return card;
    }

    private JPanel createClickableClubCard(Club c) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, true);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,10,10,10));
        card.setPreferredSize(new Dimension(500, 60));
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + c.clubName + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openClubAdminPopup(c);
            }
        });
        return card;
    }

    private JPanel createClickableEventCard(EventItem ev) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, true);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,10,10,10));
        card.setPreferredSize(new Dimension(500, 60));
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + ev.eventName + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openEventAdminPopup(ev);
            }
        });
        return card;
    }

    private JPanel createClubCardForList(Club c) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,10,10,10));
        card.setPreferredSize(new Dimension(220, 50));
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + c.clubName + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEventCardForList(EventItem e) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,10,10,10));
        card.setPreferredSize(new Dimension(220, 50));
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + e.eventName + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.CENTER);
        return card;
    }
    
    // --- Popups (Redesigned) ---
    
    private void createGlassPane() {
        glass = new JPanel(null);
        glass.setOpaque(true);
        glass.setBackground(new Color(0,0,0,100));
        glass.setVisible(false);
        parentFrame.setGlassPane(glass); // Set the glass pane on the main frame
    }
    
    private void showPopup(JComponent content) {
        glass.removeAll();
        popupPanel = new RoundedPanel(12, Color.WHITE, false);
        popupPanel.setLayout(new BorderLayout());
        popupPanel.setBorder(new EmptyBorder(25,25,25,25));
        popupPanel.add(content, BorderLayout.CENTER);
        popupPanel.setSize(600, 450);
        Dimension size = parentFrame.getContentPane().getSize();
        int x = (size.width - popupPanel.getWidth()) / 2;
        int y = (size.height - popupPanel.getHeight()) / 2;
        popupPanel.setLocation(x,y);
        glass.add(popupPanel);
        glass.setVisible(true);
    }

    private void closePopup() {
        if (glass != null) {
            glass.setVisible(false);
            glass.removeAll();
        }
    }
    
    private JComponent createDetailLine(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        JLabel labelComp = new JLabel(label);
        applyAppFont(labelComp, Font.BOLD, 16);
        JLabel valueComp = new JLabel(value);
        applyAppFont(valueComp, Font.PLAIN, 16);
        panel.add(labelComp, BorderLayout.WEST);
        panel.add(valueComp, BorderLayout.CENTER);
        return panel;
    }


    private void openClubAdminPopup(Club c) {
        JPanel p = new JPanel(new BorderLayout(10,20));
        p.setOpaque(false);
        JLabel title = new JLabel(c.clubName);
        applyAppFont(title, Font.BOLD, 22);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(title, BorderLayout.NORTH);
        
        JPanel detailsPanel = new JPanel();
        detailsPanel.setOpaque(false);
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.add(createDetailLine("Leaders:", c.leadersString()));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JTextArea descArea = new JTextArea("Description:\n" + c.description);
        applyAppFont(descArea, Font.PLAIN, 16);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        detailsPanel.add(descArea);
        
        p.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);
        JButton backButton = new JButton("Back");
        styleBorderlessButton(backButton);
        applyAppFont(backButton, Font.BOLD, 14);
        backButton.addActionListener(e -> closePopup());
        JButton editButton = new JButton("Edit");
        styleBorderlessButton(editButton);
        applyAppFont(editButton, Font.BOLD, 14);
        editButton.addActionListener(e -> {
            closePopup();
            openEditClubPopup(c);
        });
        JButton deleteButton = new JButton("Delete");
        styleBorderlessButton(deleteButton);
        applyAppFont(deleteButton, Font.BOLD, 14);
        deleteButton.addActionListener(e -> {
            if (dbConn != null) {
                try (PreparedStatement ps = dbConn.prepareStatement("DELETE FROM clubs WHERE id = ?")) {
                    ps.setInt(1, c.id);
                    ps.executeUpdate();
                    showStatusMessage("Club deleted.", false);
                    closePopup();
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error deleting club.", true);
                }
            } else {
                showStatusMessage("Database not connected.", true);
            }
        });
        buttons.add(backButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        p.add(buttons, BorderLayout.SOUTH);
        showPopup(p);
    }
    
    private void openEditClubPopup(Club c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Edit Club"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(c.clubName, 20);
        JTextField leadersField = new JTextField(c.leadersString(), 20);
        JTextArea descArea = new JTextArea(c.description, 4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        Dimension fieldSize = new Dimension(280, 35);
        nameField.setPreferredSize(fieldSize);
        leadersField.setPreferredSize(fieldSize);
        descArea.setPreferredSize(new Dimension(fieldSize.width, fieldSize.height * 2));
        
        JLabel nameLabel = new JLabel("Club Name:");
        applyAppFont(nameLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 0; p.add(nameLabel, gbc);
        gbc.gridx = 1; p.add(nameField, gbc);
        JLabel leadersLabel = new JLabel("Leaders:");
        applyAppFont(leadersLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 1; p.add(leadersLabel, gbc);
        gbc.gridx = 1; p.add(leadersField, gbc);
        JLabel descLabel = new JLabel("Description:");
        applyAppFont(descLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 2; p.add(descLabel, gbc);
        gbc.gridx = 1; p.add(new JScrollPane(descArea), gbc);
        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);
        JButton backButton = new JButton("Back");
        styleBorderlessButton(backButton);
        applyAppFont(backButton, Font.BOLD, 14);
        backButton.addActionListener(e -> { closePopup(); openClubAdminPopup(c); });
        JButton updateButton = new JButton("Update");
        styleBorderlessButton(updateButton);
        applyAppFont(updateButton, Font.BOLD, 14);
        updateButton.addActionListener(e -> {
            if (dbConn != null) {
                try(PreparedStatement ps = dbConn.prepareStatement("UPDATE clubs SET club_name = ?, description = ?, leaders = ? WHERE id = ?")) {
                    ps.setString(1, nameField.getText());
                    ps.setString(2, descArea.getText());
                    ps.setString(3, leadersField.getText());
                    ps.setInt(4, c.id);
                    ps.executeUpdate();
                    showStatusMessage("Club updated.", false);
                    closePopup();
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error updating club.", true);
                }
            } else {
                showStatusMessage("Database not connected.", true);
            }
        });
        buttons.add(backButton);
        buttons.add(updateButton);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        p.add(buttons, gbc);
        showPopup(p);
    }
    
    private void openEventAdminPopup(EventItem ev) {
        JPanel p = new JPanel(new BorderLayout(10,20));
        p.setOpaque(false);
        JLabel title = new JLabel(ev.eventName);
        applyAppFont(title, Font.BOLD, 22);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(title, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setOpaque(false);
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.add(createDetailLine("Club:", ev.clubName));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        detailsPanel.add(createDetailLine("Leaders:", ev.leadersString()));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        detailsPanel.add(createDetailLine("Venue:", ev.venue));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        detailsPanel.add(createDetailLine("Date:", ev.date));
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        detailsPanel.add(createDetailLine("Time:", ev.time));
        
        p.add(detailsPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);
        JButton backButton = new JButton("Back");
        styleBorderlessButton(backButton);
        applyAppFont(backButton, Font.BOLD, 14);
        backButton.addActionListener(e -> closePopup());
        JButton editButton = new JButton("Edit");
        styleBorderlessButton(editButton);
        applyAppFont(editButton, Font.BOLD, 14);
        editButton.addActionListener(e -> {
            closePopup();
            openEditEventPopup(ev);
        });
        JButton deleteButton = new JButton("Delete");
        styleBorderlessButton(deleteButton);
        applyAppFont(deleteButton, Font.BOLD, 14);
        deleteButton.addActionListener(e -> {
            if (dbConn != null) {
                try (PreparedStatement ps = dbConn.prepareStatement("DELETE FROM events WHERE id = ?")) {
                    ps.setInt(1, ev.id);
                    ps.executeUpdate();
                    showStatusMessage("Event deleted.", false);
                    closePopup();
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error deleting event.", true);
                }
            } else {
                showStatusMessage("Database not connected.", true);
            }
        });
        buttons.add(backButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        p.add(buttons, BorderLayout.SOUTH);
        showPopup(p);
    }
    
    private void openEditEventPopup(EventItem ev) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Edit Event"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField eventNameField = new JTextField(ev.eventName, 20);
        JTextField clubNameField = new JTextField(ev.clubName, 20);
        JTextField leadersField = new JTextField(ev.leadersString(), 20);
        JTextField venueField = new JTextField(ev.venue, 20);
        JTextField dateField = new JTextField(ev.date, 20);
        JTextField timeField = new JTextField(ev.time, 20);
        Dimension fieldSize = new Dimension(280, 35);
        eventNameField.setPreferredSize(fieldSize);
        clubNameField.setPreferredSize(fieldSize);
        leadersField.setPreferredSize(fieldSize);
        venueField.setPreferredSize(fieldSize);
        dateField.setPreferredSize(fieldSize);
        timeField.setPreferredSize(fieldSize);
        
        // Form fields...
        gbc.gridx=0; gbc.gridy=0; p.add(new JLabel("Event Name:"), gbc); gbc.gridx=1; p.add(eventNameField, gbc);
        gbc.gridx=0; gbc.gridy=1; p.add(new JLabel("Club Name:"), gbc); gbc.gridx=1; p.add(clubNameField, gbc);
        gbc.gridx=0; gbc.gridy=2; p.add(new JLabel("Leaders:"), gbc); gbc.gridx=1; p.add(leadersField, gbc);
        gbc.gridx=0; gbc.gridy=3; p.add(new JLabel("Venue:"), gbc); gbc.gridx=1; p.add(venueField, gbc);
        gbc.gridx=0; gbc.gridy=4; p.add(new JLabel("Date:"), gbc); gbc.gridx=1; p.add(dateField, gbc);
        gbc.gridx=0; gbc.gridy=5; p.add(new JLabel("Time:"), gbc); gbc.gridx=1; p.add(timeField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);
        JButton backButton = new JButton("Back");
        styleBorderlessButton(backButton);
        applyAppFont(backButton, Font.BOLD, 14);
        backButton.addActionListener(e -> { closePopup(); openEventAdminPopup(ev); });
        JButton updateButton = new JButton("Update");
        styleBorderlessButton(updateButton);
        applyAppFont(updateButton, Font.BOLD, 14);
        updateButton.addActionListener(e -> {
            if (dbConn != null) {
                try(PreparedStatement ps = dbConn.prepareStatement("UPDATE events SET event_name=?, club_name=?, leaders=?, venue=?, date=?, time=? WHERE id = ?")) {
                    ps.setString(1, eventNameField.getText()); ps.setString(2, clubNameField.getText()); ps.setString(3, leadersField.getText());
                    ps.setString(4, venueField.getText()); ps.setString(5, dateField.getText()); ps.setString(6, timeField.getText());
                    ps.setInt(7, ev.id);
                    ps.executeUpdate();
                    showStatusMessage("Event updated.", false);
                    closePopup();
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error updating event.", true);
                }
            } else {
                showStatusMessage("Database not connected.", true);
            }
        });
        buttons.add(backButton);
        buttons.add(updateButton);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        p.add(buttons, gbc);
        showPopup(p);
    }
    
    // --- Logout Functionality ---
    private void performLogout() {
        if (dbConn != null) {
            try {
                dbConn.close();
            } catch (SQLException ex) {
                System.err.println("Error closing database connection on logout: " + ex.getMessage());
            }
        }
        
        // Use the static method from LoginRegisterUI to switch the content pane back
        LoginRegisterUI.show(parentFrame);
    }

    // --- Utility Methods & Inner Classes ---
    
    private String getCurrentVisibleCard() {
        if (currentCardIndex >= 0 && currentCardIndex < navNames.length) return navNames[currentCardIndex];
        return "Dashboard";
    }
    private void makeButtonTransparent(AbstractButton b) {
        b.setFocusPainted(false); b.setOpaque(false); b.setContentAreaFilled(false); b.setBorder(null);
        b.setForeground(accentBlue);
        b.addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){ b.setForeground(new Color(20,70,200)); } @Override public void mouseExited(MouseEvent e){ b.setForeground(accentBlue); }});
    }
    private void applyAppFont(Component c, int style, int size) { c.setFont(new Font(PREFERRED_FONT, style, size)); }
    private void styleBorderlessButton(JButton button) {
        button.setOpaque(false); button.setContentAreaFilled(false); button.setBorderPainted(false); button.setFocusPainted(false);
        button.setForeground(accentBlue);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setForeground(new Color(20, 70, 200)); }
            @Override public void mouseExited(MouseEvent e) { button.setForeground(accentBlue); }
        });
    }
    private class RoundedPanel extends JPanel {
        private int radius; private Color currentBg; private boolean isClickable; private Color defaultBg;
        public RoundedPanel(int radius, Color bg, boolean isClickable) { 
            super(); this.radius = radius; this.defaultBg = bg; this.currentBg = bg; this.isClickable = isClickable; setOpaque(false); 
            if (isClickable) addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { currentBg = defaultBg.brighter(); repaint(); }
                @Override public void mouseExited(MouseEvent e) { currentBg = defaultBg; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentBg); 
            g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,radius,radius));
            g2.setColor(cardBorder); g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-3,getHeight()-3,radius,radius));
            g2.dispose();
        }
        @Override public void setBackground(Color color) { this.defaultBg = color; this.currentBg = color; }
        @Override public Color getBackground() { return defaultBg; }
    }
    private class SidebarHoverAdapter extends MouseAdapter {
        private final JButton btn; private Color normal = new Color(248,249,251); private Color hover = new Color(238,243,255);
        public SidebarHoverAdapter(JButton b) { btn = b; }
        @Override public void mouseEntered(MouseEvent e){ if (btn.getBackground().equals(normal)) btn.setBackground(hover); }
        @Override public void mouseExited(MouseEvent e){ Color sel = new Color(233,241,255); if (!btn.getBackground().equals(sel)) btn.setBackground(normal); }
    }

    // --- Data Model Classes (Updated) ---
    private class Club { 
        int id; String nid, abbreviation, clubName, description; String[] leaders; 
        Club(int id, String nid, String abbr, String name, String desc, String[] leaders){
            this.id = id; this.nid = nid; this.abbreviation = abbr; this.clubName = name; this.description = desc; this.leaders = leaders;
        } 
        String leadersString(){ if (leaders==null||leaders.length==0) return ""; return String.join(", ", leaders); }
    }
    private class EventItem { 
        int id; String nid, abbreviation, eventName, clubName, venue, date, time; String[] leaders; 
        EventItem(int id, String nid, String abbr, String eventName, String clubName, String[] leaders, String venue, String date, String time){
            this.id=id; this.nid = nid; this.abbreviation = abbr; this.eventName=eventName; this.clubName=clubName; this.leaders=leaders; this.venue=venue; this.date=date; this.time=time;
        } 
        String leadersString(){ if (leaders==null||leaders.length==0) return ""; return String.join(", ", leaders); } 
    }
}

