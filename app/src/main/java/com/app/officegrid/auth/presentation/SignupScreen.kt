package com.app.officegrid.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    role: UserRole,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val companyId by viewModel.companyId.collectAsState()
    val organisationName by viewModel.organisationName.collectAsState()
    val organisationType by viewModel.organisationType.collectAsState()
    
    var passwordVisible by remember { mutableStateOf(false) }
    var orgTypeExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val orgTypes = listOf(
        "Technology", "Software Development", "Finance & Banking", "Healthcare", 
        "Education", "Retail & E-commerce", "Manufacturing", "Logistics & Transport", 
        "Real Estate", "Marketing & Advertising", "Consulting", "Automotive", 
        "Hospitality & Tourism", "Legal Services", "Construction", "Media & Entertainment", 
        "Telecommunications", "Agriculture", "Energy & Utilities", "Non-Profit", 
        "Government", "Others"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Technical Header - Full Width
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepCharcoal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (role == UserRole.ADMIN) "ORG_INITIALIZATION" else "NODE_ENROLLMENT",
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = DeepCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (role == UserRole.ADMIN) "WORKSPACE_PROVISIONING" else "ACCESS_CONFIGURATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = StoneGray
                    )
                }
            }

            // Form Content - Consistent Padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {

            // Section 1: User Identity
            EliteSignupSection(label = "IDENTITY_SPECIFICATIONS") {
                EliteSignupTextField(
                    value = fullName,
                    onValueChange = viewModel::onFullNameChange,
                    placeholder = "FULL_NAME_LITERAL",
                    icon = Icons.Default.Person,
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.height(1.dp))
                EliteSignupTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "AUTH_EMAIL_NODE",
                    icon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.height(1.dp))
                EliteSignupTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "ACCESS_CREDENTIAL",
                    icon = Icons.Default.Lock,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = StoneGray
                            )
                        }
                    },
                    enabled = !state.isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section 2: Organisation Metadata
            EliteSignupSection(label = "WORKSPACE_PARAMETERS") {
                if (role == UserRole.ADMIN) {
                    EliteSignupTextField(
                        value = organisationName,
                        onValueChange = viewModel::onOrganisationNameChange,
                        placeholder = "ORGANISATION_NOMENCLATURE",
                        icon = Icons.Default.Business,
                        enabled = !state.isLoading
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    
                    // Org Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = orgTypeExpanded,
                        onExpandedChange = { if (!state.isLoading) orgTypeExpanded = it }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Category, null, modifier = Modifier.size(18.dp), tint = DeepCharcoal)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = if (organisationType.isBlank()) "SECTOR_CLASSIFICATION" else organisationType.uppercase(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = if (organisationType.isBlank()) WarmBorder else DeepCharcoal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(18.dp), tint = StoneGray)
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = orgTypeExpanded,
                            onDismissRequest = { orgTypeExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            orgTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.uppercase(), style = MaterialTheme.typography.labelSmall) },
                                    onClick = {
                                        viewModel.onOrganisationTypeChange(type)
                                        orgTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                }

                EliteSignupTextField(
                    value = companyId,
                    onValueChange = viewModel::onCompanyIdChange,
                    placeholder = if (role == UserRole.ADMIN) "ASSIGN_UNIQUE_WORKSPACE_ID" else "TARGET_WORKSPACE_KEY",
                    icon = Icons.Default.Key,
                    enabled = !state.isLoading
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DeepCharcoal,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                Surface(
                    onClick = { viewModel.signup(role) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    color = DeepCharcoal,
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (role == UserRole.ADMIN) "INITIALIZE_ORG_STATION" else "REQUEST_NODE_ACCESS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            ),
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "EXISTING_NODE? PROCEED_TO_AUTH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = DeepCharcoal
                    )
                }
            }

            // Error Message
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ProfessionalError.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ERROR: $error",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = ProfessionalError,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun EliteSignupSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                fontSize = 11.sp
            ),
            color = MutedSlate,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun EliteSignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = WarmBorder
            )
        },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = DeepCharcoal
            )
        },
        trailingIcon = trailingIcon,
        enabled = enabled,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = DeepCharcoal,
            focusedTextColor = DeepCharcoal,
            unfocusedTextColor = DeepCharcoal,
            disabledTextColor = StoneGray
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        shape = RoundedCornerShape(2.dp)
    )
}
