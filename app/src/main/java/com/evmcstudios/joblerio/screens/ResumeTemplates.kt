package com.evmcstudios.joblerio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evmcstudios.joblerio.data.Resume

@Composable
fun ProfessionalTemplate(resume: Resume) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = resume.personalInfo.fullName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF25324B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (resume.personalInfo.email.isNotBlank()) Text(resume.personalInfo.email, fontSize = 11.sp, color = Color(0xFF7C8493))
            if (resume.personalInfo.phone.isNotBlank()) Text(resume.personalInfo.phone, fontSize = 11.sp, color = Color(0xFF7C8493))
            if (resume.personalInfo.location.isNotBlank()) Text(resume.personalInfo.location, fontSize = 11.sp, color = Color(0xFF7C8493))
        }
        if (resume.personalInfo.linkedIn.isNotBlank()) {
            Text(resume.personalInfo.linkedIn, fontSize = 11.sp, color = Color(0xFF4640DE))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color(0xFF4640DE), thickness = 2.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (resume.summary.isNotBlank()) {
            SectionTitle("Professional Summary")
            Text(resume.summary, fontSize = 11.sp, color = Color(0xFF444444), lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (resume.experience.isNotEmpty()) {
            SectionTitle("Experience")
            resume.experience.forEach { exp ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(exp.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF25324B))
                    Text("${exp.startDate} - ${if (exp.current) "Present" else exp.endDate}", fontSize = 10.sp, color = Color(0xFF7C8493))
                }
                Text("${exp.company}${if (exp.location.isNotBlank()) ", ${exp.location}" else ""}", fontSize = 11.sp, color = Color(0xFF4640DE))
                if (exp.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exp.description, fontSize = 10.sp, color = Color(0xFF444444), lineHeight = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (resume.education.isNotEmpty()) {
            SectionTitle("Education")
            resume.education.forEach { edu ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${edu.degree}${if (edu.field.isNotBlank()) " in ${edu.field}" else ""}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF25324B))
                    Text("${edu.startDate} - ${edu.endDate}", fontSize = 10.sp, color = Color(0xFF7C8493))
                }
                Text(edu.school, fontSize = 11.sp, color = Color(0xFF4640DE))
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        if (resume.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle("Skills")
            Text(resume.skills.joinToString(" • "), fontSize = 11.sp, color = Color(0xFF444444), lineHeight = 16.sp)
        }
    }
}

@Composable
fun ModernTemplate(resume: Resume) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Column(
            modifier = Modifier
                .width(200.dp)
                .background(Color(0xFF25324B))
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(resume.personalInfo.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            if (resume.personalInfo.email.isNotBlank()) ContactItem("Email", resume.personalInfo.email)
            if (resume.personalInfo.phone.isNotBlank()) ContactItem("Phone", resume.personalInfo.phone)
            if (resume.personalInfo.location.isNotBlank()) ContactItem("Location", resume.personalInfo.location)
            if (resume.personalInfo.linkedIn.isNotBlank()) ContactItem("LinkedIn", resume.personalInfo.linkedIn)

            Spacer(modifier = Modifier.height(20.dp))
            SideSectionTitle("Skills")
            resume.skills.forEach { skill ->
                Text(skill, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(vertical = 2.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
            SideSectionTitle("Education")
            resume.education.forEach { edu ->
                Text(edu.degree, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(edu.school, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            if (resume.summary.isNotBlank()) {
                SectionTitle("About Me")
                Text(resume.summary, fontSize = 11.sp, color = Color(0xFF444444), lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (resume.experience.isNotEmpty()) {
                SectionTitle("Experience")
                resume.experience.forEach { exp ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(exp.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF25324B))
                    Text("${exp.company}${if (exp.location.isNotBlank()) ", ${exp.location}" else ""}", fontSize = 11.sp, color = Color(0xFF4640DE))
                    Text("${exp.startDate} - ${if (exp.current) "Present" else exp.endDate}", fontSize = 10.sp, color = Color(0xFF7C8493))
                    if (exp.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(exp.description, fontSize = 10.sp, color = Color(0xFF444444), lineHeight = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MinimalTemplate(resume: Resume) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(32.dp)
    ) {
        Text(
            text = resume.personalInfo.fullName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF25324B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (resume.personalInfo.email.isNotBlank()) Text(resume.personalInfo.email, fontSize = 11.sp, color = Color(0xFF7C8493))
            if (resume.personalInfo.phone.isNotBlank()) Text(resume.personalInfo.phone, fontSize = 11.sp, color = Color(0xFF7C8493))
            if (resume.personalInfo.location.isNotBlank()) Text(resume.personalInfo.location, fontSize = 11.sp, color = Color(0xFF7C8493))
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (resume.summary.isNotBlank()) {
            MinimalSectionTitle("SUMMARY")
            Text(resume.summary, fontSize = 11.sp, color = Color(0xFF444444), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (resume.experience.isNotEmpty()) {
            MinimalSectionTitle("EXPERIENCE")
            resume.experience.forEach { exp ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(exp.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF25324B))
                Text("${exp.company}${if (exp.location.isNotBlank()) " — ${exp.location}" else ""}", fontSize = 11.sp, color = Color(0xFF4640DE))
                Text("${exp.startDate} – ${if (exp.current) "Present" else exp.endDate}", fontSize = 10.sp, color = Color(0xFF7C8493))
                if (exp.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exp.description, fontSize = 10.sp, color = Color(0xFF444444), lineHeight = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (resume.education.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            MinimalSectionTitle("EDUCATION")
            resume.education.forEach { edu ->
                Spacer(modifier = Modifier.height(6.dp))
                Text("${edu.degree}${if (edu.field.isNotBlank()) " in ${edu.field}" else ""}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF25324B))
                Text(edu.school, fontSize = 11.sp, color = Color(0xFF4640DE))
                Text("${edu.startDate} – ${edu.endDate}", fontSize = 10.sp, color = Color(0xFF7C8493))
            }
        }

        if (resume.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            MinimalSectionTitle("SKILLS")
            Text(resume.skills.joinToString("  |  "), fontSize = 11.sp, color = Color(0xFF444444))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4640DE),
        letterSpacing = 0.5.sp
    )
}

@Composable
fun MinimalSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF25324B),
        letterSpacing = 1.5.sp
    )
}

@Composable
fun SideSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF4640DE),
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Divider(color = Color(0xFF4640DE), thickness = 1.dp)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun ContactItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4640DE))
        Text(value, fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
    }
}
