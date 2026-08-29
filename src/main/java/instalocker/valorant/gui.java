package instalocker.valorant;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import instalocker.utils.http.authorization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import instalocker.main;

public class gui {

    static final Color BG = new Color(0x0F1923);
    static final Color BG2 = new Color(0x111E27);
    static final Color CARD = new Color(0x1B2A38);
    static final Color RED = new Color(0xFF4655);
    static final Color RED_DK = new Color(0xC2313C);
    static final Color LINE = new Color(0x223140);

    private static final Set<String> DEFAULT_UNLOCKED =
            new HashSet<>(Arrays.asList("Jett", "Phoenix", "Brimstone", "Sova", "Sage"));

    instalocker instalocker = new instalocker();
    public static JButton lockButton;
    public static boolean run;
    String lastClickedAgent = "";
    JFrame frame;
    JPanel agentsPanel;
    CardLayout agentsCardLayout;

    private final List<TButton> roleButtons = new ArrayList<>();
    private TButton selectedAgentButton;

    public gui() {
        LafManager.install(new OneDarkTheme());
        JsonObject check = instalocker.data();
        if (check.get("updateAvailable").getAsBoolean()) {
            JOptionPane.showMessageDialog(null, "the program is currently in maintenance mode.");
            return;
        }
        if (!instalocker.lockfilePath.exists()) {
            JOptionPane.showMessageDialog(null, "you must be open the game.");
            return;
        }
        frame = new JFrame(generateTitle());
        frame.setIconImage(new ImageIcon(main.program_path + "\\logo.png").getImage());
        frame.setSize(600, 380);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        frame.setContentPane(root);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);

        JPanel rail = new JPanel(new GridLayout(4, 1, 0, 6));
        rail.setBackground(BG2);
        rail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, LINE),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        rail.setPreferredSize(new Dimension(74, 0));

        agentsCardLayout = new CardLayout();
        agentsPanel = new JPanel(agentsCardLayout);
        agentsPanel.setBackground(BG);

        var authorization = instalocker.authorization();
        Map<String, String> header = instalocker.authHeaders(authorization);
        Set<String> ownedItemIDs = fetchOwnedAgents(authorization, header);

        String[][] cats = {
                {"duelists", "duelist"}, {"controllers", "controller"},
                {"initiators", "initiator"}, {"sentinels", "sentinel"}
        };
        JsonArray categories = instalocker.data().getAsJsonArray("categories");
        for (String[] cat : cats) {
            String catKey = cat[0], folder = cat[1];

            TButton roleBtn = new TButton(TButton.Style.ROLE);
            roleBtn.setIcon(scaled(folder + ".png", 36));
            roleBtn.addActionListener(e -> {
                agentsCardLayout.show(agentsPanel, catKey);
                for (TButton rb : roleButtons) rb.setPicked(rb == roleBtn);
            });
            roleButtons.add(roleBtn);
            rail.add(roleBtn);

            JPanel grid = new JPanel(new GridLayout(2, 5, 10, 10));
            grid.setBackground(BG);
            grid.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            JsonObject agentsObj = findCategory(categories, catKey);
            if (agentsObj != null) {
                for (Map.Entry<String, JsonElement> ag : agentsObj.entrySet()) {
                    grid.add(buildAgentButton(folder, ag.getKey(), ag.getValue().getAsString(), ownedItemIDs));
                }
            }
            agentsPanel.add(grid, catKey);
        }

        center.add(rail, BorderLayout.WEST);
        center.add(agentsPanel, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        lockButton = new TButton(TButton.Style.LOCK);
        lockButton.setText("Lock");
        lockButton.setPreferredSize(new Dimension(0, 54));
        lockButton.addActionListener(e -> onLock());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));
        bottom.add(lockButton, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        if (!roleButtons.isEmpty()) {
            roleButtons.get(0).setPicked(true);
            agentsCardLayout.show(agentsPanel, "duelists");
        }

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private TButton buildAgentButton(String folder, String agentName, String agentUUID, Set<String> owned) {
        TButton b = new TButton(TButton.Style.AGENT);
        b.setIcon(scaled(folder + "\\" + agentName.toLowerCase(Locale.ENGLISH) + ".png", 74));
        b.setEnabled(DEFAULT_UNLOCKED.contains(agentName) || owned.contains(agentUUID));
        b.addActionListener(e -> {
            lastClickedAgent = agentName;
            if (selectedAgentButton != null) selectedAgentButton.setPicked(false);
            selectedAgentButton = b;
            b.setPicked(true);
        });
        return b;
    }

    private void onLock() {
        if (lastClickedAgent == null || lastClickedAgent.isEmpty()) {
            JOptionPane.showMessageDialog(null, "first you must choose an agent.");
            return;
        }
        new Thread(() -> {
            try {
                run = true;
                if (lockButton.getText().equals("Stop") && lockButton.isEnabled()) {
                    run = false;
                    lockButton.setText("Lock");
                } else {
                    lockButton.setText("Stop");
                }
                instalocker.run(lastClickedAgent);
            } catch (Exception ignored) {
            }
        }).start();
    }

    private Set<String> fetchOwnedAgents(authorization auth, Map<String, String> header) {
        Set<String> owned = new HashSet<>();
        try {
            JsonObject inv = instalocker.requests.GSON.fromJson(
                    instalocker.requests.get("https://pd.eu.a.pvp.net/store/v1/entitlements/" + auth.getUuid() + "/01bb38e1-da47-4e6a-9b3d-945fe4655707", header).join().body(),
                    JsonObject.class);
            JsonArray ent = inv.getAsJsonArray("Entitlements");
            for (int j = 0; j < ent.size(); j++) owned.add(ent.get(j).getAsJsonObject().get("ItemID").getAsString());
        } catch (Exception ignored) {
        }
        return owned;
    }

    private static JsonObject findCategory(JsonArray categories, String key) {
        for (int i = 0; i < categories.size(); i++) {
            JsonObject o = categories.get(i).getAsJsonObject();
            if (o.has(key)) return o.getAsJsonObject(key);
        }
        return null;
    }

    private static ImageIcon scaled(String rel, int size) {
        ImageIcon ic = new ImageIcon(main.program_path + "\\" + rel);
        if (ic.getIconWidth() <= 0) return null;
        return new ImageIcon(ic.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    static Color brighten(Color c, int d) {
        return new Color(Math.min(255, c.getRed() + d), Math.min(255, c.getGreen() + d), Math.min(255, c.getBlue() + d));
    }

    public static String generateTitle() {
        String plainText = UUID.randomUUID().toString();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (Exception ignored) {
        }
        assert messageDigest != null;
        messageDigest.reset();
        messageDigest.update(plainText.getBytes());
        byte[] digest = messageDigest.digest();
        BigInteger bigInteger = new BigInteger(1, digest);
        StringBuilder sb = new StringBuilder(bigInteger.toString(16));
        while (sb.length() < 32) sb.insert(0, "0");
        return sb.toString();
    }

    static class TButton extends JButton {
        enum Style {ROLE, AGENT, LOCK}

        private final Style style;
        private boolean sel;
        private boolean hover;

        TButton(Style style) {
            this.style = style;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setPicked(boolean s) {
            if (sel != s) {
                sel = s;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight();
            boolean on = isEnabled();

            if (style == Style.LOCK) {
                boolean stopping = "Stop".equalsIgnoreCase(getText());
                Color fill = stopping ? RED_DK : RED;
                if (hover) fill = brighten(fill, 18);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                String label = stopping ? "STOP" : "LOCK IN";
                drawCentered(g2, label, w, h);
            } else if (style == Style.ROLE) {
                Color fill = sel ? CARD : (hover ? new Color(0x18262F) : BG2);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                if (sel) {
                    g2.setColor(RED);
                    g2.fillRoundRect(0, 6, 4, h - 12, 4, 4);
                }
                paintIcon(g2, w, h, sel ? 1f : (hover ? 0.95f : 0.72f));
            } else {
                if (sel) {
                    g2.setColor(new Color(0x22FF4655, true));
                    g2.fillRoundRect(0, 0, w, h, 12, 12);
                } else if (hover && on) {
                    g2.setColor(CARD);
                    g2.fillRoundRect(0, 0, w, h, 12, 12);
                }
                paintIcon(g2, w, h, on ? 1f : 0.30f);
                if (sel) {
                    g2.setColor(RED);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);
                }
            }
            g2.dispose();
        }

        private void paintIcon(Graphics2D g2, int w, int h, float alpha) {
            Icon ic = getIcon();
            if (ic == null) return;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
            int ix = (w - ic.getIconWidth()) / 2;
            int iy = (h - ic.getIconHeight()) / 2;
            ic.paintIcon(this, g2, ix, iy);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        private void drawCentered(Graphics2D g2, String s, int w, int h) {
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(s)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(s, tx, ty);
        }
    }
}
