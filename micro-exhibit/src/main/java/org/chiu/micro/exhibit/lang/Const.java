package org.chiu.micro.exhibit.lang;

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
    
    DAY_VISIT("{visit_record}_day"),

    WEEK_VISIT("{visit_record}_week"),

    MONTH_VISIT("{visit_record}_month"),

    YEAR_VISIT("{visit_record}_year"),

    READ_TOKEN("read_token:"),

    HOT_BLOGS("hot_blogs"),

    HOT_BLOG("hot_blog"),

    BLOG_STATUS("blog_status"),

    BLOOM_FILTER_BLOG("bloom_filter_blog"),

    BLOOM_FILTER_PAGE("bloom_filter_page"),

    BLOOM_FILTER_YEAR_PAGE("bloom_filter_page_"),

    BLOOM_FILTER_YEARS("bloom_filter_years"),

    HOT_READ("hot_read");
        
    private final String info;

}

