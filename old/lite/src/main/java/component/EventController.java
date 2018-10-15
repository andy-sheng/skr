package component;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Basic definition of Event Controller
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 * @date 2017/5/7
 */
public abstract class EventController implements IEventController {
    protected final String TAG = getTAG();

    public static final int MSG_DEFAULT = 0;

    private final Map<Integer, Set<IEventObserver>> mEventActionMap = new HashMap<>();

    @Nullable
    protected abstract String getTAG();

    @Override
    public void registerObserverForEvent(int event, IEventObserver observer) {
        if (observer == null) {
            Log.e(TAG, "registerObserverForEvent but observer is null for event=" + event);
            return;
        }
        Set<IEventObserver> actionSet = mEventActionMap.get(event);
        if (actionSet == null) {
            actionSet = new LinkedHashSet<>(); // 保序
            mEventActionMap.put(event, actionSet);
        }
        actionSet.add(observer);
    }

    @Override
    public void unregisterObserverForEvent(int event, IEventObserver observer) {
        if (observer == null) {
            Log.e(TAG, "unregisterObserverForEvent but observer is null for event=" + event);
            return;
        }
        Set<IEventObserver> actionSet = mEventActionMap.get(event);
        if (actionSet != null) {
            actionSet.remove(observer);
        }
    }

    @Override
    public void unregisterObserver(IEventObserver observer) {
        if (observer == null) {
            Log.e(TAG, "unregisterObserver but observer is null");
            return;
        }
        for (Set<IEventObserver> actionSet : mEventActionMap.values()) {
            actionSet.remove(observer);
        }
    }

    @Override
    public boolean postEvent(int event) {
        return postEvent(event, null);
    }

    @Override
    public boolean postEvent(int event, @Nullable Params params) {
        Set<IEventObserver> actionSet = mEventActionMap.get(event);
        if (actionSet == null || actionSet.isEmpty()) {
            Log.e(TAG, "no action registered for source " + event);
            return false;
        }
        boolean result = false;
        for (IEventObserver action : actionSet) {
            result |= action.onEvent(event, params);
        }
        return result;
    }

    @Override
    public void release() {
        mEventActionMap.clear();
    }
}
