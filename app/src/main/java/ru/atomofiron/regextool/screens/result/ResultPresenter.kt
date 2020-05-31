package ru.atomofiron.regextool.screens.result

import android.content.Context
import android.content.Intent
import androidx.core.os.ConfigurationCompat
import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import ru.atomofiron.regextool.injectable.channel.ResultChannel
import ru.atomofiron.regextool.injectable.interactor.ResultInteractor
import ru.atomofiron.regextool.injectable.store.FinderStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.injectable.store.ResultStore
import ru.atomofiron.regextool.logE
import ru.atomofiron.regextool.model.other.ExplorerItemOptions
import ru.atomofiron.regextool.screens.result.ResultFragment.Companion.KEY_TASK_ID
import ru.atomofiron.regextool.screens.result.adapter.ResultItemActionListener
import ru.atomofiron.regextool.screens.result.presenter.BottomSheetMenuListenerDelegate
import ru.atomofiron.regextool.screens.result.presenter.ResultItemActionDelegate
import ru.atomofiron.regextool.utils.Const
import java.text.SimpleDateFormat
import java.util.*

class ResultPresenter(
        viewModel: ResultViewModel,
        private val scope: CoroutineScope,
        private val resultStore: ResultStore,
        private val finderStore: FinderStore,
        private val preferenceStore: PreferenceStore,
        private val interactor: ResultInteractor,
        router: ResultRouter,
        private val resultChannel: ResultChannel,
        itemActionDelegate: ResultItemActionDelegate,
        menuListenerDelegate: BottomSheetMenuListenerDelegate
) : BasePresenter<ResultViewModel, ResultRouter>(viewModel, router),
        ResultItemActionListener by itemActionDelegate,
        BottomSheetMenuListener by menuListenerDelegate {
    companion object {
        private const val UNDEFINED = -1L
    }
    private var taskId = UNDEFINED

    init {
        onSubscribeData()
    }

    override fun onCreate(context: Context, intent: Intent) {
        if (taskId == UNDEFINED) {
            taskId = intent.getLongExtra(KEY_TASK_ID, UNDEFINED)
            val task = finderStore.tasks.find { it.id == taskId }
            if (task == null) {
                logE("No task found!")
                router.popScreen()
            } else {
                viewModel.task.value = task.copyTask()
            }
        }
    }

    override fun onSubscribeData() {
        super.onSubscribeData()
        finderStore.notifications.addObserver(onClearedCallback) { update ->
            if (taskId != UNDEFINED) {
                scope.launch {
                    viewModel.updateState(update)
                }
            }
        }
        preferenceStore.explorerItemComposition.addObserver(onClearedCallback) {
            viewModel.composition.value = it
        }
        resultStore.itemsShellBeDeleted.addObserver(onClearedCallback) {
            scope.launch {
                viewModel.updateState()
            }
        }
        resultChannel.notifyItemChanged.addObserver(onClearedCallback) {
            scope.launch {
                viewModel.notifyItemChanged.invoke(it)
            }
        }
    }

    fun onStopClick() = interactor.stop(viewModel.task.value.uuid)

    fun onOptionsClick() {
        val ids = when (viewModel.checked.size) {
            1 -> viewModel.oneFileOptions
            else -> viewModel.manyFilesOptions
        }
        val options = ExplorerItemOptions(ids, viewModel.checked, viewModel.composition.value)
        viewModel.showOptions.invoke(options)
    }

    fun onExportClick() {
        val task = viewModel.task.value
        val context = viewModel.context
        val resources = context.resources

        val data = StringBuilder()
        for (result in task.results) {
            data.append(result.toMarkdown())
        }
        val locale = ConfigurationCompat.getLocales(resources.configuration)[0]
        val date = SimpleDateFormat(Const.DATE_PATTERN, locale).format(Date())
        val title = "search_$date.md.txt";

        if (!router.shareFile(title, data.toString())) {
            viewModel.alerts.invoke(resources.getString(R.string.no_activity))
        }
    }

    fun onDropTaskErrorClick() {
        interactor.dropTaskError(taskId)
    }
}

/*

				((android.content.ClipboardManager) ac.getSystemService(Context.CLIPBOARD_SERVICE))
						.setPrimaryClip(android.content.ClipData.newPlainText("RegexFinder", str));
				snackbarHelper.show(R.string.copied);



			ResultsHolder.setResult(resultsList.get(position));
			startActivity(
					new Intent(ac, MainActivity.class)
							.setAction(MainActivity.ACTION_SHOW_RESULT)
			);
		} else
			try {
				Uri uri = Build.VERSION.SDK_INT < 24 ? Uri.fromFile(file) :
						FileProvider.getUriForFile(ac, ac.getApplicationContext().getPackageName() + ".provider", file);
				Intent intent = new Intent()
						.setAction(android.content.Intent.ACTION_VIEW)
						.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(format));

				if (intent.resolveActivity(ac.getPackageManager()) != null)
					startActivity(intent);
				else
					snackbarHelper.show(R.string.no_activity);
			} catch (Exception e) {
				if (e.getMessage().startsWith("Failed to find configured root that contains")) {
					snackbarHelper.showLong(R.string.fucking_provider);
					showPathWithCopyAction(file.getAbsolutePath());
				} else
					snackbarHelper.show(R.string.error);
			}
 */