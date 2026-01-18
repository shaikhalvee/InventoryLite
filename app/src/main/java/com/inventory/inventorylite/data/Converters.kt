package com.inventory.inventorylite.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toMovementType(value: String): MovementType = MovementType.valueOf(value)

    @TypeConverter
    fun fromMovementType(value: MovementType): String = value.name

    @TypeConverter
    fun toRole(value: String): Role = Role.valueOf(value)

    @TypeConverter
    fun fromRole(value: Role): String = value.name
}
