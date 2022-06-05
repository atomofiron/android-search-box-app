package app.atomofiron.searchboxapp.screens.viewer

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModel
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsModelDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import javax.inject.Inject

class TextViewerViewModel : BaseViewModel<TextViewerComponent, TextViewerFragment, TextViewerPresenter>(),
    FinderItemsModel by FinderItemsModelDelegate() {
    companion object {
        const val UNDEFINED = -1
    }

    @Inject
    override lateinit var presenter: TextViewerPresenter
    private lateinit var params: TextViewerParams

    override fun inject(view: TextViewerFragment) {
        params = TextViewerParams.params(view.requireArguments())
        super.inject(view)
        component.inject(this)
    }

    override fun createComponent(fragmentProperty: WeakProperty<Fragment>) = DaggerTextViewerComponent
        .builder()
        .bind(this)
        .bind(fragmentProperty)
        .bind(viewModelScope)
        .bind(params)
        .dependencies(DaggerInjector.appComponent)
        .build()

    val insertInQuery = dataFlow<String>(single = true)

    val textLines = dataFlow<List<TextLine>>()
    /** line index -> line matches */
    val matchesMap = dataFlow<Map<Int, List<TextLineMatch>>>()
    /** match counter -> matches quantity */
    val matchesCounter = dataFlow<Long?>(null)
    /** line index -> line match index */
    val matchesCursor = dataFlow<Long?>(null)
    val loading = dataFlow(value = true)
    lateinit var composition: ExplorerItemComposition
    lateinit var xFile: XFile

    private var matchesIndex = -1
    /** line index -> line matches */
    var lineIndexMatches: List<LineIndexMatches>? = null

    val currentLineIndexCursor: Int? get() = matchesCursor.value?.shr(32)?.toInt()

    /** @return true если есть на что переключаться, иначе нужно догрузить файл. */
    fun changeCursor(increment: Boolean): Boolean {
        val cursor = matchesCursor.value
        val matches = lineIndexMatches!!
        when (cursor) {
            null -> {
                if (matches.isEmpty()) {
                    return false
                }
                val lineIndex = matches.first().lineIndex.toLong()
                matchesCursor.value = lineIndex.shl(32)
                matchesIndex = 0
            }
            else -> {
                val lineIndex = cursor.shr(32).toInt()
                var matchIndex = cursor.toInt()
                val lineMatches = matches[matchesIndex].lineMatches
                when {
                    increment && matchIndex.inc() == lineMatches.size -> {
                        if (matchesIndex.inc() == matches.size) {
                            return false
                        }
                        val lineIndexMatches = matches[++matchesIndex]
                        matchesCursor.value = lineIndexMatches.lineIndex.toLong().shl(32)
                    }
                    increment -> {
                        matchesCursor.value = lineIndex.toLong().shl(32) + matchIndex.inc().toLong()
                    }
                    !increment && matchIndex == 0 -> {
                        val lineIndexMatches = matches[--matchesIndex]
                        matchIndex = lineIndexMatches.lineMatches.size.dec()
                        matchesCursor.value = lineIndexMatches.lineIndex.toLong().shl(32) + matchIndex.toLong()
                    }
                    !increment -> {
                        matchesCursor.value = lineIndex.toLong().shl(32) + matchIndex.dec().toLong()
                    }
                }
            }
        }
        val counter = matchesCounter.value!!
        val index = when {
            increment -> counter.shr(32).inc().shl(32)
            else -> counter.shr(32).dec().shl(32)
        }
        val count = counter.toInt().toLong()
        matchesCounter.value = index + count
        return true
    }

    fun setTasks(tasks: List<FinderTask>) {
        val items = tasks.map { FinderStateItem.ProgressItem(it) }
        progressItems.clear()
        progressItems.addAll(items)
        updateState()
    }
}