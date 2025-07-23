package com.example.sentinela

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseStorageManager {

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // GARANTA QUE ESTA LINHA ESTEJA EXATAMENTE ASSIM, COM O "suspend"
    suspend fun uploadRegistro(
        data: Date,
        anotacao: String,
        uriDaFoto: Uri?,
        uriDoAudio: Uri?
    ): Boolean {
        val user = auth.currentUser ?: return false
        val uid = user.uid

        try {
            val urlDaFoto = uriDaFoto?.let { uploadArquivo(uid, "fotos", it) }
            val urlDoAudio = uriDoAudio?.let { uploadArquivo(uid, "audios", it) }

            val registro = hashMapOf(
                "uid_do_dono" to uid,
                "timestamp" to data.time,
                "anotacao" to anotacao,
                "url_da_foto" to urlDaFoto,
                "url_do_audio" to urlDoAudio
            )

            firestore.collection("registros").add(registro).await()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private suspend fun uploadArquivo(uid: String, tipo: String, uri: Uri): String {
        val nomeDoArquivo = "${System.currentTimeMillis()}"
        val caminhoNoStorage = "$uid/$tipo/$nomeDoArquivo"
        val referenciaDoArquivo = storage.getReference(caminhoNoStorage)

        referenciaDoArquivo.putFile(uri).await()
        return referenciaDoArquivo.downloadUrl.await().toString()
    }
}