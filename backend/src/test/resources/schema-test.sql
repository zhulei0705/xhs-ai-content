DROP ALL OBJECTS;

CREATE TABLE xhs_topic (
  id BIGINT PRIMARY KEY,
  topic_title VARCHAR(200) NOT NULL,
  core_angle VARCHAR(500) NOT NULL,
  target_audience VARCHAR(200) NOT NULL,
  content_type VARCHAR(32) NOT NULL,
  keywords_json VARCHAR(2000) NOT NULL,
  target_match_score INT NOT NULL,
  viral_potential_score INT NOT NULL,
  overall_score INT NOT NULL,
  potential_analysis VARCHAR(1000) NOT NULL,
  publish_priority INT NOT NULL,
  source_type VARCHAR(20) NOT NULL,
  generation_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  UNIQUE (generation_date, topic_title, deleted)
);

CREATE TABLE xhs_content (
  id BIGINT PRIMARY KEY,
  topic_id BIGINT,
  selected_title VARCHAR(100) NOT NULL,
  body CLOB NOT NULL,
  cover_title VARCHAR(50) NOT NULL,
  cover_subtitle VARCHAR(80) NOT NULL,
  tags_json VARCHAR(2000) NOT NULL,
  interaction_guide VARCHAR(500) NOT NULL,
  status VARCHAR(32) NOT NULL,
  current_review_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL
);

CREATE TABLE xhs_content_title (
  id BIGINT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  title_text VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL,
  selected BOOLEAN NOT NULL,
  attraction_score INT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL
);

CREATE TABLE xhs_content_card (
  id BIGINT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  card_no INT NOT NULL,
  card_title VARCHAR(60) NOT NULL,
  card_body VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL
);

CREATE TABLE xhs_ai_review (
  id BIGINT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  ai_tone_score INT NOT NULL,
  title_attraction_score INT NOT NULL,
  authenticity_score INT NOT NULL,
  platform_fit_score INT NOT NULL,
  exaggerated_income_risk BOOLEAN NOT NULL,
  sensitive_expression_risk BOOLEAN NOT NULL,
  marketing_risk BOOLEAN NOT NULL,
  risk_level VARCHAR(16) NOT NULL,
  summary VARCHAR(1000) NOT NULL,
  suggestions_json VARCHAR(2000) NOT NULL,
  provider VARCHAR(32) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  reviewed_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL
);

CREATE TABLE xhs_publish_record (
  id BIGINT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  published_at TIMESTAMP NOT NULL,
  note_url VARCHAR(500) NOT NULL,
  exposure_count BIGINT DEFAULT 0 NOT NULL,
  like_count BIGINT DEFAULT 0 NOT NULL,
  favorite_count BIGINT DEFAULT 0 NOT NULL,
  comment_count BIGINT DEFAULT 0 NOT NULL,
  follower_growth INT DEFAULT 0 NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  UNIQUE (content_id, deleted)
);

CREATE TABLE xhs_performance_analysis (
  id BIGINT PRIMARY KEY,
  publish_record_id BIGINT NOT NULL,
  overall_grade VARCHAR(8) NOT NULL,
  performance_summary VARCHAR(1000) NOT NULL,
  topic_analysis VARCHAR(1000) NOT NULL,
  title_analysis VARCHAR(1000) NOT NULL,
  favorite_rate_analysis VARCHAR(1000) NOT NULL,
  interaction_rate_analysis VARCHAR(1000) NOT NULL,
  next_topic_suggestions_json VARCHAR(2000) NOT NULL,
  provider VARCHAR(32) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  analyzed_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL
);

CREATE TABLE xhs_ai_task (
  id BIGINT PRIMARY KEY,
  task_no VARCHAR(40) NOT NULL UNIQUE,
  task_type VARCHAR(32) NOT NULL,
  biz_id BIGINT,
  status VARCHAR(20) NOT NULL,
  provider VARCHAR(32) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  prompt_version VARCHAR(32) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  result_summary VARCHAR(1000),
  prompt_tokens INT,
  completion_tokens INT,
  duration_ms BIGINT,
  error_message VARCHAR(1000),
  started_at TIMESTAMP NOT NULL,
  finished_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version INT DEFAULT 0 NOT NULL,
  deleted INT DEFAULT 0 NOT NULL
);
