<?php
declare(strict_types=1);

// Set Header JSON & CORS
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With');

// Tangani preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../models/Mahasiswa.php';

use App\Config\Database;
use App\Models\Mahasiswa;

// Inisialisasi Database dan Model
$database = new Database();
$db = $database->getConnection();
$mahasiswaModel = new Mahasiswa($db);

$method = $_SERVER['REQUEST_METHOD'];

// Helper fungsi respon JSON
function sendResponse(int $statusCode, array $payload): void
{
    http_response_code($statusCode);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    exit;
}

// Ambil payload JSON dari body request
function getJsonInput(): array
{
    $raw = file_get_contents('php://input');
    if (empty($raw)) {
        return [];
    }
    $decoded = json_decode($raw, true);
    return is_array($decoded) ? $decoded : [];
}

switch ($method) {
    case 'GET':
        handleGet($mahasiswaModel);
        break;

    case 'POST':
        handlePost($mahasiswaModel);
        break;

    case 'PUT':
        handlePut($mahasiswaModel);
        break;

    case 'DELETE':
        handleDelete($mahasiswaModel);
        break;

    default:
        sendResponse(405, [
            'status' => 'error',
            'message' => "Method {$method} tidak diizinkan",
        ]);
        break;
}

/**
 * Handler GET: Ambil semua data, cari, atau ambil berdasarkan ID / NIM
 */
function handleGet(Mahasiswa $model): void
{
    if (isset($_GET['id']) && $_GET['id'] !== '') {
        $id = (int)$_GET['id'];
        $data = $model->getById($id);

        if (!$data) {
            sendResponse(404, [
                'status' => 'error',
                'message' => "Data mahasiswa dengan ID {$id} tidak ditemukan",
            ]);
        }

        sendResponse(200, [
            'status' => 'success',
            'data' => $data,
        ]);
    }

    if (isset($_GET['nim']) && trim($_GET['nim']) !== '') {
        $nim = trim($_GET['nim']);
        $data = $model->getByNim($nim);

        if (!$data) {
            sendResponse(404, [
                'status' => 'error',
                'message' => "Data mahasiswa dengan NIM '{$nim}' tidak ditemukan",
            ]);
        }

        sendResponse(200, [
            'status' => 'success',
            'data' => $data,
        ]);
    }

    // Ambil list semua mahasiswa (dengan opsi search)
    $search = $_GET['search'] ?? null;
    $list = $model->getAll($search);

    sendResponse(200, [
        'status' => 'success',
        'total' => count($list),
        'data' => $list,
    ]);
}

/**
 * Handler POST: Tambah data mahasiswa baru
 */
function handlePost(Mahasiswa $model): void
{
    $input = getJsonInput();
    if (empty($input)) {
        // Fallback jika dikirimkan sebagai form-data x-www-form-urlencoded
        $input = $_POST;
    }

    $errors = validateMahasiswaInput($input, $model);
    if (!empty($errors)) {
        sendResponse(400, [
            'status' => 'error',
            'message' => 'Validasi gagal',
            'errors' => $errors,
        ]);
    }

    $insertedId = $model->create([
        'nim' => trim((string)$input['nim']),
        'nama' => trim((string)$input['nama']),
        'jurusan' => trim((string)$input['jurusan']),
        'email' => trim((string)$input['email']),
        'alamat' => isset($input['alamat']) ? trim((string)$input['alamat']) : null,
    ]);

    if (!$insertedId) {
        sendResponse(500, [
            'status' => 'error',
            'message' => 'Gagal menyimpan data mahasiswa',
        ]);
    }

    $createdData = $model->getById($insertedId);

    sendResponse(201, [
        'status' => 'success',
        'message' => 'Data mahasiswa berhasil ditambahkan',
        'data' => $createdData,
    ]);
}

/**
 * Handler PUT: Update data mahasiswa
 */
function handlePut(Mahasiswa $model): void
{
    $input = getJsonInput();

    // ID bisa didapat dari query parameter (?id=1) atau JSON body
    $id = isset($_GET['id']) ? (int)$_GET['id'] : (isset($input['id']) ? (int)$input['id'] : 0);

    if ($id <= 0) {
        sendResponse(400, [
            'status' => 'error',
            'message' => 'Parameter ID mahasiswa wajib disertakan untuk update',
        ]);
    }

    // Cek keberadaan data mahasiswa
    $existing = $model->getById($id);
    if (!$existing) {
        sendResponse(404, [
            'status' => 'error',
            'message' => "Data mahasiswa dengan ID {$id} tidak ditemukan",
        ]);
    }

    $errors = validateMahasiswaInput($input, $model, $id);
    if (!empty($errors)) {
        sendResponse(400, [
            'status' => 'error',
            'message' => 'Validasi gagal',
            'errors' => $errors,
        ]);
    }

    $success = $model->update($id, [
        'nim' => trim((string)$input['nim']),
        'nama' => trim((string)$input['nama']),
        'jurusan' => trim((string)$input['jurusan']),
        'email' => trim((string)$input['email']),
        'alamat' => isset($input['alamat']) ? trim((string)$input['alamat']) : null,
    ]);

    if (!$success) {
        sendResponse(500, [
            'status' => 'error',
            'message' => 'Gagal memperbarui data mahasiswa',
        ]);
    }

    $updatedData = $model->getById($id);

    sendResponse(200, [
        'status' => 'success',
        'message' => 'Data mahasiswa berhasil diperbarui',
        'data' => $updatedData,
    ]);
}

/**
 * Handler DELETE: Hapus data mahasiswa
 */
function handleDelete(Mahasiswa $model): void
{
    $input = getJsonInput();
    $id = isset($_GET['id']) ? (int)$_GET['id'] : (isset($input['id']) ? (int)$input['id'] : 0);

    if ($id <= 0) {
        sendResponse(400, [
            'status' => 'error',
            'message' => 'Parameter ID mahasiswa wajib disertakan untuk penghapusan',
        ]);
    }

    $existing = $model->getById($id);
    if (!$existing) {
        sendResponse(404, [
            'status' => 'error',
            'message' => "Data mahasiswa dengan ID {$id} tidak ditemukan",
        ]);
    }

    $deleted = $model->delete($id);
    if (!$deleted) {
        sendResponse(500, [
            'status' => 'error',
            'message' => 'Gagal menghapus data mahasiswa',
        ]);
    }

    sendResponse(200, [
        'status' => 'success',
        'message' => "Data mahasiswa dengan ID {$id} berhasil dihapus",
    ]);
}

/**
 * Validasi field input
 */
function validateMahasiswaInput(array $input, Mahasiswa $model, ?int $excludeId = null): array
{
    $errors = [];

    // Validasi NIM
    if (!isset($input['nim']) || trim((string)$input['nim']) === '') {
        $errors['nim'] = 'NIM wajib diisi';
    } elseif ($model->isNimExists(trim((string)$input['nim']), $excludeId)) {
        $errors['nim'] = 'NIM sudah terdaftar';
    }

    // Validasi Nama
    if (!isset($input['nama']) || trim((string)$input['nama']) === '') {
        $errors['nama'] = 'Nama lengkap wajib diisi';
    }

    // Validasi Jurusan
    if (!isset($input['jurusan']) || trim((string)$input['jurusan']) === '') {
        $errors['jurusan'] = 'Jurusan wajib diisi';
    }

    // Validasi Email
    if (!isset($input['email']) || trim((string)$input['email']) === '') {
        $errors['email'] = 'Email wajib diisi';
    } elseif (!filter_var((string)$input['email'], FILTER_VALIDATE_EMAIL)) {
        $errors['email'] = 'Format email tidak valid';
    } elseif ($model->isEmailExists(trim((string)$input['email']), $excludeId)) {
        $errors['email'] = 'Email sudah terdaftar';
    }

    return $errors;
}
