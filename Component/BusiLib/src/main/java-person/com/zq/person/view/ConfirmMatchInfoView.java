package com.zq.person.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectChangeListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExConstraintLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.component.busilib.R;
import com.contrarywind.wheelview.interfaces.IPickerViewData;
import com.zq.live.proto.Common.ESex;

import java.util.ArrayList;
import java.util.List;

/**
 * 双人匹配确认信息
 */
public class ConfirmMatchInfoView extends RelativeLayout {

    public final static String TAG = "ConfirmMatchInfoView";

    Listener mListener;

    View mOutView;
    ExConstraintLayout mContainer;
    ExTextView mBoyTv;
    ExTextView mGirlTv;
    TextView mErrorHintTv;
    FrameLayout mFrameLayout;
    ExTextView mConfirmTv;
    Context mContext;

    OptionsPickerView mCustomOptions;
    List<AgeTag> mAgeTags = new ArrayList<>();

    Drawable unSelected;
    Drawable selected;

    int sex = 0;// 未知、非法参数
    AgeTag selectModel;

    public ConfirmMatchInfoView(Context context, Listener listener) {
        super(context);
        this.mListener = listener;
        mContext = context;
        unSelected = new DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(16f))
                .setSolidColor(U.getColor(R.color.transparent))
                .setStrokeColor(Color.parseColor("#807B89ED"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f))
                .build();

        selected = new DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(16f))
                .setSolidColor(Color.parseColor("#807B89ED"))
                .build();
        init();
    }

    private void init() {
        inflate(mContext, R.layout.confirm_match_info_view, this);

        mOutView = findViewById(R.id.out_view);
        mContainer = findViewById(R.id.container);
        mBoyTv = findViewById(R.id.boy_tv);
        mGirlTv = findViewById(R.id.girl_tv);
        mErrorHintTv = findViewById(R.id.error_hint_tv);
        mFrameLayout = findViewById(R.id.frameLayout);
        mConfirmTv = findViewById(R.id.confirm_tv);

        initAgeTags();

        mCustomOptions = new OptionsPickerBuilder(mContext, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                selectModel = mAgeTags.get(options1);
            }
        })
                .setLayoutRes(R.layout.pick_age_area_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        // 空的方法放着，必须实现
                    }
                })
                .setOptionsSelectChangeListener(new OnOptionsSelectChangeListener() {
                    @Override
                    public void onOptionsSelectChanged(int options1, int options2, int options3) {
                        mCustomOptions.returnData();
                    }
                })
                .setContentTextSize(24)
                .setTextColorCenter(Color.parseColor("#6A83C0"))
                .setTextColorOut(Color.parseColor("#666A83C0"))
                .setDividerColor(Color.parseColor("#33404A9A"))
                .setLineSpacingMultiplier(1.2f)
                .isDialog(false)
                .setDecorView(mFrameLayout)
                .build();
        mCustomOptions.setPicker(mAgeTags);//添加数据
        mCustomOptions.setSelectOptions(3);
        mCustomOptions.show();

        mBoyTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                selectSex(true);
            }
        });

        mGirlTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                selectSex(false);
            }
        });

        mConfirmTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (sex == 0 || selectModel == null) {
                    mErrorHintTv.setText("匹配唱聊伙伴，性别&年龄信息不能为空");
                    mErrorHintTv.setVisibility(VISIBLE);
                } else {
                    // 都可以传给服务器
                    if (mListener != null) {
                        mListener.onSelect(sex, selectModel.getId());
                    }
                }
            }
        });

        mOutView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null){
                    mListener.onClickOutView();
                }
            }
        });
    }

    private void initAgeTags() {
        mAgeTags.add(new AgeTag(1, "小学党"));
        mAgeTags.add(new AgeTag(2, "初中党"));
        mAgeTags.add(new AgeTag(3, "高中党"));
        mAgeTags.add(new AgeTag(4, "大学党"));
        mAgeTags.add(new AgeTag(5, "工作党"));
        selectModel = mAgeTags.get(0);  // 默认第一个
    }

    private void selectSex(boolean isBoy) {
        this.sex = isBoy ? ESex.SX_MALE.getValue() : ESex.SX_FEMALE.getValue();
        if (isBoy) {
            mBoyTv.setBackground(selected);
            mGirlTv.setBackground(unSelected);
            mBoyTv.setClickable(false);
            mGirlTv.setClickable(true);
        } else {
            mBoyTv.setBackground(unSelected);
            mGirlTv.setBackground(selected);
            mBoyTv.setClickable(true);
            mGirlTv.setClickable(false);
        }

        if (sex != 0 && selectModel != null) {
            mErrorHintTv.setVisibility(GONE);
        }
    }

    public interface Listener {
        void onSelect(int sex, int ageTag);

        void onClickOutView();
    }

    class AgeTag implements IPickerViewData {
        private int id;
        private String desc;

        public AgeTag(int id, String desc) {
            this.id = id;
            this.desc = desc;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String getPickerViewText() {
            return desc;
        }
    }
}
