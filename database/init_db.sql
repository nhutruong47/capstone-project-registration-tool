-- Script khởi tạo database cho dự án SmartCourt AI
USE master;
GO
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'bookDB')
BEGIN
    CREATE DATABASE bookDB;
END
GO
USE bookDB;
GO

-- Tạo bảng người dùng
CREATE TABLE users (
                       id BIGINT IDENTITY(1,1) PRIMARY KEY,
                       username NVARCHAR(50) UNIQUE NOT NULL,
                       password NVARCHAR(255) NOT NULL,
                       full_name NVARCHAR(100)
);

-- Thêm dữ liệu mẫu để test Đăng nhập
INSERT INTO users (username, password, full_name)
VALUES ('admin', '12345', 'Trương Thái Như'); -- Dữ liệu khớp với application.properties của bạn