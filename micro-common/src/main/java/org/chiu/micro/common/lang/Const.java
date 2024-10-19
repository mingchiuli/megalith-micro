package org.chiu.micro.common.lang;


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

    WEBSOCKET_SERVICE("micro-websocket"),

    DAY_VISIT("{visit_record}_day"),

    WEEK_VISIT("{visit_record}_week"),

    MONTH_VISIT("{visit_record}_month"),

    YEAR_VISIT("{visit_record}_year"),

    A_WEEK("604899"),

    HOT_READ("hot_read"),

    QUERY_DELETED("del_blog_user:"),

    READ_TOKEN("read_token:"),

    TEMP_EDIT_BLOG("temp_edit_blog:"),

    PARAGRAPH_SPLITTER("\n\n"),

    PARAGRAPH_PREFIX("para::"),

    HOT_BLOGS("hot_blogs"),

    HOT_BLOG("hot_blog"),

    BLOG_STATUS("blog_status"),

    BLOOM_FILTER_BLOG("bloom_filter_blog"),

    BLOOM_FILTER_PAGE("bloom_filter_page"),

    BLOOM_FILTER_YEAR_PAGE("bloom_filter_page_"),

    BLOOM_FILTER_YEARS("bloom_filter_years"),

    REGISTER_PREFIX("register_prefix:"),

    USER("user");

    private final String info;

    Const(String info) {
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }
}

