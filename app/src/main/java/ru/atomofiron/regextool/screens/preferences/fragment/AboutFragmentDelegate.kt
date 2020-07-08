package ru.atomofiron.regextool.screens.preferences.fragment

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetDelegate
import ru.atomofiron.regextool.utils.Const

class AboutFragmentDelegate : BottomSheetDelegate(R.layout.sheet_about), View.OnClickListener {
    companion object {
        private const val ALPHA_ENABLED = 1f
        private const val ALPHA_DISABLED = 0.5f
    }
    private val githubIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.GITHUB_URL))
    private val forpdaIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Const.FORPDA_URL))

    private val tvGithub: TextView get() = bottomSheetView.findViewById(R.id.about_tv_github)
    private val tvForpda: TextView get() = bottomSheetView.findViewById(R.id.about_tv_forpda)

    public override fun show() = super.show()

    override fun onViewReady() {
        var componentName = githubIntent.resolveActivity(context.packageManager)
        tvGithub.isEnabled = componentName != null
        tvGithub.alpha = if (componentName == null) ALPHA_DISABLED else ALPHA_ENABLED

        componentName = forpdaIntent.resolveActivity(context.packageManager)
        tvForpda.isEnabled = componentName != null
        tvForpda.alpha = if (componentName == null) ALPHA_DISABLED else ALPHA_ENABLED

        tvGithub.setOnClickListener(this)
        tvForpda.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.about_tv_github -> context.startActivity(githubIntent)
            R.id.about_tv_forpda -> context.startActivity(forpdaIntent)
        }
    }
}