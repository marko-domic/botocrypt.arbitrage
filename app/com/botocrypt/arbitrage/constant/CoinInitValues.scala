package com.botocrypt.arbitrage.constant

object CoinInitValues {

  case class ConversionInfo(landingCurrencyId: String, exchange: String, commissions: (Double, Double))

  val CoinsPerExchange = Map(
    "CEX.IO" -> Map(
      "BTC" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("ETH", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XRP", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XLM", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("LTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("ADA", "CEX.IO", (0.25, 0.00))
      ),
      "ETH" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00))
      ),
      "USD" -> Set(
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("ETH", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XRP", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XLM", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("LTC", "CEX.IO", (0.25, 0.00))
      ),
      "XRP" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00))
      ),
      "XLM" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00))
      ),
      "LTC" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00))
      ),
      "ADA" -> Set(
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00))
      )
    )
  )
}
