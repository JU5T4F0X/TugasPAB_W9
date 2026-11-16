-- Skrip Pembuatan Database & Tabel Mahasiswa
CREATE DATABASE IF NOT EXISTS `kampus_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `kampus_db`;

CREATE TABLE IF NOT EXISTS `mahasiswa` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `nim` VARCHAR(20) NOT NULL UNIQUE,
    `nama` VARCHAR(100) NOT NULL,
    `jurusan` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `alamat` TEXT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data Dummy Awal
INSERT INTO `mahasiswa` (`nim`, `nama`, `jurusan`, `email`, `alamat`) VALUES
('2024001', 'Ahmad Pratama', 'Teknik Informatika', 'ahmad.pratama@kampus.ac.id', 'Jl. Merdeka No. 10, Jakarta'),
('2024002', 'Siti Nurhaliza', 'Sistem Informasi', 'siti.nurhaliza@kampus.ac.id', 'Jl. Sudirman No. 45, Bandung'),
('2024003', 'Budi Santoso', 'Teknik Elektro', 'budi.santoso@kampus.ac.id', 'Jl. Diponegoro No. 88, Surabaya');
