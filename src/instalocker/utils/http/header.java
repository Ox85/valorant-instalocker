package instalocker.utils.http;

public class header {

    private final String key;
    private final String value;

    public header(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}