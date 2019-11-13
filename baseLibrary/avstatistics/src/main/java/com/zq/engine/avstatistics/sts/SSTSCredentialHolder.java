package com.zq.engine.avstatistics.sts;


/**
 * @author gongjun@skrer.net
 * @date 2019.11.12
 * @brief log-services mode use this interface to get/valid the token when is prepared by top-level code-routine
 */
public interface SSTSCredentialHolder
{
    /**
     * @brief implementation rules for {@link this#isExpired()}: if the credential is expired, then get the new credential automatically.
     */
    boolean isExpired();
//
//    void performAuthentication() throws Exception;

    String getAK();
    String getSK();
    String getToken();
}