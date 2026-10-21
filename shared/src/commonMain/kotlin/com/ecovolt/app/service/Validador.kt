package com.ecovolt.app.service

// Validaciones de los campos del login, del registro y de la lectura del medidor. Devuelven el mensaje de error, o null si todo está bien.
object Validador {

    fun validarUsuario(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "Escribe tu usuario"
            texto.length < 4 -> "Debe tener al menos 4 caracteres"
            texto.length > 20 -> "Debe tener máximo 20 caracteres"
            !texto.all { it.isLetterOrDigit() || it == '.' || it == '_' } ->
                "Solo letras, números, punto y guion bajo"
            else -> null
        }
    }

    fun validarContrasena(valor: String): String? = when {
        valor.isEmpty() -> "Escribe tu contraseña"
        valor.length < 6 -> "Debe tener al menos 6 caracteres"
        valor.length > 32 -> "Debe tener máximo 32 caracteres"
        valor.any { it.isWhitespace() } -> "No puede llevar espacios"
        else -> null
    }

    fun validarNombre(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "Escribe tu nombre"
            texto.length < 3 -> "Debe tener al menos 3 caracteres"
            texto.length > 40 -> "Debe tener máximo 40 caracteres"
            !texto.all { it.isLetter() || it == ' ' || it == '.' || it == '\'' || it == '-' } ->
                "Usa solo letras y espacios"
            else -> null
        }
    }

    fun validarConfirmacion(contrasena: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite tu contraseña"
        contrasena != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    // Lectura del medidor en kWh: número positivo, con punto o coma decimal.
    // El backend repite esta misma revisión (Validators.valorKwh) porque nunca se fía del cliente.
    fun validarLectura(valor: String): String? {
        val texto = valor.trim().replace(',', '.')
        val numero = texto.toDoubleOrNull()
        return when {
            texto.isEmpty() -> "Escribe o elige el número del medidor"
            numero == null || numero.isNaN() -> "Escribe solo números (puedes usar un punto decimal)"
            numero <= 0.0 -> "Debe ser mayor que cero"
            numero >= 10_000_000.0 -> "El valor es demasiado grande"
            else -> null
        }
    }
}
