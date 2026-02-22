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
    val state by viewModel.uiState.collectAsState()
    
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = state.authResult is AuthResult.Loading
    val error = (state.authResult as? AuthResult.Error)?.message 
        ?: if (state.authResult is AuthResult.InvalidCredentials) "Invalid login credentials."
        else if (state.authResult is AuthResult.EmailNotVerified) "Email verification required."
        else if (state.authResult is AuthResult.NotApproved) "Your account is pending approval."
        else null

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
                text = "OfficeGrid",
                style = MaterialTheme.typography.displaySmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = DeepCharcoal
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Professional Inputs
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Email Address", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("you@company.com", style = MaterialTheme.typography.bodyMedium.copy(color = StoneGray)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(2.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepCharcoal,
                        unfocusedBorderColor = WarmBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Password", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !isLoading,
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
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(color = DeepCharcoal, strokeWidth = 1.dp, modifier = Modifier.size(24.dp))
            } else {
                Button(
                    onClick = viewModel::login,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                ) {
                    Text("Log In", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                TextButton(
                    onClick = onNavigateToSignup,
                    modifier = Modifier.align(Alignment.Start),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Don't have an account? Sign Up",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate
                    )
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = it,
                    color = ProfessionalError,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1.5f))
            
            Text(
                "OfficeGrid v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = WarmBorder
            )
        }
    }
}
