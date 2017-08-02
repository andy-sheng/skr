package ${PACKAGE};

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;

${IMPORT}

/**
 * Created by ${USER} on ${DATE}.
 *
 * Generated using ${COMMAND}
 *
 * @module [TODO add module]
 */
public class ${NAME}View implements IComponentView<${NAME}View.IPresenter, ${NAME}View.IView> {
	private static final String TAG = "${NAME}View";

	@Nullable
	protected IPresenter mPresenter;

	protected final <T extends View> T $(@IdRes int resId) {
		return (T) findViewById(resId);
	}

	protected final void $click(View view, View.OnClickListener listener) {
		if (view != null) {
			view.setOnClickListener(listener);
		}
	}

	@Override
	public void setPresenter(@Nullable IPresenter iPresenter) {
		mPresenter = iPresenter;
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
