package instalocker.utils.http;

public class authorization {

    private final String accessToken;
    private final String token;
    private final String uuid;

    public authorization(String accessToken, String token, String uuid) {
        this.accessToken = accessToken;
        this.token = token;
        this.uuid = uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getToken() {
        return token;
    }

    public String getUuid() {
        return uuid;
    }
}
