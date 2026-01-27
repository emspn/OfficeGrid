package com.app.officegrid.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.officegrid.core.common.UserRole
import com.app.officegrid.ui.theme.PrimaryModern
import com.app.officegrid.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    role: UserRole,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Branding Section
            Surface(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (role == UserRole.ADMIN) Icons.Default.AddBusiness else Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (role == UserRole.ADMIN) "Create Organisation" else "Join Team",
                style = MaterialTheme.typography.displaySmall,
                color = Slate900,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = if (role == UserRole.ADMIN) "Setup your company workspace" else "Connect with your workplace",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Unified Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Profile Info Label
                    Text(
                        text = "PERSONAL INFORMATION",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryModern
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = viewModel::onFullNameChange,
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryModern) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Work Email") },
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = PrimaryModern) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !state.isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Organisation Info Label
                    Text(
                        text = "ORGANISATION DETAILS",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryModern
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (role == UserRole.ADMIN) {
                        OutlinedTextField(
                            value = organisationName,
                            onValueChange = viewModel::onOrganisationNameChange,
                            label = { Text("Organisation Name") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryModern) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = orgTypeExpanded,
                            onExpandedChange = { if (!state.isLoading) orgTypeExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = organisationType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Organisation Type") },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = PrimaryModern) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orgTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                enabled = !state.isLoading,
                                shape = RoundedCornerShape(16.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = orgTypeExpanded,
                                onDismissRequest = { orgTypeExpanded = false }
                            ) {
                                orgTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            viewModel.onOrganisationTypeChange(type)
                                            orgTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Organisation ID Field
                    OutlinedTextField(
                        value = companyId,
                        onValueChange = viewModel::onCompanyIdChange,
                        label = { Text("Organisation ID") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimaryModern) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. ACME-123") },
                        enabled = !state.isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        supportingText = {
                            Text(
                                if (role == UserRole.ADMIN) "Share this ID with your team to join." else "Enter the ID provided by your admin.",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Security Label
                    Text(
                        text = "SECURITY",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryModern
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = PrimaryModern) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !state.isLoading,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = PrimaryModern)
            } else {
                Button(
                    onClick = { viewModel.signup(role) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (role == UserRole.ADMIN) "Create Workspace" else "Join Workspace",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Already have an account? Sign In",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryModern,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}