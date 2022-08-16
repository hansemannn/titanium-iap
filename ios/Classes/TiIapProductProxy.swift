/**
 * Axway Titanium
 * Copyright (c) 2018-present by Axway Appcelerator. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

import TitaniumKit
import StoreKit

@objc(TiIapProductProxy)
public class TiIapProductProxy: TiProxy {

  private var product: SKProduct?

  public func _init(withPageContext context: TiEvaluator!, product: SKProduct) -> Self? {
    super._init(withPageContext: context)
    self.product = product

    return self
  }

  @objc(identifier)
  func identifier() -> String {
    return product!.productIdentifier
  }

  @objc(localizedTitle)
  func localizedTitle() -> String {
    return product!.localizedTitle
  }

  @objc(localizedDescription)
  func localizedDescription() -> String {
    return product!.localizedDescription
  }

  @objc(price)
  func price() -> Double {
    return product!.price.doubleValue
  }

  @objc(introductoryPrice)
  func introductoryPrice() -> [String: Any] {
    guard #available(iOS 11.2, *), let introductoryPrice = product!.introductoryPrice else {
      return [:]
    }

    if #available(iOS 12.2, *) {
      return [
        "identifier": introductoryPrice.identifier ?? "",
        "localizedPrice": introductoryPrice.localizedPrice ?? "",
        "price": introductoryPrice.price,
        "localizedSubscriptionPeriod": introductoryPrice.localizedSubscriptionPeriod,
        "numberOfPeriods": introductoryPrice.numberOfPeriods,
        "paymentMode": introductoryPrice.paymentMode.rawValue,
        "subscriptionPeriod": [
          "unit": introductoryPrice.subscriptionPeriod.unit.rawValue,
          "numberOfUnits": introductoryPrice.subscriptionPeriod.numberOfUnits
        ]
      ]
    }

    return [
      "localizedPrice": introductoryPrice.localizedPrice ?? "",
      "localizedSubscriptionPeriod": introductoryPrice.localizedSubscriptionPeriod,
      "numberOfPeriods": introductoryPrice.numberOfPeriods
    ]
  }

  @objc(discounts)
  func discounts() -> [[String: Any]] {
    guard #available(iOS 12.2, *) else {
      return []
    }

    return product!.discounts.map { (productDiscount) -> [String: Any] in
      [
        "identifier": productDiscount.identifier ?? "",
        "price": productDiscount.price.doubleValue,
        // --> These are crashing on iOS 13 right now
        //
        // "localizedPrice": productDiscount.localizedPrice ?? "",
        // "priceLocale": productDiscount.priceLocale.identifier,
        // "localizedSubscriptionPeriod": productDiscount.localizedSubscriptionPeriod,
        "subscriptionPeriod": [
          "numberOfUnits": productDiscount.subscriptionPeriod.numberOfUnits,
          "unit": productDiscount.subscriptionPeriod.unit.rawValue
        ],
        "numberOfPeriods": productDiscount.numberOfPeriods,
        "paymentMode": productDiscount.paymentMode.rawValue,
        "type": productDiscount.type.rawValue
      ]
    }
  }

  @objc(priceCurrencyCode)
  func priceCurrencyCode() -> String? {
    return product!.priceLocale.currencyCode
  }

  @objc(localizedPrice)
  func localizedPrice() -> String {
    return priceStringForProduct() ?? "\(price())"
  }

  func toObject() -> [String: Any] {
    var payload: [String: Any] = [
      "identifier": identifier(),
      "price": price(),
      "localizedPrice": localizedPrice(),
      "localizedTitle": localizedTitle(),
      "localizedDescription": localizedDescription(),
      "introductoryPrice": introductoryPrice(),
      "discounts": discounts()
    ]

    if let priceCurrencyCode = priceCurrencyCode() {
      payload["priceCurrencyCode"] = priceCurrencyCode
    }

    return payload
  }

  private func priceStringForProduct() -> String? {
      let numberFormatter = NumberFormatter()
      let price = product!.price
      let locale = product!.priceLocale

      numberFormatter.numberStyle = .currency
      numberFormatter.locale = locale

      return numberFormatter.string(from: price)
  }
}
