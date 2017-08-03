package ${PACKAGE};

import android.util.Log;

import com.thornbirds.component.ComponentController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 *
 * Generated using ${COMMAND}
 *
 * @module [TODO add module]
 */
public class ${NAME}Presenter extends ComponentPresenter<${NAME}Panel.IView, ComponentController>
		implements ${NAME}Panel.IPresenter {
    private static final String TAG = "${NAME}Presenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ${NAME}Presenter(ComponentController controller) {
        super(controller);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            Log.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            default:
            break;
        }
        return false;
    }
}
