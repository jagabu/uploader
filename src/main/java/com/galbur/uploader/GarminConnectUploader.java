package com.galbur.uploader;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
        
public class GarminConnectUploader implements Closeable {
    private final CloseableHttpClient httpClient;
    private final CookieStore cookieStore;
    
    public GarminConnectUploader() {
        cookieStore = new BasicCookieStore();
        httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36")
                .build();
    }
    
    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    public void doGet(String label, String url) throws URISyntaxException, IOException {
        doGet(label, url, null);
    }

    public void doGet(String label, String url, File saveFile) throws URISyntaxException, IOException {
        StatusLine status;
        System.out.println("\n" + label + " request start");
        
        HttpGet httpGet = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            status = response.getStatusLine();
            if (saveFile != null) {
                try (FileWriter writer = new FileWriter(saveFile)) {
                    IOUtils.copy(response.getEntity().getContent(), writer);
                }
            } else {
                EntityUtils.consume(response.getEntity());
            }
        }
        System.out.println(label + " response status: " + status);
    }

    public void doLogin() throws IOException, URISyntaxException {
        doLogin(null);
    }
    
    public void doLogin(File saveFile) throws IOException, URISyntaxException {
        StatusLine status;
        System.out.println("\nLogin request start");

        URI uri = new URIBuilder("https://sso.garmin.com/sso/login")
                .addParameter("service", "http://connect.garmin.com/post-auth/login")
                .addParameter("webhost", "olaxpw-connect21.garmin.com")
                .addParameter("source", "olaxpw-connect21.garmin.com")
                .addParameter("redirectAfterAccountLoginUrl", "http://connect.garmin.com/post-auth/login")
                .addParameter("redirectAfterAccountCreationUrl", "http://connect.garmin.com/post-auth/login")
                .addParameter("gauthHost", "https://sso.garmin.com/sso")
                .addParameter("locale", "es")
                .addParameter("id", "gauth-widget")
                .addParameter("cssUrl", "https://static.garmincdn.com/com.garmin.connect/ui/css/gauth-custom-v1.0-min.css")
                .addParameter("clientId", "GarminConnect")
                .addParameter("rememberMeShown", "true")
                .addParameter("rememberMeChecked", "false")
                .addParameter("createAccountShown", "true")
                .addParameter("openCreateAccount", "false")
                .addParameter("usernameShown", "false")
                .addParameter("displayNameShown", "false")
                .addParameter("consumeServiceTicket", "false")
                .addParameter("initialFocus", "true")
                .addParameter("embedWidget", "false")
                .addParameter("generateExtraServiceTicket", "false")
                .build();

        HttpUriRequest login = RequestBuilder.post()
                .setUri(uri)
                .addParameter("username", "jagabu@gmail.com")
                .addParameter("password", "hcdlo2005Ga")
                .addParameter("embed", "true")
                .addParameter("lt", "e5s1")
                .addParameter("_eventId", "submit")
                .addParameter("displayNameRequired", "false")
                .addHeader("Accept-Language", "es-ES,es;q=0.8,en;q=0.6")
                .addHeader("Cache-Control", "max-age=0")
                .addHeader("Origin", "https://sso.garmin.com")
                .addHeader("Referer", uri.toURL().toString())
                .build();

        try (CloseableHttpResponse response = httpClient.execute(login)) {
            status = response.getStatusLine();
            if (saveFile != null) {
                try (FileWriter writer = new FileWriter(saveFile)) {
                    IOUtils.copy(response.getEntity().getContent(), writer);
                }
            } else {
                EntityUtils.consume(response.getEntity());
            }
        }
        System.out.println("Login response status: " + status);
    }
    
    public void doUpload() throws URISyntaxException, UnsupportedEncodingException, IOException {
        doUpload(null);
    }
   
    public void doUpload(File saveFile) throws URISyntaxException, UnsupportedEncodingException, IOException {
        StatusLine status;
        System.out.println("\nUpload request start");

        URI uri = new URI("http://connect.garmin.com/proxy/upload-service-1.1/json/upload/.tcx");
        File file = new File("/Users/javi/Downloads/Prueba.tcx");
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        entity.addPart("responseContentType", new StringBody("text/html"));
        entity.addPart("data", new FileBody(file, ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
        HttpPost post = new HttpPost(uri);
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            status = response.getStatusLine();
            if (saveFile != null) {
                try (FileWriter writer = new FileWriter(saveFile)) {
                    IOUtils.copy(response.getEntity().getContent(), writer);
                }
            } else {
                EntityUtils.consume(response.getEntity());
            }
        }
        System.out.println("Upload response status: " + status);
    }

    public void printCookies(PrintStream printStream, String message) {
        List<Cookie> cookies = cookieStore.getCookies();

        printStream.println(message);

        if (cookies.isEmpty()) {
            printStream.println("None");
        } else {
            for (Cookie cookie : cookies) {
                printStream.println("- " + cookie.toString());
            }
        }
    }
    
    public static void main(String... args) throws Exception {
        try (GarminConnectUploader uploader = new GarminConnectUploader()) {
            // uploader.doGet("Home", "http://connect.garmin.com");
            uploader.doLogin(new File("/Users/javi/Desktop/Login.html"));
            // uploader.doGet("Activities", "http://connect.garmin.com/modern/activities", new File("/Users/javi/Desktop/Activities.html"));
            // uploader.doUpload();
        }
    }
}