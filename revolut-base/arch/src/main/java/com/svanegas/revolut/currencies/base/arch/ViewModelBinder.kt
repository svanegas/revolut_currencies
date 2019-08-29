package com.svanegas.revolut.currencies.base.arch

import android.view.LayoutInflater
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner

interface ViewModelBinder<V : BaseViewModel, B : ViewDataBinding> : LifecycleOwner, BaseView {
    var binding: B
    var viewModel: V
    val currentFragmentManager: FragmentManager

    fun inflateBindingLayout(inflater: LayoutInflater): B
    fun setupViewModel(): V
    fun getViewLifecycleOwner(): LifecycleOwner

    fun setupBinding(inflater: LayoutInflater): B = inflateBindingLayout(inflater).apply {
        lifecycleOwner = getViewLifecycleOwner()
        setVariable(BR.view, this@ViewModelBinder)
        setVariable(BR.viewModel, viewModel)
    }
}