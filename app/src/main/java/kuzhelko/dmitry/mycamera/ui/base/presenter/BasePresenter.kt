package kuzhelko.dmitry.mycamera.ui.base.presenter

import kuzhelko.dmitry.mycamera.ui.base.view.BaseView

interface BasePresenter<V : BaseView> {

    fun onAttach(view: V)

    fun onDetach()

    fun getView(): V?
}