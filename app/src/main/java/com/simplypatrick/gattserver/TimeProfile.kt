package com.simplypatrick.gattserver

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.Calendar
import java.util.UUID

class TimeProfile {
    companion object {
        /* Current Time Service UUID */
        var TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
        /* Mandatory Current Time Information Characteristic */
        var CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
        /* Optional Local Time Information Characteristic */
        var LOCAL_TIME_INFO = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")
        /* Mandatory Client Characteristic Config Descriptor */
        var CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        fun createTimeService(): BluetoothGattService? {
            return BluetoothGattService(TIME_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY).apply {
                addCharacteristic(
                        BluetoothGattCharacteristic(CURRENT_TIME,
                                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                                BluetoothGattCharacteristic.PERMISSION_READ).apply {
                            addDescriptor(BluetoothGattDescriptor(CLIENT_CONFIG, BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE))
                        }
                )
                addCharacteristic(
                        BluetoothGattCharacteristic(LOCAL_TIME_INFO,
                                BluetoothGattCharacteristic.PROPERTY_READ,
                                BluetoothGattCharacteristic.PERMISSION_READ)
                )
            }
        }

        // Adjustment Flags
        val ADJUST_NONE: Byte = 0x0
        val ADJUST_MANUAL: Byte = 0x1
        val ADJUST_EXTERNAL: Byte = 0x2
        val ADJUST_TIMEZONE: Byte = 0x4
        val ADJUST_DST: Byte = 0x8

        /**
         * Construct the field values for a Current Time characteristic
         * from the given epoch timestamp and adjustment reason.
         */
        fun getExactTime(timestamp: Long, adjustReason: Byte): ByteArray {
            val time = Calendar.getInstance()
            time.timeInMillis = timestamp

            val field = ByteArray(10)

            // Year
            val year = time.get(Calendar.YEAR)
            field[0] = (year and 0xFF).toByte()
            field[1] = (year shr 8 and 0xFF).toByte()
            // Month
            field[2] = (time.get(Calendar.MONTH) + 1).toByte()
            // Day
            field[3] = time.get(Calendar.DATE).toByte()
            // Hours
            field[4] = time.get(Calendar.HOUR_OF_DAY).toByte()
            // Minutes
            field[5] = time.get(Calendar.MINUTE).toByte()
            // Seconds
            field[6] = time.get(Calendar.SECOND).toByte()
            // Day of Week (1-7)
            field[7] = getDayOfWeekCode(time.get(Calendar.DAY_OF_WEEK))
            // Fractions256
            field[8] = (time.get(Calendar.MILLISECOND) / 256).toByte()

            field[9] = adjustReason

            return field
        }

        private val DAY_UNKNOWN: Byte = 0
        private val DAY_MONDAY: Byte = 1
        private val DAY_TUESDAY: Byte = 2
        private val DAY_WEDNESDAY: Byte = 3
        private val DAY_THURSDAY: Byte = 4
        private val DAY_FRIDAY: Byte = 5
        private val DAY_SATURDAY: Byte = 6
        private val DAY_SUNDAY: Byte = 7

        /**
         * Convert a [Calendar] weekday value to the corresponding
         * Bluetooth weekday code.
         */
        private fun getDayOfWeekCode(dayOfWeek: Int): Byte {
            return when (dayOfWeek) {
                Calendar.MONDAY -> DAY_MONDAY
                Calendar.TUESDAY -> DAY_TUESDAY
                Calendar.WEDNESDAY -> DAY_WEDNESDAY
                Calendar.THURSDAY -> DAY_THURSDAY
                Calendar.FRIDAY -> DAY_FRIDAY
                Calendar.SATURDAY -> DAY_SATURDAY
                Calendar.SUNDAY -> DAY_SUNDAY
                else -> DAY_UNKNOWN
            }
        }

        /* Time bucket constants for local time information */
        private val FIFTEEN_MINUTE_MILLIS = 900000
        private val HALF_HOUR_MILLIS = 1800000

        fun getLocalTimeInfo(timestamp: Long): ByteArray {
            val time = Calendar.getInstance()
            time.timeInMillis = timestamp

            val field = ByteArray(2)

            // Time zone
            val zoneOffset = time.get(Calendar.ZONE_OFFSET) / FIFTEEN_MINUTE_MILLIS // 15 minute intervals
            field[0] = zoneOffset.toByte()

            // DST Offset
            val dstOffset = time.get(Calendar.DST_OFFSET) / HALF_HOUR_MILLIS // 30 minute intervals
            field[1] = getDstOffsetCode(dstOffset)

            return field
        }

        /* Bluetooth DST Offset Codes */
        private val DST_STANDARD: Byte = 0x0
        private val DST_HALF: Byte = 0x2
        private val DST_SINGLE: Byte = 0x4
        private val DST_DOUBLE: Byte = 0x8
        private val DST_UNKNOWN = 0xFF.toByte()

        /**
         * Convert a raw DST offset (in 30 minute intervals) to the
         * corresponding Bluetooth DST offset code.
         */
        private fun getDstOffsetCode(rawOffset: Int): Byte {
            return when (rawOffset) {
                0 -> DST_STANDARD
                1 -> DST_HALF
                2 -> DST_SINGLE
                4 -> DST_DOUBLE
                else -> DST_UNKNOWN
            }
        }
    }
}
