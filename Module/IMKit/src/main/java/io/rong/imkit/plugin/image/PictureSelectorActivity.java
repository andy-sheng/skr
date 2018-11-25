//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.plugin.image;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Media;
import android.support.v4.content.FileProvider;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.rong.common.ParcelUtils;
import io.rong.imkit.R;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.ILoadImageCallback;
import io.rong.imkit.utilities.PermissionCheckUtil;

public class PictureSelectorActivity extends RongBaseNoActionbarActivity {
  public static final int REQUEST_PREVIEW = 0;
  public static final int REQUEST_CAMERA = 1;
  public static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
  private GridView mGridView;
  private ImageButton mBtnBack;
  private Button mBtnSend;
  private io.rong.imkit.plugin.image.PictureSelectorActivity.PicTypeBtn mPicType;
  private io.rong.imkit.plugin.image.PictureSelectorActivity.PreviewBtn mPreviewBtn;
  private View mCatalogView;
  private ListView mCatalogListView;
  private List<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem> mAllItemList;
  private ArrayList<Uri> mAllSelectedItemList;
  private Map<String, List<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem>> mItemMap;
  private List<String> mCatalogList;
  private String mCurrentCatalog = "";
  private Uri mTakePictureUri;
  private boolean mSendOrigin = false;
  private int perWidth;
  private int perHeight;

  public PictureSelectorActivity() {
  }

  @TargetApi(23)
  protected void onCreate(Bundle savedInstanceState) {
    this.requestWindowFeature(1);
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.rc_picsel_activity);
    this.mGridView = (GridView)this.findViewById(R.id.gridlist);
    this.mBtnBack = (ImageButton)this.findViewById(R.id.back);
    this.mBtnBack.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        io.rong.imkit.plugin.image.PictureSelectorActivity.this.finish();
      }
    });
    this.mBtnSend = (Button)this.findViewById(R.id.send);
    this.mPicType = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicTypeBtn)this.findViewById(R.id.pic_type);
    this.mPicType.init(this);
    this.mPicType.setEnabled(false);
    this.mPreviewBtn = (io.rong.imkit.plugin.image.PictureSelectorActivity.PreviewBtn)this.findViewById(R.id.preview);
    this.mPreviewBtn.init(this);
    this.mPreviewBtn.setEnabled(false);
    this.mCatalogView = this.findViewById(R.id.catalog_window);
    this.mCatalogListView = (ListView)this.findViewById(R.id.catalog_listview);
    String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
    if (!PermissionCheckUtil.checkPermissions(this, permissions)) {
      PermissionCheckUtil.requestPermissions(this, permissions, 100);
    } else {
      this.initView();
    }
  }

  private void initView() {
    this.updatePictureItems();
    this.mGridView.setAdapter(new io.rong.imkit.plugin.image.PictureSelectorActivity.GridViewAdapter());
    this.mGridView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
          io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList = new ArrayList();
          if (io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog.isEmpty()) {
            io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList.addAll(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mAllItemList);
            io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemSelectedList = null;
          } else {
            io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList.addAll((Collection) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog));
            io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemSelectedList = new ArrayList();
            Iterator var6 = io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.keySet().iterator();

            label32:
            while(true) {
              String key;
              do {
                if (!var6.hasNext()) {
                  break label32;
                }

                key = (String)var6.next();
              } while(key.equals(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog));

              Iterator var8 = ((List) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(key)).iterator();

              while(var8.hasNext()) {
                io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)var8.next();
                if (item.selected) {
                  io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemSelectedList.add(item);
                }
              }
            }
          }

          Intent intent = new Intent(io.rong.imkit.plugin.image.PictureSelectorActivity.this, PicturePreviewActivity.class);
          intent.putExtra("index", position - 1);
          intent.putExtra("sendOrigin", io.rong.imkit.plugin.image.PictureSelectorActivity.this.mSendOrigin);
          io.rong.imkit.plugin.image.PictureSelectorActivity.this.startActivityForResult(intent, 0);
        }
      }
    });
    this.mBtnSend.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent data = new Intent();
        new ArrayList();
        data.putExtra("sendOrigin", io.rong.imkit.plugin.image.PictureSelectorActivity.this.mSendOrigin);
        data.putExtra("android.intent.extra.RETURN_RESULT", io.rong.imkit.plugin.image.PictureSelectorActivity.this.mAllSelectedItemList);
        io.rong.imkit.plugin.image.PictureSelectorActivity.this.setResult(-1, data);
        io.rong.imkit.plugin.image.PictureSelectorActivity.this.finish();
      }
    });
    this.mPicType.setEnabled(true);
    this.mPicType.setTextColor(this.getResources().getColor(R.color.rc_picsel_toolbar_send_text_normal));
    this.mPicType.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogView.setVisibility(View.VISIBLE);
      }
    });
    this.mPreviewBtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList = new ArrayList();
        Iterator var2 = io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.keySet().iterator();

        while(var2.hasNext()) {
          String key = (String)var2.next();
          Iterator var4 = ((List) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(key)).iterator();

          while(var4.hasNext()) {
            io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)var4.next();
            if (item.selected) {
              io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList.add(item);
            }
          }
        }

        io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemSelectedList = null;
        Intent intent = new Intent(io.rong.imkit.plugin.image.PictureSelectorActivity.this, PicturePreviewActivity.class);
        intent.putExtra("sendOrigin", io.rong.imkit.plugin.image.PictureSelectorActivity.this.mSendOrigin);
        io.rong.imkit.plugin.image.PictureSelectorActivity.this.startActivityForResult(intent, 0);
      }
    });
    this.mCatalogView.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == 1 && io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogView.getVisibility() == View.VISIBLE) {
          io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogView.setVisibility(View.GONE);
        }

        return true;
      }
    });
    this.mCatalogListView.setAdapter(new io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter());
    this.mCatalogListView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String catalog;
        if (position == 0) {
          catalog = "";
        } else {
          catalog = (String) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogList.get(position - 1);
        }

        if (catalog.equals(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog)) {
          io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogView.setVisibility(View.GONE);
        } else {
          io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog = catalog;
          TextView textView = (TextView)view.findViewById(R.id.name);
          io.rong.imkit.plugin.image.PictureSelectorActivity.this.mPicType.setText(textView.getText().toString());
          io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogView.setVisibility(View.GONE);
          ((io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogListView.getAdapter()).notifyDataSetChanged();
          ((io.rong.imkit.plugin.image.PictureSelectorActivity.GridViewAdapter) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mGridView.getAdapter()).notifyDataSetChanged();
        }
      }
    });
    this.perWidth = ((WindowManager)((WindowManager)this.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getWidth() / 3;
    this.perHeight = ((WindowManager)((WindowManager)this.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getHeight() / 5;
  }

  @TargetApi(23)
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 100) {
      if (VERSION.SDK_INT >= 23 && this.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
        this.initView();
      } else {
        Toast.makeText(this.getApplicationContext(), this.getString(R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
        this.finish();
      }
    }

    if (resultCode != 0) {
      if (resultCode == 1) {
        this.setResult(-1, data);
        this.finish();
      } else {
        switch(requestCode) {
          case 0:
            this.mSendOrigin = data.getBooleanExtra("sendOrigin", false);
            ArrayList<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem> list = io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList;
            if (list == null) {
              return;
            }

            Iterator var9 = list.iterator();

            while(var9.hasNext()) {
              io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem it = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)var9.next();
              io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = this.findByUri(it.uri);
              if (item != null) {
                item.selected = it.selected;
              }
            }

            ((io.rong.imkit.plugin.image.PictureSelectorActivity.GridViewAdapter)this.mGridView.getAdapter()).notifyDataSetChanged();
            ((io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter)this.mCatalogListView.getAdapter()).notifyDataSetChanged();
            this.updateToolbar();
            break;
          case 1:
            if (this.mTakePictureUri != null) {
              io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList = new ArrayList();
              io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = new io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem();
              item.uri = this.mTakePictureUri.getPath();
              io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList.add(item);
              io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemSelectedList = null;
              Intent intent = new Intent(this, PicturePreviewActivity.class);
              this.startActivityForResult(intent, 0);
              MediaScannerConnection.scanFile(this, new String[]{this.mTakePictureUri.getPath()}, (String[])null, new OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                  io.rong.imkit.plugin.image.PictureSelectorActivity.this.updatePictureItems();
                }
              });
            }
        }

      }
    }
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == 4 && this.mCatalogView != null && this.mCatalogView.getVisibility() == View.VISIBLE) {
      this.mCatalogView.setVisibility(View.GONE);
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  protected void requestCamera() {
    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    if (!path.exists()) {
      path.mkdirs();
    }

    String name = System.currentTimeMillis() + ".jpg";
    File file = new File(path, name);
    this.mTakePictureUri = Uri.fromFile(file);
    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    if (resInfoList.size() <= 0) {
      Toast.makeText(this, this.getResources().getString(R.string.rc_voip_cpu_error), Toast.LENGTH_SHORT).show();
    } else {
      Uri uri = null;

      try {
        uri = FileProvider.getUriForFile(this, this.getPackageName() + this.getString(R.string.rc_authorities_fileprovider), file);
      } catch (Exception var10) {
        var10.printStackTrace();
        throw new RuntimeException("Please check IMKit Manifest FileProvider config. Please refer to http://support.rongcloud.cn/kb/NzA1");
      }

      Iterator var7 = resInfoList.iterator();

      while(var7.hasNext()) {
        ResolveInfo resolveInfo = (ResolveInfo)var7.next();
        String packageName = resolveInfo.activityInfo.packageName;
        this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }

      intent.putExtra("output", uri);
      this.startActivityForResult(intent, 1);
    }
  }

  private void updatePictureItems() {
    String[] projection = new String[]{"_data", "date_added"};
    String orderBy = "datetaken DESC";
    Cursor cursor = this.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, (String)null, (String[])null, orderBy);
    this.mAllItemList = new ArrayList();
    this.mCatalogList = new ArrayList();
    this.mAllSelectedItemList = new ArrayList();
    this.mItemMap = new ArrayMap();
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        do {
          io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = new io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem();
          item.uri = cursor.getString(0);
          if (item.uri != null) {
            File file = new File(item.uri);
            if (file.exists() && file.length() != 0L) {
              this.mAllItemList.add(item);
              int last = item.uri.lastIndexOf("/");
              if (last != -1) {
                String catalog;
                if (last == 0) {
                  catalog = "/";
                } else {
                  int secondLast = item.uri.lastIndexOf("/", last - 1);
                  catalog = item.uri.substring(secondLast + 1, last);
                }

                if (this.mItemMap.containsKey(catalog)) {
                  ((List)this.mItemMap.get(catalog)).add(item);
                } else {
                  ArrayList<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem> itemList = new ArrayList();
                  itemList.add(item);
                  this.mItemMap.put(catalog, itemList);
                  this.mCatalogList.add(catalog);
                }
              }
            }
          }
        } while(cursor.moveToNext());
      }

      cursor.close();
    }

  }

  private int getTotalSelectedNum() {
    int sum = 0;
    Iterator var2 = this.mItemMap.keySet().iterator();

    while(var2.hasNext()) {
      String key = (String)var2.next();
      Iterator var4 = ((List)this.mItemMap.get(key)).iterator();

      while(var4.hasNext()) {
        io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)var4.next();
        if (item.selected) {
          ++sum;
        }
      }
    }

    return sum;
  }

  private void updateToolbar() {
    int sum = this.getTotalSelectedNum();
    if (sum == 0) {
      this.mBtnSend.setEnabled(false);
      this.mBtnSend.setTextColor(this.getResources().getColor(R.color.rc_picsel_toolbar_send_text_disable));
      this.mBtnSend.setText(R.string.rc_picsel_toolbar_send);
      this.mPreviewBtn.setEnabled(false);
      this.mPreviewBtn.setText(R.string.rc_picsel_toolbar_preview);
    } else if (sum <= 9) {
      this.mBtnSend.setEnabled(true);
      this.mBtnSend.setTextColor(this.getResources().getColor(R.color.rc_picsel_toolbar_send_text_normal));
      this.mBtnSend.setText(String.format(this.getResources().getString(R.string.rc_picsel_toolbar_send_num), sum));
      this.mPreviewBtn.setEnabled(true);
      this.mPreviewBtn.setText(String.format(this.getResources().getString(R.string.rc_picsel_toolbar_preview_num), sum));
    }

  }

  private io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem getItemAt(int index) {
    int sum = 0;
    Iterator var3 = this.mItemMap.keySet().iterator();

    while(var3.hasNext()) {
      String key = (String)var3.next();

      for(Iterator var5 = ((List)this.mItemMap.get(key)).iterator(); var5.hasNext(); ++sum) {
        io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)var5.next();
        if (sum == index) {
          return item;
        }
      }
    }

    return null;
  }

  private io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem getItemAt(String catalog, int index) {
    if (!this.mItemMap.containsKey(catalog)) {
      return null;
    } else {
      int sum = 0;

      for(Iterator var4 = ((List)this.mItemMap.get(catalog)).iterator(); var4.hasNext(); ++sum) {
        io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)var4.next();
        if (sum == index) {
          return item;
        }
      }

      return null;
    }
  }

  private io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem findByUri(String uri) {
    Iterator var2 = this.mItemMap.keySet().iterator();

    while(var2.hasNext()) {
      String key = (String)var2.next();
      Iterator var4 = ((List)this.mItemMap.get(key)).iterator();

      while(var4.hasNext()) {
        io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)var4.next();
        if (item.uri.equals(uri)) {
          return item;
        }
      }
    }

    return null;
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch(requestCode) {
      case 100:
        if (grantResults[0] == 0) {
          if (permissions[0].equals("android.permission.READ_EXTERNAL_STORAGE")) {
            this.initView();
          } else if (permissions[0].equals("android.permission.CAMERA")) {
            this.requestCamera();
          }
        } else if (permissions[0].equals("android.permission.CAMERA")) {
          Toast.makeText(this.getApplicationContext(), this.getString(R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(this.getApplicationContext(), this.getString(R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
          this.finish();
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

  }

  protected void onDestroy() {
    io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemList = null;
    io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder.itemSelectedList = null;
    super.onDestroy();
  }

  public static class PicItemHolder {
    public static ArrayList<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem> itemList;
    public static ArrayList<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem> itemSelectedList;

    public PicItemHolder() {
    }
  }

  @SuppressLint("AppCompatCustomView")
  public static class SelectBox extends ImageView {
    private boolean mIsChecked;

    public SelectBox(Context context, AttributeSet attrs) {
      super(context, attrs);
      this.setImageResource(R.drawable.rc_select_check_nor);
    }

    public void setChecked(boolean check) {
      this.mIsChecked = check;
      this.setImageResource(this.mIsChecked ? R.drawable.rc_select_check_sel : R.drawable.rc_select_check_nor);
    }

    public boolean getChecked() {
      return this.mIsChecked;
    }
  }

  public static class PreviewBtn extends LinearLayout {
    private TextView mText;

    public PreviewBtn(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public void init(Activity root) {
      this.mText = (TextView)root.findViewById(R.id.preview_text);
    }

    public void setText(int id) {
      this.mText.setText(id);
    }

    public void setText(String text) {
      this.mText.setText(text);
    }

    public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      int color = enabled ? R.color.rc_picsel_toolbar_send_text_normal : R.color.rc_picsel_toolbar_send_text_disable;
      this.mText.setTextColor(this.getResources().getColor(color));
    }

    public boolean onTouchEvent(MotionEvent event) {
      if (this.isEnabled()) {
        switch(event.getAction()) {
          case 0:
            this.mText.setVisibility(INVISIBLE);
            break;
          case 1:
            this.mText.setVisibility(VISIBLE);
        }
      }

      return super.onTouchEvent(event);
    }
  }

  public static class PicTypeBtn extends LinearLayout {
    TextView mText;

    public PicTypeBtn(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public void init(Activity root) {
      this.mText = (TextView)root.findViewById(R.id.type_text);
    }

    public void setText(String text) {
      this.mText.setText(text);
    }

    public void setTextColor(int color) {
      this.mText.setTextColor(color);
    }

    public boolean onTouchEvent(MotionEvent event) {
      if (this.isEnabled()) {
        switch(event.getAction()) {
          case 0:
            this.mText.setVisibility(INVISIBLE);
            break;
          case 1:
            this.mText.setVisibility(VISIBLE);
        }
      }

      return super.onTouchEvent(event);
    }
  }

  public static class PicItem implements Parcelable {
    String uri;
    boolean selected;
    public static final Creator<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem> CREATOR = new Creator<io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem>() {
      public io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem createFromParcel(Parcel source) {
        return new io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem(source);
      }

      public io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem[] newArray(int size) {
        return new io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem[size];
      }
    };

    public int describeContents() {
      return 0;
    }

    public PicItem() {
    }

    public PicItem(Parcel in) {
      this.uri = ParcelUtils.readFromParcel(in);
      this.selected = ParcelUtils.readIntFromParcel(in) == 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
      ParcelUtils.writeToParcel(dest, this.uri);
      ParcelUtils.writeToParcel(dest, this.selected ? 1 : 0);
    }
  }

  private class CatalogAdapter extends BaseAdapter {
    private LayoutInflater mInflater = io.rong.imkit.plugin.image.PictureSelectorActivity.this.getLayoutInflater();

    public CatalogAdapter() {
    }

    public int getCount() {
      return io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.size() + 1;
    }

    public Object getItem(int position) {
      return null;
    }

    public long getItemId(int position) {
      return (long)position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter.ViewHolder holder;
      if (convertView == null) {
        view = this.mInflater.inflate(R.layout.rc_picsel_catalog_listview, parent, false);
        holder = new io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter.ViewHolder();
        holder.image = (ImageView)view.findViewById(R.id.image);
        holder.name = (TextView)view.findViewById(R.id.name);
        holder.number = (TextView)view.findViewById(R.id.number);
        holder.selected = (ImageView)view.findViewById(R.id.selected);
        view.setTag(holder);
      } else {
        holder = (io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter.ViewHolder)convertView.getTag();
      }

      String path;
      if (holder.image.getTag() != null) {
        path = (String)holder.image.getTag();
        AlbumBitmapCacheHelper.getInstance().removePathFromShowlist(path);
      }

      int num = 0;
      boolean showSelected = false;
      String name;
      Bitmap bitmap;
      BitmapDrawable bd;
      if (position == 0) {
        if (io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.size() == 0) {
          holder.image.setImageResource(R.drawable.rc_picsel_empty_pic);
        } else {
          path = ((io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)((List) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogList.get(0))).get(0)).uri;
          AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
          holder.image.setTag(path);
          bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, io.rong.imkit.plugin.image.PictureSelectorActivity.this.perWidth, io.rong.imkit.plugin.image.PictureSelectorActivity.this.perHeight, new ILoadImageCallback() {
            public void onLoadImageCallBack(Bitmap bitmap, String path1, Object... objects) {
              if (bitmap != null) {
                BitmapDrawable bd = new BitmapDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources(), bitmap);
                View v = io.rong.imkit.plugin.image.PictureSelectorActivity.this.mGridView.findViewWithTag(path1);
                if (v != null) {
                  v.setBackgroundDrawable(bd);
                  io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter.this.notifyDataSetChanged();
                }

              }
            }
          }, new Object[]{position});
          if (bitmap != null) {
            bd = new BitmapDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources(), bitmap);
            holder.image.setBackgroundDrawable(bd);
          } else {
            holder.image.setBackgroundResource(R.drawable.rc_grid_image_default);
          }
        }

        name = io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources().getString(R.string.rc_picsel_catalog_allpic);
        holder.number.setVisibility(View.GONE);
        showSelected = io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog.isEmpty();
      } else {
        path = ((io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem)((List) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogList.get(position - 1))).get(0)).uri;
        name = (String) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogList.get(position - 1);
        num = ((List) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCatalogList.get(position - 1))).size();
        holder.number.setVisibility(View.VISIBLE);
        showSelected = name.equals(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog);
        AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
        holder.image.setTag(path);
        bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, io.rong.imkit.plugin.image.PictureSelectorActivity.this.perWidth, io.rong.imkit.plugin.image.PictureSelectorActivity.this.perHeight, new ILoadImageCallback() {
          public void onLoadImageCallBack(Bitmap bitmap, String path1, Object... objects) {
            if (bitmap != null) {
              BitmapDrawable bd = new BitmapDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources(), bitmap);
              View v = io.rong.imkit.plugin.image.PictureSelectorActivity.this.mGridView.findViewWithTag(path1);
              if (v != null) {
                v.setBackgroundDrawable(bd);
                io.rong.imkit.plugin.image.PictureSelectorActivity.CatalogAdapter.this.notifyDataSetChanged();
              }

            }
          }
        }, new Object[]{position});
        if (bitmap != null) {
          bd = new BitmapDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources(), bitmap);
          holder.image.setBackgroundDrawable(bd);
        } else {
          holder.image.setBackgroundResource(R.drawable.rc_grid_image_default);
        }
      }

      holder.name.setText(name);
      holder.number.setText(String.format(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources().getString(R.string.rc_picsel_catalog_number), num));
      holder.selected.setVisibility(showSelected ? View.VISIBLE : View.INVISIBLE);
      return view;
    }

    private class ViewHolder {
      ImageView image;
      TextView name;
      TextView number;
      ImageView selected;

      private ViewHolder() {
      }
    }
  }

  private class GridViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater = io.rong.imkit.plugin.image.PictureSelectorActivity.this.getLayoutInflater();

    public GridViewAdapter() {
    }

    public int getCount() {
      int sum = 1;
      String key;
      if (io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog.isEmpty()) {
        for(Iterator var2 = io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.keySet().iterator(); var2.hasNext(); sum += ((List) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(key)).size()) {
          key = (String)var2.next();
        }
      } else {
        sum += ((List) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mItemMap.get(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog)).size();
      }

      return sum;
    }

    public Object getItem(int position) {
      return null;
    }

    public long getItemId(int position) {
      return (long)position;
    }

    @TargetApi(23)
    public View getView(int position, View convertView, ViewGroup parent) {
      if (position == 0) {
        View viewx = this.mInflater.inflate(R.layout.rc_picsel_grid_camera, parent, false);
        ImageButton mask = (ImageButton)viewx.findViewById(R.id.camera_mask);
        mask.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            String[] permissions = new String[]{"android.permission.CAMERA"};
            if (!PermissionCheckUtil.checkPermissions(io.rong.imkit.plugin.image.PictureSelectorActivity.this, permissions)) {
              PermissionCheckUtil.requestPermissions(io.rong.imkit.plugin.image.PictureSelectorActivity.this, permissions, 100);
            } else {
              io.rong.imkit.plugin.image.PictureSelectorActivity.this.requestCamera();
            }
          }
        });
        return viewx;
      } else {
        final io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem item;
        if (io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog.isEmpty()) {
          item = (io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem) io.rong.imkit.plugin.image.PictureSelectorActivity.this.mAllItemList.get(position - 1);
        } else {
          item = io.rong.imkit.plugin.image.PictureSelectorActivity.this.getItemAt(io.rong.imkit.plugin.image.PictureSelectorActivity.this.mCurrentCatalog, position - 1);
        }

        View view = convertView;
        final io.rong.imkit.plugin.image.PictureSelectorActivity.GridViewAdapter.ViewHolder holder;
        if (convertView != null && convertView.getTag() != null) {
          holder = (io.rong.imkit.plugin.image.PictureSelectorActivity.GridViewAdapter.ViewHolder)convertView.getTag();
        } else {
          view = this.mInflater.inflate(R.layout.rc_picsel_grid_item, parent, false);
          holder = new io.rong.imkit.plugin.image.PictureSelectorActivity.GridViewAdapter.ViewHolder();
          holder.image = (ImageView)view.findViewById(R.id.image);
          holder.mask = view.findViewById(R.id.mask);
          holder.checkBox = (io.rong.imkit.plugin.image.PictureSelectorActivity.SelectBox)view.findViewById(R.id.checkbox);
          view.setTag(holder);
        }

        String path;
        if (holder.image.getTag() != null) {
          path = (String)holder.image.getTag();
          AlbumBitmapCacheHelper.getInstance().removePathFromShowlist(path);
        }

        path = item.uri;
        AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
        holder.image.setTag(path);
        Bitmap bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, io.rong.imkit.plugin.image.PictureSelectorActivity.this.perWidth, io.rong.imkit.plugin.image.PictureSelectorActivity.this.perHeight, new ILoadImageCallback() {
          public void onLoadImageCallBack(Bitmap bitmap, String path1, Object... objects) {
            if (bitmap != null) {
              BitmapDrawable bd = new BitmapDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources(), bitmap);
              View v = io.rong.imkit.plugin.image.PictureSelectorActivity.this.mGridView.findViewWithTag(path1);
              if (v != null) {
                v.setBackgroundDrawable(bd);
              }

            }
          }
        }, new Object[]{position});
        if (bitmap != null) {
          BitmapDrawable bd = new BitmapDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources(), bitmap);
          holder.image.setBackgroundDrawable(bd);
        } else {
          holder.image.setBackgroundResource(R.drawable.rc_grid_image_default);
        }

        holder.checkBox.setChecked(item.selected);
        holder.checkBox.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            if (!holder.checkBox.getChecked() && io.rong.imkit.plugin.image.PictureSelectorActivity.this.getTotalSelectedNum() == 9) {
              Toast.makeText(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getApplicationContext(), R.string.rc_picsel_selected_max, Toast.LENGTH_SHORT).show();
            } else {
              holder.checkBox.setChecked(!holder.checkBox.getChecked());
              item.selected = holder.checkBox.getChecked();
              if (item.selected) {
                io.rong.imkit.plugin.image.PictureSelectorActivity.this.mAllSelectedItemList.add(Uri.parse("file://" + item.uri));
                holder.mask.setBackgroundColor(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources().getColor(R.color.rc_picsel_grid_mask_pressed));
              } else {
                try {
                  io.rong.imkit.plugin.image.PictureSelectorActivity.this.mAllSelectedItemList.remove(Uri.parse("file://" + item.uri));
                } catch (Exception var3) {
                  var3.printStackTrace();
                }

                holder.mask.setBackgroundDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources().getDrawable(R.drawable.rc_sp_grid_mask));
              }

              io.rong.imkit.plugin.image.PictureSelectorActivity.this.updateToolbar();
            }
          }
        });
        if (item.selected) {
          holder.mask.setBackgroundColor(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources().getColor(R.color.rc_picsel_grid_mask_pressed));
        } else {
          holder.mask.setBackgroundDrawable(io.rong.imkit.plugin.image.PictureSelectorActivity.this.getResources().getDrawable(R.drawable.rc_sp_grid_mask));
        }

        return view;
      }
    }

    private class ViewHolder {
      ImageView image;
      View mask;
      io.rong.imkit.plugin.image.PictureSelectorActivity.SelectBox checkBox;

      private ViewHolder() {
      }
    }
  }
}
