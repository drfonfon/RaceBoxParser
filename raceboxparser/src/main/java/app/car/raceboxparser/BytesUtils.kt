package app.car.raceboxparser

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.packetCheckSum(): Boolean {
    var ckA = 0
    var ckB = 0
    for (i in 2 until size - 2) {
        ckA += this[i]
        ckB += ckA
    }
    return this[size - 2] == ckA.toByte() && this[size - 1] == ckB.toByte()
}

class ByteReader(bytes: ByteArray) {

    private val reader = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    private var index = 0

    fun len(): Int = index

    fun nextByteInt(): Int {
        val old = index
        index++
        return reader.get(old).toInt()
    }

    fun nextInt(): Int {
        val old = index
        index += 4
        return reader.getInt(old)
    }

    fun nextShortInt(): Int {
        val old = index
        index += 2
        return reader.getShort(old).toInt()
    }
}
