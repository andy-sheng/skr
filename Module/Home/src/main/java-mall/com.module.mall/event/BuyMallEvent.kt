package com.module.mall.event

import com.module.mall.model.Price
import com.module.mall.model.ProductModel

class BuyMallEvent(val productModel: ProductModel, val price: Price)