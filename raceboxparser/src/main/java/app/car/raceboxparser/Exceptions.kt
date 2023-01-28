package app.car.raceboxparser

open class RaceBoxPacketException(message: String?) : RuntimeException(message)

class HeaderException(message: String?) : RaceBoxPacketException(message)

class CheckSumException(message: String?) : RaceBoxPacketException(message)

class PacketClassException(message: String?) : RaceBoxPacketException(message)

class PacketSizeException(message: String?) : RaceBoxPacketException(message)
