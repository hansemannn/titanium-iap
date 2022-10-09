package ti.iap.handlers

import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsResponseListener
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollFunction
import org.appcelerator.kroll.KrollObject
import ti.iap.IAPConstants
import ti.iap.models.SkuModel

class ProductsHandler(private val callback: KrollFunction?, private val krollObject: KrollObject): SkuDetailsResponseListener {
    companion object {
        @JvmStatic val skuList = ArrayList<SkuModel>()

        @JvmStatic fun getSkuDetails(productId: String): SkuDetails? {
            var skuDetails: SkuDetails? = null

            for (skuModel in skuList) {
                if (skuModel.skuDetails.sku == productId) {
                    skuDetails = skuModel.skuDetails
                    break
                }
            }

            return skuDetails
        }

        @JvmStatic fun updateProductList(skuDetailsList: MutableList<SkuDetails>) {
            // first update the existing SKUs
            for (skuDetails in skuDetailsList) {
                for (i in skuList.indices) {
                    // if sku-id is matched, update the sku
                    if (skuList[i].skuDetails.sku == skuDetails.sku) {
                        skuList[i] = SkuModel(skuDetails)
                        break
                    }
                }
            }

            // finally add all new SKUs
            for (skuDetails in skuDetailsList) {
                var isSkuAvailable = false

                for (skuModel in skuList) {
                    if (skuModel.skuDetails.sku == skuDetails.sku) {
                        isSkuAvailable = true
                        break
                    }
                }

                // add if sku is not available in our sku-catalog list
                if (!isSkuAvailable) {
                    skuList.add(SkuModel(skuDetails))
                }
            }
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>?) {
        val success = billingResult.responseCode == OK && skuDetailsList != null
        val productList = ArrayList<KrollDict>()

        if (skuDetailsList != null) {
            for (skuDetails in skuDetailsList) {
                productList.add(SkuModel(skuDetails).modelData)
            }

            // update locally saved products' details
            updateProductList(skuDetailsList)
        }

        val resultData = KrollDict()
        resultData[IAPConstants.Properties.SUCCESS] = success
        resultData[IAPConstants.Properties.CODE] = billingResult.responseCode
        resultData[IAPConstants.Properties.PRODUCT_LIST] = productList.toTypedArray()

        callback?.callAsync(krollObject, resultData)
    }
}