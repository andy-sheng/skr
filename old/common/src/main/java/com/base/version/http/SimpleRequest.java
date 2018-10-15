package com.base.version.http;
/**
 * Created by chengsimin on 2016/12/12.
 */

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.annotation.SuppressLint;


import com.base.version.http.bean.NameValuePair;
import com.base.version.http.exception.AccessDeniedException;
import com.base.version.http.exception.AuthenticationFailureException;
import com.base.version.http.utils.IOUtils;
import com.base.version.http.utils.ObjectUtils;
import com.base.version.http.utils.URLEncodedUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@SuppressLint({"NewApi"})
public final class SimpleRequest {
    private static final boolean DEBUG = false;
    public static final String UTF8 = "utf-8";
    private static final Logger log = Logger.getLogger(SimpleRequest.class.getSimpleName());
    private static final int TIMEOUT = 30000;
    public static final String LOCATION = "Location";

    private static SimpleRequest.HttpURLConnectionFactory sHttpURLConnectionFactory = new SimpleRequest.HttpURLConnectionFactory() {
        public HttpURLConnection makeConn(URL url) throws IOException {

            return (HttpURLConnection)url.openConnection();
        }
    };

    public SimpleRequest() {
    }

    static void injectHttpURLConnectionFactoryForTest(SimpleRequest.HttpURLConnectionFactory httpURLConnectionFactory) {
        sHttpURLConnectionFactory = httpURLConnectionFactory;
    }
    private static String appendUrl(String origin, List<NameValuePair> nameValuePairs) {
        if(origin == null) {
            throw new NullPointerException("origin is not allowed null");
        } else {
            StringBuilder urlBuilder = new StringBuilder(origin);
            if(nameValuePairs != null) {
                String paramPart = URLEncodedUtils.format(nameValuePairs, "utf-8");
                if(paramPart != null && paramPart.length() > 0) {
                    if(origin.contains("?")) {
                        urlBuilder.append("&");
                    } else {
                        urlBuilder.append("?");
                    }

                    urlBuilder.append(paramPart);
                }
            }

            return urlBuilder.toString();
        }
    }

    public static SimpleRequest.StringContent getAsString(String url, Map<String, String> params, Map<String, String> cookies, boolean readBody) throws IOException, AccessDeniedException, AuthenticationFailureException {
        return getAsString(url, params, (Map)null, cookies, readBody);
    }

    public static SimpleRequest.StringContent getAsString(String url, Map<String, String> params, Map<String, String> headers, Map<String, String> cookies, boolean readBody) throws IOException, AccessDeniedException, AuthenticationFailureException {
        List nameValuePairs = ObjectUtils.mapToPairs(params);
        String fullUrl = appendUrl(url, nameValuePairs);
        HttpURLConnection conn = makeConn(fullUrl, cookies, headers);
        if(conn == null) {
            log.severe("failed to create URLConnection");
            throw new IOException("failed to create connection");
        } else {
            SimpleRequest.StringContent line1;
            try {
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                conn.connect();
                int e = conn.getResponseCode();
                if(e != 200 && e != 302) {
                    if(e == 403) {
                        throw new AccessDeniedException("access denied, encrypt error or user is forbidden to access the resource");
                    }

                    if(e != 401 && e != 400) {
                        log.info("http status error when GET: " + e);
                        if(e == 301) {
                            log.info("unexpected redirect from " + conn.getURL().getHost() + " to " + conn.getHeaderField("Location"));
                        }

                        throw new IOException("unexpected http res code: " + e);
                    }

                    throw new AuthenticationFailureException("authentication failure for get, code: " + e);
                }

                Map headerFields = conn.getHeaderFields();
                CookieManager cm = new CookieManager();
                URI reqUri = URI.create(fullUrl);
                cm.put(reqUri, headerFields);
                List httpCookies = cm.getCookieStore().get(reqUri);
                Map cookieMap = parseCookies(httpCookies);
                cookieMap.putAll(ObjectUtils.listToMap(headerFields));
                StringBuilder sb = new StringBuilder();
                if(readBody) {
                    BufferedReader stringContent = new BufferedReader(new InputStreamReader(conn.getInputStream()), 1024);

                    String line;
                    try {
                        while((line = stringContent.readLine()) != null) {
                            sb.append(line);
                        }
                    } finally {
                        IOUtils.closeQuietly(stringContent);
                    }
                }

                SimpleRequest.StringContent stringContent1 = new SimpleRequest.StringContent(sb.toString());
                stringContent1.putHeaders(cookieMap);
                line1 = stringContent1;
            } catch (ProtocolException var26) {
                throw new IOException("protocol error");
            } finally {
                conn.disconnect();
            }

            return line1;
        }
    }

    public static SimpleRequest.StreamContent getAsStream(String url, Map<String, String> params, Map<String, String> cookies) throws IOException, AccessDeniedException, AuthenticationFailureException {
        List nameValuePairs = ObjectUtils.mapToPairs(params);
        String fullUrl = appendUrl(url, nameValuePairs);
        HttpURLConnection conn = makeConn(fullUrl, cookies);
        if(conn == null) {
            log.severe("failed to create URLConnection");
            throw new IOException("failed to create connection");
        } else {
            try {
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                conn.setInstanceFollowRedirects(true);
                conn.connect();
                int e = conn.getResponseCode();
                if(e == 200) {
                    Map headerFields = conn.getHeaderFields();
                    CookieManager cm = new CookieManager();
                    URI reqUri = URI.create(fullUrl);
                    cm.put(reqUri, headerFields);
                    List httpCookies = cm.getCookieStore().get(reqUri);
                    Map cookieMap = parseCookies(httpCookies);
                    cookieMap.putAll(ObjectUtils.listToMap(headerFields));
                    SimpleRequest.StreamContent streamContent = new SimpleRequest.StreamContent(conn.getInputStream());
                    streamContent.putHeaders(cookieMap);
                    return streamContent;
                } else if(e == 403) {
                    throw new AccessDeniedException("access denied, encrypt error or user is forbidden to access the resource");
                } else if(e != 401 && e != 400) {
                    log.info("http status error when GET: " + e);
                    if(e == 301) {
                        log.info("unexpected redirect from " + conn.getURL().getHost() + " to " + conn.getHeaderField("Location"));
                    }

                    throw new IOException("unexpected http res code: " + e);
                } else {
                    throw new AuthenticationFailureException("authentication failure for get, code: " + e);
                }
            } catch (ProtocolException var13) {
                throw new IOException("protocol error");
            }
        }
    }

    public static SimpleRequest.MapContent getAsMap(String url, Map<String, String> params, Map<String, String> cookies, boolean readBody) throws IOException, AccessDeniedException, AuthenticationFailureException {
        SimpleRequest.StringContent stringContent = getAsString(url, params, cookies, readBody);
        return convertStringToMap(stringContent);
    }

    public static SimpleRequest.StringContent postAsString(String url, Map<String, String> params, Map<String, String> cookies, boolean readBody) throws IOException, AccessDeniedException, AuthenticationFailureException {
        return postAsString(url, params, cookies, (Map)null, (Map)null, readBody);
    }

    public static SimpleRequest.StringContent postAsString(String url, Map<String, String> params, Map<String, String> cookies, Map<String, String> headers, Map<String, String> urlParams, boolean readBody) throws IOException, AccessDeniedException, AuthenticationFailureException {
        if(urlParams != null) {
            List conn = ObjectUtils.mapToPairs(urlParams);
            url = appendUrl(url, conn);
        }

        HttpURLConnection conn1 = makeConn(url, cookies, headers);
        if(conn1 == null) {
            log.severe("failed to create URLConnection");
            throw new IOException("failed to create connection");
        } else {
            SimpleRequest.StringContent line1;
            try {
                conn1.setDoInput(true);
                conn1.setDoOutput(true);
                conn1.setRequestMethod("POST");
                conn1.connect();
                List e = ObjectUtils.mapToPairs(params);
                if(e != null) {
                    String code = URLEncodedUtils.format(e, "utf-8");
                    OutputStream e1 = conn1.getOutputStream();
                    BufferedOutputStream cm = new BufferedOutputStream(e1);

                    try {
                        cm.write(code.getBytes("utf-8"));
                    } finally {
                        IOUtils.closeQuietly(cm);
                    }
                }

                int code1 = conn1.getResponseCode();
                if(code1 != 200 && code1 != 302) {
                    if(code1 == 403) {
                        throw new AccessDeniedException("access denied, encrypt error or user is forbidden to access the resource");
                    }

                    if(code1 != 401 && code1 != 400) {
                        log.info("http status error when POST: " + code1);
                        if(code1 == 301) {
                            log.info("unexpected redirect from " + conn1.getURL().getHost() + " to " + conn1.getHeaderField("Location"));
                        }

                        throw new IOException("unexpected http res code: " + code1);
                    }

                    AuthenticationFailureException e3 = new AuthenticationFailureException("authentication failure for post, code: " + code1);
                    e3.setWwwAuthenticateHeader(conn1.getHeaderField("WWW-Authenticate"));
                    throw e3;
                }

                Map e2 = conn1.getHeaderFields();
                CookieManager cm1 = new CookieManager();
                URI reqUri = URI.create(url);
                cm1.put(reqUri, e2);
                Map cookieMap = parseCookies(cm1.getCookieStore().get(reqUri));
                cookieMap.putAll(ObjectUtils.listToMap(e2));
                StringBuilder sb = new StringBuilder();
                if(readBody) {
                    BufferedReader stringContent = new BufferedReader(new InputStreamReader(conn1.getInputStream()), 1024);

                    String line;
                    try {
                        while((line = stringContent.readLine()) != null) {
                            sb.append(line);
                        }
                    } finally {
                        IOUtils.closeQuietly(stringContent);
                    }
                }

                SimpleRequest.StringContent stringContent1 = new SimpleRequest.StringContent(sb.toString());
                stringContent1.putHeaders(cookieMap);
                line1 = stringContent1;
            } catch (ProtocolException var32) {
                throw new IOException("protocol error");
            } finally {
                conn1.disconnect();
            }

            return line1;
        }
    }

    public static SimpleRequest.MapContent postAsMap(String url, Map<String, String> params, Map<String, String> cookies, boolean readBody) throws IOException, AccessDeniedException, AuthenticationFailureException {
        SimpleRequest.StringContent stringContent = postAsString(url, params, cookies, readBody);
        return convertStringToMap(stringContent);
    }

    protected static SimpleRequest.MapContent convertStringToMap(SimpleRequest.StringContent stringContent) {
        if(stringContent == null) {
            return null;
        } else {
            String bodyString = stringContent.getBody();
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(bodyString);
            } catch (JSONException var5) {
                var5.printStackTrace();
            }

            if(jsonObject == null) {
                return null;
            } else {
                Map contentMap = ObjectUtils.jsonToMap(jsonObject);
                SimpleRequest.MapContent mapContent = new SimpleRequest.MapContent(contentMap);
                mapContent.putHeaders(stringContent.getHeaders());
                return mapContent;
            }
        }
    }

    protected static HttpURLConnection makeConn(String url, Map<String, String> cookies) {
        return makeConn(url, cookies, (Map)null);
    }

    protected static HttpURLConnection makeConn(String url, Map<String, String> cookies, Map<String, String> headers) {
        URL req = null;

        try {
            req = new URL(url);
        } catch (MalformedURLException var7) {
            var7.printStackTrace();
        }

        if(req == null) {
            log.severe("failed to init url");
            return null;
        } else {
            try {
                HttpURLConnection e = sHttpURLConnectionFactory.makeConn(req);
                e.setInstanceFollowRedirects(false);
                e.setConnectTimeout(30000);
                e.setReadTimeout(30000);
                e.setUseCaches(false);
                e.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                if(!TextUtils.isEmpty(XMPassportSettings.getUserAgent())) {
//                    e.setRequestProperty("User-Agent", XMPassportSettings.getUserAgent());
//                }

                if(cookies != null) {
                    e.setRequestProperty("Cookie", joinMap(cookies, "; "));
                }

                if(headers != null) {
                    Iterator i$ = headers.keySet().iterator();

                    while(i$.hasNext()) {
                        String key = (String)i$.next();
                        e.setRequestProperty(key, (String)headers.get(key));
                    }
                }

                return e;
            } catch (Exception var8) {
                var8.printStackTrace();
                return null;
            }
        }
    }

    protected static String joinMap(Map<String, String> map, String sp) {
        if(map == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            Set entries = map.entrySet();
            int i = 0;

            for(Iterator i$ = entries.iterator(); i$.hasNext(); ++i) {
                Map.Entry entry = (Map.Entry)i$.next();
                if(i > 0) {
                    sb.append(sp);
                }

                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }

            return sb.toString();
        }
    }

    protected static Map<String, String> parseCookies(List<HttpCookie> cookies) {
        HashMap cookieMap = new HashMap();
        Iterator i$ = cookies.iterator();

        while(i$.hasNext()) {
            HttpCookie cookie = (HttpCookie)i$.next();
            if(!cookie.hasExpired()) {
                String name = cookie.getName();
                String value = cookie.getValue();
                if(name != null) {
                    cookieMap.put(name, value);
                }
            }
        }

        return cookieMap;
    }

    public static class StreamContent extends SimpleRequest.HeaderContent {
        private InputStream stream;

        public StreamContent(InputStream stream) {
            this.stream = stream;
        }

        public InputStream getStream() {
            return this.stream;
        }

        public void closeStream() {
            IOUtils.closeQuietly(this.stream);
        }
    }

    public static class MapContent extends SimpleRequest.HeaderContent {
        private Map<String, Object> bodies;

        public MapContent(Map<String, Object> bodies) {
            this.bodies = bodies;
        }

        public Object getFromBody(String key) {
            return this.bodies.get(key);
        }

        public String toString() {
            return "MapContent{bodies=" + this.bodies + '}';
        }
    }

    public static class StringContent extends SimpleRequest.HeaderContent {
        private String body;

        public StringContent(String body) {
            this.body = body;
        }

        public String getBody() {
            return this.body;
        }

        public String toString() {
            return "StringContent{body=\'" + this.body + '\'' + '}';
        }
    }

    public static class HeaderContent {
        private final Map<String, String> headers = new HashMap();

        public HeaderContent() {
        }

        public void putHeader(String key, String value) {
            this.headers.put(key, value);
        }

        public String getHeader(String key) {
            return (String)this.headers.get(key);
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }

        public void putHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
        }

        public String toString() {
            return "HeaderContent{headers=" + this.headers + '}';
        }
    }

    public interface HttpURLConnectionFactory {
        HttpURLConnection makeConn(URL var1) throws IOException;
    }
}
