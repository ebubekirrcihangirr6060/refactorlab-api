-- OPSİYONEL: Uygulama bu tabloları zaten otomatik oluşturur (ddl-auto=update).
-- Elle kurmak/incelemek isteyenler için şema.

CREATE DATABASE IF NOT EXISTS refactorlab;
USE refactorlab;

CREATE TABLE IF NOT EXISTS users (
  id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,       -- Argon2id hash'i
  enabled BIT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS roles (
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  role VARCHAR(50) NOT NULL,            -- ROLE_VIEWER / ROLE_DEVELOPER / ROLE_REVIEWER
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS code_snippet (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(150) NOT NULL,
  language VARCHAR(20) NOT NULL,
  source_code TEXT NOT NULL,
  complexity_level VARCHAR(10),
  bottleneck_type VARCHAR(50),
  owner_username VARCHAR(50),
  created_at DATETIME(6),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS refactoring_rule (
  id INT NOT NULL AUTO_INCREMENT,
  smell_name VARCHAR(100) NOT NULL UNIQUE,
  category VARCHAR(50) NOT NULL,
  technique VARCHAR(100) NOT NULL,
  description VARCHAR(1000),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS analysis_result (
  id INT NOT NULL AUTO_INCREMENT,
  snippet_id INT NOT NULL,
  engine_name VARCHAR(50),
  detected_smells VARCHAR(1000),
  suggested_techniques VARCHAR(1000),
  modernization_hint TEXT,
  complexity_before VARCHAR(20),
  complexity_after VARCHAR(20),
  created_at DATETIME(6),
  PRIMARY KEY (id),
  FOREIGN KEY (snippet_id) REFERENCES code_snippet (id)
);

-- Kullanıcılar Argon2id hash'i gerektirdiği için uygulama ilk açılışta oluşturur (DataInitializer).
