package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Mahasiswa
import com.example.myapplication.ui.components.DeleteConfirmDialog
import com.example.myapplication.ui.components.MahasiswaCard
import com.example.myapplication.ui.components.MahasiswaDialog
import com.example.myapplication.ui.components.ServerConfigDialog
import com.example.myapplication.ui.viewmodel.MahasiswaUiState
import com.example.myapplication.ui.viewmodel.MahasiswaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahasiswaScreen(
    viewModel: MahasiswaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDepartment by viewModel.selectedDepartment.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val deletingMahasiswa by viewModel.deletingMahasiswa.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val isServerConfigOpen by viewModel.isServerConfigOpen.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Data Mahasiswa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sistem Akademik Kampus",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari Mahasiswa"
                        )
                    }
                    IconButton(onClick = { viewModel.loadData(showLoadingIndicator = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang"
                        )
                    }
                    IconButton(onClick = { viewModel.openServerConfig() }) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Pengaturan Server"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Mahasiswa") },
                shape = RoundedCornerShape(14.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar (Animasi buka-tutup)
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Cari nama, NIM, jurusan...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Body Content berdasarkan State
            when (val state = uiState) {
                is MahasiswaUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Menghubungi server backend...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is MahasiswaUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Gagal Memuat Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.openServerConfig() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ubah URL Server")
                                }
                                Button(
                                    onClick = { viewModel.loadData() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Coba Lagi")
                                }
                            }
                        }
                    }
                }

                is MahasiswaUiState.Success -> {
                    // Filter Chips Jurusan
                    if (state.allDepartments.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedDepartment == null,
                                onClick = { viewModel.onDepartmentSelected(null) },
                                label = { Text("Semua Jurusan") },
                                shape = RoundedCornerShape(8.dp)
                            )
                            state.allDepartments.forEach { dept ->
                                FilterChip(
                                    selected = selectedDepartment == dept,
                                    onClick = { viewModel.onDepartmentSelected(dept) },
                                    label = { Text(dept) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Count bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menampilkan ${state.list.size} mahasiswa",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (state.list.isEmpty()) {
                        // Empty State yang informatif dan jelas
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                val isFiltering = searchQuery.isNotBlank() || selectedDepartment != null
                                Text(
                                    text = if (isFiltering) "Pencarian Tidak Ditemukan" else "Belum Ada Data Mahasiswa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = if (isFiltering)
                                        "Tidak ada mahasiswa yang sesuai dengan filter atau kata kunci saat ini."
                                    else
                                        "Database mahasiswa masih kosong. Tambahkan mahasiswa baru sekarang.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                if (isFiltering) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.onSearchQueryChanged("")
                                            viewModel.onDepartmentSelected(null)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Reset Filter")
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.openAddDialog() },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Tambah Mahasiswa")
                                    }
                                }
                            }
                        }
                    } else {
                        // List Mahasiswa
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = state.list,
                                key = { it.id }
                            ) { item ->
                                MahasiswaCard(
                                    mahasiswa = item,
                                    onEdit = { viewModel.openEditDialog(item) },
                                    onDelete = { viewModel.promptDelete(item) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
                            }
                        }
                    }
                }
            }
        }

        // Dialog Add / Edit
        if (formState.isOpen) {
            MahasiswaDialog(
                mahasiswa = formState.mahasiswa,
                isSaving = formState.isSaving,
                serverErrorMessage = formState.errorMessage,
                onDismiss = { viewModel.closeFormDialog() },
                onSave = { nim, nama, jurusan, email, alamat ->
                    viewModel.saveMahasiswa(nim, nama, jurusan, email, alamat)
                }
            )
        }

        // Dialog Konfirmasi Hapus
        deletingMahasiswa?.let { target ->
            DeleteConfirmDialog(
                mahasiswa = target,
                isDeleting = isDeleting,
                onDismiss = { viewModel.dismissDelete() },
                onConfirm = { viewModel.confirmDelete() }
            )
        }

        // Dialog Pengaturan Endpoint Server
        if (isServerConfigOpen) {
            ServerConfigDialog(
                currentUrl = viewModel.apiClient.baseUrl,
                onDismiss = { viewModel.closeServerConfig() },
                onSave = { newUrl -> viewModel.updateServerUrl(newUrl) }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun MahasiswaScreenPreview() {
    com.example.myapplication.ui.theme.MyApplicationTheme {
        MahasiswaCard(
            mahasiswa = Mahasiswa(
                id = 1,
                nim = "2024001",
                nama = "Ahmad Pratama",
                jurusan = "Teknik Informatika",
                email = "ahmad.pratama@kampus.ac.id",
                alamat = "Jl. Merdeka No. 10, Jakarta"
            ),
            onEdit = {},
            onDelete = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

