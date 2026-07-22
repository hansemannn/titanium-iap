package ti.iap.models

import com.android.billingclient.api.Purchase
import org.appcelerator.kroll.KrollDict
import ti.iap.IAPConstants.PurchaseModelKeys

class PurchaseModel(val purchase: Purchase) {
    companion object {
        @JvmStatic fun createPurchaseData(purchase: Purchase): KrollDict {
            val modelDict = KrollDict()

            modelDict[PurchaseModelKeys.PURCHASE_TOKEN] = purchase.purchaseToken // primary-key and globally unique, can be used in database records safely
            modelDict[PurchaseModelKeys.PRODUCT_ID] = purchase.products.first()
            modelDict[PurchaseModelKeys.PRODUCT_IDS] = purchase.products.toTypedArray()
            modelDict[PurchaseModelKeys.ORDER_ID] = purchase.orderId
            modelDict[PurchaseModelKeys.QUANTITY] = purchase.quantity
            modelDict[PurchaseModelKeys.DEVELOPER_PAYLOAD] = purchase.developerPayload
//            modelDict[PurchaseModelKeys.ORIGINAL_JSON] = purchase.originalJson
            modelDict[PurchaseModelKeys.PACKAGE_NAME] = purchase.packageName
            modelDict[PurchaseModelKeys.PURCHASE_STATE] = purchase.purchaseState
            modelDict[PurchaseModelKeys.PURCHASE_TIME] = purchase.purchaseTime
            modelDict[PurchaseModelKeys.SIGNATURE] = purchase.signature
            modelDict[PurchaseModelKeys.IS_ACKNOWLEDGED] = purchase.isAcknowledged
            modelDict[PurchaseModelKeys.IS_AUTORENEWING] = purchase.isAutoRenewing
            modelDict[PurchaseModelKeys.IS_SUSPENDED] = purchase.isSuspended
            modelDict[PurchaseModelKeys.OBFUSCATED_ACCOUNT_ID] = purchase.accountIdentifiers?.obfuscatedAccountId ?: ""
            modelDict[PurchaseModelKeys.OBFUSCATED_PROFILE_ID] = purchase.accountIdentifiers?.obfuscatedProfileId ?: ""

            return modelDict
        }

    }

    val modelData: KrollDict get() {
        return createPurchaseData(this.purchase)
    }
}
