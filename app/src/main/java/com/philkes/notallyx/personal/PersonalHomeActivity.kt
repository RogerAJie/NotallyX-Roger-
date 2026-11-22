package com.philkes.notallyx.personal

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.philkes.notallyx.R
import com.philkes.notallyx.databinding.ActivityPersonalHomeBinding
import com.philkes.notallyx.databinding.RowPersonalMemoBinding
import com.philkes.notallyx.databinding.RowPersonalTaskBinding

/**
 * Roger's simple landing screen: two tabs, without folders, dates or reminders.
 */
class PersonalHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalHomeBinding
    private val store by lazy { PersonalNotesStore(this) }
    private var tabIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityPersonalHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }

        binding.PersonalList.layoutManager = LinearLayoutManager(this)
        binding.PersonalTabs.apply {
            addTab(newTab().setText(R.string.personal_memos))
            addTab(newTab().setText(R.string.personal_tasks))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tabIndex = tab.position
                    refresh()
                }
                override fun onTabUnselected(tab: TabLayout.Tab) = Unit
                override fun onTabReselected(tab: TabLayout.Tab) = Unit
            })
        }
        binding.PersonalAdd.setOnClickListener {
            if (tabIndex == 0) {
                startActivity(Intent(this, PersonalMemoEditorActivity::class.java))
            } else {
                showTaskDialog(null)
            }
        }
        tabIndex = savedInstanceState?.getInt(STATE_TAB, 0) ?: 0
        binding.PersonalTabs.getTabAt(tabIndex)?.select()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_TAB, tabIndex)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        store.close()
    }

    private fun refresh() {
        if (tabIndex == 0) {
            val notes = store.getMemos()
            binding.PersonalList.adapter = MemoAdapter(notes)
            binding.PersonalEmpty.setText(R.string.personal_empty_memos)
            binding.PersonalEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        } else {
            val tasks = store.getTasks()
            binding.PersonalList.adapter = TaskAdapter(tasks)
            binding.PersonalEmpty.setText(R.string.personal_empty_tasks)
            binding.PersonalEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showTaskDialog(task: PersonalTask?) {
        val field = EditText(this).apply {
            setText(task?.body.orEmpty())
            hint = getString(R.string.personal_task_hint)
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 2
            maxLines = 5
            setSelection(text.length)
        }
        val gap = (20 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this).apply {
            setPadding(gap, 0, gap, 0)
            addView(field)
        }
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(if (task == null) R.string.personal_new_task else R.string.personal_edit_task)
            .setView(container)
            .setNegativeButton(R.string.personal_cancel, null)
            .setPositiveButton(R.string.personal_save) { _, _ ->
                val newText = field.text.toString().trim()
                if (newText.isNotEmpty()) {
                    if (task == null) store.addTask(newText)
                    else store.updateTask(task.id, newText)
                    refresh()
                }
            }
        if (task != null) {
            builder.setNeutralButton(R.string.personal_delete) { _, _ ->
                confirmDeleteTask(task)
            }
        }
        builder.show()
    }

    private fun confirmDeleteTask(task: PersonalTask) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.personal_delete_task_confirm)
            .setNegativeButton(R.string.personal_cancel, null)
            .setPositiveButton(R.string.personal_delete) { _, _ ->
                store.deleteTask(task.id)
                refresh()
            }
            .show()
    }

    private fun confirmDeleteMemo(memo: PersonalMemo) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.personal_delete_memo_confirm)
            .setNegativeButton(R.string.personal_cancel, null)
            .setPositiveButton(R.string.personal_delete) { _, _ ->
                store.deleteMemo(memo.id)
                refresh()
            }
            .show()
    }

    private inner class MemoAdapter(private val notes: List<PersonalMemo>) :
        RecyclerView.Adapter<MemoAdapter.Holder>() {

        inner class Holder(val row: RowPersonalMemoBinding) : RecyclerView.ViewHolder(row.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
            Holder(RowPersonalMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = notes.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val memo = notes[position]
            holder.row.MemoTitle.text =
                memo.title.ifBlank { PersonalNotesStore.titleFromFirstSentence(memo.body) }
            holder.row.MemoPreview.text = memo.body
            holder.row.MemoPreview.visibility = if (memo.body.isBlank()) View.GONE else View.VISIBLE
            holder.row.root.setOnClickListener {
                startActivity(
                    Intent(this@PersonalHomeActivity, PersonalMemoEditorActivity::class.java)
                        .putExtra(PersonalMemoEditorActivity.EXTRA_MEMO_ID, memo.id)
                )
            }
            holder.row.root.setOnLongClickListener {
                confirmDeleteMemo(memo)
                true
            }
        }
    }

    private inner class TaskAdapter(private val tasks: List<PersonalTask>) :
        RecyclerView.Adapter<TaskAdapter.Holder>() {

        inner class Holder(val row: RowPersonalTaskBinding) : RecyclerView.ViewHolder(row.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
            Holder(RowPersonalTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = tasks.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val task = tasks[position]
            holder.row.TaskCheckbox.setOnCheckedChangeListener(null)
            holder.row.TaskCheckbox.isChecked = task.completed
            holder.row.TaskBody.text = task.body
            holder.row.TaskBody.setTextColor(if (task.completed) 0xFFAAAAAA.toInt() else 0xFF222222.toInt())
            holder.row.TaskBody.paintFlags =
                if (task.completed) holder.row.TaskBody.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else holder.row.TaskBody.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.row.TaskCheckbox.setOnCheckedChangeListener { _, checked ->
                store.setTaskCompleted(task.id, checked)
                refresh()
            }
            holder.row.TaskBody.setOnClickListener { showTaskDialog(task) }
            holder.row.root.setOnLongClickListener {
                confirmDeleteTask(task)
                true
            }
        }
    }

    companion object {
        private const val STATE_TAB = "personal_selected_tab"
    }
}
