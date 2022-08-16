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
        fireEvent(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
    }

    private fun fireEvent(connected: Boolean = false) {
        isConnected = connected

        val event = KrollDict()
        event[IAPConstants.Properties.SUCCESS] = isConnected
        krollProxy.fireEvent(IAPConstants.Events.ON_CONNECTION_UPDATE, event)
    }
}