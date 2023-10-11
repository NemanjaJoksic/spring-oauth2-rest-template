package org.joksin.springoauth2.commons.oauth.exception;

import org.joksin.springoauth2.commons.oauth.GrantType;

public class UnsupportedGrantTypeException extends RuntimeException {

    public UnsupportedGrantTypeException(GrantType grantType) {
        super(String.format("Grant type %s is not supported", grantType));
    }

}
