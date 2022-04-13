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
        ConversionInfo("ADA", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("USDT", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "Binance", (0.00, 0.0005))
      ),
      "ETH" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("USDT", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("ETH", "Binance", (0.00, 0.007))
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
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("USDT", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XRP", "Binance", (0.00, 0.25))
      ),
      "XLM" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("USDT", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XLM", "Binance", (0.00, 0.25))
      ),
      "LTC" -> Set(
        ConversionInfo("USD", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("USDT", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("LTC", "Binance", (0.00, 0.001))
      ),
      "ADA" -> Set(
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("USDT", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("ADA", "Binance", (0.00, 1.00))
      ),
      "USDT" -> Set(
        ConversionInfo("BTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("ETH", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XRP", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("XLM", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("LTC", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("ADA", "CEX.IO", (0.25, 0.00)),
        ConversionInfo("USDT", "Binance", (0.00, 40.00))
      )
    ),
    "Binance" -> Map(
      "BTC" -> Set(
        ConversionInfo("USDT", "Binance", (0.1, 0.00)),
        ConversionInfo("ETH", "Binance", (0.1, 0.00)),
        ConversionInfo("XRP", "Binance", (0.1, 0.00)),
        ConversionInfo("XLM", "Binance", (0.1, 0.00)),
        ConversionInfo("LTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ADA", "Binance", (0.1, 0.00)),
        ConversionInfo("BTC", "CEX.IO", (0.00, 0.00061))
      ),
      "ETH" -> Set(
        ConversionInfo("USDT", "Binance", (0.1, 0.00)),
        ConversionInfo("BTC", "Binance", (0.1, 0.00)),
        ConversionInfo("XRP", "Binance", (0.1, 0.00)),
        ConversionInfo("XLM", "Binance", (0.1, 0.00)),
        ConversionInfo("LTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ADA", "Binance", (0.1, 0.00)),
        ConversionInfo("ETH", "CEX.IO", (0.00, 0.002))
      ),
      "USDT" -> Set(
        ConversionInfo("BTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ETH", "Binance", (0.1, 0.00)),
        ConversionInfo("XRP", "Binance", (0.1, 0.00)),
        ConversionInfo("XLM", "Binance", (0.1, 0.00)),
        ConversionInfo("LTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ADA", "Binance", (0.1, 0.00)),
        ConversionInfo("USDT", "CEX.IO", (0.00, 10.00))
      ),
      "XRP" -> Set(
        ConversionInfo("USDT", "Binance", (0.1, 0.00)),
        ConversionInfo("BTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ETH", "Binance", (0.1, 0.00)),
        ConversionInfo("XRP", "CEX.IO", (0.00, 33.00))
      ),
      "XLM" -> Set(
        ConversionInfo("USDT", "Binance", (0.1, 0.00)),
        ConversionInfo("BTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ETH", "Binance", (0.1, 0.00)),
        ConversionInfo("XLM", "CEX.IO", (0.00, 1.08))
      ),
      "LTC" -> Set(
        ConversionInfo("USDT", "Binance", (0.1, 0.00)),
        ConversionInfo("BTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ETH", "Binance", (0.1, 0.00)),
        ConversionInfo("LTC", "CEX.IO", (0.00, 0.002))
      ),
      "ADA" -> Set(
        ConversionInfo("USDT", "Binance", (0.1, 0.00)),
        ConversionInfo("BTC", "Binance", (0.1, 0.00)),
        ConversionInfo("ETH", "Binance", (0.1, 0.00)),
        ConversionInfo("ADA", "CEX.IO", (0.00, 1.00))
      )
    )
  )
}
