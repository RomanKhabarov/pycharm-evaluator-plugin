package com.evaluator.plugin.action

import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.*;
import kotlinx.coroutines.*
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> initiateExecution(
    loadData: () -> T,
    updateUiWithData: (T) -> Unit,
    submitExecutions: (T) -> Unit,
    onCompleteCallback: () -> Unit
) {
    val data = withContext(Dispatchers.IO) {
        coroutineContext.ensureActive()
        loadData()
    }

    withContext(Dispatchers.Main) {
        coroutineContext.ensureActive()
        updateUiWithData(data)
    }

    withContext(Dispatchers.Default) {
        coroutineContext.ensureActive()
        submitExecutions(data)
        onCompleteCallback()
    }
}

suspend fun cancelExecution(
    cancelExecution: () -> Unit,
    onCompleteCallback: () -> Unit
) {
    withContext(Dispatchers.Default) {
        coroutineContext.ensureActive()
        cancelExecution()
        onCompleteCallback()
    }
}

fun <T> initiateExecutionWithBackgroundProgress(
    project: Project,
    loadData: () -> T,
    updateUiWithData: (T) -> Unit,
    submitExecutions: (T) -> Unit,
    onCompleteCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            withBackgroundProgress(project, "Initiating execution...", true) {
                initiateExecution(loadData, updateUiWithData, submitExecutions, onCompleteCallback)
            }
        } catch (e: CancellationException) {
            onCompleteCallback()
        }
    }
}

fun cancelExecutionWithBackgroundProgress(
    project: Project,
    cancelExecution: () -> Unit,
    onCompleteCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            withBackgroundProgress(project, "Cancelling execution...", true) {
                cancelExecution(cancelExecution, onCompleteCallback)
            }
        } catch (e: CancellationException) {
            onCompleteCallback()
        }
    }
}
