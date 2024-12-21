package wiki.chiu.micro.common.lang;


import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author mingchiuli
 * @create 2021-12-14 11:58 AM
 */
public class Const {

    public static final String EMAIL_CODE = "email_code";

    public static final String PHONE_CODE = "phone_code";

    public static final String SMS_CODE= "sms_code";

    public static final String EMAIL_KEY = "email_validation:";

    public static final String PHONE_KEY = "phone_validation:";

    public static final String PASSWORD_KEY = "password_validation:";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String BLOCK_USER = "block_user:";

    public static final String DAY_VISIT = "{visit_record}_day";

    public static final String WEEK_VISIT = "{visit_record}_week";

    public static final String MONTH_VISIT = "{visit_record}_month";

    public static final String YEAR_VISIT = "{visit_record}_year";

    public static final String A_WEEK = "604899";

    public static final String HOT_READ = "hot_read";

    public static final String QUERY_DELETED = "del_blog_user:";

    public static final String READ_TOKEN = "read_token:";

    public static final String TEMP_EDIT_BLOG = "temp_edit_blog:";

    public static final String PARAGRAPH_SPLITTER = "\n\n";

    public static final String PARAGRAPH_PREFIX = "para::";

    public static final String HOT_BLOGS = "hot_blogs";

    public static final String HOT_BLOG = "hot_blog";

    public static final String BLOG_STATUS = "blog_status";

    public static final String BLOOM_FILTER_BLOG = "bloom_filter_blog";

    public static final String BLOOM_FILTER_PAGE = "bloom_filter_page";

    public static final String BLOOM_FILTER_YEAR_PAGE = "bloom_filter_page_";

    public static final String BLOOM_FILTER_YEARS = "bloom_filter_years";

    public static final String REGISTER_PREFIX = "register_prefix:";

    public static final String USER = "user";

    public static final String REFRESH = "refresh";

    public static final String ALL_SERVICE = "all_service";

    public static final String ROLE_AUTHORITY = "role_authority";

    public static final String BLOG_TABLE = "m_blog";

    public static final String BLOG_SENSITIVE_TABLE = "m_blog_sensitive_content";

    public static final String AUTHORITY_TABLE = "m_authority";

    public static final String ROLE_AUTHORITY_TABLE = "m_role_authority";

    public static final String MENU_TABLE = "m_menu";

    public static final String ROLE_MENU_TABLE = "m_role_menu";

    public static final String ROLE_TABLE = "m_role";

    public static final String USER_TABLE = "m_user";

    public static final String USER_ROLE_TABLE = "m_user_role";

    public static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    public static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$");

    public static final String ES_QUEUE = "blog.change.queue.es";

    public static final String CACHE_QUEUE = "blog.change.queue.cache";

    public static final String BLOG_CHANGE_FANOUT_EXCHANGE = "blog.change.fanout.exchange";

    public static final String USER_QUEUE = "user.auth.menu.change.queue.auth";

    public static final String USER_CHANGE_FANOUT_EXCHANGE = "user.auth.menu.change.fanout.exchange";

    public static final String CODE_KEY = "code";

    public static final String TRY_COUNT_KEY = "try_count";

    public static final int SUCCESS_CODE = 200;

    public static final Set<Integer> BLOG_STATUS_SET = Arrays.stream(BlogStatusEnum.values())
            .map(BlogStatusEnum::getCode)
            .collect(Collectors.toSet());

    public static final Set<Integer> SENSITIVE_SET = Arrays.stream(SensitiveTypeEnum.values())
            .map(SensitiveTypeEnum::getCode)
            .collect(Collectors.toSet());
}

