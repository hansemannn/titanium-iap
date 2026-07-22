package ti.iap.models

import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import org.appcelerator.kroll.KrollDict
import ti.iap.IAPConstants.SkuModelKeys

class SkuModel(val productDetails: ProductDetails) {
    private val subscriptionOffer = productDetails.subscriptionOfferDetails
        ?.firstOrNull { it.offerId == null }
        ?: productDetails.subscriptionOfferDetails?.firstOrNull()

    private val oneTimeOffer = productDetails.oneTimePurchaseOfferDetailsList?.firstOrNull()
        ?: productDetails.oneTimePurchaseOfferDetails

    val offerToken: String?
        get() = if (productDetails.productType == ProductType.SUBS) {
            subscriptionOffer?.offerToken
        } else {
            oneTimeOffer?.offerToken
        }

    val modelData: KrollDict get() {
        val modelDict = KrollDict()
        val pricingPhases = subscriptionOffer?.pricingPhases?.pricingPhaseList.orEmpty()
        val recurringPrice = pricingPhases.lastOrNull()
        val trialPhase = pricingPhases.firstOrNull { it.priceAmountMicros == 0L }
        val introductoryPhase = pricingPhases.dropLast(1).firstOrNull { it.priceAmountMicros > 0L }

        val formattedPrice = recurringPrice?.formattedPrice ?: oneTimeOffer?.formattedPrice.orEmpty()
        val priceAmountMicros = recurringPrice?.priceAmountMicros ?: oneTimeOffer?.priceAmountMicros ?: 0L
        val currencyCode = recurringPrice?.priceCurrencyCode ?: oneTimeOffer?.priceCurrencyCode.orEmpty()
        val originalPriceAmountMicros = oneTimeOffer?.fullPriceMicros ?: priceAmountMicros

        modelDict[SkuModelKeys.DESCRIPTION] = productDetails.description
        modelDict[SkuModelKeys.ICON_URL] = ""
        modelDict[SkuModelKeys.FREE_TRIAL_PERIOD] = trialPhase?.billingPeriod.orEmpty()
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE] = introductoryPhase?.formattedPrice.orEmpty()
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE_AMOUNT_MICROS] = introductoryPhase?.priceAmountMicros ?: 0L
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE_CYCLES] = introductoryPhase?.billingCycleCount ?: 0
        modelDict[SkuModelKeys.INTRODUCTORY_PRICE_PERIOD] = introductoryPhase?.billingPeriod.orEmpty()
        modelDict[SkuModelKeys.ORIGINAL_PRICE] = formattedPrice
        modelDict[SkuModelKeys.ORIGINAL_PRICE_AMOUNT_MICROS] = originalPriceAmountMicros
        modelDict[SkuModelKeys.PRICE] = formattedPrice
        modelDict[SkuModelKeys.PRICE_AMOUNT_MICROS] = priceAmountMicros
        modelDict[SkuModelKeys.PRICE_CURRENCY_CODE] = currencyCode
        modelDict[SkuModelKeys.PRODUCT_ID] = productDetails.productId
        modelDict[SkuModelKeys.SUBSCRIPTION_PERIOD] = recurringPrice?.billingPeriod.orEmpty()
        modelDict[SkuModelKeys.TITLE] = productDetails.title
        modelDict[SkuModelKeys.TYPE] = productDetails.productType
        modelDict[SkuModelKeys.OFFER_TOKEN] = offerToken.orEmpty()
        modelDict[SkuModelKeys.OFFER_ID] = subscriptionOffer?.offerId ?: oneTimeOffer?.offerId.orEmpty()
        modelDict[SkuModelKeys.BASE_PLAN_ID] = subscriptionOffer?.basePlanId.orEmpty()
        modelDict[SkuModelKeys.OFFER_TAGS] = (subscriptionOffer?.offerTags ?: oneTimeOffer?.offerTags).orEmpty().toTypedArray()

        return modelDict
    }
}
