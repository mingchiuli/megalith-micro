package org.chiu.micro.user.lang;


/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */
public enum Const {

    BLOCK_USER("block_user:"),

    REGISTER_PREFIX("register_prefix:"),

    USER("user"),

    WHITELIST("whitelist");

    private final String info;

    Const(String info) {
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }


}

