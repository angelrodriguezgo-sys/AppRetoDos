package com.appretodos.ui.historial



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appretodos.data.AppContainer
import com.appretodos.data.model.Transaccion
import com.appretodos.data.repository.BilleteraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistorialViewModel(
    repo: BilleteraRepository = AppContainer.billetera
) : ViewModel() {

    val transacciones: StateFlow<List<Transaccion>> = repo.transacciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}