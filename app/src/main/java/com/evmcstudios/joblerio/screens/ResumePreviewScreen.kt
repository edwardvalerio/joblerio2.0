package com.evmcstudios.joblerio.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.evmcstudios.joblerio.data.Resume
import com.evmcstudios.joblerio.data.ResumeManager
import com.evmcstudios.joblerio.data.ResumeTemplate
import com.evmcstudios.joblerio.ui.theme.PrimaryBlue
import com.evmcstudios.joblerio.ui.theme.TitleDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumePreviewScreen(
    resumeId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val resume = remember { ResumeManager.getResume(context, resumeId) }
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text("Preview", color = TitleDark) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TitleDark)
                }
            },
            actions = {
                IconButton(onClick = {
                    if (resume != null) {
                        scope.launch {
                            isExporting = true
                            val file = exportResumeToPdf(context, resume)
                            isExporting = false
                            if (file != null) {
                                Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = PrimaryBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        if (resume != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                when (resume.templateId) {
                    "modern" -> ModernTemplate(resume = resume)
                    "minimal" -> MinimalTemplate(resume = resume)
                    else -> ProfessionalTemplate(resume = resume)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val file = exportResumeToPdf(context, resume)
                                isExporting = false
                                if (file != null) {
                                    Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isExporting
                    ) {
                        Text("Save PDF", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val file = exportResumeToPdf(context, resume)
                                isExporting = false
                                if (file != null) {
                                    sharePdf(context, file, resume.personalInfo.fullName)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !isExporting
                    ) {
                        Text("Share PDF", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private suspend fun exportResumeToPdf(context: Context, resume: Resume): File? {
    return withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)

            val canvas = page.canvas
            val paint = android.graphics.Paint()

            canvas.drawColor(android.graphics.Color.WHITE)

            var yPos = 50f

            canvas.drawText(resume.personalInfo.fullName.ifBlank { "Resume" }, 50f, yPos, paint.apply {
                this.color = android.graphics.Color.parseColor("#25324B")
                this.textSize = 20f
                this.isFakeBoldText = true
            })
            yPos += 30f

            val contactInfo = listOfNotNull(
                resume.personalInfo.email.ifBlank { null },
                resume.personalInfo.phone.ifBlank { null },
                resume.personalInfo.location.ifBlank { null }
            ).joinToString("  |  ")
            canvas.drawText(contactInfo, 50f, yPos, paint.apply {
                this.color = android.graphics.Color.parseColor("#7C8493")
                this.textSize = 10f
                this.isFakeBoldText = false
            })
            yPos += 30f

            canvas.drawLine(50f, yPos, 545f, yPos, paint.apply {
                this.color = android.graphics.Color.parseColor("#4640DE")
                this.strokeWidth = 2f
            })
            yPos += 25f

            if (resume.summary.isNotBlank()) {
                canvas.drawText("PROFESSIONAL SUMMARY", 50f, yPos, paint.apply {
                    this.color = android.graphics.Color.parseColor("#4640DE")
                    this.textSize = 12f
                    this.isFakeBoldText = true
                })
                yPos += 18f
                val summaryLines = wrapText(resume.summary, 80)
                summaryLines.forEach { line ->
                    canvas.drawText(line, 50f, yPos, paint.apply {
                        this.color = android.graphics.Color.parseColor("#444444")
                        this.textSize = 10f
                        this.isFakeBoldText = false
                    })
                    yPos += 14f
                }
                yPos += 15f
            }

            if (resume.experience.isNotEmpty()) {
                canvas.drawText("EXPERIENCE", 50f, yPos, paint.apply {
                    this.color = android.graphics.Color.parseColor("#4640DE")
                    this.textSize = 12f
                    this.isFakeBoldText = true
                })
                yPos += 18f

                resume.experience.forEach { exp ->
                    canvas.drawText(exp.title, 50f, yPos, paint.apply {
                        this.color = android.graphics.Color.parseColor("#25324B")
                        this.textSize = 11f
                        this.isFakeBoldText = true
                    })
                    canvas.drawText("${exp.company}${if (exp.location.isNotBlank()) ", ${exp.location}" else ""}", 50f, yPos + 14f, paint.apply {
                        this.color = android.graphics.Color.parseColor("#4640DE")
                        this.textSize = 10f
                        this.isFakeBoldText = false
                    })
                    canvas.drawText("${exp.startDate} - ${if (exp.current) "Present" else exp.endDate}", 400f, yPos, paint.apply {
                        this.color = android.graphics.Color.parseColor("#7C8493")
                        this.textSize = 9f
                    })
                    yPos += 30f
                    if (exp.description.isNotBlank()) {
                        val descLines = wrapText(exp.description, 90)
                        descLines.take(4).forEach { line ->
                            canvas.drawText(line, 50f, yPos, paint.apply {
                                this.color = android.graphics.Color.parseColor("#444444")
                                this.textSize = 9f
                            })
                            yPos += 12f
                        }
                    }
                    yPos += 12f
                }
            }

            if (resume.education.isNotEmpty()) {
                canvas.drawText("EDUCATION", 50f, yPos, paint.apply {
                    this.color = android.graphics.Color.parseColor("#4640DE")
                    this.textSize = 12f
                    this.isFakeBoldText = true
                })
                yPos += 18f

                resume.education.forEach { edu ->
                    canvas.drawText("${edu.degree}${if (edu.field.isNotBlank()) " in ${edu.field}" else ""}", 50f, yPos, paint.apply {
                        this.color = android.graphics.Color.parseColor("#25324B")
                        this.textSize = 11f
                        this.isFakeBoldText = true
                    })
                    canvas.drawText(edu.school, 50f, yPos + 14f, paint.apply {
                        this.color = android.graphics.Color.parseColor("#4640DE")
                        this.textSize = 10f
                    })
                    canvas.drawText("${edu.startDate} - ${edu.endDate}", 400f, yPos, paint.apply {
                        this.color = android.graphics.Color.parseColor("#7C8493")
                        this.textSize = 9f
                    })
                    yPos += 28f
                }
            }

            if (resume.skills.isNotEmpty()) {
                yPos += 10f
                canvas.drawText("SKILLS", 50f, yPos, paint.apply {
                    this.color = android.graphics.Color.parseColor("#4640DE")
                    this.textSize = 12f
                    this.isFakeBoldText = true
                })
                yPos += 18f
                val skillsText = resume.skills.joinToString("  |  ")
                val skillLines = wrapText(skillsText, 100)
                skillLines.forEach { line ->
                    canvas.drawText(line, 50f, yPos, paint.apply {
                        this.color = android.graphics.Color.parseColor("#444444")
                        this.textSize = 10f
                        this.isFakeBoldText = false
                    })
                    yPos += 14f
                }
            }

            document.finishPage(page)

            val fileName = "${resume.personalInfo.fullName.ifBlank { "resume" }.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}

private fun sharePdf(context: Context, file: File, name: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Resume - $name")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Resume"))
}

private fun wrapText(text: String, maxChars: Int): List<String> {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()
    words.forEach { word ->
        if (currentLine.length + word.length + 1 > maxChars) {
            lines.add(currentLine.toString())
            currentLine = StringBuilder(word)
        } else {
            if (currentLine.isNotEmpty()) currentLine.append(" ")
            currentLine.append(word)
        }
    }
    if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
    return lines
}
