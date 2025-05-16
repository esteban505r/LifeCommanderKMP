package com.esteban.ruano.lifecommander.ui.screens

import com.lifecommander.finance.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TransactionImportScreenTest {
    @Test
    fun `test parse transactions with valid input`() = runTest {
        val input = """
            Compra TECNIPAGOS S A

            10 de mayo, 2025

            - $13.000,00


            Compra UBER RIDES

            10 de mayo, 2025

            - $8.743,00


            Retiro en Cajero Automatico

            09 de mayo, 2025

            - $600.000,00


            Abono De Bolsillo A Cuenta

            09 de mayo, 2025

            $600.000,00
        """.trimIndent()

        val transactions = parsePastedTransactions(input, "test-account-id")

        assertEquals(4, transactions.size)

        // Test first transaction
        assertEquals("Compra TECNIPAGOS S A", transactions[0].description)
        assertEquals("2025-05-10", transactions[0].date)
        assertEquals(-13000.0, transactions[0].amount)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)

        // Test second transaction
        assertEquals("Compra UBER RIDES", transactions[1].description)
        assertEquals("2025-05-10", transactions[1].date)
        assertEquals(-8743.0, transactions[1].amount)
        assertEquals(TransactionType.EXPENSE, transactions[1].type)

        // Test third transaction
        assertEquals("Retiro en Cajero Automatico", transactions[2].description)
        assertEquals("2025-05-09", transactions[2].date)
        assertEquals(-600000.0, transactions[2].amount)
        assertEquals(TransactionType.EXPENSE, transactions[2].type)

        // Test fourth transaction (income)
        assertEquals("Abono De Bolsillo A Cuenta", transactions[3].description)
        assertEquals("2025-05-09", transactions[3].date)
        assertEquals(600000.0, transactions[3].amount)
        assertEquals(TransactionType.INCOME, transactions[3].type)
    }

    @Test
    fun `test parse transactions with invalid date format`() = runTest {
        val input = """
            Invalid Transaction

            Invalid Date Format

            $100,00
        """.trimIndent()

        assertFailsWith<IllegalArgumentException> {
            parsePastedTransactions(input, "test-account-id")
        }
    }

    @Test
    fun `test parse transactions with invalid amount format`() = runTest {
        val input = """
            Invalid Transaction

            10 de mayo, 2025

            Invalid Amount
        """.trimIndent()

        assertFailsWith<IllegalArgumentException> {
            parsePastedTransactions(input, "test-account-id")
        }
    }

    @Test
    fun `test parse transactions with empty input`() = runTest {
        val transactions = parsePastedTransactions("", "test-account-id")
        assertTrue(transactions.isEmpty())
    }

    @Test
    fun `test parse transactions with incomplete transaction`() = runTest {
        val input = """
            Incomplete Transaction

            10 de mayo, 2025
        """.trimIndent()

        val transactions = parsePastedTransactions(input, "test-account-id")
        assertTrue(transactions.isEmpty())
    }

    @Test
    fun `test parse transactions with multiple blank lines`() = runTest {
        val input = """
            Compra TECNIPAGOS S A


            10 de mayo, 2025


            - $13.000,00


            Compra UBER RIDES


            10 de mayo, 2025


            - $8.743,00
        """.trimIndent()

        val transactions = parsePastedTransactions(input, "test-account-id")
        assertEquals(2, transactions.size)
    }

    @Test
    fun `test parse transactions with no trailing blank line`() = runTest {
        val input = """
            Compra TECNIPAGOS S A

            10 de mayo, 2025

            - $13.000,00
        """.trimIndent()

        val transactions = parsePastedTransactions(input, "test-account-id")
        assertEquals(1, transactions.size)
    }
} 