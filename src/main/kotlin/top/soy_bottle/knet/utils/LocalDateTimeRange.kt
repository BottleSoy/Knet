package top.soy_bottle.knet.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class LocalDateTimeRange(override val start: LocalDateTime, override val endInclusive: LocalDateTime) :
	ClosedRange<LocalDateTime> {
	
}

operator fun LocalDateTime.rangeTo(another: LocalDateTime) = LocalDateTimeRange(this, another)

fun Date.toLocalDateTime() = LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())