package com.example.bumpbeats


import com.google.firebase.firestore.FirebaseFirestore

/**
 * A utility class to delete a Firestore collection.
 */
object CollectionDeleter {

    /**
     * Deletes all documents in the specified Firestore collection.
     *
     * @param collectionPath The path of the collection to delete.
     * @param onComplete A callback invoked when the deletion is successful.
     * @param onError A callback invoked when an error occurs, providing the error message.
     */
    fun deleteCollection(
        collectionPath: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()

                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }

                // Commit the batch
                batch.commit()
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { exception ->
                        onError("Failed to delete collection: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                onError("Error retrieving collection: ${exception.message}")
            }
    }
}