package org.joksin.springoauth2.commons.oauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class InvalidOAuthServerResponseStatusException extends RuntimeException {

    public InvalidOAuthServerResponseStatusException(HttpStatusCode httpStatusCode) {
        super(String.format("Error while fetching data from OAuth server. Response status: %s", httpStatusCode.value()));
    }

}
