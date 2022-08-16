package ti.iap.models

import com.android.billingclient.api.SkuDetails
import org.appcelerator.kroll.KrollDict
import ti.iap.IAPConstants.SkuModelKeys

class SkuModel(val skuDetails: SkuDetails) {
    val modelData: KrollDict get() {
        val modelDict = KrollDict()

        modelDict[SkuModelKeys.DESCRIPTION] = skuDetails.description
        modelDict[SkuModelKeys.ICON_URL] = skuDetails.iconUrl
        modelDict[SkuModelKeys.FREE_TRIAL_PERIOD] = skuDetails.freeTrialPeriod
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE] = skuDetails.introductoryPrice
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE_AMOUNT_MICROS] = skuDetails.introductoryPriceAmountMicros
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE_CYCLES] = skuDetails.introductoryPriceCycles
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE_PERIOD] = skuDetails.introductoryPricePeriod
        modelDict[SkuModelKeys.ORIGINAL_PRICE] = skuDetails.originalPrice
        modelDict[SkuModelKeys.ORIGINAL_PRICE_AMOUNT_MICROS] = skuDetails.originalPriceAmountMicros
        modelDict[SkuModelKeys.PRICE] = skuDetails.price
        modelDict[SkuModelKeys.PRICE_AMOUNT_MICROS] = skuDetails.priceAmountMicros
        modelDict[SkuModelKeys.PRICE_CURRENCY_CODE] = skuDetails.priceCurrencyCode
        modelDict[SkuModelKeys.PRODUCT_ID] = skuDetails.sku
        modelDict[SkuModelKeys.SUBSCRIPTION_PERIOD] = skuDetails.subscriptionPeriod
        modelDict[SkuModelKeys.TITLE] = skuDetails.title
        modelDict[SkuModelKeys.TYPE] = skuDetails.type

        return modelDict
    }
}