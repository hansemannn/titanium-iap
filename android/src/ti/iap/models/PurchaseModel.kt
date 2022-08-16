package ti.iap.models

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import org.appcelerator.kroll.KrollDict
import ti.iap.IAPConstants.PurchaseModelKeys

class PurchaseModel(val purchase: Purchase) {
    companion object {
        @JvmStatic fun createPurchaseData(purchase: Purchase): KrollDict {
            val modelDict = KrollDict()

            modelDict[PurchaseModelKeys.PURCHASE_TOKEN] = purchase.purchaseToken // primary-key and globally unique, can be used in database records safely
            modelDict[PurchaseModelKeys.PRODUCT_ID] = purchase.products.toString()
            modelDict[PurchaseModelKeys.ORDER_ID] = purchase.orderId
            modelDict[PurchaseModelKeys.DEVELOPER_PAYLOAD] = purchase.developerPayload
//            modelDict[PurchaseModelKeys.ORIGINAL_JSON] = purchase.originalJson
            modelDict[PurchaseModelKeys.PACKAGE_NAME] = purchase.packageName
            modelDict[PurchaseModelKeys.PURCHASE_STATE] = purchase.purchaseState
            modelDict[PurchaseModelKeys.PURCHASE_TIME] = purchase.purchaseTime
            modelDict[PurchaseModelKeys.SIGNATURE] = purchase.signature
            modelDict[PurchaseModelKeys.IS_ACKNOWLEDGED] = purchase.isAcknowledged
            modelDict[PurchaseModelKeys.IS_AUTORENEWING] = purchase.isAutoRenewing
            modelDict[PurchaseModelKeys.OBFUSCATED_ACCOUNT_ID] = purchase.accountIdentifiers?.obfuscatedAccountId ?: ""
            modelDict[PurchaseModelKeys.OBFUSCATED_PROFILE_ID] = purchase.accountIdentifiers?.obfuscatedAccountId ?: ""

            return modelDict
        }

        @JvmStatic fun createPurchaseHistoryRecord(purchaseHistoryRecord: PurchaseHistoryRecord): KrollDict {
            val modelDict = KrollDict()

            modelDict[PurchaseModelKeys.PURCHASE_TOKEN] = purchaseHistoryRecord.purchaseToken // primary-key and globally unique, can be used in database records safely
            modelDict[PurchaseModelKeys.PRODUCT_ID] = purchaseHistoryRecord.products.toString()
            modelDict[PurchaseModelKeys.ORDER_ID] = null
            modelDict[PurchaseModelKeys.DEVELOPER_PAYLOAD] = purchaseHistoryRecord.developerPayload
//            modelDict[PurchaseModelKeys.ORIGINAL_JSON] = purchaseHistoryRecord.originalJson
            modelDict[PurchaseModelKeys.PACKAGE_NAME] = null
            modelDict[PurchaseModelKeys.PURCHASE_STATE] = null
            modelDict[PurchaseModelKeys.PURCHASE_TIME] = purchaseHistoryRecord.purchaseTime
            modelDict[PurchaseModelKeys.SIGNATURE] = purchaseHistoryRecord.signature
            modelDict[PurchaseModelKeys.IS_ACKNOWLEDGED] = null
            modelDict[PurchaseModelKeys.IS_AUTORENEWING] = null
            modelDict[PurchaseModelKeys.OBFUSCATED_ACCOUNT_ID] = null
            modelDict[PurchaseModelKeys.OBFUSCATED_PROFILE_ID] = null

            return modelDict
        }
    }

    val modelData: KrollDict get() {
        return createPurchaseData(this.purchase)
    }
}