import APIManager from 'api';
import IAP from 'ti.iap';
import { InAppPurchaseProduct } from 'enums'

let purchaseResolver;
let purchaseRejecter;
let tripId;
let selectedProduct;

let isBillingInitialized = false;

export default class InAppPurchaseManager {

	static prepare() {
		if (OS_ANDROID) {
			IAP.addEventListener('connectionUpdate', InAppPurchaseManager._onConnectionUpdate);
			IAP.addEventListener('purchaseUpdate', InAppPurchaseManager._onPurchaseUpdated);

			IAP.initialize();
		}
	}

	static shutdown() {
		if (!OS_ANDROID) { return; }

		IAP.disconnect();
		isBillingInitialized = false;
	}

	static get isReady() {
		return isBillingInitialized;
	}

	static async getProductInfos() {
		return new Promise(async (resolve, reject) => {
			if (OS_ANDROID) {
				try {
					const inAppPurchases = await InAppPurchaseManager.getAndroidProducts(IAP.SKU_TYPE_INAPP);
					const subscriptions = await InAppPurchaseManager.getAndroidProducts(IAP.SKU_TYPE_SUBS);

					resolve([...inAppPurchases, ...subscriptions]);
				} catch (error) {
					reject(error);
				}
			} else {
				IAP.retrieveProductsInfo({
					identifiers: Object.values(InAppPurchaseProduct),
					callback: function (event) {
						if (!event.success && !event.retrievedProducts) {
							reject(event);
						} else {
							resolve(event.retrievedProducts);
						}
					}
				});
			}
		});
	}

	static async getAndroidProducts(productType) {
		return new Promise((resolve, reject) => {
			IAP.retrieveProductsInfo({
				productType,
				productIdList: Object.values(InAppPurchaseProduct),
				callback: event => {
					if (!event.success) {
						reject();
					} else {
						resolve(InAppPurchaseManager.mappedAndroidProductList(event.productList));
					}
				}
			});
		})
	}

	static async presentCodeRedemptionSheet(code) {
		if (OS_ANDROID) {
			// Not supported natively so far
		} else {
			const { index} = await Utils.showAlert({ title: 'The code has been copied to your clipboard!' });
			if (index === -1) { return; }
	
			Ti.UI.Clipboard.setText(code);
			IAP.presentCodeRedemptionSheet();
		}
	}

	static async purchase(product, _tripId) {
		tripId = _tripId;

		Ti.API.warn(`** Purchasing in app product (${product.identifier}) **`);

		return new Promise((resolve, reject) => {
			if (OS_ANDROID) {
				if (!isBillingInitialized) {
					alert('Google Pay is not ready so far!');
					return;
				}

				purchaseResolver = resolve;
				purchaseRejecter = reject;

				selectedProduct = product;

				IAP.purchase(product.identifier); // TODO: Use a callback here as well?
			} else {
				IAP.purchase({
					identifier: product.identifier,
					applicationUsername: Alloy.Models.user.get('identifier'),
					quantity: 1,
					atomically: true,
					callback: event => {
						if (!event.success) {
							reject(event);
							return;
						}

						Ti.API.warn('** Purchase successful for *' + product.identifier + ', validating purchase on server … *');
						console.warn(JSON.stringify(event, null, 4));

						APIManager.postSubscription({ /* ... */ });
					}
				});
			}
		});
	}

	// ---- Android-only events ----

	static _onConnectionUpdate(event) {
		isBillingInitialized = event.success;
	}

	static _onPurchaseUpdated(event) {
		console.warn('** Purchase updated!');
		console.warn(JSON.stringify(event, null, 4));

		function submitSubscription() {
			APIManager.postSubscription({ /* ... */ });
		}

		if (!event.success) {
			purchaseRejecter && purchaseRejecter(event);
			purchaseRejecter = undefined;
			selectedProduct = undefined;
			return;
		}

		if (event.purchaseList.length === 0) {
			purchaseRejecter && purchaseRejecter(event);
			purchaseRejecter = undefined;
			selectedProduct = undefined;
			return;
		}

		const purchaseDetails = event.purchaseList[event.purchaseList.length - 1];

		// Android needs to acknowledge purchases (!)
		if (purchaseDetails.purchaseState === IAP.PURCHASE_STATE_PURCHASED) {
			if (!purchaseDetails.isAcknowledged) {
				Ti.API.warn('** Acknowledging ' + purchaseDetails.productId + '…');

				// A bit hacky way to use a different signature based on the product
				// TODO: Pass a flag IN_APP / SUBS to identify the correct method internally
				const method = [ InAppPurchaseProduct.SUBSCRIPTION, InAppPurchaseProduct.SUBSCRIPTION_FREE_TRIAL ].includes(purchaseDetails.productId) ? 'acknowledgeNonConsumableProduct' : 'acknowledgeConsumableProduct';

				IAP[method]({
					purchaseToken: purchaseDetails.purchaseToken,
					callback: purchaseResult => {
						Ti.API.warn('** Acknowledgement done for *' + purchaseDetails.productId + '* : ' + purchaseResult.success);
						// For some very rare cases, the "success" flag can be false, but the token may still be valid
						const success = purchaseResult.success || !!purchaseResult.purchaseToken;
	
						if (success) {
							Ti.API.warn('** Purchase successful for *' + purchaseDetails.productId + ', validating purchase on server … *');
							submitSubscription();
						} else {
							purchaseRejecter && purchaseRejecter(event);
							purchaseRejecter = undefined;
							selectedProduct = undefined;
						}
					}
				});
			} else {
				submitSubscription();
				Ti.API.warn('** Purchase acknowledged & successful for *' + purchaseDetails.productId + '*');
			}
		} else if (purchaseDetails.purchaseState === IAP.PURCHASE_STATE_PENDING) {
			Ti.API.warn('** Purchase still pending! *');
			Utils.showAlert({	
				title: L('purchase_pending'),
				message: L('purchase_pending_message'),
				buttonNames: [ L('alrighty') ]
			});
		}
	}

	static mappedAndroidProductList(productList) {
		return productList.map(product => {
			return {
				localizedTitle: product.title,
				identifier: product.productId,
				localizedPrice: product.originalPrice,
				price: product.priceAmountMicros / 1000000,
				priceCurrencyCode: product.priceCurrencyCode
			}
		});
	}
}
