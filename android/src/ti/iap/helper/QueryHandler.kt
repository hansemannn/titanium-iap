package ti.iap.helper

import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollFunction
import org.appcelerator.kroll.KrollObject
import ti.iap.IAPConstants
import ti.iap.TitaniumInAppPurchaseModule
import ti.iap.handlers.ProductsHandler
import ti.iap.models.PurchaseModel

object QueryHandler {
    // acknowledge non-consumable in-app products
    fun acknowledgeNonConsumableProduct(billingClient: BillingClient, args: KrollDict, krollObject: KrollObject) {
        val callback = args[IAPConstants.Properties.CALLBACK] as KrollFunction?
        val purchaseToken = args.getString(IAPConstants.PurchaseModelKeys.PURCHASE_TOKEN)
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()

        val acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener { billingResult ->
            val resultData = KrollDict()
            resultData[IAPConstants.Properties.CODE] = billingResult.responseCode
            resultData[IAPConstants.Properties.SUCCESS] = billingResult.responseCode == OK
            callback?.callAsync(krollObject, resultData)
        }

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener)
    }

    // acknowledge consumable in-app products
    fun acknowledgeConsumableProduct(billingClient: BillingClient, args: KrollDict, krollObject: KrollObject) {
        val callback = args[IAPConstants.Properties.CALLBACK] as KrollFunction?
        val purchaseToken = args.getString(IAPConstants.PurchaseModelKeys.PURCHASE_TOKEN)
        val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()


        val consumeResponseListener = ConsumeResponseListener { billingResult, updatedPurchaseToken ->
            val resultData = KrollDict()
            resultData[IAPConstants.Properties.CODE] = billingResult.responseCode
            resultData[IAPConstants.Properties.SUCCESS] = billingResult.responseCode == OK
            resultData[IAPConstants.PurchaseModelKeys.PURCHASE_TOKEN] = updatedPurchaseToken
            callback?.callAsync(krollObject, resultData)
        }

        billingClient.consumeAsync(consumeParams, consumeResponseListener)
    }

    // fetch products info from Google
    fun fetchProductsInfo(billingClient: BillingClient, args: KrollDict, krollObject: KrollObject) {
        val callback = args[IAPConstants.Properties.CALLBACK] as KrollFunction?
        val productIdList = args.getStringArray(IAPConstants.Properties.PRODUCT_ID_LIST)
        val productType = args.optString(IAPConstants.Properties.PRODUCT_TYPE, TitaniumInAppPurchaseModule.SKU_TYPE_INAPP)
        val skuList = ArrayList<String>()

        for (productID in productIdList) {
            skuList.add(productID)
        }

        val skuDetailsParams = SkuDetailsParams.newBuilder()
        skuDetailsParams.setSkusList(skuList).setType(productType)

        val productsHandler = ProductsHandler(callback, krollObject)
        billingClient.querySkuDetailsAsync(skuDetailsParams.build(), productsHandler as SkuDetailsResponseListener)
    }

    // fetch recent purchases records
    fun queryPurchaseHistoryAsync(billingClient: BillingClient, args: KrollDict, krollObject: KrollObject) {
        val callback = args[IAPConstants.Properties.CALLBACK] as KrollFunction?
        val productType = args.getString(IAPConstants.Properties.PRODUCT_TYPE)

        val purchaseHistoryResponseListener = PurchaseHistoryResponseListener { billingResult, purchaseHistoryRecordList ->
            val resultData = KrollDict()
            resultData[IAPConstants.Properties.CODE] = billingResult.responseCode
            resultData[IAPConstants.Properties.SUCCESS] = billingResult.responseCode == OK

            val purchaseList = ArrayList<KrollDict>()

            if (purchaseHistoryRecordList != null) {
                for (purchaseRecord in purchaseHistoryRecordList) {
                    purchaseList.add(PurchaseModel.createPurchaseHistoryRecord(purchaseRecord))
                }
            }

            resultData[IAPConstants.Properties.PURCHASE_LIST] = purchaseList.toTypedArray()

            callback?.callAsync(krollObject, resultData)
        }

        billingClient.queryPurchaseHistoryAsync(productType, purchaseHistoryResponseListener)
    }
}