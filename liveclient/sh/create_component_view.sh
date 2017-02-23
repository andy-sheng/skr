#!bin/sh

# 文件目录
root_path="../src/main/java/com/wali/live/livesdk/live/component"
# 包名
root_package="com.wali.live.livesdk.live.component";

# 获取类名
name=$1
if [ ! $name ]; then
    echo "error: bad argument, a component name is required!"
    exit
fi
view_class="${name}View";
presenter_class="${name}Presenter";

# 获取文件名
view_file="${root_path}/view/${view_class}.java"
presenter_file="${root_path}/presenter/${presenter_class}.java"

# 获取用户名及日期
user_name=${LOGNAME}
user_date=`date "+%y/%m/%d"`

# 生成View
class=${view_class}
package="${root_package}.view"
import="import ${root_package}.presenter.${presenter_class};"
out_file=${view_file}
if [ ! -f ${out_file} ]; then
	echo "package ${package};

${import}

/**
 * Created by ${user_name} on ${user_date}.
 *
 * @module [TODO add module]
 */
public class ${class} implements IComponentView<${class}.IPresenter, ${class}.IView> {
	
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
}" > ${out_file}
else
 echo "waring: ${out_file} already exist!"
fi

# 生成Presenter
package="${root_package}.presenter"
class=${presenter_class}
import="import ${root_package}.view.${view_class};"
out_file=${presenter_file}
if [ ! -f ${out_file} ]; then
	echo "package ${package};

${import}

/**
 * Created by ${user_name} on ${user_date}.
 *
 * @module [TODO add module]
 */
public class ${class} extends ComponentPresenter<${view_class}.IView> implements ${view_class}.IPresenter {
	
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
}" > ${out_file}
else
 echo "waring: ${out_file} already exist!"
fi
