package com.ecovolt.app.service

// Busca en el texto que devolvió el OCR los números que pueden ser la lectura del medidor.
// El OCR no es perfecto, por eso siempre se le muestra el resultado al usuario para confirmarlo.
object ExtractorLectura {

    // Grupos de caracteres que parecen un número, incluyendo letras que el OCR suele confundir
    private val patron = Regex("[0-9OoIlSB|.,]{3,}")

    fun candidatos(texto: String): List<String> {
        val lista = patron.findAll(texto)
            .map { it.value }
            .filter { token -> token.count { it in '0'..'9' } >= 3 } // al menos 3 dígitos reales
            .mapNotNull { normalizar(it) }
            .filter { it.count { c -> c.isDigit() } in 3..8 }
            .distinct()
            .toList()

        // Una lectura de medidor suele tener entre 4 y 7 dígitos. Los números de serie son más largos.
        return lista
            .sortedWith(compareBy({ if (it.count { c -> c.isDigit() } in 4..7) 0 else 1 }, { -it.count { c -> c.isDigit() } }))
            .take(4)
    }

    private fun normalizar(token: String): String? {
        val corregido = token.map {
            when (it) {
                'O', 'o' -> '0'
                'I', 'l', '|' -> '1'
                'S' -> '5'
                'B' -> '8'
                ',' -> '.'
                else -> it
            }
        }.joinToString("").trim('.')

        if (corregido.isEmpty() || corregido.none { it.isDigit() }) return null

        val partes = corregido.split('.')
        return when (partes.size) {
            1 -> partes[0]
            2 -> if (partes[1].length in 1..2) "${partes[0]}.${partes[1]}" else partes[0] + partes[1]
            else -> partes.joinToString("")
        }
    }
}
