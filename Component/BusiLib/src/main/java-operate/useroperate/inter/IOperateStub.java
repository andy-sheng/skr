package useroperate.inter;

public interface IOperateStub<T> {
    int getFriendType();  //0 好友，1 关注，2 粉丝

    String getTitle();

    int getViewLayout();

    IOperateHolder<T> getHolder();
}
