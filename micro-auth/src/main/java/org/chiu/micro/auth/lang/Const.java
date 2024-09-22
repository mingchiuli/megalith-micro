package org.chiu.micro.auth.lang;


/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */
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

    HOT_MENUS_AND_BUTTONS("hot_menus_and_buttons"),

    USER_SERVICE("micro-user"),

    BLOG_SERVICE("micro-blog"),

    SEARCH_SERVICE("micro-search"),

    EXHIBIT_SERVICE("micro-exhibit"),

    AUTH_SERVICE("micro-auth"),

    GATEWAY_SERVICE("micro-gateway"),

    WHITELIST("whitelist"),

    DAY_VISIT("{visit_record}_day"),

    WEEK_VISIT("{visit_record}_week"),

    MONTH_VISIT("{visit_record}_month"),

    YEAR_VISIT("{visit_record}_year");

    private final String info;

    Const(String info) {
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }
}

