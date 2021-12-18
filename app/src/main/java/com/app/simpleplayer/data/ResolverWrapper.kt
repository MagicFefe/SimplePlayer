package com.app.simpleplayer.data

import android.content.ContentResolver
import android.content.Context
import kotlinx.coroutines.flow.Flow

abstract class ResolverWrapper<out T>(context: Context): ContentResolver(context) {
    abstract fun getExternalContent(): Flow<List<T>>
}