package com.module.playways.rank.room.utils;

import android.view.ViewGroup;

import com.common.log.MyLog;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.module.playways.rank.room.model.score.ScoreStateModel;
import com.module.playways.rank.room.view.RecordCircleView;
import com.zq.level.view.NormalLevelView;

// 辅助战绩动画的类
public class ScoreAnimationHelp {
    public final static String TAG = "ScoreAnimationHelp";

    /**
     * 星星变化动画
     *
     * @param view      变化的view
     * @param viewGroup 变化view的容器
     * @param form      变化前的状态
     * @param to        变化后的状态
     * @param listener  动画的监听
     */
    public static void starChangeAnimation(NormalLevelView view, ViewGroup viewGroup, ScoreStateModel form, ScoreStateModel to, AnimationListener listener) {
        // 在这可加一些校验的工作
        // 分成3段动画，段位动画前， 段位动画， 段位动画后
        beforeLevelChangeAnimation(view, viewGroup, form, to, new AnimationListener() {
            @Override
            public void onFinish() {
                levelChangeAnimation(view, viewGroup, form, to, new AnimationListener() {
                    @Override
                    public void onFinish() {
                        afterLevelChangeAnimation(view, viewGroup, form, to, new AnimationListener() {
                            @Override
                            public void onFinish() {
                                if (listener != null) {
                                    listener.onFinish();
                                }
                            }
                        });
                    }
                });
            }
        });
    }


    public static void battleChangeAnimation(RecordCircleView mRecordCircleView, ScoreResultModel scoreResultModel, ScoreStateModel form, ScoreStateModel to, AnimationListener listener) {
        // 战力保护优先算加分，最后算掉段
        if (form.getMaxBattleIndex() == 0) {
            // 之前已无战力上限
            if (to.getMaxBattleIndex() == 0) {
                //现在依然无上限
                mRecordCircleView.fullLevel();
                listener.onFinish();
            } else {
                // 直接更新表盘，无动画
                mRecordCircleView.setData(0, to.getMaxBattleIndex(), 0, 0, to.getProtectBattleIndex(), new AnimationListener() {
                    @Override
                    public void onFinish() {
                        listener.onFinish();
                    }
                });
            }
        } else if (to.getMaxBattleIndex() == 0) {
            // 当前已无战力上限, 且之前有
            // 之前升满，表盘变满级
            mRecordCircleView.setData(0, form.getMaxBattleIndex(), form.getCurrBattleIndex(), form.getMaxBattleIndex(), form.getProtectBattleIndex(), new AnimationListener() {
                @Override
                public void onFinish() {
                    mRecordCircleView.fullLevel();
                    listener.onFinish();
                }
            });
        } else {
            if (scoreResultModel.isExchangeStar()) {
                //TODO: 2019/1/22  涨星了，服务器确定，一定不会触发掉段
                mRecordCircleView.setData(0, form.getMaxBattleIndex(), form.getCurrBattleIndex(), form.getMaxBattleIndex(), form.getProtectBattleIndex(), new AnimationListener() {
                    @Override
                    public void onFinish() {
                        // 表盘清空
                        mRecordCircleView.setData(0, form.getMaxBattleIndex(), form.getMaxBattleIndex(), 0, form.getProtectBattleIndex(), new AnimationListener() {
                            @Override
                            public void onFinish() {
                                // 换表盘
                                mRecordCircleView.setData(0, to.getMaxBattleIndex(), 0, to.getCurrBattleIndex(), to.getProtectBattleIndex(), new AnimationListener() {
                                    @Override
                                    public void onFinish() {
                                        listener.onFinish();
                                    }
                                });
                            }
                        });
                    }
                });
            } else if (scoreResultModel.isProtectRank()) {
                // TODO: 2019/1/22 触发掉段保护了 服务器确定 一定不回触发兑换星星
                // 先涨分
                mRecordCircleView.setData(0, form.getMaxBattleIndex(), form.getCurrBattleIndex(), form.getCurrBattleIndex() + scoreResultModel.getBattleChange()
                        , form.getProtectBattleIndex(), new AnimationListener() {
                            @Override
                            public void onFinish() {
                                // 清空
                                mRecordCircleView.setData(0, form.getMaxBattleIndex(), form.getCurrBattleIndex() + scoreResultModel.getBattleChange(), 0,
                                        form.getProtectBattleIndex(), new AnimationListener() {
                                            @Override
                                            public void onFinish() {
                                                // 换表盘
                                                mRecordCircleView.setData(0, to.getMaxBattleIndex(), 0, to.getCurrBattleIndex(), to.getProtectBattleIndex(), new AnimationListener() {
                                                    @Override
                                                    public void onFinish() {
                                                        listener.onFinish();
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
            } else {
                // 直接涨分即可
                mRecordCircleView.setData(0, to.getMaxBattleIndex(), form.getCurrBattleIndex(), to.getCurrBattleIndex(), to.getProtectBattleIndex(), new AnimationListener() {
                    @Override
                    public void onFinish() {
                        listener.onFinish();
                    }
                });
            }

        }
    }

    // 段位动画前
    private static void beforeLevelChangeAnimation(NormalLevelView view, ViewGroup viewGroup, ScoreStateModel form, ScoreStateModel to, AnimationListener listener) {
        int leveChange = getLevelChange(form, to);
        if (leveChange > 0) {
            // 升段
            if (form.getMaxStar() == 0 || form.getMaxStar() > 6) {
                // 超过限制
                listener.onFinish();
            } else if (form.getMaxStar() == form.getCurrStar()) {
                // 满星星, 则无第一段动画
                listener.onFinish();
            } else if (form.getMaxStar() > form.getCurrStar()) {
                // 星星砸满
                view.starUp(viewGroup, form.getCurrStar(), form.getMaxStar() - 1, new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        // 第一段动画播放完成
                        listener.onFinish();
                    }
                });
            } else {
                MyLog.d(TAG, "beforeLevelChangeAnimation 数据异常 leveChange > 0 error form currStar > maxStar");
            }
        } else if (leveChange < 0) {
            // 降段
            if (form.getMaxStar() == 0 || form.getMaxStar() > 6) {
                // 超过限制
                listener.onFinish();
            } else if (form.getCurrStar() == 0) {
                // 之前无星，则无第一段动画
                listener.onFinish();
            } else if (form.getMaxStar() > form.getCurrStar()) {
                // 星星掉到0
                view.starLoss(viewGroup, form.getCurrStar() - 1, 0, new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        // 第一段动画播放完成
                        listener.onFinish();
                    }
                });
            } else {
                MyLog.d(TAG, "beforeLevelChangeAnimation 数据异常 leveChange < 0 error form currStar > maxStar");
            }
        } else {
            if (to.getMaxStar() == 0 || to.getMaxStar() > 6) {
                view.bindStarData(to.getMaxStar(), to.getCurrStar());
                listener.onFinish();
            } else {
                listener.onFinish();
            }
        }
    }

    // 段位动画
    private static void levelChangeAnimation(NormalLevelView view, ViewGroup viewGroup, ScoreStateModel form, ScoreStateModel to, AnimationListener listener) {
        int leveChange = getLevelChange(form, to);
        if (leveChange != 0) {
            view.levelChange(viewGroup, form.getMainRanking(), form.getSubRanking(), to.getMainRanking(), to.getSubRanking(),
                    to.getMaxStar(), new NormalLevelView.SVGAListener() {
                        @Override
                        public void onFinish() {
                            listener.onFinish();
                        }
                    });
        } else {
            listener.onFinish();
        }
    }

    // 段位动画后,listener 可能为null
    private static void afterLevelChangeAnimation(NormalLevelView view, ViewGroup viewGroup, ScoreStateModel form, ScoreStateModel to, AnimationListener listener) {
        int leveChange = getLevelChange(form, to);
        if (leveChange > 0) {
            MyLog.d(TAG, "afterLevelChangeAnimation" + " 升段 星星增加 ");
            // 升段 星星增加
            if (to.getMaxStar() == 0 || to.getMaxStar() > 6) {
                // 星星限制已经取消,直接显示几颗星即可
                view.bindStarData(to.getMaxStar(), to.getCurrStar());
                if (listener != null) {
                    listener.onFinish();
                }
            } else if (to.getCurrStar() == 0) {
                // 新段位0星, 则无动画
                if (listener != null) {
                    listener.onFinish();
                }
            } else if (to.getMaxStar() > to.getCurrStar()) {
                // 之前有升段变化，则必有从0到现在的过程
                view.starUp(viewGroup, 0, to.getCurrStar() - 1, new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            } else {
                MyLog.d(TAG, "afterLevelChangeAnimation 数据异常 leveChange > 0 error to currStar > maxStar");
            }
        } else if (leveChange < 0) {
            MyLog.d(TAG, "afterLevelChangeAnimation" + "降段 星星减少");
            // 降段 星星减少
            if (to.getMaxStar() == 0 || to.getMaxStar() > 6) {
                // 星星限制已经取消,直接显示几颗星即可
                view.bindStarData(to.getMaxStar(), to.getCurrStar());
                if (listener != null) {
                    listener.onFinish();
                }
            } else if (to.getMaxStar() == to.getCurrStar()) {
                // 新段位满星, 无动画
                view.bindStarData(to.getMaxStar(), to.getCurrStar());
                if (listener != null) {
                    listener.onFinish();
                }
            } else if (to.getMaxStar() > to.getCurrStar()) {
                // 星星数掉到现在
                view.starLoss(viewGroup, to.getMaxStar() - 1, to.getCurrStar(), new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            } else {
                MyLog.d(TAG, "afterLevelChangeAnimation 数据异常 leveChange < 0 error to currStar > maxStar");
            }
        } else {
            MyLog.d(TAG, "afterLevelChangeAnimation" + " 只有星星变化 ");
            // 只有星星变化
            if (to.getMaxStar() == 0 || to.getMaxStar() > 6) {
                // 星星限制已经取消,直接显示几颗星即可
                view.bindStarData(to.getMaxStar(), to.getCurrStar());
                if (listener != null) {
                    listener.onFinish();
                }
            } else if (to.getCurrStar() > form.getCurrStar()) {
                // 第三段从之前涨到现在
                view.starUp(viewGroup, form.getCurrStar(), to.getCurrStar() - 1, new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            } else if (to.getCurrStar() < form.getCurrStar()) {
                // 第三段从之前掉到现在
                view.starLoss(viewGroup, form.getCurrStar() - 1, to.getCurrStar(), new NormalLevelView.SVGAListener() {
                    @Override
                    public void onFinish() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            } else {
                // 星星也无变化
                if (listener != null) {
                    listener.onFinish();
                }
            }
        }
    }

    // 段位的变化 0 没变化， 大于0 升段位  小于0 降段位
    public static int getLevelChange(ScoreStateModel form, ScoreStateModel to) {
        if (to.getMainRanking() > form.getMainRanking()) {
            // 父段位现在比之前高
            return 1;
        } else if (to.getMainRanking() < form.getMainRanking()) {
            // 父段位现在比之前低
            return -1;
        } else {
            if (to.getSubRanking() < form.getSubRanking()) {
                // 子段位越小越高，子段位现在比之前高
                return 1;
            } else if (to.getSubRanking() > form.getSubRanking()) {
                // 子段位越小越高，子段位现在比之前低
                return -1;
            } else {
                return 0;
            }
        }
    }


    public interface AnimationListener {
        void onFinish();
    }
}
