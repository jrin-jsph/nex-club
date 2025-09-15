import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Arrays;

public class ClubsInfoUI extends JFrame {
    public ClubsInfoUI() {
        setTitle("Available Clubs - Nexclub");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(28, 28, 28, 28));
        mainPanel.setBackground(new Color(0xF8F9FB));

        // Stylish Title
        JLabel title = new JLabel("All Available Clubs at MITS ", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(0x3366FF));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel clubsPanel = new JPanel();
        clubsPanel.setLayout(new BoxLayout(clubsPanel, BoxLayout.Y_AXIS));
        clubsPanel.setOpaque(false);

        // Club data: {Club Name, Category, Description}
        List<String[]> clubs = Arrays.asList(
            new String[]{
                "Google Developer Student Club (GDSC)", "Tech & Innovation",
                "<b>Faculty Advisor:</b> Prof. Anjali Menon<br>" +
                "<b>About:</b> Empowers students with the latest technology trends, offering workshops and resources for technical skill development.<br>" +
                "<b>Activities:</b> Google tech talks, Android Study Jams, Solution Challenge, peer-to-peer learning, and hands-on coding sessions.<br>" +
                "<b>Contact:</b> gdsc@mitskochi.ac.in"
            },
            new String[]{
                "FOSS Club", "Tech & Innovation",
                "<b>Faculty Advisor:</b> Mr. Rajeev Kumar<br>" +
                "<b>About:</b> Hands-on experience with Free and Open Source Software tools for software development and collaboration.<br>" +
                "<b>Activities:</b> Linux install fests, open source contribution drives, FOSS workshops, and hackathons.<br>" +
                "<b>Contact:</b> fossclub@mitskochi.ac.in"
            },
            new String[]{
                "Computational Intelligence Club / AI & DS", "Tech & Innovation",
                "<b>Faculty Advisor:</b> Dr. Priya Suresh<br>" +
                "<b>About:</b> Focuses on Artificial Intelligence and Data Science, hosting workshops and events in these domains.<br>" +
                "<b>Activities:</b> AI/ML bootcamps, Kaggle competitions, guest lectures, and research paper discussions.<br>" +
                "<b>Contact:</b> aiclub@mitskochi.ac.in"
            },
            new String[]{
                "Tinker Hub", "Tech & Innovation",
                "<b>Faculty Advisor:</b> Ms. Neethu Thomas<br>" +
                "<b>About:</b> A hub for innovation and hands-on projects, providing a space for students to create and experiment.<br>" +
                "<b>Activities:</b> Maker sessions, hardware hackathons, project showcases, and startup ideation events.<br>" +
                "<b>Contact:</b> tinkerhub@mitskochi.ac.in"
            },
            new String[]{
                "IEDC (Innovation and Entrepreneurship Development Centre)", "Tech & Innovation",
                "<b>Faculty Advisor:</b> Dr. Suresh Babu<br>" +
                "<b>About:</b> Promotes innovation and entrepreneurship among students by providing resources, mentorship, and opportunities to develop startup ideas.<br>" +
                "<b>Activities:</b> Startup bootcamps, business plan competitions, mentoring sessions, and industry visits.<br>" +
                "<b>Contact:</b> iedc@mitskochi.ac.in"
            },
            new String[]{
                "IEEE Student Branch", "Tech & Innovation",
                "<b>Faculty Advisor:</b> Dr. Lakshmi Nair<br>" +
                "<b>About:</b> A professional community for students interested in electrical, electronics, and computer engineering, offering technical events, seminars, and networking opportunities.<br>" +
                "<b>Activities:</b> IEEE Day, technical paper presentations, workshops, and student conferences.<br>" +
                "<b>Contact:</b> ieee@mitskochi.ac.in"
            },
            new String[]{
                "Quiz Club", "Academic & Skill Development",
                "<b>Faculty Advisor:</b> Mr. Arun Mathew<br>" +
                "<b>About:</b> Organizes quizzes on various topics and hosts an annual inter-collegiate quiz competition.<br>" +
                "<b>Activities:</b> Weekly quizzes, quiz league, and inter-college quiz fests.<br>" +
                "<b>Contact:</b> quizclub@mitskochi.ac.in"
            },
            new String[]{
                "Math Club", "Academic & Skill Development",
                "<b>Faculty Advisor:</b> Dr. Radhika Menon<br>" +
                "<b>About:</b> Enhances mathematical skills and problem-solving through talks and engaging activities.<br>" +
                "<b>Activities:</b> Math Olympiads, puzzle days, and guest lectures.<br>" +
                "<b>Contact:</b> mathclub@mitskochi.ac.in"
            },
            new String[]{
                "Toastmasters Club", "Academic & Skill Development",
                "<b>Faculty Advisor:</b> Ms. Divya George<br>" +
                "<b>About:</b> Provides a structured environment for developing public speaking and leadership skills.<br>" +
                "<b>Activities:</b> Speech contests, table topics, and leadership workshops.<br>" +
                "<b>Contact:</b> toastmasters@mitskochi.ac.in"
            },
            new String[]{
                "Music Club", "Arts & Culture",
                "<b>Faculty Advisor:</b> Mr. Joseph Paul<br>" +
                "<b>About:</b> Promotes self-expression and community through musical events, including open mic evenings.<br>" +
                "<b>Activities:</b> Band performances, open mics, and music production workshops.<br>" +
                "<b>Contact:</b> musicclub@mitskochi.ac.in"
            },
            new String[]{
                "Media Club", "Arts & Culture",
                "<b>Faculty Advisor:</b> Ms. Sneha Ramesh<br>" +
                "<b>About:</b> Organizes creative events like the Under 25 Summit and Design Dojo, fostering media and design skills.<br>" +
                "<b>Activities:</b> Photography contests, video editing workshops, and event coverage.<br>" +
                "<b>Contact:</b> mediaclub@mitskochi.ac.in"
            },
            new String[]{
                "Arts Club", "Arts & Culture",
                "<b>Faculty Advisor:</b> Ms. Anu Varghese<br>" +
                "<b>About:</b> A platform for artistic expression and cultural activities within the campus.<br>" +
                "<b>Activities:</b> Art exhibitions, painting competitions, and cultural fests.<br>" +
                "<b>Contact:</b> artsclub@mitskochi.ac.in"
            },
            new String[]{
                "Rotaract Club", "Service & Environment",
                "<b>Faculty Advisor:</b> Mr. Sajan John<br>" +
                "<b>About:</b> Engages students in community service and leadership development initiatives.<br>" +
                "<b>Activities:</b> Blood donation camps, social outreach, and leadership seminars.<br>" +
                "<b>Contact:</b> rotaract@mitskochi.ac.in"
            },
            new String[]{
                "Eco Club", "Service & Environment",
                "<b>Faculty Advisor:</b> Ms. Meera Nair<br>" +
                "<b>About:</b> Promotes environmental awareness, tree planting, and sustainability efforts on campus.<br>" +
                "<b>Activities:</b> Clean-up drives, tree plantation, and eco-awareness campaigns.<br>" +
                "<b>Contact:</b> ecoclub@mitskochi.ac.in"
            },
            new String[]{
                "National Service Scheme (NSS) Unit 264", "Service & Environment",
                "<b>Faculty Advisor:</b> Mr. Ajith Kumar<br>" +
                "<b>About:</b> Leads community service projects, such as the 'THEJUS' old bulb recycling program.<br>" +
                "<b>Activities:</b> Village adoption, health camps, and social awareness programs.<br>" +
                "<b>Contact:</b> nss@mitskochi.ac.in"
            },
            new String[]{
                "Sports Club", "Sports & Fitness",
                "<b>Faculty Advisor:</b> Mr. Manoj Pillai<br>" +
                "<b>About:</b> Encourages participation in various sports and fitness activities, fostering teamwork and health.<br>" +
                "<b>Activities:</b> Annual sports meet, inter-department tournaments, and fitness workshops.<br>" +
                "<b>Contact:</b> sportsclub@mitskochi.ac.in"
            }
        );

        for (String[] club : clubs) {
            JPanel clubCard = new JPanel(new BorderLayout(10, 6)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            clubCard.setOpaque(false);
            clubCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 240), 1, true),
                new EmptyBorder(18, 22, 18, 22)
            ));
            clubCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            clubCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Make it look clickable

            JLabel clubName = new JLabel(club[0]);
            clubName.setFont(new Font("Segoe UI", Font.BOLD, 20));
            clubName.setForeground(new Color(0x22223B));

            JLabel clubCategory = new JLabel(club[1]);
            clubCategory.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            clubCategory.setForeground(new Color(0x3366FF));

            JLabel clubDesc = new JLabel("<html><body style='width:650px; font-family:Segoe UI;'>" + club[2] + "</body></html>");
            clubDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            clubDesc.setForeground(new Color(60, 60, 60));

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(clubName, BorderLayout.WEST);
            topPanel.add(clubCategory, BorderLayout.EAST);

            clubCard.add(topPanel, BorderLayout.NORTH);
            clubCard.add(clubDesc, BorderLayout.CENTER);

            // Add mouse listener to open details page
            clubCard.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    new ClubDetailsUI(club[0], club[1], club[2]).setVisible(true);
                }
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    clubCard.setBackground(new Color(0xE3EFFF));
                    clubCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    clubCard.setBackground(Color.WHITE);
                    clubCard.setCursor(Cursor.getDefaultCursor());
                }
            });

            clubsPanel.add(clubCard);
            clubsPanel.add(Box.createVerticalStrut(18));
        }

        JScrollPane scrollPane = new JScrollPane(clubsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(0xF8F9FB));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }
}