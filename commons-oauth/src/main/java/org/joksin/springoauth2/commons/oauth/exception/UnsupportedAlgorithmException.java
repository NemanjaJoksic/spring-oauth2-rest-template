package org.joksin.springoauth2.commons.oauth.exception;

public class UnsupportedAlgorithmException extends RuntimeException {

    public UnsupportedAlgorithmException(String algorithm) {
        super(String.format("Algorithm %s is not supported", algorithm));
    }

}
