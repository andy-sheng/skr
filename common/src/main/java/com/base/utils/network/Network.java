package com.base.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.utils.MD5;
import com.base.log.MyLog;
import com.base.utils.PhoneNumUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 *
 * @module 网络请求
 */
public class Network {
    private static final String LogTag = "com.xiaomi.common.Network";

    /**
     * user agent for chrome browser on PC
     */
    public static final String UserAgent_PC_Chrome_6_0_464_0 = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.464.0 Safari/534.3";

    public static final String UserAgent_PC_Chrome = UserAgent_PC_Chrome_6_0_464_0;

    public static final String CMWAP_GATEWAY = "10.0.0.172";

    public static final int CMWAP_PORT = 80;

    public static final String CMWAP_HEADER_HOST_KEY = "X-Online-Host";

    public static final String USER_AGENT = "User-Agent";

    public static final int CONNECTION_TIMEOUT = 10 * 1000;

    public static final int READ_TIMEOUT = 15 * 1000;

    public static final String NETWORK_TYPE_3GWAP = "3gwap";

    public static final String NETWORK_TYPE_3GNET = "3gnet";

    public static final String NETWORK_TYPE_WIFI = "wifi";

    public static final String NETWORK_TYPE_CHINATELECOM = "#777";

    public static InputStream downloadXmlAsStream(Context context, URL url)
            throws IOException {
        return downloadXmlAsStream(context, url, true, null, null, null, null);
    }

    public static InputStream downloadXmlAsStream(Context context, URL url,
                                                  boolean noEncryptUrl, String userAgent, String cookie)
            throws IOException {
        return downloadXmlAsStream(context, url, noEncryptUrl, userAgent,
                cookie, null, null);
    }

    /**
     * 包装 HTTP request/response 的辅助函数
     *
     * @param context      应用程序上下文
     * @param url          HTTP地址
     * @param noEncryptUrl 是否加密
     * @param userAgent
     * @param cookie
     * @param requestHdrs  用于传入除userAgent和cookie之外的其他header info
     * @param responseHdrs 返回的HTTP response headers;
     * @return
     * @throws IOException
     */
    public static InputStream downloadXmlAsStream(
    /* in */Context context,
    /* in */URL url, boolean noEncryptUrl, String userAgent, String cookie,
    Map<String, String> requestHdrs,
            /* out */NetworkUtils.HttpHeaderInfo responseHdrs) throws IOException {
        if (null == context)
            throw new IllegalArgumentException("context");
        if (null == url)
            throw new IllegalArgumentException("url");

        URL newUrl = url;
        if (!noEncryptUrl)
            newUrl = new URL(encryptURL(url.toString()));

        InputStream responseStream = null;
        try {
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection conn = getHttpUrlConnection(context, newUrl);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty(Network.USER_AGENT, userAgent);
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }
            if (null != requestHdrs) {
                for (String key : requestHdrs.keySet()) {
                    conn.setRequestProperty(key, requestHdrs.get(key));
                }
            }

            if ((responseHdrs != null)
                    && (url.getProtocol().equals("http") || url.getProtocol()
                    .equals("https"))) {
                responseHdrs.ResponseCode = conn.getResponseCode();
                if (responseHdrs.AllHeaders == null)
                    responseHdrs.AllHeaders = new HashMap<String, String>();
                for (int i = 0; ; i++) {
                    String name = conn.getHeaderFieldKey(i);
                    String value = conn.getHeaderField(i);

                    if (name == null && value == null)
                        break;
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value))
                        continue;
                    responseHdrs.AllHeaders.put(name, value);
                }
            }
            responseStream = new DoneHandlerInputStream(conn.getInputStream());
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        }
        return responseStream;
    }

    public static InputStream downloadXmlAsStreamWithoutRedirect(URL url,
                                                                 String userAgent, String cookie) throws IOException {
        InputStream responseStream = null;
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty(Network.USER_AGENT, userAgent);
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            int resCode = conn.getResponseCode();
            if (resCode < 300 || resCode >= 400) {
                responseStream = conn.getInputStream();
            }
            return new DoneHandlerInputStream(responseStream);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String downloadXml(Context context, URL url)
            throws IOException {
        return downloadXml(context, url, false, null, "UTF-8", null);
    }

    public static String downloadXml(Context context, URL url,
                                     boolean noEncryptUrl, String userAgent, String encoding,
                                     String cookie) throws IOException {
        InputStream responseStream = null;
        StringBuilder sbReponse;
        try {
            responseStream = downloadXmlAsStream(context, url, noEncryptUrl,
                    userAgent, cookie);
            sbReponse = new StringBuilder(1024);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseStream, encoding), 1024);
            String line;
            while (null != (line = reader.readLine())) {
                sbReponse.append(line);
                sbReponse.append("\r\n");
            }
        } finally {
            if (null != responseStream) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    MyLog.e(LogTag, "Failed to close responseStream" + e.toString());
                }
            }
        }

        String responseXml = sbReponse.toString();
        return responseXml;
    }

    public static String downloadXml(Context context, URL url,
                                     String userAgent, String cookie, Map<String, String> requestHdrs,
                                     NetworkUtils.HttpHeaderInfo response) throws IOException {
        InputStream responseStream = null;
        StringBuilder sbReponse;
        try {
            responseStream = downloadXmlAsStream(context, url, true, userAgent,
                    cookie, requestHdrs, response);
            sbReponse = new StringBuilder(1024);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseStream, "UTF-8"), 1024);
            String line;
            while (null != (line = reader.readLine())) {
                sbReponse.append(line);
                sbReponse.append("\r\n");
            }
        } finally {
            if (null != responseStream) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    MyLog.e(LogTag,
                            "Failed to close responseStream" + e.toString());
                }
            }
        }

        String responseXml = sbReponse.toString();
        return responseXml;
    }

    /**
     * Based on the doc at
     * "http://diveintomark.org/archives/2004/02/13/xml-media-types" RFC 3023
     * (XML Media Types) defines the interaction between XML and HTTP as it
     * relates to character encoding. HTTP uses MIME to define a method of
     * specifying the character encoding, as part of the Content-Type HTTP
     * header, which looks like this: Content-Type: text/html; charset="utf-8"
     * If no charset is specified, HTTP defaults to iso-8859-1, but only for
     * text/* media types. (Thanks, Ian.) For other media types, the default
     * encoding is undefined, which is where RFC 3023 comes in. In XML, the
     * character encoding is optional and can be given in the XML declaration in
     * the first line of the document, like this: <xml version="1.0"
     * encoding="iso-8859-1"?> If no encoding is given and no Byte Order Mark is
     * present (don’t ask), XML defaults to utf-8. (For those of you smart
     * enough to realize that this is a Catch-22, that an XML processor can’t
     * possibly read the XML declaration to determine the document’s character
     * encoding without already knowing the document’s character encoding,
     * please read Section F of the XML specification and bow in awe at the
     * intricate care with which this issue was thought out.) According to RFC
     * 3023, if the media type given in the Content-Type HTTP header is
     * application/xml, application/xml-dtd,
     * application/xml-external-parsed-entity, or any one of the subtypes of
     * application/xml such as application/atom+xml or application/rss+xml or
     * even application/rdf+xml, then the encoding is: 1. the encoding given in
     * the charset parameter of the Content-Type HTTP header, 2. or the encoding
     * given in the encoding attribute of the XML declaration within the
     * document, 3. or utf-8. On the other hand, if the media type given in the
     * Content-Type HTTP header is text/xml, text/xml-external-parsed-entity, or
     * a subtype like text/AnythingAtAll+xml, then the encoding attribute of the
     * XML declaration within the document is ignored completely, and the
     * encoding is 1. the encoding given in the charset parameter of the
     * Content-Type HTTP header, 2. or us-ascii.
     *
     * @param url
     * @param userAgent
     * @return
     * @throws java.io.IOException
     * @throws IOException
     */
    public static String tryDetectCharsetEncoding(URL url, String userAgent)
            throws IOException {
        if (null == url)
            throw new IllegalArgumentException("url");

        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        if (!TextUtils.isEmpty(userAgent)) {
            conn.setRequestProperty(Network.USER_AGENT, userAgent);
        }

        String ret = null;

        // 1. the encoding given in the charset parameter of the Content-Type
        // HTTP header,
        String contentType = conn.getContentType();
        if (!TextUtils.isEmpty(contentType)) {
            Matcher matcher = ContentTypePattern_Charset.matcher(contentType);
            if (matcher.matches() && matcher.groupCount() >= 3) {
                String charset = matcher.group(2);
                if (!TextUtils.isEmpty(charset)) {
                    ret = charset;
                    MyLog.v(LogTag, "HTTP charset detected is: " + ret);
                }
            }

            // 2. or the encoding given in the encoding attribute of the XML
            // declaration within the document,
            if (TextUtils.isEmpty(ret)) {
                matcher = ContentTypePattern_MimeType.matcher(contentType);
                if (matcher.matches() && matcher.groupCount() >= 2) {
                    String mimetype = matcher.group(1);
                    if (!TextUtils.isEmpty(mimetype)) {
                        mimetype = mimetype.toLowerCase();
                        BufferedReader reader = null;
                        if (mimetype.startsWith("application/")
                                && (mimetype.startsWith("application/xml") || mimetype
                                .endsWith("+xml"))) {
                            InputStream responseStream = null;
                            try {
                                responseStream = new DoneHandlerInputStream(
                                        conn.getInputStream());
                                reader = new BufferedReader(
                                        new InputStreamReader(responseStream));
                                String aLine;
                                while ((aLine = reader.readLine()) != null) {
                                    aLine = aLine.trim();
                                    if (aLine.length() == 0)
                                        continue;

                                    matcher = ContentTypePattern_XmlEncoding
                                            .matcher(aLine);
                                    if (matcher.matches()
                                            && matcher.groupCount() >= 3) {
                                        String charset = matcher.group(2);
                                        if (!TextUtils.isEmpty(charset)) {
                                            ret = charset;
                                            MyLog.v(LogTag, "XML charset detected is: "
                                                    + ret);
                                        }
                                    }
                                    break;
                                }
                            } catch (IOException e) {
                                throw e;
                            } catch (Throwable e) {
                                throw new IOException(e.getMessage());
                            } finally {
                                if (responseStream != null)
                                    responseStream.close();
                                if (null != reader) {
                                    reader.close();
                                }
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    public static final Pattern ContentTypePattern_MimeType = Pattern
            .compile("([^\\s;]+)(.*)");

    public static final Pattern ContentTypePattern_Charset = Pattern.compile(
            "(.*?charset\\s*=[^a-zA-Z0-9]*)([-a-zA-Z0-9]+)(.*)",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern ContentTypePattern_XmlEncoding = Pattern
            .compile(
                    "(\\<\\?xml\\s+.*?encoding\\s*=[^a-zA-Z0-9]*)([-a-zA-Z0-9]+)(.*)",
                    Pattern.CASE_INSENSITIVE);

    public static InputStream getHttpPostAsStream(URL url, String data,
                                                  Map<String, String> headers, String userAgent, String cookie)
            throws IOException {
        if (null == url)
            throw new IllegalArgumentException("url");

        URL newUrl = url;

        InputStream responseStream = null;
        OutputStream outputStream = null;
        try {
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection conn = (HttpURLConnection) newUrl
                    .openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty(Network.USER_AGENT, userAgent);
            }

            if (!TextUtils.isEmpty(cookie)) {
                conn.setRequestProperty("Cookie", cookie);
            }

            outputStream = conn.getOutputStream();
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();
            outputStream = null;

            String responseCode = conn.getResponseCode() + "";
            headers.put("ResponseCode", responseCode);
            for (int i = 0; ; i++) {
                String name = conn.getHeaderFieldKey(i);
                String value = conn.getHeaderField(i);
                if (name == null && value == null) {
                    break;
                }
                headers.put(name, value);
            }
            responseStream = conn.getInputStream();
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                MyLog.e(LogTag, "error while closing strean", e);
            }
        }
        return responseStream;
    }

    public static NetworkUtils.HttpHeaderInfo getHttpHeaderInfo(String urlString,
                                                                String userAgent, String cookie) {
        try {
            URL url = new URL(urlString);
            if (!url.getProtocol().equals("http")
                    && !url.getProtocol().equals("https")) {
                // this is not a http protocol, return
                return null;
            }
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (urlString.indexOf("wap") == -1) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
            } else {
                // this is suspected as a wap site,
                // let's wait for the result a little longer
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
            }
            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty(Network.USER_AGENT, userAgent);
            }

            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            NetworkUtils.HttpHeaderInfo ret = new NetworkUtils.HttpHeaderInfo();
            ret.ResponseCode = conn.getResponseCode();

            ret.UserAgent = userAgent;
            for (int i = 0; ; i++) {
                String name = conn.getHeaderFieldKey(i);
                String value = conn.getHeaderField(i);
                if (name == null && value == null) {
                    break;
                }
                if (name != null && name.equals("content-type")) {
                    ret.ContentType = value;
                }

                if (name != null && name.equals("location")) {
                    URI uri = new URI(value);
                    if (!uri.isAbsolute()) {
                        URI baseUri = new URI(urlString);
                        uri = baseUri.resolve(uri);
                    }
                    ret.realUrl = uri.toString();
                }
            }
            return ret;
        } catch (MalformedURLException e) {
            MyLog.e(LogTag, "Failed to transform URL", e);
        } catch (IOException e) {
            MyLog.e(LogTag, "Failed to get mime type", e);
        } catch (URISyntaxException e) {
            MyLog.e(LogTag, "Failed to parse URI", e);
        } catch (Throwable e) {
            MyLog.e(LogTag, "Failed to get HttpHeaderInfo", e);
        }
        return null;
    }

    public static class HttpHeaderInfo {
        public int ResponseCode;

        public String ContentType;

        public String UserAgent;

        public String realUrl;

        public Map<String, String> AllHeaders;

        @Override
        public String toString() {
            return String.format("resCode = %1$d, headers = %2$s",
                    ResponseCode, AllHeaders.toString());
        }
    }

    public static String fromParamListToString(
            List<NameValuePair> nameValuePairs) {
        StringBuffer params = new StringBuffer();
        for (NameValuePair pair : nameValuePairs) {
            try {
                if (pair.getValue() == null)
                    continue;
                params.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                params.append("=");
                params.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
                params.append("&");
            } catch (UnsupportedEncodingException e) {
                MyLog.d(LogTag, "Failed to convert from param list to string: "
                        + e.toString());
                MyLog.d(LogTag, "pair: " + pair.toString());
                return null;
            }
        }
        if (params.length() > 0) {
            params = params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }

    /**
     * 向服务端提交HttpPost请求 设置为5秒钟连接超时，发送数据超时为15秒
     *
     * @param url
     * : HTTP post的URL地址
     * @param nameValuePairs
     * : HTTP post参数
     * @return JSONObject { "RESPONSE_CODE" : 200, "RESPONSE_BODY" :
     * "Hello, world!" }
     * @throws IOException
     * : 调用过程中可能抛出到exception
     */
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_BODY = "RESPONSE_BODY";

    public static JSONObject doHttpPostWithResponseStatus(Context context,
                                                          String url, List<NameValuePair> nameValuePairs,
                                                          Map<String, String> headers, String userAgent, String cookie) {

        if (null == context)
            throw new IllegalArgumentException("context");

        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url");

        JSONObject result = new JSONObject();

        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, READ_TIMEOUT);
        if (!TextUtils.isEmpty(userAgent)) {
            HttpProtocolParams.setUserAgent(httpParameters, userAgent);
        }
        if (!TextUtils.isEmpty(cookie)) {
            httpParameters.setParameter("Cookie", cookie);
        }
        HttpClient httpClient = new DefaultHttpClient(httpParameters);

        try {
            HttpPost httpPost;
            if (isCmwap(context)) {
                URL _url = new URL(url);
                String cmwapUrl = getCMWapUrl(_url);
                String host = _url.getHost();
                httpPost = new HttpPost(cmwapUrl);
                httpPost.addHeader(CMWAP_HEADER_HOST_KEY, host);
            } else {
                httpPost = new HttpPost(url);
            }
            if (null != nameValuePairs && nameValuePairs.size() != 0)
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                        "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            String strResponseBody = "";
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity body = response.getEntity();
            if (null != body) {
                strResponseBody = EntityUtils.toString(body);
            }

            result.put(Network.RESPONSE_CODE, responseCode);
            result.put(Network.RESPONSE_BODY, strResponseBody);

        } catch (ParseException e) {
            MyLog.e(LogTag, "doHttpPostWithResponseStatus", e);
        } catch (IOException e) {
            MyLog.e(LogTag, "doHttpPostWithResponseStatus", e);
        } catch (JSONException e) {
            MyLog.e(LogTag, "doHttpPostWithResponseStatus", e);
        } finally {
            if (!result.has(Network.RESPONSE_CODE)
                    || !result.has(Network.RESPONSE_BODY)) {
                result.remove(Network.RESPONSE_CODE);
                result.remove(Network.RESPONSE_BODY);
            }
        }

        return result;
    }

    /**
     * 向服务端提交HttpPost请求 设置为5秒钟连接超时，发送数据不超时；
     *
     * @param url            : HTTP post的URL地址
     * @param nameValuePairs : HTTP post参数
     * @throws IOException : 调用过程中可能抛出到exception
     * @return: 如果post
     * response代码不是2xx，表示发生了错误，返回null。否则返回服务器返回的数据（如果服务器没有返回任何数据，返回""）；
     */
    public static String doHttpPost(Context context, String url,
                                    List<NameValuePair> nameValuePairs) throws IOException {
        return doHttpPost(context, url, nameValuePairs, null, null, null);
    }

    /**
     * Someone replaces the http handler with a bad one, we should use the default one supported by android.
     *
     * @param url
     * @return
     * @throws MalformedURLException
     */
    private static URL getDefaultStreamHandlerURL(String url) throws MalformedURLException {
        URL urlInstance = new URL(url);
        return urlInstance;
    }

    public static String doHttpPost(Context context, String url,
                                    List<NameValuePair> nameValuePairs, HttpHeaderInfo responseHdrs,
                                    String userAgent, String cookie) throws IOException {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url");

        HttpURLConnection conn = null;
        String responseContent = null;
        OutputStream outputStream = null;
        BufferedReader rd = null;
        try {
            conn = getHttpUrlConnection(context, getDefaultStreamHandlerURL(url));
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty(Network.USER_AGENT, userAgent);
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            String strParams = fromParamListToString(nameValuePairs);
            if (null == strParams) {
                throw new IllegalArgumentException("nameValuePairs");
            }

            conn.setDoOutput(true);
            byte[] b = strParams.getBytes();
            outputStream = conn.getOutputStream();
            outputStream.write(b, 0, b.length);
            outputStream.flush();
            outputStream.close();
            outputStream = null;
            int statusCode = conn.getResponseCode();
            MyLog.d(LogTag, "Http POST Response Code: " + statusCode);
            if (responseHdrs != null) {
                responseHdrs.ResponseCode = statusCode;
                if (responseHdrs.AllHeaders == null)
                    responseHdrs.AllHeaders = new HashMap<String, String>();
                for (int i = 0; ; i++) {
                    String name = conn.getHeaderFieldKey(i);
                    String value = conn.getHeaderField(i);
                    if (name == null && value == null) {
                        break;
                    }
                    responseHdrs.AllHeaders.put(name, value);
                    i++;
                }
            }

            rd = new BufferedReader(new InputStreamReader(
                    new DoneHandlerInputStream(conn.getInputStream())));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            String crlf = System.getProperty("line.separator");
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempStr.append(crlf);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            rd = null;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (rd != null) {
                    rd.close();
                }
            } catch (IOException e) {
                MyLog.e(LogTag, "error while closing strean", e);
            }
            try{
                if (conn != null) {
                    conn.disconnect();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    //只返回request结果 不反悔具体内容
    public static boolean doHttpPostReqOnly(Context context, String url, List<NameValuePair> nameValuePairs) throws IOException {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url");

        HttpURLConnection conn = null;
        OutputStream outputStream = null;
        int statusCode;
        try {
            conn = getHttpUrlConnection(context, getDefaultStreamHandlerURL(url));
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");

            String strParams = fromParamListToString(nameValuePairs);
            if (null == strParams) {
                throw new IllegalArgumentException("nameValuePairs");
            }

            conn.setDoOutput(true);
            byte[] b = strParams.getBytes();
            outputStream = conn.getOutputStream();
            outputStream.write(b, 0, b.length);
            outputStream.flush();
            outputStream.close();
            outputStream = null;
            statusCode = conn.getResponseCode();
            MyLog.d(LogTag, "Http POST Response Code: " + statusCode);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                MyLog.e(LogTag, "error while closing strean", e);
            }
            try{
                if (conn != null) {
                    conn.disconnect();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return statusCode == HttpURLConnection.HTTP_OK;
    }

    /**
     * @param strUrl 要加密的URL string
     * @return 获取加密后的URL string
     */
    public static String encryptURL(String strUrl) {
        if (!TextUtils.isEmpty(strUrl)) {
            new String();
            String strTemp = String.format(
                    "%sbe988a6134bc8254465424e5a70ef037", strUrl);
            return String.format("%s&key=%s", strUrl, MD5.MD5_32(strTemp));
        } else
            return null;
    }

    public interface PostDownloadHandler {
        void OnPostDownload(boolean sucess);
    }

    /**
     * 开始下载远程文件到指定输出流
     *
     * @param url     远程文件地址
     * @param output  输出流
     * @param handler 下载成功或者失败的处理
     */
    public static void beginDownloadFile(String url, OutputStream output,
                                         PostDownloadHandler handler) {
        DownloadTask task = new DownloadTask(url, output, handler);
        task.execute();
    }

    public static void beginDownloadFile(String url, OutputStream output,
                                         Context context, boolean bOnlyWifi, PostDownloadHandler handler) {
        DownloadTask task = new DownloadTask(url, output, handler, bOnlyWifi,
                context);
        task.execute();
    }

    /**
     * 下载远程文件到指定输出流
     *
     * @param urlStr 远程文件地址
     * @param output 输出流
     * @return 成功与否
     */
    public static boolean downloadFile(String urlStr, OutputStream output) {
        return downloadFile(urlStr, output, false, null);
    }

    public static boolean downloadFile(String urlStr, OutputStream output,
                                       boolean bOnlyWifi, Context context) {
        return downloadFile(urlStr,null,output,bOnlyWifi,context);
    }

    public static boolean downloadFile(String urlStr,String host, OutputStream output,
                                       boolean bOnlyWifi, Context context) {
        boolean bCanceled = false;

        InputStream input = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            if(!TextUtils.isEmpty(host)){
                conn.setRequestProperty(Network.CMWAP_HEADER_HOST_KEY, host);
                conn.setRequestProperty("host", host);
            }
            HttpURLConnection.setFollowRedirects(true);
            conn.connect();
            input = conn.getInputStream();

            byte[] buffer = new byte[1024];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                if (bOnlyWifi && context != null && !isWIFIConnected(context)) {
                    bCanceled = true;
                    break;
                }
            }
            return !bCanceled;
        } catch (IOException e) {
            MyLog.e(LogTag, "error while download file" + e);
        } catch (Throwable e) {
            MyLog.e(LogTag, "error while download file" + e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    //
                }
            }
        }

        return false;
    }

    /**
     * 下载远程文件到指定输出流
     *
     * @param urlStr 远程文件地址
     * @param output
     * @return 成功与否
     */
    public static boolean downloadFile(String urlStr, OutputStream output,
                                       Context context) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(urlStr);
            if (Network.isCmwap(context)) {
                HttpURLConnection.setFollowRedirects(false);
                String cmwapUrl = Network.getCMWapUrl(url);
                String host = url.getHost();
                conn = (HttpURLConnection) new URL(cmwapUrl).openConnection();
                conn.setRequestProperty(Network.CMWAP_HEADER_HOST_KEY, host);
                int resCode = conn.getResponseCode();
                while (resCode >= 300 && resCode < 400) {
                    String redirectedUrl = conn.getHeaderField("location");
                    if (TextUtils.isEmpty(redirectedUrl)) {
                        break;
                    }
                    url = new URL(redirectedUrl);
                    cmwapUrl = Network.getCMWapUrl(url);
                    host = url.getHost();
                    conn = (HttpURLConnection) new URL(cmwapUrl)
                            .openConnection();
                    conn.setRequestProperty(Network.CMWAP_HEADER_HOST_KEY, host);
                    resCode = conn.getResponseCode();
                }
            } else {
                conn = (HttpURLConnection) url.openConnection();
                HttpURLConnection.setFollowRedirects(true);
            }

            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.connect();
            InputStream input = conn.getInputStream();

            byte[] buffer = new byte[1024];
            int count;

            while ((count = input.read(buffer)) > 0) {
                output.write(buffer, 0, count);
            }

            input.close();
            output.close();
            return true;
        } catch (IOException e) {
            MyLog.e(LogTag, "error while download file" + e);
        } catch (Throwable e) {
            MyLog.e(LogTag, "error while download file" + e);
        }

        return false;
    }

    public static String uploadFile(String url, File file, String fileKey)
            throws IOException {

        if (!file.exists()) {
            return null;
        }
        String filename = file.getName();

        HttpURLConnection conn = null;

        final String lineEnd = "\r\n";
        final String twoHyphens = "--";
        final String boundary = "*****";

        FileInputStream fileInputStream = null;
        DataOutputStream dos = null;
        BufferedReader rd = null;

        try {
            URL _url = new URL(url);
            conn = (HttpURLConnection) _url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);

            // Allow Inputs
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            final int EXTRA_LEN = 77; // 除去文件名和文件内容之外，所有内容的length
            int len = EXTRA_LEN + filename.length() + (int) file.length()
                    + fileKey.length();
            conn.setFixedLengthStreamingMode(len);

            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + fileKey
                    + "\";filename=\"" + file.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // read file and write it into form...
            fileInputStream = new FileInputStream(file);
            int bytesRead = -1;
            final int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                dos.flush();
            }
            // send multi-part form data necessary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens);
            dos.writeBytes(boundary);
            dos.writeBytes(twoHyphens);
            dos.writeBytes(lineEnd);

            // flush streams
            dos.flush();
            StringBuffer sb = new StringBuffer();
            rd = new BufferedReader(new InputStreamReader(
                    new DoneHandlerInputStream(conn.getInputStream())));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (dos != null) {
                    dos.close();
                }
                if (rd != null) {
                    rd.close();
                }
            } catch (IOException e) {
                MyLog.e(LogTag, "error while closing strean", e);
            }
        }
    }

    private static class DownloadTask extends AsyncTask<Void, Void, Boolean> {
        private String url;

        private OutputStream output;

        private PostDownloadHandler handler;

        private boolean bOnlyWifi;

        private Context mContext;

        public DownloadTask(String url, OutputStream output,
                            PostDownloadHandler handler) {
            this(url, output, handler, false, null);
        }

        public DownloadTask(String url, OutputStream output,
                            PostDownloadHandler handler, boolean bOnlyWifi, Context context) {
            this.url = url;
            this.output = output;
            this.handler = handler;
            this.bOnlyWifi = bOnlyWifi;
            this.mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return Network.downloadFile(this.url, this.output, this.bOnlyWifi,
                    this.mContext);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            this.handler.OnPostDownload(result);
        }
    }

    public static int getActiveNetworkType(Context context) {
        int defaultValue = -1;
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            return defaultValue;
        }
        if (cm == null)
            return defaultValue;
        NetworkInfo info = null;
        try {
            info = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            // ignore
            return defaultValue;
        }
        if (info == null)
            return defaultValue;
        return info.getType();
    }

    public static String getActiveNetworkName(final Context context) {
        String defaultValue = "null";
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            return defaultValue;
        }
        if (cm == null)
            return defaultValue;
        NetworkInfo info = null;
        try {
            info = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            // ignore
            return defaultValue;
        }
        if (info == null)
            return defaultValue;
        if (TextUtils.isEmpty(info.getSubtypeName()))
            return info.getTypeName();
        return String
                .format("%s-%s", info.getTypeName(), info.getSubtypeName());
    }

    public static boolean isCmwap(Context context) {
        // 如果不是中国sim卡，直接返回否
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String countryISO = tm.getSimCountryIso();
        if (!"CN".equalsIgnoreCase(countryISO)) {
            return false;
        }
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            return false;
        }
        if (cm == null)
            return false;
        NetworkInfo info = null;
        try {
            info = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            // ignore
            return false;
        }
        if (info == null)
            return false;
        String extraInfo = info.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3))
            return false;
        if (extraInfo.contains("ctwap")) {
            return false;
        }
        return extraInfo.regionMatches(true, extraInfo.length() - 3, "wap", 0,
                3);
    }

    public static boolean isCtwap(Context context) {
        // 如果不是中国sim卡，直接返回否
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String countryISO = tm.getSimCountryIso();
        if (!"CN".equalsIgnoreCase(countryISO)) {
            return false;
        }
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            return false;
        }
        if (cm == null)
            return false;
        NetworkInfo info = null;
        try {
            info = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            // ignore
            return false;
        }
        if (info == null)
            return false;
        String extraInfo = info.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3))
            return false;
        return extraInfo.contains("ctwap");

    }

    public static HttpURLConnection getHttpUrlConnection(Context context,
                                                         URL url) throws IOException {
        if (!"http".equals(url.getProtocol())) {
            return (HttpURLConnection) url.openConnection();
        }
        if (isCtwap(context)) {
            java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,
                    new InetSocketAddress("10.0.0.200", 80));
            return (HttpURLConnection) url.openConnection(proxy);
        }
        if (!isCmwap(context)) {
            return (HttpURLConnection) url.openConnection();
        } else {
            String host = url.getHost();
            String cmwapUrl = getCMWapUrl(url);
            URL gatewayUrl = new URL(cmwapUrl);
            HttpURLConnection conn = (HttpURLConnection) gatewayUrl
                    .openConnection();
            conn.addRequestProperty(CMWAP_HEADER_HOST_KEY, host);
            return conn;
        }
    }

    public static String getCMWapUrl(URL oriUrl) {
        StringBuilder gatewayBuilder = new StringBuilder();
        gatewayBuilder.append(oriUrl.getProtocol()).append("://")
                .append(CMWAP_GATEWAY).append(oriUrl.getPath());
        if (!TextUtils.isEmpty(oriUrl.getQuery())) {
            gatewayBuilder.append("?").append(oriUrl.getQuery());
        }
        return gatewayBuilder.toString();
    }

    /**
     * This input stream won't read() after the underlying stream is exhausted.
     * http://code.google.com/p/android/issues/detail?id=14562
     */
    public final static class DoneHandlerInputStream extends FilterInputStream {
        private boolean done;

        public DoneHandlerInputStream(InputStream stream) {
            super(stream);
        }

        @Override
        public int read(byte[] bytes, int offset, int count) throws IOException {
            if (!done) {
                int result = super.read(bytes, offset, count);
                if (result != -1) {
                    return result;
                }
            }
            done = true;
            return -1;
        }
    }

    /**
     * 注意：方法返回的是当前有没有active的网络，而不是当前有没有可以连接得上的网络。不要修改方法的实现。
     *
     * @param context
     * @return
     */
    public static boolean hasNetwork(final Context context) {
        return Network.getActiveNetworkType(context) >= 0;
    }

    public static String getLocalNetworkType(final Context context) {
        if (isWIFIConnected(context)) {
            return NETWORK_TYPE_WIFI;
        }

        String extraInfo = "unknown";
        final ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo mobNetInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobNetInfo != null) {
            extraInfo = mobNetInfo.getExtraInfo();
        }
        if (PhoneNumUtils.isChinaTelecom(context)) {
            extraInfo = NETWORK_TYPE_CHINATELECOM;
        }
        return extraInfo;
    }

    public static boolean isWIFIConnected(final Context context) {
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            return false;
        }

        if (cm == null) {
            return false;
        }

        NetworkInfo info = null;
        try {
            info = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            // ignore
            return false;
        }
        if (info == null) {
            return false;
        }

        return ConnectivityManager.TYPE_WIFI == info.getType();
    }

    public static String getActiveConnPoint(final Context context) {
        if (isWIFIConnected(context)) {
            return "wifi";
        } else {
            ConnectivityManager cm = null;
            try {
                cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Exception e) {
                return "";
            }
            if (cm == null) {
                return "";
            }

            NetworkInfo info = null;
            try {
                info = cm.getActiveNetworkInfo();
            } catch (Exception e) {
                // ignore
                return "";
            }
            if (info == null) {
                return "";
            }
            return (info.getTypeName() + "-" + info.getSubtypeName() + "-" + info
                    .getExtraInfo()).toLowerCase();
        }
    }

    public static String doHttpGet(Context context, String strUrl)
            throws IOException {
        return doHttpGet(context, strUrl, true, null, null, null, null);
    }

    public static String doHttpGet(Context context, String strUrl, boolean noEncryptUrl, String userAgent, String cookie, Map<String, String> requestHdrs,
                                   NetworkUtils.HttpHeaderInfo responseHdrs) throws IOException {
        String content = "";
        if (null == context)
            throw new IllegalArgumentException("context");
        if (null == strUrl)
            throw new IllegalArgumentException("url");
        URL url = getDefaultStreamHandlerURL(strUrl);
        URL newUrl = url;
        if (!noEncryptUrl)
            newUrl = new URL(encryptURL(url.toString()));

        InputStream responseStream = null;
        ByteArrayOutputStream baos = null;
        try {
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection conn = getHttpUrlConnection(context, newUrl);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty(Network.USER_AGENT, userAgent);
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }
            if (null != requestHdrs) {
                for (String key : requestHdrs.keySet()) {
                    conn.setRequestProperty(key, requestHdrs.get(key));
                }
            }

            if ((responseHdrs != null)
                    && (url.getProtocol().equals("http") || url.getProtocol()
                    .equals("https"))) {
                responseHdrs.ResponseCode = conn.getResponseCode();
                if (responseHdrs.AllHeaders == null)
                    responseHdrs.AllHeaders = new HashMap<String, String>();
                for (int i = 0; ; i++) {
                    String name = conn.getHeaderFieldKey(i);
                    String value = conn.getHeaderField(i);

                    if (name == null && value == null)
                        break;
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value))
                        continue;
                    responseHdrs.AllHeaders.put(name, value);
                }
            }
            responseStream = conn.getInputStream();
            int i = -1;
            baos = new ByteArrayOutputStream();
            while ((i = responseStream.read()) != -1) {
                baos.write(i);
            }
            content = baos.toString();
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e.getMessage());
        } finally {
            try {
                if (responseStream != null) {
                    responseStream.close();
                }
            } catch (IOException e) {
                MyLog.e(LogTag, "error while closing responseStream", e);
            }
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                MyLog.e(LogTag, "error while closing baos", e);
            }
        }
        return content;
    }

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
//        final String TAG = "trustAllHosts";
//        // Create a trust manager that does not validate certificate chains
//        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                return new java.security.cert.X509Certificate[]{};
//            }
//
//            @Override
//            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                Log.i(TAG, "checkClientTrusted");
//            }
//
//            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                Log.i(TAG, "checkServerTrusted");
//            }
//        }};
//
//        // Install the all-trusting trust manager
//        try {
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static String httpGet(String httpUrl) {
        BufferedReader input = null;
        StringBuilder sb = null;
        URL url = null;
        HttpURLConnection con = null;
        try {
            url = new URL(httpUrl);
            try {
                // trust all hosts
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                if (url.getProtocol().toLowerCase().equals("https")) {
                    https.setHostnameVerifier(DO_NOT_VERIFY);
                    con = https;
                } else {
                    con = (HttpURLConnection) url.openConnection();
                }
                input = new BufferedReader(new InputStreamReader(con.getInputStream()));
                sb = new StringBuilder();
                String s;
                while ((s = input.readLine()) != null) {
                    sb.append(s).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } finally {
            // close buffered
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // disconnecting releases the resources held by a connection so they may be closed or reused
            if (con != null) {
                con.disconnect();
            }
        }

        return sb == null ? null : sb.toString();
    }

    public static boolean isCmwap() {
        // 如果不是中国sim卡，直接返回否
        if (null == GlobalData.app()) {
            MyLog
                    .d("NetWork", " isCmwap Globaldata.app = null ");
        }
        TelephonyManager tm = (TelephonyManager) GlobalData.app()
                .getSystemService(Context.TELEPHONY_SERVICE);

        if (null == tm) {
            MyLog.d("NetWork", " isCmwap tm= null ");
        }

        String countryISO = tm.getSimCountryIso();
        if (!"CN".equalsIgnoreCase(countryISO)) {
            return false;
        }
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) GlobalData.app().getSystemService(
                    Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            return false;
        }
        if (cm == null)
            return false;
        NetworkInfo info = null;
        try {
            info = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            // ignore
            return false;
        }
        if (info == null)
            return false;
        String extraInfo = info.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3))
            return false;
        if (extraInfo.contains("ctwap")) {
            return false;
        }
        return extraInfo.regionMatches(true, extraInfo.length() - 3, "wap", 0,
                3);
    }
}
