package cl.payeasynfc.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SellerDatabase(context: Context) :
    SQLiteOpenHelper(context, "payeasy.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE sellers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                password_salt TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Las migraciones se agregarán cuando evolucione el modelo.
    }

    fun registerSeller(
        name: String,
        email: String,
        password: String
    ): Boolean {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            120_000,
            256
        )

        val hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec)
            .encoded

        spec.clearPassword()

        val values = ContentValues().apply {
            put("name", name)
            put("email", email.trim().lowercase())
            put("password_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            put("password_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
        }

        return try {
            writableDatabase.insertOrThrow(
                "sellers",
                null,
                values
            )
            true
        } catch (e: SQLiteConstraintException) {
            false // El correo ya está registrado.
        }
    }
}