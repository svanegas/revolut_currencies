package com.svanegas.revolut.currencies.base.arch.statefullayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.svanegas.revolut.currencies.base.arch.BR
import com.svanegas.revolut.currencies.base.arch.BaseView
import com.svanegas.revolut.currencies.base.arch.BaseViewModel
import com.svanegas.revolut.currencies.base.arch.R
import com.svanegas.revolut.currencies.base.logException
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class StatefulLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(CONTENT, PROGRESS, OFFLINE, EMPTY, ERROR)
    annotation class State

    companion object {
        const val CONTENT = 0
        const val PROGRESS = 1
        const val OFFLINE = 2
        const val EMPTY = 3
        const val ERROR = 4

        private val stateToAttribute = mapOf(
            PROGRESS to R.styleable.StatefulLayout_progressLayout,
            OFFLINE to R.styleable.StatefulLayout_offlineLayout,
            EMPTY to R.styleable.StatefulLayout_emptyLayout,
            ERROR to R.styleable.StatefulLayout_errorLayout
        )
    }

    @State
    private val initialState: Int
    private val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }
    private val contentLayoutList: MutableList<View> by lazy { ArrayList<View>() }
    private var isContentSet = false

    private val layoutIds: MutableMap<Int, Int> = HashMap()
    private val inflatedLayouts: MutableMap<Int, Pair<View, ViewDataBinding?>> = HashMap()

    @State
    @get:State
    var state: Int = 0
        set(@State toState) {
            if (toState != CONTENT && !layoutIds.containsKey(toState)) {
                logException("Setting state which is not used in XML (state=$toState)")
            }

            field = toState

            switchLayout(toState)
        }

    private fun switchLayout(toState: Int) {
        contentLayoutList.forEach { it.visibility = determineVisibility(toState == CONTENT) }

        inflatedLayouts.forEach { (viewState, viewToBinding) ->
            viewToBinding.first.visibility = determineVisibility(toState == viewState)
        }
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StatefulLayout)

        // parse states from attributes
        stateToAttribute.forEach { (viewState, attribute) ->
            if (!typedArray.hasValue(attribute)) {
                return@forEach // skips current attribute
            }

            val layoutId = typedArray.getResourceId(attribute, 0)
            if (layoutId != 0) {
                layoutIds[viewState] = layoutId
            }
        }

        if (layoutIds.isEmpty()) {
            throw IllegalArgumentException("At least one state layout must be set")
        }

        initialState = typedArray.getInt(R.styleable.StatefulLayout_state, CONTENT)

        typedArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupView()
    }

    private fun setupView() {
        if (isInEditMode || isContentSet) {
            return
        }

        // adds all content children to list
        (0 until childCount).forEach { contentLayoutList.add(getChildAt(it)) }

        // adds all state views
        layoutIds.forEach { (viewState, layoutId) ->
            val viewToBinding = inflateAndBindIfPossible(layoutId)
            inflatedLayouts[viewState] = viewToBinding
            addView(viewToBinding.first)
        }
        state = initialState
        isContentSet = true
    }

    private fun inflateAndBindIfPossible(layoutId: Int): Pair<View, ViewDataBinding?> {
        // inflates regular layout
        val view = layoutInflater.inflate(layoutId, this, false)

        return try {
            // attempt to set databinding to it
            val binding = DataBindingUtil.bind<ViewDataBinding>(view)
            view to binding
        } catch (e: IllegalArgumentException) {
            // if fails, we have just regular layout
            view to null
        }
    }

    private fun determineVisibility(visible: Boolean): Int = when {
        visible -> View.VISIBLE
        else -> View.GONE
    }

    object StatefulLayoutBindingAdapters {
        @JvmStatic
        @BindingAdapter("app:bindView", "app:bindViewModel", requireAll = false)
        fun bindParentListeners(
            view: StatefulLayout,
            viewListener: BaseView?,
            viewModelListener: BaseViewModel?
        ) {
            view.inflatedLayouts
                .filter { it.value.second != null }
                .map { it.value.second!! }
                .forEach { binding ->
                    binding.setVariableOptionally(BR.view, viewListener)
                    binding.setVariableOptionally(BR.viewModel, viewModelListener)
                }
        }

        private fun <T> ViewDataBinding.setVariableOptionally(variable: Int, bindTo: T?) {
            if (bindTo == null) {
                return
            }

            val isDeclared = setVariable(variable, bindTo)
            if (!isDeclared) {
                Timber.e("Trying to bind view, but <variable> tag is not set int layout!")
            }
        }
    }
}
