package com.zq.mediaengine.filter.imgtex;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.PinAdapter;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.gles.GLRender;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * ImgTexFilter manager for convenient use.
 */
public class ImgTexFilterMgt {
    private static final String TAG = "ImgTexFilterMgt";
    private static final boolean VERBOSE = false;

    public static final int KSY_FILTER_BEAUTY_DISABLE = 0;
    public static final int KSY_FILTER_BEAUTY_SOFT = 16;
    public static final int KSY_FILTER_BEAUTY_SKINWHITEN = 17;
    public static final int KSY_FILTER_BEAUTY_ILLUSION = 18;
    public static final int KSY_FILTER_BEAUTY_DENOISE = 19;
    public static final int KSY_FILTER_BEAUTY_SMOOTH = 20;
    public static final int KSY_FILTER_BEAUTY_SOFT_EXT = 21;
    public static final int KSY_FILTER_BEAUTY_SOFT_SHARPEN = 22;
    public static final int KSY_FILTER_BEAUTY_PRO = 23;
    public static final int KSY_FILTER_BEAUTY_PRO1 = 24;
    public static final int KSY_FILTER_BEAUTY_PRO2 = 25;
    public static final int KSY_FILTER_BEAUTY_PRO3 = 26;
    public static final int KSY_FILTER_BEAUTY_PRO4 = 27;

    private Context mContext;
    private PinAdapter<ImgTexFrame> mInputFilter;
    private PinAdapter<ImgTexFrame> mOutputFilter;

    private LinkedList<ImgFilterBase> mFilters;
    private LinkedList<ImgFilterBase> mExtraFilters;
    private final Object mFiltersLock = new Object();

    private ImgTexFilterBase.OnErrorListener mOnErrorListener;

    public ImgTexFilterMgt(Context context) {
        mContext = context;
        mInputFilter = new PinAdapter<>();
        mOutputFilter = new PinAdapter<>();
        mInputFilter.mSrcPin.connect(mOutputFilter.mSinkPin);
        mFilters = new LinkedList<>();
        mExtraFilters = new LinkedList<>();
    }

    /**
     * Get input pin instance.
     *
     * @return sink pin instance
     */
    public SinkPin<ImgTexFrame> getSinkPin() {
        return mInputFilter.mSinkPin;
    }

    /**
     * Get output pint instance.
     *
     * @return source pin instance
     */
    public SrcPin<ImgTexFrame> getSrcPin() {
        return mOutputFilter.mSrcPin;
    }

    /**
     * Set error listener.
     *
     * @param listener listener to set
     */
    public void setOnErrorListener(ImgTexFilterBase.OnErrorListener listener) {
        mOnErrorListener = listener;
        synchronized (mFiltersLock) {
            if (mOnErrorListener != null && mFilters != null && !mFilters.isEmpty()) {
                for (ImgFilterBase filter : mFilters) {
                    filter.setOnErrorListener(mOnErrorListener);
                }
            }
        }
    }

    /**
     * Set an builtin gpu image filer to use, the previous ones would be released automatically.
     *
     * @param glRender  audio filter to set, null means disable filter
     * @param filterIdx builtin filter index, see KSY_FILTER_BEAUTY_XXX
     */
    public void setFilter(GLRender glRender, int filterIdx) {
        ImgFilterBase filter;
        switch (filterIdx) {
            case KSY_FILTER_BEAUTY_SOFT:
                filter = new ImgBeautySoftFilter(glRender);
                break;
            case KSY_FILTER_BEAUTY_SKINWHITEN:
                filter = new ImgBeautySkinWhitenFilter(glRender);
                break;
            case KSY_FILTER_BEAUTY_ILLUSION:
                filter = new ImgBeautyIllusionFilter(glRender);
                break;
            case KSY_FILTER_BEAUTY_DENOISE:
                filter = new ImgBeautyDenoiseFilter(glRender);
                break;
            case KSY_FILTER_BEAUTY_SOFT_EXT:
                filter = new ImgBeautySoftExtFilter(glRender);
                break;
            case KSY_FILTER_BEAUTY_SOFT_SHARPEN:
                filter = new ImgBeautySoftSharpenFilter(glRender);
                break;
            case KSY_FILTER_BEAUTY_SMOOTH:
                filter = new ImgBeautySmoothFilter(glRender, mContext);
                break;
            case KSY_FILTER_BEAUTY_PRO:
                filter = new ImgBeautyProFilter(glRender, mContext);
                break;
            case KSY_FILTER_BEAUTY_PRO1:
                filter = new ImgBeautyProFilter(glRender, mContext, 1);
                break;
            case KSY_FILTER_BEAUTY_PRO2:
                filter = new ImgBeautyProFilter(glRender, mContext, 2);
                break;
            case KSY_FILTER_BEAUTY_PRO3:
                filter = new ImgBeautyProFilter(glRender, mContext, 3);
                break;
            case KSY_FILTER_BEAUTY_PRO4:
                filter = new ImgBeautyProFilter(glRender, mContext, 4);
                break;
            default:
                filter = null;
                break;
        }
        setFilter(filter);
    }

    /**
     * Set an gpu image filer to use. <br/>
     * The previous ones would be released automatically.
     *
     * @param filter gpu image filter to set, null means disable filter
     * @param autoRelease should added filters be auto released.
     *                    If set to false, the all added filters should be released by user.
     */
    public void setFilter(ImgFilterBase filter, boolean autoRelease) {
        List<ImgFilterBase> filters = null;
        if (filter != null) {
            filters = new LinkedList<>();
            filters.add(filter);
        }
        setFilter(filters, autoRelease);
    }

    /**
     * Set an gpu image filer to use. <br/>
     * The previous ones would be released automatically.
     *
     * @param filter gpu image filter to set, null means disable filter
     */
    public void setFilter(ImgFilterBase filter) {
        List<ImgFilterBase> filters = null;
        if (filter != null) {
            filters = new LinkedList<>();
            filters.add(filter);
        }
        setFilter(filters);
    }

    /**
     * Set gpu image filter in array, you can set a image group filter with this method,
     * the filters set would be connected in order.<br/>
     * The previous ones would be released automatically.
     *
     * @param filterArray filter array to set, null or empty means disable filter
     */
    public void setFilter(ImgFilterBase[] filterArray) {
        List<ImgFilterBase> filters = null;
        if (filterArray != null && filterArray.length > 0) {
            filters = new LinkedList<>();
            Collections.addAll(filters, filterArray);
        }
        setFilter(filters);
    }

    /**
     * Set gpu image filter in list, you can set a image group filter with this method,
     * the filters set would be connected in order.<br/>
     * The previous ones would be released automatically.
     *
     * @param filters filter list to set, null or empty means disable filter
     * @param autoRelease should added filters be auto released.
     *                    If set to false, the all added filters should be released by user.
     */
    public void setFilter(List<? extends ImgFilterBase> filters, boolean autoRelease) {
        if (mOnErrorListener != null && filters != null && !filters.isEmpty()) {
            for (ImgFilterBase filter : filters) {
                filter.setOnErrorListener(mOnErrorListener);
            }
        }
        synchronized (mFiltersLock) {
            if (!mFilters.isEmpty()) {
                mFilters.get(mFilters.size() - 1).getSrcPin().disconnect(false);
                mInputFilter.mSrcPin.disconnect(autoRelease);
                mFilters.clear();
            } else if (filters != null && !filters.isEmpty()) {
                mInputFilter.mSrcPin.disconnect(false);
            }
            if (filters == null || filters.isEmpty()) {
                if (mExtraFilters.isEmpty()) {
                    mInputFilter.mSrcPin.connect(mOutputFilter.mSinkPin);
                } else {
                    mInputFilter.mSrcPin.connect(mExtraFilters.get(0).getSinkPin());
                }
            } else {
                mInputFilter.mSrcPin.connect(filters.get(0).getSinkPin());
                for (int i = 1; i < filters.size(); i++) {
                    filters.get(i - 1).getSrcPin().connect(filters.get(i).getSinkPin());
                }
                if (mExtraFilters.isEmpty()) {
                    filters.get(filters.size() - 1).getSrcPin().connect(mOutputFilter.mSinkPin);
                } else {
                    filters.get(filters.size() - 1).getSrcPin()
                            .connect(mExtraFilters.get(0).getSinkPin());
                }
                mFilters.addAll(filters);
            }
        }
    }

    /**
     * Set gpu image filter in list, you can set a image group filter with this method,
     * the filters set would be connected in order.<br/>
     * The previous ones would be released automatically.
     *
     * @param filters filter list to set, null or empty means disable filter
     */
    public void setFilter(List<? extends ImgFilterBase> filters) {
        setFilter(filters, true);
    }

    /**
     * Replace the filter in filter list to new one.
     *
     * The replaced filter will be auto released after this call, and should not be reused.
     *
     * @param replaced  filter to be replaced, must not be null
     * @param filter    filter to replace, null means disable the previous filter
     * @throws InvalidParameterException the filter to be replaced not found
     */
    public void replaceFilter(@NonNull ImgFilterBase replaced, @Nullable ImgFilterBase filter) {
        replaceFilter(replaced, filter, true);
    }

    /**
     * Replace the filter in filter list to new one.
     *
     * @param replaced filter to be replaced, must not be null
     * @param filter   filter to replace, null means disable the previous filter
     * @param autoRelease should the replaced filter be auto released.
     *                    If set to false, the replaced filter should be released by user.
     * @throws InvalidParameterException the filter to be replaced not found
     */
    public void replaceFilter(@NonNull ImgFilterBase replaced, @Nullable ImgFilterBase filter,
                              boolean autoRelease)
            throws InvalidParameterException {
        synchronized (mFiltersLock) {
            if (VERBOSE) {
                Log.d(TAG, "replaceFilter " + replaced + " to " + filter);
            }
            if (mFilters.isEmpty() || !mFilters.contains(replaced)) {
                throw new InvalidParameterException("The filter to be replaced not found!");
            }
            if (mOnErrorListener != null && filter != null) {
                filter.setOnErrorListener(mOnErrorListener);
            }

            SrcPin<ImgTexFrame> preSrcPin;
            SinkPin<ImgTexFrame> nextSinkPin;
            int idx = mFilters.indexOf(replaced);
            if (idx == 0) {
                preSrcPin = mInputFilter.mSrcPin;
            } else {
                preSrcPin = mFilters.get(idx - 1).getSrcPin();
            }
            if (idx == mFilters.size() - 1) {
                if (mExtraFilters.isEmpty()) {
                    nextSinkPin = mOutputFilter.mSinkPin;
                } else {
                    nextSinkPin = mExtraFilters.get(0).getSinkPin();
                }
            } else {
                nextSinkPin = mFilters.get(idx + 1).getSinkPin();
            }
            replaced.getSrcPin().disconnect(false);
            preSrcPin.disconnect(autoRelease);
            if (filter != null) {
                preSrcPin.connect(filter.getSinkPin());
                filter.getSrcPin().connect(nextSinkPin);
                mFilters.set(idx, filter);
            } else {
                preSrcPin.connect(nextSinkPin);
                mFilters.remove(idx);
            }
        }
    }

    /**
     * Add filter to the end of the filter list.
     *
     * @param filter filter to add
     */
    public void addFilter(ImgFilterBase filter) {
        if (filter == null) {
            return;
        }
        if (mOnErrorListener != null) {
            filter.setOnErrorListener(mOnErrorListener);
        }

        synchronized (mFiltersLock) {
            if (VERBOSE) {
                ImgFilterBase lastFilter = null;
                if (mFilters.size() > 0) {
                    lastFilter = mFilters.getLast();
                }
                Log.d(TAG, "addFilter " + filter + " after " + lastFilter);
            }

            SrcPin<ImgTexFrame> preSrcPin;
            SinkPin<ImgTexFrame> nextSinkPin;
            if (mFilters.isEmpty()) {
                preSrcPin = mInputFilter.mSrcPin;
            } else {
                preSrcPin = mFilters.getLast().getSrcPin();
            }
            if (mExtraFilters.isEmpty()) {
                nextSinkPin = mOutputFilter.mSinkPin;
            } else {
                nextSinkPin = mExtraFilters.get(0).getSinkPin();
            }
            preSrcPin.disconnect(false);
            preSrcPin.connect(filter.getSinkPin());
            filter.getSrcPin().connect(nextSinkPin);
            mFilters.add(filter);
        }
    }

    /**
     * Add filter after the specified filter.
     *
     * @param previous the filter to add after
     * @param filter   the filter to be added
     * @throws InvalidParameterException the specified previous filter not found
     */
    public void addFilterAfter(@NonNull ImgFilterBase previous, ImgFilterBase filter) {
        if (filter == null) {
            return;
        }

        synchronized (mFiltersLock) {
            if (mFilters.isEmpty() || !mFilters.contains(previous)) {
                throw new InvalidParameterException("The filter specified not found!");
            }
            if (mOnErrorListener != null) {
                filter.setOnErrorListener(mOnErrorListener);
            }
            if (VERBOSE) {
                Log.d(TAG, "addFilter " + filter + " after " + previous);
            }

            SinkPin<ImgTexFrame> nextSinkPin;
            int idx = mFilters.indexOf(previous);
            if (idx == mFilters.size() - 1) {
                if (mExtraFilters.isEmpty()) {
                    nextSinkPin = mOutputFilter.mSinkPin;
                } else {
                    nextSinkPin = mExtraFilters.get(0).getSinkPin();
                }
            } else {
                nextSinkPin = mFilters.get(idx + 1).getSinkPin();
            }
            previous.getSrcPin().disconnect(false);
            previous.getSrcPin().connect(filter.getSinkPin());
            filter.getSrcPin().connect(nextSinkPin);
            mFilters.add(idx + 1, filter);
        }
    }

    /**
     * Add filter before the specified filter.
     *
     * @param next   the filter to add after
     * @param filter the filter to be added
     * @throws InvalidParameterException the specified next filter not found
     */
    public void addFilterBefore(@NonNull ImgFilterBase next, ImgFilterBase filter) {
        if (filter == null) {
            return;
        }

        synchronized (mFiltersLock) {
            if (mFilters.isEmpty() || !mFilters.contains(next)) {
                throw new InvalidParameterException("The filter specified not found!");
            }
            if (mOnErrorListener != null) {
                filter.setOnErrorListener(mOnErrorListener);
            }
            if (VERBOSE) {
                Log.d(TAG, "addFilter " + filter + " before " + next);
            }

            SrcPin<ImgTexFrame> preSrcPin;
            int idx = mFilters.indexOf(next);
            if (idx == 0) {
                preSrcPin = mInputFilter.mSrcPin;
            } else {
                preSrcPin = mFilters.get(idx - 1).getSrcPin();
            }
            preSrcPin.disconnect(false);
            preSrcPin.connect(filter.getSinkPin());
            filter.getSrcPin().connect(next.getSinkPin());
            mFilters.add(idx, filter);
        }
    }

    /**
     * Get current set filters.
     *
     * @return current set filters
     */
    synchronized public List<ImgFilterBase> getFilter() {
        return mFilters;
    }

    /**
     * Set extra filter, which would be added after the filters set by setFilter.
     *
     * The previous extra filters will be auto released, and should not be used again.
     *
     * @param filter extra filter to set
     * @param autoRelease should the replaced filter be auto released.
     *                    If set to false, the replaced filter should be released by user.
     *
     */
    public void setExtraFilter(ImgFilterBase filter, boolean autoRelease) {
        List<ImgFilterBase> filters = null;
        if (filter != null) {
            filters = new LinkedList<>();
            filters.add(filter);
        }
        setExtraFilter(filters, autoRelease);
    }

    /**
     * Set extra filter, which would be added after the filters set by setFilter.
     *
     * The previous extra filters will be auto released, and should not be used again.
     *
     * @param filter extra filter to set
     */
    public void setExtraFilter(ImgFilterBase filter) {
        List<ImgFilterBase> filters = null;
        if (filter != null) {
            filters = new LinkedList<>();
            filters.add(filter);
        }
        setExtraFilter(filters);
    }

    /**
     * Set extra filters, which would be added after the filters set by setFilter.
     *
     * The previous extra filters will be auto released, and should not be used again.
     *
     * @param filters extra filters to set
     */
    public void setExtraFilter(List<? extends ImgFilterBase> filters, boolean autoRelease) {
        synchronized (mFiltersLock) {
            if (!mExtraFilters.isEmpty()) {
                mExtraFilters.get(mExtraFilters.size() - 1).getSrcPin().disconnect(false);
                if (mFilters.isEmpty()) {
                    mInputFilter.mSrcPin.disconnect(autoRelease);
                } else {
                    mFilters.get(mFilters.size() - 1).getSrcPin().disconnect(true);
                }
                mExtraFilters.clear();
            } else if (filters != null && !filters.isEmpty()) {
                if (mFilters.isEmpty()) {
                    mInputFilter.mSrcPin.disconnect(false);
                } else {
                    mFilters.get(mFilters.size() - 1).getSrcPin().disconnect(false);
                }
            }
            if (filters == null || filters.isEmpty()) {
                if (mFilters.isEmpty()) {
                    mInputFilter.mSrcPin.connect(mOutputFilter.mSinkPin);
                } else {
                    mFilters.get(mFilters.size() - 1).getSrcPin().connect(mOutputFilter.mSinkPin);
                }
            } else {
                if (mFilters.isEmpty()) {
                    mInputFilter.mSrcPin.connect(filters.get(0).getSinkPin());
                } else {
                    mFilters.get(mFilters.size() - 1).getSrcPin()
                            .connect(filters.get(0).getSinkPin());
                }
                for (int i = 1; i < filters.size(); i++) {
                    filters.get(i - 1).getSrcPin().connect(filters.get(i).getSinkPin());
                }
                filters.get(filters.size() - 1).getSrcPin().connect(mOutputFilter.mSinkPin);
                mExtraFilters.addAll(filters);
            }
        }
    }

    /**
     * Set extra filters, which would be added after the filters set by setFilter.
     *
     * The previous extra filters will be auto released, and should not be used again.
     *
     * @param filters extra filters to set
     */
    public void setExtraFilter(List<? extends ImgFilterBase> filters) {
        setExtraFilter(filters, true);
    }

    /**
     * Get current set extra filters.
     *
     * @return current set extra filters.
     */
    public List<ImgFilterBase> getExtraFilters() {
        return mExtraFilters;
    }

    public void release() {
        synchronized (mFiltersLock) {
            mInputFilter.mSrcPin.disconnect(true);
            mFilters.clear();
            mExtraFilters.clear();
        }
    }
}
