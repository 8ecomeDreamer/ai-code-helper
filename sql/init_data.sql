-- ----------------------------
-- 初始化-部门表数据 纺织公司组织
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          '纺织科技',   0, '管理员', '15888888888', 'admin@textile.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '技术研发部', 1, '梁俊杰', '15888888888', 'dev@textile.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '业务生产部', 2, '生产主管', '15888888888', 'biz@textile.com', '0', '0', 'admin', sysdate(), '', null);

-- ----------------------------
-- 初始化-用户信息表数据 【对应登录页3个账号】
-- ----------------------------
insert into sys_user values(1,  101, 'admin', '系统管理员', '00','1', 'admin@textile.com', '15888888888', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '纺织智能体超级管理员');
insert into sys_user values(2,  102, 'user',    '正式业务用户', '00','1', 'user@textile.com',  '15666666666', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '纺织业务正式账号');
insert into sys_user values(3,  null, 'guest', '体验访客', '00','2', 'guest@textile.com', '', '2', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '访客体验账号，有调用次数限制');

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------
insert into sys_post values(1, 'admin',  '系统管理员',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'biz_user',   '业务操作员',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'guest', '访客用户',  3, '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 初始化-角色信息表数据 新增【纺织AI管理员、正式业务角色、访客角色】
-- ----------------------------
insert into sys_role values('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '拥有全部权限');
insert into sys_role values('2', '纺织AI业务用户',    'textile_biz', 2, 4, 1, 1, '0', '0', 'admin', sysdate(), '', null, '正式业务，可使用全部AI能力，无菜单管理权限');
insert into sys_role values('3', '体验访客角色', 'textile_guest',3,0,1,1,'0','0','admin',sysdate(),'',null,'访客：仅允许对话，不可配置模型、知识库');

-- ----------------------------
-- 初始化菜单：原有系统菜单 +【纺织智能体AI模块完整菜单、按钮权限】
-- ----------------------------
-- 一级菜单：纺织智能体 M目录
insert into sys_menu values('2000', '纺织智能体', '0', '5', 'agent',           null, '', '', 1, 0, 'M', '0', '0', '', 'ai',   'admin', sysdate(), '', null, '纺织AI智能体业务模块');
-- 二级菜单
insert into sys_menu values('2001', 'Agent对话工作台','2000','1','chat','agent/chat/index','','','1',0,'C','0','0','agent:chat:list','message','admin',sysdate(),'',null,'AI对话工作台');
insert into sys_menu values('2002', '知识库管理','2000','2','knowledge','agent/knowledge/index','','','1',0,'C','0','0','agent:knowledge:list','book','admin',sysdate(),'',null,'RAG知识库管理');
insert into sys_menu values('2003', '提示词模板','2000','3','prompt','agent/prompt/index','','','1',0,'C','0','0','agent:prompt:list','edit','admin',sysdate(),'',null,'系统提示词模板');
insert into sys_menu values('2004', '向量库管理','2000','4','vector','agent/vector/index','','','1',0,'C','0','0','agent:vector:list','database','admin',sysdate(),'',null,'向量库运维');
insert into sys_menu values('2005', '模型配置','2000','5','model','agent/model/index','','','1',0,'C','0','0','agent:model:list','cpu','admin',sysdate(),'',null,'大模型参数配置');
insert into sys_menu values('2006', 'AI调用日志','2000','6','agentLog','agent/log/index','','','1',0,'C','0','0','agent:log:list','log','admin',sysdate(),'',null,'AI调用审计日志');

-- 按钮权限-F
-- 对话工作台按钮
insert into sys_menu values('2100','对话查询','2001','1','','','','',1,0,'F','0','0','agent:chat:query','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2101','对话新增','2001','2','','','','',1,0,'F','0','0','agent:chat:add','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2102','对话删除','2001','3','','','','',1,0,'F','0','0','agent:chat:remove','#','admin',sysdate(),'',null,'');
-- 知识库
insert into sys_menu values('2110','知识库查询','2002','1','','','','',1,0,'F','0','0','agent:knowledge:query','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2111','知识库新增','2002','2','','','','',1,0,'F','0','0','agent:knowledge:add','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2112','知识库编辑','2002','3','','','','',1,0,'F','0','0','agent:knowledge:edit','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2113','知识库删除','2002','4','','','','',1,0,'F','0','0','agent:knowledge:remove','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2114','文档上传解析','2002','5','','','','',1,0,'F','0','0','agent:knowledge:upload','#','admin',sysdate(),'',null,'');
-- 模型配置
insert into sys_menu values('2120','模型查询','2005','1','','','','',1,0,'F','0','0','agent:model:query','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2121','模型新增','2005','2','','','','',1,0,'F','0','0','agent:model:add','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2122','模型修改','2005','3','','','','',1,0,'F','0','0','agent:model:edit','#','admin',sysdate(),'',null,'');
insert into sys_menu values('2123','模型删除','2005','4','','','','',1,0,'F','0','0','agent:model:remove','#','admin',sysdate(),'',null,'');

-- ----------------------------
-- 用户角色绑定
-- ----------------------------
insert into sys_user_role values ('1', '1'); -- admin →超级管理员
insert into sys_user_role values ('2', '2'); -- user →纺织AI业务用户
insert into sys_user_role values ('3', '3'); -- guest →体验访客

-- ----------------------------
-- 角色菜单关联
-- 角色1(admin)：拥有全部菜单，不需要插入，若依逻辑超级管理员自动放行全部权限
-- 角色2 text_biz正式用户：开放对话、知识库、提示词；隐藏模型配置、向量库
-- 角色3 guest访客：仅开放对话工作台
-- ----------------------------
insert into sys_role_menu values ('2', '2000');
insert into sys_role_menu values ('2', '2001');
insert into sys_role_menu values ('2', '2002');
insert into sys_role_menu values ('2', '2003');
insert into sys_role_menu values ('2', '2006');
insert into sys_role_menu values ('2', '2100');
insert into sys_role_menu values ('2', '2101');
insert into sys_role_menu values ('2', '2102');
insert into sys_role_menu values ('2', '2110');
insert into sys_role_menu values ('2', '2111');
insert into sys_role_menu values ('2', '2112');
insert into sys_role_menu values ('2', '2113');
insert into sys_role_menu values ('2', '2114');

insert into sys_role_menu values ('3', '2000');
insert into sys_role_menu values ('3', '2001');
insert into sys_role_menu values ('3', '2100');
insert into sys_role_menu values ('3', '2101');

-- ----------------------------
-- 用户岗位关联
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');
insert into sys_user_post values ('3', '3');

-- ----------------------------
-- AI智能体：初始化用户额度
-- ----------------------------
insert into sys_user_agent_quota(user_id,day_quota,day_used,total_quota,total_used,reset_time,create_time)
values
    (1,9999,0,99999,0,null,sysdate()),
    (2,200,0,10000,0,null,sysdate()),
    (3,10,0,100,0,null,sysdate()); -- guest访客每日仅10次调用

-- ----------------------------
-- AI智能体：初始化模型配置记录
-- ----------------------------
insert into sys_agent_model(model_name,model_code,base_url,api_key,temperature,max_tokens,status,create_by,create_time,remark)
values
    ('DeepSeek‑R1','deepseek‑r1','https://api.deepseek.com/v1','','0.7',8192,'0','admin',sysdate(),'纺织智能体默认推理模型');

-- 下面保留原若依基础字典、参数、公告、job等初始化数据
insert into sys_dict_type values(1,  '用户性别', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '用户性别列表');
insert into sys_dict_type values(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '菜单状态列表');
insert into sys_dict_type values(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '系统开关列表');
insert into sys_dict_type values(4,  '任务状态', 'sys_job_status',      '0', 'admin', sysdate(), '', null, '任务状态列表');
insert into sys_dict_type values(5,  '任务分组', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '任务分组列表');
insert into sys_dict_type values(6,  '系统是否', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '系统是否列表');
insert into sys_dict_type values(7,  '通知类型', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, '通知类型列表');
insert into sys_dict_type values(8,  '通知状态', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, '通知状态列表');
insert into sys_dict_type values(9,  '操作类型', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '操作类型列表');
insert into sys_dict_type values(10, '系统状态', 'sys_common_status',   '0', 'admin', sysdate(), '', null, '登录状态列表');

insert into sys_dict_data values(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '性别男');
insert into sys_dict_data values(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别女');
insert into sys_dict_data values(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别未知');
insert into sys_dict_data values(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '显示菜单');
insert into sys_dict_data values(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '隐藏菜单');
insert into sys_dict_data values(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(8,  1,  '正常',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(9,  2,  '暂停',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(10, 1,  '默认',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '默认分组');
insert into sys_dict_data values(11, 2,  '系统',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '系统分组');
insert into sys_dict_data values(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统默认是');
insert into sys_dict_data values(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '系统默认否');
insert into sys_dict_data values(14, 1,  '通知',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, '通知');
insert into sys_dict_data values(15, 2,  '公告',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, '公告');
insert into sys_dict_data values(16, 1,  '正常',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(17, 2,  '关闭',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '关闭状态');
insert into sys_dict_data values(18, 99, '其他',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '其他操作');
insert into sys_dict_data values(19, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data values(20, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data values(21, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '删除操作');
insert into sys_dict_data values(22, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '授权操作');
insert into sys_dict_data values(23, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导出操作');
insert into sys_dict_data values(24, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导入操作');
insert into sys_dict_data values(25, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '强退操作');
insert into sys_dict_data values(26, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '生成操作');
insert into sys_dict_data values(27, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '清空操作');
insert into sys_dict_data values(28, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(29, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');

insert into sys_config values(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', sysdate(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',        'Y', 'admin', sysdate(), '', null, '初始化密码 123456' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'true',          'Y', 'admin', sysdate(), '', null, '是否开启验证码功能（true开启，false关闭）');
insert into sys_config values(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, '是否开启注册用户功能（true开启，false关闭）');

insert into sys_job values(1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');

insert into sys_notice values('1', '纺织智能体系统上线通知', '1', '<p>纺织AI智能体已上线，支持面料知识库、工艺问答</p>', '0', 'admin', sysdate(), '', null, '管理员');