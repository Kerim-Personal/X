package com.techwithakshay.pdfviewer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // assets klasörüne koyacağınız PDF dosyasının adını buraya yazın
    private val pdfDosyaAdi = "matmat.pdf"

    // XML'deki arayüz elemanları
    private lateinit var pdfImageView: ImageView
    private lateinit var sayfaTextView: TextView
    private lateinit var oncekiButton: Button
    private lateinit var sonrakiButton: Button

    // PDF işlemleri için gerekli nesneler
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var mevcutSayfa = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Yeni layout'u yüklüyoruz
        setContentView(R.layout.activity_main)

        // XML elemanlarını koda bağlıyoruz
        pdfImageView = findViewById(R.id.pdfImageView)
        sayfaTextView = findViewById(R.id.sayfaTextView)
        oncekiButton = findViewById(R.id.oncekiButton)
        sonrakiButton = findViewById(R.id.sonrakiButton)

        // Butonlara tıklama olaylarını atıyoruz
        oncekiButton.setOnClickListener {
            if (mevcutSayfa > 0) {
                mevcutSayfa--
                showPage(mevcutSayfa)
            }
        }

        sonrakiButton.setOnClickListener {
            if (pdfRenderer != null && mevcutSayfa < pdfRenderer!!.pageCount - 1) {
                mevcutSayfa++
                showPage(mevcutSayfa)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            openPdfRenderer()
            showPage(mevcutSayfa)
        } catch (e: IOException) {
            Toast.makeText(this, "Hata: PDF dosyası açılamadı! 'assets' klasöründe olduğundan emin olun.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            closePdfRenderer()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun openPdfRenderer() {
        // PDF dosyasını assets'ten alıp cihazın önbelleğine kopyalıyoruz.
        // PdfRenderer'ın bir dosyaya direkt erişime ihtiyacı var.
        val file = File(cacheDir, pdfDosyaAdi)
        if (!file.exists()) {
            assets.open(pdfDosyaAdi).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        // Kopyalanan dosyayı açıyoruz
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
    }

    private fun closePdfRenderer() {
        currentPage?.close()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }

    private fun showPage(index: Int) {
        // Mevcut sayfayı bellekten temizle
        currentPage?.close()

        // Yeni sayfayı aç ve render et
        currentPage = pdfRenderer?.openPage(index)

        // Bir Bitmap oluşturup PDF sayfasını içine çiz
        val bitmap = Bitmap.createBitmap(currentPage!!.width, currentPage!!.height, Bitmap.Config.ARGB_8888)
        currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        // ImageView'da göster
        pdfImageView.setImageBitmap(bitmap)

        // Sayfa numarasını ve buton durumunu güncelle
        val sayfaSayisi = pdfRenderer?.pageCount ?: 0
        sayfaTextView.text = "${index + 1} / $sayfaSayisi"
        oncekiButton.isEnabled = index > 0
        sonrakiButton.isEnabled = index + 1 < sayfaSayisi
    }
}