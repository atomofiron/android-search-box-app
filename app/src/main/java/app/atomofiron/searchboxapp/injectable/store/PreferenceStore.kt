package app.atomofiron.searchboxapp.injectable.store

import android.content.Context
import android.view.Gravity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.atomofiron.common.util.flow.SharedFlowProperty
import app.atomofiron.common.util.flow.asProperty
import app.atomofiron.searchboxapp.model.preference.*
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_APP_ORIENTATION
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_APP_THEME
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_DEEP_BLACK
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_DOCK_GRAVITY
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_EXCLUDE_DIRS
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_EXPLORER_ITEM
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_JOYSTICK
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_MAX_DEPTH
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_MAX_SIZE
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_OPENED_DIR_PATH
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_SPECIAL_CHARACTERS
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_STORAGE_PATH
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_TEXT_FORMATS
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_TOYBOX
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KEY_USE_SU
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferences",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, context.packageName + "_preferences"))
    },
)

class PreferenceStore(
    private val context: Context,
    private val scope: CoroutineScope,
) : DataStore<Preferences> by context.dataStore {

    lateinit var initialPreferences: Preferences

    init {
        scope.launch {
            data.collect {
                initialPreferences = it
            }
        }
    }

    operator fun invoke(block: suspend PreferenceStore.() -> Unit) {
        scope.launch(Dispatchers.Main.immediate, CoroutineStart.UNDISPATCHED) {
            this@PreferenceStore.block()
        }
    }

    val useSu = getFlow(KEY_USE_SU, false)

    suspend fun setUseSu(value: Boolean) {
        edit { it[KEY_USE_SU] = value }
    }

    val storagePath = getFlow(KEY_STORAGE_PATH, Const.ROOT)

    suspend fun setStoragePath(value: String) {
        edit { it[KEY_STORAGE_PATH] = value }
    }

    val openedDirPath = getFlow(KEY_OPENED_DIR_PATH, "")

    suspend fun setOpenedDirPath(value: String?) {
        edit {
            when (value) {
                null -> it.remove(KEY_OPENED_DIR_PATH)
                else -> it[KEY_OPENED_DIR_PATH] = value
            }
        }
    }

    val dockGravity = getFlow(KEY_DOCK_GRAVITY, Gravity.START)

    suspend fun setDockGravity(value: Int) {
        edit { it[KEY_DOCK_GRAVITY] = value }
    }

    val specialCharacters = getFlow(KEY_SPECIAL_CHARACTERS, Const.DEFAULT_SPECIAL_CHARACTERS) {
        it.split(" ").toTypedArray()
    }

    suspend fun setSpecialCharacters(value: Array<String>) {
        edit { it[KEY_SPECIAL_CHARACTERS] = value.joinToString(separator = " ") }
    }

    val excludeDirs = getFlow(KEY_EXCLUDE_DIRS, false)

    suspend fun setExcludeDirs(value: Boolean) {
        edit { it[KEY_EXCLUDE_DIRS] = value }
    }

    val textFormats = getFlow(KEY_TEXT_FORMATS, Const.DEFAULT_TEXT_FORMATS) {
        it.split(" ").toTypedArray()
    }

    suspend fun setTextFormats(value: Array<String>) {
        edit { it[KEY_TEXT_FORMATS] = value.joinToString(separator = " ") }
    }

    val maxFileSizeForSearch = getFlow(KEY_MAX_SIZE, Const.DEFAULT_MAX_SIZE)

    suspend fun setMaxFileSizeForSearch(value: Long) {
        edit { it[KEY_MAX_SIZE] = value }
    }

    val maxDepthForSearch = getFlow(KEY_MAX_DEPTH, Const.DEFAULT_MAX_DEPTH)

    suspend fun setMaxDepthForSearch(value: Int) {
        edit { it[KEY_MAX_DEPTH] = value }
    }

    val deepBlack = getFlow(KEY_DEEP_BLACK, false)

    suspend fun setDeepBlack(value: Boolean) {
        edit { it[KEY_DEEP_BLACK] = value }
    }

    private val appThemeMode = getFlow(KEY_APP_THEME, AppTheme.defaultName())

    val appTheme = data.map {
        val appThemeMode = it[KEY_APP_THEME] ?: AppTheme.defaultName()
        val deepBlack = it[KEY_DEEP_BLACK] ?: false
        AppTheme.fromString(appThemeMode, deepBlack)
    }.shareInOne(scope).asProperty()

    suspend fun setAppTheme(value: AppTheme) {
        edit { it[KEY_APP_THEME] = value.name }
    }

    val appOrientation = getFlow(KEY_APP_ORIENTATION, AppOrientation.UNDEFINED.ordinal.toString()) {
        AppOrientation.values()[it.toInt()]
    }

    suspend fun setAppOrientation(value: AppOrientation) {
        edit { it[KEY_APP_ORIENTATION] = value.ordinal.toString() }
    }

    val explorerItemComposition = getFlow(KEY_EXPLORER_ITEM, Const.DEFAULT_EXPLORER_ITEM) {
        ExplorerItemComposition(it)
    }

    suspend fun setExplorerItemComposition(value: ExplorerItemComposition) {
        edit { it[KEY_EXPLORER_ITEM] = value.flags }
    }

    val joystickComposition = getFlow(KEY_JOYSTICK, Const.DEFAULT_JOYSTICK) {
        JoystickComposition(it)
    }

    suspend fun setJoystickComposition(value: JoystickComposition) {
        edit { it[KEY_JOYSTICK] = value.data }
    }

    val toyboxVariant = getFlow(KEY_TOYBOX, setOf(Const.VALUE_TOYBOX_CUSTOM, Const.DEFAULT_TOYBOX_PATH)) {
        ToyboxVariant.fromSet(context, it)
    }

    suspend fun setToyboxVariant(value: Set<String>) {
        edit { it[KEY_TOYBOX] = value }
    }

    private fun <V> getFlow(key: Preferences.Key<V>, default: V): SharedFlowProperty<V> {
        return data.map { it[key] ?: default }.shareInOne(scope).asProperty()
    }

    private fun <V,E> getFlow(key: Preferences.Key<V>, default: V, transformation: (V) -> E): SharedFlowProperty<E> {
        return data.map { (it[key] ?: default).let(transformation) }.shareInOne(scope).asProperty()
    }

    private fun <T> Flow<T>.shareInOne(scope: CoroutineScope): SharedFlow<T> {
        return distinctUntilChanged().shareIn(scope, SharingStarted.Lazily, replay = 1)
    }
}
