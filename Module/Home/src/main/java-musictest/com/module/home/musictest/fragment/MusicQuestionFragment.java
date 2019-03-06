package com.module.home.musictest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.jakewharton.rxbinding2.view.RxView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.home.R;
import com.module.home.musictest.model.Answer;
import com.module.home.musictest.model.Question;
import com.module.home.musictest.presenter.MusicQuestionPresenter;
import com.module.home.musictest.view.IQuestionView;
import com.module.home.musictest.view.QuestionView;
import com.zq.toast.CommonToastView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

// 音乐问题页面
public class MusicQuestionFragment extends BaseFragment implements IQuestionView {

    CommonTitleBar mTitlebar;
    RelativeLayout mQuestionArea;

    MusicQuestionPresenter mMusicTestPresenter;

    List<Question> mQuestions = new ArrayList<>();                         // 问题
    Map<String, Answer> mAnswerHashMap = new HashMap<String, Answer>();    // 答案
    Map<String, Set<Integer>> mSetMap = new HashMap<String, Set<Integer>>();                   //记录问题的答案

    LoadService mLoadService;

    @Override
    public int initView() {
        return R.layout.music_question_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mQuestionArea = (RelativeLayout) mRootView.findViewById(R.id.question_area);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                                .setPopFragment(MusicQuestionFragment.this)
                                .setPopAbove(false)
                                .setHasAnimation(true)
                                .build());
                    }
                });

        mMusicTestPresenter = new MusicQuestionPresenter(this);
        addPresent(mMusicTestPresenter);

        mMusicTestPresenter.getQuestionList();

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LoadingCallback(R.drawable.wufensi, "数据真的在加载中..."))
                .addCallback(new EmptyCallback(R.drawable.wufensi, "数据空了"))
                .addCallback(new ErrorCallback(R.drawable.wufensi, "请求出错了"))
                .setDefaultCallback(LoadingCallback.class)
                .build();
        mLoadService = mLoadSir.register(mQuestionArea, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                mMusicTestPresenter.getQuestionList();
            }
        });

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void loadQuestionsData(List<Question> questionList) {
        this.mQuestions = questionList;
        if (mQuestions == null || mQuestions.size() == 0) {
            mLoadService.showCallback(EmptyCallback.class);
            return;
        }

        mLoadService.showSuccess();
        QuestionView quesetionView = new QuestionView(getContext());
        quesetionView.setMaxNum(mQuestions.size());
        quesetionView.setData(questionList.get(0), 0, null);
        mTitlebar.getRightTextView().setText("1/" + mQuestions.size());
        quesetionView.setListener(new QuestionView.OnClickListener() {
            @Override
            public void nextQuestion(Answer answer, int next, Set<Integer> selectList) {
                if (answer != null && answer.getAnswerIDs() != null && answer.getAnswerIDs().size() > 0) {
                    mTitlebar.getRightTextView().setText(String.valueOf(next + 1) + "/" + mQuestions.size());
                    mAnswerHashMap.put(answer.getQuestionID(), answer);
                    mSetMap.put(answer.getQuestionID(), selectList);
                    if (mSetMap.containsKey(questionList.get(next).getQuestionID())) {
                        quesetionView.setData(questionList.get(next), next, mSetMap.get(questionList.get(next).getQuestionID()));
                    } else {
                        quesetionView.setData(questionList.get(next), next, null);
                    }
                } else {
                    U.getToastUtil().showShort("至少选一个喔~");
                }
            }

            @Override
            public void lastQuestion(int last) {
                mTitlebar.getRightTextView().setText(String.valueOf(last + 1) + "/" + mQuestions.size());
                if (mSetMap.containsKey(questionList.get(last).getQuestionID())) {
                    quesetionView.setData(questionList.get(last), last, mSetMap.get(questionList.get(last).getQuestionID()));
                } else {
                    quesetionView.setData(questionList.get(last), last, null);
                }
            }

            @Override
            public void onCompletion(Answer answer) {
                if (answer != null && answer.getAnswerIDs() != null && answer.getAnswerIDs().size() > 0) {
                    mAnswerHashMap.put(answer.getQuestionID(), answer);
                    mMusicTestPresenter.reportAnswer(mAnswerHashMap);
                } else {
                    U.getToastUtil().showShort("至少选一个喔~");
                }
            }
        });
        mQuestionArea.addView(quesetionView);
    }

    @Override
    public void loadQuestionsDataFail() {
        mLoadService.showCallback(ErrorCallback.class);
    }

    @Override
    public void onComplete() {
        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(getContext())
                .setImage(R.drawable.touxiangshezhichenggong_icon)
                .setText("提交成功")
                .build());
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopFragment(MusicQuestionFragment.this)
                .setPopAbove(false)
                .setHasAnimation(true)
                .build());
    }
}
