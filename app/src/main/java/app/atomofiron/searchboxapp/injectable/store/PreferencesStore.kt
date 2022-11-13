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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferences",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, context.packageName + "_preferences"))
    },
)

class PreferencesStore(
    private val context: Context,
    private val scope: CoroutineScope,
) : DataStore<Preferences> by context.dataStore {
    companion object {
        private val KEY_STORAGE_PATH = stringPreferencesKey("pref_storage_path")
        private val KEY_OPENED_DIR_PATH = stringPreferencesKey("pref_opened_dir_path")
        private val KEY_DOCK_GRAVITY = intPreferencesKey("pref_drawer_gravity")
        private val KEY_SPECIAL_CHARACTERS = stringPreferencesKey("pref_special_characters")
        private val KEY_TEXT_FORMATS = stringPreferencesKey("pref_text_formats")
        private val KEY_APP_ORIENTATION = stringPreferencesKey("pref_app_orientation")
        private val KEY_APP_THEME = stringPreferencesKey("pref_app_theme")
        private val KEY_DEEP_BLACK = booleanPreferencesKey("pref_deep_black")
        private val KEY_MAX_SIZE = longPreferencesKey("pref_max_size")
        private val KEY_MAX_DEPTH = intPreferencesKey("pref_max_depth")
        private val KEY_EXCLUDE_DIRS = booleanPreferencesKey("pref_exclude_dirs")
        private val KEY_USE_SU = booleanPreferencesKey("pref_use_su")
        private val KEY_EXPLORER_ITEM = intPreferencesKey("pref_explorer_item")
        private val KEY_JOYSTICK = intPreferencesKey("pref_joystick")
        private val KEY_TOYBOX = stringSetPreferencesKey("pref_toybox")
    }

    lateinit var initialPreferences: Preferences

    init {
        scope.launch {
            initialPreferences = data.first()
            data.collect {
                initialPreferences = it
            }
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

    val appTheme = getFlow(KEY_APP_THEME, AppTheme.defaultName()) {
        AppTheme.fromString(it, deepBlack.value)
    }

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

    private fun <V> getFlow(key: Preferences.Key<V>, default: V): SharedFlowProperty<V> {
        return data.map { it[key] ?: default }.shareInOne(scope).asProperty()
    }

    private fun <V,E> getFlow(key: Preferences.Key<V>, default: V, transformation: (V) -> E): SharedFlowProperty<E> {
        return data.map { (it[key] ?: default).let(transformation) }.shareInOne(scope).asProperty()
    }

    private fun <T> Flow<T>.shareInOne(scope: CoroutineScope) = shareIn(scope, SharingStarted.Lazily, replay = 1)
}
