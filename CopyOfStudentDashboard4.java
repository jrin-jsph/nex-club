// CopyOfStudentDashboard4.java
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

public class CopyOfStudentDashboard4 extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentCards;
    private JButton[] navButtons;
    private String[] navNames = {"Dashboard", "Browse Clubs", "Events", "Notifications"};
    private Color accentBlue = new Color(40, 104, 255);
    private Color pageBg = Color.WHITE;
    private Color cardBorder = new Color(220, 220, 220);
    private int sidebarWidth = 220;
    private final String PREFERRED_FONT = "Microsoft JhengHei";
    private JPanel indicator;
    private int indicatorTargetY = 0;
    private Timer indicatorTimer;
    private JPanel glass;
    private JPanel popupPanel;
    private List<Club> clubs = new ArrayList<>();
    private List<EventItem> events = new ArrayList<>();
    private List<NotificationItem> notifications = new ArrayList<>();
    private List<Club> myClubs = new ArrayList<>();
    private int joinedClubsCount = 0;
    private int eventsThisMonthCount = 0;
    private int pendingApplicationsCount = 0;
    private Connection dbConn = null;

    // Status label for non-blocking messages
    private JLabel statusLabel;
    private Timer statusTimer;

    public CopyOfStudentDashboard4() {
        setTitle("Student Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Window opens maximized
        setLayout(new BorderLayout());
        getContentPane().setBackground(pageBg);
        
        // UI must be initialized before showing status messages
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        connectDatabase();
        if (dbConn == null) {
            showStatusMessage("DB connection failed. Using static data.", true);
        }
        
        prepareTables();
        loadDataFromDB();
        
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
            st.execute("CREATE TABLE IF NOT EXISTS clubs (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), short_desc TEXT, leaders TEXT, location VARCHAR(255), next_event VARCHAR(255))");
            st.execute("CREATE TABLE IF NOT EXISTS events (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255), club_name VARCHAR(255), leaders TEXT, venue VARCHAR(255), date VARCHAR(50), time VARCHAR(50))");
            st.execute("CREATE TABLE IF NOT EXISTS notifications (id INT AUTO_INCREMENT PRIMARY KEY, club VARCHAR(255), event_name VARCHAR(255), venue VARCHAR(255), date VARCHAR(50), time VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS my_clubs (id INT AUTO_INCREMENT PRIMARY KEY, club_id INT, joined_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException ignored) {
            showStatusMessage("Failed to prepare DB tables.", true);
        }
    }

    private void loadDataFromDB() {
        clubs.clear(); events.clear(); notifications.clear(); myClubs.clear();
        if (dbConn == null) {
            loadStaticDefaults();
            return;
        }
        try {
            try (Statement st = dbConn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT * FROM clubs");
                while (rs.next()) {
                    String name = rs.getString("name");
                    String shortDesc = rs.getString("short_desc");
                    String leaders = rs.getString("leaders");
                    String loc = rs.getString("location");
                    String next = rs.getString("next_event");
                    clubs.add(new Club(name, shortDesc, split(leaders), loc, next));
                }
                rs = st.executeQuery("SELECT * FROM events");
                while (rs.next()) {
                    String title = rs.getString("title");
                    String clubName = rs.getString("club_name");
                    String leaders = rs.getString("leaders");
                    String venue = rs.getString("venue");
                    String date = rs.getString("date");
                    String time = rs.getString("time");
                    events.add(new EventItem(title, clubName, split(leaders), venue, date, time));
                }
                rs = st.executeQuery("SELECT n.club, n.event_name, n.venue, n.date, n.time FROM notifications n ORDER BY n.created_at DESC");
                while (rs.next()) {
                    notifications.add(new NotificationItem(rs.getString("club"), rs.getString("event_name"), rs.getString("venue"), rs.getString("date"), rs.getString("time")));
                }
                rs = st.executeQuery("SELECT c.* FROM clubs c JOIN my_clubs m ON c.id = m.club_id");
                while (rs.next()) {
                    myClubs.add(new Club(rs.getString("name"), rs.getString("short_desc"), split(rs.getString("leaders")), rs.getString("location"), rs.getString("next_event")));
                }
            }
        } catch (SQLException e) {
            loadStaticDefaults();
            showStatusMessage("Failed to load data. Using static defaults.", true);
        }
        joinedClubsCount = myClubs.size();
        eventsThisMonthCount = events.size();
    }

    private String[] split(String s) {
        if (s == null || s.trim().isEmpty()) return new String[0];
        return s.split(",");
    }

    private void loadStaticDefaults() {
        clubs.add(new Club("Computer Science Society", "Learn programming, participate in hackathons.", new String[]{"Alice Kumar","Ravi Patel"}, "Tech Building", "Mar 15"));
        clubs.add(new Club("Drama Club", "Express creativity through theatre & workshops.", new String[]{"Maya Joseph"}, "Arts Center", "Mar 10"));
        clubs.add(new Club("Environmental Action Group", "Promote sustainability and conservation initiatives.", new String[]{"Samir Rao","Nina Roy"}, "Student Center", "Mar 20"));
        clubs.add(new Club("Photography Club", "Workshops, field trips and portfolio building.", new String[]{"Priya Menon"}, "Media Lab", "Mar 18"));
        clubs.add(new Club("Robotics Club", "Robotics projects, competitions and tutorials.", new String[]{"Arjun Singh"}, "Robotics Lab", "Mar 22"));
        clubs.add(new Club("Literature Circle", "Book discussions, writing workshops, readings.", new String[]{"Leena Das"}, "Library Hall", "Mar 25"));

        events.add(new EventItem("CS Society Hackathon", "Computer Science Society", new String[]{"Alice Kumar"}, "Tech Building", "2025-03-15", "09:00 AM"));
        events.add(new EventItem("Drama Auditions", "Drama Club", new String[]{"Maya Joseph"}, "Arts Center", "2025-03-10", "02:00 PM"));
        events.add(new EventItem("Photography Workshop", "Photography Club", new String[]{"Priya Menon"}, "Media Lab", "2025-03-12", "04:00 PM"));
        events.add(new EventItem("Robotics Bot Build", "Robotics Club", new String[]{"Arjun Singh"}, "Robotics Lab", "2025-03-22", "10:00 AM"));
        events.add(new EventItem("Environmental Cleanup", "Environmental Action Group", new String[]{"Samir Rao"}, "Student Center", "2025-03-20", "08:00 AM"));
        events.add(new EventItem("Poetry Night", "Literature Circle", new String[]{"Leena Das"}, "Library Hall", "2025-03-25", "06:30 PM"));

        notifications.add(new NotificationItem("Drama Club","Pop-up Play Rehearsal","Arts Center","2025-03-09","10:00 AM"));
        notifications.add(new NotificationItem("Photography Club","Sprint Photo Walk","Campus Grounds","2025-03-11","07:00 AM"));
        notifications.add(new NotificationItem("Robotics Club","Unexpected Workshop","Robotics Lab","2025-03-12","11:00 AM"));
        notifications.add(new NotificationItem("CS Society","Lightning Talk","Tech Building","2025-03-13","03:00 PM"));
        notifications.add(new NotificationItem("Env Action","Emergency Cleanup","Riverside","2025-03-14","06:00 AM"));

        myClubs.add(clubs.get(0));
        myClubs.add(clubs.get(1));
        myClubs.add(clubs.get(2));

        joinedClubsCount = myClubs.size();
        eventsThisMonthCount = events.size();
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel(new BorderLayout()); // Use BorderLayout
        side.setBackground(new Color(248,249,251));
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
        topContent.add(navPanel); // Add navPanel to topContent
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
        indicator.setLocation(0, 30); // Location relative to topContent
        topContent.add(indicator); // Add indicator to topContent
        
        side.add(topContent, BorderLayout.CENTER); // Add topContent to CENTER

        JButton logout = new JButton("Logout");
        makeButtonTransparent(logout); // Use the helper method
        applyAppFont(logout, Font.BOLD, 13);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10, 2, 10, 2)); // Add padding
        logout.addActionListener(e -> {
            new Logout();
            showStatusMessage("Logout placeholder instantiated.", false); // Use status label
        });
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
        JLabel dashboardLabel = new JLabel("Dashboard");
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

        JLabel name = new JLabel("John Doe");
        applyAppFont(name, Font.PLAIN, 13);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        right.add(name, gbc);
        
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
        JLabel welcome = new JLabel("<html><div style='font-size:20px;'><b>Welcome Back, John!</b></div><div style='color:#6b7280;'>Discover new opportunities and stay connected with your clubs.</div></html>");
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
        for (int i = 0; i < Math.min(6, clubs.size()); i++) gridPanel.add(createMinimalClubCard(clubs.get(i)));
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
        JLabel upTitle = new JLabel("Upcoming Events");
        applyAppFont(upTitle, Font.BOLD, 14);
        upCard.add(upTitle, BorderLayout.NORTH);
        JPanel evList = new JPanel();
        evList.setOpaque(false);
        evList.setLayout(new BoxLayout(evList, BoxLayout.Y_AXIS));
        for (EventItem ev : events) {
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
        JLabel title = new JLabel("Browse Clubs");
        applyAppFont(title, Font.BOLD, 18);
        panel.add(title, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0,3,12,12));
        grid.setOpaque(false);
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
        JLabel title = new JLabel("Events");
        applyAppFont(title, Font.BOLD, 18);
        panel.add(title, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0,3,12,12));
        grid.setOpaque(false);
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
        JLabel title = new JLabel("Notifications");
        applyAppFont(title, Font.BOLD, 18);
        panel.add(title, BorderLayout.NORTH);
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(12,0,12,0));
        if (notifications.isEmpty()) {
            JLabel none = new JLabel("No Notifications");
            applyAppFont(none, Font.PLAIN, 14);
            none.setForeground(Color.GRAY);
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
        lbl.setForeground(Color.GRAY);
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
        lbl.setForeground(Color.GRAY);
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
        JTextArea desc = new JTextArea(c.shortDesc);
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
        meta.setForeground(Color.GRAY);
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
        meta.setForeground(Color.GRAY);
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

    private void createGlassPane() {
        glass = new JPanel(null);
        glass.setOpaque(true);
        glass.setBackground(new Color(0,0,0,100)); // Dims background
        glass.setVisible(false);
        glass.addMouseListener(new MouseAdapter(){});
        setGlassPane(glass);
    }

    // Helper method to resize the popup
    private void resizePopup(boolean maximizedLike) {
        if (popupPanel == null) return;
        if (maximizedLike) {
            Dimension size = getContentPane().getSize();
            popupPanel.setSize((int)(size.width*0.96), (int)(size.height*0.92));
        } else {
            popupPanel.setSize(720,420); // Default smaller size
        }
        Dimension size = getContentPane().getSize();
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
        JTextArea desc = new JTextArea(c.shortDesc + "\n\nLeaders: " + c.leadersString() + "\nVenue: " + c.location);
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
                try (PreparedStatement ps = dbConn.prepareStatement("INSERT INTO my_clubs(club_id) VALUES((SELECT id FROM clubs WHERE name=? LIMIT 1))")) {
                    ps.setString(1,c.name); ps.executeUpdate();
                } catch (SQLException ignored) {
                    showStatusMessage("DB error joining club.", true);
                }
            }
            myClubs.add(c);
            joinedClubsCount = myClubs.size();
            updateDashboardCounts();
            showStatusMessage("Joined " + c.name, false); // Use status label
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
            pendingApplicationsCount++;
            if (dbConn != null) {
                try (PreparedStatement ps = dbConn.prepareStatement("INSERT INTO events(title, club_name, leaders, venue, date, time) VALUES(?,?,?,?,?,?)")) {
                    ps.setString(1, ev.title); ps.setString(2, ev.clubName); ps.setString(3, String.join(",", ev.leaders)); ps.setString(4, ev.venue); ps.setString(5, ev.date); ps.setString(6, ev.time);
                    ps.executeUpdate();
                } catch (SQLException ignored) {
                    showStatusMessage("DB error applying to event.", true);
                }
            }
            updateDashboardCounts();
            showStatusMessage("Applied to " + ev.title, false); // Use status label
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
        JTextArea desc = new JTextArea(c.shortDesc + "\n\nLeaders: " + c.leadersString() + "\nVenue: " + c.location);
        desc.setEditable(false); desc.setLineWrap(true); desc.setWrapStyleWord(true); desc.setOpaque(false);
        applyAppFont(desc, Font.PLAIN, 13);
        p.add(desc, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,16,8));
        btns.setOpaque(false);
        JButton back = new JButton("Back");
        JButton showEvents = new JButton("Show Events");
        makeButtonTransparent(back); makeButtonTransparent(showEvents);
        back.setPreferredSize(new Dimension(88,38)); showEvents.setPreferredSize(new Dimension(98,38));
        applyAppFont(back, Font.BOLD, 13); applyAppFont(showEvents, Font.BOLD, 13);
        back.addActionListener(e -> closePopup());
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
            for (EventItem ev : events) {
                if (ev.clubName.equalsIgnoreCase(c.name)) {
                    JPanel row = new JPanel(new BorderLayout());
                    row.setOpaque(false);
                    JLabel t = new JLabel("<html><b>" + ev.title + "</b></html>");
                    applyAppFont(t, Font.BOLD, 14);
                    JLabel meta = new JLabel(ev.date + " | " + ev.time + " | " + ev.venue);
                    applyAppFont(meta, Font.PLAIN, 12);
                    meta.setForeground(Color.GRAY);
                    row.add(t, BorderLayout.WEST); row.add(meta, BorderLayout.EAST);
                    list.add(row); list.add(Box.createRigidArea(new Dimension(0,8)));
                    found = true;
                }
            }
            if (!found) {
                JLabel none = new JLabel("No Events Added for this club");
                applyAppFont(none, Font.PLAIN, 14);
                none.setForeground(Color.GRAY);
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
        btns.add(back); btns.add(showEvents);
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
        b.addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){ b.setForeground(new Color(20,70,200)); } @Override public void mouseExited(MouseEvent e){ b.setForeground(accentBlue); }});
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
            if (highlight) { g2.setColor(new Color(120,160,255)); g2.setStroke(new BasicStroke(2f)); } else { g2.setColor(new Color(220,220,220)); g2.setStroke(new BasicStroke(1f)); }
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
        private final JButton btn; private Color normal = new Color(248,249,251); private Color hover = new Color(238,243,255);
        public SidebarHoverAdapter(JButton b) { btn = b; }
        @Override public void mouseEntered(MouseEvent e){ if (btn.getBackground().equals(normal)) btn.setBackground(hover); }
        @Override public void mouseExited(MouseEvent e){ Color sel = new Color(233,241,255); if (!btn.getBackground().equals(sel)) btn.setBackground(normal); }
    }

    public static void show(JFrame parent) {
        try { FlatLightLaf.setup(); } catch (Exception ex) { System.err.println("FlatLaf not found; add JAR to classpath."); }
        SwingUtilities.invokeLater(() -> new CopyOfStudentDashboard4());
    }

    private class Club { String name, shortDesc, location, next; String[] leaders; Club(String a,String b,String[] c,String d,String e){name=a;shortDesc=b;leaders=c;location=d;next=e;} String leadersString(){ if (leaders==null||leaders.length==0) return ""; StringBuilder sb=new StringBuilder(); for (int i=0;i<leaders.length;i++){ sb.append(leaders[i]); if (i<leaders.length-1) sb.append(", "); } return sb.toString(); } }
    private class EventItem { String title, clubName, venue, date, time; String[] leaders; EventItem(String t,String c,String[] l,String v,String d,String tm){title=t;clubName=c;leaders=l;venue=v;date=d;time=tm;} String leadersString(){ if (leaders==null||leaders.length==0) return ""; StringBuilder sb=new StringBuilder(); for (int i=0;i<leaders.length;i++){ sb.append(leaders[i]); if (i<leaders.length-1) sb.append(", "); } return sb.toString(); } }
    private class NotificationItem { String club,eventName,venue,date,time; NotificationItem(String a,String b,String c,String d,String e){club=a;eventName=b;venue=c;date=d;time=e;} }
}
class Logout {}
