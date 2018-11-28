//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.common.image.fresco.FrescoWorker;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Locale;

import io.rong.common.RLog;
import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongIM.PublicServiceBehaviorListener;
import io.rong.imkit.model.Event.PublicServiceFollowableEvent;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.LoadingDialogFragment;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Conversation.PublicServiceType;
import io.rong.imlib.model.PublicServiceProfile;

public class PublicServiceProfileFragment extends DispatchResultFragment {
    public static final String AGS_PUBLIC_ACCOUNT_INFO = "arg_public_account_info";
    PublicServiceProfile mPublicAccountInfo;
    private SimpleDraweeView mPortraitIV;
    private TextView mNameTV;
    private TextView mAccountTV;
    private TextView mDescriptionTV;
    private Button mEnterBtn;
    private Button mFollowBtn;
    private Button mUnfollowBtn;
    private String mTargetId;
    private ConversationType mConversationType;
    private String name;
    private LoadingDialogFragment mLoadingDialogFragment;

    public PublicServiceProfileFragment() {
    }

    protected void initFragment(Uri uri) {
        if (this.getActivity().getIntent() != null) {
            this.mPublicAccountInfo = (PublicServiceProfile) this.getActivity().getIntent().getParcelableExtra("arg_public_account_info");
        }

        if (uri != null) {
            if (this.mPublicAccountInfo == null) {
                String typeStr = !TextUtils.isEmpty(uri.getLastPathSegment()) ? uri.getLastPathSegment().toUpperCase(Locale.US) : "";
                this.mConversationType = ConversationType.valueOf(typeStr);
                this.mTargetId = uri.getQueryParameter("targetId");
                this.name = uri.getQueryParameter("name");
            } else {
                this.mConversationType = this.mPublicAccountInfo.getConversationType();
                this.mTargetId = this.mPublicAccountInfo.getTargetId();
                this.name = this.mPublicAccountInfo.getName();
            }
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rc_fr_public_service_inf, container, false);
        this.mPortraitIV = (SimpleDraweeView) view.findViewById(R.id.portrait);
        this.mNameTV = (TextView) view.findViewById(R.id.name);
        this.mAccountTV = (TextView) view.findViewById(R.id.account);
        this.mDescriptionTV = (TextView) view.findViewById(R.id.description);
        this.mEnterBtn = (Button) view.findViewById(R.id.enter);
        this.mFollowBtn = (Button) view.findViewById(R.id.follow);
        this.mUnfollowBtn = (Button) view.findViewById(R.id.unfollow);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mLoadingDialogFragment = LoadingDialogFragment.newInstance("", this.getResources().getString(R.string.rc_notice_data_is_loading));
        if (this.mPublicAccountInfo != null) {
            this.initData(this.mPublicAccountInfo);
        } else if (!TextUtils.isEmpty(this.mTargetId)) {
            PublicServiceType publicServiceType = null;
            if (this.mConversationType == ConversationType.APP_PUBLIC_SERVICE) {
                publicServiceType = PublicServiceType.APP_PUBLIC_SERVICE;
            } else if (this.mConversationType == ConversationType.PUBLIC_SERVICE) {
                publicServiceType = PublicServiceType.PUBLIC_SERVICE;
            } else {
                System.err.print("the public service type is error!!");
            }

            RongIM.getInstance().getPublicServiceProfile(publicServiceType, this.mTargetId, new ResultCallback<PublicServiceProfile>() {
                public void onSuccess(PublicServiceProfile info) {
                    if (info != null) {
                        io.rong.imkit.fragment.PublicServiceProfileFragment.this.initData(info);
                        RongUserInfoManager.getInstance().setPublicServiceProfile(info);
                    }

                }

                public void onError(ErrorCode e) {
                    RLog.e("PublicServiceProfileFragment", "Failure to get data!!!");
                }
            });
        }

    }

    private void initData(final PublicServiceProfile info) {
        if (info != null) {
            FrescoWorker.preLoadImg(this.mPortraitIV, info.getPortraitUri().toString(), 0);
            this.mNameTV.setText(info.getName());
            this.mAccountTV.setText(String.format(this.getResources().getString(R.string.rc_pub_service_info_account), info.getTargetId()));
            this.mDescriptionTV.setText(info.getIntroduction());
            boolean isFollow = info.isFollow();
            boolean isGlobal = info.isGlobal();
            FragmentTransaction ft;
            if (isGlobal) {
                ft = this.getFragmentManager().beginTransaction();
                ft.add(R.id.rc_layout, SetConversationNotificationFragment.newInstance());
                ft.commitAllowingStateLoss();
                this.mFollowBtn.setVisibility(View.GONE);
                this.mEnterBtn.setVisibility(View.VISIBLE);
                this.mUnfollowBtn.setVisibility(View.GONE);
            } else if (isFollow) {
                ft = this.getFragmentManager().beginTransaction();
                ft.add(R.id.rc_layout, SetConversationNotificationFragment.newInstance());
                ft.commitAllowingStateLoss();
                this.mFollowBtn.setVisibility(View.GONE);
                this.mEnterBtn.setVisibility(View.VISIBLE);
                this.mUnfollowBtn.setVisibility(View.VISIBLE);
            } else {
                this.mFollowBtn.setVisibility(View.VISIBLE);
                this.mEnterBtn.setVisibility(View.GONE);
                this.mUnfollowBtn.setVisibility(View.GONE);
            }

            this.mEnterBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    PublicServiceBehaviorListener listener = RongContext.getInstance().getPublicServiceBehaviorListener();
                    if (listener == null || !listener.onEnterConversationClick(v.getContext(), info)) {
                        io.rong.imkit.fragment.PublicServiceProfileFragment.this.getActivity().finish();
                        RongIM.getInstance().startConversation(io.rong.imkit.fragment.PublicServiceProfileFragment.this.getActivity(), info.getConversationType(), info.getTargetId(), info.getName());
                    }
                }
            });
            this.mFollowBtn.setOnClickListener(new OnClickListener() {
                public void onClick(final View v) {
                    PublicServiceType publicServiceType = null;
                    if (io.rong.imkit.fragment.PublicServiceProfileFragment.this.mConversationType == ConversationType.APP_PUBLIC_SERVICE) {
                        publicServiceType = PublicServiceType.APP_PUBLIC_SERVICE;
                    } else if (io.rong.imkit.fragment.PublicServiceProfileFragment.this.mConversationType == ConversationType.PUBLIC_SERVICE) {
                        publicServiceType = PublicServiceType.PUBLIC_SERVICE;
                    } else {
                        System.err.print("the public service type is error!!");
                    }

                    RongIM.getInstance().subscribePublicService(publicServiceType, info.getTargetId(), new OperationCallback() {
                        public void onSuccess() {
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mLoadingDialogFragment.dismiss();
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mFollowBtn.setVisibility(View.GONE);
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mEnterBtn.setVisibility(View.VISIBLE);
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mUnfollowBtn.setVisibility(View.VISIBLE);
                            RongUserInfoManager.getInstance().setPublicServiceProfile(info);
                            RongContext.getInstance().getEventBus().post(PublicServiceFollowableEvent.obtain(info.getTargetId(), info.getConversationType(), true));
                            PublicServiceBehaviorListener listener = RongContext.getInstance().getPublicServiceBehaviorListener();
                            if (listener == null || !listener.onFollowClick(v.getContext(), info)) {
                                io.rong.imkit.fragment.PublicServiceProfileFragment.this.getActivity().finish();
                                RongIM.getInstance().startConversation(io.rong.imkit.fragment.PublicServiceProfileFragment.this.getActivity(), info.getConversationType(), info.getTargetId(), info.getName());
                            }
                        }

                        public void onError(ErrorCode errorCode) {
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mLoadingDialogFragment.dismiss();
                        }
                    });
                    io.rong.imkit.fragment.PublicServiceProfileFragment.this.mLoadingDialogFragment.show(io.rong.imkit.fragment.PublicServiceProfileFragment.this.getFragmentManager());
                }
            });
            this.mUnfollowBtn.setOnClickListener(new OnClickListener() {
                public void onClick(final View v) {
                    PublicServiceType publicServiceType = null;
                    if (io.rong.imkit.fragment.PublicServiceProfileFragment.this.mConversationType == ConversationType.APP_PUBLIC_SERVICE) {
                        publicServiceType = PublicServiceType.APP_PUBLIC_SERVICE;
                    } else if (io.rong.imkit.fragment.PublicServiceProfileFragment.this.mConversationType == ConversationType.PUBLIC_SERVICE) {
                        publicServiceType = PublicServiceType.PUBLIC_SERVICE;
                    } else {
                        System.err.print("the public service type is error!!");
                    }

                    RongIM.getInstance().unsubscribePublicService(publicServiceType, info.getTargetId(), new OperationCallback() {
                        public void onSuccess() {
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mFollowBtn.setVisibility(View.VISIBLE);
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mEnterBtn.setVisibility(View.GONE);
                            io.rong.imkit.fragment.PublicServiceProfileFragment.this.mUnfollowBtn.setVisibility(View.GONE);
                            RongContext.getInstance().getEventBus().post(PublicServiceFollowableEvent.obtain(info.getTargetId(), info.getConversationType(), false));
                            PublicServiceBehaviorListener listener = RongContext.getInstance().getPublicServiceBehaviorListener();
                            if (listener == null || !listener.onUnFollowClick(v.getContext(), info)) {
                                io.rong.imkit.fragment.PublicServiceProfileFragment.this.getActivity().finish();
                            }
                        }

                        public void onError(ErrorCode errorCode) {
                        }
                    });
                }
            });
        }

    }

    public boolean handleMessage(Message msg) {
        return false;
    }
}
