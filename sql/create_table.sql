-- ----------------------------
-- 1、部门表 【原生保留，纺织企业直接复用】
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
                          dept_id           bigint(20)      not null auto_increment    comment '部门id',
                          parent_id         bigint(20)      default 0                  comment '父部门id',
                          ancestors         varchar(50)     default ''                 comment '祖级列表',
                          dept_name         varchar(30)     default ''                 comment '部门名称',
                          order_num         int(4)          default 0                  comment '显示顺序',
                          leader            varchar(20)     default null               comment '负责人',
                          phone             varchar(11)     default null               comment '联系电话',
                          email             varchar(50)     default null               comment '邮箱',
                          status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
                          del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
                          create_by         varchar(64)     default ''                 comment '创建者',
                          create_time 	    datetime                                   comment '创建时间',
                          update_by         varchar(64)     default ''                 comment '更新者',
                          update_time       datetime                                   comment '更新时间',
                          primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 2、用户信息表：扩展 agent_type 区分普通用户/体验访客，复用若依全部逻辑
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
                          user_id           bigint(20)      not null auto_increment    comment '用户ID',
                          dept_id           bigint(20)      default null               comment '部门ID',
                          user_name         varchar(30)     not null                   comment '用户账号',
                          nick_name         varchar(30)     not null                   comment '用户昵称',
                          user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
                          agent_type        char(1)         default '1'                comment 'AI用户类型：1正式用户 2体验访客guest',
                          email             varchar(50)     default ''                 comment '用户邮箱',
                          phonenumber       varchar(11)     default ''                 comment '手机号码',
                          sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
                          avatar            varchar(100)    default ''                 comment '头像地址',
                          password          varchar(100)    default ''                 comment '密码',
                          status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
                          del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
                          login_ip          varchar(128)    default ''                 comment '最后登录IP',
                          login_date        datetime                                   comment '最后登录时间',
                          pwd_update_date   datetime                                   comment '密码最后更新时间',
                          create_by         varchar(64)     default ''                 comment '创建者',
                          create_time       datetime                                   comment '创建时间',
                          update_by         varchar(64)     default ''                 comment '更新者',
                          update_time       datetime                                   comment '更新时间',
                          remark            varchar(500)    default null               comment '备注',
                          primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
drop table if exists sys_post;
create table sys_post (
                          post_id       bigint(20)      not null auto_increment    comment '岗位ID',
                          post_code     varchar(64)     not null                   comment '岗位编码',
                          post_name     varchar(50)     not null                   comment '岗位名称',
                          post_sort     int(4)          not null                   comment '显示顺序',
                          status        char(1)         not null                   comment '状态（0正常 1停用）',
                          create_by     varchar(64)     default ''                 comment '创建者',
                          create_time   datetime                                   comment '创建时间',
                          update_by     varchar(64)     default ''			       comment '更新者',
                          update_time   datetime                                   comment '更新时间',
                          remark        varchar(500)    default null               comment '备注',
                          primary key (post_id)
) engine=innodb comment = '岗位信息表';

-- ----------------------------
-- 4、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
                          role_id              bigint(20)      not null auto_increment    comment '角色ID',
                          role_name            varchar(30)     not null                   comment '角色名称',
                          role_key             varchar(100)    not null                   comment '角色权限字符串',
                          role_sort            int(4)          not null                   comment '显示顺序',
                          data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
                          menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
                          dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
                          status               char(1)         not null                   comment '角色状态（0正常 1停用）',
                          del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
                          create_by            varchar(64)     default ''                 comment '创建者',
                          create_time          datetime                                   comment '创建时间',
                          update_by            varchar(64)     default ''                 comment '更新者',
                          update_time          datetime                                   comment '更新时间',
                          remark               varchar(500)    default null               comment '备注',
                          primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 5、菜单权限表：保留原有，后续插入新增AI智能体菜单
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
                          menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
                          menu_name         varchar(50)     not null                   comment '菜单名称',
                          parent_id         bigint(20)      default 0                  comment '父菜单ID',
                          order_num         int(4)          default 0                  comment '显示顺序',
                          path              varchar(200)    default ''                 comment '路由地址',
                          component         varchar(255)    default null               comment '组件路径',
                          query             varchar(255)    default null               comment '路由参数',
                          route_name        varchar(50)     default ''                 comment '路由名称',
                          is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
                          is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
                          menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
                          visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
                          status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
                          perms             varchar(100)    default null               comment '权限标识',
                          icon              varchar(100)    default '#'                comment '菜单图标',
                          create_by         varchar(64)     default ''                 comment '创建者',
                          create_time       datetime                                   comment '创建时间',
                          update_by         varchar(64)     default ''                 comment '更新者',
                          update_time       datetime                                   comment '更新时间',
                          remark            varchar(500)    default ''                 comment '备注',
                          primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 6、用户和角色关联表
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
                               user_id   bigint(20) not null comment '用户ID',
                               role_id   bigint(20) not null comment '角色ID',
                               primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 7、角色和菜单关联表
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
                               role_id   bigint(20) not null comment '角色ID',
                               menu_id   bigint(20) not null comment '菜单ID',
                               primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 8、角色和部门关联表
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
                               role_id   bigint(20) not null comment '角色ID',
                               dept_id   bigint(20) not null comment '部门ID',
                               primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 9、用户与岗位关联表
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post (
                               user_id   bigint(20) not null comment '用户ID',
                               post_id   bigint(20) not null comment '岗位ID',
                               primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
                              oper_id           bigint(20)      not null auto_increment    comment '日志主键',
                              title             varchar(50)     default ''                 comment '模块标题',
                              business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
                              method            varchar(200)    default ''                 comment '方法名称',
                              request_method    varchar(10)     default ''                 comment '请求方式',
                              operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
                              oper_name         varchar(50)     default ''                 comment '操作人员',
                              dept_name         varchar(50)     default ''                 comment '部门名称',
                              oper_url          varchar(255)    default ''                 comment '请求URL',
                              oper_ip           varchar(128)    default ''                 comment '主机地址',
                              oper_location     varchar(255)    default ''                 comment '操作地点',
                              oper_param        varchar(2000)   default ''                 comment '请求参数',
                              json_result       varchar(2000)   default ''                 comment '返回参数',
                              status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
                              error_msg         varchar(2000)   default ''                 comment '错误消息',
                              oper_time         datetime                                   comment '操作时间',
                              cost_time         bigint(20)      default 0                  comment '消耗时间',
                              primary key (oper_id),
                              key idx_sys_oper_log_bt (business_type),
                              key idx_sys_oper_log_s  (status),
                              key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';

-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type (
                               dict_id          bigint(20)      not null auto_increment    comment '字典主键',
                               dict_name        varchar(100)    default ''                 comment '字典名称',
                               dict_type        varchar(100)    default ''                 comment '字典类型',
                               status           char(1)         default '0'                comment '状态（0正常 1停用）',
                               create_by        varchar(64)     default ''                 comment '创建者',
                               create_time      datetime                                   comment '创建时间',
                               update_by        varchar(64)     default ''                 comment '更新者',
                               update_time      datetime                                   comment '更新时间',
                               remark           varchar(500)    default null               comment '备注',
                               primary key (dict_id),
                               unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data (
                               dict_code        bigint(20)      not null auto_increment    comment '字典编码',
                               dict_sort        int(4)          default 0                  comment '字典排序',
                               dict_label       varchar(100)    default ''                 comment '字典标签',
                               dict_value       varchar(100)    default ''                 comment '字典键值',
                               dict_type        varchar(100)    default ''                 comment '字典类型',
                               css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
                               list_class       varchar(100)    default null               comment '表格回显样式',
                               is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
                               status           char(1)         default '0'                comment '状态（0正常 1停用）',
                               create_by        varchar(64)     default ''                 comment '创建者',
                               create_time      datetime                                   comment '创建时间',
                               update_by        varchar(64)     default ''                 comment '更新者',
                               update_time      datetime                                   comment '更新时间',
                               remark           varchar(500)    default null               comment '备注',
                               primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典数据表';

-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
                            config_id         int(5)          not null auto_increment    comment '参数主键',
                            config_name       varchar(100)    default ''                 comment '参数名称',
                            config_key        varchar(100)    default ''                 comment '参数键名',
                            config_value      varchar(500)    default ''                 comment '参数键值',
                            config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
                            create_by         varchar(64)     default ''                 comment '创建者',
                            create_time       datetime                                   comment '创建时间',
                            update_by         varchar(64)     default ''                 comment '更新者',
                            update_time       datetime                                   comment '更新时间',
                            remark            varchar(500)    default null               comment '备注',
                            primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
                                info_id        bigint(20)     not null auto_increment   comment '访问ID',
                                user_name      varchar(50)    default ''                comment '用户账号',
                                ipaddr         varchar(128)   default ''                comment '登录IP地址',
                                login_location varchar(255)   default ''                comment '登录地点',
                                browser        varchar(50)    default ''                comment '浏览器类型',
                                os             varchar(50)    default ''                comment '操作系统',
                                status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
                                msg            varchar(255)   default ''                comment '提示消息',
                                login_time     datetime                                 comment '访问时间',
                                primary key (info_id),
                                key idx_sys_logininfor_s  (status),
                                key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系统访问记录';

-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
                         job_id              bigint(20)    not null auto_increment    comment '任务ID',
                         job_name            varchar(64)   default ''                 comment '任务名称',
                         job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
                         invoke_target       varchar(500)  not null                   comment '调用目标字符串',
                         cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
                         misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
                         concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
                         status              char(1)       default '0'                comment '状态（0正常 1暂停）',
                         create_by           varchar(64)   default ''                 comment '创建者',
                         create_time         datetime                                 comment '创建时间',
                         update_by           varchar(64)   default ''                 comment '更新者',
                         update_time         datetime                                 comment '更新时间',
                         remark              varchar(500)  default ''                 comment '备注信息',
                         primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
                             job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
                             job_name            varchar(64)    not null                   comment '任务名称',
                             job_group           varchar(64)    not null                   comment '任务组名',
                             invoke_target       varchar(500)   not null                   comment '调用目标字符串',
                             job_message         varchar(500)                              comment '日志信息',
                             status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
                             exception_info      varchar(2000)  default ''                 comment '异常信息',
                             start_time          datetime                                  comment '执行开始时间',
                             end_time            datetime                                  comment '执行结束时间',
                             create_time         datetime                                  comment '创建时间',
                             primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';

-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
                            notice_id         int(4)          not null auto_increment    comment '公告ID',
                            notice_title      varchar(50)     not null                   comment '公告标题',
                            notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',
                            notice_content    longblob        default null               comment '公告内容',
                            status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',
                            create_by         varchar(64)     default ''                 comment '创建者',
                            create_time       datetime                                   comment '创建时间',
                            update_by         varchar(64)     default ''                 comment '更新者',
                            update_time       datetime                                   comment '更新时间',
                            remark            varchar(255)    default null               comment '备注',
                            primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 18、公告已读记录表
-- ----------------------------
drop table if exists sys_notice_read;
create table sys_notice_read (
                                 read_id          bigint(20)       not null auto_increment    comment '已读主键',
                                 notice_id        int(4)           not null                   comment '公告id',
                                 user_id          bigint(20)       not null                   comment '用户id',
                                 read_time        datetime         not null                   comment '阅读时间',
                                 primary key (read_id),
                                 unique key uk_user_notice (user_id, notice_id)   comment '同一用户同一公告只记录一次'
) engine=innodb auto_increment=1 comment='公告已读记录表';

-- ----------------------------
-- 19、代码生成业务表
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
                           table_id          bigint(20)      not null auto_increment    comment '编号',
                           table_name        varchar(200)    default ''                 comment '表名称',
                           table_comment     varchar(500)    default ''                 comment '表描述',
                           sub_table_name    varchar(64)     default null               comment '关联子表的表名',
                           sub_table_fk_name varchar(64)     default null               comment '子表关联的外键名',
                           class_name        varchar(100)    default ''                 comment '实体类名称',
                           tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',
                           tpl_web_type      varchar(30)     default ''                 comment '前端模板类型（element-ui模版 element-plus模版）',
                           package_name      varchar(100)                               comment '生成包路径',
                           module_name       varchar(30)                                comment '生成模块名',
                           business_name     varchar(30)                                comment '生成业务名',
                           function_name     varchar(50)                                comment '生成功能名',
                           function_author   varchar(50)                                comment '生成功能作者',
                           form_col_num      int(1)          default 1                  comment '表单布局（单列 双列 三列）',
                           gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',
                           gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',
                           options           varchar(1000)                              comment '其它生成选项',
                           create_by         varchar(64)     default ''                 comment '创建者',
                           create_time 	    datetime                                   comment '创建时间',
                           update_by         varchar(64)     default ''                 comment '更新者',
                           update_time       datetime                                   comment '更新时间',
                           remark            varchar(500)    default null               comment '备注',
                           primary key (table_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表';

-- ----------------------------
-- 20、代码生成业务表字段
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
                                  column_id         bigint(20)      not null auto_increment    comment '编号',
                                  table_id          bigint(20)                                 comment '归属表编号',
                                  column_name       varchar(200)                               comment '列名称',
                                  column_comment    varchar(500)                               comment '列描述',
                                  column_type       varchar(100)                               comment '列类型',
                                  java_type         varchar(500)                               comment 'JAVA类型',
                                  java_field        varchar(200)                               comment 'JAVA字段名',
                                  is_pk             char(1)                                    comment '是否主键（1是）',
                                  is_increment      char(1)                                    comment '是否自增（1是）',
                                  is_required       char(1)                                    comment '是否必填（1是）',
                                  is_insert         char(1)                                    comment '是否为插入字段（1是）',
                                  is_edit           char(1)                                    comment '是否编辑字段（1是）',
                                  is_list           char(1)                                    comment '是否列表字段（1是）',
                                  is_query          char(1)                                    comment '是否查询字段（1是）',
                                  query_type        varchar(200)    default 'EQ'               comment '查询方式（等于、不等于、大于、小于、范围）',
                                  html_type         varchar(200)                               comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
                                  dict_type         varchar(200)    default ''                 comment '字典类型',
                                  sort              int                                        comment '排序',
                                  create_by         varchar(64)     default ''                 comment '创建者',
                                  create_time 	    datetime                                   comment '创建时间',
                                  update_by         varchar(64)     default ''                 comment '更新者',
                                  update_time       datetime                                   comment '更新时间',
                                  primary key (column_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';

-- ===================== 纺织AI智能体【新增业务表】=====================
-- AI大模型配置表：管理DeepSeek、Claude等模型参数、api‑key
drop table if exists sys_agent_model;
create table sys_agent_model(
                                model_id bigint auto_increment primary key comment '模型主键',
                                model_name varchar(100) not null comment '模型展示名称',
                                model_code varchar(100) not null comment '模型编码 deepseek-r1/claude‑3‑5',
                                base_url varchar(255) comment '接口地址',
                                api_key varchar(500) comment '密钥',
                                temperature decimal(3,2) default 0.7 comment '温度',
                                max_tokens int default 4096 comment '最大输出token',
                                status char(1) default '0' comment '0启用 1停用',
                                create_by varchar(64) default '',
                                create_time datetime,
                                update_by varchar(64) default '',
                                update_time datetime,
                                remark varchar(500)
)engine=innodb comment='AI智能体-大模型配置';

-- 用户AI调用额度管控表：区分正式用户、guest访客限额
drop table if exists sys_user_agent_quota;
create table sys_user_agent_quota(
                                     quota_id bigint auto_increment primary key,
                                     user_id bigint not null comment '关联sys_user.user_id',
                                     day_quota int default 50 comment '每日最大调用次数',
                                     day_used int default 0 comment '今日已调用次数',
                                     total_quota bigint default 10000 comment '总额度',
                                     total_used bigint default 0 comment '已消耗总次数',
                                     reset_time datetime comment '额度重置时间',
                                     create_time datetime,
                                     update_time datetime,
                                     unique key uk_uid(user_id)
)engine=innodb comment='AI智能体-用户调用额度';


-- 存储所有业务知识库文档元数据（本地文档/数据库文档），支撑文档管理、状态控制、多租户隔离
CREATE TABLE IF NOT EXISTS ai_knowledge_document (
                                                     id BIGSERIAL PRIMARY KEY COMMENT '主键ID',
                                                     doc_id VARCHAR(128) NOT NULL DEFAULT '' COMMENT '文档唯一标识',
    title VARCHAR(256) NOT NULL DEFAULT '' COMMENT '文档标题',
    content TEXT COMMENT '文档原始完整内容',
    file_url VARCHAR(512) DEFAULT '' COMMENT '文档存储地址',
    file_type VARCHAR(32) DEFAULT '' COMMENT '文件类型：md/txt/pdf',
    scope_type VARCHAR(64) DEFAULT '' COMMENT '知识领域分类编码',
    business_type VARCHAR(64) DEFAULT '' COMMENT '业务领域分类编码',
    group_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '租户ID，多租户隔离',
    status TINYINT NOT NULL DEFAULT 2 COMMENT '文档状态：0-下架 1-上架 2-待向量化 3-向量化完成',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user BIGINT DEFAULT 0 COMMENT '创建人ID',
    update_user BIGINT DEFAULT 0 COMMENT '更新人ID',
    description VARCHAR(512) DEFAULT '' COMMENT '文档描述备注'
    );

-- 索引
CREATE INDEX idx_knowledge_doc_group ON ai_knowledge_document(group_id);
CREATE INDEX idx_knowledge_doc_status ON ai_knowledge_document(status);
CREATE INDEX idx_knowledge_doc_scope ON ai_knowledge_document(scope_type, business_type);
CREATE UNIQUE INDEX idx_knowledge_doc_id ON ai_knowledge_document(doc_id);


-- 持久化用户所有对话记录、多模态附件信息、会话上下文，支撑历史对话查询、多轮对话、Token统计
CREATE TABLE IF NOT EXISTS ai_context_user_record (
                                                      id BIGSERIAL PRIMARY KEY COMMENT '主键ID',
                                                      chat_id VARCHAR(128) NOT NULL COMMENT '会话唯一ID',
    user_id BIGINT NOT NULL DEFAULT 0 COMMENT '用户ID',
    group_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '租户ID，多租户隔离',
    role VARCHAR(32) NOT NULL COMMENT '消息角色：user-用户 assistant-助手 tool-工具',
    content TEXT COMMENT '对话文本内容',
    media_info JSON COMMENT '多模态附件信息，存储图片/音频/视频/PDF等附件数据',
    prompt_tokens INT DEFAULT 0 COMMENT '本次请求消耗Prompt Token',
    completion_tokens INT DEFAULT 0 COMMENT '本次回复消耗Completion Token',
    total_tokens INT DEFAULT 0 COMMENT '本次总消耗Token',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    );

-- 索引
CREATE INDEX idx_chat_record_chatid ON ai_context_user_record(chat_id);
CREATE INDEX idx_chat_record_user ON ai_context_user_record(user_id);
CREATE INDEX idx_chat_record_group ON ai_context_user_record(group_id);
CREATE INDEX idx_chat_record_time ON ai_context_user_record(create_time);


-- 存储文档切片内容+向量数据，支撑混合RAG检索、父子分块检索、相似度匹配，核心检索表
CREATE TABLE IF NOT EXISTS ai_knowledge_vector_chunk (
                                                         id BIGSERIAL PRIMARY KEY COMMENT '主键ID',
                                                         doc_id VARCHAR(128) NOT NULL COMMENT '关联文档唯一标识，关联ai_knowledge_document',
    parent_id BIGINT DEFAULT 0 COMMENT '父级分片ID，用于父子分块检索',
    chunk_level VARCHAR(16) DEFAULT 'CHILD' COMMENT '分片层级：PARENT-父分片 CHILD-子分片',
    chunk_content TEXT NOT NULL COMMENT '文档切片文本内容',
    embedding vector(1024) NOT NULL COMMENT '文本向量，维度1024，适配qwen系列模型',
    scope_type VARCHAR(64) DEFAULT '' COMMENT '知识领域分类编码',
    business_type VARCHAR(64) DEFAULT '' COMMENT '业务领域分类编码',
    group_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '租户ID，多租户隔离',
    similarity_threshold DECIMAL(5,4) DEFAULT 0.6000 COMMENT '分片相似度阈值',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    );

-- 普通索引
CREATE INDEX idx_vector_chunk_docid ON ai_knowledge_vector_chunk(doc_id);
CREATE INDEX idx_vector_chunk_parent ON ai_knowledge_vector_chunk(parent_id);
CREATE INDEX idx_vector_chunk_group ON ai_knowledge_vector_chunk(group_id);
-- 向量HNSW索引（适配百万级数据，项目默认索引类型）
CREATE INDEX idx_vector_embedding_hnsw ON ai_knowledge_vector_chunk USING hnsw (embedding vector_cosine_ops);