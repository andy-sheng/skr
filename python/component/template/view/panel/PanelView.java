package ${PACKAGE};

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
public class ${NAME1} extends BaseBottomPanel<LinearLayout, RelativeLayout>
	implements View.OnClickListener, IComponentView<${NAME1}.IPresenter, ${NAME1}.IView> {
	private static final String TAG = "${NAME1}";

	@Nullable
	protected IPresenter mPresenter;

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
		return 0; // [TODO-COMPONENT replace to correct LayoutRes]
	}

	@Override
	public void setPresenter(@Nullable IPresenter iPresenter) {
		mPresenter = iPresenter;
	}

	public ${NAME1}(@NonNull RelativeLayout parentView) {
		super(parentView);
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
