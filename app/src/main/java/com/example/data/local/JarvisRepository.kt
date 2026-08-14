package com.example.data.local

import kotlinx.coroutines.flow.Flow

class JarvisRepository(
    private val taskDao: TaskDao,
    private val chatMessageDao: ChatMessageDao,
    private val smartDeviceDao: SmartDeviceDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allChatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()
    val allSmartDevices: Flow<List<SmartDeviceEntity>> = smartDeviceDao.getAllDevices()

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(id: Long) = taskDao.deleteTaskById(id)
    suspend fun clearCompletedTasks() = taskDao.clearCompletedTasks()

    suspend fun insertChatMessage(message: ChatMessageEntity) = chatMessageDao.insertMessage(message)
    suspend fun clearChatHistory() = chatMessageDao.clearHistory()

    suspend fun updateSmartDevice(device: SmartDeviceEntity) = smartDeviceDao.updateDevice(device)
    
    suspend fun initializeDefaultSmartDevices() {
        if (smartDeviceDao.getDeviceCount() == 0) {
            val defaults = listOf(
                SmartDeviceEntity(
                    name = "Living Room Lights",
                    type = "LIGHT",
                    location = "Living Room",
                    isOn = true,
                    value = "85%",
                    statusDetail = "RGB Cyan Active"
                ),
                SmartDeviceEntity(
                    name = "HVAC Climate Control",
                    type = "THERMOSTAT",
                    location = "Entire House",
                    isOn = true,
                    value = "22°C",
                    statusDetail = "Cooling Mode"
                ),
                SmartDeviceEntity(
                    name = "Main Gate Arc Lock",
                    type = "LOCK",
                    location = "Entrance",
                    isOn = true,
                    value = "Locked",
                    statusDetail = "Biometric Security Active"
                ),
                SmartDeviceEntity(
                    name = "Front Door Security HD",
                    type = "CAMERA",
                    location = "Exterior",
                    isOn = true,
                    value = "Live HD 1080p",
                    statusDetail = "Motion Sensor Clear"
                ),
                SmartDeviceEntity(
                    name = "Garage Overhead Cam",
                    type = "CAMERA",
                    location = "Garage",
                    isOn = true,
                    value = "Live HD 1080p",
                    statusDetail = "Vehicle Detected"
                ),
                SmartDeviceEntity(
                    name = "Backyard CCTV Sensor",
                    type = "CAMERA",
                    location = "Perimeter",
                    isOn = true,
                    value = "Infrared Night",
                    statusDetail = "Perimeter Patrol Active"
                )
            )
            smartDeviceDao.insertDevices(defaults)
        }
    }
}
