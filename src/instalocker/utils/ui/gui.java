package instalocker.utils.ui;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import instalocker.start;

public class gui implements ActionListener {

    private static final List<JButton> allAgentButtons = new ArrayList<>();
    private static final Map<JButton, Boolean> initialButtonStates = new HashMap<>();
    instalocker instalocker = new instalocker();
    JPanel mainPanel, buttonsPanel, duelistsPanel, controllersPanel, initiatorsPanel, sentinelsPanel;
    JButton duelistsButton, controllersButton, initiatorsButton, sentinelsButton;
    public static JButton lockButton;
    String lastClickedAgent = "";
    private JButton lastClickedButton = null;
    public static String region;
    public static boolean run;

    JPanel rightPanel;
    JFrame frame;
    CardLayout cardLayout;
    JPanel agentsPanel;
    CardLayout agentsCardLayout;

    public gui() {
        LafManager.install(new OneDarkTheme());
        UIManager.put("Button.focusPainted", false);
        JsonObject check = instalocker.requests.gson.fromJson(instalocker.requests.get(start.DATA_LINK).join().body(), JsonObject.class);
        if (check.get("updateAvailable").getAsBoolean()) {
            JOptionPane.showMessageDialog(null, "the program is currently in maintenance mode.");
            return;
        }
        if (!instalocker.lockfilePath.exists()) {
            JOptionPane.showMessageDialog(null, "you must be open the game.");
            return;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(System.getenv("LOCALAPPDATA") + "\\" + "VALORANT" + "\\" + "Saved" + "\\" + "Logs" + "\\" + "ShooterGame.log");
            String content = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
            region = content.split("regions/")[1].split("]")[0];
        } catch (Exception ignored) {}
        frame = new JFrame(generateTitle());
        frame.setIconImage(new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "logo.png").getImage());
        frame.setSize(500, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new JPanel(new BorderLayout());
        frame.getContentPane().add(mainPanel);
        buttonsPanel = new JPanel(new GridLayout(4, 1));
        duelistsButton = new JButton();
        duelistsButton.setPreferredSize(new Dimension(80, 80));
        duelistsButton.addActionListener(this);
        duelistsButton.setFocusPainted(false);
        ImageIcon duelistIcon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "duelist.png");
        Image duelistImage = duelistIcon.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT);
        duelistsButton.setIcon(new ImageIcon(duelistImage));
        buttonsPanel.add(duelistsButton);
        controllersButton = new JButton();
        controllersButton.setPreferredSize(new Dimension(80, 80));
        controllersButton.addActionListener(this);
        controllersButton.setFocusPainted(false);
        ImageIcon controllerIcon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "controller.png");
        Image controllerImage = controllerIcon.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT);
        controllersButton.setIcon(new ImageIcon(controllerImage));
        buttonsPanel.add(controllersButton);
        initiatorsButton = new JButton();
        initiatorsButton.setPreferredSize(new Dimension(80, 80));
        initiatorsButton.addActionListener(this);
        initiatorsButton.setFocusPainted(false);
        ImageIcon initiatorIcon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "initiator.png");
        Image initiatorImage = initiatorIcon.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT);
        initiatorsButton.setIcon(new ImageIcon(initiatorImage));
        buttonsPanel.add(initiatorsButton);
        sentinelsButton = new JButton();
        sentinelsButton.setPreferredSize(new Dimension(80, 80));
        sentinelsButton.addActionListener(this);
        sentinelsButton.setFocusPainted(false);
        ImageIcon sentinelIcon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "sentinel.png");
        Image sentinelImage = sentinelIcon.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT);
        sentinelsButton.setIcon(new ImageIcon(sentinelImage));
        buttonsPanel.add(sentinelsButton);
        duelistsPanel = new JPanel(new GridLayout(2, 5));
        controllersPanel = new JPanel(new GridLayout(2, 5));
        initiatorsPanel = new JPanel(new GridLayout(2, 5));
        sentinelsPanel = new JPanel(new GridLayout(2, 5));
        var authorization = instalocker.authorization();
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + authorization.getAccessToken());
        header.put("X-Riot-Entitlements-JWT", authorization.getToken());
        header.put("X-Riot-ClientPlatform", "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9");
        header.put("X-Riot-ClientVersion", instalocker.getVersion());
        instalocker.requests.get(start.DATA_LINK).thenAcceptAsync(dataResponse -> {
            JsonObject data = instalocker.requests.gson.fromJson(dataResponse.body(), JsonObject.class);
            JsonArray categories = data.getAsJsonArray("categories");
            for (int i = 0; i < categories.size(); i++) {
                for(Map.Entry<String, JsonElement> entry : categories.get(i).getAsJsonObject().entrySet()) {
                    for (Map.Entry<String, JsonElement> agents : entry.getValue().getAsJsonObject().entrySet()) {
                        if ("duelists".equals(entry.getKey())) {
                            JButton button = new JButton();
                            button.setFocusPainted(false);
                            ImageIcon icon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "duelist" + "\\" + agents.getKey().toLowerCase(Locale.ENGLISH) + ".png");
                            Image image = icon.getImage().getScaledInstance(85, 85, Image.SCALE_DEFAULT);
                            button.setIcon(new ImageIcon(image));
                            button.putClientProperty("agentName", agents.getKey());
                            button.setEnabled(agents.getKey().contains("Jett") || agents.getKey().contains("Phoenix"));
                            instalocker.requests.get("https://pd.eu.a.pvp.net/store/v1/entitlements/" + authorization.getUuid() + "/01bb38e1-da47-4e6a-9b3d-945fe4655707",header).thenAcceptAsync(response -> {
                                JsonObject inventory = instalocker.requests.gson.fromJson(response.body(), JsonObject.class);
                                JsonArray entitlements = inventory.getAsJsonArray("Entitlements");
                                for (int j = 0; j < entitlements.size(); j++) {
                                    String itemID = entitlements.get(j).getAsJsonObject().get("ItemID").getAsString();
                                    if (agents.getValue().getAsString().equals(itemID)) {
                                        button.setEnabled(true);
                                        frame.revalidate();
                                        frame.repaint();
                                    }
                                }
                                initialButtonStates.put(button, button.isEnabled());
                            }).join();
                            button.addActionListener(e -> {
                                if (lastClickedButton != null) {
                                    lastClickedButton.setBorder(null);
                                }
                                lastClickedAgent = agents.getKey();
                                lastClickedButton = (JButton) e.getSource();
                                lastClickedButton.setBorder(BorderFactory.createLineBorder(new Color(255, 81, 82, 255), 3));
                            });
                            button.setContentAreaFilled(false);
                            duelistsPanel.add(button);
                            allAgentButtons.add(button);
                        } else if ("controllers".equals(entry.getKey())) {
                            JButton button = new JButton();
                            button.setFocusPainted(false);
                            ImageIcon icon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "controller" + "\\" + agents.getKey().toLowerCase(Locale.ENGLISH) + ".png");
                            Image image = icon.getImage().getScaledInstance(85, 85, Image.SCALE_DEFAULT);
                            button.setIcon(new ImageIcon(image));
                            button.putClientProperty("agentName", agents.getKey());
                            button.setEnabled(agents.getKey().contains("Brimstone"));
                            instalocker.requests.get("https://pd.eu.a.pvp.net/store/v1/entitlements/" + authorization.getUuid() + "/01bb38e1-da47-4e6a-9b3d-945fe4655707", header).thenAcceptAsync(response -> {
                                JsonObject inventory = instalocker.requests.gson.fromJson(response.body(), JsonObject.class);
                                JsonArray entitlements = inventory.getAsJsonArray("Entitlements");
                                for (int j = 0; j < entitlements.size(); j++) {
                                    String itemID = entitlements.get(j).getAsJsonObject().get("ItemID").getAsString();
                                    if (agents.getValue().getAsString().equals(itemID)) {
                                        button.setEnabled(true);
                                        frame.revalidate();
                                        frame.repaint();
                                    }
                                }
                                initialButtonStates.put(button, button.isEnabled());
                            }).join();
                            button.addActionListener(e -> {
                                if (lastClickedButton != null) {
                                    lastClickedButton.setBorder(null);
                                }
                                lastClickedAgent = agents.getKey();
                                lastClickedButton = (JButton) e.getSource();
                                lastClickedButton.setBorder(BorderFactory.createLineBorder(new Color(255, 81, 82, 255), 3));
                            });
                            button.setContentAreaFilled(false);
                            controllersPanel.add(button);
                            allAgentButtons.add(button);
                        } else if ("initiators".equals(entry.getKey())) {
                            JButton button = new JButton();
                            button.setFocusPainted(false);
                            ImageIcon icon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "initiator" + "\\" + agents.getKey().toLowerCase(Locale.ENGLISH) + ".png");
                            Image image = icon.getImage().getScaledInstance(85, 85, Image.SCALE_DEFAULT);
                            button.setIcon(new ImageIcon(image));
                            button.putClientProperty("agentName", agents.getKey());
                            button.setEnabled(agents.getKey().contains("Sova"));
                            instalocker.requests.get("https://pd.eu.a.pvp.net/store/v1/entitlements/" + authorization.getUuid() + "/01bb38e1-da47-4e6a-9b3d-945fe4655707", header).thenAcceptAsync(response -> {
                                JsonObject inventory = instalocker.requests.gson.fromJson(response.body(), JsonObject.class);
                                JsonArray entitlements = inventory.getAsJsonArray("Entitlements");
                                for (int j = 0; j < entitlements.size(); j++) {
                                    String itemID = entitlements.get(j).getAsJsonObject().get("ItemID").getAsString();
                                    if (agents.getValue().getAsString().equals(itemID)) {
                                        button.setEnabled(true);
                                        frame.revalidate();
                                        frame.repaint();
                                    }
                                }
                                initialButtonStates.put(button, button.isEnabled());
                            }).join();
                            button.addActionListener(e -> {
                                if (lastClickedButton != null) {
                                    lastClickedButton.setBorder(null);
                                }
                                lastClickedAgent = agents.getKey();
                                lastClickedButton = (JButton) e.getSource();
                                lastClickedButton.setBorder(BorderFactory.createLineBorder(new Color(255, 81, 82, 255), 3));
                            });
                            button.setContentAreaFilled(false);
                            initiatorsPanel.add(button);
                            allAgentButtons.add(button);
                        } else if ("sentinels".equals(entry.getKey())) {
                            JButton button = new JButton();
                            button.setFocusPainted(false);
                            ImageIcon icon = new ImageIcon(System.getProperty("java.io.tmpdir") + File.separator + start.NAME + "\\" + "sentinel" + "\\" + agents.getKey().toLowerCase(Locale.ENGLISH) + ".png");
                            Image image = icon.getImage().getScaledInstance(85, 85, Image.SCALE_DEFAULT);
                            button.setIcon(new ImageIcon(image));
                            button.putClientProperty("agentName", agents.getKey());
                            button.setEnabled(agents.getKey().contains("Sage"));
                            instalocker.requests.get("https://pd.eu.a.pvp.net/store/v1/entitlements/" + authorization.getUuid() + "/01bb38e1-da47-4e6a-9b3d-945fe4655707", header).thenAcceptAsync(response -> {
                                JsonObject inventory = instalocker.requests.gson.fromJson(response.body(), JsonObject.class);
                                JsonArray entitlements = inventory.getAsJsonArray("Entitlements");
                                for (int j = 0; j < entitlements.size(); j++) {
                                    String itemID = entitlements.get(j).getAsJsonObject().get("ItemID").getAsString();
                                    if (agents.getValue().getAsString().equals(itemID)) {
                                        button.setEnabled(true);
                                        frame.revalidate();
                                        frame.repaint();
                                    }
                                }
                                initialButtonStates.put(button, button.isEnabled());
                            }).join();
                            button.addActionListener(e -> {
                                if (lastClickedButton != null) {
                                    lastClickedButton.setBorder(null);
                                }
                                lastClickedAgent = agents.getKey();
                                lastClickedButton = (JButton) e.getSource();
                                lastClickedButton.setBorder(BorderFactory.createLineBorder(new Color(255, 81, 82, 255), 3));
                            });
                            button.setContentAreaFilled(false);
                            sentinelsPanel.add(button);
                            allAgentButtons.add(button);
                        }
                    }
                }
            }
        });
        cardLayout = new CardLayout();
        rightPanel = new JPanel(cardLayout);
        JPanel instalockerPanel = new JPanel(new BorderLayout());
        instalockerPanel.add(buttonsPanel, BorderLayout.WEST);
        agentsCardLayout = new CardLayout();
        agentsPanel = new JPanel(agentsCardLayout);
        agentsPanel.add(duelistsPanel, "duelists");
        agentsPanel.add(controllersPanel, "controllers");
        agentsPanel.add(initiatorsPanel, "initiators");
        agentsPanel.add(sentinelsPanel, "sentinels");
        instalockerPanel.add(agentsPanel, BorderLayout.CENTER);
        lockButton = new JButton("Lock");
        lockButton.addActionListener(this);
        lockButton.setFocusPainted(false);
        instalockerPanel.add(lockButton, BorderLayout.SOUTH);
        rightPanel.add(instalockerPanel, "instalocker");
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void disableAllButtonsExceptSelected() {
        for (JButton button : allAgentButtons) {
            if (button != lastClickedButton) {
                button.setEnabled(false);
            }
        }
        if (lastClickedButton != null) {
            lastClickedButton.setEnabled(true);
        }
        frame.revalidate();
        frame.repaint();
    }

    public static void restoreOriginalButtonStates() {
        for (JButton button : allAgentButtons) {
            Boolean initialState = initialButtonStates.get(button);
            button.setEnabled(Objects.requireNonNullElse(initialState, true));
        }
        if (lockButton != null && lockButton.getTopLevelAncestor() instanceof JFrame frame) {
            frame.revalidate();
            frame.repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == duelistsButton) {
            agentsCardLayout.show(agentsPanel, "duelists");
        } else if (e.getSource() == controllersButton) {
            agentsCardLayout.show(agentsPanel, "controllers");
        } else if (e.getSource() == initiatorsButton) {
            agentsCardLayout.show(agentsPanel, "initiators");
        } else if (e.getSource() == sentinelsButton) {
            agentsCardLayout.show(agentsPanel, "sentinels");
        } else if (e.getSource() == lockButton) {
            if (lastClickedAgent != null && lastClickedButton != null) {
                try {
                    Thread thread = new Thread(() -> {
                        try {
                            run = true;
                            if (lockButton.getText().equals("Stop") && lockButton.isEnabled()) {
                                run = false;
                                lockButton.setText("Lock");
                                SwingUtilities.invokeLater(gui::restoreOriginalButtonStates);
                            } else {
                                lockButton.setText("Stop");
                                SwingUtilities.invokeLater(this::disableAllButtonsExceptSelected);
                            }
                            instalocker.run(lastClickedAgent);
                            if (run) {
                                SwingUtilities.invokeLater(() -> {
                                    lockButton.setText("Lock");
                                    restoreOriginalButtonStates();
                                });
                            }
                        } catch (Exception ignored) {
                            SwingUtilities.invokeLater(() -> {
                                lockButton.setText("Lock");
                                restoreOriginalButtonStates();
                            });
                        }
                    });
                    thread.start();
                } catch (Exception ignored) {}
            } else {
                JOptionPane.showMessageDialog(null, "first you must choose an agent.");
            }
        }
    }

    public static String generateTitle() {
        try {
            String plainText = UUID.randomUUID().toString();
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (Exception ignored) {}
            assert messageDigest != null;
            messageDigest.reset();
            messageDigest.update(plainText.getBytes());
            byte[] digest = messageDigest.digest();
            BigInteger bigInteger = new BigInteger(1, digest);
            StringBuilder stringBuilder = new StringBuilder(bigInteger.toString(16));
            while (stringBuilder.length() < 32) {
                stringBuilder.insert(0, "0");
            }
            return stringBuilder.toString();
        } catch (Exception ignored) {}
        return null;
    }
}
