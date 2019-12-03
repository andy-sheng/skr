package com.module.mall.event

import com.module.mall.model.Price
import com.module.mall.model.ProductModel

class SelectMallStickyEvent(val productModel: ProductModel, val price: Price)