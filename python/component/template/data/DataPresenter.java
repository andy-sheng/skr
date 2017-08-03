package ${PACKAGE};

import com.thornbirds.component.ComponentController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 * <p>
 * Generated using ${COMMAND}
 *
 * @module [TODO add module]
 */
public class ${NAME}Presenter extends ComponentPresenter<Object, ComponentController> {
    private static final String TAG = "${NAME}Presenter";

	public TestPresenter(ComponentController controller) {
		super(controller);
	}

	@Override
	protected String getTAG() {
		return TAG;
	}

	@Override
	public boolean onEvent(int event, IParams params) {
		switch (event) {
			default:
			break;
		}
		return false;
	}
}
