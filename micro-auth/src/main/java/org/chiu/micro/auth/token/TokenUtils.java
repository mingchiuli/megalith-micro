package org.chiu.micro.auth.token;

import org.chiu.micro.common.exception.AuthException;

import java.util.List;

public interface TokenUtils<T> {

    String generateToken(String userId, List<String> roles, long expire);

    T getVerifierByToken(String token) throws AuthException;
}
