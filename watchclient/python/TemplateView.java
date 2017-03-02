package ${PACKAGE};

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 *
 * @module [TODO add module]
 */
public class ${NAME}View implements IComponentView<${NAME}View.IPresenter, ${NAME}View.IView> {
	
	// Auto-generated to easy use findViewById
	protected final <T extends View> T $(@IdRes int resId) {
		return (T) findViewById(resId);
	}

	// Auto-generated to easy use setOnClickListener
	protected final void $click(@IdRes int resId, View.OnClickListener listener) {
		View view = $(resId);
		if (view != null) {
			view.setOnClickListener(listener);
		}
	}
		
	@Override
	public IView getViewProxy() {
		/**
		 * 局部内部类，用于Presenter回调通知该View改变状态
		 */
		class ComponentView implements IView {
		}
		return new ComponentView();
	}

	public interface IPresenter {
	}
	
	public interface IView extends IViewProxy {
	}
}
