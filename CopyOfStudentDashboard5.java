// CopyOfStudentDashboard5.java
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
 * CopyOfStudentDashboard5 class, as a JFrame.
 * This version is compatible with being called from an external LoginRegisterUI.
 */
public class CopyOfStudentDashboard5 extends JFrame { // Reverted to JFrame
    private CardLayout cardLayout;
    private JPanel contentCards;
    private JButton[] navButtons;
    private String[] navNames = {"Dashboard", "Browse Clubs", "Events", "Notifications"};
    private Color accentBlue = new Color(40, 104, 255);
    private Color pageBg = Color.WHITE;
    private Color cardBorder = new Color(230, 230, 230); // Changed from white to light grey
    private int sidebarWidth = 220;
    private final String PREFERRED_FONT = "Microsoft JhengHei";
    private JPanel indicator;
    private int indicatorTargetY = 0;
    private Timer indicatorTimer;
    
    // Glass pane for popups
    private JPanel glass;
    private JPanel popupPanel;
    
    // Data lists
    private List<Club> clubs = new ArrayList<>(); // Clubs for student's department
    private List<EventItem> events = new ArrayList<>(); // Events for student's department
    private List<NotificationItem> notifications = new ArrayList<>();
    private List<Club> myClubs = new ArrayList<>(); // Clubs student has joined
    private List<Club> recentClubs = new ArrayList<>(); // 6 most recent clubs (for dashboard)
    private List<EventItem> myAppliedEvents = new ArrayList<>(); // Events student has applied for

    private int joinedClubsCount = 0;
    private int eventsThisMonthCount = 0;
    private int pendingApplicationsCount = 0;
    private Connection dbConn = null;
    
    // Student-specific data
    private String nid;
    private String abbreviation;

    // Status label for non-blocking messages
    private JLabel statusLabel;
    private Timer statusTimer;

    /**
     * Constructor for the CopyOfStudentDashboard5 Frame.
     * @param nid The NID of the logged-in student (e.g., "STUABC123").
     */
    public CopyOfStudentDashboard5(String nid) {
        super("Student Dashboard"); // JFrame constructor
        
        this.nid = nid;
        if (this.nid == null || this.nid.length() < 6) {
            this.nid = "STUERR000"; // Fallback NID
            this.abbreviation = "ERR";
        } else {
            this.abbreviation = this.nid.substring(3, 6).toUpperCase();
        }

        setTitle("Student Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close this window, not the app
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Window opens maximized
        setLayout(new BorderLayout());
        getContentPane().setBackground(pageBg);

        // UI must be initialized before showing status messages
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        connectDatabase();
        if (dbConn == null) {
            showStatusMessage("DB connection failed. App cannot function.", true);
        } else {
            prepareTables(); 
            loadDataFromDB(); 
        }
        
        // Pass the onLogoutAction to the sidebar creator
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(pageBg);
        contentCards.add(createDashboardPanel(), "Dashboard");
        contentCards.add(createBrowseClubsPanel(), "Browse Clubs");
        contentCards.add(createEventsPanel(), "Events");
        contentCards.add(createNotificationsPanel(), "Notifications");
        add(contentCards, BorderLayout.CENTER);
        
        // Create and add the glass pane for popups
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
            st.execute("CREATE TABLE IF NOT EXISTS my_clubs (id INT AUTO_INCREMENT PRIMARY KEY, nid VARCHAR(255), club_name VARCHAR(255))");
            st.execute("CREATE TABLE IF NOT EXISTS my_events (id INT AUTO_INCREMENT PRIMARY KEY, nid VARCHAR(255), club_name VARCHAR(255), event_name VARCHAR(255), date VARCHAR(50), time VARCHAR(50))");
        } catch (SQLException e) {
            showStatusMessage("Failed to prepare DB tables.", true);
        }
    }

    private void loadDataFromDB() {
        clubs.clear(); 
        events.clear(); 
        notifications.clear(); 
        myClubs.clear();
        recentClubs.clear();
        myAppliedEvents.clear();
        
        if (dbConn == null) {
            showStatusMessage("Database not connected. Cannot load data.", true);
            return;
        }

        try {
            // 1. Load department-specific clubs (for Browse Clubs tab)
            try (PreparedStatement ps = dbConn.prepareStatement("SELECT id, club_name, leaders, description FROM clubs WHERE abbreviation = ?")) {
                ps.setString(1, this.abbreviation);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    clubs.add(new Club(rs.getInt("id"), rs.getString("club_name"), rs.getString("description"), split(rs.getString("leaders"))));
                }
            }
            
            // 2. Load department-specific events (for Events tab)
            try (PreparedStatement ps = dbConn.prepareStatement("SELECT event_name, club_name, leaders, venue, date, time FROM events WHERE abbreviation = ?")) {
                ps.setString(1, this.abbreviation);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    events.add(new EventItem(rs.getString("event_name"), rs.getString("club_name"), split(rs.getString("leaders")), rs.getString("venue"), rs.getString("date"), rs.getString("time")));
                }
            }
            
            // 3. Populate Notifications from the events list
            for (EventItem ev : events) {
                notifications.add(new NotificationItem(ev.clubName, ev.title, ev.venue, ev.date, ev.time));
            }

            // 4. Load clubs *this student* has joined (for My Clubs section)
            try (PreparedStatement ps = dbConn.prepareStatement("SELECT c.id, c.club_name, c.description, c.leaders FROM clubs c JOIN my_clubs m ON c.club_name = m.club_name WHERE m.nid = ?")) {
                ps.setString(1, this.nid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    myClubs.add(new Club(rs.getInt("id"), rs.getString("club_name"), rs.getString("description"), split(rs.getString("leaders"))));
                }
            }
            
            // 5. Load 6 most recent clubs (for Dashboard "Discover" section)
            try (Statement st = dbConn.createStatement()) {
                 ResultSet rs = st.executeQuery("SELECT id, club_name, leaders, description FROM clubs ORDER BY id DESC LIMIT 6");
                 while (rs.next()) {
                    recentClubs.add(new Club(rs.getInt("id"), rs.getString("club_name"), rs.getString("description"), split(rs.getString("leaders"))));
                }
            }

            // 6. Load events *this student* has applied for (for Dashboard "Upcoming Events")
            try (PreparedStatement ps = dbConn.prepareStatement(
                "SELECT m.event_name, m.club_name, e.leaders, e.venue, m.date, m.time " +
                "FROM my_events m LEFT JOIN events e ON m.event_name = e.event_name AND m.club_name = e.club_name " +
                "WHERE m.nid = ?")) {
                ps.setString(1, this.nid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    myAppliedEvents.add(new EventItem(
                        rs.getString("event_name"), 
                        rs.getString("club_name"), 
                        split(rs.getString("leaders")), // leaders from 'events' table
                        rs.getString("venue"), // venue from 'events' table
                        rs.getString("date"), // date from 'my_events'
                        rs.getString("time") // time from 'my_events'
                    ));
                }
            }
            
        } catch (SQLException e) {
            showStatusMessage("Failed to load data from DB.", true);
            e.printStackTrace(); // For debugging
        }
        
        // Update counts
        joinedClubsCount = myClubs.size();
        eventsThisMonthCount = events.size(); // All events for the department
        pendingApplicationsCount = myAppliedEvents.size(); // All applied events
    }

    private String[] split(String s) {
        if (s == null || s.trim().isEmpty()) return new String[0];
        return s.split(",");
    }

    // loadStaticDefaults() method removed

    /**
     * Creates the sidebar panel.
     * @return The configured JPanel for the sidebar.
     */
    private JPanel createSidebar() { 
        JPanel side = new JPanel(new BorderLayout()); // Use BorderLayout
        side.setBackground(pageBg); // Changed from grey
        side.setPreferredSize(new Dimension(sidebarWidth, 0));
        side.setBorder(new EmptyBorder(18, 12, 18, 12));

        // Panel to hold nav and indicator using null layout
        JPanel topContent = new JPanel(null);
        topContent.setOpaque(false);

        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBounds(10, 30, sidebarWidth - 20, 300); // Bounds relative to topContent
        navButtons = new JButton[navNames.length];
        for (int i = 0; i < navNames.length; i++) {
            JButton b = new JButton(navNames[i]);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFocusPainted(false);
            b.setBackground(pageBg); // Changed from grey
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
        topContent.add(navPanel); // Add navPanel to topContent
        indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); // Changed from light blue to white
                g2.fillRoundRect(4, 0, 6, 44, 6, 6);
                g2.dispose();
            }
        };
        indicator.setOpaque(false);
        indicator.setSize(14, 44);
        indicator.setLocation(0, 30); // Location relative to topContent
        topContent.add(indicator); // Add indicator to topContent
        
        side.add(topContent, BorderLayout.CENTER); // Add topContent to CENTER

        JButton logout = new JButton("Logout");
        makeButtonTransparent(logout); // Use the helper method
        applyAppFont(logout, Font.BOLD, 13);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10, 2, 10, 2)); // Add padding
        
        // --- LOGOUT BUTTON CHANGED ---
        // It now just disposes this JFrame
        logout.addActionListener(e -> {
            this.dispose(); // Close the dashboard window
            // Your LoginRegisterUI window should regain focus
        });
        // --- END OF CHANGE ---

        side.add(logout, BorderLayout.SOUTH); // Add to SOUTH
        
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
                topContent.repaint(); // Repaint topContent
            }
        });
        return side;
    }

    private void animateIndicatorTo(int index) {
        if (index < 0 || index >= navButtons.length) return;
        int gap = 6;
        int yBase = 30;
        int itemHeight = 44 + gap;
        indicatorTargetY = yBase + index * itemHeight;
        for (int i = 0; i < navButtons.length; i++) {
            JButton b = navButtons[i];
            if (i == index) {
                b.setBackground(Color.WHITE); // Changed from light blue to white
                b.setForeground(accentBlue);
                applyAppFont(b, Font.BOLD, 14);
            } else {
                b.setBackground(pageBg); // Changed from grey
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
        JLabel dashboardLabel = new JLabel("Student Dashboard"); // Changed text
        dashboardLabel.setOpaque(true);
        dashboardLabel.setPreferredSize(new Dimension(420, 34));
        dashboardLabel.setBorder(new LineRoundedBorder(8, cardBorder, 1));
        dashboardLabel.setBackground(pageBg);
        dashboardLabel.setHorizontalAlignment(SwingConstants.LEFT);
        dashboardLabel.setBorder(new EmptyBorder(6,12,6,12));
        applyAppFont(dashboardLabel, Font.BOLD, 16);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(dashboardLabel);
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new GridBagLayout()); // Use GridBagLayout
        right.setOpaque(false);

        statusLabel = new JLabel(" ");
        applyAppFont(statusLabel, Font.BOLD, 13);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(220, 255, 220)); // Default green
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusLabel.setVisible(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 15); // Margin to the right
        gbc.gridx = 0;
        gbc.gridy = 0;
        right.add(statusLabel, gbc);

        // Removed "John Doe" label
        
        top.add(right, BorderLayout.EAST);

        // Timer to hide the status label
        statusTimer = new Timer(3000, e -> statusLabel.setVisible(false));
        statusTimer.setRepeats(false);

        return top;
    }

    // New method to show status messages
    private void showStatusMessage(String message, boolean isError) {
        if (statusTimer.isRunning()) {
            statusTimer.stop();
        }
        statusLabel.setText(message);
        if (isError) {
            statusLabel.setBackground(new Color(255, 220, 220)); // Reddish
            statusLabel.setForeground(Color.RED.darker());
        } else {
            statusLabel.setBackground(new Color(220, 255, 220)); // Greenish
            statusLabel.setForeground(Color.GREEN.darker());
        }
        statusLabel.setVisible(true);
        statusTimer.start();
    }


    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(pageBg);
        panel.setBorder(new EmptyBorder(16,16,16,16));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        // Changed "Welcome Back, John!" to "Welcome" and text color to black
        JLabel welcome = new JLabel("<html><div style='font-size:20px;'><b>Welcome</b></div><div style='color:black;'>Discover new opportunities and stay connected with your clubs.</div></html>");
        applyAppFont(welcome, Font.PLAIN, 14);
        header.add(welcome, BorderLayout.WEST);
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(12, 0, 12, 0));
        RoundedPanel joined = createStatCardClickable(String.valueOf(joinedClubsCount), "Joined Clubs", new Color(58,78,237), () -> { cardLayout.show(contentCards, "Browse Clubs"); animateIndicatorTo(1); });
        RoundedPanel eventsStat = createStatCardClickable(String.valueOf(eventsThisMonthCount), "Events This Month", new Color(118,81,255), () -> { cardLayout.show(contentCards, "Events"); animateIndicatorTo(2); });
        // Removed Pending Applications card
        stats.add(joined); 
        stats.add(eventsStat);
        header.add(stats, BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.78);
        split.setDividerSize(0);
        split.setBorder(null);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(16,0,0,16));
        JLabel discoverTitle = new JLabel("Discover Clubs");
        applyAppFont(discoverTitle, Font.BOLD, 16);
        discoverTitle.setBorder(new EmptyBorder(8,0,8,0));
        center.add(discoverTitle, BorderLayout.NORTH);
        JPanel gridPanel = new JPanel(new GridLayout(2,3,16,16));
        gridPanel.setOpaque(false);
        // Changed to use recentClubs list
        for (Club c : recentClubs) {
            gridPanel.add(createMinimalClubCard(c));
        }
        JScrollPane gridScroll = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.getViewport().setBackground(pageBg);
        gridScroll.setBorder(null);
        center.add(gridScroll, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(pageBg);
        rightPanel.setBorder(new EmptyBorder(0,0,0,0));
        rightPanel.setPreferredSize(new Dimension(340, 0));
        RoundedPanel upCard = new RoundedPanel(12, pageBg);
        upCard.setLayout(new BorderLayout());
        upCard.setBorder(new EmptyBorder(16,16,16,16));
        JLabel upTitle = new JLabel("Upcoming Events (My Applications)"); // Title clarified
        applyAppFont(upTitle, Font.BOLD, 14);
        upCard.add(upTitle, BorderLayout.NORTH);
        JPanel evList = new JPanel();
        evList.setOpaque(false);
        evList.setLayout(new BoxLayout(evList, BoxLayout.Y_AXIS));
        // Changed to use myAppliedEvents list
        for (EventItem ev : myAppliedEvents) {
            evList.add(createUpcomingEventRow(ev));
            evList.add(Box.createRigidArea(new Dimension(0,8)));
        }
        JScrollPane evScroll = new JScrollPane(evList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        evScroll.getViewport().setBackground(pageBg);
        evScroll.setBorder(null);
        upCard.add(evScroll, BorderLayout.CENTER);
        JPanel viewAllWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        viewAllWrap.setOpaque(false);
        JButton viewAll = new JButton("View All Events");
        makeButtonTransparent(viewAll);
        applyAppFont(viewAll, Font.PLAIN, 13);
        viewAll.addActionListener(e -> { cardLayout.show(contentCards, "Events"); animateIndicatorTo(2); });
        viewAllWrap.add(viewAll);
        upCard.add(viewAllWrap, BorderLayout.SOUTH);
        rightPanel.add(upCard, BorderLayout.CENTER);
        split.setLeftComponent(center);
        split.setRightComponent(rightPanel);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBrowseClubsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(pageBg);
        panel.setBorder(new EmptyBorder(16,16,16,16));
        JLabel title = new JLabel("Browse Clubs (Your Department)"); // Title clarified
        applyAppFont(title, Font.BOLD, 18);
        panel.add(title, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0,3,12,12));
        grid.setOpaque(false);
        // Populated from 'clubs' list (department specific)
        for (Club c : clubs) grid.add(createClubCardForList(c));
        JScrollPane gridScroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.getViewport().setBackground(pageBg);
        gridScroll.setBorder(null);
        panel.add(gridScroll, BorderLayout.CENTER);
        JPanel myClubsWrap = new JPanel(new BorderLayout());
        myClubsWrap.setOpaque(false);
        myClubsWrap.setBorder(new EmptyBorder(12,0,0,0));
        JLabel myTitle = new JLabel("My Clubs");
        applyAppFont(myTitle, Font.BOLD, 16);
        myClubsWrap.add(myTitle, BorderLayout.NORTH);
        JPanel myList = new JPanel();
        myList.setOpaque(false);
        myList.setLayout(new BoxLayout(myList, BoxLayout.Y_AXIS));
        // Populated from 'myClubs' list
        for (Club c : myClubs) {
            RoundedPanel row = new RoundedPanel(8, pageBg);
            row.setLayout(new BorderLayout());
            row.setBorder(new EmptyBorder(10,10,10,10));
            // Simplified label to show only club name
            JLabel lab = new JLabel("<html><b>" + c.name + "</b></html>");
            applyAppFont(lab, Font.PLAIN, 13);
            row.add(lab, BorderLayout.CENTER);
            row.addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ row.setHighlight(true); row.repaint(); }
                @Override public void mouseExited(MouseEvent e){ row.setHighlight(false); row.repaint(); }
                @Override public void mouseClicked(MouseEvent e){ openMyClubPopup(c); }
            });
            myList.add(row);
            myList.add(Box.createRigidArea(new Dimension(0,8)));
        }
        JScrollPane myScroll = new JScrollPane(myList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        myScroll.getViewport().setBackground(pageBg);
        myScroll.setBorder(null);
        myClubsWrap.add(myScroll, BorderLayout.CENTER);
        panel.add(myClubsWrap, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(pageBg);
        panel.setBorder(new EmptyBorder(16,16,16,16));
        JLabel title = new JLabel("Events (Your Department)"); // Title clarified
        applyAppFont(title, Font.BOLD, 18);
        panel.add(title, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0,3,12,12));
        grid.setOpaque(false);
        // Populated from 'events' list (department specific)
        for (EventItem e : events) grid.add(createEventCardForList(e));
        JScrollPane scroll = new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(pageBg);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(pageBg);
        panel.setBorder(new EmptyBorder(16,16,16,16));
        JLabel title = new JLabel("Notifications (From Your Department's Events)"); // Title clarified
        applyAppFont(title, Font.BOLD, 18);
        panel.add(title, BorderLayout.NORTH);
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(12,0,12,0));
        if (notifications.isEmpty()) {
            JLabel none = new JLabel("No Notifications");
            applyAppFont(none, Font.PLAIN, 14);
            none.setForeground(Color.BLACK); // Changed from GRAY
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
            wrap.setOpaque(false);
            wrap.add(none);
            list.add(wrap);
        } else {
            for (NotificationItem n : notifications) {
                list.add(createNotificationRowWithView(n));
                list.add(Box.createRigidArea(new Dimension(0,8)));
            }
        }
        JScrollPane scroll = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(pageBg);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private RoundedPanel createStatCard(String big, String small, Color color) {
        RoundedPanel card = new RoundedPanel(10, pageBg);
        card.setPreferredSize(new Dimension(180, 70));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,14,10,14));
        JLabel bigLbl = new JLabel(big);
        applyAppFont(bigLbl, Font.BOLD, 22);
        bigLbl.setForeground(color);
        JLabel lbl = new JLabel(small);
        applyAppFont(lbl, Font.PLAIN, 12);
        lbl.setForeground(Color.BLACK); // Changed from GRAY
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(bigLbl, BorderLayout.NORTH);
        left.add(lbl, BorderLayout.SOUTH);
        card.add(left, BorderLayout.WEST);
        return card;
    }

    private RoundedPanel createStatCardClickable(String big, String small, Color color, Runnable onClick) {
        RoundedPanel card = new RoundedPanel(10, pageBg);
        card.setPreferredSize(new Dimension(200, 70));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,14,10,14));
        JLabel bigLbl = new JLabel(big);
        applyAppFont(bigLbl, Font.BOLD, 22);
        bigLbl.setForeground(color);
        JLabel lbl = new JLabel(small);
        applyAppFont(lbl, Font.PLAIN, 12);
        lbl.setForeground(Color.BLACK); // Changed from GRAY
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(bigLbl, BorderLayout.NORTH);
        left.add(lbl, BorderLayout.SOUTH);
        card.add(left, BorderLayout.WEST);
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            @Override public void mouseEntered(MouseEvent e) { card.setHighlight(true); card.repaint(); }
            @Override public void mouseExited(MouseEvent e) { card.setHighlight(false); card.repaint(); }
        });
        return card;
    }

    private JPanel createMinimalClubCard(Club c) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12,12,12,12));
        card.setPreferredSize(new Dimension(200,120));
        JLabel t = new JLabel("<html><b>" + c.name + "</b></html>");
        applyAppFont(t, Font.BOLD, 13);
        JTextArea desc = new JTextArea(c.description); // Changed from shortDesc
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setOpaque(false);
        desc.setEditable(false);
        applyAppFont(desc, Font.PLAIN, 12);
        card.add(t, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){ openClubPopup(c); }
            @Override public void mouseEntered(MouseEvent e){ card.setHighlight(true); card.repaint(); }
            @Override public void mouseExited(MouseEvent e){ card.setHighlight(false); card.repaint(); }
        });
        return card;
    }

    private JPanel createClubCardForList(Club c) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,10,10,10));
        card.setPreferredSize(new Dimension(200, 80)); // Reduced height
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + c.name + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        // Removed description
        card.add(title, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){ openClubPopup(c); }
            @Override public void mouseEntered(MouseEvent e){ card.setHighlight(true); card.repaint(); }
            @Override public void mouseExited(MouseEvent e){ card.setHighlight(false); card.repaint(); }
        });
        return card;
    }

    private JPanel createEventCardForList(EventItem e) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10,10,10,10));
        card.setPreferredSize(new Dimension(200, 80)); // Same dimensions as club card
        JLabel title = new JLabel("<html><b style='font-size:14px;'>" + e.title + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        // Removed other info
        card.add(title, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent ev){ openEventPopup(e); }
            @Override public void mouseEntered(MouseEvent ev){ card.setHighlight(true); card.repaint(); }
            @Override public void mouseExited(MouseEvent ev){ card.setHighlight(false); card.repaint(); }
        });
        return card;
    }

    private JPanel createUpcomingEventRow(EventItem ev) {
        RoundedPanel row = new RoundedPanel(8, Color.WHITE);
        row.setLayout(new BorderLayout());
        row.setBorder(new EmptyBorder(12,12,12,12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        JLabel title = new JLabel("<html><b>" + ev.title + "</b></html>");
        applyAppFont(title, Font.PLAIN, 13);
        // Changed to show only date
        JLabel meta = new JLabel(ev.date);
        applyAppFont(meta, Font.PLAIN, 12);
        meta.setForeground(Color.BLACK); // Changed from GRAY
        row.add(title, BorderLayout.WEST);
        row.add(meta, BorderLayout.EAST);
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e){ openEventPopup(ev); }
            @Override public void mouseEntered(MouseEvent e){ row.setHighlight(true); row.repaint(); }
            @Override public void mouseExited(MouseEvent e){ row.setHighlight(false); row.repaint(); }
        });
        return row;
    }

    private JPanel createNotificationRowWithView(NotificationItem n) {
        RoundedPanel row = new RoundedPanel(8, Color.WHITE);
        row.setLayout(new BorderLayout());
        row.setBorder(new EmptyBorder(12,12,12,12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        JLabel title = new JLabel("<html><b>" + n.club + " - " + n.eventName + "</b></html>");
        applyAppFont(title, Font.BOLD, 14);
        JLabel meta = new JLabel(n.venue + " | " + n.date + " | " + n.time);
        applyAppFont(meta, Font.PLAIN, 12);
        meta.setForeground(Color.BLACK); // Changed from GRAY
        JButton view = new JButton("View");
        makeButtonTransparent(view);
        applyAppFont(view, Font.PLAIN, 13);
        view.addActionListener(e -> {
            EventItem match = null;
            for (EventItem ev : events) if (ev.title.equalsIgnoreCase(n.eventName) || ev.clubName.equalsIgnoreCase(n.club)) { match = ev; break; }
            if (match != null) openEventPopup(match); else {
                JPanel p = new JPanel(new BorderLayout());
                p.setOpaque(false);
                JLabel heading = new JLabel("<html><b>" + n.eventName + "</b></html>");
                applyAppFont(heading, Font.BOLD, 14);
                p.add(heading, BorderLayout.NORTH);
                JLabel info = new JLabel(n.club + " | " + n.venue + " | " + n.date + " | " + n.time);
                applyAppFont(info, Font.PLAIN, 13);
                p.add(info, BorderLayout.CENTER);
                showPopup(p, false);
            }
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(view);
        row.add(title, BorderLayout.NORTH);
        row.add(meta, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e){ row.setHighlight(true); row.repaint(); }
            @Override public void mouseExited(MouseEvent e){ row.setHighlight(false); row.repaint(); }
        });
        return row;
    }

    /**
     * Creates the glass pane panel and sets it on the JFrame.
     */
    private void createGlassPane() {
        glass = new JPanel(null);
        glass.setOpaque(true);
        glass.setBackground(new Color(0,0,0,100)); // Dims background
        glass.setVisible(false);
        glass.addMouseListener(new MouseAdapter(){});
        setGlassPane(glass); // Re-added for JFrame
    }

    // Helper method to resize the popup
    private void resizePopup(boolean maximizedLike) {
        if (popupPanel == null) return;
        Dimension size = getContentPane().getSize(); // Reverted to getContentPane()
        if (maximizedLike) {
            popupPanel.setSize((int)(size.width*0.96), (int)(size.height*0.92));
        } else {
            popupPanel.setSize(720,420); // Default smaller size
        }
        int x = (size.width - popupPanel.getWidth())/2;
        int y = (size.height - popupPanel.getHeight())/2;
        popupPanel.setLocation(x,y);
        glass.revalidate();
        glass.repaint();
    }


    private void showPopup(JComponent content, boolean maximizedLike) {
        glass.removeAll();
        popupPanel = new RoundedPanel(12, Color.WHITE);
        popupPanel.setLayout(new BorderLayout());
        popupPanel.setBorder(new EmptyBorder(16,16,16,16));
        
        popupPanel.add(content, BorderLayout.CENTER);
        glass.setLayout(null);
        glass.add(popupPanel);
        
        resizePopup(maximizedLike); // Use the resize method

        glass.setVisible(true);
        glass.revalidate();
        glass.repaint();
    }

    private void openClubPopup(Club c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("<html><div style='font-size:18px;'><b>" + c.name + "</b></div></html>");
        applyAppFont(title, Font.BOLD, 16);
        p.add(title, BorderLayout.NORTH);
        // Changed to use c.description and remove location
        JTextArea desc = new JTextArea(c.description + "\n\nLeaders: " + c.leadersString());
        desc.setEditable(false); desc.setLineWrap(true); desc.setWrapStyleWord(true);
        desc.setOpaque(false);
        applyAppFont(desc, Font.PLAIN, 13);
        p.add(desc, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,16,8));
        btns.setOpaque(false);
        JButton back = new JButton("Back");
        JButton join = new JButton("Join");
        makeButtonTransparent(back); makeButtonTransparent(join);
        back.setPreferredSize(new Dimension(88,38)); join.setPreferredSize(new Dimension(88,38));
        applyAppFont(back, Font.BOLD, 13); applyAppFont(join, Font.BOLD, 13);
        back.addActionListener(e -> closePopup());
        join.addActionListener(e -> {
            if (dbConn != null) {
                // Updated SQL to insert into my_clubs with nid and check for duplicates
                String sql = "INSERT INTO my_clubs(nid, club_name) " +
                             "SELECT ?, ? WHERE NOT EXISTS " +
                             "(SELECT 1 FROM my_clubs WHERE nid = ? AND club_name = ?)";
                try (PreparedStatement ps = dbConn.prepareStatement(sql)) {
                    ps.setString(1, this.nid);
                    ps.setString(2, c.name);
                    ps.setString(3, this.nid);
                    ps.setString(4, c.name);
                    int rowsAffected = ps.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        myClubs.add(c); // Add to local list
                        joinedClubsCount = myClubs.size();
                        updateDashboardCounts(); // Refresh UI
                        showStatusMessage("Joined " + c.name, false);
                    } else {
                        showStatusMessage("You have already joined this club.", true);
                    }
                } catch (SQLException ex) {
                    showStatusMessage("DB error joining club.", true);
                }
            } else {
                // Fallback if DB is offline (though not ideal)
                myClubs.add(c);
                joinedClubsCount = myClubs.size();
                updateDashboardCounts();
                showStatusMessage("Joined " + c.name, false); // Use status label
            }
            closePopup();
        });
        btns.add(back); btns.add(join);
        p.add(btns, BorderLayout.SOUTH);
        showPopup(p, false);
    }

    private void openEventPopup(EventItem ev) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("<html><div style='font-size:18px;'><b>" + ev.title + "</b></div></html>");
        applyAppFont(title, Font.BOLD, 16);
        p.add(title, BorderLayout.NORTH);
        JTextArea details = new JTextArea("Organized by: " + ev.clubName + "\nLeaders: " + ev.leadersString() + "\nVenue: " + ev.venue + "\nDate: " + ev.date + "\nTime: " + ev.time);
        details.setEditable(false); details.setLineWrap(true); details.setWrapStyleWord(true); details.setOpaque(false);
        applyAppFont(details, Font.PLAIN, 13);
        p.add(details, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,16,8));
        btns.setOpaque(false);
        JButton back = new JButton("Back");
        JButton apply = new JButton("Apply");
        makeButtonTransparent(back); makeButtonTransparent(apply);
        back.setPreferredSize(new Dimension(88,38)); apply.setPreferredSize(new Dimension(88,38));
        applyAppFont(back, Font.BOLD, 13); applyAppFont(apply, Font.BOLD, 13);
        back.addActionListener(e -> closePopup());
        apply.addActionListener(e -> {
            if (dbConn != null) {
                // Updated SQL to insert into my_events and check for duplicates
                String sql = "INSERT INTO my_events(nid, club_name, event_name, date, time) " +
                             "SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS " +
                             "(SELECT 1 FROM my_events WHERE nid = ? AND event_name = ?)";
                try (PreparedStatement ps = dbConn.prepareStatement(sql)) {
                    ps.setString(1, this.nid);
                    ps.setString(2, ev.clubName);
                    ps.setString(3, ev.title);
                    ps.setString(4, ev.date);
                    ps.setString(5, ev.time);
                    ps.setString(6, this.nid);
                    ps.setString(7, ev.title);
                    
                    int rowsAffected = ps.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        myAppliedEvents.add(ev); // Add to local list
                        pendingApplicationsCount = myAppliedEvents.size();
                        updateDashboardCounts(); // Refresh UI
                        showStatusMessage("Applied to " + ev.title, false);
                    } else {
                        showStatusMessage("Already applied to this event.", true);
                    }
                } catch (SQLException ex) {
                    showStatusMessage("DB error applying to event.", true);
                }
            } else {
                pendingApplicationsCount++;
                updateDashboardCounts();
                showStatusMessage("Applied to " + ev.title, false); // Use status label
            }
            closePopup();
        });
        btns.add(back); btns.add(apply);
        p.add(btns, BorderLayout.SOUTH);
        showPopup(p, false);
    }

    private void openMyClubPopup(Club c) {
        // This 'p' is the original club details panel
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("<html><div style='font-size:18px;'><b>" + c.name + "</b></div></html>");
        applyAppFont(title, Font.BOLD, 16);
        p.add(title, BorderLayout.NORTH);
        // Changed to use c.description
        JTextArea desc = new JTextArea(c.description + "\n\nLeaders: " + c.leadersString());
        desc.setEditable(false); desc.setLineWrap(true); desc.setWrapStyleWord(true); desc.setOpaque(false);
        applyAppFont(desc, Font.PLAIN, 13);
        p.add(desc, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,16,8));
        btns.setOpaque(false);
        JButton back = new JButton("Back");
        JButton showEvents = new JButton("Show Events");
        JButton leave = new JButton("Leave Club"); // Added Leave button
        
        makeButtonTransparent(back); 
        makeButtonTransparent(showEvents);
        makeButtonTransparent(leave);
        leave.setForeground(Color.RED.darker()); // Make leave button red
        
        back.setPreferredSize(new Dimension(88,38)); 
        showEvents.setPreferredSize(new Dimension(98,38));
        leave.setPreferredSize(new Dimension(98, 38));
        
        applyAppFont(back, Font.BOLD, 13); 
        applyAppFont(showEvents, Font.BOLD, 13);
        applyAppFont(leave, Font.BOLD, 13);
        
        back.addActionListener(e -> closePopup());
        
        leave.addActionListener(e -> {
            // Use JOptionPane relative to this panel
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to leave " + c.name + "?", "Confirm Leave", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dbConn != null) {
                    try (PreparedStatement ps = dbConn.prepareStatement("DELETE FROM my_clubs WHERE nid = ? AND club_name = ?")) {
                        ps.setString(1, this.nid);
                        ps.setString(2, c.name);
                        int rowsAffected = ps.executeUpdate();
                        if (rowsAffected > 0) {
                            myClubs.remove(c); // Remove from local list
                            joinedClubsCount = myClubs.size();
                            updateDashboardCounts(); // Refresh UI
                            showStatusMessage("Left " + c.name, false);
                            closePopup();
                        } else {
                            showStatusMessage("Could not leave club.", true);
                        }
                    } catch (SQLException ex) {
                        showStatusMessage("DB error leaving club.", true);
                    }
                }
            }
        });
        
        showEvents.addActionListener(e -> {
            // This 'wrapper' is the new event list panel
            JPanel wrapper = new JPanel(new BorderLayout()); 
            wrapper.setOpaque(false);
            JLabel h = new JLabel("<html><b>Events for " + c.name + "</b></html>");
            applyAppFont(h, Font.BOLD, 16);
            h.setBorder(new EmptyBorder(0,0,10,0));
            wrapper.add(h, BorderLayout.NORTH);

            JPanel list = new JPanel();
            list.setOpaque(false);
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            boolean found = false;
            for (EventItem ev : events) { // Check against department's events
                if (ev.clubName.equalsIgnoreCase(c.name)) {
                    JPanel row = new JPanel(new BorderLayout());
                    row.setOpaque(false);
                    JLabel t = new JLabel("<html><b>" + ev.title + "</b></html>");
                    applyAppFont(t, Font.BOLD, 14);
                    JLabel meta = new JLabel(ev.date + " | " + ev.time + " | " + ev.venue);
                    applyAppFont(meta, Font.PLAIN, 12);
                    meta.setForeground(Color.BLACK); // Changed from GRAY
                    row.add(t, BorderLayout.WEST); row.add(meta, BorderLayout.EAST);
                    list.add(row); list.add(Box.createRigidArea(new Dimension(0,8)));
                    found = true;
                }
            }
            if (!found) {
                JLabel none = new JLabel("No Events Added for this club");
                applyAppFont(none, Font.PLAIN, 14);
                none.setForeground(Color.BLACK); // Changed from GRAY
                JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT)); wrap.setOpaque(false); wrap.add(none);
                list.add(wrap);
            }
            JScrollPane sc = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            sc.getViewport().setBackground(pageBg); sc.setBorder(null);
            wrapper.add(sc, BorderLayout.CENTER);

            // Add a "Back" button to this event list panel
            JPanel eventBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
            eventBtns.setOpaque(false);
            JButton eventBack = new JButton("Back");
            makeButtonTransparent(eventBack);
            eventBack.setPreferredSize(new Dimension(88, 38));
            applyAppFont(eventBack, Font.BOLD, 13);
            eventBack.addActionListener(backEvent -> {
                // Replace popupPanel's content back to the original 'p'
                popupPanel.removeAll();
                popupPanel.add(p, BorderLayout.CENTER);
                resizePopup(true); // Restore to maximized
                popupPanel.revalidate();
                popupPanel.repaint();
            });
            eventBtns.add(eventBack);
wrapper.add(eventBtns, BorderLayout.SOUTH);

            // Replace the popup's content with the new 'wrapper'
            popupPanel.removeAll();
            popupPanel.add(wrapper, BorderLayout.CENTER);
            resizePopup(false); // Make it smaller
            popupPanel.revalidate();
            popupPanel.repaint();
        });
        btns.add(back); 
        btns.add(showEvents);
        btns.add(leave); // Add the leave button
        p.add(btns, BorderLayout.SOUTH);
        showPopup(p, true); // Show the initial club popup as maximized
    }

    private void closePopup() {
        if (glass != null) {
            glass.setVisible(false);
            glass.removeAll();
            repaint();
        }
    }

    private void updateDashboardCounts() {
        // Reload data from DB to get fresh counts and lists
        loadDataFromDB(); 
        
        String visible = getCurrentVisibleCard();
        contentCards.removeAll();
        contentCards.add(createDashboardPanel(), "Dashboard");
        contentCards.add(createBrowseClubsPanel(), "Browse Clubs");
        contentCards.add(createEventsPanel(), "Events");
        contentCards.add(createNotificationsPanel(), "Notifications");
        cardLayout.show(contentCards, visible);
    }

    private String getCurrentVisibleCard() {
        int y = indicator.getY();
        int gap = 6;
        int yBase = 30;
        int itemHeight = 44 + gap;
        int idx = Math.max(0, Math.min(navButtons.length-1, (y - yBase + itemHeight/2) / itemHeight));
        return navNames[idx];
    }

    private void makeButtonTransparent(AbstractButton b) {
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(null);
        b.setForeground(accentBlue);
        b.addMouseListener(new MouseAdapter(){ 
            @Override public void mouseEntered(MouseEvent e){ b.setForeground(new Color(20,70,200)); } 
            @Override public void mouseExited(MouseEvent e){ b.setForeground(accentBlue); }
        });
    }

    private void applyAppFont(Component c, int style, int size) {
        Font f = findAppFont(style,size);
        if (f != null) c.setFont(f);
    }

    private Font findAppFont(int style, int size) {
        try {
            Font f = new Font(PREFERRED_FONT, style, size);
            if (!f.getFamily().equalsIgnoreCase(PREFERRED_FONT) && !f.getName().equalsIgnoreCase(PREFERRED_FONT)) f = new Font("SansSerif", style, size);
            return f;
        } catch (Exception e) {
            return new Font("SansSerif", style, size);
        }
    }

    private class RoundedPanel extends JPanel {
        private int radius; private Color bg; private boolean highlight = false;
        public RoundedPanel(int radius, Color bg) { super(); this.radius = radius; this.bg = bg; setOpaque(false); }
        public void setHighlight(boolean h) { highlight = h; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,radius,radius));
            if (highlight) { 
                g2.setColor(new Color(120,160,255)); g2.setStroke(new BasicStroke(2f)); 
            } else { 
                g2.setColor(cardBorder); // Use the class-level cardBorder color
                g2.setStroke(new BasicStroke(1f)); 
            }
            g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-3,getHeight()-3,radius,radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class LineRoundedBorder extends javax.swing.border.AbstractBorder {
        private int r; private Color color; private int thickness;
        public LineRoundedBorder(int r, Color color, int thickness) { this.r=r; this.color=color; this.thickness=thickness; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i=0;i<thickness;i++) g2.drawRoundRect(x+i,y+i,width-1-2*i,height-1-2*i,r,r);
            g2.dispose();
        }
    }

    private class SidebarHoverAdapter extends MouseAdapter {
        private final JButton btn; 
        private Color normal = pageBg; // Changed from grey
        private Color hover = new Color(238,243,255);
        public SidebarHoverAdapter(JButton b) { btn = b; }
        @Override public void mouseEntered(MouseEvent e){ if (btn.getBackground().equals(normal)) btn.setBackground(hover); }
        @Override public void mouseExited(MouseEvent e){ Color sel = Color.WHITE; if (!btn.getBackground().equals(sel)) btn.setBackground(normal); } // Changed from light blue
    }
    
    /**
     * ADDED BACK: Static show method to be called from LoginRegisterUI.
     * This method creates and shows the dashboard as a new, maximized JFrame.
     * @param parent The parent frame (can be null, but good practice to pass)
     * @param nid The student's NID (e.g., "STUABC123")
     */
    public static void show(JFrame parent, String nid) {
        if (parent != null) {
            parent.setVisible(false); // Hide the parent login window
        }
        try { 
            FlatLightLaf.setup(); 
        } catch (Exception ex) { 
            System.err.println("FlatLaf not found; add JAR to classpath."); 
        }
        
        SwingUtilities.invokeLater(() -> {
            CopyOfStudentDashboard5 dashboard = new CopyOfStudentDashboard5(nid);
            // Add a listener to show the parent window again when this one is closed
            dashboard.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (parent != null) {
                        parent.setVisible(true); // Re-show the login window
                    }
                }
            });
            dashboard.setVisible(true);
        });
    }

    // --- Updated Inner Data Classes ---

    /**
     * Represents a Club. Updated to match new DB schema.
     */
    private class Club { 
        int id; // Club's primary key from 'clubs' table
        String name, description; 
        String[] leaders; 
        
        Club(int id, String name, String description, String[] leaders) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.leaders = leaders;
        } 
        
        String leadersString() { 
            if (leaders==null||leaders.length==0) return "N/A"; 
            StringBuilder sb=new StringBuilder(); 
            for (int i=0;i<leaders.length;i++){ 
                sb.append(leaders[i]); 
                if (i<leaders.length-1) sb.append(", "); 
            } 
            return sb.toString(); 
        } 
    }
    
    /**
     * Represents an Event. This class was already compatible.
     */
    private class EventItem { 
        String title, clubName, venue, date, time; 
        String[] leaders; 
        
        EventItem(String t,String c,String[] l,String v,String d,String tm){
            title=t;clubName=c;leaders=l;venue=v;date=d;time=tm;
        } 
        
        String leadersString() { 
            if (leaders==null||leaders.length==0) return "N/A"; 
            StringBuilder sb=new StringBuilder(); 
            for (int i=0;i<leaders.length;i++){ 
                sb.append(leaders[i]); 
                if (i<leaders.length-1) sb.append(", "); 
            } 
            return sb.toString(); 
        } 
    }
    
    /**
     * Represents a Notification. This class was already compatible.
     */
    private class NotificationItem { 
        String club,eventName,venue,date,time; 
        NotificationItem(String a,String b,String c,String d,String e){
            club=a;eventName=b;venue=c;date=d;time=e;
        } 
    }
}
class Logout {} // This class is no longer used but safe to keep

