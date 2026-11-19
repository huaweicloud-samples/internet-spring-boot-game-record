package com.huaweicloud.scorekeep.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/** 资源标识生成器：产出短、无连字符、可直接用作键的随机标识。 */
public final class Identifiers {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Identifiers() {
    }

    /** 生成一个 40 位随机数并以 32 进制表示，返回大写字符串。 */
    public static String random() {
        return new BigInteger(40, SECURE_RANDOM).toString(32).toUpperCase();
    }
}
