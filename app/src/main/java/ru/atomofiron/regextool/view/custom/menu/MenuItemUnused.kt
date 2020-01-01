package ru.atomofiron.regextool.view.custom.menu

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.*
import ru.atomofiron.regextool.log

class MenuItemUnused(private val context: Context) : MenuItem {
    private val resources: Resources = context.resources

    private var id: Int = 0
    private var isEnabled: Boolean = true
    private var isChecked: Boolean = false
    private var isVisible: Boolean = true
    private var isCheckable: Boolean = false
    private var title: CharSequence? = null
    private var icon: Drawable? = null

    private var onMenuItemClickListener: MenuItem.OnMenuItemClickListener? = null

    override fun expandActionView(): Boolean {
        log("UNSUPPORTED expandActionView()")
        return false
    }

    override fun hasSubMenu(): Boolean = false

    override fun getMenuInfo(): ContextMenu.ContextMenuInfo? {
        log("UNSUPPORTED getMenuInfo()")
        return null
    }

    override fun getItemId(): Int = id

    override fun getAlphabeticShortcut(): Char {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setEnabled(enabled: Boolean): MenuItem {
        isEnabled = enabled
        return this
    }

    override fun setTitle(title: CharSequence?): MenuItem {
        this.title = title
        return this
    }

    override fun setTitle(title: Int): MenuItem {
        this.title = resources.getString(title)
        return this
    }

    override fun setChecked(checked: Boolean): MenuItem {
        isChecked = checked
        return this
    }

    override fun getActionView(): View? {
        log("UNSUPPORTED getActionView()")
        return null
    }

    override fun getTitle(): CharSequence? = title

    override fun getOrder(): Int {
        log("UNSUPPORTED getOrder()")
        return 0
    }

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem {
        log("UNSUPPORTED setOnActionExpandListener()")
        return this
    }

    override fun getIntent(): Intent? = null

    override fun setVisible(visible: Boolean): MenuItem {
        isVisible = visible
        return this
    }

    override fun isEnabled(): Boolean = isEnabled

    override fun isCheckable(): Boolean = isCheckable

    override fun setShowAsAction(actionEnum: Int) {
        log("UNSUPPORTED setShowAsAction()")
    }

    override fun getGroupId(): Int {
        log("UNSUPPORTED getGroupId()")
        return 0
    }

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem {
        log("UNSUPPORTED setActionProvider()")
        return this
    }

    override fun setTitleCondensed(title: CharSequence?): MenuItem {
        log("UNSUPPORTED setTitleCondensed()")
        return this
    }

    override fun getNumericShortcut(): Char {
        log("UNSUPPORTED getNumericShortcut()")
        return '0'
    }

    override fun isActionViewExpanded(): Boolean {
        log("UNSUPPORTED isActionViewExpanded()")
        return false
    }

    override fun collapseActionView(): Boolean {
        log("UNSUPPORTED collapseActionView()")
        return false
    }

    override fun isVisible(): Boolean = isVisible

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        log("UNSUPPORTED setNumericShortcut()")
        return this
    }

    override fun setActionView(view: View?): MenuItem {
        log("UNSUPPORTED setActionView()")
        return this
    }

    override fun setActionView(resId: Int): MenuItem {
        log("UNSUPPORTED setActionView()")
        return this
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        log("UNSUPPORTED setAlphabeticShortcut()")
        return this
    }

    override fun setIcon(icon: Drawable?): MenuItem {
        this.icon = icon
        return this
    }

    override fun setIcon(iconRes: Int): MenuItem {
        this.icon = resources.getDrawable(iconRes, context.theme)
        return this
    }

    override fun isChecked(): Boolean = isChecked

    override fun setIntent(intent: Intent?): MenuItem {
        log("UNSUPPORTED setIntent()")
        return this
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        log("UNSUPPORTED setShortcut()")
        return this
    }

    override fun getIcon(): Drawable? = icon

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        log("UNSUPPORTED setShowAsActionFlags()")
        return this
    }

    override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener?): MenuItem {
        onMenuItemClickListener = menuItemClickListener
        return this
    }

    override fun getActionProvider(): ActionProvider? {
        log("UNSUPPORTED getActionProvider()")
        return null
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        isCheckable = checkable
        return this
    }

    override fun getSubMenu(): SubMenu? {
        log("UNSUPPORTED getSubMenu()")
        return null
    }

    override fun getTitleCondensed(): CharSequence? {
        log("UNSUPPORTED getTitleCondensed()")
        return null
    }
}