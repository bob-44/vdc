Key Features & Security Considerations:

Modern Architecture:

Uses Hilt for dependency injection

MVVM pattern with StateFlow for UI state management

Coroutines for background processing

Secure File Access:

Uses Storage Access Framework (SAF) for folder selection

No need for READ_EXTERNAL_STORAGE permission

Processes files through ContentResolver URIs

Compatibility:

Works on all Android versions (4.4+)

Handles scoped storage properly

Uses DocumentFile API for tree traversal

Performance:

Uses built-in MediaMetadataRetriever instead of FFmpeg

Processes files recursively in background thread

Handles large folders efficiently

Error Handling:

Proper error states in UI

RunCatching for file processing errors

Memory management with MediaMetadataRetriever

Required Dependencies (build.gradle):

text
implementation "androidx.activity:activity-compose:1.8.0"
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2"
implementation "com.google.dagger:hilt-android:2.48"
implementation "androidx.documentfile:documentfile:1.0.1"