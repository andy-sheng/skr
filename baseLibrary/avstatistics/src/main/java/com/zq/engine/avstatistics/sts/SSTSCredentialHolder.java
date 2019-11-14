package com.zq.engine.avstatistics.sts;


/**
 * @author gongjun@skrer.net
 * @date 2019.11.12
 * @brief log-services use this interface to get/valid the token stuff
 */
public interface SSTSCredentialHolder
{
    /**
     * @brief implementation rules for {@link this#isExpired()}: if the credential is expired, then get the new credential automatically.
     */
    boolean isExpired();

    String getAK();
    String getSK();
    String getToken();
}