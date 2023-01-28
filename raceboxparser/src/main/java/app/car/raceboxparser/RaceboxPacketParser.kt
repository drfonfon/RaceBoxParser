package app.car.raceboxparser

class RaceboxPacketParser {

    @Throws(
        HeaderException::class,
        CheckSumException::class,
        PacketClassException::class,
        PacketSizeException::class
    )
    fun parse(byteArray: ByteArray): Packet {
        val reader = ByteReader(byteArray)
        val header = reader.nextShortInt()
        if (header != 25269) throw HeaderException("Data packet header do not match")
        if (!byteArray.packetCheckSum()) throw CheckSumException("Data packet checkSum do not match")
        val packetClass = reader.nextByteInt()
        if (packetClass != -1) throw PacketClassException("Packet class do not match")
        val packetId = reader.nextByteInt()
        val len = reader.nextShortInt()
        if (len != 80) throw PacketSizeException("Packet length do not match")
        return Packet(
            iTOW = reader.nextInt(),
            year = reader.nextShortInt(),
            month = reader.nextByteInt(),
            day = reader.nextByteInt(),
            hour = reader.nextByteInt(),
            minute = reader.nextByteInt(),
            second = reader.nextByteInt(),
            validityFlags = reader.nextByteInt(),
            timeAccuracy = reader.nextInt(),
            nanoseconds = reader.nextInt(),
            fixStatusValue = reader.nextByteInt(),
            fixStatusFlags = reader.nextByteInt(),
            dateTimeFlags = reader.nextByteInt(),
            numberOfSvs = reader.nextByteInt(),
            longitude = reader.nextInt().toDouble().div(1E7),
            latitude = reader.nextInt().toDouble().div(1E7),
            wgsAltitude = reader.nextInt().toDouble().div(1000f),
            mslAltitude = reader.nextInt().toDouble().div(1000f),
            hAccuracy = reader.nextInt().toFloat().div(1000f),
            vAccuracy = reader.nextInt().toFloat().div(1000f),
            speed = reader.nextInt().toFloat().div(1000f),
            heading = reader.nextInt().toDouble().div(1E5),
            speedAccuracy = reader.nextInt().toFloat().div(1000f),
            headingAccuracy = reader.nextInt().toDouble().div(1E5),
            pdop = reader.nextShortInt().toDouble().div(100.0),
            latLonFlags = reader.nextByteInt(),
            batteryStatus = reader.nextByteInt(),
            gForceX = reader.nextShortInt().toFloat().div(1000f),
            gForceY = reader.nextShortInt().toFloat().div(1000f),
            gForceZ = reader.nextShortInt().toFloat().div(1000f),
            rotationRateX = reader.nextShortInt().toFloat().div(100f),
            rotationRateY = reader.nextShortInt().toFloat().div(100f),
            rotationRateZ = reader.nextShortInt().toFloat().div(100f)
        )
    }

}
