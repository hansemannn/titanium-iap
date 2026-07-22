package ti.iap.handlers

import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.QueryProductDetailsResult
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollFunction
import org.appcelerator.kroll.KrollObject
import org.appcelerator.kroll.common.Log
import ti.iap.IAPConstants
import ti.iap.models.SkuModel

class ProductsHandler(private val callback: KrollFunction?, private val krollObject: KrollObject): ProductDetailsResponseListener {
    companion object {
        @JvmStatic val skuList = ArrayList<SkuModel>()

        @JvmStatic fun getSkuModel(productId: String): SkuModel? {
            var skuModel: SkuModel? = null

            Log.w("IAP", productId);
            Log.w("IAP", skuList.toString());

            for (model in skuList) {
                if (model.productDetails.productId == productId) {
                    skuModel = model
                    break
                }
            }

            return skuModel
        }

        @JvmStatic fun updateProductList(productDetailsList: List<ProductDetails>) {
            // first update the existing SKUs
            for (productDetails in productDetailsList) {
                for (i in skuList.indices) {
                    // if sku-id is matched, update the sku
                    if (skuList[i].productDetails.productId == productDetails.productId) {
                        skuList[i] = SkuModel(productDetails)
                        break
                    }
                }
            }

            // finally add all new SKUs
            for (productDetails in productDetailsList) {
                var isSkuAvailable = false

                for (skuModel in skuList) {
                    if (skuModel.productDetails.productId == productDetails.productId) {
                        isSkuAvailable = true
                        break
                    }
                }

                // add if sku is not available in our sku-catalog list
                if (!isSkuAvailable) {
                    skuList.add(SkuModel(productDetails))
                }
            }
        }
    }

    override fun onProductDetailsResponse(billingResult: BillingResult, productDetailsResult: QueryProductDetailsResult) {
        val productDetailsList = productDetailsResult.productDetailsList
        val success = billingResult.responseCode == OK
        val productList = ArrayList<KrollDict>()

        for (productDetails in productDetailsList) {
            productList.add(SkuModel(productDetails).modelData)
        }

        // update locally saved products' details
        updateProductList(productDetailsList)

        val resultData = KrollDict()
        resultData[IAPConstants.Properties.SUCCESS] = success
        resultData[IAPConstants.Properties.CODE] = billingResult.responseCode
        resultData[IAPConstants.Properties.PRODUCT_LIST] = productList.toTypedArray()
        resultData["unfetchedProductCount"] = productDetailsResult.unfetchedProductList.size

        callback?.callAsync(krollObject, resultData)
    }
}
