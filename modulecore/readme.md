核心功能库，几乎跟app无关，几乎每个app都有的核心业务放这。如账号管理，个人信息等

greendao 设置联合组主键 的方法
https://github.com/greenrobot/greenDAO/issues/712

GreenDao OpenHelper
MigrationHelper 数据库升级的帮助类

android layout drawable 等也要分模块
方法是放到各自的 sourceSets 里
res.srcDirs += ['src/main/res-login']