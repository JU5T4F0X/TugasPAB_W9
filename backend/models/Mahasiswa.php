<?php
declare(strict_types=1);

namespace App\Models;

use PDO;

class Mahasiswa
{
    private PDO $conn;
    private string $table = 'mahasiswa';

    public function __construct(PDO $db)
    {
        $this->conn = $db;
    }

    /**
     * Mengambil semua data mahasiswa atau berdasarkan pencarian nama/nim/jurusan
     */
    public function getAll(?string $search = null): array
    {
        if ($search !== null && trim($search) !== '') {
            $query = "SELECT * FROM {$this->table}
                      WHERE nama LIKE :search OR nim LIKE :search OR jurusan LIKE :search
                      ORDER BY id DESC";
            $stmt = $this->conn->prepare($query);
            $stmt->execute([':search' => '%' . trim($search) . '%']);
        } else {
            $query = "SELECT * FROM {$this->table} ORDER BY id DESC";
            $stmt = $this->conn->prepare($query);
            $stmt->execute();
        }

        return $stmt->fetchAll();
    }

    /**
     * Mengambil 1 data mahasiswa berdasarkan ID
     */
    public function getById(int $id): ?array
    {
        $query = "SELECT * FROM {$this->table} WHERE id = :id LIMIT 1";
        $stmt = $this->conn->prepare($query);
        $stmt->execute([':id' => $id]);
        $row = $stmt->fetch();

        return $row ?: null;
    }

    /**
     * Mengambil 1 data mahasiswa berdasarkan NIM
     */
    public function getByNim(string $nim): ?array
    {
        $query = "SELECT * FROM {$this->table} WHERE nim = :nim LIMIT 1";
        $stmt = $this->conn->prepare($query);
        $stmt->execute([':nim' => $nim]);
        $row = $stmt->fetch();

        return $row ?: null;
    }

    /**
     * Cek apakah NIM sudah terdaftar (bisa kecualikan ID tertentu untuk update)
     */
    public function isNimExists(string $nim, ?int $excludeId = null): bool
    {
        $query = "SELECT COUNT(*) FROM {$this->table} WHERE nim = :nim";
        $params = [':nim' => $nim];

        if ($excludeId !== null) {
            $query .= " AND id != :excludeId";
            $params[':excludeId'] = $excludeId;
        }

        $stmt = $this->conn->prepare($query);
        $stmt->execute($params);

        return ((int)$stmt->fetchColumn()) > 0;
    }

    /**
     * Cek apakah Email sudah terdaftar (bisa kecualikan ID tertentu untuk update)
     */
    public function isEmailExists(string $email, ?int $excludeId = null): bool
    {
        $query = "SELECT COUNT(*) FROM {$this->table} WHERE email = :email";
        $params = [':email' => $email];

        if ($excludeId !== null) {
            $query .= " AND id != :excludeId";
            $params[':excludeId'] = $excludeId;
        }

        $stmt = $this->conn->prepare($query);
        $stmt->execute($params);

        return ((int)$stmt->fetchColumn()) > 0;
    }

    /**
     * Menyimpan data mahasiswa baru
     */
    public function create(array $data): ?int
    {
        $query = "INSERT INTO {$this->table} (nim, nama, jurusan, email, alamat)
                  VALUES (:nim, :nama, :jurusan, :email, :alamat)";

        $stmt = $this->conn->prepare($query);
        $success = $stmt->execute([
            ':nim' => $data['nim'],
            ':nama' => $data['nama'],
            ':jurusan' => $data['jurusan'],
            ':email' => $data['email'],
            ':alamat' => $data['alamat'] ?? null,
        ]);

        return $success ? (int)$this->conn->lastInsertId() : null;
    }

    /**
     * Mengubah data mahasiswa
     */
    public function update(int $id, array $data): bool
    {
        $query = "UPDATE {$this->table}
                  SET nim = :nim, nama = :nama, jurusan = :jurusan, email = :email, alamat = :alamat
                  WHERE id = :id";

        $stmt = $this->conn->prepare($query);
        return $stmt->execute([
            ':id' => $id,
            ':nim' => $data['nim'],
            ':nama' => $data['nama'],
            ':jurusan' => $data['jurusan'],
            ':email' => $data['email'],
            ':alamat' => $data['alamat'] ?? null,
        ]);
    }

    /**
     * Menghapus data mahasiswa berdasarkan ID
     */
    public function delete(int $id): bool
    {
        $query = "DELETE FROM {$this->table} WHERE id = :id";
        $stmt = $this->conn->prepare($query);
        $stmt->execute([':id' => $id]);

        return $stmt->rowCount() > 0;
    }
}
