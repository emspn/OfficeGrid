package com.app.officegrid.tasks.presentation.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.tasks.domain.model.*
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTemplateSelectionScreen(
    onTemplateSelected: (TaskTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }

    val templates = remember(selectedCategory) {
        if (selectedCategory == null) {
            TaskTemplates.getPopular()
        } else {
            TaskTemplates.getByCategory(selectedCategory!!)
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Task Templates",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            "Start faster with pre-built workflows",
                            style = MaterialTheme.typography.labelSmall,
                            color = StoneGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = DeepCharcoal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // Templates List
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    if (selectedCategory == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = ProfessionalSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "MOST_POPULAR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = ProfessionalSuccess
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: TemplateCategory?,
    onCategorySelected: (TemplateCategory?) -> Unit
) {
    data class CategoryItem(val category: TemplateCategory?, val label: String, val icon: ImageVector)

    val categories = listOf(
        CategoryItem(null, "All", Icons.Default.Apps),
        CategoryItem(TemplateCategory.CLIENT_ONBOARDING, "Clients", Icons.Default.Business),
        CategoryItem(TemplateCategory.EMPLOYEE_ONBOARDING, "Employees", Icons.Default.People),
        CategoryItem(TemplateCategory.BUG_FIX_WORKFLOW, "Bug Fix", Icons.Default.BugReport),
        CategoryItem(TemplateCategory.SPRINT_PLANNING, "Sprint", Icons.Default.CalendarMonth),
        CategoryItem(TemplateCategory.MARKETING_CAMPAIGN, "Marketing", Icons.Default.Campaign)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { item ->
            FilterChip(
                selected = selectedCategory == item.category,
                onClick = { onCategorySelected(item.category) },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeepCharcoal,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: TaskTemplate,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            when (template.category) {
                                TemplateCategory.CLIENT_ONBOARDING -> ProfessionalSuccess.copy(alpha = 0.1f)
                                TemplateCategory.EMPLOYEE_ONBOARDING -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                                TemplateCategory.BUG_FIX_WORKFLOW -> ProfessionalError.copy(alpha = 0.1f)
                                TemplateCategory.SPRINT_PLANNING -> ProfessionalWarning.copy(alpha = 0.1f)
                                TemplateCategory.MARKETING_CAMPAIGN -> Color(0xFF8B5CF6).copy(alpha = 0.1f)
                                else -> StoneGray.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCategoryIcon(template.category),
                        contentDescription = null,
                        tint = when (template.category) {
                            TemplateCategory.CLIENT_ONBOARDING -> ProfessionalSuccess
                            TemplateCategory.EMPLOYEE_ONBOARDING -> Color(0xFF3B82F6)
                            TemplateCategory.BUG_FIX_WORKFLOW -> ProfessionalError
                            TemplateCategory.SPRINT_PLANNING -> ProfessionalWarning
                            TemplateCategory.MARKETING_CAMPAIGN -> Color(0xFF8B5CF6)
                            else -> StoneGray
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        template.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = DeepCharcoal
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = StoneGray
                    )
                }

                if (template.popularity > 90) {
                    Surface(
                        color = ProfessionalSuccess.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = ProfessionalSuccess,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Popular",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ProfessionalSuccess
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatItem(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    label = "${template.tasks.size} tasks"
                )
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = template.estimatedDuration
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = StoneGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = StoneGray
        )
    }
}

private fun getCategoryIcon(category: TemplateCategory): ImageVector {
    return when (category) {
        TemplateCategory.CLIENT_ONBOARDING -> Icons.Default.Business
        TemplateCategory.EMPLOYEE_ONBOARDING -> Icons.Default.People
        TemplateCategory.BUG_FIX_WORKFLOW -> Icons.Default.BugReport
        TemplateCategory.SPRINT_PLANNING -> Icons.Default.CalendarMonth
        TemplateCategory.MARKETING_CAMPAIGN -> Icons.Default.Campaign
        TemplateCategory.SALES_PIPELINE -> Icons.AutoMirrored.Filled.TrendingUp
        TemplateCategory.MONTHLY_REPORTING -> Icons.Default.Assessment
        TemplateCategory.PROJECT_KICKOFF -> Icons.Default.Rocket
        TemplateCategory.CUSTOM -> Icons.Default.Apps
    }
}

