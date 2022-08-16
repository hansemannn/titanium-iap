//
//  TiIapModule.swift
//  titanium-in-app-purchase
//
//  Created by Hans Knöchel
//  Copyright (c) 2020 Hans Knöchel. All rights reserved.
//

import UIKit
import TitaniumKit
import StoreKit
import SwiftyStoreKit

@objc(TiIapModule)
class TiIapModule: TiModule {

  // MARK: Constants

  @available(iOS 11.2, *)
  @objc func SUBSCRIPTION_PERIOD_UNIT_DAY() -> UInt {
    return SKProduct.PeriodUnit.day.rawValue
  }

  @available(iOS 11.2, *)
  @objc func SUBSCRIPTION_PERIOD_UNIT_WEEK() -> UInt {
    return SKProduct.PeriodUnit.week.rawValue
  }

  @available(iOS 11.2, *)
  @objc func SUBSCRIPTION_PERIOD_UNIT_MONTH() -> UInt {
    return SKProduct.PeriodUnit.month.rawValue
  }

  @available(iOS 11.2, *)
  @objc func SUBSCRIPTION_PERIOD_UNIT_YEAR() -> UInt {
    return SKProduct.PeriodUnit.year.rawValue
  }

  @objc let SUBSCRIPTION_TYPE_AUTO_RENEWABLE = 0

  @objc let SUBSCRIPTION_TYPE_NON_RENEWING = 1

  // MARK: Proxy helpers

  func moduleGUID() -> String {
    return "8420a03d-cae0-48a0-92e5-e9ad9cda4c86"
  }

  override func moduleId() -> String! {
    return "ti.iap"
  }

  // MARK: Public methods

  @objc(initialize:)
  func initialize(unused: [Any]?) {
    // NO-OP (for Android compatibility)
  }

  @objc(completeTransactions:)
  func completeTransactions(arguments: [Any]?) {
    guard let arguments = arguments else { fatalError() }

    let callback = arguments[0] as? KrollCallback

    SwiftyStoreKit.completeTransactions(atomically: true) { purchases in
        var results: [[String: Any]] = []
        for purchase in purchases {
            switch purchase.transaction.transactionState {
            case .purchased, .restored:
                if purchase.needsFinishTransaction {
                    // Deliver content from server, then:
                    SwiftyStoreKit.finishTransaction(purchase.transaction)
                }
                results.append([
                  "productId": purchase.productId,
                  "transactionState": purchase.transaction.transactionState.rawValue
                ])
            case .failed, .purchasing, .deferred:
                break // do nothing
            @unknown default:
              break
          }
        }
        if results.count > 0, let callback = callback {
          callback.call([["results": results]], thisObject: self)
        }
    }
  }

  @objc(retrieveProductsInfo:)
  func retrieveProductsInfo(arguments: [Any]?) {
    guard let arguments = arguments, let params = arguments[0] as? [String: Any] else { fatalError() }

    guard let identifiers = params["identifiers"] as? [String],
      let callback = params["callback"] as? KrollCallback else { fatalError() }

    SwiftyStoreKit.retrieveProductsInfo(Set(identifiers)) { result in
      if result.error != nil {
        callback.call([
          [
            "success": false,
            "error": result.error?.localizedDescription ?? "Unknown error"
          ]
        ], thisObject: self)
        return
      }

      let event = [
        "success": true,
        "retrievedProducts": result.retrievedProducts.map {
          TiIapProductProxy()._init(withPageContext: self.pageContext, product: $0)!.toObject()
        },
        "invalidProductIDs": Array(result.invalidProductIDs)
      ] as [String: Any]

      callback.call([event], thisObject: self)
    }
  }

  @objc(purchase:)
  func purchase(arguments: [Any]?) {
    guard let arguments = arguments, let params = arguments[0] as? [String: Any] else { fatalError() }

    let quantity = params["quantity"] as? Int ?? 1
    let atomically = params["atomically"] as? Bool ?? true
    let applicationUsername = params["applicationUsername"] as? String ?? ""

    guard let identifier = params["identifier"] as? String,
      let callback = params["callback"] as? KrollCallback else { fatalError() }

    SwiftyStoreKit.purchaseProduct(identifier,
                                   quantity: quantity,
                                   atomically: atomically,
                                   applicationUsername: applicationUsername) { result in
        switch result {
        case .success(let purchase):
            if purchase.needsFinishTransaction {
                SwiftyStoreKit.finishTransaction(purchase.transaction)
            }

            if let receiptData = SwiftyStoreKit.localReceiptData {
              self.fire(callback, with: receiptData, and: purchase)
              return
            }

            SwiftyStoreKit.fetchReceipt(forceRefresh: true) { result in
                switch result {
                case .success(let receiptData):
                  self.fire(callback, with: receiptData, and: purchase)
                case .error(let error):
                    callback.call([["success": false, "error": error.localizedDescription]], thisObject: self)
                }
            }
        case .error(let error):
            callback.call([
              [
                "success": false,
                "error": error.localizedDescription,
                "code": error.code.rawValue,
                "message": self.errorMessageFromError(error)
              ]
            ], thisObject: self)
        case .deferred(purchase: _):
          NSLog("[WARN] Deferred purchase!")
        }
    }
  }

  @objc(fetchReceipt:)
  func fetchReceipt(arguments: [Any]?) {
    guard let arguments = arguments, let callback = arguments[0] as? KrollCallback else { fatalError() }

    SwiftyStoreKit.fetchReceipt(forceRefresh: true) { result in
        switch result {
        case .success(let receiptData):
            callback.call([[
                "success": true,
                "receipt": receiptData.base64EncodedString(options: [])
            ]], thisObject: self)
        case .error(let error):
            callback.call([["success": false, "error": error.localizedDescription]], thisObject: self)
        }
    }
  }

  @objc(restorePurchases:)
  func restorePurchases(arguments: [Any]?) {
    guard let arguments = arguments, let params = arguments[0] as? [String: Any] else { fatalError() }

    guard let atomically = params["atomically"] as? Bool,
      let callback = params["callback"] as? KrollCallback else { fatalError() }

    SwiftyStoreKit.restorePurchases(atomically: atomically) { results in
        if results.restoreFailedPurchases.count > 0 {
            callback.call([
              [
                "success": false,
                "restoreFailedPurchases": results.restoreFailedPurchases.map({ $0.0.localizedDescription })
              ]
            ], thisObject: self)
        } else if results.restoredPurchases.count > 0 {
            callback.call([
              [
                "success": true,
                "restoreFailedPurchases": [],
                "restoredPurchases": results.restoredPurchases.map({
                  ["productId": $0.productId, "transactionIdentifier": $0.transaction.transactionIdentifier]
                })
              ]
            ], thisObject: self)
        } else {
            callback.call([
              [
                "success": true,
                "restoreFailedPurchases": [],
                "restoredPurchases": []
              ]
            ], thisObject: self)
            print("Nothing to Restore")
        }
    }
  }

  @objc(shouldAddStorePaymentHandler:)
  func shouldAddStorePaymentHandler(arguments: [Any]?) {
    guard let arguments = arguments, let value = arguments[0] as? Bool else { fatalError() }

    SwiftyStoreKit.shouldAddStorePaymentHandler = { payment, product in
      if !value, let callback = arguments[1] as? KrollCallback {
        callback.call([
          ["product": TiIapProductProxy()._init(withPageContext: self.pageContext, product: product)!.toObject()]
        ], thisObject: self)
      }
      return value
    }
  }

  @objc(verifyReceipt:)
  func verifyReceipt(arguments: [Any]?) {
    guard let arguments = arguments, let params = arguments[0] as? [String: Any] else { fatalError() }

    let service = params["service"] as? String ?? "sandbox"

    guard let sharedSecret = params["sharedSecret"] as? String,
      let productId = params["productId"] as? String,
      let callback = params["callback"] as? KrollCallback else { fatalError() }

    let appleValidator = AppleReceiptValidator(service: service == "production" ? .production : .sandbox,
                                               sharedSecret: sharedSecret)
    SwiftyStoreKit.verifyReceipt(using: appleValidator) { result in
        switch result {
        case .success(let receipt):
            let purchaseResult = SwiftyStoreKit.verifyPurchase(productId: productId, inReceipt: receipt)

            switch purchaseResult {
            case .purchased(let receiptItem):
              callback.call([[
                "success": true,
                "purchased": true,
                "result": receiptItem.productId,
                "receipt": receipt as [String: Any]
              ]], thisObject: self)
              print("\(productId) is purchased: \(receiptItem)")
            case .notPurchased:
                callback.call([["success": true, "purchased": false]], thisObject: self)
                print("The user has never purchased \(productId)")
            }
        case .error(let error):
            callback.call([["success": false, "error": error.localizedDescription]], thisObject: self)
            print("Receipt verification failed: \(error)")
        }
    }
  }

  @objc(verifySubscription:)
  func verifySubscription(arguments: [Any]?) {
    guard let arguments = arguments, let params = arguments[0] as? [String: Any] else { fatalError() }

    let service = params["service"] as? String ?? "sandbox"

    guard let sharedSecret = params["sharedSecret"] as? String,
      let productId = params["productId"] as? String,
      let subscriptionRawType = params["subscriptionType"] as? Int,
      let callback = params["callback"] as? KrollCallback else { fatalError() }

    let validDuration = params["validDuration"] as? Double ?? 0

    let subscriptionType = subscriptionRawType == SUBSCRIPTION_TYPE_AUTO_RENEWABLE
      ? SubscriptionType.autoRenewable
      : SubscriptionType.nonRenewing(validDuration: TimeInterval(exactly: validDuration)!)

    let appleValidator = AppleReceiptValidator(service: service == "production" ? .production : .sandbox,
                                               sharedSecret: sharedSecret)

    SwiftyStoreKit.verifyReceipt(using: appleValidator) { result in
        switch result {
        case .success(let receipt):
            let subResult = SwiftyStoreKit.verifySubscription(ofType: subscriptionType,
                                              productId: productId,
                                              inReceipt: receipt)
            switch subResult {
            case .notPurchased:
              callback.call([["purchased": false, "expired": false]], thisObject: self)
            case .expired:
              callback.call([["purchased": false, "expired": true]], thisObject: self)
            case .purchased:
              callback.call([["purchased": true, "expired": false]], thisObject: self)
            }
        case .error(let error):
            callback.call([["success": false, "error": error.localizedDescription]], thisObject: self)
        }
    }
  }

  @objc(presentCodeRedemptionSheet:)
  func presentCodeRedemptionSheet(unused: [Any]?) {
    if #available(iOS 14.0, *) {
      SKPaymentQueue.default().presentCodeRedemptionSheet()
    } else {
      print("[ERROR] The \"presentCodeRedemptionSheet()\" function is only available on iOS 14+")
    }
  }

  // MARK: Utilities
  
  private func errorMessageFromError(_ error: SKError) -> String {
    var message = "Unknown error"

    switch error.code {
    case .unknown: message = "Unknown error. Please contact support"
    case .clientInvalid: message = "Not allowed to make the payment"
    case .paymentCancelled: message = "Payment cancelled"
    case .paymentInvalid: message = "The purchase identifier was invalid"
    case .paymentNotAllowed: message = "The device is not allowed to make the payment"
    case .storeProductNotAvailable: message = "The product is not available in the current storefront"
    case .cloudServicePermissionDenied: message = "Access to cloud service information is not allowed"
    case .cloudServiceNetworkConnectionFailed: message = "Could not connect to the network"
    case .cloudServiceRevoked: message = "User has revoked permission to use this cloud service"
    default: print((error as NSError).localizedDescription)
    }

    return message
  }

  private func fire(_ callback: KrollCallback, with receiptData: Data, and purchase: PurchaseDetails) {
    callback.call([[
        "success": true,
        "productId": purchase.productId,
        "transactionIdentifier": purchase.transaction.transactionIdentifier ?? "",
        "transactionDate": purchase.transaction.transactionDate ?? "",
        "transactionState": purchase.transaction.transactionState.rawValue,
        "receipt": receiptData.base64EncodedString(options: [])
    ]], thisObject: self)
  }
}
