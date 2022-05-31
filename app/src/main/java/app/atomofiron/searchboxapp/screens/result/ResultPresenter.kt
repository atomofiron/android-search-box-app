package app.atomofiron.searchboxapp.screens.result

import androidx.core.os.ConfigurationCompat
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.value
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.injectable.store.ResultStore
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.screens.result.presenter.BottomSheetMenuListenerDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.utils.Const
import java.text.SimpleDateFormat
import java.util.*

class ResultPresenter(
    params: ResultPresenterParams,
    viewModel: ResultViewModel,
    private val resultStore: ResultStore,
    private val finderStore: FinderStore,
    private val preferenceStore: PreferenceStore,
    private val interactor: ResultInteractor,
    router: ResultRouter,
    private val resultChannel: ResultChannel,
    appStore: AppStore,
    itemActionDelegate: ResultItemActionDelegate,
    private val menuListenerDelegate: BottomSheetMenuListenerDelegate
) : BasePresenter<ResultViewModel, ResultRouter>(viewModel, router),
        ResultItemActionListener by itemActionDelegate,
        MenuListener by menuListenerDelegate {
    companion object {
        private const val UNDEFINED = -1L
    }
    private val taskId = params.taskId

    private val resources by appStore.resourcesProperty

    init {
        onSubscribeData()

        //if (taskId == UNDEFINED) {
            val task = finderStore.tasks.find { it.id == taskId }
            // todo start task here
            if (task == null) {
                logE("No task found!")
                router.navigateBack()
            } else {
                viewModel.task.value = task.copyTask()
            }
        //}
    }

    override fun onSubscribeData() {
        finderStore.notifications.collect(scope) { update ->
            if (taskId != UNDEFINED) {
                scope.launch {
                    viewModel.updateState(update)
                }
            }
        }
        preferenceStore.explorerItemComposition.collect(scope) {
            viewModel.composition.value = it
        }
        resultStore.itemsShellBeDeleted.collect(scope) {
            scope.launch {
                viewModel.updateState()
            }
        }
        resultChannel.notifyItemChanged.collect(scope) {
            scope.launch {
                viewModel.notifyItemChanged.value = it
            }
        }
    }

    fun onStopClick() = interactor.stop(viewModel.task.value.uuid)

    fun onOptionsClick() = viewModel.run {
        val ids = when (viewModel.checked.size) {
            1 -> viewModel.oneFileOptions
            else -> viewModel.manyFilesOptions
        }
        val options = ExplorerItemOptions(ids, viewModel.checked, viewModel.composition.value)
        menuListenerDelegate.showOptions(options)
    }

    fun onExportClick() {
        val task = viewModel.task.value

        val data = StringBuilder()
        for (result in task.results) {
            data.append(result.toMarkdown())
        }
        val locale = ConfigurationCompat.getLocales(resources.configuration)[0]
        val date = SimpleDateFormat(Const.DATE_PATTERN, locale).format(Date())
        val title = "search_$date.md.txt";

        if (!router.shareFile(title, data.toString())) {
            viewModel.alerts.value = resources.getString(R.string.no_activity)
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