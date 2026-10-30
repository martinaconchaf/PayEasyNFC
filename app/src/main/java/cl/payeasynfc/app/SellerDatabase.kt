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
import android.database.Cursor
import java.security.MessageDigest

class SellerDatabase(context: Context) :
    SQLiteOpenHelper(context, "payeasy.db", null, 3){

    override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE bank_accounts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller_id INTEGER NOT NULL UNIQUE,
                    bank_name TEXT NOT NULL,
                    account_type TEXT NOT NULL,
                    account_holder TEXT NOT NULL,
                    account_number TEXT NOT NULL,
                    FOREIGN KEY(seller_id)
                        REFERENCES sellers(id)
                        ON DELETE CASCADE
            )
            """.trimIndent()
                )
        }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 3) {
            db.execSQL(
                """
            CREATE TABLE bank_accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                seller_id INTEGER NOT NULL UNIQUE,
                bank_name TEXT NOT NULL,
                account_type TEXT NOT NULL,
                account_holder TEXT NOT NULL,
                account_number TEXT NOT NULL,
                FOREIGN KEY(seller_id)
                    REFERENCES sellers(id)
                    ON DELETE CASCADE
            )
        """.trimIndent()
            )
        }
    }

    fun getSellerIdByEmail(email: String): Long? {
        val cursor = readableDatabase.query(
            "sellers",
            arrayOf("id"),
            "email = ?",
            arrayOf(email.trim().lowercase()),
            null,
            null,
            null
        )

        cursor.use {
            return if (it.moveToFirst()) {
                it.getLong(0)
            } else {
                null
            }
        }
    }

    fun saveBankAccount(
        sellerId: Long,
        bankName: String,
        accountType: String,
        accountHolder: String,
        accountNumber: String
    ): Boolean {

        val values = android.content.ContentValues().apply {
            put("seller_id", sellerId)
            put("bank_name", bankName)
            put("account_type", accountType)
            put("account_holder", accountHolder)
            put("account_number", accountNumber)
        }

        val result = writableDatabase.insertWithOnConflict(
            "bank_accounts",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        return result != -1L
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
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
    fun authenticateSeller(
        email: String,
        password: String
    ): LoginResult {

        val normalizedEmail = email.trim().lowercase()

        val cursor: Cursor = readableDatabase.query(
            "sellers",
            arrayOf(
                "id",
                "password_hash",
                "password_salt",
                "failed_attempts",
                "locked_until"
            ),
            "email = ?",
            arrayOf(normalizedEmail),
            null,
            null,
            null
        )

        cursor.use {
            if (!it.moveToFirst()) {
                return LoginResult.INVALID_CREDENTIALS
            }

            val id = it.getLong(0)
            val storedHash = it.getString(1)
            val storedSalt = it.getString(2)
            var failedAttempts = it.getInt(3)
            val lockedUntil = it.getLong(4)

            val now = System.currentTimeMillis()

            // Comprobar si la cuenta está bloqueada
            if (lockedUntil > now) {
                return LoginResult.ACCOUNT_LOCKED
            }

            // Si terminó el bloqueo, reiniciar los intentos
            if (lockedUntil != 0L && lockedUntil <= now) {
                writableDatabase.execSQL(
                    """
                    UPDATE sellers
                    SET failed_attempts = 0,
                        locked_until = 0
                    WHERE id = ?
                    """.trimIndent(),
                    arrayOf(id)
                )

                failedAttempts = 0
            }

            // Calcular el hash de la contraseña ingresada
            val salt = Base64.decode(
                storedSalt,
                Base64.NO_WRAP
            )

            val spec = PBEKeySpec(
                password.toCharArray(),
                salt,
                120_000,
                256
            )

            val enteredHash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded

            spec.clearPassword()

            val originalHash = Base64.decode(
                storedHash,
                Base64.NO_WRAP
            )

            val passwordCorrect = MessageDigest.isEqual(
                enteredHash,
                originalHash
            )

            if (passwordCorrect) {
                writableDatabase.execSQL(
                    """
                    UPDATE sellers
                    SET failed_attempts = 0,
                        locked_until = 0
                    WHERE id = ?
                    """.trimIndent(),
                    arrayOf(id)
                )

                return LoginResult.SUCCESS
            }

            // Registrar intento fallido
            failedAttempts++

            if (failedAttempts >= 5) {
                val lockDuration = 15 * 60 * 1000L
                val newLockedUntil = now + lockDuration

                writableDatabase.execSQL(
                    """
                    UPDATE sellers
                    SET failed_attempts = 5,
                        locked_until = ?
                    WHERE id = ?
                    """.trimIndent(),
                    arrayOf(newLockedUntil, id)
                )

                return LoginResult.ACCOUNT_LOCKED
            }

            writableDatabase.execSQL(
                """
                UPDATE sellers
                SET failed_attempts = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf(failedAttempts, id)
            )

            return LoginResult.INVALID_CREDENTIALS
        }
    }
}

enum class LoginResult {
    SUCCESS,
    INVALID_CREDENTIALS,
    ACCOUNT_LOCKED
}