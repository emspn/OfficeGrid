package com.app.officegrid.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.app.officegrid.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Brand Anchor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DeepCharcoal, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "OFFICE_GRID",
                style = MaterialTheme.typography.displaySmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = DeepCharcoal
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enterprise Workspace Portal",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Professional Inputs
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("EMAIL_ADDRESS", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("user@company.com", style = MaterialTheme.typography.bodyMedium.copy(color = StoneGray)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !state.isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(2.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepCharcoal,
                        unfocusedBorderColor = WarmBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("PASSWORD", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !state.isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(2.dp),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = StoneGray, modifier = Modifier.size(18.dp))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepCharcoal,
                        unfocusedBorderColor = WarmBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
            } else {
                Button(
                    onClick = viewModel::login,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                ) {
                    Text("SIGN_IN", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                TextButton(
                    onClick = onNavigateToSignup,
                    modifier = Modifier.align(Alignment.Start),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Request Access Credentials",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate
                    )
                }
            }

            state.error?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "AUTH_FAILURE: $it",
                    color = ProfessionalError,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1.5f))
            
            Text(
                "OFFICE_GRID // KERNEL v1.2.4",
                style = MaterialTheme.typography.labelSmall,
                color = WarmBorder
            )
        }
    }
}
