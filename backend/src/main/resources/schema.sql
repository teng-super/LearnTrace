CREATE DATABASE IF NOT EXISTS learntrace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE learntrace;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(80) NOT NULL,
  avatar_url VARCHAR(500),
  theme VARCHAR(40) NOT NULL DEFAULT 'battle-dark',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goals (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  description TEXT,
  category VARCHAR(80),
  priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
  start_date DATE,
  deadline DATE,
  progress INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_goals_user_status (user_id, status),
  INDEX idx_goals_deadline (deadline),
  CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goal_stages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goal_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  description TEXT,
  stage_order INT NOT NULL DEFAULT 1,
  status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
  start_date DATE,
  deadline DATE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_goal_stages_goal (goal_id, stage_order),
  CONSTRAINT fk_goal_stages_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_goal_stages_goal FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tasks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goal_id BIGINT,
  stage_id BIGINT,
  week_no INT,
  title VARCHAR(220) NOT NULL,
  description TEXT,
  task_type VARCHAR(40) NOT NULL DEFAULT 'CUSTOM',
  status VARCHAR(20) NOT NULL DEFAULT 'TODO',
  priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  due_date DATE,
  completed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_tasks_user_status_due (user_id, status, due_date),
  INDEX idx_tasks_goal (goal_id),
  INDEX idx_tasks_week (week_no),
  CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_tasks_goal FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL,
  CONSTRAINT fk_tasks_stage FOREIGN KEY (stage_id) REFERENCES goal_stages(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS error_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goal_id BIGINT,
  task_id BIGINT,
  title VARCHAR(220) NOT NULL,
  error_type VARCHAR(50) NOT NULL DEFAULT 'OTHER',
  description MEDIUMTEXT,
  wrong_code MEDIUMTEXT,
  correct_code MEDIUMTEXT,
  reason MEDIUMTEXT,
  solution MEDIUMTEXT,
  summary TEXT,
  severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  status VARCHAR(30) NOT NULL DEFAULT 'UNRESOLVED',
  review_count INT NOT NULL DEFAULT 0,
  next_review_at DATETIME,
  last_reviewed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_errors_user_status (user_id, status),
  INDEX idx_errors_goal (goal_id),
  INDEX idx_errors_type (error_type),
  INDEX idx_errors_review (next_review_at),
  CONSTRAINT fk_errors_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_errors_goal FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL,
  CONSTRAINT fk_errors_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL,
  color VARCHAR(24) NOT NULL DEFAULT '#f6c453',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tags_user_name (user_id, name),
  CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS error_log_tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  error_log_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  UNIQUE KEY uk_error_tag (error_log_id, tag_id),
  INDEX idx_error_log_tags_tag (tag_id),
  CONSTRAINT fk_error_log_tags_error FOREIGN KEY (error_log_id) REFERENCES error_logs(id) ON DELETE CASCADE,
  CONSTRAINT fk_error_log_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  error_log_id BIGINT NOT NULL,
  result VARCHAR(30) NOT NULL,
  note TEXT,
  reviewed_at DATETIME NOT NULL,
  next_review_at DATETIME NOT NULL,
  INDEX idx_reviews_user_time (user_id, reviewed_at),
  INDEX idx_reviews_error (error_log_id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_reviews_error FOREIGN KEY (error_log_id) REFERENCES error_logs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS plan_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(80) NOT NULL UNIQUE,
  title VARCHAR(160) NOT NULL,
  description TEXT,
  source_type VARCHAR(40) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS plan_template_tasks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_id BIGINT NOT NULL,
  week_no INT,
  section_title VARCHAR(120),
  title VARCHAR(260) NOT NULL,
  description TEXT,
  task_type VARCHAR(40) NOT NULL,
  sort_order INT NOT NULL,
  UNIQUE KEY uk_template_sort (template_id, sort_order),
  INDEX idx_template_tasks_template (template_id),
  CONSTRAINT fk_template_tasks_template FOREIGN KEY (template_id) REFERENCES plan_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goal_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(80) NOT NULL UNIQUE,
  title VARCHAR(160) NOT NULL,
  description TEXT,
  category VARCHAR(80),
  priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  source_type VARCHAR(40) NOT NULL,
  recommended_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goal_template_stages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  description TEXT,
  stage_order INT NOT NULL,
  UNIQUE KEY uk_goal_template_stage (template_id, stage_order),
  CONSTRAINT fk_goal_template_stage_template FOREIGN KEY (template_id) REFERENCES goal_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS file_assets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  original_name VARCHAR(260) NOT NULL,
  stored_name VARCHAR(260) NOT NULL,
  storage_path VARCHAR(700) NOT NULL,
  file_type VARCHAR(40) NOT NULL,
  mime_type VARCHAR(160),
  size_bytes BIGINT NOT NULL DEFAULT 0,
  relation_type VARCHAR(60),
  relation_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_files_user (user_id, relation_type, relation_id),
  CONSTRAINT fk_files_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS study_notes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  goal_id BIGINT,
  task_id BIGINT,
  error_log_id BIGINT,
  title VARCHAR(220) NOT NULL,
  content_markdown MEDIUMTEXT,
  note_type VARCHAR(40) NOT NULL DEFAULT 'MARKDOWN',
  source_file_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_notes_user (user_id, updated_at),
  INDEX idx_notes_goal (goal_id),
  CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_notes_goal FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL,
  CONSTRAINT fk_notes_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
  CONSTRAINT fk_notes_error FOREIGN KEY (error_log_id) REFERENCES error_logs(id) ON DELETE SET NULL,
  CONSTRAINT fk_notes_file FOREIGN KEY (source_file_id) REFERENCES file_assets(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pdf_annotations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  file_asset_id BIGINT NOT NULL,
  page_no INT NOT NULL,
  annotation_type VARCHAR(40) NOT NULL DEFAULT 'HIGHLIGHT',
  selected_text TEXT,
  note TEXT,
  rect_json TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_pdf_annotations_file (file_asset_id, page_no),
  CONSTRAINT fk_pdf_annotations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_pdf_annotations_file FOREIGN KEY (file_asset_id) REFERENCES file_assets(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
