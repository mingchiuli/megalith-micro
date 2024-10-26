package wiki.chiu.micro.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author mingchiuli
 * @create 2022-11-27 8:27 pm
 */
public interface CodeService {

    void createEmailCode(String loginName);

    void createSMSCode(String loginSMS) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException;
}
