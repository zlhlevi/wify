CREATE TABLE IF NOT EXISTS provider (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name varchar(100) NOT NULL DEFAULT '' COMMENT '供应商名称，唯一',
    type varchar(30) NOT NULL DEFAULT '' COMMENT '供应商类型：OPENAI/ANTHROPIC/OLLAMA/OPENAI_COMPATIBLE',
    base_url varchar(500) NOT NULL DEFAULT '' COMMENT 'API基础地址',
    auth_config json DEFAULT NULL COMMENT '鉴权配置，结构按type不同',
    enabled tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_name (name),
    KEY idx_provider_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型提供商';

CREATE TABLE IF NOT EXISTS model_config (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    provider_id bigint NOT NULL COMMENT '提供商ID',
    name varchar(100) NOT NULL DEFAULT '' COMMENT '展示名，如GPT-4o',
    model_id varchar(100) NOT NULL DEFAULT '' COMMENT '调用时传给API的值',
    context_size int DEFAULT NULL COMMENT '上下文窗口大小（token数）',
    extra_params json DEFAULT NULL COMMENT '模型级别扩展参数',
    enabled tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_model_config_provider_id_name (provider_id, name),
    KEY idx_model_config_model_id (model_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型配置';

CREATE TABLE IF NOT EXISTS provider_health (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    provider_id bigint NOT NULL COMMENT '供应商ID，唯一索引',
    status varchar(20) NOT NULL DEFAULT 'UNKNOWN' COMMENT '健康状态：UP/DOWN/DEGRADED/UNKNOWN',
    last_check_at datetime DEFAULT NULL COMMENT '最后探测时间',
    last_success_at datetime DEFAULT NULL COMMENT '最后成功时间',
    fail_count int NOT NULL DEFAULT 0 COMMENT '连续失败次数',
    latency_ms int DEFAULT NULL COMMENT '最近一次延迟，单位毫秒',
    error_message varchar(500) NOT NULL DEFAULT '' COMMENT '最近失败原因',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_health_provider_id (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商健康状态';

CREATE TABLE IF NOT EXISTS mcp_server (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name varchar(100) NOT NULL DEFAULT '' COMMENT 'MCP服务名称',
    description varchar(255) NOT NULL DEFAULT '' COMMENT '服务描述',
    transport_type varchar(32) NOT NULL DEFAULT '' COMMENT '传输类型，例如 sse/stdio/http',
    server_url varchar(255) NOT NULL DEFAULT '' COMMENT '服务地址',
    command varchar(255) NOT NULL DEFAULT '' COMMENT 'stdio启动命令',
    args_json json DEFAULT NULL COMMENT '启动参数配置',
    headers_json json DEFAULT NULL COMMENT '请求头配置',
    env_json json DEFAULT NULL COMMENT '环境变量配置',
    timeout_ms int NOT NULL DEFAULT 10000 COMMENT '调用超时时间，单位毫秒',
    status tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    remark varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mcp_server_name (name),
    KEY idx_mcp_server_transport_type (transport_type),
    KEY idx_mcp_server_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP服务配置';

CREATE TABLE IF NOT EXISTS agent (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name varchar(100) NOT NULL DEFAULT '' COMMENT 'Agent名称',
    description varchar(255) NOT NULL DEFAULT '' COMMENT 'Agent描述',
    model_config_id bigint NOT NULL COMMENT '绑定的模型配置ID',
    system_prompt mediumtext COMMENT '系统提示词',
    status tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    config_json json DEFAULT NULL COMMENT 'Agent扩展配置',
    remark varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_name (name),
    KEY idx_agent_model_config_id (model_config_id),
    KEY idx_agent_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent配置';

CREATE TABLE IF NOT EXISTS agent_tool (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    agent_id bigint NOT NULL COMMENT 'Agent ID',
    mcp_server_id bigint NOT NULL COMMENT 'MCP服务ID',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_tool_agent_id_mcp_server_id (agent_id, mcp_server_id),
    KEY idx_agent_tool_agent_id (agent_id),
    KEY idx_agent_tool_mcp_server_id (mcp_server_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent与MCP服务关联表';

CREATE TABLE IF NOT EXISTS chat_session (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    agent_id bigint NOT NULL COMMENT 'Agent ID',
    title varchar(255) NOT NULL DEFAULT '' COMMENT '会话标题',
    status tinyint NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1进行中',
    last_message_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
    metadata_json json DEFAULT NULL COMMENT '会话元数据',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    KEY idx_chat_session_agent_id (agent_id),
    KEY idx_chat_session_status (status),
    KEY idx_chat_session_last_message_at (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话会话';

CREATE TABLE IF NOT EXISTS chat_message (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    session_id bigint NOT NULL COMMENT '会话ID',
    agent_id bigint NOT NULL COMMENT 'Agent ID',
    role varchar(32) NOT NULL DEFAULT '' COMMENT '消息角色，例如 system/user/assistant/tool',
    message_type varchar(32) NOT NULL DEFAULT 'text' COMMENT '消息类型，例如 text/tool_call/tool_result',
    content mediumtext NOT NULL COMMENT '消息内容',
    model_name varchar(100) NOT NULL DEFAULT '' COMMENT '生成消息使用的模型名称',
    prompt_tokens int NOT NULL DEFAULT 0 COMMENT '输入Token数',
    completion_tokens int NOT NULL DEFAULT 0 COMMENT '输出Token数',
    total_tokens int NOT NULL DEFAULT 0 COMMENT '总Token数',
    finish_reason varchar(64) NOT NULL DEFAULT '' COMMENT '结束原因',
    metadata_json json DEFAULT NULL COMMENT '消息元数据',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    KEY idx_chat_message_session_id (session_id),
    KEY idx_chat_message_agent_id (agent_id),
    KEY idx_chat_message_session_id_id (session_id, id),
    KEY idx_chat_message_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话消息';

CREATE TABLE IF NOT EXISTS demo_item (
    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name varchar(100) NOT NULL DEFAULT '' COMMENT '名称',
    status int NOT NULL DEFAULT 0 COMMENT '状态',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常 1删除',
    PRIMARY KEY (id),
    KEY idx_demo_item_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CRUD演示表';
