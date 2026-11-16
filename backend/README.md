# Backend REST API Data Mahasiswa (PHP Native)

Backend murni (*backend-only*) tanpa antarmuka/UI, menyajikan endpoint RESTful API berbasis JSON untuk manajemen data mahasiswa (CRUD).

---

## 📁 Struktur Direktori

```
backend/
├── config/
│   └── Database.php       # Konfigurasi koneksi MySQL berbasis PDO
├── models/
│   └── Mahasiswa.php      # Model & query database data mahasiswa
├── api/
│   └── mahasiswa.php      # Controller REST API (GET, POST, PUT, DELETE)
├── database.sql           # Skrip database & tabel mahasiswa
└── README.md              # Dokumentasi & panduan penggunaan
```

---

## 🚀 Persiapan & Menjalankan Backend

### 1. Import Database ke MySQL
Buka phpMyAdmin atau terminal MySQL:
```bash
mysql -u root -p < backend/database.sql
```

### 2. Konfigurasi Database (Opsional)
Secara default koneksi menggunakan:
- Host: `127.0.0.1`
- Port: `3306`
- Database: `kampus_db`
- User: `root`
- Password: *(kosong)*

Jika berbeda, Anda dapat menyetel environment variable sebelum menjalankan PHP:
```bash
export DB_HOST="localhost"
export DB_NAME="kampus_db"
export DB_USER="root"
export DB_PASS="password_anda"
```

### 3. Menjalankan Server Lokal PHP
Jalankan server bawaan PHP dari root folder project:
```bash
php -S localhost:8000 -t backend
```

Endpoint API akan aktif di:
`http://localhost:8000/api/mahasiswa.php`

---

## 📡 Dokumentasi Endpoint REST API

Semua request dan response bertipe `application/json`.

### 1. Ambil Semua Data Mahasiswa
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/mahasiswa.php`
- **Parameter Opsional:** `?search=keyword` (mencari nama, nim, atau jurusan)
- **Contoh Request:**
  ```bash
  curl -X GET "http://localhost:8000/api/mahasiswa.php"
  ```
- **Contoh Response (200 OK):**
  ```json
  {
    "status": "success",
    "total": 3,
    "data": [
      {
        "id": 1,
        "nim": "2024001",
        "nama": "Ahmad Pratama",
        "jurusan": "Teknik Informatika",
        "email": "ahmad.pratama@kampus.ac.id",
        "alamat": "Jl. Merdeka No. 10, Jakarta",
        "created_at": "2026-11-10 11:42:00",
        "updated_at": "2026-11-10 11:42:00"
      }
    ]
  }
  ```

---

### 2. Ambil Data Mahasiswa Berdasarkan ID atau NIM
- **Method:** `GET`
- **URL:** 
  - Berdasarkan ID: `http://localhost:8000/api/mahasiswa.php?id=1`
  - Berdasarkan NIM: `http://localhost:8000/api/mahasiswa.php?nim=2024001`
- **Contoh Request:**
  ```bash
  curl -X GET "http://localhost:8000/api/mahasiswa.php?id=1"
  ```
- **Contoh Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "id": 1,
      "nim": "2024001",
      "nama": "Ahmad Pratama",
      "jurusan": "Teknik Informatika",
      "email": "ahmad.pratama@kampus.ac.id",
      "alamat": "Jl. Merdeka No. 10, Jakarta",
      "created_at": "2026-11-10 11:42:00",
      "updated_at": "2026-11-10 11:42:00"
    }
  }
  ```

---

### 3. Tambah Data Mahasiswa Baru
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/mahasiswa.php`
- **Header:** `Content-Type: application/json`
- **Body JSON:**
  ```json
  {
    "nim": "2024004",
    "nama": "Rian Kusuma",
    "jurusan": "Sistem Komputer",
    "email": "rian.kusuma@kampus.ac.id",
    "alamat": "Jl. Pahlawan No. 12"
  }
  ```
- **Contoh Request:**
  ```bash
  curl -X POST "http://localhost:8000/api/mahasiswa.php" \
       -H "Content-Type: application/json" \
       -d '{"nim":"2024004","nama":"Rian Kusuma","jurusan":"Sistem Komputer","email":"rian.kusuma@kampus.ac.id","alamat":"Jl. Pahlawan No. 12"}'
  ```
- **Contoh Response (201 Created):**
  ```json
  {
    "status": "success",
    "message": "Data mahasiswa berhasil ditambahkan",
    "data": {
      "id": 4,
      "nim": "2024004",
      "nama": "Rian Kusuma",
      "jurusan": "Sistem Komputer",
      "email": "rian.kusuma@kampus.ac.id",
      "alamat": "Jl. Pahlawan No. 12",
      "created_at": "2026-11-10 11:45:00",
      "updated_at": "2026-11-10 11:45:00"
    }
  }
  ```

---

### 4. Perbarui Data Mahasiswa
- **Method:** `PUT`
- **URL:** `http://localhost:8000/api/mahasiswa.php?id=4` (atau sertakan `"id": 4` pada body JSON)
- **Header:** `Content-Type: application/json`
- **Body JSON:**
  ```json
  {
    "nim": "2024004",
    "nama": "Rian Kusuma, S.Kom",
    "jurusan": "Sistem Komputer",
    "email": "rian.kusuma.new@kampus.ac.id",
    "alamat": "Jl. Pahlawan No. 99"
  }
  ```
- **Contoh Request:**
  ```bash
  curl -X PUT "http://localhost:8000/api/mahasiswa.php?id=4" \
       -H "Content-Type: application/json" \
       -d '{"nim":"2024004","nama":"Rian Kusuma, S.Kom","jurusan":"Sistem Komputer","email":"rian.kusuma.new@kampus.ac.id","alamat":"Jl. Pahlawan No. 99"}'
  ```
- **Contoh Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Data mahasiswa berhasil diperbarui",
    "data": { ... }
  }
  ```

---

### 5. Hapus Data Mahasiswa
- **Method:** `DELETE`
- **URL:** `http://localhost:8000/api/mahasiswa.php?id=4`
- **Contoh Request:**
  ```bash
  curl -X DELETE "http://localhost:8000/api/mahasiswa.php?id=4"
  ```
- **Contoh Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Data mahasiswa dengan ID 4 berhasil dihapus"
  }
  ```
