package com.app_moviles.tejisurapp.calidad.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

data class FotoDefecto(val archivo: File, val uri: Uri) {
    companion object {
        fun crear(contexto: Context): FotoDefecto {
            val carpeta = File(contexto.cacheDir, "fotos_calidad")
            if (!carpeta.isDirectory && !carpeta.mkdirs()) throw IOException("fotos_calidad")
            val archivo = File.createTempFile("defecto_", ".jpg", carpeta)
            val uri = FileProvider.getUriForFile(contexto, contexto.packageName + ".fotos_calidad", archivo)
            return FotoDefecto(archivo, uri)
        }
    }

    fun importar(contexto: Context, origen: Uri) {
        val entrada = contexto.contentResolver.openInputStream(origen) ?: throw IOException("Foto no disponible")
        entrada.use { origenFoto ->
            archivo.outputStream().use { destino ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0
                while (true) {
                    val leidos = origenFoto.read(buffer)
                    if (leidos < 0) break
                    total += leidos
                    if (total > 25 * 1024 * 1024) throw IOException("Foto demasiado grande")
                    destino.write(buffer, 0, leidos)
                }
            }
        }
    }
}

internal fun decodificarFoto(contexto: Context, uri: Uri, maximo: Int): Bitmap? =
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contexto.contentResolver, uri)) { decoder, info, _ ->
                val mayor = maxOf(info.size.width, info.size.height)
                if (mayor > maximo) {
                    val escala = maximo.toDouble() / mayor
                    decoder.setTargetSize(
                        (info.size.width * escala).toInt().coerceAtLeast(1),
                        (info.size.height * escala).toInt().coerceAtLeast(1)
                    )
                }
            }
        } else {
            decodificarFotoAnterior(contexto, uri, maximo)
        }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

private fun decodificarFotoAnterior(contexto: Context, uri: Uri, maximo: Int): Bitmap? {
    val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val resolver = contexto.contentResolver
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
    if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return null
    val mayor = maxOf(opciones.outWidth, opciones.outHeight)
    opciones.inSampleSize = 1
    while (mayor / opciones.inSampleSize > maximo) opciones.inSampleSize *= 2
    opciones.inJustDecodeBounds = false
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
}
