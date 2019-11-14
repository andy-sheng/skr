package com.zq.engine.avstatistics.sts;


/**
 * @author gongjun@skrer.net
 * @date 2019.11.12
 * @brief log-services use this interface to get/valid the token stuff
 */
public interface SSTSCredentialHolder
{
    /**
     * @brief implementation rules for {@link this#getStatus()}: if the credential is expired, then get the new credential automatically.
     */
    ServiceStatus getStatus();

    String getAK();
    String getSK();
    String getToken();


    class ServiceStatus{
        public boolean isExpired;
        public boolean toCutOff;

        public ServiceStatus() {
            isExpired = true;
            toCutOff = false;
        }
    }

}