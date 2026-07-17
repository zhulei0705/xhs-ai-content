CREATE DATABASE IF NOT EXISTS xhs_ai_content
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE xhs_ai_content;

CREATE TABLE IF NOT EXISTS xhs_topic (
  id BIGINT NOT NULL COMMENT '选题ID',
  topic_title VARCHAR(200) NOT NULL COMMENT '选题名称',
  core_angle VARCHAR(500) NOT NULL COMMENT '核心切入角度',
  target_audience VARCHAR(200) NOT NULL COMMENT '细分目标用户',
  content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
  keywords_json JSON NOT NULL COMMENT '关键词JSON数组',
  target_match_score TINYINT UNSIGNED NOT NULL COMMENT '目标用户匹配度',
  viral_potential_score TINYINT UNSIGNED NOT NULL COMMENT '爆款潜力',
  overall_score TINYINT UNSIGNED NOT NULL COMMENT '综合评分',
  potential_analysis VARCHAR(1000) NOT NULL COMMENT '潜力分析',
  publish_priority TINYINT UNSIGNED NOT NULL COMMENT '推荐优先级，越小越优先',
  source_type VARCHAR(20) NOT NULL DEFAULT 'AI' COMMENT 'AI/MANUAL',
  generation_date DATE NOT NULL COMMENT '生成日期',
  status VARCHAR(20) NOT NULL DEFAULT 'CANDIDATE' COMMENT '选题状态',
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_topic_date_title (generation_date, topic_title, deleted),
  KEY idx_topic_date_score (generation_date, overall_score),
  KEY idx_topic_status (status, deleted),
  CONSTRAINT chk_topic_score CHECK (
    target_match_score BETWEEN 0 AND 100
    AND viral_potential_score BETWEEN 0 AND 100
    AND overall_score BETWEEN 0 AND 100
    AND publish_priority BETWEEN 1 AND 10
  )
) ENGINE=InnoDB COMMENT='AI选题';

CREATE TABLE IF NOT EXISTS xhs_content (
  id BIGINT NOT NULL COMMENT '内容ID',
  topic_id BIGINT NULL COMMENT '来源选题ID',
  selected_title VARCHAR(100) NOT NULL COMMENT '当前选中标题',
  body TEXT NOT NULL COMMENT '小红书正文',
  cover_title VARCHAR(50) NOT NULL COMMENT '封面主标题',
  cover_subtitle VARCHAR(80) NOT NULL COMMENT '封面副标题',
  tags_json JSON NOT NULL COMMENT '话题标签JSON数组',
  interaction_guide VARCHAR(500) NOT NULL COMMENT '评论互动引导语',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '内容状态',
  current_review_id BIGINT NULL COMMENT '最新审核ID',
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_content_topic (topic_id),
  KEY idx_content_status_updated (status, updated_at),
  KEY idx_content_review (current_review_id)
) ENGINE=InnoDB COMMENT='内容主表';

CREATE TABLE IF NOT EXISTS xhs_content_title (
  id BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  title_text VARCHAR(100) NOT NULL,
  sort_order TINYINT UNSIGNED NOT NULL,
  selected TINYINT NOT NULL DEFAULT 0,
  attraction_score TINYINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_content_title_order (content_id, sort_order, deleted),
  KEY idx_title_content (content_id),
  CONSTRAINT chk_title_score CHECK (attraction_score IS NULL OR attraction_score BETWEEN 0 AND 100)
) ENGINE=InnoDB COMMENT='内容候选标题';

CREATE TABLE IF NOT EXISTS xhs_content_card (
  id BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  card_no TINYINT UNSIGNED NOT NULL,
  card_title VARCHAR(60) NOT NULL,
  card_body VARCHAR(500) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_content_card_no (content_id, card_no, deleted),
  KEY idx_card_content (content_id),
  CONSTRAINT chk_card_no CHECK (card_no BETWEEN 1 AND 6)
) ENGINE=InnoDB COMMENT='图文卡片文案';

CREATE TABLE IF NOT EXISTS xhs_ai_review (
  id BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  ai_tone_score TINYINT UNSIGNED NOT NULL COMMENT '自然度，越高越自然',
  title_attraction_score TINYINT UNSIGNED NOT NULL,
  authenticity_score TINYINT UNSIGNED NOT NULL,
  platform_fit_score TINYINT UNSIGNED NOT NULL,
  exaggerated_income_risk TINYINT NOT NULL DEFAULT 0,
  sensitive_expression_risk TINYINT NOT NULL DEFAULT 0,
  marketing_risk TINYINT NOT NULL DEFAULT 0,
  risk_level VARCHAR(16) NOT NULL COMMENT 'LOW/MEDIUM/HIGH',
  summary VARCHAR(1000) NOT NULL,
  suggestions_json JSON NOT NULL,
  provider VARCHAR(32) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  reviewed_at DATETIME(3) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_review_content_time (content_id, reviewed_at),
  CONSTRAINT chk_review_scores CHECK (
    ai_tone_score BETWEEN 0 AND 100
    AND title_attraction_score BETWEEN 0 AND 100
    AND authenticity_score BETWEEN 0 AND 100
    AND platform_fit_score BETWEEN 0 AND 100
  )
) ENGINE=InnoDB COMMENT='AI内容审核';

CREATE TABLE IF NOT EXISTS xhs_publish_record (
  id BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  published_at DATETIME(3) NOT NULL,
  note_url VARCHAR(500) NOT NULL,
  exposure_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  like_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  favorite_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  comment_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  follower_growth INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_publish_content (content_id, deleted),
  KEY idx_publish_time (published_at)
) ENGINE=InnoDB COMMENT='人工发布记录';

CREATE TABLE IF NOT EXISTS xhs_performance_analysis (
  id BIGINT NOT NULL,
  publish_record_id BIGINT NOT NULL,
  overall_grade VARCHAR(8) NOT NULL,
  performance_summary VARCHAR(1000) NOT NULL,
  topic_analysis VARCHAR(1000) NOT NULL,
  title_analysis VARCHAR(1000) NOT NULL,
  favorite_rate_analysis VARCHAR(1000) NOT NULL,
  interaction_rate_analysis VARCHAR(1000) NOT NULL,
  next_topic_suggestions_json JSON NOT NULL,
  provider VARCHAR(32) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  analyzed_at DATETIME(3) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_analysis_publish_time (publish_record_id, analyzed_at)
) ENGINE=InnoDB COMMENT='发布数据AI复盘';

CREATE TABLE IF NOT EXISTS xhs_ai_task (
  id BIGINT NOT NULL,
  task_no VARCHAR(40) NOT NULL,
  task_type VARCHAR(32) NOT NULL,
  biz_id BIGINT NULL,
  status VARCHAR(20) NOT NULL,
  provider VARCHAR(32) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  prompt_version VARCHAR(32) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  result_summary VARCHAR(1000) NULL,
  prompt_tokens INT NULL,
  completion_tokens INT NULL,
  duration_ms BIGINT NULL,
  error_message VARCHAR(1000) NULL,
  started_at DATETIME(3) NOT NULL,
  finished_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  version INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_task_no (task_no),
  KEY idx_ai_task_biz (task_type, biz_id),
  KEY idx_ai_task_status_time (status, started_at)
) ENGINE=InnoDB COMMENT='AI任务审计';
