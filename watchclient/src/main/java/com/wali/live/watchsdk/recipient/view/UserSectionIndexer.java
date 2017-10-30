package com.wali.live.watchsdk.recipient.view;

import android.widget.SectionIndexer;

import com.base.utils.CommonUtils;
import com.mi.live.data.data.UserListData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangzhiyuan on 16-4-19.
 */
public class UserSectionIndexer implements SectionIndexer {


    private String ALPHABET_STAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
    private String[] sections;
    private Map<Integer, Integer> mSectionForPosition = new HashMap<>();
    private Map<Integer, Integer> mPositionForSection = new HashMap<>();
    private List<Object> mDataList;

    private int mHeaderPositionOffset = 0;

    public UserSectionIndexer(){

    }
    public UserSectionIndexer(List<Object> mDataList){
        this.mDataList=mDataList;
    }

    public void setHeaderPositionOffset(int offset){
        mHeaderPositionOffset = offset;
    }

    public void setDataList(List<Object> mDataList){
        this.mDataList=mDataList;
        mSectionForPosition.clear();
        mPositionForSection.clear();
    }

    @Override
    public Object[] getSections() {
        if(sections==null||sections.length==0){
            sections = new String[ALPHABET_STAR.length()];
            for (int i = 0; i < ALPHABET_STAR.length(); i++) {
                sections[i] = String.valueOf(ALPHABET_STAR.charAt(i));
            }
        }
        return sections;
    }

    @Override
    public int getSectionForPosition(int position) {
        position -= mHeaderPositionOffset;
        if(mSectionForPosition.containsKey(position)){
            return mSectionForPosition.get(position);
        }
        if (mDataList == null||position<=0||mDataList.size()==0) {
            mSectionForPosition.put(position,0);
            return 0;
        }
        if(!(mDataList.get(0) instanceof UserListData)){
            return 0;
        }
        if (position > mDataList.size()- 1) {
            mSectionForPosition.put(position,sections.length - 1);
            return sections.length - 1;
        } else {
            UserListData userData = (UserListData)mDataList.get(position);
            if(userData==null){
                mSectionForPosition.put(position,0);
                return 0;
            }
            final char firstLetter = CommonUtils.getFirstLetterByName(userData.userNickname);
            int section = ALPHABET_STAR.indexOf(firstLetter);
            mSectionForPosition.put(position,section);
            return section;
        }

    }

    @Override
    public int getPositionForSection(int section) {

        if(mPositionForSection.containsKey(section)){
            return mPositionForSection.get(section);
        }

        if(mDataList == null||section <= 0||mDataList.size()==0){
            mPositionForSection.put(section,mHeaderPositionOffset);
            return mHeaderPositionOffset;
        }
        if(!(mDataList.get(0) instanceof UserListData)){
            return mHeaderPositionOffset;
        }
        //从当前的section往前查，直到遇到第一个有对应item的为止，否则不进行定位
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < mDataList.size(); j++) {
                UserListData userData = (UserListData)mDataList.get(j);
                if(userData==null){
                    mPositionForSection.put(section, mHeaderPositionOffset);
                    return mHeaderPositionOffset;
                }
                final char firstLetter = CommonUtils.getFirstLetterByName(userData.userNickname);
                //查询数字
                if (i == 0) {
                    for (int k = 0; k < 9; k++) {
                        //value是item的首字母
                        if (CommonUtils.match(String.valueOf(firstLetter), String.valueOf(k))) {
                            mPositionForSection.put(section, j + mHeaderPositionOffset);
                            return j + mHeaderPositionOffset;
                        }
                    }
                }
                //查询字母
                else {
                    //value是item的首字母
                    if (CommonUtils.match(String.valueOf(firstLetter), String.valueOf(ALPHABET_STAR.charAt(i)))) {
                        mPositionForSection.put(section, j + mHeaderPositionOffset);
                        return j + mHeaderPositionOffset;
                    }
                }
            }
        }
        mPositionForSection.put(section,mHeaderPositionOffset);
        return mHeaderPositionOffset;
    }


}
