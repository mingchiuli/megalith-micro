-- Canonical pages, button permissions, and gateway routes shared by development and production.
-- Service host placeholders are replaced from megalith.admin-metadata.hosts before execution.
DROP TEMPORARY TABLE IF EXISTS seed_admin_menu;
DROP TEMPORARY TABLE IF EXISTS seed_admin_authority;
DROP TEMPORARY TABLE IF EXISTS seed_admin_menu_authority;

CREATE TEMPORARY TABLE seed_admin_menu (
    depth INT NOT NULL,
    parent_name VARCHAR(100) NULL,
    title VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    component VARCHAR(255) NOT NULL,
    type INT NOT NULL,
    icon VARCHAR(100) NOT NULL,
    order_num INT NOT NULL,
    status INT NOT NULL
);

INSERT INTO seed_admin_menu
    (depth, parent_name, title, name, url, component, type, icon, order_num, status)
VALUES
    (0, NULL, '后台管理系统', 'system-root', '/backend', 'sys/SystemView', 0, '', 1, 0),
    (1, 'system-root', '系统管理', 'system-manage', '', '', 0, 'Setting', 1, 0),
    (1, 'system-root', '日志管理', 'blog-manage', '', '', 0, 'House', 1, 0),
    (2, 'system-manage', '账号管理', 'system-users', '/sys/users', 'sys/UsersView', 1, 'User', 2, 0),
    (2, 'system-manage', '角色管理', 'system-roles', '/sys/roles', 'sys/RolesView', 1, 'UserFilled', 3, 0),
    (2, 'system-manage', '菜单管理', 'system-menus', '/sys/menus', 'sys/MenusView', 1, 'Rank', 4, 0),
    (2, 'system-manage', '权限管理', 'system-authorities', '/sys/authorities', 'sys/AuthorityView', 1, 'Open', 5, 0),
    (2, 'blog-manage', '内容管理', 'system-blogs', '/sys/blogs', 'sys/BlogsView', 1, 'Notebook', 4, 0),
    (2, 'blog-manage', '在线编辑', 'system-edit', '/sys/edit', 'sys/EditView', 1, 'EditPen', 5, 0),
    (2, 'blog-manage', '删除管理', 'system-del', '/sys/delete', 'sys/DeleteView', 1, 'Delete', 6, 0),
    (3, 'system-users', '新增', 'system-users-create', '', '', 2, 'primary', 1, 0),
    (3, 'system-users', '保存', 'system-users-save', '', '', 2, 'primary', 1, 0),
    (3, 'system-users', '批量删除', 'system-users-batch-del', '', '', 2, 'danger', 2, 0),
    (3, 'system-users', '内容导出', 'system-users-download', '', '', 2, 'info', 3, 0),
    (3, 'system-users', '注册链接', 'system-users-register', '', '', 2, 'warning', 4, 0),
    (3, 'system-users', '删除', 'system-users-delete', '', '', 2, 'danger', 5, 0),
    (3, 'system-users', '编辑', 'system-users-edit', '', '', 2, 'success', 6, 0),
    (3, 'system-users', '更新链接', 'system-users-modify-register', '', '', 2, 'info', 7, 0),
    (3, 'system-roles', '新增', 'system-roles-create', '', '', 2, 'primary', 1, 0),
    (3, 'system-roles', '保存', 'system-roles-save', '', '', 2, 'primary', 1, 0),
    (3, 'system-roles', '保存菜单', 'system-roles-menus-save', '', '', 2, 'primary', 1, 0),
    (3, 'system-roles', '保存数据权限', 'system-roles-data-save', '', '', 2, 'primary', 1, 0),
    (3, 'system-roles', '批量删除', 'system-roles-batch-del', '', '', 2, 'danger', 2, 0),
    (3, 'system-roles', '内容导出', 'system-roles-download', '', '', 2, 'info', 3, 0),
    (3, 'system-roles', '删除', 'system-roles-delete', '', '', 2, 'danger', 4, 0),
    (3, 'system-roles', '菜单权限', 'system-roles-menu-perm', '', '', 2, 'success', 5, 0),
    (3, 'system-roles', '数据权限', 'system-roles-data-perm', '', '', 2, 'success', 6, 0),
    (3, 'system-roles', '编辑', 'system-roles-edit', '', '', 2, 'primary', 7, 0),
    (3, 'system-menus', '新增', 'system-menus-create', '', '', 2, 'primary', 1, 0),
    (3, 'system-menus', '保存权限', 'system-menus-authorities-save', '', '', 2, 'primary', 1, 0),
    (3, 'system-menus', '保存', 'system-menus-save', '', '', 2, 'primary', 1, 0),
    (3, 'system-menus', '删除', 'system-menus-delete', '', '', 2, 'danger', 2, 0),
    (3, 'system-menus', '编辑', 'system-menus-edit', '', '', 2, 'primary', 3, 0),
    (3, 'system-menus', '内容导出', 'system-menus-download', '', '', 2, 'info', 4, 0),
    (3, 'system-menus', '接口权限', 'system-menus-authority-perm', '', '', 2, 'warning', 6, 0),
    (3, 'system-authorities', '新增', 'system-authorities-create', '', '', 2, 'primary', 1, 0),
    (3, 'system-authorities', '保存', 'system-authorities-save', '', '', 2, 'primary', 1, 0),
    (3, 'system-authorities', '批量删除', 'system-authorities-batch-del', '', '', 2, 'danger', 2, 0),
    (3, 'system-authorities', '内容导出', 'system-authorities-download', '', '', 2, 'info', 3, 0),
    (3, 'system-authorities', '编辑', 'system-authorities-edit', '', '', 2, 'primary', 4, 0),
    (3, 'system-authorities', '删除', 'system-authorities-delete', '', '', 2, 'danger', 5, 0),
    (3, 'system-blogs', '内容导出', 'system-blogs-download', '', '', 2, 'info', 1, 0),
    (3, 'system-blogs', '搜索', 'system-blogs-search', '', '', 2, 'primary', 2, 0),
    (3, 'system-blogs', '批量删除', 'system-blogs-batch-del', '', '', 2, 'danger', 3, 0),
    (3, 'system-blogs', '删除', 'system-blogs-delete', '', '', 2, 'danger', 4, 0),
    (3, 'system-blogs', '查看', 'system-blogs-check', '', '', 2, 'primary', 5, 0),
    (3, 'system-blogs', '编辑', 'system-blogs-edit', '', '', 2, 'success', 6, 0),
    (3, 'system-blogs', '密码', 'system-blogs-password', '', '', 2, 'warning', 7, 0),
    (3, 'system-edit', '保存版本', 'system-edit-submit', '', '', 2, 'primary', 1, 0),
    (3, 'system-edit', '封面', 'system-blogs-upload', '', '', 2, '', 2, 0),
    (3, 'system-edit', 'AI', 'system-edit-ai', '', '', 2, '', 3, 0),
    (3, 'system-edit', '保存草稿', 'system-edit-save-draft', '', '', 2, 'primary', 4, 0),
    (3, 'system-edit', '版本历史', 'system-edit-versions', '', '', 2, 'info', 5, 0),
    (3, 'system-edit', '查看版本', 'system-edit-version-view', '', '', 2, 'primary', 6, 0),
    (3, 'system-edit', '版本对比', 'system-edit-version-compare', '', '', 2, 'warning', 7, 0),
    (3, 'system-edit', '发布', 'system-edit-publish', '', '', 2, 'danger', 8, 0),
    (3, 'system-edit', '上下线', 'system-edit-status', '', '', 2, 'warning', 9, 0),
    (3, 'system-edit', '恢复版本', 'system-edit-restore-version', '', '', 2, 'success', 10, 0),
    (3, 'system-del', '恢复', 'system-delete-resume', '', '', 2, 'primary', 1, 0);

INSERT INTO m_menu
    (parent_id, title, name, url, component, type, icon, order_num, status)
SELECT
    COALESCE(parent.id, 0), seed.title, seed.name, seed.url, seed.component, seed.type,
    seed.icon, seed.order_num, seed.status
FROM seed_admin_menu seed
LEFT JOIN m_menu parent ON parent.name = seed.parent_name
WHERE seed.depth = 0
  AND NOT EXISTS (SELECT 1 FROM m_menu target WHERE target.name = seed.name);

INSERT INTO m_menu
    (parent_id, title, name, url, component, type, icon, order_num, status)
SELECT
    COALESCE(parent.id, 0), seed.title, seed.name, seed.url, seed.component, seed.type,
    seed.icon, seed.order_num, seed.status
FROM seed_admin_menu seed
LEFT JOIN m_menu parent ON parent.name = seed.parent_name
WHERE seed.depth = 1
  AND NOT EXISTS (SELECT 1 FROM m_menu target WHERE target.name = seed.name);

INSERT INTO m_menu
    (parent_id, title, name, url, component, type, icon, order_num, status)
SELECT
    COALESCE(parent.id, 0), seed.title, seed.name, seed.url, seed.component, seed.type,
    seed.icon, seed.order_num, seed.status
FROM seed_admin_menu seed
LEFT JOIN m_menu parent ON parent.name = seed.parent_name
WHERE seed.depth = 2
  AND NOT EXISTS (SELECT 1 FROM m_menu target WHERE target.name = seed.name);

INSERT INTO m_menu
    (parent_id, title, name, url, component, type, icon, order_num, status)
SELECT
    COALESCE(parent.id, 0), seed.title, seed.name, seed.url, seed.component, seed.type,
    seed.icon, seed.order_num, seed.status
FROM seed_admin_menu seed
LEFT JOIN m_menu parent ON parent.name = seed.parent_name
WHERE seed.depth = 3
  AND NOT EXISTS (SELECT 1 FROM m_menu target WHERE target.name = seed.name);

UPDATE m_menu target
JOIN seed_admin_menu seed ON seed.name = target.name
LEFT JOIN m_menu parent ON parent.name = seed.parent_name
SET target.parent_id = COALESCE(parent.id, 0),
    target.title = seed.title,
    target.url = seed.url,
    target.component = seed.component,
    target.type = seed.type,
    target.icon = seed.icon,
    target.order_num = seed.order_num,
    target.status = seed.status;

CREATE TEMPORARY TABLE seed_admin_authority (
    code VARCHAR(100) NOT NULL,
    remark VARCHAR(255) NOT NULL,
    prototype VARCHAR(20) NOT NULL,
    method_type VARCHAR(20) NOT NULL,
    route_pattern VARCHAR(255) NOT NULL,
    service_host VARCHAR(100) NOT NULL,
    service_port INT NOT NULL,
    type INT NOT NULL,
    status INT NOT NULL
);

INSERT INTO seed_admin_authority
    (code, remark, prototype, method_type, route_pattern, service_host, service_port, type, status)
VALUES
    ('auth:menu:nav', '获取用户菜单', 'http', 'GET', '/auth/menu/nav', '__AUTH_HOST__', 8081, 1, 0),
    ('token:userinfo', '获取登录用户回显信息', 'http', 'GET', '/token/userinfo', '__AUTH_HOST__', 8081, 1, 0),
    ('whitelist:token:refresh', '刷新密钥', 'http', 'POST', '/token/refresh', '__AUTH_HOST__', 8081, 0, 0),
    ('token_logout', '清除浏览器刷新凭证', 'http', 'POST', '/token/logout', '__AUTH_HOST__', 8081, 0, 0),
    ('whitelist:blog:login', '登录', 'http', 'POST', '/login', '__AUTH_HOST__', 8081, 0, 0),
    ('code_email', '发送邮箱验证码', 'http', 'POST', '/code/email', '__AUTH_HOST__', 8081, 0, 0),
    ('code_sms', '发送短信验证码', 'http', 'POST', '/code/sms', '__AUTH_HOST__', 8081, 0, 0),
    ('sys:user:register:page', '生成注册页链接', 'http', 'GET', '/sys/user/auth/register/page', '__USER_HOST__', 8086, 1, 0),
    ('whitelist:user:register:check', '用户注册密钥验证', 'http', 'GET', '/sys/user/register/check', '__USER_HOST__', 8086, 0, 0),
    ('whitelist:user:register:save', '保存用户注册信息', 'http', 'POST', '/sys/user/register/save', '__USER_HOST__', 8086, 0, 0),
    ('whitelist:user:upload', '用户头像上传', 'http', 'POST', '/sys/user/register/image/upload', '__USER_HOST__', 8086, 0, 0),
    ('user_register_image_delete', '删除注册图片', 'http', 'DELETE', '/sys/user/register/image/delete', '__USER_HOST__', 8086, 0, 0),
    ('sys:user:save', '保存或修改账户', 'http', 'POST', '/sys/user/save', '__USER_HOST__', 8086, 1, 0),
    ('sys:user:page', '获取账户列表', 'http', 'GET', '/sys/user/page/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:user:delete', '删除账户', 'http', 'POST', '/sys/user/delete', '__USER_HOST__', 8086, 1, 0),
    ('sys:user:info', '获取账户实体', 'http', 'GET', '/sys/user/info/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:user:download', '账号内容导出', 'http', 'GET', '/sys/user/download', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:info', '获取角色实体', 'http', 'GET', '/sys/role/info/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:roles', '获取角色列表', 'http', 'GET', '/sys/role/roles', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:save', '保存或修改角色', 'http', 'POST', '/sys/role/save', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:delete', '删除角色', 'http', 'POST', '/sys/role/delete', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:menu:save', '保存角色下的菜单', 'http', 'POST', '/sys/role/menu/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:menu:get', '获取角色下的菜单', 'http', 'GET', '/sys/role/menu/*', '__USER_HOST__', 8086, 1, 0),
    ('system-roles-data-permission-save', '保存角色数据权限', 'http', 'POST', '/sys/role/data-permission/*', '__USER_HOST__', 8086, 1, 0),
    ('system-roles-data-permission-get', '获取角色数据权限', 'http', 'GET', '/sys/role/data-permission/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:download', '角色内容导出', 'http', 'GET', '/sys/role/download', '__USER_HOST__', 8086, 1, 0),
    ('sys:role:valid:all', '获取全部角色', 'http', 'GET', '/sys/role/valid/all', '__USER_HOST__', 8086, 1, 0),
    ('sys:menu:info', '获取菜单实体', 'http', 'GET', '/sys/menu/info/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:menu:list', '获取菜单列表', 'http', 'GET', '/sys/menu/list', '__USER_HOST__', 8086, 1, 0),
    ('sys:menu:save', '保存或修改菜单', 'http', 'POST', '/sys/menu/save', '__USER_HOST__', 8086, 1, 0),
    ('sys:menu:delete', '删除菜单', 'http', 'POST', '/sys/menu/delete/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:menu:download', '菜单内容导出', 'http', 'GET', '/sys/menu/download', '__USER_HOST__', 8086, 1, 0),
    ('sys:menu:authority', '获取菜单下的权限', 'http', 'GET', '/sys/menu/authority/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:menu:authority:save', '保存菜单下的权限', 'http', 'POST', '/sys/menu/authority/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:authority:list', '获取权限列表', 'http', 'GET', '/sys/authority/list', '__USER_HOST__', 8086, 1, 0),
    ('sys:authority:info', '获取权限实体', 'http', 'GET', '/sys/authority/info/*', '__USER_HOST__', 8086, 1, 0),
    ('sys:authority:save', '保存或修改权限', 'http', 'POST', '/sys/authority/save', '__USER_HOST__', 8086, 1, 0),
    ('sys:authority:delete', '删除权限', 'http', 'POST', '/sys/authority/delete', '__USER_HOST__', 8086, 1, 0),
    ('sys:authority:download', '权限内容导出', 'http', 'GET', '/sys/authority/download', '__USER_HOST__', 8086, 1, 0),
    ('sys:blog:echo', '编辑回显内容', 'http', 'GET', '/sys/blog/edit/pull/echo', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:draft:create', '新建文章草稿', 'http', 'POST', '/sys/blog/draft/new', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:draft:save', '保存文章草稿', 'http', 'POST', '/sys/blog/*/draft', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:draft:delete', '删除文章草稿', 'http', 'DELETE', '/sys/blog/*/draft', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:version:save', '保存文章版本', 'http', 'POST', '/sys/blog/*/versions', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:version:list', '查询文章版本', 'http', 'GET', '/sys/blog/*/versions', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:version:view', '查看文章版本', 'http', 'GET', '/sys/blog/*/versions/*', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:version:compare', '对比文章版本', 'http', 'GET', '/sys/blog/*/versions/*/compare', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:publish', '发布文章版本', 'http', 'POST', '/sys/blog/*/publish', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:offline', '下线文章', 'http', 'POST', '/sys/blog/*/offline', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:online', '上线文章', 'http', 'POST', '/sys/blog/*/online', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:delete', '删除文章', 'http', 'POST', '/sys/blog/delete', '__BLOG_HOST__', 8082, 1, 0),
    ('blog_lock', '签发一次性博客阅读凭证', 'http', 'POST', '/sys/blog/lock/*', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:blogs', '查询文章', 'http', 'GET', '/sys/blog/blogs', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:deleted', '查询已删除文章', 'http', 'GET', '/sys/blog/deleted', '__BLOG_HOST__', 8082, 1, 0),
    ('blog_recover', '恢复已删除文章', 'http', 'POST', '/sys/blog/recover/*', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:oss:upload', '上传文章封面', 'http', 'POST', '/sys/blog/oss/upload', '__BLOG_HOST__', 8082, 1, 0),
    ('blog_oss_delete', '删除文章封面', 'http', 'DELETE', '/sys/blog/oss/delete', '__BLOG_HOST__', 8082, 1, 0),
    ('sys:blog:download', '文章内容导出', 'http', 'GET', '/sys/blog/download', '__BLOG_HOST__', 8082, 1, 0),
    ('sys_blog_collaboration_ticket', '签发协作房间短期凭证', 'http', 'POST', '/sys/blog/edit/ticket', '__BLOG_HOST__', 8082, 1, 0),
    ('blog:ws:edit', 'WebSocket协作端点', 'ws', 'GET', '/rooms/*', '__SYNC_HOST__', 8089, 1, 0),
    ('whitelist:blog:detail', '获取博客内容详情', 'http', 'GET', '/public/blog/info/*', '__EXHIBIT_HOST__', 8083, 0, 0),
    ('whitelist:blog:page', '获取博客内容页面', 'http', 'GET', '/public/blog/page/*', '__EXHIBIT_HOST__', 8083, 0, 0),
    ('exhibit_blog_secret', '消费阅读凭证并返回受保护博客', 'http', 'POST', '/public/blog/secret/*', '__EXHIBIT_HOST__', 8083, 0, 0),
    ('whitelist:visit:stat', '获取访问数据', 'http', 'GET', '/public/blog/stat', '__EXHIBIT_HOST__', 8083, 0, 0),
    ('whitelist:blog:score', '获取本周最多阅读', 'http', 'GET', '/public/blog/scores', '__EXHIBIT_HOST__', 8083, 0, 0),
    ('whitelist:blog:recommendations', '获取相关与热门推荐', 'http', 'GET', '/public/blog/recommendations', '__EXHIBIT_HOST__', 8083, 0, 0),
    ('whitelist:pattern:search', '白名单公共搜索', 'http', 'GET', '/search/public/blog', '__SEARCH_HOST__', 8085, 0, 0);

INSERT INTO m_authority
    (code, remark, prototype, method_type, route_pattern, service_host, service_port, type, status)
SELECT
    seed.code, seed.remark, seed.prototype, seed.method_type, seed.route_pattern,
    seed.service_host, seed.service_port, seed.type, seed.status
FROM seed_admin_authority seed
WHERE NOT EXISTS (SELECT 1 FROM m_authority target WHERE target.code = seed.code);

UPDATE m_authority target
JOIN seed_admin_authority seed ON seed.code = target.code
SET target.remark = seed.remark,
    target.prototype = seed.prototype,
    target.method_type = seed.method_type,
    target.route_pattern = seed.route_pattern,
    target.service_host = seed.service_host,
    target.service_port = seed.service_port,
    target.type = seed.type,
    target.status = seed.status;

DELETE menu_authority
FROM m_menu_authority menu_authority
JOIN m_authority authority ON authority.id = menu_authority.authority_id
WHERE authority.code IN ('sys:blog:save', 'sys:blog:push:all');

DELETE FROM m_authority WHERE code IN ('sys:blog:save', 'sys:blog:push:all');

CREATE TEMPORARY TABLE seed_admin_menu_authority (
    menu_name VARCHAR(100) NOT NULL,
    authority_code VARCHAR(100) NOT NULL
);

INSERT INTO seed_admin_menu_authority (menu_name, authority_code)
VALUES
    ('system-root', 'auth:menu:nav'),
    ('system-root', 'token:userinfo'),
    ('system-root', 'whitelist:token:refresh'),
    ('system-root', 'token_logout'),
    ('system-root', 'whitelist:blog:login'),
    ('system-root', 'code_email'),
    ('system-root', 'code_sms'),
    ('system-users', 'sys:user:page'),
    ('system-users-create', 'sys:user:register:page'),
    ('system-users-create', 'whitelist:user:register:check'),
    ('system-users-create', 'whitelist:user:register:save'),
    ('system-users-create', 'whitelist:user:upload'),
    ('system-users-create', 'user_register_image_delete'),
    ('system-users-save', 'sys:user:save'),
    ('system-users-batch-del', 'sys:user:delete'),
    ('system-users-download', 'sys:user:download'),
    ('system-users-register', 'sys:user:register:page'),
    ('system-users-register', 'whitelist:user:register:check'),
    ('system-users-delete', 'sys:user:delete'),
    ('system-users-edit', 'sys:user:info'),
    ('system-users-modify-register', 'sys:user:register:page'),
    ('system-roles', 'sys:role:roles'),
    ('system-roles', 'sys:role:valid:all'),
    ('system-roles-create', 'sys:role:save'),
    ('system-roles-save', 'sys:role:save'),
    ('system-roles-menus-save', 'sys:role:menu:save'),
    ('system-roles-data-save', 'system-roles-data-permission-save'),
    ('system-roles-batch-del', 'sys:role:delete'),
    ('system-roles-download', 'sys:role:download'),
    ('system-roles-delete', 'sys:role:delete'),
    ('system-roles-menu-perm', 'sys:role:menu:get'),
    ('system-roles-data-perm', 'system-roles-data-permission-get'),
    ('system-roles-edit', 'sys:role:info'),
    ('system-menus', 'sys:menu:list'),
    ('system-menus-create', 'sys:menu:save'),
    ('system-menus-authorities-save', 'sys:menu:authority:save'),
    ('system-menus-save', 'sys:menu:save'),
    ('system-menus-delete', 'sys:menu:delete'),
    ('system-menus-edit', 'sys:menu:info'),
    ('system-menus-download', 'sys:menu:download'),
    ('system-menus-authority-perm', 'sys:menu:authority'),
    ('system-authorities', 'sys:authority:list'),
    ('system-authorities-create', 'sys:authority:save'),
    ('system-authorities-save', 'sys:authority:save'),
    ('system-authorities-batch-del', 'sys:authority:delete'),
    ('system-authorities-download', 'sys:authority:download'),
    ('system-authorities-edit', 'sys:authority:info'),
    ('system-authorities-delete', 'sys:authority:delete'),
    ('system-blogs', 'sys:blog:blogs'),
    ('system-blogs-download', 'sys:blog:download'),
    ('system-blogs-search', 'sys:blog:blogs'),
    ('system-blogs-batch-del', 'sys:blog:delete'),
    ('system-blogs-delete', 'sys:blog:delete'),
    ('system-blogs-check', 'blog_lock'),
    ('system-blogs-edit', 'sys:blog:echo'),
    ('system-blogs-password', 'blog_lock'),
    ('system-edit', 'sys:blog:echo'),
    ('system-edit', 'sys_blog_collaboration_ticket'),
    ('system-edit', 'blog:ws:edit'),
    ('system-edit-submit', 'sys:blog:version:save'),
    ('system-blogs-upload', 'sys:blog:oss:upload'),
    ('system-blogs-upload', 'blog_oss_delete'),
    ('system-edit-save-draft', 'sys:blog:draft:create'),
    ('system-edit-save-draft', 'sys:blog:draft:save'),
    ('system-edit-save-draft', 'sys:blog:draft:delete'),
    ('system-edit-versions', 'sys:blog:version:list'),
    ('system-edit-version-view', 'sys:blog:version:view'),
    ('system-edit-version-compare', 'sys:blog:version:compare'),
    ('system-edit-publish', 'sys:blog:publish'),
    ('system-edit-status', 'sys:blog:offline'),
    ('system-edit-status', 'sys:blog:online'),
    ('system-edit-restore-version', 'sys:blog:publish'),
    ('system-del', 'sys:blog:deleted'),
    ('system-delete-resume', 'blog_recover'),
    ('system-root', 'whitelist:blog:detail'),
    ('system-root', 'whitelist:blog:page'),
    ('system-root', 'exhibit_blog_secret'),
    ('system-root', 'whitelist:visit:stat'),
    ('system-root', 'whitelist:blog:score'),
    ('system-root', 'whitelist:blog:recommendations'),
    ('system-root', 'whitelist:pattern:search');

INSERT INTO m_menu_authority (menu_id, authority_id)
SELECT DISTINCT menu.id, authority.id
FROM seed_admin_menu_authority seed
JOIN m_menu menu ON menu.name = seed.menu_name
JOIN m_authority authority ON authority.code = seed.authority_code
WHERE NOT EXISTS (
    SELECT 1
    FROM m_menu_authority existing
    WHERE existing.menu_id = menu.id
      AND existing.authority_id = authority.id
);

INSERT INTO m_role (name, code, remark, status)
SELECT '超级管理员', 'admin', '内置管理员角色', 0
WHERE NOT EXISTS (SELECT 1 FROM m_role WHERE code = 'admin');

UPDATE m_role
SET name = '超级管理员',
    status = 0
WHERE code = 'admin';

INSERT INTO m_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM m_role role
JOIN seed_admin_menu seed ON seed.name IS NOT NULL
JOIN m_menu menu ON menu.name = seed.name
WHERE role.code = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM m_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
  );
