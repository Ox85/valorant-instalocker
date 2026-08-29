package instalocker.utils.http;

public class lockfile {

    private final int port;
    private final String password;

    public lockfile(int port, String password) {
        this.port = port;
        this.password = password;
    }

    public int port() {
        return port;
    }

    public String password() {
        return password;
    }
}
