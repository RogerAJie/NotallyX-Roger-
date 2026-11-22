package com.philkes.notallyx.personal

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.philkes.notallyx.databinding.ActivityPersonalMemoEditorBinding

/** Full-page, multiline memo editor. The memo is saved locally on leaving the page. */
class PersonalMemoEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalMemoEditorBinding
    private val store by lazy { PersonalNotesStore(this) }
    private var memoId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityPersonalMemoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(0, bars.top, 0, maxOf(bars.bottom, keyboard.bottom))
            insets
        }

        memoId = savedInstanceState?.getLong(STATE_ID)
            ?: intent.getLongExtra(EXTRA_MEMO_ID, -1L)
        if (savedInstanceState == null && memoId > 0) {
            store.getMemo(memoId)?.let { memo ->
                binding.EditorTitle.setText(memo.title)
                binding.EditorBody.setText(memo.body)
            }
        }

        binding.EditorBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.EditorSave.setOnClickListener {
            persist()
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Persist first so a rotation cannot create a duplicate new memo.
        persist()
        outState.putLong(STATE_ID, memoId)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        persist()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        store.close()
    }

    private fun persist() {
        if (!::binding.isInitialized) return
        val title = binding.EditorTitle.text.toString()
        val body = binding.EditorBody.text.toString()
        if (title.isBlank() && body.isBlank() && memoId <= 0) return
        memoId = store.saveMemo(if (memoId > 0) memoId else null, title, body)
    }

    companion object {
        const val EXTRA_MEMO_ID = "personal.memo.id"
        private const val STATE_ID = "personal.memo.editor.id"
    }
}
