package instalocker.utils.ui;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import instalocker.start;
import instalocker.utils.http.authorization;
import instalocker.utils.http.lockfile;
import instalocker.utils.requests;
import javax.swing.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class instalocker {

    public File lockfilePath = new File(System.getenv("LOCALAPPDATA") + File.separator + "Riot Games" + File.separator + "Riot Client" + File.separator + "Config" + File.separator + "lockfile");
    public requests requests = new requests();
    private long timerMS = System.currentTimeMillis();
    private State currentState;
    private boolean sent;
    public String matchID;

    private enum State {
        MENU,
        PREGAME,
        INGAME;
    }

    public void run(String agent) {
        try {
            var authorization = authorization();
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Bearer " + authorization.getAccessToken());
            header.put("X-Riot-Entitlements-JWT", authorization.getToken());
            header.put("X-Riot-ClientPlatform", "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9");
            header.put("X-Riot-ClientVersion", getVersion());
            Map<String, String> header0 = new HashMap<>();
            header0.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(lockfile().password().getBytes()));
            while (gui.run) {
                if (System.currentTimeMillis() - timerMS > 1_000) {
                    requests.get("https://glz-eu-1.eu.a.pvp.net/pregame/v1/players/" + authorization.getUuid(), header).thenAcceptAsync(response -> {
                        JsonObject player = requests.gson.fromJson(response.body(), JsonObject.class);
                        if (!player.has("errorCode")) {
                            var privateJson = new JsonObject();
                            var response_ = requests.get("https://127.0.0.1:" + lockfile().port() + "/chat/v4/presences", header0).join();
                            var data = requests.gson.fromJson(response_.body(), JsonObject.class);
                            JsonArray presences = data.getAsJsonArray("presences");
                            for (int i = 0; i < presences.size(); i++) {
                                String presenceUUID = presences.get(i).getAsJsonObject().get("puuid").getAsString();
                                String sessionUUID = session().get("puuid").getAsString();
                                if (presenceUUID.equals(sessionUUID)) {
                                    String privateJson0 = presences.get(i).getAsJsonObject().get("private").getAsString();
                                    byte[] encodedJson = Base64.getDecoder().decode(privateJson0);
                                    privateJson = requests.gson.fromJson(new String(encodedJson, StandardCharsets.UTF_8), JsonObject.class);
                                    break;
                                }
                            }
                            var presence = privateJson;
                            currentState = State.valueOf(presence.get("sessionLoopState").getAsString());
                            switch (currentState) {
                                case MENU, INGAME: {
                                    matchID = "UNKNOWN";
                                    sent = false;
                                }
                                case PREGAME: {
                                    if (!sent) {
                                        matchID = getString(player.get("MatchID"));
                                        lock(matchID, getAgentByUUID(agent));
                                        sent = true;
                                        gui.lockButton.setText("Lock");
                                        gui.run = false;
                                        SwingUtilities.invokeLater(gui::restoreOriginalButtonStates);
                                    }
                                }
                            }
                        } else sent = false;
                    });
                    timerMS = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error: " + e.getMessage().toLowerCase());
        }
    }

    public String getVersion() {
        try {
            var response = requests.gson.fromJson(requests.get("https://valorant-api.com/v1/version").join().body(), JsonObject.class);
            var data = response.getAsJsonObject("data");
            return data.get("version").getAsString();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error: " + e.getMessage().toLowerCase());
        }
        return null;
    }

    public String getString(JsonElement jsonElement) {
        return jsonElement == JsonNull.INSTANCE ? null : jsonElement.getAsString();
    }

    public void lock(String matchID, String agentID) {
        try {
            var authorization = authorization();
            Map<String, String> header = new HashMap();
            header.put("Authorization", "Bearer " + authorization.getAccessToken());
            header.put("X-Riot-Entitlements-JWT", authorization.getToken());
            header.put("X-Riot-ClientPlatform", "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9");
            header.put("X-Riot-ClientVersion", getVersion());
            requests.post("https://glz-eu-1.eu.a.pvp.net/pregame/v1/matches/" + matchID + "/lock/" + agentID, "{}", header).join();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error: " + e.getMessage().toLowerCase());
        }
    }

    public lockfile lockfile() {
        try {
            List<String> contents = new ArrayList<>();
            try {
                contents = FileUtils.readLines(lockfilePath, "UTF-8");
            } catch (Exception ignored) {}
            for (String line : contents) {
                String[] split = line.split(":");
                int port = Integer.parseInt(split[2]);
                String password = "riot:" + split[3];
                return new lockfile(port, password);
            }
            return new lockfile(1337, "1337");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error: " + e.getMessage().toLowerCase());
        }
        return null;
    }

    public JsonObject session() {
        try {
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(lockfile().password().getBytes()));
            return requests.gson.fromJson(requests.get("https://127.0.0.1:" + lockfile().port() + "/chat/v1/session", header).join().body(), JsonObject.class);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error: " + e.getMessage().toLowerCase());
        }
        return null;
    }

    public authorization authorization() {
        try {
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(lockfile().password().getBytes()));
            var data = requests.gson.fromJson(requests.get("https://127.0.0.1:" + lockfile().port() + "/entitlements/v1/token", header).join().body(), JsonObject.class);
            String accessToken = data.get("accessToken").getAsString();
            String token = data.get("token").getAsString();
            String uuid = data.get("subject").getAsString();
            return new authorization(accessToken, token, uuid);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error: " + e.getMessage().toLowerCase());
        }
        return null;
    }

    public String getAgentByUUID(String agentName) {
        try {
            JsonObject data = requests.gson.fromJson(requests.get(start.DATA_LINK).join().body(), JsonObject.class);
            JsonArray categories = data.getAsJsonArray("categories").getAsJsonArray();
            for (int i = 0; i < categories.size(); i++) {
                for (Map.Entry<String, JsonElement> entry : categories.get(i).getAsJsonObject().entrySet()) {
                    for (Map.Entry<String, JsonElement> agents : entry.getValue().getAsJsonObject().entrySet()) {
                        if (agentName.equals(agents.getKey())) {
                            agentName = agents.getValue().getAsString();
                        }
                    }
                }
            }
            return agentName;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "error: " + e.getMessage().toLowerCase());
        }
        return null;
    }
}
