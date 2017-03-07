package ${PACKAGE};

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 *
 * Generated using ${COMMAND}
 *
 * @module [TODO add module]
 */
public class ${NAME}Panel extends BaseBottomPanel<LinearLayout, RelativeLayout>
	implements View.OnClickListener, IComponentView<${NAME}Panel.IPresenter, ${NAME}Panel.IView> {
	private static final String TAG = "${NAME}Panel";

	@Nullable
	protected IPresenter mPresenter;

	// Auto-generated to easy use setOnClickListener
	protected final void $click(View view, View.OnClickListener listener) {
		if (view != null) {
			view.setOnClickListener(listener);
		}
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	protected int getLayoutResId() {
		return 0; // TODO replace to correct LayoutRes
	}

	@Override
	public void setPresenter(@Nullable IPresenter iPresenter) {
		mPresenter = iPresenter;
	}

	public ${NAME}Panel(@NonNull RelativeLayout parentView) {
		super(parentView);
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
