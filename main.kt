// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: VideoViewModel by viewModels()
    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { viewModel.processFolder(this@MainActivity, it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoDurationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppUI(viewModel)
                }
            }
        }
    }

    @Composable
    fun AppUI(viewModel: VideoViewModel) {
        val state by viewModel.uiState.collectAsState()
        
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { folderPicker.launch(null) },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Choose Folder")
            }

            when (state) {
                is VideoUiState.Loading -> CircularProgressIndicator()
                is VideoUiState.Success -> {
                    val result = (state as VideoUiState.Success).totalDuration
                    Text(
                        text = "Total duration: ${result.formatAsTime()}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                is VideoUiState.Error -> {
                    Text(
                        text = "Error: ${(state as VideoUiState.Error).message}",
                        color = Color.Red
                    )
                }
                else -> {}
            }
        }
    }
}

// VideoViewModel.kt
@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoProcessor: VideoProcessor
) : ViewModel() {
    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Idle)
    val uiState: StateFlow<VideoUiState> = _uiState

    fun processFolder(context: Context, uri: Uri) {
        _uiState.value = VideoUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val duration = videoProcessor.calculateTotalDuration(context, uri)
                _uiState.value = VideoUiState.Success(duration)
            } catch (e: Exception) {
                _uiState.value = VideoUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

// VideoProcessor.kt
class VideoProcessor @Inject constructor() {
    private val supportedFormats = setOf("mp4", "mkv", "avi", "mov")

    fun calculateTotalDuration(context: Context, rootUri: Uri): Long {
        var totalDuration = 0L
        val retriever = MediaMetadataRetriever()
        
        DocumentFile.fromTreeUri(context, rootUri)?.let { root ->
            root.listFiles().forEach { file ->
                if (file.isDirectory) {
                    totalDuration += calculateTotalDuration(context, file.uri)
                } else if (file.type?.startsWith("video/") == true || 
                          file.uri.path?.substringAfterLast(".") in supportedFormats) {
                    runCatching {
                        retriever.setDataSource(context, file.uri)
                        val duration = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_DURATION
                        )?.toLongOrNull() ?: 0
                        totalDuration += duration
                    }.onFailure { 
                        // Handle corrupted files
                    }
                }
            }
        }
        retriever.release()
        return totalDuration
    }
}

// Utils.kt
fun Long.formatAsTime(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
