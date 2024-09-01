package org.chiu.micro.gateway.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */
@Getter
@AllArgsConstructor
public enum Const {

    DAY_VISIT("{visit_record}_day"),

    WEEK_VISIT("{visit_record}_week"),

    MONTH_VISIT("{visit_record}_month"),

    YEAR_VISIT("{visit_record}_year"),
    
    HTTP("http"),
    
    WHITELIST("whitelist"),
    
    USER_SERVICE("micro-user"),
    
    BLOG_SERVICE("micro-blog"),
    
    SEARCH_SERVICE("micro-search"),
    
    EXHIBIT_SERVICE("micro-exhibit"),
    
    AUTH_SERVICE("micro-auth"),
    
    GATEWAY_SERVICE("micro-gateway");

    private final String info;

}

