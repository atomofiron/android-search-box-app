package ru.atomofiron.regextool.screens.finder
/*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView

import com.google.android.material.tabs.TabLayout

import java.util.ArrayList
import java.util.regex.Pattern

import ru.atomofiron.regextool.adapters.FilesAdapter
import ru.atomofiron.regextool.adapters.HistoryAdapter
import ru.atomofiron.regextool.adapters.ListAdapter
import ru.atomofiron.regextool.adapters.ViewPagerAdapter
import ru.atomofiron.regextool.screens.preferences.PrefsFragment
import ru.atomofiron.regextool.view.custom.RegexText
import ru.atomofiron.regextool.Util
import ru.atomofiron.regextool.MainActivity
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.SearchService
import ru.atomofiron.regextool.utils.PermissionHelper
import ru.atomofiron.regextool.utils.SnackbarHelper


class MainFragment2 : Fragment(), View.OnClickListener {

    private var co: Context? = null
    private var fragmentView: View? = null
    private var snackbarHelper: SnackbarHelper? = null

    private var regexText: RegexText? = null
    private var caseToggle: CheckBox? = null
    private var contentToggle: CheckBox? = null
    private var regexToggle: CheckBox? = null
    private var multilineToggle: CheckBox? = null
    private var viewPager: ViewPager? = null
    private var testField: EditText? = null
    private var filesListView: ListView? = null

    private var selectedListAdapter: ListAdapter? = null
    private var broadcastManager: LocalBroadcastManager? = null
    private var resultReceiver: Receiver? = null
    private var sp: SharedPreferences? = null
    private var historyAdapter: HistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        sp = Util.sp(co)

        resultReceiver = Receiver(context!!)
        broadcastManager = LocalBroadcastManager.getInstance(co!!)
        broadcastManager!!.registerReceiver(resultReceiver!!, IntentFilter(ACTION_RESULTS))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (fragmentView != null) {
            if (PrefsFragment.changedPrefs.remove(Const.PREF_SPECIAL_CHARACTERS))
                initCharacterButtons(fragmentView!!.findViewById<View>(R.id.characters_pane) as ViewGroup)

            return fragmentView
        }

        val view = inflater.inflate(R.layout.fragment_finder, container, false)

        view.findViewById(R.id.go).setOnClickListener(this)

        regexText = view.findViewById<View>(R.id.regex_text) as RegexText
        caseToggle = view.findViewById<View>(R.id.case_sense) as CheckBox
        contentToggle = view.findViewById<View>(R.id.in_files) as CheckBox
        regexToggle = view.findViewById<View>(R.id.simple_search) as CheckBox
        multilineToggle = view.findViewById<View>(R.id.multiline) as CheckBox

        caseToggle!!.setOnCheckedChangeListener { buttonView, isChecked -> regexText!!.setCaseSense(isChecked) }
        regexToggle!!.setOnCheckedChangeListener { buttonView, isChecked ->
            regexText!!.setRegex(isChecked)
            multilineToggle!!.isEnabled = isChecked && contentToggle!!.isChecked
        }
        contentToggle!!.setOnCheckedChangeListener { buttonView, isChecked -> multilineToggle!!.isEnabled = isChecked && regexToggle!!.isChecked }
        multilineToggle!!.setOnCheckedChangeListener { buttonView, isChecked -> regexText!!.setMultiline(isChecked) }

        initCharacterButtons(view.findViewById<View>(R.id.characters_pane) as ViewGroup)

        viewPager = view.findViewById(R.id.view_pager) as ViewPager
        val viewList = ArrayList<View>()

        val selectedListView = ListView(co)
        selectedListAdapter = ListAdapter(co)
        selectedListAdapter!!.update()
        selectedListView.adapter = selectedListAdapter
        selectedListView.onItemLongClickListener = selectedListAdapter
        selectedListView.onItemClickListener = selectedListAdapter

        filesListView = ListView(co)
        val filesListAdapter = FilesAdapter(co, filesListView!!)
        filesListView!!.adapter = filesListAdapter

        testField = LayoutInflater.from(co).inflate(R.layout.edittext_test, null) as EditText
        regexText!!.setTestField(testField)

        viewList.add(testField)
        viewList.add(selectedListView)
        viewList.add(filesListView)
        val pagerAdapter = ViewPagerAdapter(co, viewList)
        viewPager!!.adapter = pagerAdapter

        (view.findViewById(R.id.tab_layout) as TabLayout).setupWithViewPager(viewPager)

        viewPager!!.currentItem = 1
        viewPager!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                if (position == 1)
                    selectedListAdapter!!.update()
                else if (position == 2)
                    if (PermissionHelper.checkPerm(this@FinderFragment, REQUEST_FOR_PROVIDER))
                        filesListAdapter.updateSelected()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        snackbarHelper = SnackbarHelper(view)
        return view
    }

    private fun initCharacterButtons(pane: ViewGroup) {
        pane.removeAllViews()

        val characters = sp!!.getString(Const.PREF_SPECIAL_CHARACTERS, Const.DEFAULT_SPECIAL_CHARACTERS)!!
                .trim { it <= ' ' }.split("[ ]+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (characters.size > 0 && !characters[0].isEmpty())
            for (c in characters) {
                val view = LayoutInflater.from(co).inflate(R.layout.button_character, pane, false) as Button
                view.text = c
                view.setOnClickListener(this)
                pane.addView(view)
            }
    }

    fun setDrawerViewWithHistory(drawerView: DrawerLayout) {
        val historyList = drawerView.findViewById<ListView>(R.id.history_list)
        historyAdapter = HistoryAdapter(historyList, HistoryAdapter.OnItemClickListener { node ->
            regexText!!.setText(node)
            regexText!!.setSelection(node.length)
            drawerView.closeDrawer(GravityCompat.START)
        })
        historyList.adapter = historyAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_QUERY, regexText!!.text!!.toString())
        outState.putString(KEY_TEST, testField!!.text.toString())
        outState.putStringArrayList(KEY_SELECTED, selectedListAdapter!!.checkedPathArray)
        outState.putBoolean(KEY_FLAG_CASE, caseToggle!!.isChecked)
        outState.putBoolean(KEY_FLAG_IN_FILES, contentToggle!!.isChecked)
        outState.putBoolean(KEY_FLAG_REGEXP, regexToggle!!.isChecked)
        outState.putBoolean(KEY_FLAG_MULTILINE, multilineToggle!!.isChecked)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState == null)
            return

        regexText!!.setText(savedInstanceState.getString(KEY_QUERY, ""))
        testField!!.setText(savedInstanceState.getString(KEY_TEST, ""))
        selectedListAdapter!!.setCheckedPathsList(savedInstanceState.getStringArrayList(KEY_SELECTED))
        caseToggle!!.isChecked = savedInstanceState.getBoolean(KEY_FLAG_CASE)
        contentToggle!!.isChecked = savedInstanceState.getBoolean(KEY_FLAG_IN_FILES)
        regexToggle!!.isChecked = savedInstanceState.getBoolean(KEY_FLAG_REGEXP)
        multilineToggle!!.isChecked = savedInstanceState.getBoolean(KEY_FLAG_MULTILINE)
    }

    override fun onStart() {
        super.onStart()

        (filesListView!!.adapter as FilesAdapter).refreshIfNeeded()
    }

    fun checkListForSearch() {
        if (selectedListAdapter!!.checkedCount == 0)
            snackbarHelper!!.alpha_show(R.string.no_checked)
        else
            search()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_FOR_SEARCH)
                checkListForSearch()
            else if (requestCode == REQUEST_FOR_PROVIDER)
                (filesListView!!.adapter as FilesAdapter).refresh()
        } else if (requestCode == REQUEST_FOR_PROVIDER)
            if (!sp!!.getBoolean(Const.PREF_USE_SU, false))
                viewPager!!.currentItem = 1
    }

    fun search() {
        resultReceiver!!.counterView.text = "0/0"
        resultReceiver!!.processDialog.alpha_show()

        co!!.startService(Intent(co, SearchService::class.java)
                .putExtra(Const.CASE_SENSE, caseToggle!!.isChecked)
                .putExtra(Const.SEARCH_LIST, selectedListAdapter!!.checkedPathArray)
                .putExtra(Const.QUERY, regexText!!.text!!.toString())
                .putExtra(Const.SEARCH_IN_FILES, contentToggle!!.isChecked)
                .putExtra(Const.SEARCH_REGEX, regexToggle!!.isChecked)
                .putExtra(Const.MULTILINE, multilineToggle!!.isChecked))
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastManager!!.unregisterReceiver(resultReceiver!!)
        co!!.stopService(Intent(co, SearchService::class.java))
    }

    override fun onClick(v: View) {
        val symbol: String
        when (v.id) {
            R.id.go -> {
                val regex = regexText!!.text!!.toString()
                if (regex.isEmpty())
                    return

                historyAdapter!!.addItem(regex)

                if (regexToggle!!.isChecked)
                    try {
                        Pattern.compile(regex)
                    } catch (ignored: Exception) {
                        snackbarHelper!!.alpha_show(R.string.bad_ex)
                        return
                    }

                if (PermissionHelper.checkPerm(this@FinderFragment, REQUEST_FOR_SEARCH))
                    checkListForSearch()

                return
            }
            else -> symbol = (v as Button).text.toString()
        }
        val start = regexText!!.selectionStart
        val end = regexText!!.selectionEnd

        if (start != end)
            regexText!!.text!!.replace(start, end, "")

        regexText!!.text!!.insert(start, symbol)
        regexText!!.setSelection(start + symbol.length, start + symbol.length)
    }

    internal inner class Receiver(co: Context) : BroadcastReceiver() {
        private val processDialog: AlertDialog
        private val counterView: TextView
        private val currentView: TextView

        init {
            val view = LayoutInflater.from(this@FinderFragment.co).inflate(R.layout.layout_searching, null)
            counterView = view.findViewById(R.id.counter)
            currentView = view.findViewById(R.id.current)
            processDialog = AlertDialog.Builder(co)
                    .setView(view)
                    .setCancelable(false)
                    .setNeutralButton(R.string.cancel) { dialog, which ->
                        SearchService.needToSendResults = false
                        this@FinderFragment.co!!.stopService(Intent(this@FinderFragment.co, SearchService::class.java))
                    }
                    .setNegativeButton(R.string.stop) { dialog, which -> this@FinderFragment.co!!.stopService(Intent(this@FinderFragment.co, SearchService::class.java)) }
                    .create()
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (!intent.extras!!.containsKey(KEY_NOTICE)) {
                processDialog.cancel()

                val text: String
                val count = intent.getIntExtra(Const.SEARCH_COUNT, SEARCH_ERROR)
                when (count) {
                    SEARCH_ERROR -> {
                        snackbarHelper!!.alpha_show(intent.getStringExtra(KEY_ERROR_MESSAGE))

                        text = getString(R.string.error)
                    }
                    SEARCH_NOTHING -> snackbarHelper!!.alpha_show(text = getString(R.string.nothing))
                    else -> {
                        startActivity(
                                Intent(co, MainActivity::class.java)
                                        .setAction(MainActivity.ACTION_SHOW_RESULTS)
                                        .putExtras(intent.extras!!)
                        )

                        text = getString(R.string.results, count)
                    }
                }

                if (!isResumed)
                    showNotification(text)
            } else {
                counterView.text = intent.getStringExtra(KEY_NOTICE)
                currentView.text = intent.getStringExtra(KEY_NOTICE_CURRENT)
            }
        }

        private fun showNotification(text: String) {
            val notifier = co!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notifier != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notifier.createNotificationChannel(NotificationChannel(
                            Const.NOTIFICATION_CHANNEL_ID,
                            Const.NOTIFICATION_CHANNEL_ID,
                            NotificationManager.IMPORTANCE_DEFAULT)
                    )
                }

                notifier.notify(NOTIFICATION_ID, NotificationCompat.Builder(co!!, Const.NOTIFICATION_CHANNEL_ID)
                        .setTicker(getString(R.string.search_completed))
                        .setContentTitle(getString(R.string.search_completed))
                        .setContentText(text)
                        .setSmallIcon(R.drawable.ic_search_file_done)
                        .setColor(resources.getColor(R.color.colorPrimaryLight))
                        .setContentIntent(PendingIntent.getActivity(
                                co,
                                0,
                                Intent(co, MainActivity::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )).build()
                )
            }
        }
    }

    companion object {
        private val REQUEST_FOR_PROVIDER = 1
        private val REQUEST_FOR_SEARCH = 2

        val SEARCH_NOTHING = 0
        val SEARCH_ERROR = -1
        val NOTIFICATION_ID = 2

        val ACTION_RESULTS = "ACTION_RESULTS"
        val KEY_ERROR_MESSAGE = "KEY_ERROR_MESSAGE"
        val KEY_NOTICE = "KEY_NOTICE"
        val KEY_NOTICE_CURRENT = "KEY_NOTICE_CURRENT"

        private val KEY_QUERY = "KEY_QUERY"
        private val KEY_TEST = "KEY_TEST"
        private val KEY_SELECTED = "KEY_SELECTED"
        private val KEY_FLAG_CASE = "KEY_FLAG_CASE"
        private val KEY_FLAG_IN_FILES = "KEY_FLAG_IN_FILES"
        private val KEY_FLAG_REGEXP = "KEY_FLAG_REGEXP"
        private val KEY_FLAG_MULTILINE = "KEY_FLAG_MULTILINE"
    }
}
*/
