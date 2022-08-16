import InAppPurchaseManager from './iap-manager';

const window = Ti.UI.createWindow();
window.addEventListener('open', () => InAppPurchaseManager.prepare());
window.addEventListener('close', () => InAppPurchaseManager.shutdown());

// TODO: Add UI around the product list and purchase button here
//
// const products = await InAppPurchaseManager.getProductInfos();
// const purchase = await InAppPurchaseManager.purchase('MY_PRODUCT_ID');

window.open();