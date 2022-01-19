package com.app.simpleplayer.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun <T : R, R> Flow<T>.collectAsStateDelayed(
    initial: T,
    timeMillis: Long
): State<R> = produceState(initial, this, EmptyCoroutineContext) {
    collect {
        delay(timeMillis)
        value = it
    }
}
