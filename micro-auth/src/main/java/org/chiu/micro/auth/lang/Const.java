package org.chiu.micro.auth.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */
@Getter
@AllArgsConstructor
public enum Const {

    EMAIL_CODE("email_code"),

    PHONE_CODE("phone_code"),
    
    SMS_CODE("sms_code"),

    EMAIL_KEY("email_validation:"),

    PHONE_KEY("phone_validation:"),

    PASSWORD_KEY("password_validation:"),
    
    TOKEN_PREFIX("Bearer "),

    ROLE_PREFIX("ROLE_"),
    
    BLOCK_USER("block_user:"),
    
    HOT_AUTHORITIES("hot_authorities"),

    HOT_MENUS_AND_BUTTONS("hot_menus_and_buttons"),;



    private final String info;

}

