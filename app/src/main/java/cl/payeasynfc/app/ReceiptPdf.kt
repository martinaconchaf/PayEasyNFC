package cl.payeasynfc.app

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPdf {

    fun create(
        context: Context,
        transactionId: String,
        amount: Long,
        paymentMethod: String,
        createdAt: Long
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            textSize = 18f
            isAntiAlias = true
        }

        canvas.drawText("PAYEASY NFC", 50f, 70f, paint)

        paint.textSize = 14f
        canvas.drawText("COMPROBANTE DE VENTA", 50f, 115f, paint)
        canvas.drawText("Estado: APROBADA (SIMULACIÓN)", 50f, 160f, paint)
        canvas.drawText("Código: $transactionId", 50f, 205f, paint)
        canvas.drawText("Monto: $${amount}", 50f, 250f, paint)
        canvas.drawText("Tarjeta: $paymentMethod", 50f, 295f, paint)

        val date = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.getDefault()
        ).format(Date(createdAt))

        canvas.drawText("Fecha: $date", 50f, 340f, paint)
        canvas.drawText("Documento de demostración.", 50f, 410f, paint)
        canvas.drawText("No representa un pago bancario real.", 50f, 435f, paint)

        document.finishPage(page)

        val directory = File(context.cacheDir, "receipts")
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, "$transactionId.pdf")
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()

        return file
    }

    fun saveToDownloads(context: Context, file: File): Boolean {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            ) ?: return false

            context.contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return false

            true
        } catch (e: Exception) {
            false
        }
    }

    fun share(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Enviar comprobante")
        )
    }

}