package ${PACKAGE};

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 *
 * @module [TODO add module]
 */
public class ${NAME}Presenter extends ComponentPresenter<${NAME}View.IView>
		implements ${NAME}View.IPresenter {
    private static final String TAG = "${NAME}Presenter";

	@Nullable
	@Override
	protected IAction createAction() {
		return new Action();
	}

	public class Action implements IAction {
		@Override
		public boolean onAction(int source, @Nullable Params params) {
			if (mView == null) {
				MyLog.e(TAG, "onAction but mView is null, source=" + source);
				return false;
			}
			switch (source) {
				default:
					break;
			}
			return false;
		}
	}
}
