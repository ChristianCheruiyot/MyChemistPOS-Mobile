package com.habit.mychemistpos.data.local

import androidx.room.TypeConverter
import com.habit.mychemistpos.data.model.OrderStatus
import com.habit.mychemistpos.data.model.PaymentMethod
import com.habit.mychemistpos.data.model.PurchaseOrderItem
import com.habit.mychemistpos.data.model.SaleLineItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

    private val json = Json { ignoreUnknownKeys = true }
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // ── LocalDateTime ───────────────────────────────────────────────────────
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? =
        value?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it, dateTimeFormatter) }

    // ── LocalDate ───────────────────────────────────────────────────────────
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? =
        value?.format(dateFormatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, dateFormatter) }

    // ── List<String> ────────────────────────────────────────────────────────
    @TypeConverter
    fun fromStringList(value: List<String>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        json.decodeFromString(value)

    // ── List<SaleLineItem> ──────────────────────────────────────────────────
    @TypeConverter
    fun fromSaleLineItems(value: List<SaleLineItem>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toSaleLineItems(value: String): List<SaleLineItem> =
        json.decodeFromString(value)

    // ── List<PurchaseOrderItem> ─────────────────────────────────────────────
    @TypeConverter
    fun fromPurchaseOrderItems(value: List<PurchaseOrderItem>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toPurchaseOrderItems(value: String): List<PurchaseOrderItem> =
        json.decodeFromString(value)

    // ── PaymentMethod ───────────────────────────────────────────────────────
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try {
        PaymentMethod.valueOf(value.trim().uppercase())
    } catch (_: Exception) {
        PaymentMethod.CASH
    }

    // ── OrderStatus ─────────────────────────────────────────────────────────
    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)
}
