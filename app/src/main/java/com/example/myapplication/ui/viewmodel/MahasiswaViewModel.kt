package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ApiResult
import com.example.myapplication.data.model.Mahasiswa
import com.example.myapplication.data.remote.MahasiswaApiClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MahasiswaUiState {
    data object Loading : MahasiswaUiState
    data class Success(
        val list: List<Mahasiswa>,
        val allDepartments: List<String>,
        val selectedDepartment: String? = null
    ) : MahasiswaUiState
    data class Error(val message: String) : MahasiswaUiState
}

data class FormDialogState(
    val isOpen: Boolean = false,
    val mahasiswa: Mahasiswa? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class MahasiswaViewModel(
    val apiClient: MahasiswaApiClient = MahasiswaApiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MahasiswaUiState>(MahasiswaUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedDepartment = MutableStateFlow<String?>(null)
    val selectedDepartment = _selectedDepartment.asStateFlow()

    private val _formState = MutableStateFlow(FormDialogState())
    val formState = _formState.asStateFlow()

    private val _deletingMahasiswa = MutableStateFlow<Mahasiswa?>(null)
    val deletingMahasiswa = _deletingMahasiswa.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    private val _isServerConfigOpen = MutableStateFlow(false)
    val isServerConfigOpen = _isServerConfigOpen.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    // Cache of full list from API
    private var cachedList: List<Mahasiswa> = emptyList()

    init {
        loadData()
    }

    fun loadData(showLoadingIndicator: Boolean = true) {
        viewModelScope.launch {
            if (showLoadingIndicator) {
                _uiState.value = MahasiswaUiState.Loading
            }

            when (val result = apiClient.getAll(search = _searchQuery.value.takeIf { it.isNotBlank() })) {
                is ApiResult.Success -> {
                    cachedList = result.data
                    updateFilteredList()
                }
                is ApiResult.Error -> {
                    _uiState.value = MahasiswaUiState.Error(result.message)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // Local filtering if we already have the cache, or fetch with query
        updateFilteredList()
    }

    fun onDepartmentSelected(jurusan: String?) {
        _selectedDepartment.value = if (_selectedDepartment.value == jurusan) null else jurusan
        updateFilteredList()
    }

    private fun updateFilteredList() {
        val query = _searchQuery.value.trim().lowercase()
        val dept = _selectedDepartment.value

        val filtered = cachedList.filter { m ->
            val matchQuery = if (query.isEmpty()) true else {
                m.nama.lowercase().contains(query) ||
                m.nim.lowercase().contains(query) ||
                m.jurusan.lowercase().contains(query) ||
                (m.alamat?.lowercase()?.contains(query) == true)
            }
            val matchDept = if (dept == null) true else m.jurusan.equals(dept, ignoreCase = true)
            matchQuery && matchDept
        }

        val departments = cachedList.map { it.jurusan }.filter { it.isNotBlank() }.distinct().sorted()

        _uiState.value = MahasiswaUiState.Success(
            list = filtered,
            allDepartments = departments,
            selectedDepartment = dept
        )
    }

    // Form CRUD
    fun openAddDialog() {
        _formState.value = FormDialogState(isOpen = true, mahasiswa = null)
    }

    fun openEditDialog(mahasiswa: Mahasiswa) {
        _formState.value = FormDialogState(isOpen = true, mahasiswa = mahasiswa)
    }

    fun closeFormDialog() {
        _formState.value = FormDialogState(isOpen = false)
    }

    fun saveMahasiswa(nim: String, nama: String, jurusan: String, email: String, alamat: String?) {
        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, errorMessage = null) }

            val current = _formState.value.mahasiswa
            val target = Mahasiswa(
                id = current?.id ?: 0,
                nim = nim.trim(),
                nama = nama.trim(),
                jurusan = jurusan.trim(),
                email = email.trim(),
                alamat = alamat?.trim()?.takeIf { it.isNotBlank() }
            )

            val result = if (current == null) {
                apiClient.create(target)
            } else {
                apiClient.update(target)
            }

            when (result) {
                is ApiResult.Success -> {
                    _formState.value = FormDialogState(isOpen = false)
                    _userMessage.emit(
                        if (current == null) "Mahasiswa berhasil ditambahkan" else "Data mahasiswa berhasil diperbarui"
                    )
                    loadData(showLoadingIndicator = false)
                }
                is ApiResult.Error -> {
                    _formState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    // Delete
    fun promptDelete(mahasiswa: Mahasiswa) {
        _deletingMahasiswa.value = mahasiswa
    }

    fun dismissDelete() {
        _deletingMahasiswa.value = null
    }

    fun confirmDelete() {
        val target = _deletingMahasiswa.value ?: return
        viewModelScope.launch {
            _isDeleting.value = true
            when (val result = apiClient.delete(target.id)) {
                is ApiResult.Success -> {
                    _deletingMahasiswa.value = null
                    _isDeleting.value = false
                    _userMessage.emit("Data ${target.nama} berhasil dihapus")
                    loadData(showLoadingIndicator = false)
                }
                is ApiResult.Error -> {
                    _isDeleting.value = false
                    _userMessage.emit("Gagal menghapus: ${result.message}")
                }
            }
        }
    }

    // Server Config
    fun openServerConfig() {
        _isServerConfigOpen.value = true
    }

    fun closeServerConfig() {
        _isServerConfigOpen.value = false
    }

    fun updateServerUrl(newUrl: String) {
        apiClient.baseUrl = newUrl.trim()
        _isServerConfigOpen.value = false
        loadData(showLoadingIndicator = true)
    }
}
