package ${PACKAGE};

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 *
 * Generated using ${COMMAND}
 *
 * @module [TODO add module]
 */
public class ${NAME}Presenter extends ComponentPresenter {
    private static final String TAG = "${NAME}Presenter";

	@Nullable
	@Override
	protected IAction createAction() {
		return new Action();
	}

	public class Action implements IAction {
		@Override
		public boolean onAction(int source, @Nullable Params params) {
			switch (source) {
				default:
					break;
			}
			return false;
		}
	}
}
