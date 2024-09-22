package org.chiu.micro.websocket.lang;



/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */

public enum Const {

    TEMP_EDIT_BLOG("temp_edit_blog:"),

    WEBSOCKET_SERVICE("micro-websocket"),

    HTTP("http"),

    WS("ws"),

    WHITELIST("whitelist");

    private final String info;

    Const(String info) {
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }
}

