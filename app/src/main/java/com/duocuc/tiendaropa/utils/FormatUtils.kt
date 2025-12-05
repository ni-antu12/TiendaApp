package com.duocuc.tiendaropa.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formatea un precio en formato chileno
 * Ejemplo: 35000.0 -> "$35.000"
 */
fun formatChileanPrice(price: Double): String {
    val symbols = DecimalFormatSymbols(Locale("es", "CL"))
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    
    val formatter = DecimalFormat("#,###", symbols)
    return "$${formatter.format(price)}"
}
