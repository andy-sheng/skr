为了能在任意目录下使用本python包下的Python脚本

打开终端，切换本目录，运行如下命令即可：
sh setup_python.sh

然后，切换到目标路径，使用如下命令运行Python脚本即可：
python -m python_file [params]

[示例]在watchclient模块下生成TestView和TestPresenter：
cd ~/Development/huyu/livesdk/watchclient
python -m create_view_with_presenter Test