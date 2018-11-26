//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.plugin.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import io.rong.imkit.R;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.ILoadImageCallback;
import io.rong.imkit.plugin.image.PictureSelectorActivity.PicItem;
import io.rong.imkit.plugin.image.PictureSelectorActivity.PicItemHolder;
import io.rong.photoview.PhotoView;
import io.rong.photoview.PhotoViewAttacher.OnViewTapListener;

public class PicturePreviewActivity extends RongBaseNoActionbarActivity {
    public static final int RESULT_SEND = 1;
    private TextView mIndexTotal;
    private View mWholeView;
    private View mToolbarTop;
    private View mToolbarBottom;
    private ImageButton mBtnBack;
    private Button mBtnSend;
    private io.rong.imkit.plugin.image.PicturePreviewActivity.CheckButton mUseOrigin;
    private io.rong.imkit.plugin.image.PicturePreviewActivity.CheckButton mSelectBox;
    private HackyViewPager mViewPager;
    private ArrayList<PicItem> mItemList;
    private ArrayList<PicItem> mItemSelectedList;
    private int mCurrentIndex;
    private boolean mFullScreen;

    public PicturePreviewActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.setContentView(R.layout.rc_picprev_activity);
        this.initView();
        this.mUseOrigin.setChecked(this.getIntent().getBooleanExtra("sendOrigin", false));
        this.mCurrentIndex = this.getIntent().getIntExtra("index", 0);
        if (this.mItemList == null) {
            this.mItemList = PicItemHolder.itemList;
            this.mItemSelectedList = PicItemHolder.itemSelectedList;
        }

        this.mIndexTotal.setText(String.format("%d/%d", this.mCurrentIndex + 1, this.mItemList.size()));
        int result;
        if (VERSION.SDK_INT >= 11) {
            this.mWholeView.setSystemUiVisibility(1024);
            result = getSmartBarHeight(this);
            if (result > 0) {
                LayoutParams lp = (LayoutParams) this.mToolbarBottom.getLayoutParams();
                lp.setMargins(0, 0, 0, result);
                this.mToolbarBottom.setLayoutParams(lp);
            }
        }

        result = 0;
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = this.getResources().getDimensionPixelSize(resourceId);
        }

        LayoutParams lp = new LayoutParams(this.mToolbarTop.getLayoutParams());
        lp.setMargins(0, result, 0, 0);
        this.mToolbarTop.setLayoutParams(lp);
        this.mBtnBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("sendOrigin", io.rong.imkit.plugin.image.PicturePreviewActivity.this.mUseOrigin.getChecked());
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.setResult(-1, intent);
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.finish();
            }
        });
        this.mBtnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                ArrayList<Uri> list = new ArrayList();
                Iterator var4;
                PicItem item;
                if (io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemSelectedList != null) {
                    var4 = io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemSelectedList.iterator();

                    while (var4.hasNext()) {
                        item = (PicItem) var4.next();
                        if (item.selected) {
                            list.add(Uri.parse("file://" + item.uri));
                        }
                    }
                }

                var4 = io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemList.iterator();

                while (var4.hasNext()) {
                    item = (PicItem) var4.next();
                    if (item.selected) {
                        list.add(Uri.parse("file://" + item.uri));
                    }
                }

                data.putExtra("sendOrigin", io.rong.imkit.plugin.image.PicturePreviewActivity.this.mUseOrigin.getChecked());
                data.putExtra("android.intent.extra.RETURN_RESULT", list);
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.setResult(1, data);
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.finish();
            }
        });
        this.mUseOrigin.setText(R.string.rc_picprev_origin);
        this.mUseOrigin.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.mUseOrigin.setChecked(!io.rong.imkit.plugin.image.PicturePreviewActivity.this.mUseOrigin.getChecked());
                if (io.rong.imkit.plugin.image.PicturePreviewActivity.this.mUseOrigin.getChecked() && io.rong.imkit.plugin.image.PicturePreviewActivity.this.getTotalSelectedNum() == 0) {
                    io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.setChecked(!io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.getChecked());
                    ((PicItem) io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemList.get(io.rong.imkit.plugin.image.PicturePreviewActivity.this.mCurrentIndex)).selected = io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.getChecked();
                    io.rong.imkit.plugin.image.PicturePreviewActivity.this.updateToolbar();
                }

            }
        });
        this.mSelectBox.setText(R.string.rc_picprev_select);
        this.mSelectBox.setChecked(((PicItem) this.mItemList.get(this.mCurrentIndex)).selected);
        this.mSelectBox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.getChecked() && io.rong.imkit.plugin.image.PicturePreviewActivity.this.getTotalSelectedNum() == 9) {
                    Toast.makeText(io.rong.imkit.plugin.image.PicturePreviewActivity.this, R.string.rc_picsel_selected_max, Toast.LENGTH_SHORT).show();
                } else {
                    io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.setChecked(!io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.getChecked());
                    ((PicItem) io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemList.get(io.rong.imkit.plugin.image.PicturePreviewActivity.this.mCurrentIndex)).selected = io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.getChecked();
                    io.rong.imkit.plugin.image.PicturePreviewActivity.this.updateToolbar();
                }
            }
        });
        this.mViewPager.setAdapter(new io.rong.imkit.plugin.image.PicturePreviewActivity.PreviewAdapter());
        this.mViewPager.setCurrentItem(this.mCurrentIndex);
        this.mViewPager.setOffscreenPageLimit(1);
        this.mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.mCurrentIndex = position;
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.mIndexTotal.setText(String.format("%d/%d", position + 1, io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemList.size()));
                io.rong.imkit.plugin.image.PicturePreviewActivity.this.mSelectBox.setChecked(((PicItem) io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemList.get(position)).selected);
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        this.updateToolbar();
    }

    private void initView() {
        this.mToolbarTop = this.findViewById(R.id.toolbar_top);
        this.mIndexTotal = (TextView) this.findViewById(R.id.index_total);
        this.mBtnBack = (ImageButton) this.findViewById(R.id.back);
        this.mBtnSend = (Button) this.findViewById(R.id.send);
        this.mWholeView = this.findViewById(R.id.whole_layout);
        this.mViewPager = (HackyViewPager) this.findViewById(R.id.viewpager);
        this.mToolbarBottom = this.findViewById(R.id.toolbar_bottom);
        this.mUseOrigin = new io.rong.imkit.plugin.image.PicturePreviewActivity.CheckButton(this.findViewById(R.id.origin_check), R.drawable.rc_origin_check_nor, R.drawable.rc_origin_check_sel);
        this.mSelectBox = new io.rong.imkit.plugin.image.PicturePreviewActivity.CheckButton(this.findViewById(R.id.select_check), R.drawable.rc_select_check_nor, R.drawable.rc_select_check_sel);
    }

    protected void onResume() {
        super.onResume();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            Intent intent = new Intent();
            intent.putExtra("sendOrigin", this.mUseOrigin.getChecked());
            this.setResult(-1, intent);
        }

        return super.onKeyDown(keyCode, event);
    }

    private int getTotalSelectedNum() {
        int sum = 0;

        for (int i = 0; i < this.mItemList.size(); ++i) {
            if (((PicItem) this.mItemList.get(i)).selected) {
                ++sum;
            }
        }

        if (this.mItemSelectedList != null) {
            sum += this.mItemSelectedList.size();
        }

        return sum;
    }

    private String getTotalSelectedSize() {
        float size = 0.0F;

        int i;
        File file;
        for (i = 0; i < this.mItemList.size(); ++i) {
            if (((PicItem) this.mItemList.get(i)).selected) {
                file = new File(((PicItem) this.mItemList.get(i)).uri);
                size += (float) (file.length() / 1024L);
            }
        }

        if (this.mItemSelectedList != null) {
            for (i = 0; i < this.mItemSelectedList.size(); ++i) {
                if (((PicItem) this.mItemSelectedList.get(i)).selected) {
                    file = new File(((PicItem) this.mItemSelectedList.get(i)).uri);
                    size += (float) (file.length() / 1024L);
                }
            }
        }

        String totalSize;
        if (size < 1024.0F) {
            totalSize = String.format("%.0fK", size);
        } else {
            totalSize = String.format("%.1fM", size / 1024.0F);
        }

        return totalSize;
    }

    private void updateToolbar() {
        int selNum = this.getTotalSelectedNum();
        if (this.mItemList.size() == 1 && selNum == 0) {
            this.mBtnSend.setText(R.string.rc_picsel_toolbar_send);
            this.mUseOrigin.setText(R.string.rc_picprev_origin);
            this.mBtnSend.setEnabled(false);
            this.mBtnSend.setTextColor(this.getResources().getColor(R.color.rc_picsel_toolbar_send_text_disable));
        } else {
            if (selNum == 0) {
                this.mBtnSend.setText(R.string.rc_picsel_toolbar_send);
                this.mUseOrigin.setText(R.string.rc_picprev_origin);
                this.mUseOrigin.setChecked(false);
                this.mBtnSend.setEnabled(false);
                this.mBtnSend.setTextColor(this.getResources().getColor(R.color.rc_picsel_toolbar_send_text_disable));
            } else if (selNum <= 9) {
                this.mBtnSend.setEnabled(true);
                this.mBtnSend.setTextColor(this.getResources().getColor(R.color.rc_picsel_toolbar_send_text_normal));
                this.mBtnSend.setText(String.format(this.getResources().getString(R.string.rc_picsel_toolbar_send_num), selNum));
                this.mUseOrigin.setText(String.format(this.getResources().getString(R.string.rc_picprev_origin_size), this.getTotalSelectedSize()));
            }

        }
    }

    @TargetApi(11)
    public static int getSmartBarHeight(Context context) {
        try {
            Class c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("mz_action_button_min_height");
            int height = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(height);
        } catch (Exception var5) {
            var5.printStackTrace();
            return 0;
        }
    }

    private class CheckButton {
        private View rootView;
        private ImageView image;
        private TextView text;
        private boolean checked = false;
        private int nor_resId;
        private int sel_resId;

        public CheckButton(View root, @DrawableRes int norId, @DrawableRes int selId) {
            this.rootView = root;
            this.image = (ImageView) root.findViewById(R.id.image);
            this.text = (TextView) root.findViewById(R.id.text);
            this.nor_resId = norId;
            this.sel_resId = selId;
            this.image.setImageResource(this.nor_resId);
        }

        public void setChecked(boolean check) {
            this.checked = check;
            this.image.setImageResource(this.checked ? this.sel_resId : this.nor_resId);
        }

        public boolean getChecked() {
            return this.checked;
        }

        public void setText(int resId) {
            this.text.setText(resId);
        }

        public void setText(CharSequence chars) {
            this.text.setText(chars);
        }

        public void setOnClickListener(@Nullable OnClickListener l) {
            this.rootView.setOnClickListener(l);
        }
    }

    private class PreviewAdapter extends PagerAdapter {
        private PreviewAdapter() {
        }

        public int getCount() {
            return io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemList.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            final PhotoView photoView = new PhotoView(container.getContext());
            photoView.setOnViewTapListener(new OnViewTapListener() {
                public void onViewTap(View view, float x, float y) {
                    io.rong.imkit.plugin.image.PicturePreviewActivity.this.mFullScreen = !io.rong.imkit.plugin.image.PicturePreviewActivity.this.mFullScreen;
                    View decorView;
                    byte uiOptions;
                    if (io.rong.imkit.plugin.image.PicturePreviewActivity.this.mFullScreen) {
                        if (VERSION.SDK_INT < 16) {
                            io.rong.imkit.plugin.image.PicturePreviewActivity.this.getWindow().setFlags(1024, 1024);
                        } else {
                            decorView = io.rong.imkit.plugin.image.PicturePreviewActivity.this.getWindow().getDecorView();
                            uiOptions = 4;
                            decorView.setSystemUiVisibility(uiOptions);
                        }

                        io.rong.imkit.plugin.image.PicturePreviewActivity.this.mToolbarTop.setVisibility(View.INVISIBLE);
                        io.rong.imkit.plugin.image.PicturePreviewActivity.this.mToolbarBottom.setVisibility(View.INVISIBLE);
                    } else {
                        if (VERSION.SDK_INT < 16) {
                            io.rong.imkit.plugin.image.PicturePreviewActivity.this.getWindow().setFlags(1024, 1024);
                        } else {
                            decorView = io.rong.imkit.plugin.image.PicturePreviewActivity.this.getWindow().getDecorView();
                            uiOptions = 0;
                            decorView.setSystemUiVisibility(uiOptions);
                        }

                        io.rong.imkit.plugin.image.PicturePreviewActivity.this.mToolbarTop.setVisibility(View.VISIBLE);
                        io.rong.imkit.plugin.image.PicturePreviewActivity.this.mToolbarBottom.setVisibility(View.VISIBLE);
                    }

                }
            });
            container.addView(photoView, -1, -1);
            String path = ((PicItem) io.rong.imkit.plugin.image.PicturePreviewActivity.this.mItemList.get(position)).uri;
            AlbumBitmapCacheHelper.getInstance().removePathFromShowlist(path);
            AlbumBitmapCacheHelper.getInstance().addPathToShowlist(path);
            Bitmap bitmap = AlbumBitmapCacheHelper.getInstance().getBitmap(path, 0, 0, new ILoadImageCallback() {
                public void onLoadImageCallBack(Bitmap bitmap, String p, Object... objects) {
                    if (bitmap != null) {
                        photoView.setImageBitmap(bitmap);
                    }
                }
            }, new Object[]{position});
            if (bitmap != null) {
                photoView.setImageBitmap(bitmap);
            } else {
                photoView.setImageResource(R.drawable.rc_grid_image_default);
            }

            return photoView;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
