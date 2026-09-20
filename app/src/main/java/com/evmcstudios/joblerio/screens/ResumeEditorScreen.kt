package com.evmcstudios.joblerio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evmcstudios.joblerio.data.Education
import com.evmcstudios.joblerio.data.Experience
import com.evmcstudios.joblerio.data.PersonalInfo
import com.evmcstudios.joblerio.data.Resume
import com.evmcstudios.joblerio.data.ResumeManager
import com.evmcstudios.joblerio.data.ResumeTemplate
import com.evmcstudios.joblerio.ui.theme.BackgroundWhite
import com.evmcstudios.joblerio.ui.theme.CardWhite
import com.evmcstudios.joblerio.ui.theme.PrimaryBlue
import com.evmcstudios.joblerio.ui.theme.TextGray
import com.evmcstudios.joblerio.ui.theme.TitleDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResumeEditorScreen(
    resumeId: String?,
    onBack: () -> Unit,
    onPreview: (String) -> Unit
) {
    val context = LocalContext.current
    val existingResume = resumeId?.let { ResumeManager.getResume(context, it) }

    var resume by remember {
        mutableStateOf(
            existingResume ?: Resume(
                personalInfo = PersonalInfo(
                    fullName = UserPrefs.getUserName(context),
                    email = UserPrefs.getUserEmail(context)
                )
            )
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Experience", "Education", "Skills")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardWhite,
            contentColor = PrimaryBlue,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryBlue
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> InfoTab(resume = resume) { resume = it }
                1 -> ExperienceTab(resume = resume) { resume = it }
                2 -> EducationTab(resume = resume) { resume = it }
                3 -> SkillsTab(resume = resume) { resume = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        ResumeManager.saveResume(context, resume)
                        onBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Button(
                    onClick = {
                        ResumeManager.saveResume(context, resume)
                        onPreview(resume.id)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
                ) {
                    Text("Preview", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoTab(resume: Resume, onUpdate: (Resume) -> Unit) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TitleDark,
        unfocusedTextColor = TitleDark,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
    )

    OutlinedTextField(
        value = resume.personalInfo.fullName,
        onValueChange = { onUpdate(resume.copy(personalInfo = resume.personalInfo.copy(fullName = it))) },
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        singleLine = true
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = resume.personalInfo.email,
        onValueChange = { onUpdate(resume.copy(personalInfo = resume.personalInfo.copy(email = it))) },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = resume.personalInfo.phone,
        onValueChange = { onUpdate(resume.copy(personalInfo = resume.personalInfo.copy(phone = it))) },
        label = { Text("Phone") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = resume.personalInfo.location,
        onValueChange = { onUpdate(resume.copy(personalInfo = resume.personalInfo.copy(location = it))) },
        label = { Text("Location") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        singleLine = true
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = resume.personalInfo.linkedIn,
        onValueChange = { onUpdate(resume.copy(personalInfo = resume.personalInfo.copy(linkedIn = it))) },
        label = { Text("LinkedIn URL") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        singleLine = true
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = resume.personalInfo.website,
        onValueChange = { onUpdate(resume.copy(personalInfo = resume.personalInfo.copy(website = it))) },
        label = { Text("Website/Portfolio") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
        singleLine = true
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = resume.summary,
        onValueChange = { onUpdate(resume.copy(summary = it)) },
        label = { Text("Professional Summary") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Template",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TitleDark
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ResumeTemplate.entries.forEach { template ->
            FilterChip(
                selected = resume.templateId == template.id,
                onClick = { onUpdate(resume.copy(templateId = template.id)) },
                label = { Text(template.displayName, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    containerColor = CardWhite,
                    labelColor = TitleDark
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExperienceTab(resume: Resume, onUpdate: (Resume) -> Unit) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TitleDark,
        unfocusedTextColor = TitleDark,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
    )

    resume.experience.forEachIndexed { index, exp ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Experience ${index + 1}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = exp.title,
                    onValueChange = { value ->
                        val updated = resume.experience.toMutableList()
                        updated[index] = exp.copy(title = value)
                        onUpdate(resume.copy(experience = updated))
                    },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TitleDark,
                        unfocusedTextColor = TitleDark,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = exp.company,
                    onValueChange = { value ->
                        val updated = resume.experience.toMutableList()
                        updated[index] = exp.copy(company = value)
                        onUpdate(resume.copy(experience = updated))
                    },
                    label = { Text("Company") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TitleDark,
                        unfocusedTextColor = TitleDark,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = exp.startDate,
                        onValueChange = { value ->
                            val updated = resume.experience.toMutableList()
                            updated[index] = exp.copy(startDate = value)
                            onUpdate(resume.copy(experience = updated))
                        },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TitleDark,
                            unfocusedTextColor = TitleDark,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = if (exp.current) "Present" else exp.endDate,
                        onValueChange = { value ->
                            val updated = resume.experience.toMutableList()
                            updated[index] = exp.copy(endDate = value, current = false)
                            onUpdate(resume.copy(experience = updated))
                        },
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TitleDark,
                            unfocusedTextColor = TitleDark,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                        ),
                        singleLine = true,
                        enabled = !exp.current
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = exp.description,
                    onValueChange = { value ->
                        val updated = resume.experience.toMutableList()
                        updated[index] = exp.copy(description = value)
                        onUpdate(resume.copy(experience = updated))
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TitleDark,
                        unfocusedTextColor = TitleDark,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }

    Button(
        onClick = {
            val updated = resume.experience + Experience()
            onUpdate(resume.copy(experience = updated))
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.1f))
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Experience", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EducationTab(resume: Resume, onUpdate: (Resume) -> Unit) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TitleDark,
        unfocusedTextColor = TitleDark,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
    )

    resume.education.forEachIndexed { index, edu ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Education ${index + 1}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = edu.school,
                    onValueChange = { value ->
                        val updated = resume.education.toMutableList()
                        updated[index] = edu.copy(school = value)
                        onUpdate(resume.copy(education = updated))
                    },
                    label = { Text("School/University") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = edu.degree,
                        onValueChange = { value ->
                            val updated = resume.education.toMutableList()
                            updated[index] = edu.copy(degree = value)
                            onUpdate(resume.copy(education = updated))
                        },
                        label = { Text("Degree") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = edu.field,
                        onValueChange = { value ->
                            val updated = resume.education.toMutableList()
                            updated[index] = edu.copy(field = value)
                            onUpdate(resume.copy(education = updated))
                        },
                        label = { Text("Field of Study") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = edu.startDate,
                        onValueChange = { value ->
                            val updated = resume.education.toMutableList()
                            updated[index] = edu.copy(startDate = value)
                            onUpdate(resume.copy(education = updated))
                        },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = edu.endDate,
                        onValueChange = { value ->
                            val updated = resume.education.toMutableList()
                            updated[index] = edu.copy(endDate = value)
                            onUpdate(resume.copy(education = updated))
                        },
                        label = { Text("End") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors,
                        singleLine = true
                    )
                }
            }
        }
    }

    Button(
        onClick = {
            val updated = resume.education + Education()
            onUpdate(resume.copy(education = updated))
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.1f))
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Education", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsTab(resume: Resume, onUpdate: (Resume) -> Unit) {
    var newSkill by remember { mutableStateOf("") }

    Text(
        text = "Skills",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = TitleDark
    )
    Spacer(modifier = Modifier.height(8.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        resume.skills.forEach { skill ->
            FilterChip(
                selected = true,
                onClick = {
                    val updated = resume.skills.toMutableList()
                    updated.remove(skill)
                    onUpdate(resume.copy(skills = updated))
                },
                label = { Text(skill, fontSize = 13.sp) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    selectedTrailingIconColor = Color.White
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newSkill,
            onValueChange = { newSkill = it },
            label = { Text("Add a skill") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TitleDark,
                unfocusedTextColor = TitleDark,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = TextGray.copy(alpha = 0.3f)
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (newSkill.isNotBlank()) {
                    val updated = resume.skills + newSkill.trim()
                    onUpdate(resume.copy(skills = updated))
                    newSkill = ""
                }
            },
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
