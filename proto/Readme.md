请将每个proto 生成到 属于 相应 模块 的 module 的 java-gen-pb 目录下
每个Proto 里有自己生成脚本


注意！！！！
option java_package = "com.wali.live.proto.Common";
option java_outer_classname = "CommonProto";
请不要使用 这些语法，没什么用，还不好进行包管理