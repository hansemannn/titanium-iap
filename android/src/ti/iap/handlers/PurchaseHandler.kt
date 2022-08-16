package ti.iap.handlers

import com.android.billingclient.api.*
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollProxy
import ti.iap.IAPConstants
import ti.iap.models.PurchaseModel

class PurchaseHandler(private var krollProxy: KrollProxy) : PurchasesUpdatedListener {
    companion object {
        @JvmStatic val purchaseCatalog = ArrayList<PurchaseModel>()

        @JvmStatic fun updatePurchaseList(purchaseList: List<Purchase>) {
            // first update the existing SKUs
            for (purchase in purchaseList) {
                for (i in purchaseCatalog.indices) {
                    // if purchase-token is matched, update the purchase model
                    if (purchaseCatalog[i].purchase.purchaseToken == purchase.purchaseToken) {
                        purchaseCatalog[i] = PurchaseModel(purchase)
                        break
                    }
                }
            }

            // finally add all new SKUs
            for (purchase in purchaseList) {
                var isPurchaseAvailable = false

                for (purchaseModel in purchaseCatalog) {
                    if (purchaseModel.purchase.purchaseToken == purchase.purchaseToken) {
                        isPurchaseAvailable = true
                        break
                    }
                }

                // add if sku is not available in our sku-catalog list
                if (!isPurchaseAvailable) {
                    purchaseCatalog.add(PurchaseModel(purchase))
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchaseList: List<Purchase>?) {
        val isSuccess = billingResult.responseCode == BillingClient.BillingResponseCode.OK
        val purchaseResult = ArrayList<KrollDict>()

        if (purchaseList != null) {
            for (purchase in purchaseList) {
                purchaseResult.add( PurchaseModel(purchase).modelData )
            }

            updatePurchaseList(purchaseList)
        }

        val resultData = KrollDict()
        resultData[IAPConstants.Properties.SUCCESS] = isSuccess
        resultData[IAPConstants.Properties.CODE] = billingResult.responseCode
        resultData[IAPConstants.Properties.PURCHASE_LIST] = purchaseResult.toArray()
        krollProxy.fireEvent(IAPConstants.Events.ON_PURCHASE_UPDATE, resultData)
    }
}