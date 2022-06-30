package com.moop.gamerguides.helper

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

object FirebaseUtil {
    const val firebaseDatabaseURL = "https://moop-group-finalproj-default-rtdb.asia-southeast1.firebasedatabase.app/"
    const val firebaseStorageURL = "gs://moop-group-finalproj.appspot.com"

    fun deleteStorageFolder(path: String) {
        // initialize Firebase storage
        val firebaseStorage: FirebaseStorage = Firebase.storage(firebaseStorageURL)
        val ref = firebaseStorage.reference.child(path)
        ref.listAll()
            .addOnSuccessListener { dir ->
                dir.items.forEach { deleteStorageFile(ref.path, it.name) }
                dir.prefixes.forEach { deleteStorageFolder(it.path) }
            }
    }

    fun deleteStorageFile(path: String, name: String) {
        // initialize Firebase storage
        val firebaseStorage: FirebaseStorage = Firebase.storage(firebaseStorageURL)
        val ref = firebaseStorage.reference.child(path).child(name)
        ref.delete()
    }
}