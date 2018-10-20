注意！！！！！

务必保持 module A 的接口的包名 与 module A 的业务的包名，一致，以方便
实体类或相应的接口的下沉到 commonservice 层
举个例子
如 channel module 中的 com.wali.live.modulechannel.model.ChannelShowModel。
因为别的module也要使用，需要下沉。
则要保证下沉到 commonservice 时也要保证包名等路径要相同。