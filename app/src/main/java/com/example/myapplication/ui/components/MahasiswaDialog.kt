package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Mahasiswa

@Composable
fun MahasiswaDialog(
    mahasiswa: Mahasiswa?,
    isSaving: Boolean,
    serverErrorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (nim: String, nama: String, jurusan: String, email: String, alamat: String?) -> Unit
) {
    val isEdit = mahasiswa != null

    var nim by remember { mutableStateOf(mahasiswa?.nim ?: "") }
    var nama by remember { mutableStateOf(mahasiswa?.nama ?: "") }
    var jurusan by remember { mutableStateOf(mahasiswa?.jurusan ?: "") }
    var email by remember { mutableStateOf(mahasiswa?.email ?: "") }
    var alamat by remember { mutableStateOf(mahasiswa?.alamat ?: "") }

    var nimError by remember { mutableStateOf<String?>(null) }
    var namaError by remember { mutableStateOf<String?>(null) }
    var jurusanError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true

        if (nim.isBlank()) {
            nimError = "NIM wajib diisi"
            isValid = false
        } else {
            nimError = null
        }

        if (nama.isBlank()) {
            namaError = "Nama lengkap wajib diisi"
            isValid = false
        } else {
            namaError = null
        }

        if (jurusan.isBlank()) {
            jurusanError = "Jurusan wajib diisi"
            isValid = false
        } else {
            jurusanError = null
        }

        if (email.isBlank()) {
            emailError = "Email wajib diisi"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Format email tidak valid"
            isValid = false
        } else {
            emailError = null
        }

        return isValid
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = if (isEdit) "Edit Data Mahasiswa" else "Tambah Mahasiswa Baru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!serverErrorMessage.isNullOrBlank()) {
                    Text(
                        text = serverErrorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = nim,
                    onValueChange = {
                        nim = it
                        if (nimError != null) nimError = null
                    },
                    label = { Text("NIM") },
                    placeholder = { Text("Contoh: 2024001") },
                    isError = nimError != null,
                    supportingText = nimError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = nama,
                    onValueChange = {
                        nama = it
                        if (namaError != null) namaError = null
                    },
                    label = { Text("Nama Lengkap") },
                    placeholder = { Text("Nama lengkap mahasiswa") },
                    isError = namaError != null,
                    supportingText = namaError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = jurusan,
                    onValueChange = {
                        jurusan = it
                        if (jurusanError != null) jurusanError = null
                    },
                    label = { Text("Jurusan / Program Studi") },
                    placeholder = { Text("Contoh: Teknik Informatika") },
                    isError = jurusanError != null,
                    supportingText = jurusanError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email Kampus") },
                    placeholder = { Text("nama@kampus.ac.id") },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = { Text("Alamat (Opsional)") },
                    placeholder = { Text("Alamat tempat tinggal") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isSaving
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        onSave(nim, nama, jurusan, email, alamat)
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEdit) "Simpan Perubahan" else "Tambah Mahasiswa")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSaving,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
