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
 * @module [TODO-COMPONENT add module]
 */
public class ${NAME1} implements IComponentView<${NAME1}.IPresenter, ${NAME1}.IView> {
	private static final String TAG = "${NAME1}";

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
		class ComponentView implements IView {
			@Override
			public <T extends View> T getRealView() {
				return null; // [TODO-COMPONENT return real view of proxy]
			}
		}
		return new ComponentView();
	}

	public interface IPresenter {
	}
	
	public interface IView extends IViewProxy {
	}
}
