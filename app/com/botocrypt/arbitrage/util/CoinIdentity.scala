package com.botocrypt.arbitrage.util

object CoinIdentity {

  def getCoinId(coinBaseId: String, exchange: String): String = s"$exchange:$coinBaseId"
}
