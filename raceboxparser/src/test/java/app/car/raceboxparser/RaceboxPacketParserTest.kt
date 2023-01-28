package app.car.raceboxparser

import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RaceboxPacketParserTest {

    private val successTestInts = intArrayOf(
        0xB5, 0x62, 0xFF, 0x01, 0x50, 0x00, 0xA0, 0xE7, 0x0C, 0x07, 0xE6, 0x07, 0x01, 0x0A, 0x08, 0x33,
        0x08, 0x37, 0x19, 0x00, 0x00, 0x00, 0x2A, 0xAD, 0x4D, 0x0E, 0x03, 0x01, 0xEA, 0x0B, 0xC6, 0x93,
        0xE1, 0x0D, 0x3B, 0x37, 0x6F, 0x19, 0x61, 0x8C, 0x09, 0x00, 0x0F, 0x01, 0x09, 0x00, 0x9C, 0x03,
        0x00, 0x00, 0x2C, 0x07, 0x00, 0x00, 0x23, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xD0, 0x00,
        0x00, 0x00, 0x88, 0xA9, 0xDD, 0x00, 0x2C, 0x01, 0x00, 0x59, 0xFD, 0xFF, 0x71, 0x00, 0xCE, 0x03,
        0x2F, 0xFF, 0x56, 0x00, 0xFC, 0xFF, 0x06, 0xDB,
    )
    private val successTestBytes =
        successTestInts.foldIndexed(ByteArray(successTestInts.size)) { i, a, v ->
            a.apply { set(i, v.toByte()) }
        }

    private val errorTestInts = intArrayOf(
        0xb5, 0x62, 0x05, 0x01, 0x02, 0x00, 0x06, 0x8b, 0x99, 0xc2, 0xb5, 0x62, 0x01, 0x07, 0x5c
    )

    private val errorTestBytes =
        errorTestInts.foldIndexed(ByteArray(errorTestInts.size)) { i, a, v ->
            a.apply { set(i, v.toByte()) }
        }

    private val testPacket = Packet(
        iTOW = 118286240,
        year = 2022,
        month = 1,
        day = 10,
        hour = 8,
        minute = 51,
        second = 8,
        validityFlags = 0x37.toByte().toInt(),
        timeAccuracy = 25,
        nanoseconds = 239971626,
        fixStatusValue = 3,
        fixStatusFlags = 0x01.toByte().toInt(),
        dateTimeFlags = 0xEA.toByte().toInt(),
        numberOfSvs = 11,
        longitude = 23.2887238,
        latitude = 42.6719035,
        wgsAltitude = 625.761,
        mslAltitude = 590.095,
        hAccuracy = 0.924f,
        vAccuracy = 1.836f,
        speed = 0.035f,
        heading = 0.0,
        speedAccuracy = 0.208f,
        headingAccuracy = 145.26856,
        pdop = 3.0,
        latLonFlags = 0x00.toByte().toInt(),
        batteryStatus = 0x59.toByte().toInt(),
        gForceX = -0.003f,
        gForceY = 0.113f,
        gForceZ = 0.974f,
        rotationRateX = -2.09f,
        rotationRateY = 0.86f,
        rotationRateZ = -0.04f
    )

    private lateinit var packetParser: RaceboxPacketParser

    @Before
    fun setUp() {
        packetParser = RaceboxPacketParser()
    }

    @Test
    fun successPacketParsingTest() {
        val packet = packetParser.parse(successTestBytes)
        expectThat(packet).isEqualTo(testPacket)
    }

    @Test(expected = CheckSumException::class)
    fun errorPacketParsingTest() {
        val packet = packetParser.parse(errorTestBytes)
        expectThat(packet).isEqualTo(testPacket)
    }
}
