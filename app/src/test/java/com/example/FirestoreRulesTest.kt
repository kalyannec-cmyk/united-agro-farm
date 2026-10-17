package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class FirestoreRulesTest : FirestoreEmulatorTestBase() {

  @Test
  fun products_authenticatedUser_canCreateAndRead() = runBlocking {
    val uid = signInTestUser("farmer@test.com")
    val productId = "prod_${UUID.randomUUID()}"
    val productData = mapOf(
      "nameNp" to "गाएको ताजा दूध",
      "nameEn" to "Fresh Cow Milk",
      "category" to "DAIRY",
      "retailPrice" to 110.0,
      "wholesalePrice" to 95.0,
      "unit" to "LITRE",
      "stockQuantity" to 140.0,
      "minStockThreshold" to 25.0,
      "descriptionNp" to "शुद्ध दूध",
      "descriptionEn" to "Pure milk",
      "sku" to "UAF-MILK",
      "isOrganicCertified" to true,
      "userId" to uid,
      "createdAt" to FieldValue.serverTimestamp()
    )

    withTimeout(5000L) {
      firestore.collection("products").document(productId).set(productData).await()
    }

    val snapshot = withTimeout(5000L) {
      firestore.collection("products").document(productId).get().await()
    }
    assertTrue("Product document should exist", snapshot.exists())
    assertEquals("गाएको ताजा दूध", snapshot.getString("nameNp"))
  }

  @Test
  fun orders_authenticatedUser_canCreateAndQueryOwnOrders() = runBlocking {
    val uid = signInTestUser("customer@test.com")
    val orderId = "ord_${UUID.randomUUID()}"
    val orderData = mapOf(
      "userId" to uid,
      "customerName" to "राम श्रेष्ठ",
      "customerPhone" to "9841234567",
      "deliveryAddress" to "पाटन, ललितपुर",
      "totalAmount" to 2500.0,
      "status" to "PENDING_VERIFICATION",
      "createdAt" to FieldValue.serverTimestamp()
    )

    withTimeout(5000L) {
      firestore.collection("orders").document(orderId).set(orderData).await()
    }

    val querySnapshot = withTimeout(5000L) {
      firestore.collection("orders").whereEqualTo("userId", uid).get().await()
    }
    assertTrue("Should find at least 1 order for current user", querySnapshot.documents.isNotEmpty())
  }

  @Test
  fun orders_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser("alice@test.com")
    val orderId = "ord_${UUID.randomUUID()}"
    val orderData = mapOf(
      "userId" to aliceUid,
      "customerName" to "Alice",
      "customerPhone" to "9841000000",
      "totalAmount" to 1200.0,
      "status" to "PENDING_VERIFICATION",
      "createdAt" to FieldValue.serverTimestamp()
    )

    withTimeout(5000L) {
      firestore.collection("orders").document(orderId).set(orderData).await()
    }

    // Sign in as Bob and attempt to read Alice's order
    signInTestUser("bob@test.com")
    try {
      withTimeout(5000L) {
        firestore.collection("orders").document(orderId).get().await()
      }
      fail("Should throw FirebaseFirestoreException.Code.PERMISSION_DENIED")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun orders_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    try {
      withTimeout(5000L) {
        firestore.collection("orders").document("any_order").get().await()
      }
      fail("Unauthenticated read should fail with PERMISSION_DENIED")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }
}
