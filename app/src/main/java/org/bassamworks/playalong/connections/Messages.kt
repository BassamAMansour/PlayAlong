package org.bassamworks.playalong.connections

enum class MessageType(val prefix: String) { CONTROL("CONTROL"), FILE_NAME("FILENAME"), OTHER("OTHER") }

const val MESSAGE_DELIMITER = ":"

enum class ControlMessages { PLAY, PAUSE, STOP, SEEK, SYNC, PREPARE, GET_SONG_BUFFERED_STATUS, SONG_BUFFERED_STATUS, EXECUTION_SUCCESS, EXECUTION_FAILED }