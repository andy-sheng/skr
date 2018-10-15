package component;

import android.support.annotation.CheckResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Define Params class which is used to pass parameters for Event Controller when dispatching a event
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 * @date 2017/5/7
 */
public class Params {
    private List<Object> params;

    public Params() {
    }

    public Params putItem(Object object) {
        if (params == null) {
            params = new ArrayList<>();
        }
        params.add(object);
        return this;
    }

    @CheckResult
    public <T extends Object> T firstItem() {
        return getItem(0);
    }

    @CheckResult
    public <T extends Object> T getItem(int index) {
        if (params == null || index >= params.size()) {
            return null;
        }
        try {
            T elem = (T) params.get(index);
            return elem;
        } catch (ClassCastException e) {
            // just ignore
        }
        return null;
    }
}