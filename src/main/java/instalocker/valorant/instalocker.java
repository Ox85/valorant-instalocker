package instalocker.valorant;

import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import instalocker.utils.http.authorization;
import instalocker.utils.http.lockfile;
import instalocker.utils.requests;
import instalocker.main;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class instalocker {

    private static final String CLIENT_PLATFORM =
            "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9";

    private static final String GLZ = "https://glz-eu-1.eu.a.pvp.net";

    public File lockfilePath = new File(System.getenv("LOCALAPPDATA") + File.separator + "Riot Games"
            + File.separator + "Riot Client" + File.separator + "Config" + File.separator + "lockfile");
    public requests requests = new requests();

    public String matchID;

    private String cachedVersion;
    private JsonObject cachedData;
    private lockfile cachedLockfile;

    public void run(String agent) {
        authorization authorization;
        try {
            authorization = authorization();
        } catch (Exception e) {
            gui.run = false;
            return;
        }
        Map<String, String> header = authHeaders(authorization);
        String agentID = getAgentByUUID(agent);
        String pregameUrl = GLZ + "/pregame/v1/players/" + authorization.getUuid();

        while (gui.run) {
            try {
                var response = requests.get(pregameUrl, header).join();
                int sc = response.statusCode();
                if (sc == 200) {
                    JsonObject player = requests.GSON.fromJson(response.body(), JsonObject.class);
                    if (player != null && player.has("MatchID") && !player.get("MatchID").isJsonNull()) {
                        matchID = player.get("MatchID").getAsString();
                        lock(matchID, agentID);
                        gui.lockButton.setText("Lock");
                        gui.run = false;
                        break;
                    }
                } else if (sc == 400 || sc == 401 || sc == 403) {
                    authorization = authorization();
                    header = authHeaders(authorization);
                    pregameUrl = GLZ + "/pregame/v1/players/" + authorization.getUuid();
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public String getVersion() {
        if (cachedVersion == null || cachedVersion.isEmpty()) {
            try {
                File log = new File(System.getenv("LOCALAPPDATA") + File.separator + "VALORANT"
                        + File.separator + "Saved" + File.separator + "Logs" + File.separator + "ShooterGame.log");
                String content = FileUtils.readFileToString(log, "UTF-8");
                Matcher m = Pattern.compile("CI server version:\\s*(\\S+)").matcher(content);
                if (m.find()) {
                    cachedVersion = m.group(1);
                }
            } catch (Exception ignored) {
            }
            if (cachedVersion == null || cachedVersion.isEmpty()) {
                try {
                    JsonObject data = requests.GSON.fromJson(requests.get("https://valorant-api.com/v1/version").join().body(), JsonObject.class).getAsJsonObject("data");
                    String[] parts = data.get("version").getAsString().split("\\.");
                    cachedVersion = data.get("branch").getAsString() + "-shipping-"
                            + data.get("buildVersion").getAsString() + "-" + parts[parts.length - 1];
                } catch (Exception ignored) {
                }
            }
        }
        return cachedVersion;
    }

    public JsonObject data() {
        if (cachedData == null) {
            cachedData = requests.GSON.fromJson(requests.get(main.program_data_link).join().body(), JsonObject.class);
        }
        return cachedData;
    }

    public void lock(String matchID, String agentID) {
        var authorization = authorization();
        Map<String, String> header = authHeaders(authorization);
        String base = GLZ + "/pregame/v1/matches/" + matchID;
        var response = requests.post(base + "/lock/" + agentID, "", header).join();
        if (response.statusCode() != 200) {
            requests.post(base + "/select/" + agentID, "", header).join();
            requests.post(base + "/lock/" + agentID, "", header).join();
        }
    }

    public lockfile lockfile() {
        if (cachedLockfile == null) {
            List<String> contents = new ArrayList<>();
            try {
                contents = FileUtils.readLines(lockfilePath, "UTF-8");
            } catch (Exception ignored) {
            }
            for (String line : contents) {
                String[] split = line.split(":");
                int port = Integer.parseInt(split[2]);
                String password = "riot:" + split[3];
                cachedLockfile = new lockfile(port, password);
                return cachedLockfile;
            }
            cachedLockfile = new lockfile(1337, "1337");
        }
        return cachedLockfile;
    }

    public authorization authorization() {
        var data = requests.GSON.fromJson(requests.get("https://127.0.0.1:" + lockfile().port() + "/entitlements/v1/token", basicHeader()).join().body(), JsonObject.class);
        String accessToken = data.get("accessToken").getAsString();
        String token = data.get("token").getAsString();
        String uuid = data.get("subject").getAsString();
        return new authorization(accessToken, token, uuid);
    }

    public String getAgentByUUID(String agentName) {
        JsonArray categories = data().getAsJsonArray("categories");
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
    }

    public Map<String, String> authHeaders(authorization auth) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + auth.getAccessToken());
        header.put("X-Riot-Entitlements-JWT", auth.getToken());
        header.put("X-Riot-ClientPlatform", CLIENT_PLATFORM);
        header.put("X-Riot-ClientVersion", getVersion());
        return header;
    }

    private Map<String, String> basicHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(lockfile().password().getBytes()));
        return header;
    }
}
