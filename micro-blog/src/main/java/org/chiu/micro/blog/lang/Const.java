package org.chiu.micro.blog.lang;


/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */
public enum Const {

    A_WEEK("604899"),

    HOT_READ("hot_read"),

    QUERY_DELETED("del_blog_user:"),

    READ_TOKEN("read_token:"),

    TEMP_EDIT_BLOG("temp_edit_blog:"),

    PARAGRAPH_SPLITTER("\n\n"),

    PARAGRAPH_PREFIX("para::");

    private final String info;

    Const(String info) {
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }
}

