package component;

import android.support.annotation.Nullable;

/**
 * Basic interface of Event Observer
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 * @date 2017/5/7
 */
public interface IEventObserver {
    /**
     * Event Controller calls this to notify the Observer the an event arrives
     *
     * @param event  type of the Event
     * @param params parameters of the Event
     */
    boolean onEvent(int event, @Nullable Params params);
}