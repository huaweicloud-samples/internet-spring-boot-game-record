package com.huaweicloud.scorekeep.rules;

/** 落子不合法。 */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
