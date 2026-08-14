package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_devices")
data class SmartDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // LIGHT, THERMOSTAT, LOCK, CAMERA
    val location: String, // Living Room, Main Entrance, Garage, Bedroom
    val isOn: Boolean = false,
    val value: String = "0", // Temp ("22°C"), Brightness ("80%"), Lock ("Locked")
    val statusDetail: String = "Normal"
)
