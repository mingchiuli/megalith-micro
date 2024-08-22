package org.chiu.micro.auth.service;

/**
 * @author mingchiuli
 * @create 2022-11-27 8:27 pm
 */
public interface CodeService {

    void createEmailCode(String loginName);

    void createSMSCode(String loginSMS);
}
