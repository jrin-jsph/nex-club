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

public class CopyOfAdminDashboard extends JFrame {
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

    private String collegeName = "Default College"; // To store fetched college name
    private JLabel dashboardTitleLabel; // Field for the title label

    public CopyOfAdminDashboard() {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(pageBg);
        
        // MOVED: Initialize the top bar first so the status label exists.
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);
        
        connectDatabase();
        if (dbConn == null) {
            showStatusMessage("DB connection failed.", true);
        }
        
        prepareTables();
        loadDataFromDB();
        fetchCollegeName(); // Fetch college name after DB connection

        // Top bar is now created before this, so we update the title here.
        dashboardTitleLabel.setText("Admin Control Panel - " + collegeName);
        
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(pageBg);
        contentCards.add(createDashboardPanel(), "Dashboard");
        contentCards.add(createManageClubsPanel(), "Manage Clubs");
        contentCards.add(createManageEventsPanel(), "Manage Events");
        add(contentCards, BorderLayout.CENTER);

        createGlassPane();
        
        SwingUtilities.invokeLater(() -> animateIndicatorTo(0));
        pack();
        setVisible(true);
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
            st.execute("CREATE TABLE IF NOT EXISTS clubs (id INT AUTO_INCREMENT PRIMARY KEY, nid VARCHAR(255) DEFAULT 'default_nid', name VARCHAR(255), short_desc TEXT, leaders TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS events (id INT AUTO_INCREMENT PRIMARY KEY, nid VARCHAR(255) DEFAULT 'default_nid', title VARCHAR(255), club_name VARCHAR(255), leaders TEXT, venue VARCHAR(255), date VARCHAR(50), time VARCHAR(50))");
            // Assuming a colleges table exists or creating a dummy one for demonstration
            st.execute("CREATE TABLE IF NOT EXISTS colleges (abbreviation VARCHAR(10) PRIMARY KEY, full_name VARCHAR(255))");
            st.execute("INSERT IGNORE INTO colleges (abbreviation, full_name) VALUES ('mst', 'Muthoot Institute of Science and Technology')");
            st.execute("INSERT IGNORE INTO colleges (abbreviation, full_name) VALUES ('xyz', 'XYZ University')");
        } catch (SQLException e) {
            showStatusMessage("Failed to prepare DB tables.", true);
        }
    }

    private void fetchCollegeName() {
        if (dbConn == null) return;
        try (Statement st = dbConn.createStatement()) {
            // Fetch one nid from clubs table to get the college abbreviation
            ResultSet rsNid = st.executeQuery("SELECT nid FROM clubs LIMIT 1");
            String fetchedNid = null;
            if (rsNid.next()) {
                fetchedNid = rsNid.getString("nid");
            }
            rsNid.close();

            if (fetchedNid != null && fetchedNid.length() >= 6) {
                String abbreviation = fetchedNid.substring(3, 6).toLowerCase(); // Extract characters after first 3
                ResultSet rsCollege = st.executeQuery("SELECT full_name FROM colleges WHERE abbreviation = '" + abbreviation + "'");
                if (rsCollege.next()) {
                    collegeName = rsCollege.getString("full_name");
                }
                rsCollege.close();
            }
        } catch (SQLException e) {
            showStatusMessage("Failed to fetch college name: " + e.getMessage(), true);
        }
    }


    private void loadDataFromDB() {
        clubs.clear(); events.clear();
        if (dbConn == null) {
            showStatusMessage("Cannot load data; DB not connected.", true);
            return;
        }
        try {
            try (Statement st = dbConn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT * FROM clubs ORDER BY id DESC"); // Order by ID DESC for recent
                while (rs.next()) clubs.add(new Club(rs.getInt("id"), rs.getString("name"), rs.getString("short_desc"), split(rs.getString("leaders"))));
                rs = st.executeQuery("SELECT * FROM events ORDER BY id DESC"); // Order by ID DESC for recent
                while (rs.next()) events.add(new EventItem(rs.getInt("id"), rs.getString("title"), rs.getString("club_name"), split(rs.getString("leaders")), rs.getString("venue"), rs.getString("date"), rs.getString("time")));
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
        side.setBackground(new Color(248,249,251)); // Retaining this specific background color
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
            b.setBackground(new Color(248,249,251)); // Retaining this specific background color
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
                g2.setColor(new Color(233,241,255)); // Retaining this specific background color
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
        for (int i = 0; i < navButtons.length; i++) {
            JButton b = navButtons[i];
            if (i == index) {
                b.setBackground(new Color(233,241,255)); // Retaining this specific background color
                b.setForeground(accentBlue);
                applyAppFont(b, Font.BOLD, 14);
            } else {
                b.setBackground(new Color(248,249,251)); // Retaining this specific background color
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
        // Use a placeholder initially, it will be updated after fetching the college name.
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
        if (statusTimer.isRunning()) statusTimer.stop();
        statusLabel.setText(message);
        statusLabel.setBackground(isError ? new Color(255, 220, 220) : new Color(220, 255, 220));
        statusLabel.setForeground(isError ? Color.RED.darker() : Color.GREEN.darker());
        statusLabel.setVisible(true);
        statusTimer.start();
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

        // Recently Added Clubs (max 4)
        JPanel recentClubsGrid = new JPanel();
        recentClubsGrid.setLayout(new BoxLayout(recentClubsGrid, BoxLayout.Y_AXIS));
        recentClubsGrid.setOpaque(false);
        for (int i = 0; i < Math.min(4, clubs.size()); i++) {
            recentClubsGrid.add(createClubCardForList(clubs.get(i)));
            if (i < Math.min(4, clubs.size()) - 1) {
                recentClubsGrid.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        if (clubs.isEmpty()) {
            recentClubsGrid.add(new JLabel("No clubs added yet.", SwingConstants.CENTER));
        }
        JScrollPane recentClubsScroll = new JScrollPane(recentClubsGrid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recentClubsScroll.getViewport().setBackground(pageBg);
        recentClubsScroll.setBorder(null);
        recentActivityPanel.add(recentClubsScroll);

        // Recently Added Events (max 4)
        JPanel recentEventsGrid = new JPanel();
        recentEventsGrid.setLayout(new BoxLayout(recentEventsGrid, BoxLayout.Y_AXIS));
        recentEventsGrid.setOpaque(false);
        for (int i = 0; i < Math.min(4, events.size()); i++) {
            recentEventsGrid.add(createEventCardForList(events.get(i)));
            if (i < Math.min(4, events.size()) - 1) {
                recentEventsGrid.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        if (events.isEmpty()) {
            recentEventsGrid.add(new JLabel("No events added yet.", SwingConstants.CENTER));
        }
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
        gbc.insets = new Insets(8, 8, 8, 8); // Increased padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField leadersField = new JTextField(20);
        JTextArea descArea = new JTextArea(4, 20); // Increased rows
        JScrollPane descScroll = new JScrollPane(descArea);
        
        Dimension fieldSize = new Dimension(280, 35); // Approx 15% larger
        nameField.setPreferredSize(fieldSize);
        leadersField.setPreferredSize(fieldSize);
        descArea.setPreferredSize(new Dimension(fieldSize.width, fieldSize.height * 2)); // Adjust for TextArea

        JLabel nameLabel = new JLabel("Club Name:");
        applyAppFont(nameLabel, Font.PLAIN, 15); // Larger font
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);
        
        JLabel leadersLabel = new JLabel("Leaders (comma-sep):");
        applyAppFont(leadersLabel, Font.PLAIN, 15); // Larger font
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(leadersLabel, gbc);
        gbc.gridx = 1; formPanel.add(leadersField, gbc);
        
        JLabel descLabel = new JLabel("Description:");
        applyAppFont(descLabel, Font.PLAIN, 15); // Larger font
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(descLabel, gbc);
        gbc.gridx = 1; formPanel.add(descScroll, gbc);

        JButton addButton = new JButton("Add Club");
        styleBorderlessButton(addButton); // Apply new button style
        applyAppFont(addButton, Font.BOLD, 14); // Slightly larger font for buttons
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(addButton, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS)); // Single column
        grid.setOpaque(false);
        for (Club c : clubs) {
            JPanel cardWrapper = new JPanel();
            cardWrapper.setOpaque(false);
            cardWrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10)); // Add spacing between cards
            cardWrapper.add(createClickableClubCard(c));
            grid.add(cardWrapper);
        }
        JScrollPane gridScroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.getViewport().setBackground(pageBg);
        gridScroll.setBorder(null);
        panel.add(gridScroll, BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String leaders = leadersField.getText();
            String desc = descArea.getText();
            if (name.isEmpty() || desc.isEmpty()) {
                showStatusMessage("Name and description are required.", true);
                return;
            }
            if (dbConn != null) {
                try (PreparedStatement ps = dbConn.prepareStatement("INSERT INTO clubs(name, short_desc, leaders, nid) VALUES(?,?,?,?)")) {
                    ps.setString(1, name); ps.setString(2, desc); ps.setString(3, leaders); ps.setString(4, "mut001"); // Default NID
                    ps.executeUpdate();
                    showStatusMessage("Club added successfully!", false);
                    nameField.setText(""); leadersField.setText(""); descArea.setText("");
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error adding club to DB.", true);
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
        gbc.insets = new Insets(8, 8, 8, 8); // Increased padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextField clubNameField = new JTextField(20);
        JTextField leadersField = new JTextField(20);
        JTextField venueField = new JTextField(20);
        JTextField dateField = new JTextField(20);
        JTextField timeField = new JTextField(20);
        
        Dimension fieldSize = new Dimension(280, 35); // Approx 15% larger
        titleField.setPreferredSize(fieldSize);
        clubNameField.setPreferredSize(fieldSize);
        leadersField.setPreferredSize(fieldSize);
        venueField.setPreferredSize(fieldSize);
        dateField.setPreferredSize(fieldSize);
        timeField.setPreferredSize(fieldSize);

        JLabel titleLabel = new JLabel("Event Title:");
        applyAppFont(titleLabel, Font.PLAIN, 15);
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(titleLabel, gbc);
        gbc.gridx = 1; formPanel.add(titleField, gbc);
        
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
        styleBorderlessButton(addButton); // Apply new button style
        applyAppFont(addButton, Font.BOLD, 14); // Slightly larger font for buttons
        gbc.gridx = 1; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(addButton, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS)); // Single column
        grid.setOpaque(false);
        for (EventItem ev : events) {
            JPanel cardWrapper = new JPanel();
            cardWrapper.setOpaque(false);
            cardWrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10)); // Add spacing
            cardWrapper.add(createClickableEventCard(ev));
            grid.add(cardWrapper);
        }
        JScrollPane scroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(pageBg);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            if (titleField.getText().isEmpty()) {
                showStatusMessage("Event title is required.", true);
                return;
            }
            if (dbConn != null) {
                try (PreparedStatement ps = dbConn.prepareStatement("INSERT INTO events(title, club_name, leaders, venue, date, time, nid) VALUES(?,?,?,?,?,?,?)")) {
                    ps.setString(1, titleField.getText()); ps.setString(2, clubNameField.getText()); ps.setString(3, leadersField.getText());
                    ps.setString(4, venueField.getText()); ps.setString(5, dateField.getText()); ps.setString(6, timeField.getText());
                    ps.setString(7, "mut001"); // Default NID
                    ps.executeUpdate();
                    showStatusMessage("Event added successfully!", false);
                    titleField.setText(""); clubNameField.setText(""); leadersField.setText(""); venueField.setText(""); dateField.setText(""); timeField.setText("");
                    refreshAllPanels();
                } catch (SQLException ex) {
                    showStatusMessage("Error adding event to DB.", true);
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
        card.setPreferredSize(new Dimension(500, 60)); // Larger dimensions
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + c.name + "</b></html>");
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
        card.setPreferredSize(new Dimension(500, 60)); // Larger dimensions
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + ev.title + "</b></html>");
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
        card.setPreferredSize(new Dimension(220, 50)); // Adjusted size for dashboard recent list
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + c.name + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEventCardForList(EventItem e) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,10,10,10));
        card.setPreferredSize(new Dimension(220, 50)); // Adjusted size for dashboard recent list
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + e.title + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.CENTER);
        return card;
    }
    
    // --- Popups ---
    
    private void createGlassPane() {
        glass = new JPanel(null);
        glass.setOpaque(true);
        glass.setBackground(new Color(0,0,0,100));
        glass.setVisible(false);
        setGlassPane(glass);
    }
    
    private void showPopup(JComponent content) {
        glass.removeAll();
        popupPanel = new RoundedPanel(12, Color.WHITE, false);
        popupPanel.setLayout(new BorderLayout());
        popupPanel.setBorder(new EmptyBorder(16,16,16,16));
        popupPanel.add(content, BorderLayout.CENTER);
        
        popupPanel.setSize(600, 400);
        Dimension size = getContentPane().getSize();
        int x = (size.width - popupPanel.getWidth()) / 2;
        int y = (size.height - popupPanel.getHeight()) / 2;
        popupPanel.setLocation(x,y);
        
        glass.add(popupPanel);
        glass.setVisible(true);
    }

    private void closePopup() {
        glass.setVisible(false);
        glass.removeAll();
    }

    private void openClubAdminPopup(Club c) {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setOpaque(false);
        JLabel title = new JLabel("<html><div style='font-size:18px;'><b>" + c.name + "</b></div></html>");
        p.add(title, BorderLayout.NORTH);
        JTextArea details = new JTextArea("Leaders: " + c.leadersString() + "\n\nDescription:\n" + c.shortDesc);
        details.setEditable(false);
        details.setOpaque(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        p.add(new JScrollPane(details), BorderLayout.CENTER);

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

        JTextField nameField = new JTextField(c.name, 20);
        JTextField leadersField = new JTextField(c.leadersString(), 20);
        JTextArea descArea = new JTextArea(c.shortDesc, 4, 20);
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
        backButton.addActionListener(e -> { closePopup(); openClubAdminPopup(c); }); // Go back to view popup
        
        JButton updateButton = new JButton("Update");
        styleBorderlessButton(updateButton);
        applyAppFont(updateButton, Font.BOLD, 14);
        updateButton.addActionListener(e -> {
            if (dbConn != null) {
                try(PreparedStatement ps = dbConn.prepareStatement("UPDATE clubs SET name = ?, short_desc = ?, leaders = ? WHERE id = ?")) {
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
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setOpaque(false);
        JLabel title = new JLabel("<html><div style='font-size:18px;'><b>" + ev.title + "</b></div></html>");
        p.add(title, BorderLayout.NORTH);
        String detailsText = "Club: " + ev.clubName + "\nLeaders: " + ev.leadersString() + "\nVenue: " + ev.venue + "\nDate: " + ev.date + "\nTime: " + ev.time;
        JTextArea details = new JTextArea(detailsText);
        details.setEditable(false);
        details.setOpaque(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        p.add(new JScrollPane(details), BorderLayout.CENTER);

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

        JTextField titleField = new JTextField(ev.title, 20);
        JTextField clubNameField = new JTextField(ev.clubName, 20);
        JTextField leadersField = new JTextField(ev.leadersString(), 20);
        JTextField venueField = new JTextField(ev.venue, 20);
        JTextField dateField = new JTextField(ev.date, 20);
        JTextField timeField = new JTextField(ev.time, 20);

        Dimension fieldSize = new Dimension(280, 35);
        titleField.setPreferredSize(fieldSize);
        clubNameField.setPreferredSize(fieldSize);
        leadersField.setPreferredSize(fieldSize);
        venueField.setPreferredSize(fieldSize);
        dateField.setPreferredSize(fieldSize);
        timeField.setPreferredSize(fieldSize);

        JLabel titleLabel = new JLabel("Title:");
        applyAppFont(titleLabel, Font.PLAIN, 15);
        gbc.gridx=0; gbc.gridy=0; p.add(titleLabel, gbc);
        gbc.gridx=1; p.add(titleField, gbc);
        
        JLabel clubLabel = new JLabel("Club Name:");
        applyAppFont(clubLabel, Font.PLAIN, 15);
        gbc.gridx=0; gbc.gridy=1; p.add(clubLabel, gbc);
        gbc.gridx=1; p.add(clubNameField, gbc);
        
        JLabel leadersLabel = new JLabel("Leaders:");
        applyAppFont(leadersLabel, Font.PLAIN, 15);
        gbc.gridx=0; gbc.gridy=2; p.add(leadersLabel, gbc);
        gbc.gridx=1; p.add(leadersField, gbc);
        
        JLabel venueLabel = new JLabel("Venue:");
        applyAppFont(venueLabel, Font.PLAIN, 15);
        gbc.gridx=0; gbc.gridy=3; p.add(venueLabel, gbc);
        gbc.gridx=1; p.add(venueField, gbc);
        
        JLabel dateLabel = new JLabel("Date:");
        applyAppFont(dateLabel, Font.PLAIN, 15);
        gbc.gridx=0; gbc.gridy=4; p.add(dateLabel, gbc);
        gbc.gridx=1; p.add(dateField, gbc);
        
        JLabel timeLabel = new JLabel("Time:");
        applyAppFont(timeLabel, Font.PLAIN, 15);
        gbc.gridx=0; gbc.gridy=5; p.add(timeLabel, gbc);
        gbc.gridx=1; p.add(timeField, gbc);
        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);

        JButton backButton = new JButton("Back");
        styleBorderlessButton(backButton);
        applyAppFont(backButton, Font.BOLD, 14);
        backButton.addActionListener(e -> { closePopup(); openEventAdminPopup(ev); }); // Go back to view popup

        JButton updateButton = new JButton("Update");
        styleBorderlessButton(updateButton);
        applyAppFont(updateButton, Font.BOLD, 14);
        updateButton.addActionListener(e -> {
            if (dbConn != null) {
                try(PreparedStatement ps = dbConn.prepareStatement("UPDATE events SET title=?, club_name=?, leaders=?, venue=?, date=?, time=? WHERE id = ?")) {
                    ps.setString(1, titleField.getText()); ps.setString(2, clubNameField.getText()); ps.setString(3, leadersField.getText());
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
    
    // --- Utility Methods ---
    
    private String getCurrentVisibleCard() {
        if (currentCardIndex >= 0 && currentCardIndex < navNames.length) {
            return navNames[currentCardIndex];
        }
        return "Dashboard";
    }

    private void makeButtonTransparent(AbstractButton b) {
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(null);
        b.setForeground(accentBlue);
        b.addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){ b.setForeground(new Color(20,70,200)); } @Override public void mouseExited(MouseEvent e){ b.setForeground(accentBlue); }});
    }

    private void applyAppFont(Component c, int style, int size) {
        c.setFont(new Font(PREFERRED_FONT, style, size));
    }

    private void styleBorderlessButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(accentBlue); // Use accent blue for visibility
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(20, 70, 200)); // Darker blue on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(accentBlue);
            }
        });
    }


    private class RoundedPanel extends JPanel {
        private int radius; private Color currentBg; private boolean isClickable;
        private Color defaultBg;
        public RoundedPanel(int radius, Color bg, boolean isClickable) { 
            super(); this.radius = radius; this.defaultBg = bg; this.currentBg = bg; this.isClickable = isClickable; setOpaque(false); 
            if (isClickable) {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { currentBg = defaultBg.brighter(); repaint(); }
                    @Override public void mouseExited(MouseEvent e) { currentBg = defaultBg; repaint(); }
                });
            }
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
        @Override public void setBackground(Color color) {
            this.defaultBg = color;
            this.currentBg = color; // Reset current background too
        }
        @Override public Color getBackground() {
            return defaultBg;
        }
    }

    private class SidebarHoverAdapter extends MouseAdapter {
        private final JButton btn; private Color normal = new Color(248,249,251); private Color hover = new Color(238,243,255);
        public SidebarHoverAdapter(JButton b) { btn = b; }
        @Override public void mouseEntered(MouseEvent e){ if (btn.getBackground().equals(normal)) btn.setBackground(hover); }
        @Override public void mouseExited(MouseEvent e){ Color sel = new Color(233,241,255); if (!btn.getBackground().equals(sel)) btn.setBackground(normal); }
    }

    public static void main(String[] args) {
        try { FlatLightLaf.setup(); } catch (Exception ex) { System.err.println("FlatLaf not found; add JAR to classpath."); }
        SwingUtilities.invokeLater(() -> new CopyOfAdminDashboard());
    }

    private class Club { 
        int id; String name, shortDesc; String[] leaders; 
        Club(int id, String a,String b,String[] c){this.id = id; name=a;shortDesc=b;leaders=c;} 
        String leadersString(){ if (leaders==null||leaders.length==0) return ""; return String.join(", ", leaders); }
    }
    private class EventItem { 
        int id; String title, clubName, venue, date, time; String[] leaders; 
        EventItem(int id, String t,String c,String[] l,String v,String d,String tm){this.id=id; title=t;clubName=c;leaders=l;venue=v;date=d;time=tm;} 
        String leadersString(){ if (leaders==null||leaders.length==0) return ""; return String.join(", ", leaders); } 
    }
}

