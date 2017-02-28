package ${PACKAGE};

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 *
 * @module [TODO add module]
 */
public class ${NAME}Presenter extends ComponentPresenter<${NAME}View.IView> implements ${NAME}View.IPresenter {
	
	@Nullable
	@Override
	protected IAction createAction() {
		return new Action();
	}
	
	public class Action implements IAction {
		@Override
		public boolean onAction(int source, @Nullable Params params) {
			switch (source) {
			}
			return false;
		}
	}
}
