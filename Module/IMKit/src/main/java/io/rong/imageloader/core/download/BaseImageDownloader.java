//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.download;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore.Video.Thumbnails;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.rong.imageloader.core.assist.ContentLengthInputStream;
import io.rong.imageloader.core.download.ImageDownloader;
import io.rong.imageloader.utils.IoUtils;

public class BaseImageDownloader implements ImageDownloader {
  public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5000;
  public static final int DEFAULT_HTTP_READ_TIMEOUT = 20000;
  protected static final int BUFFER_SIZE = 32768;
  protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
  protected static final int MAX_REDIRECT_COUNT = 5;
  protected static final String CONTENT_CONTACTS_URI_PREFIX = "content://com.android.contacts/";
  private static final String ERROR_UNSUPPORTED_SCHEME = "UIL doesn't support scheme(protocol) by default [%s]. You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))";
  protected final Context context;
  protected final int connectTimeout;
  protected final int readTimeout;

  public BaseImageDownloader(Context context) {
    this(context, 5000, 20000);
  }

  public BaseImageDownloader(Context context, int connectTimeout, int readTimeout) {
    this.context = context.getApplicationContext();
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  public InputStream getStream(String imageUri, Object extra) throws IOException {
    switch(Scheme.ofUri(imageUri)) {
      case HTTP:
      case HTTPS:
        return this.getStreamFromNetwork(imageUri, extra);
      case FILE:
        return this.getStreamFromFile(imageUri, extra);
      case CONTENT:
        return this.getStreamFromContent(imageUri, extra);
      case ASSETS:
        return this.getStreamFromAssets(imageUri, extra);
      case DRAWABLE:
        return this.getStreamFromDrawable(imageUri, extra);
      case AVATAR:
        return this.getStreamByGenerateBitmap(imageUri, extra);
      case UNKNOWN:
      default:
        return this.getStreamFromOtherSource(imageUri, extra);
    }
  }

  protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
    HttpURLConnection conn = this.createConnection(imageUri, extra);

    for(int redirectCount = 0; conn.getResponseCode() / 100 == 3 && redirectCount < 5; ++redirectCount) {
      conn = this.createConnection(conn.getHeaderField("Location"), extra);
    }

    InputStream imageStream;
    try {
      imageStream = conn.getInputStream();
    } catch (IOException var7) {
      IoUtils.readAndCloseStream(conn.getErrorStream());
      throw var7;
    }

    if (!this.shouldBeProcessed(conn)) {
      IoUtils.closeSilently(imageStream);
      throw new IOException("Image request failed with response code " + conn.getResponseCode());
    } else {
      return new ContentLengthInputStream(new BufferedInputStream(imageStream, 32768), conn.getContentLength());
    }
  }

  protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException {
    return conn.getResponseCode() == 200 || conn.getResponseCode() == 404 && conn.getContentLength() > 0 && conn.getContentType().contains("image/");
  }

  protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
    String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~'%");
    HttpURLConnection conn = (HttpURLConnection)(new URL(encodedUrl)).openConnection();
    conn.setConnectTimeout(this.connectTimeout);
    conn.setReadTimeout(this.readTimeout);
    return conn;
  }

  protected InputStream getStreamFromFile(String imageUri, Object extra) throws IOException {
    String filePath = Scheme.FILE.crop(imageUri);
    if (this.isVideoFileUri(imageUri)) {
      return this.getVideoThumbnailStream(filePath);
    } else {
      BufferedInputStream imageStream = new BufferedInputStream(new FileInputStream(filePath), 32768);
      return new ContentLengthInputStream(imageStream, (int)(new File(filePath)).length());
    }
  }

  @TargetApi(8)
  private InputStream getVideoThumbnailStream(String filePath) {
    if (VERSION.SDK_INT >= 8) {
      Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, 2);
      if (bitmap != null) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, bos);
        return new ByteArrayInputStream(bos.toByteArray());
      }
    }

    return null;
  }

  protected InputStream getStreamFromContent(String imageUri, Object extra) throws FileNotFoundException {
    ContentResolver res = this.context.getContentResolver();
    Uri uri = Uri.parse(imageUri);
    if (this.isVideoContentUri(uri)) {
      Long origId = Long.valueOf(uri.getLastPathSegment());
      Bitmap bitmap = Thumbnails.getThumbnail(res, origId, 1, (Options)null);
      if (bitmap != null) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, bos);
        return new ByteArrayInputStream(bos.toByteArray());
      }
    } else if (imageUri.startsWith("content://com.android.contacts/")) {
      return this.getContactPhotoStream(uri);
    }

    return res.openInputStream(uri);
  }

  @TargetApi(14)
  protected InputStream getContactPhotoStream(Uri uri) {
    ContentResolver res = this.context.getContentResolver();
    return VERSION.SDK_INT >= 14 ? Contacts.openContactPhotoInputStream(res, uri, true) : Contacts.openContactPhotoInputStream(res, uri);
  }

  protected InputStream getStreamFromAssets(String imageUri, Object extra) throws IOException {
    String filePath = Scheme.ASSETS.crop(imageUri);
    return this.context.getAssets().open(filePath);
  }

  protected InputStream getStreamFromDrawable(String imageUri, Object extra) {
    String drawableIdString = Scheme.DRAWABLE.crop(imageUri);
    int drawableId = Integer.parseInt(drawableIdString);
    return this.context.getResources().openRawResource(drawableId);
  }

  protected InputStream getStreamByGenerateBitmap(String key, Object extra) {
    return null;
  }

  protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
    throw new UnsupportedOperationException(String.format("UIL doesn't support scheme(protocol) by default [%s]. You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))", imageUri));
  }

  private boolean isVideoContentUri(Uri uri) {
    String mimeType = this.context.getContentResolver().getType(uri);
    return mimeType != null && mimeType.startsWith("video/");
  }

  private boolean isVideoFileUri(String uri) {
    String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    return mimeType != null && mimeType.startsWith("video/");
  }
}
