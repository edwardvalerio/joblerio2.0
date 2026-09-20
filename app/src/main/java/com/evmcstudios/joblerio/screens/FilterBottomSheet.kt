package com.evmcstudios.joblerio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evmcstudios.joblerio.data.FilterState
import com.evmcstudios.joblerio.data.JobCategories
import com.evmcstudios.joblerio.ui.theme.PrimaryBlue
import com.evmcstudios.joblerio.ui.theme.TextGray
import com.evmcstudios.joblerio.ui.theme.TitleDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: FilterState,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    var selectedRadius by remember { mutableStateOf(currentFilter.radius) }
    var selectedSort by remember { mutableStateOf(currentFilter.sortBy) }
    var selectedCategories by remember { mutableStateOf(currentFilter.categories) }
    var showAllCategories by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Filters",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TitleDark
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Radius",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TitleDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(10, 15, 25, 50).forEach { radius ->
                    FilterChip(
                        text = "${radius} mi",
                        selected = selectedRadius == radius,
                        onClick = { selectedRadius = radius },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sort By",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TitleDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    text = "Relevance",
                    selected = selectedSort == "relevance",
                    onClick = { selectedSort = "relevance" },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    text = "Newest",
                    selected = selectedSort == "age",
                    onClick = { selectedSort = "age" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Category",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TitleDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoriesToShow = if (showAllCategories) {
                    JobCategories.all
                } else {
                    JobCategories.getPopularCategories()
                }
                categoriesToShow.forEach { category ->
                    FilterChip(
                        text = category.name,
                        selected = selectedCategories.contains(category.id),
                        onClick = {
                            selectedCategories = if (selectedCategories.contains(category.id)) {
                                selectedCategories - category.id
                            } else {
                                selectedCategories + category.id
                            }
                        }
                    )
                }
            }

            if (!showAllCategories) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Show all categories",
                    fontSize = 14.sp,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { showAllCategories = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onApply(
                        FilterState(
                            radius = selectedRadius,
                            sortBy = selectedSort,
                            categories = selectedCategories
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(
                    text = "Apply Filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) PrimaryBlue else Color.Transparent
    val textColor = if (selected) Color.White else TitleDark
    val borderColor = if (selected) PrimaryBlue else TextGray.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}
