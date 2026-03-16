package com.lentoo.fitnessapp

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Shapes
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.noties.markwon.Markwon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path as RetrofitPath
import retrofit2.http.Query
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ExerciseLibraryItem(
    val id: Int,
    val name: String
)

data class WorkoutSetUi(
    val reps: String = "",
    val weight: String = "",
    val duration: String = "",
    val speed: String = "",
    val incline: String = "",
    val distance: String = "",
    val resistance: String = "",
    val cadence: String = "",
    val completed: Boolean = false
)

data class WorkoutExerciseUi(
    val name: String = "",
    val category: String = "",
    val sets: List<WorkoutSetUi> = listOf()
)

data class FitnessLogUi(
    val date: String,
    val weight: String = "",
    val targetMuscle: String = "",
    val targetMuscles: List<String> = emptyList(),
    val workoutPlanName: String = "",
    val workoutDetails: List<WorkoutExerciseUi> = emptyList(),
    val dailySuggestion: String = "",
    val completed: Boolean = false,
    val notes: String = ""
)

data class PlanSectionUi(
    val category: String,
    val exercises: List<ExerciseLibraryItem>
)

data class FitnessUiState(
    val baseUrl: String = DEFAULT_BASE_URL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val currentDate: LocalDate = LocalDate.now(),
    val currentLog: FitnessLogUi = createEmptyLog(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)),
    val logs: Map<String, FitnessLogUi> = emptyMap(),
    val exerciseLibrary: Map<String, List<ExerciseLibraryItem>> = emptyMap(),
    val selectedMuscleGroup: String = MUSCLE_GROUPS.first(),
    val showExerciseLibrary: Boolean = false,
    val isEditMode: Boolean = false,
    val editingExercise: ExerciseLibraryItem? = null,
    val showAddExerciseForm: Boolean = false,
    val newExerciseCategory: String = MUSCLE_GROUPS.first(),
    val newExerciseName: String = "",
    val analysisResult: String = "",
    val dailyAnalysisResult: String = "",
    val isAnalyzing: Boolean = false,
    val isAnalyzingDaily: Boolean = false,
    val errorMessage: String? = null
)

private data class FitnessLogRequest(
    @SerializedName("date") val date: String,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("target_muscle") val targetMuscle: String,
    @SerializedName("target_muscles") val targetMuscles: List<String>,
    @SerializedName("workout_plan_name") val workoutPlanName: String,
    @SerializedName("workout_details") val workoutDetails: List<WorkoutExerciseRequest>,
    @SerializedName("daily_suggestion") val dailySuggestion: String?,
    @SerializedName("completed") val completed: Boolean,
    @SerializedName("notes") val notes: String
)

private data class WorkoutExerciseRequest(
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("sets") val sets: List<WorkoutSetRequest>
)

private data class WorkoutSetRequest(
    @SerializedName("reps") val reps: String,
    @SerializedName("weight") val weight: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("speed") val speed: String?,
    @SerializedName("incline") val incline: String?,
    @SerializedName("distance") val distance: String?,
    @SerializedName("resistance") val resistance: String?,
    @SerializedName("cadence") val cadence: String?,
    @SerializedName("completed") val completed: Boolean
)

private data class ExerciseLibraryItemDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null
)

private data class WorkoutSetDto(
    @SerializedName("reps") val reps: String? = null,
    @SerializedName("weight") val weight: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("speed") val speed: String? = null,
    @SerializedName("incline") val incline: String? = null,
    @SerializedName("distance") val distance: String? = null,
    @SerializedName("resistance") val resistance: String? = null,
    @SerializedName("cadence") val cadence: String? = null,
    @SerializedName("completed") val completed: Boolean? = null
)

private data class WorkoutExerciseDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("sets") val sets: List<WorkoutSetDto>? = null
)

private data class FitnessLogDto(
    @SerializedName("date") val date: String? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("target_muscle") val targetMuscle: String? = null,
    @SerializedName("target_muscles") val targetMuscles: List<String>? = null,
    @SerializedName("workout_plan_name") val workoutPlanName: String? = null,
    @SerializedName("workout_details") val workoutDetails: List<WorkoutExerciseDto>? = null,
    @SerializedName("daily_suggestion") val dailySuggestion: String? = null,
    @SerializedName("completed") val completed: Boolean? = null,
    @SerializedName("notes") val notes: String? = null
)

private data class ExerciseLibraryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("exercises") val exercises: Map<String, List<ExerciseLibraryItemDto>>? = null,
    @SerializedName("error") val error: String? = null
)

private data class LogsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("logs") val logs: List<FitnessLogDto>? = null,
    @SerializedName("error") val error: String? = null
)

private data class SaveLogResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("log") val log: FitnessLogDto? = null,
    @SerializedName("error") val error: String? = null
)

private data class AnalyzeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("analysis") val analysis: String? = null,
    @SerializedName("error") val error: String? = null
)

private data class ExerciseMutationRequest(
    @SerializedName("category") val category: String,
    @SerializedName("name") val name: String
)

private data class ExerciseMutationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("exercise") val exercise: ExerciseLibraryItemDto? = null,
    @SerializedName("error") val error: String? = null
)

private data class SimpleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String? = null
)

private data class AnalyzeDailyRequest(
    @SerializedName("workout") val workout: FitnessLogRequest
)

private interface FitnessApiService {
    @GET("api/fitness/exercises")
    suspend fun getExerciseLibrary(): ExerciseLibraryResponse

    @GET("api/fitness/logs")
    suspend fun getLogs(
        @Query("start") start: String? = null,
        @Query("end") end: String? = null
    ): LogsResponse

    @POST("api/fitness/logs")
    suspend fun saveLog(@Body log: FitnessLogRequest): SaveLogResponse

    @POST("api/fitness/analyze")
    suspend fun analyzeFitness(): AnalyzeResponse

    @POST("api/fitness/analyze-daily")
    suspend fun analyzeDaily(@Body request: AnalyzeDailyRequest): AnalyzeResponse

    @POST("api/fitness/exercises")
    suspend fun addExercise(@Body request: ExerciseMutationRequest): ExerciseMutationResponse

    @PUT("api/fitness/exercises/{id}")
    suspend fun updateExercise(
        @RetrofitPath("id") id: Int,
        @Body request: ExerciseMutationRequest
    ): ExerciseMutationResponse

    @DELETE("api/fitness/exercises/{id}")
    suspend fun deleteExercise(@RetrofitPath("id") id: Int): SimpleResponse
}

private class FitnessRepository(baseUrl: String) {
    private var apiService: FitnessApiService = createApi(baseUrl)

    fun updateBaseUrl(baseUrl: String) {
        apiService = createApi(baseUrl)
    }

    suspend fun getExerciseLibrary(): Map<String, List<ExerciseLibraryItem>> {
        val response = apiService.getExerciseLibrary()
        if (!response.success) {
            throw IllegalStateException(response.error ?: "加载动作库失败")
        }
        return response.exercises.orEmpty().mapValues { (_, items) ->
            items.mapNotNull { item ->
                val id = item.id ?: return@mapNotNull null
                val name = item.name ?: return@mapNotNull null
                ExerciseLibraryItem(id = id, name = name)
            }
        }
    }

    suspend fun getLogs(): List<FitnessLogUi> {
        val response = apiService.getLogs()
        if (!response.success) {
            throw IllegalStateException(response.error ?: "加载训练记录失败")
        }
        return response.logs.orEmpty().map { normalizeLog(it, emptyMap()) }
    }

    suspend fun saveLog(log: FitnessLogUi): FitnessLogUi {
        val response = apiService.saveLog(log.toRequest())
        if (!response.success) {
            throw IllegalStateException(response.error ?: "保存失败")
        }
        return normalizeLog(response.log ?: FitnessLogDto(date = log.date), emptyMap())
    }

    suspend fun analyzeFitness(): String {
        val response = apiService.analyzeFitness()
        if (!response.success) {
            throw IllegalStateException(response.error ?: "分析失败")
        }
        return response.analysis.orEmpty()
    }

    suspend fun analyzeDaily(log: FitnessLogUi): String {
        val response = apiService.analyzeDaily(AnalyzeDailyRequest(log.toRequest()))
        if (!response.success) {
            throw IllegalStateException(response.error ?: "生成建议失败")
        }
        return response.analysis.orEmpty()
    }

    suspend fun addExercise(category: String, name: String): ExerciseLibraryItem {
        val response = apiService.addExercise(ExerciseMutationRequest(category, name))
        if (!response.success) {
            throw IllegalStateException(response.error ?: "添加动作失败")
        }
        return ExerciseLibraryItem(
            id = response.exercise?.id ?: 0,
            name = response.exercise?.name.orEmpty()
        )
    }

    suspend fun updateExercise(id: Int, category: String, name: String): ExerciseLibraryItem {
        val response = apiService.updateExercise(id, ExerciseMutationRequest(category, name))
        if (!response.success) {
            throw IllegalStateException(response.error ?: "更新动作失败")
        }
        return ExerciseLibraryItem(
            id = response.exercise?.id ?: id,
            name = response.exercise?.name.orEmpty()
        )
    }

    suspend fun deleteExercise(id: Int) {
        val response = apiService.deleteExercise(id)
        if (!response.success) {
            throw IllegalStateException(response.error ?: "删除动作失败")
        }
    }

    private fun createApi(baseUrl: String): FitnessApiService {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        val gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FitnessApiService::class.java)
    }
}

class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(
        FitnessUiState(baseUrl = getSavedBaseUrl())
    )
    val uiState: StateFlow<FitnessUiState> = _uiState.asStateFlow()

    private val repository = FitnessRepository(_uiState.value.baseUrl)
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()
    private var saveJob: Job? = null

    init {
        refreshAll(initial = true)
    }

    fun refreshAll(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial,
                    isRefreshing = !initial,
                    errorMessage = null
                )
            }
            runCatching {
                val library = repository.getExerciseLibrary()
                val logs = repository.getLogs().map { normalizeLog(it, library) }
                val logsMap = logs.associateBy { it.date }
                val selectedCategory = if (library.containsKey(_uiState.value.selectedMuscleGroup)) {
                    _uiState.value.selectedMuscleGroup
                } else {
                    library.keys.firstOrNull() ?: MUSCLE_GROUPS.first()
                }
                val currentDate = _uiState.value.currentDate
                val currentDateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val currentLog = normalizeLog(logsMap[currentDateStr] ?: createEmptyLog(currentDateStr), library)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        exerciseLibrary = library,
                        logs = logsMap,
                        selectedMuscleGroup = selectedCategory,
                        currentLog = currentLog,
                        dailyAnalysisResult = currentLog.dailySuggestion,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "初始化失败"
                    )
                }
                emitMessage(throwable.message ?: "初始化失败")
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { current ->
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val log = normalizeLog(current.logs[dateString] ?: createEmptyLog(dateString), current.exerciseLibrary)
            current.copy(
                currentDate = date,
                currentLog = log,
                dailyAnalysisResult = log.dailySuggestion
            )
        }
    }

    fun updateBaseUrl(baseUrl: String) {
        val normalized = baseUrl.trim().trimEnd('/').ifBlank { DEFAULT_BASE_URL }
        preferences.edit().putString(PREF_BASE_URL, normalized).apply()
        repository.updateBaseUrl(normalized)
        _uiState.update {
            it.copy(baseUrl = normalized)
        }
        refreshAll(initial = true)
    }

    fun updateWeight(value: String) {
        mutateCurrentLog { it.copy(weight = value) }
    }

    fun updateNotes(value: String) {
        mutateCurrentLog { it.copy(notes = value) }
    }

    fun toggleCompleted() {
        mutateCurrentLog { it.copy(completed = !it.completed) }
    }

    fun toggleMuscleSelection(muscle: String) {
        mutateCurrentLog { log ->
            val next = log.targetMuscles.toMutableSet()
            if (next.contains(muscle)) next.remove(muscle) else next.add(muscle)
            val ordered = MUSCLE_GROUPS.filter { next.contains(it) }
            log.copy(
                targetMuscles = ordered,
                targetMuscle = ordered.joinToString("、"),
                workoutPlanName = if (ordered.isEmpty()) "" else "${ordered.joinToString(" / ")} 训练"
            )
        }
    }

    fun addExercise() {
        val category = _uiState.value.currentLog.targetMuscles.firstOrNull() ?: _uiState.value.selectedMuscleGroup
        mutateCurrentLog { log ->
            log.copy(workoutDetails = log.workoutDetails + createExercise(category = category))
        }
    }

    fun removeExercise(exerciseIndex: Int) {
        mutateCurrentLog { log ->
            log.copy(workoutDetails = log.workoutDetails.filterIndexed { index, _ -> index != exerciseIndex })
        }
    }

    fun updateExerciseName(exerciseIndex: Int, value: String) {
        mutateCurrentLog { log ->
            log.copy(
                workoutDetails = log.workoutDetails.mapIndexed { index, exercise ->
                    if (index != exerciseIndex) exercise
                    else exercise.copy(name = value)
                }
            )
        }
    }

    fun syncExerciseCategory(exerciseIndex: Int) {
        mutateCurrentLog { log ->
            log.copy(
                workoutDetails = log.workoutDetails.mapIndexed { index, exercise ->
                    if (index != exerciseIndex) exercise
                    else {
                        val inferred = inferCategoryByName(exercise.name, _uiState.value.exerciseLibrary)
                        exercise.copy(category = inferred.ifBlank { exercise.category })
                    }
                }
            )
        }
    }

    fun updateExerciseCategory(exerciseIndex: Int, category: String) {
        mutateCurrentLog { log ->
            log.copy(
                workoutDetails = log.workoutDetails.mapIndexed { index, exercise ->
                    if (index != exerciseIndex) exercise else exercise.copy(category = category)
                }
            )
        }
    }

    fun addSet(exerciseIndex: Int) {
        mutateCurrentLog { log ->
            log.copy(
                workoutDetails = log.workoutDetails.mapIndexed { index, exercise ->
                    if (index != exerciseIndex) exercise
                    else exercise.copy(sets = exercise.sets + createDefaultSet(exercise.category, exercise.name))
                }
            )
        }
    }

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        mutateCurrentLog { log ->
            log.copy(
                workoutDetails = log.workoutDetails.mapIndexed { index, exercise ->
                    if (index != exerciseIndex) exercise else {
                        val nextSets = exercise.sets.filterIndexed { currentIndex, _ -> currentIndex != setIndex }
                        exercise.copy(
                            sets = if (nextSets.isEmpty()) listOf(createDefaultSet(exercise.category, exercise.name)) else nextSets
                        )
                    }
                }
            )
        }
    }

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        mutateCurrentLog { log ->
            log.copy(
                workoutDetails = log.workoutDetails.mapIndexed { currentExerciseIndex, exercise ->
                    if (currentExerciseIndex != exerciseIndex) exercise
                    else exercise.copy(
                        sets = exercise.sets.mapIndexed { currentSetIndex, set ->
                            if (currentSetIndex != setIndex) set else set.copy(completed = !set.completed)
                        }
                    )
                }
            )
        }
    }

    fun updateSetField(exerciseIndex: Int, setIndex: Int, field: String, value: String) {
        mutateCurrentLog { log ->
            log.copy(
                workoutDetails = log.workoutDetails.mapIndexed { currentExerciseIndex, exercise ->
                    if (currentExerciseIndex != exerciseIndex) exercise else exercise.copy(
                        sets = exercise.sets.mapIndexed { currentSetIndex, set ->
                            if (currentSetIndex != setIndex) set else set.updateField(field, value)
                        }
                    )
                }
            )
        }
    }

    fun addExerciseFromLibrary(name: String, category: String) {
        val current = _uiState.value.currentLog
        if (current.workoutDetails.any { it.name == name && it.category == category }) {
            _uiState.update { it.copy(showExerciseLibrary = false) }
            return
        }
        mutateCurrentLog(updateDialogState = false) { log ->
            log.copy(workoutDetails = log.workoutDetails + createExercise(name = name, category = category))
        }
        _uiState.update { it.copy(showExerciseLibrary = false) }
    }

    fun addExercisesByCategory(category: String) {
        val currentLog = _uiState.value.currentLog
        val items = _uiState.value.exerciseLibrary[category].orEmpty()
        var added = 0
        mutateCurrentLog { log ->
            val nextDetails = log.workoutDetails.toMutableList()
            items.forEach { exercise ->
                if (currentLog.workoutDetails.none { it.name == exercise.name && it.category == category }) {
                    nextDetails += createExercise(exercise.name, category)
                    added += 1
                }
            }
            log.copy(workoutDetails = nextDetails)
        }
        if (added > 0) emitMessage("已加入 ${added} 个${category}动作")
    }

    fun addRecommendedExercises() {
        val recommended = recommendedExercises(_uiState.value.currentLog.targetMuscles, _uiState.value.exerciseLibrary)
        var added = 0
        mutateCurrentLog { log ->
            val nextDetails = log.workoutDetails.toMutableList()
            recommended.forEach { exercise ->
                if (nextDetails.none { it.name == exercise.name && it.category == exercise.category }) {
                    nextDetails += createExercise(exercise.name, exercise.category)
                    added += 1
                }
            }
            log.copy(workoutDetails = nextDetails)
        }
        if (added > 0) emitMessage("已加入 ${added} 个推荐动作")
    }

    fun openExerciseLibrary() {
        _uiState.update { it.copy(showExerciseLibrary = true) }
    }

    fun closeExerciseLibrary() {
        _uiState.update {
            it.copy(
                showExerciseLibrary = false,
                isEditMode = false,
                editingExercise = null,
                showAddExerciseForm = false
            )
        }
    }

    fun selectMuscleGroup(group: String) {
        _uiState.update { it.copy(selectedMuscleGroup = group) }
    }

    fun toggleEditMode() {
        _uiState.update {
            it.copy(
                isEditMode = !it.isEditMode,
                editingExercise = null,
                showAddExerciseForm = false
            )
        }
    }

    fun startEditExercise(item: ExerciseLibraryItem) {
        _uiState.update { it.copy(editingExercise = item) }
    }

    fun cancelEditExercise() {
        _uiState.update { it.copy(editingExercise = null) }
    }

    fun updateEditingExerciseName(name: String) {
        _uiState.update { current ->
            current.copy(editingExercise = current.editingExercise?.copy(name = name))
        }
    }

    fun showAddExerciseForm() {
        _uiState.update {
            it.copy(
                showAddExerciseForm = true,
                newExerciseCategory = it.selectedMuscleGroup,
                newExerciseName = ""
            )
        }
    }

    fun cancelAddExerciseForm() {
        _uiState.update { it.copy(showAddExerciseForm = false, newExerciseName = "") }
    }

    fun updateNewExerciseName(name: String) {
        _uiState.update { it.copy(newExerciseName = name) }
    }

    fun updateNewExerciseCategory(category: String) {
        _uiState.update { it.copy(newExerciseCategory = category) }
    }

    fun saveEditedExercise() {
        val editing = _uiState.value.editingExercise ?: return
        if (editing.name.isBlank()) {
            emitMessage("请输入动作名称")
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.updateExercise(editing.id, _uiState.value.selectedMuscleGroup, editing.name.trim())
                repository.getExerciseLibrary()
            }.onSuccess { library ->
                val normalized = normalizeAllLogs(_uiState.value.logs, library)
                val currentDate = _uiState.value.currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val currentLog = normalizeLog(normalized[currentDate] ?: createEmptyLog(currentDate), library)
                _uiState.update {
                    it.copy(
                        exerciseLibrary = library,
                        logs = normalized,
                        currentLog = currentLog,
                        editingExercise = null
                    )
                }
                emitMessage("动作已更新")
            }.onFailure { throwable ->
                emitMessage(throwable.message ?: "更新失败")
            }
        }
    }

    fun saveNewExercise() {
        val state = _uiState.value
        if (state.newExerciseName.isBlank()) {
            emitMessage("请输入动作名称")
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.addExercise(state.newExerciseCategory, state.newExerciseName.trim())
                repository.getExerciseLibrary()
            }.onSuccess { library ->
                val normalized = normalizeAllLogs(_uiState.value.logs, library)
                val currentDate = _uiState.value.currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val currentLog = normalizeLog(normalized[currentDate] ?: createEmptyLog(currentDate), library)
                _uiState.update {
                    it.copy(
                        exerciseLibrary = library,
                        logs = normalized,
                        currentLog = currentLog,
                        selectedMuscleGroup = state.newExerciseCategory,
                        showAddExerciseForm = false,
                        newExerciseName = ""
                    )
                }
                emitMessage("动作已添加")
            }.onFailure { throwable ->
                emitMessage(throwable.message ?: "添加失败")
            }
        }
    }

    fun deleteExercise(item: ExerciseLibraryItem) {
        viewModelScope.launch {
            runCatching {
                repository.deleteExercise(item.id)
                repository.getExerciseLibrary()
            }.onSuccess { library ->
                val normalized = normalizeAllLogs(_uiState.value.logs, library)
                val currentDate = _uiState.value.currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val currentLog = normalizeLog(normalized[currentDate] ?: createEmptyLog(currentDate), library)
                _uiState.update {
                    it.copy(
                        exerciseLibrary = library,
                        logs = normalized,
                        currentLog = currentLog
                    )
                }
                emitMessage("动作已删除")
            }.onFailure { throwable ->
                emitMessage(throwable.message ?: "删除失败")
            }
        }
    }

    fun analyzeFitness() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            runCatching {
                repository.analyzeFitness()
            }.onSuccess { analysis ->
                _uiState.update { it.copy(analysisResult = analysis, isAnalyzing = false) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isAnalyzing = false) }
                emitMessage(throwable.message ?: "分析失败")
            }
        }
    }

    fun analyzeDaily() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingDaily = true) }
            val workout = normalizeLog(_uiState.value.currentLog, _uiState.value.exerciseLibrary)
            runCatching {
                repository.analyzeDaily(workout)
            }.onSuccess { analysis ->
                _uiState.update {
                    val nextLog = it.currentLog.copy(dailySuggestion = analysis)
                    it.copy(
                        currentLog = nextLog,
                        dailyAnalysisResult = analysis,
                        isAnalyzingDaily = false
                    )
                }
                scheduleSave()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isAnalyzingDaily = false) }
                emitMessage(throwable.message ?: "生成建议失败")
            }
        }
    }

    private fun mutateCurrentLog(updateDialogState: Boolean = true, transform: (FitnessLogUi) -> FitnessLogUi) {
        _uiState.update { current ->
            val mutated = normalizeLog(transform(current.currentLog), current.exerciseLibrary)
            current.copy(
                currentLog = mutated,
                dailyAnalysisResult = mutated.dailySuggestion,
                showExerciseLibrary = if (updateDialogState) current.showExerciseLibrary else current.showExerciseLibrary
            )
        }
        scheduleSave()
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(350)
            persistCurrentLog()
        }
    }

    private suspend fun persistCurrentLog() {
        _uiState.update { it.copy(isSaving = true) }
        runCatching {
            repository.saveLog(normalizeLog(_uiState.value.currentLog, _uiState.value.exerciseLibrary))
        }.onSuccess { savedLog ->
            val library = _uiState.value.exerciseLibrary
            val normalized = normalizeLog(savedLog, library)
            _uiState.update { current ->
                val nextLogs = current.logs.toMutableMap().apply {
                    put(normalized.date, normalized)
                }
                val currentDate = current.currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                current.copy(
                    isSaving = false,
                    logs = nextLogs,
                    currentLog = if (currentDate == normalized.date) normalized else current.currentLog,
                    dailyAnalysisResult = if (currentDate == normalized.date) normalized.dailySuggestion else current.dailyAnalysisResult
                )
            }
        }.onFailure { throwable ->
            _uiState.update { it.copy(isSaving = false) }
            emitMessage(throwable.message ?: "保存失败")
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _messages.emit(message)
        }
    }

    private fun getSavedBaseUrl(): String {
        return preferences.getString(PREF_BASE_URL, DEFAULT_BASE_URL)?.trim().orEmpty().ifBlank { DEFAULT_BASE_URL }
    }
}

@Composable
fun FitnessNativeApp() {
    val viewModel: FitnessViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    MaterialTheme(
        colorScheme = FitnessColorScheme,
        shapes = FitnessShapes
    ) {
        FitnessScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onRefresh = { viewModel.refreshAll() },
            onBaseUrlChanged = viewModel::updateBaseUrl,
            onSelectDate = viewModel::selectDate,
            onUpdateWeight = viewModel::updateWeight,
            onToggleCompleted = viewModel::toggleCompleted,
            onToggleMuscle = viewModel::toggleMuscleSelection,
            onAddRecommended = viewModel::addRecommendedExercises,
            onOpenLibrary = viewModel::openExerciseLibrary,
            onAddByCategory = viewModel::addExercisesByCategory,
            onAddExercise = viewModel::addExercise,
            onRemoveExercise = viewModel::removeExercise,
            onUpdateExerciseName = viewModel::updateExerciseName,
            onSyncExerciseCategory = viewModel::syncExerciseCategory,
            onUpdateExerciseCategory = viewModel::updateExerciseCategory,
            onAddSet = viewModel::addSet,
            onRemoveSet = viewModel::removeSet,
            onToggleSetCompleted = viewModel::toggleSetCompleted,
            onUpdateSetField = viewModel::updateSetField,
            onUpdateNotes = viewModel::updateNotes,
            onAnalyzeFitness = viewModel::analyzeFitness,
            onAnalyzeDaily = viewModel::analyzeDaily,
            onCloseLibrary = viewModel::closeExerciseLibrary,
            onSelectMuscleGroup = viewModel::selectMuscleGroup,
            onToggleEditMode = viewModel::toggleEditMode,
            onStartEditExercise = viewModel::startEditExercise,
            onCancelEditExercise = viewModel::cancelEditExercise,
            onUpdateEditingExerciseName = viewModel::updateEditingExerciseName,
            onSaveEditedExercise = viewModel::saveEditedExercise,
            onDeleteExercise = viewModel::deleteExercise,
            onShowAddForm = viewModel::showAddExerciseForm,
            onCancelAddForm = viewModel::cancelAddExerciseForm,
            onUpdateNewExerciseName = viewModel::updateNewExerciseName,
            onUpdateNewExerciseCategory = viewModel::updateNewExerciseCategory,
            onSaveNewExercise = viewModel::saveNewExercise,
            onAddExerciseFromLibrary = viewModel::addExerciseFromLibrary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FitnessScreen(
    uiState: FitnessUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onUpdateWeight: (String) -> Unit,
    onToggleCompleted: () -> Unit,
    onToggleMuscle: (String) -> Unit,
    onAddRecommended: () -> Unit,
    onOpenLibrary: () -> Unit,
    onAddByCategory: (String) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onUpdateExerciseName: (Int, String) -> Unit,
    onSyncExerciseCategory: (Int) -> Unit,
    onUpdateExerciseCategory: (Int, String) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onToggleSetCompleted: (Int, Int) -> Unit,
    onUpdateSetField: (Int, Int, String, String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onAnalyzeFitness: () -> Unit,
    onAnalyzeDaily: () -> Unit,
    onCloseLibrary: () -> Unit,
    onSelectMuscleGroup: (String) -> Unit,
    onToggleEditMode: () -> Unit,
    onStartEditExercise: (ExerciseLibraryItem) -> Unit,
    onCancelEditExercise: () -> Unit,
    onUpdateEditingExerciseName: (String) -> Unit,
    onSaveEditedExercise: () -> Unit,
    onDeleteExercise: (ExerciseLibraryItem) -> Unit,
    onShowAddForm: () -> Unit,
    onCancelAddForm: () -> Unit,
    onUpdateNewExerciseName: (String) -> Unit,
    onUpdateNewExerciseCategory: (String) -> Unit,
    onSaveNewExercise: () -> Unit,
    onAddExerciseFromLibrary: (String, String) -> Unit
) {
    val context = LocalContext.current
    var showBaseUrlDialog by rememberSaveable { mutableStateOf(false) }
    val recommendedSections = remember(uiState.currentLog.targetMuscles, uiState.exerciseLibrary) {
        recommendedPlanSections(uiState.currentLog.targetMuscles, uiState.exerciseLibrary)
    }
    val sortedLogs = remember(uiState.logs) {
        uiState.logs.values.sortedByDescending { it.date }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FitnessBackground,
                    titleContentColor = FitnessText,
                    actionIconContentColor = FitnessPrimary
                ),
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("健身追踪", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        Text(
                            text = uiState.baseUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = FitnessTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = { showBaseUrlDialog = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "地址设置")
                    }
                }
            )
        },
        containerColor = FitnessBackground
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val twoColumns = maxWidth >= 980.dp
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (twoColumns) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1.8f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                OverviewCard(
                                    uiState = uiState,
                                    onSelectDate = { showDatePicker(context, uiState.currentDate, onSelectDate) },
                                    onUpdateWeight = onUpdateWeight,
                                    onToggleCompleted = onToggleCompleted,
                                    onToggleMuscle = onToggleMuscle
                                )
                            }
                            item {
                                TrainingPlanCard(
                                    sections = recommendedSections,
                                    onAddRecommended = onAddRecommended,
                                    onOpenLibrary = onOpenLibrary,
                                    onAddByCategory = onAddByCategory,
                                    onAddExerciseFromRecommendation = { item, category -> onAddExerciseFromLibrary(item.name, category) }
                                )
                            }
                            item {
                                WorkoutDetailsCard(
                                    uiState = uiState,
                                    onAddExercise = onAddExercise,
                                    onRemoveExercise = onRemoveExercise,
                                    onUpdateExerciseName = onUpdateExerciseName,
                                    onSyncExerciseCategory = onSyncExerciseCategory,
                                    onUpdateExerciseCategory = onUpdateExerciseCategory,
                                    onAddSet = onAddSet,
                                    onRemoveSet = onRemoveSet,
                                    onToggleSetCompleted = onToggleSetCompleted,
                                    onUpdateSetField = onUpdateSetField,
                                    onUpdateNotes = onUpdateNotes
                                )
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                DailySuggestionCard(
                                    content = uiState.dailyAnalysisResult,
                                    isLoading = uiState.isAnalyzingDaily,
                                    onAnalyzeDaily = onAnalyzeDaily
                                )
                            }
                            item {
                                AnalysisCard(
                                    content = uiState.analysisResult,
                                    isLoading = uiState.isAnalyzing,
                                    onAnalyzeFitness = onAnalyzeFitness
                                )
                            }
                            item {
                                WeightTrendCard(logs = sortedLogs)
                            }
                            item {
                                HistoryCard(sortedLogs = sortedLogs, currentDate = uiState.currentLog.date, onSelectDate = onSelectDate)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            OverviewCard(
                                uiState = uiState,
                                onSelectDate = { showDatePicker(context, uiState.currentDate, onSelectDate) },
                                onUpdateWeight = onUpdateWeight,
                                onToggleCompleted = onToggleCompleted,
                                onToggleMuscle = onToggleMuscle
                            )
                        }
                        item {
                            TrainingPlanCard(
                                sections = recommendedSections,
                                onAddRecommended = onAddRecommended,
                                onOpenLibrary = onOpenLibrary,
                                onAddByCategory = onAddByCategory,
                                onAddExerciseFromRecommendation = { item, category -> onAddExerciseFromLibrary(item.name, category) }
                            )
                        }
                        item {
                            WorkoutDetailsCard(
                                uiState = uiState,
                                onAddExercise = onAddExercise,
                                onRemoveExercise = onRemoveExercise,
                                onUpdateExerciseName = onUpdateExerciseName,
                                onSyncExerciseCategory = onSyncExerciseCategory,
                                onUpdateExerciseCategory = onUpdateExerciseCategory,
                                onAddSet = onAddSet,
                                onRemoveSet = onRemoveSet,
                                onToggleSetCompleted = onToggleSetCompleted,
                                onUpdateSetField = onUpdateSetField,
                                onUpdateNotes = onUpdateNotes
                            )
                        }
                        item {
                            DailySuggestionCard(
                                content = uiState.dailyAnalysisResult,
                                isLoading = uiState.isAnalyzingDaily,
                                onAnalyzeDaily = onAnalyzeDaily
                            )
                        }
                        item {
                            AnalysisCard(
                                content = uiState.analysisResult,
                                isLoading = uiState.isAnalyzing,
                                onAnalyzeFitness = onAnalyzeFitness
                            )
                        }
                        item { WeightTrendCard(logs = sortedLogs) }
                        item { HistoryCard(sortedLogs = sortedLogs, currentDate = uiState.currentLog.date, onSelectDate = onSelectDate) }
                    }
                }
            }

            if (uiState.isRefreshing || uiState.isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = FitnessPrimary,
                    trackColor = FitnessPrimarySoft
                )
            }
        }
    }

    if (showBaseUrlDialog) {
        BaseUrlDialog(
            currentBaseUrl = uiState.baseUrl,
            onDismiss = { showBaseUrlDialog = false },
            onConfirm = {
                showBaseUrlDialog = false
                onBaseUrlChanged(it)
            }
        )
    }

    if (uiState.showExerciseLibrary) {
        ExerciseLibraryDialog(
            uiState = uiState,
            onDismiss = onCloseLibrary,
            onSelectMuscleGroup = onSelectMuscleGroup,
            onToggleEditMode = onToggleEditMode,
            onStartEditExercise = onStartEditExercise,
            onCancelEditExercise = onCancelEditExercise,
            onUpdateEditingExerciseName = onUpdateEditingExerciseName,
            onSaveEditedExercise = onSaveEditedExercise,
            onDeleteExercise = onDeleteExercise,
            onShowAddForm = onShowAddForm,
            onCancelAddForm = onCancelAddForm,
            onUpdateNewExerciseName = onUpdateNewExerciseName,
            onUpdateNewExerciseCategory = onUpdateNewExerciseCategory,
            onSaveNewExercise = onSaveNewExercise,
            onAddExerciseFromLibrary = onAddExerciseFromLibrary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewCard(
    uiState: FitnessUiState,
    onSelectDate: () -> Unit,
    onUpdateWeight: (String) -> Unit,
    onToggleCompleted: () -> Unit,
    onToggleMuscle: (String) -> Unit
) {
    FitnessCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onSelectDate, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(uiState.currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
                OutlinedTextField(
                    value = uiState.currentLog.weight,
                    onValueChange = onUpdateWeight,
                    label = { Text("今日体重") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = fitnessTextFieldColors()
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("完成状态", fontWeight = FontWeight.SemiBold)
                    Text(if (uiState.currentLog.completed) "今天训练已完成" else "训练尚未完成", color = Color(0xFF64748B))
                }
                Switch(checked = uiState.currentLog.completed, onCheckedChange = { onToggleCompleted() })
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("今日训练", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("支持多选，自动生成当日训练建议", color = Color(0xFF64748B), fontSize = 12.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MUSCLE_GROUPS.forEach { muscle ->
                        FilterChip(
                            selected = uiState.currentLog.targetMuscles.contains(muscle),
                            onClick = { onToggleMuscle(muscle) },
                            label = { Text(muscle) },
                            shape = RoundedCornerShape(18.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FitnessPrimarySoft,
                                selectedLabelColor = FitnessPrimary,
                                containerColor = Color.White,
                                labelColor = FitnessText,
                                disabledSelectedContainerColor = FitnessPrimarySoft
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.currentLog.targetMuscles.contains(muscle),
                                borderColor = FitnessBorder,
                                selectedBorderColor = FitnessPrimarySoft
                            )
                        )
                    }
                }
                if (uiState.currentLog.targetMuscles.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.currentLog.targetMuscles.forEach { muscle ->
                            AssistChip(
                                onClick = {},
                                label = { Text(muscle) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = FitnessPrimarySoft, labelColor = FitnessPrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingPlanCard(
    sections: List<PlanSectionUi>,
    onAddRecommended: () -> Unit,
    onOpenLibrary: () -> Unit,
    onAddByCategory: (String) -> Unit,
    onAddExerciseFromRecommendation: (ExerciseLibraryItem, String) -> Unit
) {
    FitnessCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("训练计划", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("不再使用预设计划，改为根据今日训练动态推荐动作。", color = Color(0xFF64748B), fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryActionButton(text = "加入推荐动作", onClick = onAddRecommended, enabled = sections.isNotEmpty())
                    OutlineActionButton(text = "打开动作库", onClick = onOpenLibrary)
                }
            }
            if (sections.isEmpty()) {
                EmptyHint("先选择今日训练部位，即可生成对应动作建议。选中胸部时，这里会直接列出胸部训练动作。")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sections.forEach { section ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(section.category, fontWeight = FontWeight.SemiBold)
                                        Text("推荐 ${section.exercises.size} 个动作", color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                    OutlineActionButton(text = "加入本组动作", onClick = { onAddByCategory(section.category) })
                                }
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    section.exercises.forEach { exercise ->
                                        AssistChip(onClick = { onAddExerciseFromRecommendation(exercise, section.category) }, label = { Text(exercise.name) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailsCard(
    uiState: FitnessUiState,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onUpdateExerciseName: (Int, String) -> Unit,
    onSyncExerciseCategory: (Int) -> Unit,
    onUpdateExerciseCategory: (Int, String) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onToggleSetCompleted: (Int, Int) -> Unit,
    onUpdateSetField: (Int, Int, String, String) -> Unit,
    onUpdateNotes: (String) -> Unit
) {
    FitnessCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("今日训练明细", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("力量动作支持重量与次数；有氧支持时长、速度、坡度、距离、阻力和踏频。", color = Color(0xFF64748B), fontSize = 12.sp)
                }
                OutlineActionButton(text = "手动添加动作", onClick = onAddExercise, leadingIcon = Icons.Default.Add)
            }
            if (uiState.currentLog.workoutDetails.isEmpty()) {
                EmptyHint("还没有安排动作，可以从上方推荐动作或动作库快速添加。")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.currentLog.workoutDetails.forEachIndexed { exerciseIndex, exercise ->
                        ExerciseEditorCard(
                            exercise = exercise,
                            exerciseIndex = exerciseIndex,
                            onRemoveExercise = onRemoveExercise,
                            onUpdateExerciseName = onUpdateExerciseName,
                            onSyncExerciseCategory = onSyncExerciseCategory,
                            onUpdateExerciseCategory = onUpdateExerciseCategory,
                            onAddSet = onAddSet,
                            onRemoveSet = onRemoveSet,
                            onToggleSetCompleted = onToggleSetCompleted,
                            onUpdateSetField = onUpdateSetField
                        )
                    }
                }
            }
            HorizontalDivider(color = FitnessBorder.copy(alpha = 0.55f), thickness = 1.dp)
            OutlinedTextField(
                value = uiState.currentLog.notes,
                onValueChange = onUpdateNotes,
                label = { Text("训练备注 / 感受") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(20.dp),
                colors = fitnessTextFieldColors()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseEditorCard(
    exercise: WorkoutExerciseUi,
    exerciseIndex: Int,
    onRemoveExercise: (Int) -> Unit,
    onUpdateExerciseName: (Int, String) -> Unit,
    onSyncExerciseCategory: (Int) -> Unit,
    onUpdateExerciseCategory: (Int, String) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onToggleSetCompleted: (Int, Int) -> Unit,
    onUpdateSetField: (Int, Int, String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = exercise.name,
                    onValueChange = { onUpdateExerciseName(exerciseIndex, it) },
                    label = { Text("动作名称") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = fitnessTextFieldColors()
                )
                Box {
                    OutlineActionButton(text = exercise.category.ifBlank { "选择分类" }, onClick = { expanded = true })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        MUSCLE_GROUPS.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    expanded = false
                                    onUpdateExerciseCategory(exerciseIndex, group)
                                    onSyncExerciseCategory(exerciseIndex)
                                }
                            )
                        }
                    }
                }
                OutlineActionButton(text = "删除", onClick = { onRemoveExercise(exerciseIndex) }, contentColor = FitnessDanger)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                exercise.sets.forEachIndexed { setIndex, set ->
                    SetEditorCard(
                        exercise = exercise,
                        set = set,
                        exerciseIndex = exerciseIndex,
                        setIndex = setIndex,
                        onRemoveSet = onRemoveSet,
                        onToggleSetCompleted = onToggleSetCompleted,
                        onUpdateSetField = onUpdateSetField
                    )
                }
            }
            TextButton(onClick = { onAddSet(exerciseIndex) }) {
                Text("+ 添加一组")
            }
        }
    }
}

@Composable
private fun SetEditorCard(
    exercise: WorkoutExerciseUi,
    set: WorkoutSetUi,
    exerciseIndex: Int,
    setIndex: Int,
    onRemoveSet: (Int, Int) -> Unit,
    onToggleSetCompleted: (Int, Int) -> Unit,
    onUpdateSetField: (Int, Int, String, String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("第 ${setIndex + 1} 组", fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleSetCompleted(exerciseIndex, setIndex) }) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = if (set.completed) Color(0xFF10B981) else Color(0xFF94A3B8))
                    }
                    TextButton(onClick = { onRemoveSet(exerciseIndex, setIndex) }) {
                        Text("删除组")
                    }
                }
            }
            when {
                isCardioExercise(exercise) -> {
                    FieldGrid(
                        fields = cardioFields(exercise, set),
                        onValueChange = { field, value -> onUpdateSetField(exerciseIndex, setIndex, field, value) }
                    )
                }
                isRecoveryExercise(exercise) -> {
                    FieldGrid(
                        fields = listOf(
                            FieldConfig("duration", "时长(分钟)", set.duration),
                            FieldConfig("reps", "备注", set.reps)
                        ),
                        columns = 2,
                        onValueChange = { field, value -> onUpdateSetField(exerciseIndex, setIndex, field, value) }
                    )
                }
                else -> {
                    val fields = mutableListOf(
                        FieldConfig("weight", "重量(kg)", set.weight),
                        FieldConfig("reps", "次数", set.reps)
                    )
                    if (isCoreExercise(exercise)) {
                        fields += FieldConfig("duration", "时长(秒)", set.duration)
                    }
                    FieldGrid(fields = fields, onValueChange = { field, value -> onUpdateSetField(exerciseIndex, setIndex, field, value) })
                }
            }
        }
    }
}

data class FieldConfig(val key: String, val label: String, val value: String)

@Composable
private fun FieldGrid(
    fields: List<FieldConfig>,
    columns: Int = 3,
    onValueChange: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        fields.chunked(columns).forEach { rowFields ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowFields.forEach { field ->
                    OutlinedTextField(
                        value = field.value,
                        onValueChange = { onValueChange(field.key, it) },
                        label = { Text(field.label) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = fitnessTextFieldColors()
                    )
                }
                repeat(columns - rowFields.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DailySuggestionCard(content: String, isLoading: Boolean, onAnalyzeDaily: () -> Unit) {
    GradientCard(start = Color(0xFFECFDF5), end = Color(0xFFF0FDF4), border = Color(0xFFA7F3D0)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("明日建议", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF065F46))
                PrimaryActionButton(text = if (isLoading) "分析中..." else "生成建议", onClick = onAnalyzeDaily, enabled = !isLoading)
            }
            Text("分析完成后会自动保存，刷新页面仍可直接查看。", color = Color(0xFF047857), fontSize = 12.sp)
            AnalysisText(content = content.ifBlank { "暂无建议，点击右上角生成。" }, emptyStyle = content.isBlank())
        }
    }
}

@Composable
private fun AnalysisCard(content: String, isLoading: Boolean, onAnalyzeFitness: () -> Unit) {
    GradientCard(start = Color(0xFFEEF2FF), end = Color(0xFFE0F2FE), border = Color(0xFFBFDBFE)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("AI 教练分析", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF312E81))
                PrimaryActionButton(text = if (isLoading) "分析中..." else "开始分析", onClick = onAnalyzeFitness, enabled = !isLoading)
            }
            AnalysisText(content = content.ifBlank { "基于近期训练和体重数据生成阶段性建议。" }, emptyStyle = content.isBlank())
        }
    }
}

@Composable
private fun WeightTrendCard(logs: List<FitnessLogUi>) {
    val weightedLogs = logs.filter { it.weight.toDoubleOrNull() != null }.sortedBy { it.date }.takeLast(60)
    FitnessCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("体重趋势", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("近 60 天", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            if (weightedLogs.isEmpty()) {
                EmptyHint("暂无体重数据")
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(264.dp)) {
                    WeightLineChart(weightedLogs)
                }
            }
        }
    }
}

@Composable
private fun WeightLineChart(logs: List<FitnessLogUi>) {
    val density = LocalDensity.current
    val axisPaint = remember(density) {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = FitnessTextMuted.toArgb()
            textSize = with(density) { 11.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
    }
    val strongAxisPaint = remember(density) {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = FitnessText.toArgb()
            textSize = with(density) { 11.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    val points = logs.mapNotNull { it.weight.toDoubleOrNull() }
    val minValue = points.minOrNull() ?: 0.0
    val maxValue = points.maxOrNull() ?: minValue + 1.0
    val range = (maxValue - minValue).takeIf { it > 0 } ?: 1.0
    Canvas(modifier = Modifier.fillMaxSize()) {
        val leftPadding = 44.dp.toPx()
        val rightPadding = 14.dp.toPx()
        val topPadding = 14.dp.toPx()
        val bottomPadding = 28.dp.toPx()
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding
        val bottomY = size.height - bottomPadding
        val nativeCanvas = drawContext.canvas.nativeCanvas

        repeat(4) { index ->
            val ratio = index / 3f
            val y = topPadding + chartHeight * ratio
            val value = maxValue - range * ratio
            drawLine(
                color = FitnessBorder.copy(alpha = if (index == 3) 0.7f else 0.42f),
                start = Offset(leftPadding, y),
                end = Offset(size.width - rightPadding, y),
                strokeWidth = if (index == 3) 2.5f else 1.6f
            )
            axisPaint.textAlign = AndroidPaint.Align.RIGHT
            nativeCanvas.drawText(String.format(Locale.US, "%.1f", value), leftPadding - 10.dp.toPx(), y + 4.dp.toPx(), axisPaint)
        }

        drawLine(FitnessBorder.copy(alpha = 0.7f), Offset(leftPadding, topPadding), Offset(leftPadding, bottomY), strokeWidth = 2.2f)
        drawLine(FitnessBorder.copy(alpha = 0.7f), Offset(leftPadding, bottomY), Offset(size.width - rightPadding, bottomY), strokeWidth = 2.2f)

        val linePath = Path()
        val fillPath = Path()
        var firstPoint: Offset? = null
        var lastPoint: Offset? = null
        logs.forEachIndexed { index, log ->
            val x = leftPadding + chartWidth * (index.toFloat() / (logs.size - 1).coerceAtLeast(1))
            val yRatio = ((log.weight.toDoubleOrNull() ?: minValue) - minValue) / range
            val y = bottomY - chartHeight * yRatio.toFloat()
            val point = Offset(x, y)
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, bottomY)
                fillPath.lineTo(x, y)
                firstPoint = point
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            lastPoint = point
            drawCircle(FitnessChartBlue, radius = 5.5f, center = point)
        }
        if (lastPoint != null && firstPoint != null) {
            fillPath.lineTo(lastPoint!!.x, bottomY)
            fillPath.close()
        }
        drawPath(path = fillPath, color = FitnessChartBlue.copy(alpha = 0.12f))
        drawPath(path = linePath, color = FitnessChartBlue, style = Stroke(width = 4.5f, cap = StrokeCap.Round))

        val labelIndices = listOf(0, logs.lastIndex / 2, logs.lastIndex).distinct()
        axisPaint.textAlign = AndroidPaint.Align.CENTER
        labelIndices.forEach { index ->
            val x = leftPadding + chartWidth * (index.toFloat() / (logs.size - 1).coerceAtLeast(1))
            nativeCanvas.drawText(logs[index].date.takeLast(5), x, size.height - 6.dp.toPx(), if (index == logs.lastIndex) strongAxisPaint else axisPaint)
        }
    }
}

@Composable
private fun HistoryCard(sortedLogs: List<FitnessLogUi>, currentDate: String, onSelectDate: (LocalDate) -> Unit) {
    FitnessCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("历史训练记录", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (sortedLogs.isEmpty()) {
                EmptyHint("暂无训练记录")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedLogs.forEach { log ->
                        val isSelected = currentDate == log.date
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onSelectDate(LocalDate.parse(log.date.take(10), DateTimeFormatter.ISO_LOCAL_DATE))
                            },
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEAF3FF) else Color.White),
                            border = BorderStroke(1.25.dp, if (isSelected) Color(0xFFBFD8FF) else FitnessBorder.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.date, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${formatTargetMuscles(log)} · ${log.workoutDetails.size} 个动作",
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(if (log.completed) FitnessSuccess.copy(alpha = 0.16f) else FitnessPrimarySoft, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(if (log.completed) "✅" else "待完成", color = if (log.completed) FitnessSuccess else FitnessPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseLibraryDialog(
    uiState: FitnessUiState,
    onDismiss: () -> Unit,
    onSelectMuscleGroup: (String) -> Unit,
    onToggleEditMode: () -> Unit,
    onStartEditExercise: (ExerciseLibraryItem) -> Unit,
    onCancelEditExercise: () -> Unit,
    onUpdateEditingExerciseName: (String) -> Unit,
    onSaveEditedExercise: () -> Unit,
    onDeleteExercise: (ExerciseLibraryItem) -> Unit,
    onShowAddForm: () -> Unit,
    onCancelAddForm: () -> Unit,
    onUpdateNewExerciseName: (String) -> Unit,
    onUpdateNewExerciseCategory: (String) -> Unit,
    onSaveNewExercise: () -> Unit,
    onAddExerciseFromLibrary: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(30.dp),
        containerColor = Color.White,
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 680.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("动作库", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("已移除图片展示，聚焦动作名称与分类管理。", color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onToggleEditMode) {
                            Text(if (uiState.isEditMode) "完成编辑" else "编辑管理")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }
                }
                ScrollableTabRow(selectedTabIndex = MUSCLE_GROUPS.indexOf(uiState.selectedMuscleGroup).coerceAtLeast(0)) {
                    MUSCLE_GROUPS.forEachIndexed { index, group ->
                        Tab(selected = group == uiState.selectedMuscleGroup, onClick = { onSelectMuscleGroup(group) }, text = { Text(group) })
                    }
                }
                if (uiState.isEditMode && !uiState.showAddExerciseForm) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        PrimaryActionButton(text = "添加新动作", onClick = onShowAddForm)
                    }
                }
                if (uiState.showAddExerciseForm) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("新增动作", fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = uiState.newExerciseName, onValueChange = onUpdateNewExerciseName, label = { Text("动作名称") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = fitnessTextFieldColors())
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                MUSCLE_GROUPS.forEach { group ->
                                    FilterChip(selected = uiState.newExerciseCategory == group, onClick = { onUpdateNewExerciseCategory(group) }, label = { Text(group) })
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = onCancelAddForm) { Text("取消") }
                                PrimaryActionButton(text = "保存", onClick = onSaveNewExercise)
                            }
                        }
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(uiState.exerciseLibrary[uiState.selectedMuscleGroup].orEmpty()) { _, exercise ->
                        val editing = uiState.editingExercise?.id == exercise.id
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            if (uiState.isEditMode && editing) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = uiState.editingExercise?.name.orEmpty(),
                                        onValueChange = onUpdateEditingExerciseName,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("动作名称") },
                                        shape = RoundedCornerShape(18.dp),
                                        colors = fitnessTextFieldColors()
                                    )
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = onCancelEditExercise) { Text("取消") }
                                        PrimaryActionButton(text = "保存", onClick = onSaveEditedExercise)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exercise.name, fontWeight = FontWeight.SemiBold)
                                        Text(uiState.selectedMuscleGroup, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                    if (uiState.isEditMode) {
                                        Row {
                                            IconButton(onClick = { onStartEditExercise(exercise) }) {
                                                Icon(Icons.Default.Edit, contentDescription = null)
                                            }
                                            IconButton(onClick = { onDeleteExercise(exercise) }) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                                            }
                                        }
                                    } else {
                                        OutlineActionButton(text = "加入", onClick = { onAddExerciseFromLibrary(exercise.name, uiState.selectedMuscleGroup) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun BaseUrlDialog(
    currentBaseUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(30.dp),
        containerColor = Color.White,
        title = { Text("设置服务端地址") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("请输入现有系统的服务端根地址，App 会自动连接健身接口。")
                OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(18.dp), colors = fitnessTextFieldColors())
            }
        },
        confirmButton = {
            PrimaryActionButton(text = "保存", onClick = { onConfirm(text) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun FitnessCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, FitnessBorder.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
private fun GradientCard(start: Color, end: Color, border: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(start, end)))
                .border(1.dp, border, RoundedCornerShape(30.dp))
                .padding(20.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(text, color = Color(0xFF64748B), fontSize = 13.sp)
    }
}

@Composable
private fun AnalysisText(content: String, emptyStyle: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (emptyStyle) FitnessBorder.copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(22.dp))
            .background(if (emptyStyle) Color.White.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.36f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        if (emptyStyle) {
            Text(content, lineHeight = 22.sp, color = FitnessTextMuted)
        } else {
            val context = LocalContext.current
            val markwon = remember(context) { Markwon.create(context) }
            AndroidView(
                factory = {
                    TextView(it).apply {
                        setTextColor(FitnessText.toArgb())
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        setLineSpacing(12f, 1.12f)
                        movementMethod = LinkMovementMethod.getInstance()
                        setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
                    }
                },
                update = { textView ->
                    textView.setTextColor(FitnessText.toArgb())
                    markwon.setMarkdown(textView, content)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FitnessPrimary,
            contentColor = Color.White,
            disabledContainerColor = FitnessPrimary.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OutlineActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    contentColor: Color = FitnessPrimary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.5.dp, if (contentColor == FitnessPrimary) FitnessBorder else contentColor.copy(alpha = 0.28f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun fitnessTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FitnessPrimary,
    unfocusedBorderColor = FitnessBorder,
    focusedLabelColor = FitnessPrimary,
    unfocusedLabelColor = FitnessTextMuted,
    cursorColor = FitnessPrimary,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedTextColor = FitnessText,
    unfocusedTextColor = FitnessText
)

private val FitnessPrimary = Color(0xFF6D4AB8)
private val FitnessPrimaryDark = Color(0xFF55359A)
private val FitnessPrimarySoft = Color(0xFFEBDDFF)
private val FitnessBackground = Color(0xFFF5F7FC)
private val FitnessSurface = Color(0xFFFFFFFF)
private val FitnessText = Color(0xFF20222C)
private val FitnessTextMuted = Color(0xFF70829E)
private val FitnessBorder = Color(0xFFD8CFEF)
private val FitnessDanger = Color(0xFFFF5D5D)
private val FitnessSuccess = Color(0xFF0BBE92)
private val FitnessChartBlue = Color(0xFF1E6BE3)

private val FitnessColorScheme = lightColorScheme(
    primary = FitnessPrimary,
    onPrimary = Color.White,
    primaryContainer = FitnessPrimarySoft,
    onPrimaryContainer = FitnessPrimaryDark,
    secondary = Color(0xFF6C7FA5),
    onSecondary = Color.White,
    background = FitnessBackground,
    onBackground = FitnessText,
    surface = FitnessSurface,
    onSurface = FitnessText,
    surfaceVariant = Color(0xFFF0F3FB),
    onSurfaceVariant = FitnessTextMuted,
    outline = FitnessBorder,
    outlineVariant = FitnessBorder.copy(alpha = 0.48f),
    error = FitnessDanger,
    onError = Color.White
)

private val FitnessShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private fun normalizeBaseUrl(baseUrl: String): String {
    val trimmed = baseUrl.trim().ifBlank { DEFAULT_BASE_URL }.trimEnd('/')
    return if (trimmed.endsWith('/')) trimmed else "$trimmed/"
}

private fun FitnessLogUi.toRequest(): FitnessLogRequest = FitnessLogRequest(
    date = date,
    weight = weight.toDoubleOrNull(),
    targetMuscle = targetMuscle,
    targetMuscles = targetMuscles,
    workoutPlanName = workoutPlanName,
    workoutDetails = workoutDetails.map { exercise ->
        WorkoutExerciseRequest(
            name = exercise.name,
            category = exercise.category,
            sets = exercise.sets.map { set ->
                WorkoutSetRequest(
                    reps = set.reps,
                    weight = set.weight.ifBlank { null },
                    duration = set.duration.ifBlank { null },
                    speed = set.speed.ifBlank { null },
                    incline = set.incline.ifBlank { null },
                    distance = set.distance.ifBlank { null },
                    resistance = set.resistance.ifBlank { null },
                    cadence = set.cadence.ifBlank { null },
                    completed = set.completed
                )
            }
        )
    },
    dailySuggestion = dailySuggestion.ifBlank { null },
    completed = completed,
    notes = notes
)

private fun normalizeLog(log: FitnessLogDto, library: Map<String, List<ExerciseLibraryItem>>): FitnessLogUi {
    val normalizedDate = normalizeDate(log.date)
    val targetMuscles = when {
        !log.targetMuscles.isNullOrEmpty() -> log.targetMuscles.filter { it.isNotBlank() }
        !log.targetMuscle.isNullOrBlank() -> log.targetMuscle.split(Regex("[、,，/\\s]+"))
            .filter { it.isNotBlank() }
        else -> emptyList()
    }
    return FitnessLogUi(
        date = normalizedDate,
        weight = log.weight?.toString().orEmpty(),
        targetMuscle = targetMuscles.joinToString("、"),
        targetMuscles = targetMuscles,
        workoutPlanName = if (targetMuscles.isEmpty()) log.workoutPlanName.orEmpty() else "${targetMuscles.joinToString(" / ")} 训练",
        workoutDetails = log.workoutDetails.orEmpty().map { exercise ->
            val category = exercise.category.orEmpty().ifBlank {
                inferCategoryByName(exercise.name.orEmpty(), library)
            }.ifBlank { targetMuscles.firstOrNull().orEmpty().ifBlank { MUSCLE_GROUPS.first() } }
            WorkoutExerciseUi(
                name = exercise.name.orEmpty(),
                category = category,
                sets = if (exercise.sets.isNullOrEmpty()) {
                    listOf(createDefaultSet(category, exercise.name.orEmpty()))
                } else {
                    exercise.sets.map { set ->
                        createDefaultSet(category, exercise.name.orEmpty()).copy(
                            reps = set.reps.orEmpty(),
                            weight = set.weight.orEmpty(),
                            duration = set.duration.orEmpty(),
                            speed = set.speed.orEmpty(),
                            incline = set.incline.orEmpty(),
                            distance = set.distance.orEmpty(),
                            resistance = set.resistance.orEmpty(),
                            cadence = set.cadence.orEmpty(),
                            completed = set.completed ?: false
                        )
                    }
                }
            )
        },
        dailySuggestion = log.dailySuggestion.orEmpty(),
        completed = log.completed ?: false,
        notes = log.notes.orEmpty()
    )
}

private fun normalizeLog(log: FitnessLogUi, library: Map<String, List<ExerciseLibraryItem>>): FitnessLogUi {
    val targetMuscles = log.targetMuscles.filter { it.isNotBlank() }
    return log.copy(
        date = normalizeDate(log.date),
        targetMuscles = targetMuscles,
        targetMuscle = targetMuscles.joinToString("、"),
        workoutPlanName = if (targetMuscles.isEmpty()) "" else "${targetMuscles.joinToString(" / ")} 训练",
        workoutDetails = log.workoutDetails.map { exercise ->
            val category = exercise.category.ifBlank {
                inferCategoryByName(exercise.name, library)
            }.ifBlank { targetMuscles.firstOrNull().orEmpty().ifBlank { MUSCLE_GROUPS.first() } }
            exercise.copy(
                category = category,
                sets = if (exercise.sets.isEmpty()) listOf(createDefaultSet(category, exercise.name)) else exercise.sets
            )
        }
    )
}

private fun normalizeAllLogs(logs: Map<String, FitnessLogUi>, library: Map<String, List<ExerciseLibraryItem>>): Map<String, FitnessLogUi> {
    return logs.mapValues { (_, log) -> normalizeLog(log, library) }
}

private fun createEmptyLog(date: String): FitnessLogUi = FitnessLogUi(date = date)

private fun createExercise(name: String = "", category: String = ""): WorkoutExerciseUi {
    return WorkoutExerciseUi(
        name = name,
        category = category.ifBlank { MUSCLE_GROUPS.first() },
        sets = listOf(createDefaultSet(category, name))
    )
}

private fun createDefaultSet(category: String = "", name: String = ""): WorkoutSetUi {
    return when {
        category == "有氧" || matchesKeyword(name, CARDIO_KEYWORDS) -> WorkoutSetUi()
        category == "休息" -> WorkoutSetUi()
        else -> WorkoutSetUi()
    }
}

private fun inferCategoryByName(name: String, library: Map<String, List<ExerciseLibraryItem>>): String {
    library.forEach { (category, exercises) ->
        if (exercises.any { it.name == name }) return category
    }
    return if (matchesKeyword(name, CARDIO_KEYWORDS)) "有氧" else ""
}

private fun recommendedPlanSections(targetMuscles: List<String>, library: Map<String, List<ExerciseLibraryItem>>): List<PlanSectionUi> {
    val order = targetMuscles.toMutableList().apply {
        if (contains("胸部")) {
            sortWith { left, right ->
                when {
                    left == "胸部" && right != "胸部" -> -1
                    right == "胸部" && left != "胸部" -> 1
                    else -> 0
                }
            }
        }
    }
    return order.mapNotNull { category ->
        val exercises = library[category].orEmpty().take(if (category == "胸部") 8 else 6)
        if (exercises.isEmpty()) null else PlanSectionUi(category = category, exercises = exercises)
    }
}

private fun recommendedExercises(targetMuscles: List<String>, library: Map<String, List<ExerciseLibraryItem>>): List<WorkoutExerciseUi> {
    val seen = mutableSetOf<String>()
    return recommendedPlanSections(targetMuscles, library)
        .flatMap { section ->
            section.exercises.mapNotNull { exercise ->
                val key = "${section.category}:${exercise.name}"
                if (!seen.add(key)) null else WorkoutExerciseUi(name = exercise.name, category = section.category, sets = listOf(createDefaultSet(section.category, exercise.name)))
            }
        }
}

private fun WorkoutSetUi.updateField(field: String, value: String): WorkoutSetUi {
    return when (field) {
        "reps" -> copy(reps = value)
        "weight" -> copy(weight = value)
        "duration" -> copy(duration = value)
        "speed" -> copy(speed = value)
        "incline" -> copy(incline = value)
        "distance" -> copy(distance = value)
        "resistance" -> copy(resistance = value)
        "cadence" -> copy(cadence = value)
        else -> this
    }
}

private fun normalizeDate(date: String?): String {
    if (date.isNullOrBlank()) return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    return if (date.length >= 10) date.substring(0, 10) else date
}

private fun matchesKeyword(name: String, keywords: List<String>): Boolean {
    return keywords.any { keyword -> name.contains(keyword) }
}

private fun isCardioExercise(exercise: WorkoutExerciseUi): Boolean {
    return exercise.category == "有氧" || matchesKeyword(exercise.name, CARDIO_KEYWORDS)
}

private fun isRecoveryExercise(exercise: WorkoutExerciseUi): Boolean = exercise.category == "休息"

private fun isCoreExercise(exercise: WorkoutExerciseUi): Boolean = exercise.category == "核心"

private fun supportsIncline(exercise: WorkoutExerciseUi): Boolean = isCardioExercise(exercise) && matchesKeyword(exercise.name, INCLINE_KEYWORDS)

private fun supportsResistance(exercise: WorkoutExerciseUi): Boolean = isCardioExercise(exercise) && matchesKeyword(exercise.name, RESISTANCE_KEYWORDS)

private fun supportsCadence(exercise: WorkoutExerciseUi): Boolean = isCardioExercise(exercise) && matchesKeyword(exercise.name, CADENCE_KEYWORDS)

private fun cardioFields(exercise: WorkoutExerciseUi, set: WorkoutSetUi): List<FieldConfig> {
    val fields = mutableListOf(
        FieldConfig("duration", "时长(分钟)", set.duration),
        FieldConfig("distance", "距离(km)", set.distance),
        FieldConfig("speed", "速度(km/h)", set.speed)
    )
    if (supportsIncline(exercise)) fields += FieldConfig("incline", "坡度(%)", set.incline)
    if (supportsResistance(exercise)) fields += FieldConfig("resistance", "阻力", set.resistance)
    if (supportsCadence(exercise)) fields += FieldConfig("cadence", "踏频(rpm)", set.cadence)
    return fields
}

private fun formatTargetMuscles(log: FitnessLogUi): String {
    return if (log.targetMuscles.isEmpty()) log.targetMuscle.ifBlank { "未设置" } else log.targetMuscles.joinToString("、")
}

private fun showDatePicker(context: Context, currentDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        currentDate.year,
        currentDate.monthValue - 1,
        currentDate.dayOfMonth
    ).show()
}

private val MUSCLE_GROUPS = listOf("胸部", "背部", "腿部", "肩部", "手臂", "核心", "有氧", "休息")
private val CARDIO_KEYWORDS = listOf("跑", "骑", "椭圆机", "划船机", "跳绳", "爬楼", "快走", "有氧")
private val INCLINE_KEYWORDS = listOf("跑", "坡走", "快走")
private val RESISTANCE_KEYWORDS = listOf("骑", "单车", "椭圆机", "划船机", "爬楼")
private val CADENCE_KEYWORDS = listOf("骑", "单车", "划船机")
private const val PREFS_NAME = "fitness_native_prefs"
private const val PREF_BASE_URL = "pref_base_url"
private const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
