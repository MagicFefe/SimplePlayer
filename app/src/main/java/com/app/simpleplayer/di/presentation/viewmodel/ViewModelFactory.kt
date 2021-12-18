package com.app.simpleplayer.di.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModelProvider = creators[modelClass]

        @Suppress("UNCHECKED_CAST")
        return viewModelProvider?.get() as T
            ?: throw IllegalArgumentException("Not found activity $modelClass")
    }
}