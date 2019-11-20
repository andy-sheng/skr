package com.module.mall.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.module.home.R
import com.module.mall.adapter.PackageAdapter

class PackageView : ConstraintLayout {
    var recyclerView: RecyclerView

    var productAdapter: PackageAdapter? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.product_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        productAdapter = PackageAdapter()
        recyclerView.adapter = productAdapter
        productAdapter?.notifyDataSetChanged()
    }

    fun tryLoad() {

    }
}