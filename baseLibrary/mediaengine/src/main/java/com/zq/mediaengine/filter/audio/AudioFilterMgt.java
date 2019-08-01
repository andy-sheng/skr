package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.PinAdapter;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * AudioFilter manager for convenient use.
 */
public class AudioFilterMgt {
    private static final String TAG = "AudioFilterMgt";

    private PinAdapter<AudioBufFrame> mInputFilter;
    private PinAdapter<AudioBufFrame> mOutputFilter;

    private LinkedList<AudioFilterBase> mFilters;
    private final Object mFiltersLock = new Object();

    public AudioFilterMgt() {
        mInputFilter = new AudioBufPinAdapter();
        mOutputFilter = new AudioBufPinAdapter();
        mInputFilter.mSrcPin.connect(mOutputFilter.mSinkPin);
        mFilters = new LinkedList<>();
    }

    /**
     * Get input pin of current AudioFilterMgt.
     *
     * @return input pin instance
     */
    public SinkPin<AudioBufFrame> getSinkPin() {
        return mInputFilter.mSinkPin;
    }

    /**
     * Get output pin of current AudioFilterMgt.
     *
     * @return output pin instance
     */
    public SrcPin<AudioBufFrame> getSrcPin() {
        return mOutputFilter.mSrcPin;
    }

    /**
     * Set an audio filer to use.<br/>
     * The previous ones would be released automatically.
     *
     * @param filter audio filter to set, null means disable filter
     */
    public void setFilter(AudioFilterBase filter) {
        List<AudioFilterBase> filters = null;
        if (filter != null) {
            filters = new LinkedList<>();
            filters.add(filter);
        }
        setFilter(filters);
    }

    /**
     * Set audio filter in array, you can set a audio group filter with this method,
     * the filters set would be connected in order.<br/>
     * The previous ones would be released automatically.
     *
     * @param filterArray filter array to set, null or empty means disable filter
     */
    public void setFilter(AudioFilterBase[] filterArray) {
        List<AudioFilterBase> filters = null;
        if (filterArray != null && filterArray.length > 0) {
            filters = new LinkedList<>();
            Collections.addAll(filters, filterArray);
        }
        setFilter(filters);
    }

    /**
     * Set audio filter in list, you can set a audio group filter with this method,
     * the filters set would be connected in order.<br/>
     * The previous ones would be released automatically.
     *
     * @param filters filter list to set, null or empty means disable filter
     */
    public void setFilter(List<? extends AudioFilterBase> filters) {
        synchronized (mFiltersLock) {
            if (!mFilters.isEmpty()) {
                mFilters.get(mFilters.size() - 1).getSrcPin().disconnect(false);
                mInputFilter.mSrcPin.disconnect(true);
                mFilters.clear();
            } else if (filters != null && !filters.isEmpty()) {
                mInputFilter.mSrcPin.disconnect(false);
            }
            if (filters == null || filters.isEmpty()) {
                mInputFilter.mSrcPin.connect(mOutputFilter.mSinkPin);
            } else {
                mInputFilter.mSrcPin.connect(filters.get(0).getSinkPin());
                for (int i = 1; i < filters.size(); i++) {
                    filters.get(i - 1).getSrcPin().connect(filters.get(i).getSinkPin());
                }
                filters.get(filters.size() - 1).getSrcPin().connect(mOutputFilter.mSinkPin);
                mFilters.addAll(filters);
            }
        }
    }

    /**
     * Get current set filters.
     *
     * @return current set filters
     */
    public List<AudioFilterBase> getFilter() {
        synchronized (mFiltersLock) {
            return mFilters;
        }
    }
}
