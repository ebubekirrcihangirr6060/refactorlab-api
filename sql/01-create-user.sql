-- Uygulamanın kullandığı MySQL kullanıcısını oluşturur (root ile BİR KEZ çalıştırılır).
-- Çalıştırma: mysql -u root -p < sql/01-create-user.sql

CREATE USER IF NOT EXISTS 'springstudent'@'localhost' IDENTIFIED BY 'springstudent';

GRANT ALL PRIVILEGES ON *.* TO 'springstudent'@'localhost';

FLUSH PRIVILEGES;
