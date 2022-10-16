package ti.iap.handlers

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollProxy
import ti.iap.IAPConstants

class BillingConnectionHandler(private var krollProxy: KrollProxy) : BillingClientStateListener {
    var isConnected = false

    override fun onBillingServiceDisconnected() {
        fireEvent()
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        fireEvent(billingResult.responseCode == BillingClient.BillingResponseCode.OK, billingResult.debugMessage)
    }

    private fun fireEvent(connected: Boolean = false, message: String? = null) {
        isConnected = connected

        val event = KrollDict()
        event[IAPConstants.Properties.SUCCESS] = isConnected
        message?.let {
            event[IAPConstants.Properties.MESSAGE] = message
        }

        krollProxy.fireEvent(IAPConstants.Events.ON_CONNECTION_UPDATE, event)
    }
}