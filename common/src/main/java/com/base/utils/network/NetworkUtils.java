package com.base.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.base.log.MyLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtils {
    private static final String LogTag = NetworkUtils.class.getName();

    /**
     * user agent for chrome browser on PC
     */
    public static final String UserAgent_PC_Chrome_6_0_464_0 = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.464.0 Safari/534.3";

    public static final String UserAgent_PC_Chrome = UserAgent_PC_Chrome_6_0_464_0;

    public static final String CMWAP_GATEWAY = "10.0.0.172";

    public static final int CMWAP_PORT = 80;

    public static final String CMWAP_HEADER_HOST_KEY = "X-Online-Host";

    public static final int CONNECTION_TIMEOUT = 10 * 1000;

    public static final int READ_TIMEOUT = 15 * 1000;

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
     */
    public static String tryDetectCharsetEncoding(URL url, String userAgent) throws IOException {
        if (null == url)
            throw new IllegalArgumentException("url");

        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        if (!TextUtils.isEmpty(userAgent)) {
            conn.setRequestProperty(CoreProtocolPNames.USER_AGENT, userAgent);
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
                    Log.v(LogTag, "HTTP charset detected is: " + ret);
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
                        if (mimetype.startsWith("application/")
                                && (mimetype.startsWith("application/xml") || mimetype.endsWith("+xml"))) {
                            InputStream responseStream = null;
                            try {
                                Log.i(LogTag, "tryDetectCharsetEncoding, getInputStream from http request " + url);
                                responseStream = new DoneHandlerInputStream(conn.getInputStream());
                                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                                String aLine;
                                while ((aLine = reader.readLine()) != null) {
                                    aLine = aLine.trim();
                                    if (aLine.length() == 0)
                                        continue;

                                    matcher = ContentTypePattern_XmlEncoding.matcher(aLine);
                                    if (matcher.matches() && matcher.groupCount() >= 3) {
                                        String charset = matcher.group(2);
                                        if (!TextUtils.isEmpty(charset)) {
                                            ret = charset;
                                            Log.v(LogTag, "XML charset detected is: " + ret);
                                        }
                                    }
                                    break;
                                }
                            } finally {
                                if (responseStream != null)
                                    responseStream.close();
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    public static final Pattern ContentTypePattern_MimeType = Pattern.compile("([^\\s;]+)(.*)");

    public static final Pattern ContentTypePattern_Charset = Pattern.compile(
            "(.*?charset\\s*=[^a-zA-Z0-9]*)([-a-zA-Z0-9]+)(.*)", Pattern.CASE_INSENSITIVE);

    public static final Pattern ContentTypePattern_XmlEncoding = Pattern.compile(
            "(\\<\\?xml\\s+.*?encoding\\s*=[^a-zA-Z0-9]*)([-a-zA-Z0-9]+)(.*)", Pattern.CASE_INSENSITIVE);

    public static InputStream getHttpPostAsStream(URL url, String data, Map<String, String> headers, String userAgent,
                                                  String cookie) throws IOException {
        if (null == url)
            throw new IllegalArgumentException("url");

        URL newUrl = url;

        InputStream responseStream = null;
        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection conn = (HttpURLConnection) newUrl.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        if (!TextUtils.isEmpty(userAgent)) {
            conn.setRequestProperty(CoreProtocolPNames.USER_AGENT, userAgent);
        }

        if (!TextUtils.isEmpty(cookie)) {
            conn.setRequestProperty("Cookie", cookie);
        }

        conn.getOutputStream().write(data.getBytes());
        conn.getOutputStream().flush();
        conn.getOutputStream().close();

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
        Log.i(LogTag, "getHttpPostAsStream, getInputStream from http request " + url);
        responseStream = conn.getInputStream();
        return responseStream;
    }

    public static HttpHeaderInfo getHttpHeaderInfo(String urlString, String userAgent, String cookie) {
        try {
            URL url = new URL(urlString);
            if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
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
                conn.setRequestProperty(CoreProtocolPNames.USER_AGENT, userAgent);
            }

            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            HttpHeaderInfo ret = new HttpHeaderInfo();
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
            Log.e(LogTag, "Failed to transform URL", e);
        } catch (IOException e) {
            Log.e(LogTag, "Failed to get mime type", e);
        } catch (URISyntaxException e) {
            Log.e(LogTag, "Failed to parse URI", e);
        }
        return null;
    }

    public static class HttpHeaderInfo {
        public int ResponseCode;

        public String ContentType;

        public String UserAgent;

        public String realUrl;

        public Map<String, String> AllHeaders;
    }

    public static String fromParamListToString(List<NameValuePair> nameValuePairs) {
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
                Log.i(LogTag, "Failed to convert from param list to string: " + e.toString());
                Log.i(LogTag, "pair: " + pair.toString());
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

    public static JSONObject doHttpPostWithResponseStatus(Context context, String url,
                                                          List<NameValuePair> nameValuePairs, Map<String, String> headers, String userAgent, String cookie) {

        if (null == context)
            throw new IllegalArgumentException("context");

        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url");

        JSONObject result = new JSONObject();

        BasicHttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
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
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            Log.i(LogTag, "execute http request " + httpPost.getURI());
            HttpResponse response = httpClient.execute(httpPost);
            String strResponseBody = "";
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity body = response.getEntity();
            if (null != body) {
                strResponseBody = EntityUtils.toString(body);
            }

            result.put(RESPONSE_CODE, responseCode);
            result.put(RESPONSE_BODY, strResponseBody);

        } catch (ParseException e) {
            Log.e(LogTag, "doHttpPostWithResponseStatus", e);
        } catch (IOException e) {
            Log.e(LogTag, "doHttpPostWithResponseStatus", e);
        } catch (JSONException e) {
            Log.e(LogTag, "doHttpPostWithResponseStatus", e);
        } finally {
            if (!result.has(RESPONSE_CODE) || !result.has(RESPONSE_BODY)) {
                result.remove(RESPONSE_CODE);
                result.remove(RESPONSE_BODY);
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
    public static String doHttpPost(Context context, String url, List<NameValuePair> nameValuePairs) throws IOException {
        return doHttpPost(context, url, nameValuePairs, null, null, null);
    }

    public static String doHttpPost(Context context, String url, List<NameValuePair> nameValuePairs,
                                    Map<String, String> headers, String userAgent, String cookie) throws IOException {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url");
        String responseContent = "";
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            conn = getHttpUrlConnection(context, new URL(url));
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty(CoreProtocolPNames.USER_AGENT, userAgent);
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            String strParams = fromParamListToString(nameValuePairs);
            Log.i("Dozen", " url : " + url + ", str params : " + strParams);
            if (null == strParams) {
                throw new IllegalArgumentException("nameValuePairs");
            }

            conn.setDoOutput(true);
            byte[] b = strParams.getBytes();
            conn.getOutputStream().write(b, 0, b.length);
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            int statusCode = conn.getResponseCode();
            Log.i(LogTag, "http POST Response Code: " + statusCode);
            is = conn.getInputStream();
            if (null != is) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(new DoneHandlerInputStream(is)));
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
                if (headers != null) {
                    for (int i = 0; ; i++) {
                        String name = conn.getHeaderFieldKey(i);
                        String value = conn.getHeaderField(i);
                        if (name == null && value == null) {
                            break;
                        }
                        headers.put(name, value);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            MyLog.e(e);
        } finally {
            if (null != is) {
                is.close();
            }
            if (null != conn) {
                conn.disconnect();
                conn = null;
            }
        }
        return responseContent;
    }

//    /**
//     * 下载远程文件到指定输出流
//     *
//     * @param url
//     *            远程文件地址
//     * @param output
//     *            输出流
//     * @return 成功与否
//     */
//    public static boolean downloadFile(String urlStr, OutputStream output) {
//        return downloadFile(urlStr, output, false, null);
//    }

////    public static boolean downloadFile(String urlStr, OutputStream output, boolean bOnlyWifi, Context context) {
////        boolean bCanceled = false;
////
////        InputStream input = null;
////        try {
////            URL url = new URL(urlStr);
////            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
////            conn.setConnectTimeout(CONNECTION_TIMEOUT);
////            conn.setReadTimeout(READ_TIMEOUT);
////            HttpURLConnection.setFollowRedirects(true);
////            conn.connect();
////            Log.i(LogTag, "downloadFile, connect http request " + conn.getURL());
////            input = conn.getInputStream();
////
////            byte[] buffer = new byte[1024];
////            int count;
////
////            while ((count = input.read(buffer)) != -1) {
////                output.write(buffer, 0, count);
////                if (bOnlyWifi && context != null && !isWifi(context)) {
////                    bCanceled = true;
////                    break;
////                }
////            }
////            return !bCanceled;
////        } catch (IOException e) {
////            Log.e(LogTag, "error while download file" + e);
////        } finally {
////            if (input != null) {
////                try {
////                    input.close();
////                } catch (IOException e) {
////                    //
////                }
////            }
////            if (output != null) {
////                try {
////                    output.close();
////                } catch (IOException e) {
////                    //
////                }
////            }
////        }
////
////        return false;
////    }
//
//    /**
//     * 下载远程文件到指定输出流
//     *
//     * @param url
//     *            远程文件地址
//     * @param output
//     *            输出流
//     * @return 成功与否
//     */
//    public static boolean downloadFile(String urlStr, OutputStream output, Context context) {
//        try {
//            HttpURLConnection conn = null;
//            URL url = new URL(urlStr);
//            if (isCmwap(context)) {
//                HttpURLConnection.setFollowRedirects(false);
//                String cmwapUrl = getCMWapUrl(url);
//                String host = url.getHost();
//                conn = (HttpURLConnection) new URL(cmwapUrl).openConnection();
//                conn.setRequestProperty(CMWAP_HEADER_HOST_KEY, host);
//                int resCode = conn.getResponseCode();
//                while (resCode >= 300 && resCode < 400) {
//                    String redirectedUrl = conn.getHeaderField("location");
//                    if (TextUtils.isEmpty(redirectedUrl)) {
//                        break;
//                    }
//                    url = new URL(redirectedUrl);
//                    cmwapUrl = getCMWapUrl(url);
//                    host = url.getHost();
//                    conn = (HttpURLConnection) new URL(cmwapUrl).openConnection();
//                    conn.setRequestProperty(CMWAP_HEADER_HOST_KEY, host);
//                    resCode = conn.getResponseCode();
//                }
//            } else {
//                conn = (HttpURLConnection) url.openConnection();
//                HttpURLConnection.setFollowRedirects(true);
//            }
//
//            conn.setConnectTimeout(CONNECTION_TIMEOUT);
//            conn.setReadTimeout(READ_TIMEOUT);
//            conn.connect();
//            Log.i(LogTag, "connect http request " + conn.getURL());
//            InputStream input = conn.getInputStream();
//
//            byte[] buffer = new byte[1024];
//            int count;
//
//            while ((count = input.read(buffer)) > 0) {
//                output.write(buffer, 0, count);
//            }
//
//            input.close();
//            output.close();
//            return true;
//        } catch (IOException e) {
//            Log.e(LogTag, "error while download file" + e);
//        }
//
//        return false;
//    }

    public static String KEY_SUFFIX = "#XiaomiKey123";


    public static String uploadFile(String url, File file, String fileKey) throws IOException {

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
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            final int EXTRA_LEN = 77; // 除去文件名和文件内容之外，所有内容的length
            int len = EXTRA_LEN + filename.length() + (int) file.length() + fileKey.length();
            conn.setFixedLengthStreamingMode(len);

            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + fileKey + "\";filename=\"" + file.getName()
                    + "\"" + lineEnd);
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
            rd = new BufferedReader(new InputStreamReader(new DoneHandlerInputStream(conn.getInputStream())));
            Log.i(LogTag, "uploadFile, getInputStream from http request " + conn.getURL());
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
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
                Log.e(LogTag, "error while closing strean", e);
            }
        }
    }

    public static int getActiveNetworkType(Context context) {
        int defaultValue = -1;
        if(context==null){
            return defaultValue;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return defaultValue;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
            return defaultValue;
        return info.getType();
    }

    public static String getActiveNetworkName(final Context context) {
        String defaultValue = "null";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return defaultValue;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
            return defaultValue;
        if (TextUtils.isEmpty(info.getSubtypeName()))
            return info.getTypeName();
        return String.format("%s-%s", info.getTypeName(), info.getSubtypeName());
    }

    public static boolean isWifi(Context context) {
        return (getActiveNetworkType(context) == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean hasNetwork(Context context) {
        return (getActiveNetworkType(context) != -1);
    }

    public static boolean isCmwap(Context context) {
        // 如果不是中国sim卡，直接返回否
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryISO = tm.getSimCountryIso();
        if (!"CN".equalsIgnoreCase(countryISO)) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
            return false;
        String extraInfo = info.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3))
            return false;
        if (extraInfo.contains("ctwap")) {
            return false;
        }
        return extraInfo.regionMatches(true, extraInfo.length() - 3, "wap", 0, 3);
    }

    public static boolean isCtwap(Context context) {
        // 如果不是中国sim卡，直接返回否
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryISO = tm.getSimCountryIso();
        if (!"CN".equalsIgnoreCase(countryISO)) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
            return false;
        String extraInfo = info.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3))
            return false;
        return extraInfo.contains("ctwap");

    }

    public static HttpURLConnection getHttpUrlConnection(Context context, URL url) throws IOException {
        if (isCtwap(context)) {
            java.net.Proxy proxy = new java.net.Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.200", 80));
            return (HttpURLConnection) url.openConnection(proxy);
        }
        if (!isCmwap(context)) {
            return (HttpURLConnection) url.openConnection();
        } else {
            String host = url.getHost();
            String cmwapUrl = getCMWapUrl(url);
            URL gatewayUrl = new URL(cmwapUrl);
            HttpURLConnection conn = (HttpURLConnection) gatewayUrl.openConnection();
            conn.addRequestProperty(CMWAP_HEADER_HOST_KEY, host);
            return conn;
        }
    }

    public static String getCMWapUrl(URL oriUrl) {
        StringBuilder gatewayBuilder = new StringBuilder();
        gatewayBuilder.append(oriUrl.getProtocol()).append("://").append(CMWAP_GATEWAY).append(oriUrl.getPath());
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

    public static final int UNKNOWN = 0;
    public static final int CHINA_MOBILE = 1; // China Mobile 中国移动
    public static final int CHINA_UNICOM = 2; // China Unicom 中国联通
    public static final int CHINA_TELECOM = 3; // China Telecom 中国电信

    //http://wiki.n.miui.com/pages/viewpage.action?pageId=7362162
    public static int getNetType(Context context) {
        int status = 0;
        NetworkInfo ni = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (ni == null) {
            return status;
        }
        status = ni.getType();
        return status + 1;  //因为status有-1，不方便分析，所以返回值统一加1
    }

    public static int getNetSubType(Context context) {
        int status = 0;
        TelephonyManager teleManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (teleManager == null) {
            return status;
        }
        status = teleManager.getNetworkType();
        return status;
    }

    /**
     * 获得运营商信息
     *
     * @param context
     * @return
     */
    public static int getCarrier(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String IMSI = telephonyManager.getSubscriberId() == null ? ""
                : telephonyManager.getSubscriberId();

        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")
                || IMSI.startsWith("46007")) {
            return 1;// China Mobile 中国移动
        } else if (IMSI.startsWith("46001") || IMSI.startsWith("46006")) {
            return 2;// China Unicom 中国联通
        } else if (IMSI.startsWith("46003") || IMSI.startsWith("46005")) {
            return 3;// China Telecom 中国电信
        }
        return 0;
    }

    public static List<String> getAddressByHost(String host) {
        List<String> address = new ArrayList<>();
        if (!TextUtils.isEmpty(host)) {

            //DnsPod
            String dnsPodStr = DnsPodUtils.getAddressByHostDnsPod(host);
            if (!TextUtils.isEmpty(dnsPodStr)) {
                dnsPodStr = dnsPodStr.split(",")[0];
                String[] dnsPodStrs = dnsPodStr.split(";");
                MyLog.w("HttpDns", "domain=" + host + ",ipsList=" + dnsPodStr);
                for (int i = dnsPodStrs.length - 1; i >= 0; i--) {
                    String ipStr = dnsPodStrs[i];
                    if (!address.contains(ipStr)) {
                        address.add(0, ipStr);
                    }
                }
            }

            InetAddress[] addr = null;
            try {
                addr = InetAddress.getAllByName(host);
                if (addr != null && addr.length > 0) {
                    for (InetAddress item : addr) {
                        String hostAddress = item.getHostAddress();
                        if (!address.contains(hostAddress)) {
                            address.add(hostAddress);
                        }
                    }
                }
            } catch (Exception e) {
                MyLog.e("getAddressByHost error = " + e);
            }

        }
        return address;
    }

    public static List<String> getAddressByHost(String host, boolean forceUseDnsPod) {
        List<String> address = new ArrayList<>();
        if (!TextUtils.isEmpty(host)) {
            InetAddress[] addr = null;
            try {
                addr = InetAddress.getAllByName(host);
                if (addr != null && addr.length > 0) {
                    String localDnsStr = "";
                    for (InetAddress item : addr) {
                        address.add(item.getHostAddress());
                        localDnsStr += localDnsStr.isEmpty() ? item.getHostAddress() : ";" + item.getHostAddress();
                    }
                    MyLog.w("LocalDns", "domain=" + host + ",ipsList=" + localDnsStr);
                }
            } catch (Exception e) {
                MyLog.e("getAddressByHost error = " + e);
            }

            //DnsPod
            if (address.size() == 0 || forceUseDnsPod) {
                String dnsPodStr = DnsPodUtils.getAddressByHostDnsPod(host);
                if (!TextUtils.isEmpty(dnsPodStr)) {
                    processDnsPodResult(address, dnsPodStr, host);
                } else {
                    // chenyong1 失败的话重试一次
                    MyLog.w("HttpDns", "dnspod failed retry");
                    dnsPodStr = DnsPodUtils.getAddressByHostDnsPod(host);
                    if (!TextUtils.isEmpty(dnsPodStr)) {
                        processDnsPodResult(address, dnsPodStr, host);
                    } else {
                        MyLog.w("HttpDns", "dnspod failed again");
                    }
                }
            }
        }
        return address;
    }

    private static void processDnsPodResult(List<String> address, String dnsPodStr, String host) {
        dnsPodStr = dnsPodStr.split(",")[0];
        String[] dnsPodStrs = dnsPodStr.split(";");
        MyLog.w("HttpDns", "domain=" + host + ",ipsList=" + dnsPodStr);
        for (int i = dnsPodStrs.length - 1; i >= 0; i--) {
            String ipStr = dnsPodStrs[i];
            if (!address.contains(ipStr)) {
                address.add(0, ipStr);
            }
        }
    }
}