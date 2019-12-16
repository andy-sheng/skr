package com.module.club;


import com.alibaba.android.arouter.facade.template.IProvider;

public interface IClubModuleService extends IProvider {
    void tryGoClubHomePage(int clubID);
}