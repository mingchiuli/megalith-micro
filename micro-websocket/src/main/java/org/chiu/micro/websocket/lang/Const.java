package org.chiu.micro.websocket.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */
@Getter
@AllArgsConstructor
public enum Const {

    TEMP_EDIT_BLOG("temp_edit_blog:"),
    
    SERVICE("micro-websocket"),
    
    HTTP("http"),
    
    WS("ws"),
    
    WHITELIST("whitelist");

    private final String info;

}

