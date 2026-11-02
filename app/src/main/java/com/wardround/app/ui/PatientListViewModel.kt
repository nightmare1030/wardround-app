package com.wardround.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wardround.app.data.AppRepository
import com.wardround.app.data.Patient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PatientListViewModel(private val repository: AppRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val patients = _searchQuery
        .flatMapLatest { repository.searchPatients(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch { repository.deletePatient(patient) }
    }

    fun setSurgeryDate(patient: Patient, dateMillis: Long) {
        viewModelScope.launch {
            repository.updatePatient(patient.copy(surgeryDate = dateMillis))
        }
    }

    fun setSurgeryInfo(patient: Patient, surgeryDate: Long, startTime: Long, endTime: Long) {
        viewModelScope.launch {
            repository.updatePatient(
                patient.copy(
                    surgeryDate = surgeryDate,
                    surgeryStartTime = startTime,
                    surgeryEndTime = endTime
                )
            )
        }
    }

    fun markDischarged(patient: Patient) {
        viewModelScope.launch {
            repository.markDischarged(patient)
        }
    }

    fun markUndischarged(patient: Patient) {
        viewModelScope.launch {
            repository.markUndischarged(patient)
        }
    }

    companion object {
        fun factory(repository: AppRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PatientListViewModel(repository) as T
            }
    }
}