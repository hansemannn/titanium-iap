package ti.iap

object IAPConstants {
    object Properties {
        const val SUCCESS = "success"
        const val CODE = "code"
        const val CALLBACK = "callback"
        const val PRODUCT_TYPE = "productType"
        const val PRODUCT_LIST = "productList"
        const val PRODUCT_ID_LIST = "productIdList"
        const val PURCHASE_LIST = "purchaseList"
    }

    object PurchaseModelKeys {
        const val PURCHASE_TOKEN = "purchaseToken"
        const val PRODUCT_ID = "productId"
        const val PRODUCT_IDS = "productIds" // Added in Play Billing v4+
        const val ORDER_ID = "orderId"
        const val QUANTITY = "quantity"
        const val DEVELOPER_PAYLOAD = "developerPayload"
        const val ORIGINAL_JSON = "originalJson"
        const val PACKAGE_NAME = "packageName"
        const val PURCHASE_STATE = "purchaseState"
        const val PURCHASE_TIME = "purchaseTime"
        const val SIGNATURE = "signature"
        const val IS_ACKNOWLEDGED = "isAcknowledged"
        const val IS_AUTORENEWING = "isAutoRenewing"
        const val OBFUSCATED_ACCOUNT_ID = "obfuscatedAccountId"
        const val OBFUSCATED_PROFILE_ID = "obfuscatedProfileId"
    }

    object SkuModelKeys {
        const val DESCRIPTION = "description"
        const val ICON_URL = "iconUrl"
        const val FREE_TRIAL_PERIOD = "freeTrialPeriod"
        const val INTRODUCTORY_PRICE = "introductoryPrice"
        const val INTRODUCTORY_PRICE_AMOUNT_MICROS = "introductoryPriceAmountMicros"
        const val INTRODUCTORY_PRICE_CYCLES = "introductoryPriceCycles"
        const val INTRODUCTORY_PRICE_PERIOD = "introductoryPricePeriod"
        const val ORIGINAL_JSON = "originalJson"
        const val ORIGINAL_PRICE = "originalPrice"
        const val ORIGINAL_PRICE_AMOUNT_MICROS = "originalPriceAmountMicros"
        const val PRICE = "price"
        const val PRICE_AMOUNT_MICROS = "priceAmountMicros"
        const val PRICE_CURRENCY_CODE = "priceCurrencyCode"
        const val PRODUCT_ID = "productId"
        const val SUBSCRIPTION_PERIOD = "subscriptionPeriod"
        const val TITLE = "title"
        const val TYPE = "type"
    }

    object Events {
        const val ON_CONNECTION_UPDATE = "connectionUpdate"
        const val ON_PURCHASE_UPDATE = "purchaseUpdate"
    }
}