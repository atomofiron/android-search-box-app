package app.atomofiron.searchboxapp.screens.explorer

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.databinding.FragmentExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.custom.OrientationLayoutDelegate
import app.atomofiron.searchboxapp.recyclerView
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerPagerAdapter
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.setContentMaxWidthRes
import app.atomofiron.searchboxapp.utils.getString
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewState, ExplorerPresenter> by BaseFragmentImpl(),
    KeyCodeConsumer
{
    private lateinit var binding: FragmentExplorerBinding
    private lateinit var pagerAdapter: ExplorerPagerAdapter
    private val explorerViews get() = pagerAdapter.items
    private val tabIds = arrayOf(R.id.first_button, R.id.second_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ExplorerViewModel::class, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExplorerBinding.bind(view)
        pagerAdapter = ExplorerPagerAdapter(binding.pager, presenter)
        binding.initView()
        viewState.onViewCollect()
        onApplyInsets(view)
    }

    private fun FragmentExplorerBinding.initView() {
        pager.adapter = pagerAdapter
        bottomBar.setContentMaxWidthRes(R.dimen.bottom_bar_max_width)
        bottomBar.isItemActiveIndicatorEnabled = false
        bottomBar.setOnItemSelectedListener(::onNavigationItemSelected)
        binding.navigationRail.menu.removeItem(R.id.stub)
        navigationRail.setOnItemSelectedListener(::onNavigationItemSelected)
        navigationRail.isItemActiveIndicatorEnabled = false
        pager.recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                presenter.onTabSelected(position)
                explorerTabs.check(tabIds[position])
            }
        })

        explorerTabs.addOnButtonCheckedListener { group, id, isChecked ->
            if (!isChecked && group.checkedButtonId == View.NO_ID) {
                getCurrentTabView().scrollToTop()
                group.check(id)
            }
            if (isChecked) {
                pager.currentItem = tabIds.indexOf(id)
            }
        }
        explorerTabs.setOnClickListener {
            getCurrentTabView().scrollToTop()
        }

        val textColors = ContextCompat.getColorStateList(requireContext(), R.color.redio_text_button_foreground_color_selector)
        firstButton.setTextColor(textColors)
        secondButton.setTextColor(textColors)
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> presenter.onSearchOptionSelected()
            R.id.menu_settings -> presenter.onSettingsOptionSelected()
        }
        return false
    }

    override fun ExplorerViewState.onViewCollect() {
        //viewCollect(actions, collector = explorerAdapter::onAction)
        viewCollect(firstTabItems) {
            val first = explorerViews.first()
            first.submitList(it)
            binding.firstButton.text = first.title ?: getString(R.string.dash)
        }
        viewCollect(secondTabItems) {
            val second = explorerViews.last()
            second.submitList(it)
            binding.secondButton.text = second.title ?: getString(R.string.dash)
        }
        viewCollect(itemComposition) { composition ->
            explorerViews.forEach { it.setComposition(composition) }
        }
        viewCollect(permissionRequiredWarning, collector = ::showPermissionRequiredWarning)
        viewCollect(scrollTo) { item ->
            getCurrentTabView().scrollTo(item)
        }
        viewCollect(alerts, collector = ::showAlert)
        viewCollect(currentTab) {
            binding.pager.currentItem = it.index
        }
    }

    override fun onApplyInsets(root: View) {
        binding.explorerTabs.applyPaddingInsets(start = true, top = true, end = true)
        binding.bottomBar.applyPaddingInsets(start = true, bottom = true, end = true)
        binding.navigationRail.applyPaddingInsets()
        binding.run {
            OrientationLayoutDelegate(
                binding.root,
                explorerViews,
                bottomView = bottomBar,
                railView = navigationRail,
                tabLayout = explorerTabs,
            )
        }
    }

    override fun onKeyDown(keyCode: Int): Boolean = when {
        !isVisible -> false
        keyCode != KeyEvent.KEYCODE_VOLUME_UP -> false
        else -> getCurrentTabView().isCurrentDirVisible()?.also {
            presenter.onVolumeUp(it)
        } != null
    }

    private fun getCurrentTabView(): ExplorerView = explorerViews[binding.pager.currentItem]

    private fun showPermissionRequiredWarning(unit: Unit) {
        val view = view ?: return
        Snackbar.make(view, R.string.access_to_storage_forbidden, Snackbar.LENGTH_LONG)
            .setAnchorView(view)
            .setAction(R.string.allow) { presenter.onAllowStorageClick() }
            .show()
    }

    private fun showAlert(error: NodeError) {
        val view = view ?: return
        Snackbar.make(view, resources.getString(error), Snackbar.LENGTH_LONG)
            .setAnchorView(view)
            .setAction(R.string.allow) { presenter.onAllowStorageClick() }
            .show()
    }
}