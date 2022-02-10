package com.botocrypt.arbitrage.util

object CoinUtil {

  def createCoinIdentity(id: String, exchange: String): String = s"$exchange:$id"
}
