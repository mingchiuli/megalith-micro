package org.chiu.micro.auth.token;

import java.util.List;

import org.chiu.micro.auth.exception.AuthException;

public interface TokenUtils<T> {

    String generateToken(String userId, List<String> roles, long expire);

    T getVerifierByToken(String token) throws AuthException;
}
