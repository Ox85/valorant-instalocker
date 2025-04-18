package instalocker.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class requests {

    private final HttpClient httpClient;
    public Gson gson;

    public requests() {
        httpClient = HttpClient.newBuilder().sslContext(createInsecureSslContext()).followRedirects(HttpClient.Redirect.NORMAL).build();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @SafeVarargs
    public final CompletableFuture<HttpResponse<String>> get(String url, Map<String, String>... headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        for (Map<String, String> headerMap : headers) {
            if (headerMap != null && !headerMap.isEmpty()) {
                headerMap.forEach(requestBuilder::header);
            }
        }
        HttpRequest request = requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    @SafeVarargs
    public final CompletableFuture<HttpResponse<String>> post(String url, String body, Map<String, String>... headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.ofString(body));
        for (Map<String, String> headerMap : headers) {
            if (headerMap != null && !headerMap.isEmpty()) {
                headerMap.forEach(requestBuilder::header);
            }
        }
        HttpRequest request = requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private SSLContext createInsecureSslContext() {
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception ignored) {}
        return null;
    }
}
