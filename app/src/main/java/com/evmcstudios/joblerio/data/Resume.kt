package com.evmcstudios.joblerio.data

data class Resume(
    val id: String = System.currentTimeMillis().toString(),
    val templateId: String = "professional",
    val personalInfo: PersonalInfo = PersonalInfo(),
    val summary: String = "",
    val experience: List<Experience> = emptyList(),
    val education: List<Education> = emptyList(),
    val skills: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PersonalInfo(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val linkedIn: String = "",
    val website: String = ""
)

data class Experience(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val current: Boolean = false,
    val description: String = ""
)

data class Education(
    val id: String = System.currentTimeMillis().toString(),
    val school: String = "",
    val degree: String = "",
    val field: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val gpa: String = ""
)

enum class ResumeTemplate(val id: String, val displayName: String) {
    PROFESSIONAL("professional", "Professional"),
    MODERN("modern", "Modern"),
    MINIMAL("minimal", "Minimal")
}
