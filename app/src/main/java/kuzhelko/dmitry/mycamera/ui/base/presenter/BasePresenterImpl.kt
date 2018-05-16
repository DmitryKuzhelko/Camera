package kuzhelko.dmitry.mycamera.ui.base.presenter

import kuzhelko.dmitry.mycamera.ui.base.view.BaseView

abstract class BasePresenterImpl<V : BaseView> : BasePresenter<V> {

    private var view: V? = null

    override fun onAttach(view: V) {
        this.view = view
    }

    override fun onDetach() {
        view = null
    }

    override fun getView(): V? {
        return view
    }
}