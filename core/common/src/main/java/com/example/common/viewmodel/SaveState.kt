package com.example.common.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared save-lifecycle state for add/edit ViewModels: tracks whether a save is currently in
 * progress and the error message from the last failed attempt, and guards [launch] so a save
 * already in flight makes concurrent calls no-ops.
 */
class SaveState {
    @PublishedApi
    internal val _isSaving = MutableStateFlow(false)
    /** Whether a save is currently in progress. */
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    @PublishedApi
    internal val _error = MutableStateFlow<String?>(null)
    /** Error message from the last failed save, or `null` if the last save succeeded. */
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Clears the current [error], e.g. after it has been shown to the user. */
    fun clearError() {
        _error.value = null
    }

    /**
     * Launches [block] in [scope], guarded so a save already in progress is a no-op. Clears
     * [error] before running and resets [isSaving] once [block] and [always] complete. Any [E]
     * thrown by [block] is caught and forwarded to [onError] instead of propagating; other
     * exceptions propagate as usual. Callers decide within [block] whether follow-up work (e.g.
     * an `afterSave` callback) should only run on success, or pass [always] to run it
     * unconditionally once saving finishes.
     */
    inline fun <reified E : Throwable> launch(
        scope: CoroutineScope,
        noinline onError: (E) -> Unit = { _error.value = it.message },
        noinline always: () -> Unit = {},
        crossinline block: suspend () -> Unit
    ) {
        if (_isSaving.value) return
        _isSaving.value = true
        scope.launch {
            _error.value = null
            try {
                block()
            } catch (e: Throwable) {
                if (e is E) onError(e) else throw e
            } finally {
                _isSaving.value = false
                always()
            }
        }
    }
}
