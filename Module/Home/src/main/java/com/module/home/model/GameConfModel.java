package com.module.home.model;

public class GameConfModel {
    /**
     * isSupport : true
     * detail : {"isOpen":true,"content":""}
     */

    private boolean isSupport;
    private DetailBean detail;

    public boolean isIsSupport() {
        return isSupport;
    }

    public void setIsSupport(boolean isSupport) {
        this.isSupport = isSupport;
    }

    public DetailBean getDetail() {
        return detail;
    }

    public void setDetail(DetailBean detail) {
        this.detail = detail;
    }

    public static class DetailBean {
        /**
         * isOpen : true
         * content :
         */

        private boolean isOpen;
        private String content;

        public boolean isIsOpen() {
            return isOpen;
        }

        public void setIsOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
