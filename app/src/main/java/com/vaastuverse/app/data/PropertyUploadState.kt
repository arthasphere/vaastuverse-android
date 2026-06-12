package com.vaastuverse.app.data

sealed class PropertyUploadState {
    data object Idle : PropertyUploadState()
    data class Uploading(val progressPercent: Int) : PropertyUploadState()
    data class Completed(val remoteUrl: String) : PropertyUploadState()
    data class Failed(val message: String, val retryUri: String?) : PropertyUploadState()
}

fun Map<String, PropertyUploadState>.hasBlockingUpload(): Boolean =
    values.any { it is PropertyUploadState.Uploading }

fun Map<String, PropertyUploadState>.hasActiveUpload(): Boolean =
    values.any { it is PropertyUploadState.Uploading }
