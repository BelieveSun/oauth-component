package com.believe.sun.shiro.error;

import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sungj on 17-8-14.
 */
@RestController
public class ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @ExceptionHandler(value = {UnauthenticatedException.class})
    public ResponseEntity error(UnauthenticatedException ex) {
        logger.info("Exception :", ex);
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }
}
