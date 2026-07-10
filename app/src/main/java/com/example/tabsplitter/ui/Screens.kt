package com.example.tabsplitter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import android.net.Uri
import android.widget.Toast
import android.content.Intent
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import com.example.tabsplitter.data.entity.PaymentStatus
import com.example.tabsplitter.data.entity.Transaction
import com.example.tabsplitter.data.entity.TransactionDirection
import com.example.tabsplitter.data.entity.Category
import com.example.tabsplitter.data.entity.Friend
import com.example.tabsplitter.data.entity.SplitBill
import com.example.tabsplitter.data.entity.SplitBillParticipant
import androidx.compose.ui.text.font.FontFamily

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.CircleShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

@Composable
fun getOwesYouColor(): Color {
    return if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
}

@Composable
fun getYouOweColor(): Color {
    return if (isSystemInDarkTheme()) Color(0xFFE57373) else Color(0xFFC62828)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: TabSplitterViewModel,
    onAddFriendClick: () -> Unit,
    onSplitBillClick: () -> Unit,
    onTabClick: (tabId: Long, friendName: String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val openTabsWithFriend by viewModel.openTabsWithFriend.collectAsState(initial = emptyList())
    var editingFriend by remember { mutableStateOf<TabWithFriend?>(null) }

    var showHomeAddMenu by remember { mutableStateOf(false) }
    var showExistingFriendSelector by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val themeColor = if (isDark) Color.Black else Color.White
    val contentColor = if (isDark) Color.White else Color.Black

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        floatingActionButton = {
            Box(modifier = Modifier.padding(bottom = bottomBarPadding, end = 8.dp)) {
                FloatingActionButton(
                    onClick = { showHomeAddMenu = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Menu")
                }
                
                androidx.compose.material3.DropdownMenu(
                    expanded = showHomeAddMenu,
                    onDismissRequest = { showHomeAddMenu = false },
                    offset = androidx.compose.ui.unit.DpOffset(0.dp, (-80).dp)
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Start new tab") },
                        onClick = {
                            showHomeAddMenu = false
                            onAddFriendClick()
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Start tab with existing friend") },
                        onClick = {
                            showHomeAddMenu = false
                            showExistingFriendSelector = true
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = bottomBarPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (openTabsWithFriend.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No open tabs yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the + button to start a tab.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(openTabsWithFriend) { item ->
                    val netBalance = item.tab.totalOwedToMe - item.tab.totalOwedByMe
                    val brush = remember(netBalance) {
                        if (netBalance >= 0.01) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00E676), Color(0xFF1B5E20))
                            )
                        } else if (netBalance <= -0.01) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE53935), Color(0xFFFF6F00))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0D47A1), Color(0xFF00B0FF))
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onTabClick(item.tab.id, item.friendName) },
                                onLongClick = { editingFriend = item }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(brush)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = item.friendName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val amountVal = if (netBalance >= 0.01) netBalance else if (netBalance <= -0.01) -netBalance else 0.0
                                    if (amountVal > 0.0) {
                                        Text(
                                            text = "₹${amountVal.toInt()}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = Color.White
                                        )
                                        val labelText = if (netBalance >= 0.01) "owes you" else "you owe"
                                        Text(
                                            text = labelText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    } else {
                                        Text(
                                            text = "Settled",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExistingFriendSelector) {
        val availableFriends by viewModel.friendsWithoutOpenTab.collectAsState(initial = emptyList())
        AlertDialog(
            onDismissRequest = { showExistingFriendSelector = false },
            title = { Text("Start Tab with Friend", fontWeight = FontWeight.Bold) },
            text = {
                if (availableFriends.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No friends available to start a tab with.",
                            color = contentColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableFriends) { friend ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.openTabForExistingFriend(context, friend.id) {
                                            showExistingFriendSelector = false
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF161616) else Color(0xFFF3F3F3)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = friend.name,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor
                                        )
                                        Text(
                                            text = friend.upiVpa,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor.copy(alpha = 0.5f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Select",
                                        tint = contentColor.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExistingFriendSelector = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (editingFriend != null) {
        val item = editingFriend!!
        var editName by remember(item) { mutableStateOf(item.friendName) }
        val friendsList by viewModel.friends.collectAsState(initial = emptyList())
        val friendObj = remember(friendsList, item) { friendsList.find { it.id == item.tab.friendId } }
        var editUpiVpa by remember(friendObj) { mutableStateOf(friendObj?.upiVpa ?: "") }
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingFriend = null },
            title = { Text("Edit Friend Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Friend's Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editUpiVpa,
                        onValueChange = { editUpiVpa = it },
                        label = { Text("UPI VPA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Tab")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Tab")
                    }
                }
            },
            confirmButton = {
                val upiPattern = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$".toRegex()
                val isUpiValid = editUpiVpa.isEmpty() || upiPattern.matches(editUpiVpa)
                Button(
                    onClick = {
                        if (editName.isNotBlank() && isUpiValid) {
                            friendObj?.id?.let { friendId ->
                                viewModel.updateFriend(friendId, editName.trim(), editUpiVpa.trim())
                            }
                            editingFriend = null
                        }
                    },
                    enabled = editName.isNotBlank() && isUpiValid
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFriend = null }) {
                    Text("Cancel")
                }
            }
        )

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Tab", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete the tab for $editName? All transaction history will be lost.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTab(item.tab.id)
                            showDeleteConfirmDialog = false
                            editingFriend = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CameraScannerView(
    onScanSuccess: (vpa: String, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            var hasFailedToastBeenShown by remember { mutableStateOf(false) }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val scanner = BarcodeScanning.getClient()

                        imageAnalysis.setAnalyzer(
                            ctx.mainExecutor
                        ) { imageProxy ->
                            @OptIn(ExperimentalGetImage::class)
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val rawValue = barcode.rawValue ?: continue
                                            if (rawValue.startsWith("upi://pay", ignoreCase = true)) {
                                                val uri = Uri.parse(rawValue)
                                                val pa = uri.getQueryParameter("pa")
                                                val pn = uri.getQueryParameter("pn") ?: ""
                                                if (!pa.isNullOrBlank()) {
                                                    onScanSuccess(pa, pn)
                                                    imageAnalysis.clearAnalyzer()
                                                    cameraProvider.unbindAll()
                                                    imageProxy.close()
                                                    return@addOnSuccessListener
                                                }
                                            } else {
                                                if (!hasFailedToastBeenShown) {
                                                    hasFailedToastBeenShown = true
                                                    Toast.makeText(ctx, "Not a valid UPI QR code", Toast.LENGTH_SHORT).show()
                                                    previewView.postDelayed({
                                                        hasFailedToastBeenShown = false
                                                    }, 2000)
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            Toast.makeText(ctx, "Use camera failed", Toast.LENGTH_SHORT).show()
                        }
                    }, ctx.mainExecutor)

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Camera",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    viewModel: TabSplitterViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val themeColor = if (isDark) Color.Black else Color.White
    val contentColor = if (isDark) Color.White else Color.Black

    var name by remember { mutableStateOf("") }
    var upiVpa by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val upiPattern = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$".toRegex()
    val isUpiValid = upiVpa.isEmpty() || upiPattern.matches(upiVpa)
    val isFormValid = name.isNotBlank() && upiVpa.isNotEmpty() && upiPattern.matches(upiVpa) && !isSaving

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        } else {
            Toast.makeText(context, "Camera permission is required to scan QR code", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = themeColor,
        topBar = {
            TopAppBar(
                title = { Text("Add Friend", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSaving) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create a new splitter tab by adding friend details below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Friend's Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = upiVpa,
                onValueChange = { upiVpa = it },
                label = { Text("UPI VPA (e.g. name@bank)") },
                placeholder = { Text("friendname@upi") },
                singleLine = true,
                isError = !isUpiValid,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                showScanner = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Scan QR"
                        )
                    }
                },
                supportingText = {
                    if (!isUpiValid) {
                        Text(
                            text = "Invalid UPI format. Must be format like name@bank",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (isFormValid) {
                            isSaving = true
                            viewModel.addFriendDirect(name.trim(), upiVpa.trim()) {
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = isFormValid,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save")
                }

                Button(
                    onClick = {
                        if (isFormValid) {
                            isSaving = true
                            viewModel.addFriendAndTab(context, name.trim(), upiVpa.trim()) {
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp),
                    enabled = isFormValid,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Save & Open Tab",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showScanner) {
        CameraScannerView(
            onScanSuccess = { scannedVpa, scannedName ->
                upiVpa = scannedVpa
                if (name.isBlank() && scannedName.isNotEmpty()) {
                    name = scannedName
                }
                showScanner = false
            },
            onDismiss = {
                showScanner = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TabDetailScreen(
    viewModel: TabSplitterViewModel,
    tabId: Long,
    friendName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabState by viewModel.getTabFlow(tabId).collectAsState(initial = null)
    val transactions by viewModel.getTransactionsForTab(tabId).collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())
    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val currentFriend = remember(friends, tabState) {
        val friendId = tabState?.friendId
        if (friendId != null) friends.find { it.id == friendId } else null
    }
    
    // Multi-selection states
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedTransactionIds = remember { mutableStateListOf<Long>() }
    
    // Editing state
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    
    val dateFormatter = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedTransactionIds.size} Selected", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Tab: $friendName", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedTransactionIds.clear()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Selection"
                            )
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            viewModel.deleteTransactions(tabId, selectedTransactionIds.toList())
                            isSelectionMode = false
                            selectedTransactionIds.clear()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Header Card
            tabState?.let { tab ->
                val totalByMe = tab.totalOwedByMe
                val totalToMe = tab.totalOwedToMe
                val netBalance = totalToMe - totalByMe
                
                val balanceText: String
                val balanceColor: Color
                if (netBalance >= 0.01) {
                    balanceText = "They owe you: ₹${"%.2f".format(netBalance)}"
                    balanceColor = getOwesYouColor()
                } else if (netBalance <= -0.01) {
                    balanceText = "You owe: ₹${"%.2f".format(-netBalance)}"
                    balanceColor = getYouOweColor()
                } else {
                    balanceText = "Settled up"
                    balanceColor = MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = balanceText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = balanceColor,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Owed by me", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text("₹${"%.2f".format(totalByMe)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Owed to me", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text("₹${"%.2f".format(totalToMe)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            Text(
                "Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No transactions recorded for this tab yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(transactions) { transaction ->
                        val dateStr = remember(transaction.createdAt) {
                            dateFormatter.format(java.util.Date(transaction.createdAt))
                        }
                        val category = categoryMap[transaction.categoryId]
                        val isSelected = selectedTransactionIds.contains(transaction.id)
                        val isPaid = transaction.paymentStatus == PaymentStatus.PAID
                        
                        // Greying-out / opacity styling for paid transactions
                        val contentAlpha = if (isPaid) 0.4f else 1.0f
                        val descriptionDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedTransactionIds.clear()
                                            selectedTransactionIds.add(transaction.id)
                                        }
                                    },
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (isSelected) {
                                                selectedTransactionIds.remove(transaction.id)
                                                if (selectedTransactionIds.isEmpty()) {
                                                    isSelectionMode = false
                                                }
                                            } else {
                                                selectedTransactionIds.add(transaction.id)
                                            }
                                        } else {
                                            editingTransaction = transaction
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .alpha(contentAlpha),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Circular Select checkbox
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                                    shape = androidx.compose.foundation.shape.CircleShape
                                                )
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = transaction.description,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    textDecoration = descriptionDecoration
                                                ),
                                                fontWeight = FontWeight.Bold
                                            )
                                            category?.let { cat ->
                                                val catColor = try {
                                                    Color(android.graphics.Color.parseColor(cat.colorHex))
                                                } catch (e: Exception) {
                                                    MaterialTheme.colorScheme.secondary
                                                }
                                                Surface(
                                                    color = catColor.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.5f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Surface(
                                                            modifier = Modifier.size(6.dp),
                                                            shape = androidx.compose.foundation.shape.CircleShape,
                                                            color = catColor
                                                        ) {}
                                                        Text(
                                                            text = cat.name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = catColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "₹${"%.2f".format(transaction.amount)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // Direction Badge: 'You paid' for I_OWE / 'They paid' for THEY_OWE
                                    val directionLabel = if (transaction.direction == TransactionDirection.I_OWE) "You paid" else "They paid"
                                    val directionColor = if (transaction.direction == TransactionDirection.I_OWE) getOwesYouColor() else getYouOweColor()
                                    Text(
                                        text = directionLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = directionColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Pinned Inline Add Transaction Form
            if (!isSelectionMode) {
                var isAddExpanded by remember { mutableStateOf(false) }

                if (!isAddExpanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAddExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add transaction",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    var amountText by remember { mutableStateOf("") }
                    var descriptionText by remember { mutableStateOf("") }
                    var selectedDirection by remember { mutableStateOf(TransactionDirection.I_OWE) }
                    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "New Transaction",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { amountText = it },
                                    label = { Text("Amount (₹)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = descriptionText,
                                    onValueChange = { descriptionText = it },
                                    label = { Text("Description (Optional)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Who paid toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Who paid?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.selectableGroup(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedDirection == TransactionDirection.I_OWE,
                                        onClick = { selectedDirection = TransactionDirection.I_OWE },
                                        label = { Text("I paid") }
                                    )
                                    FilterChip(
                                        selected = selectedDirection == TransactionDirection.THEY_OWE,
                                        onClick = { selectedDirection = TransactionDirection.THEY_OWE },
                                        label = { Text("They paid") }
                                    )
                                }
                            }

                            // Category picker
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Category",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isUncategorizedSelected = selectedCategoryId == null
                                    FilterChip(
                                        selected = isUncategorizedSelected,
                                        onClick = { selectedCategoryId = null },
                                        label = { Text("Uncategorized") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                        )
                                    )

                                    categories.forEach { category ->
                                        val isSelected = selectedCategoryId == category.id
                                        val chipColor = try {
                                            Color(android.graphics.Color.parseColor(category.colorHex))
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.secondary
                                        }
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedCategoryId = category.id },
                                            label = { Text(category.name) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = chipColor.copy(alpha = 0.15f),
                                                labelColor = chipColor,
                                                selectedContainerColor = chipColor,
                                                selectedLabelColor = Color.White
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = isSelected,
                                                borderColor = chipColor,
                                                selectedBorderColor = chipColor
                                            )
                                        )
                                    }
                                }
                            }

                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { isAddExpanded = false }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val amount = amountText.toDoubleOrNull()
                                        if (amount != null && amount > 0.0) {
                                            viewModel.addTransaction(
                                                tabId = tabId,
                                                amount = amount,
                                                description = if (descriptionText.isBlank()) "Expense" else descriptionText.trim(),
                                                direction = selectedDirection,
                                                categoryId = selectedCategoryId
                                            )
                                            isAddExpanded = false
                                        }
                                    },
                                    enabled = amountText.toDoubleOrNull() != null && amountText.toDouble() > 0.0
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Row Tap Editing / Deleting Dialog
    if (editingTransaction != null) {
        val tx = editingTransaction!!
        var editAmountText by remember(tx) { mutableStateOf(tx.amount.toString()) }
        var editDescriptionText by remember(tx) { mutableStateOf(tx.description) }
        var editDirection by remember(tx) { mutableStateOf(tx.direction) }
        var editCategoryId by remember(tx) { mutableStateOf<Long?>(tx.categoryId) }
        var editPaymentStatus by remember(tx) { mutableStateOf(tx.paymentStatus) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { editingTransaction = null },
            title = { Text("Edit Transaction", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { editAmountText = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDescriptionText,
                        onValueChange = { editDescriptionText = it },
                        label = { Text("Description (Optional)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Who paid toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Who paid?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = editDirection == TransactionDirection.I_OWE,
                                onClick = { editDirection = TransactionDirection.I_OWE },
                                label = { Text("I paid") }
                            )
                            FilterChip(
                                selected = editDirection == TransactionDirection.THEY_OWE,
                                onClick = { editDirection = TransactionDirection.THEY_OWE },
                                label = { Text("They paid") }
                            )
                        }
                    }

                    // Category picker
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Category",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isUncategorizedSelected = editCategoryId == null
                            FilterChip(
                                selected = isUncategorizedSelected,
                                onClick = { editCategoryId = null },
                                label = { Text("Uncategorized") },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                )
                            )

                            categories.forEach { category ->
                                val isSelected = editCategoryId == category.id
                                val chipColor = try {
                                    Color(android.graphics.Color.parseColor(category.colorHex))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.secondary
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { editCategoryId = category.id },
                                    label = { Text(category.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = chipColor.copy(alpha = 0.15f),
                                        labelColor = chipColor,
                                        selectedContainerColor = chipColor,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = chipColor,
                                        selectedBorderColor = chipColor
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isTxPaid = editPaymentStatus == PaymentStatus.PAID
                        Button(
                            onClick = {
                                editPaymentStatus = if (isTxPaid) PaymentStatus.PENDING else PaymentStatus.PAID
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTxPaid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isTxPaid) "Mark as Unpaid" else "Mark as Paid")
                        }

                        if (editPaymentStatus == PaymentStatus.PENDING && editDirection == TransactionDirection.THEY_OWE) {
                            Button(
                                onClick = {
                                    val recipientVpa = currentFriend?.upiVpa ?: ""
                                    val recipientName = currentFriend?.name ?: ""
                                    if (recipientVpa.isNotEmpty()) {
                                        val upiUri = Uri.parse(
                                            "upi://pay?pa=$recipientVpa" +
                                                    "&pn=${Uri.encode(recipientName)}" +
                                                    "&am=${editAmountText.toDoubleOrNull() ?: tx.amount}" +
                                                    "&cu=INR" +
                                                    "&tn=${Uri.encode(editDescriptionText.ifBlank { "TabSplitter Payment" })}"
                                        )
                                        val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                        val chooser = Intent.createChooser(upiIntent, "Pay with UPI")
                                        try {
                                            context.startActivity(chooser)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No UPI app installed", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Recipient VPA is missing", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pay via UPI")
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            viewModel.deleteTransactions(tabId, listOf(tx.id))
                            editingTransaction = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Single Transaction")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Transaction")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = editAmountText.toDoubleOrNull()
                        if (amount != null && amount > 0.0) {
                            viewModel.updateTransaction(
                                transactionId = tx.id,
                                newAmount = amount,
                                newDescription = if (editDescriptionText.isBlank()) "Expense" else editDescriptionText.trim(),
                                newDirection = editDirection,
                                newCategoryId = editCategoryId,
                                newPaymentStatus = editPaymentStatus
                            )
                            editingTransaction = null
                        }
                    },
                    enabled = editAmountText.toDoubleOrNull() != null && editAmountText.toDouble() > 0.0
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTransaction = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: TabSplitterViewModel,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp
) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsState(initial = emptyList())
    val monthlyTotals by viewModel.monthlyTotals.collectAsState(initial = MonthlyTotals(0.0, 0.0, 0.0))
    val monthlyCategorySpending by viewModel.monthlyCategorySpending.collectAsState(initial = emptyList())
    val monthlyDetailedTransactions by viewModel.monthlyDetailedTransactions.collectAsState(initial = emptyList())

    var categorySortBy by remember { mutableStateOf("highest") } // "highest" or "alphabetical"
    var transactionSortBy by remember { mutableStateOf("date") } // "date" or "amount"

    val sortedCategorySpending = remember(monthlyCategorySpending, categorySortBy) {
        val nonZero = monthlyCategorySpending.filter { it.totalAmount > 0.0 }
        when (categorySortBy) {
            "alphabetical" -> nonZero.sortedBy { it.category.name.lowercase() }
            else -> nonZero.sortedByDescending { it.totalAmount }
        }
    }

    val sortedDetailedTransactions = remember(monthlyDetailedTransactions, transactionSortBy) {
        when (transactionSortBy) {
            "amount" -> monthlyDetailedTransactions.sortedByDescending { it.transaction.amount }
            else -> monthlyDetailedTransactions.sortedByDescending { it.transaction.createdAt }
        }
    }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthText = "${months[currentYearMonth.second]} ${currentYearMonth.first}"

    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd MMM, yyyy", java.util.Locale.getDefault())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = bottomBarPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector Header: ◀ [Month Year] ▶
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectPreviousMonth() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = monthText,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    IconButton(onClick = { viewModel.selectNextMonth() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = Color.White
                        )
                    }
                }
            }

            // Totals Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main Total Spent Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${monthlyTotals.totalSpent.toInt()}",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    // Secondary Stats: Owe / Owed to You side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "You Owe",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${monthlyTotals.totalYouOwe.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = getYouOweColor()
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Owed to You",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${monthlyTotals.totalOwedToYou.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = getOwesYouColor()
                                )
                            }
                        }
                    }
                }
            }

            // Section Title: Category Breakdown with Sorting
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    TextButton(onClick = {
                        categorySortBy = if (categorySortBy == "highest") "alphabetical" else "highest"
                    }) {
                        Text(
                            text = if (categorySortBy == "highest") "Sort: Value" else "Sort: A-Z",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Category list scoped to selected month and sorted

            if (sortedCategorySpending.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No category spending recorded.",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(sortedCategorySpending) { item ->
                    val color = remember(item.category.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(item.category.colorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161616), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color, CircleShape)
                            )
                            Text(
                                text = item.category.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "₹${item.totalAmount.toInt()}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Section Title: Monthly Transactions with Sorting
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions this Month",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    TextButton(onClick = {
                        transactionSortBy = if (transactionSortBy == "date") "amount" else "date"
                    }) {
                        Text(
                            text = if (transactionSortBy == "date") "Sort: Date" else "Sort: Amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Transaction list scoped to selected month and sorted

            if (sortedDetailedTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions in $monthText",
                            color = Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(sortedDetailedTransactions) { item ->
                    val tx = item.transaction
                    val directionColor = if (tx.direction == TransactionDirection.THEY_OWE) getOwesYouColor() else getYouOweColor()
                    val prefix = if (tx.direction == TransactionDirection.THEY_OWE) "+" else "-"
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161616), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.friendName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                item.category?.let { cat ->
                                    val catColor = remember(cat.colorHex) {
                                        try {
                                            Color(android.graphics.Color.parseColor(cat.colorHex))
                                        } catch (e: Exception) {
                                            Color.Gray
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(catColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = cat.name,
                                            color = catColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            if (tx.description.isNotBlank()) {
                                Text(
                                    text = tx.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text = dateFormatter.format(tx.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                        Text(
                            text = "$prefix₹${tx.amount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = directionColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillScreen(
    viewModel: TabSplitterViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())

    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    
    // Selection states
    val selectedFriendIds = remember { mutableStateListOf<Long>() }
    
    // Custom split share states: maps friendId to text input
    val customShares = remember { mutableStateMapOf<Long, String>() }
    var isCustomSplit by remember { mutableStateOf(false) }

    // Inline Add Friend State
    var showInlineAddFriend by remember { mutableStateOf(false) }
    var inlineName by remember { mutableStateOf("") }
    var inlineUpiVpa by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }

    val upiPattern = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$".toRegex()
    val isInlineUpiValid = inlineUpiVpa.isEmpty() || upiPattern.matches(inlineUpiVpa)
    val isInlineFormValid = inlineName.isNotBlank() && inlineUpiVpa.isNotEmpty() && upiPattern.matches(inlineUpiVpa)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        } else {
            Toast.makeText(context, "Camera permission is required to scan QR code", Toast.LENGTH_LONG).show()
        }
    }

    val amount = amountStr.toDoubleOrNull() ?: 0.0

    // Calculate live shares
    val equalShare = if (selectedFriendIds.isNotEmpty() && amount > 0.0) {
        amount / selectedFriendIds.size
    } else {
        0.0
    }

    // Live shares mapped to double
    val finalShares = remember(isCustomSplit, selectedFriendIds, equalShare, customShares) {
        selectedFriendIds.associateWith { friendId ->
            if (isCustomSplit) {
                customShares[friendId]?.toDoubleOrNull() ?: 0.0
            } else {
                equalShare
            }
        }
    }

    val sumCustomShares = finalShares.values.sum()
    val remainingAmount = amount - sumCustomShares

    val isFormValid = amount > 0.0 &&
            selectedFriendIds.isNotEmpty() &&
            (!isCustomSplit || Math.abs(remainingAmount) < 0.01)

    val isDark = isSystemInDarkTheme()
    val themeColor = if (isDark) Color.Black else Color.White
    val contentColor = if (isDark) Color.White else Color.Black

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = themeColor,
        topBar = {
            TopAppBar(
                title = { Text("Split Bill", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total Bill Amount Input
                    item {
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Total Amount (₹)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Optional Description Input
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (Optional)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Category Selector (Chips)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Category",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = contentColor.copy(alpha = 0.6f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { category ->
                                    val isSelected = selectedCategoryId == category.id
                                    val color = remember(category.colorHex) {
                                        try {
                                            Color(android.graphics.Color.parseColor(category.colorHex))
                                        } catch (e: Exception) {
                                            Color.Gray
                                        }
                                    }
                                    val chipShape = RoundedCornerShape(20.dp)
                                    Box(
                                        modifier = Modifier
                                            .clip(chipShape)
                                            .background(
                                                if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isSelected) color else color.copy(alpha = 0.3f),
                                                shape = chipShape
                                            )
                                            .clickable {
                                                selectedCategoryId = if (isSelected) null else category.id
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = category.name,
                                            color = if (isSelected) color else contentColor.copy(alpha = 0.8f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Split Style Toggle: Equal vs Custom
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Custom Split",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = contentColor
                                )
                                Text(
                                    text = "Enter individual shares manually",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor.copy(alpha = 0.5f)
                                )
                            }
                            Switch(
                                checked = isCustomSplit,
                                onCheckedChange = { isCustomSplit = it }
                            )
                        }
                    }

                    // Select Friends List Header with Add Inline Action
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Split With Friends",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = contentColor
                            )
                            TextButton(onClick = { showInlineAddFriend = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Friend")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Friend")
                            }
                        }
                    }

                    if (friends.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No friends added yet. Tap Add Friend to start.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(friends) { friend ->
                            val isSelected = selectedFriendIds.contains(friend.id)
                            val currentShareStr = customShares[friend.id] ?: ""
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDark) Color(0xFF161616) else Color(0xFFF3F3F3),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                selectedFriendIds.add(friend.id)
                                                customShares[friend.id] = ""
                                            } else {
                                                selectedFriendIds.remove(friend.id)
                                                customShares.remove(friend.id)
                                            }
                                        }
                                    )
                                    Text(
                                        text = friend.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = contentColor
                                    )
                                }
                                
                                if (isSelected) {
                                    if (isCustomSplit) {
                                        OutlinedTextField(
                                            value = currentShareStr,
                                            onValueChange = { customShares[friend.id] = it },
                                            placeholder = { Text("₹0") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.width(100.dp),
                                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                                        )
                                    } else {
                                        Text(
                                            text = "₹${"%.1f".format(equalShare)}",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Info and Action Button
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCustomSplit && amount > 0.0) {
                        val remainingColor = if (Math.abs(remainingAmount) < 0.01) getOwesYouColor() else getYouOweColor()
                        val text = if (remainingAmount >= 0.0) {
                            "Remaining: ₹${"%.1f".format(remainingAmount)}"
                        } else {
                            "Over split: -₹${"%.1f".format(-remainingAmount)}"
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = remainingColor,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else if (!isCustomSplit && selectedFriendIds.isNotEmpty() && amount > 0.0) {
                        Text(
                            text = "Split share: ₹${"%.1f".format(equalShare)} each",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                viewModel.splitBill(
                                    context = context,
                                    totalAmount = amount,
                                    description = description.trim(),
                                    categoryId = selectedCategoryId,
                                    friendShares = finalShares,
                                    onComplete = onBack
                                )
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Submit Split",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Inline Add Friend Dialog
            if (showInlineAddFriend) {
                AlertDialog(
                    onDismissRequest = { showInlineAddFriend = false },
                    title = { Text("Add Friend Inline", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = inlineName,
                                onValueChange = { inlineName = it },
                                label = { Text("Friend's Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = inlineUpiVpa,
                                onValueChange = { inlineUpiVpa = it },
                                label = { Text("UPI VPA") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = !isInlineUpiValid,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val permissionCheck = ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.CAMERA
                                        )
                                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                            showScanner = true
                                        } else {
                                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                    }) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = "Scan QR")
                                    }
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isInlineFormValid) {
                                        viewModel.addFriendDirect(inlineName.trim(), inlineUpiVpa.trim()) { newFriendId ->
                                            selectedFriendIds.add(newFriendId)
                                            showInlineAddFriend = false
                                            inlineName = ""
                                            inlineUpiVpa = ""
                                        }
                                    }
                                },
                                enabled = isInlineFormValid,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save")
                            }

                            Button(
                                onClick = {
                                    if (isInlineFormValid) {
                                        viewModel.addFriendAndTabDirect(context, inlineName.trim(), inlineUpiVpa.trim()) { newFriendId ->
                                            selectedFriendIds.add(newFriendId)
                                            showInlineAddFriend = false
                                            inlineName = ""
                                            inlineUpiVpa = ""
                                        }
                                    }
                                },
                                enabled = isInlineFormValid,
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("Save & Tab")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showInlineAddFriend = false
                            inlineName = ""
                            inlineUpiVpa = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Scanning Camera Dialog overlay
            if (showScanner) {
                CameraScannerView(
                    onScanSuccess = { scannedVpa, scannedName ->
                        inlineUpiVpa = scannedVpa
                        if (inlineName.isBlank() && scannedName.isNotEmpty()) {
                            inlineName = scannedName
                        }
                        showScanner = false
                    },
                    onDismiss = {
                        showScanner = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SplitBillsListScreen(
    viewModel: TabSplitterViewModel,
    onCreateSplitBillClick: () -> Unit,
    onSplitBillClick: (Long) -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val splitBills by viewModel.splitBillsWithParticipantCount.collectAsState(initial = emptyList())
    var splitBillSortBy by remember { mutableStateOf("date") } // "date" or "status"
    var billToDelete by remember { mutableStateOf<SplitBill?>(null) }

    val sortedSplitBills = remember(splitBills, splitBillSortBy) {
        when (splitBillSortBy) {
            "status" -> splitBills.sortedWith(
                compareBy<SplitBillUiItem> { if (it.status == "Pending") 0 else 1 }
                    .thenByDescending { it.splitBill.createdAt }
            )
            else -> splitBills.sortedByDescending { it.splitBill.createdAt }
        }
    }

    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd MMM, yyyy", java.util.Locale.getDefault())
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSplitBillClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = bottomBarPadding)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Split Bill")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = bottomBarPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Split Bills",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    TextButton(onClick = {
                        splitBillSortBy = if (splitBillSortBy == "date") "status" else "date"
                    }) {
                        Text(
                            text = if (splitBillSortBy == "date") "Sort: Date" else "Sort: Status",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Sorted split bills list is already processed

            if (sortedSplitBills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No split bills yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the + button to create a new split bill.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(sortedSplitBills) { item ->
                    val bill = item.splitBill
                    val title = if (bill.description.isNotBlank()) bill.description else "Split Bill"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onSplitBillClick(bill.id) },
                                onLongClick = { billToDelete = bill }
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "₹${bill.totalAmount.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormatter.format(bill.createdAt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val badgeColor = if (item.status == "Settled") getOwesYouColor() else getYouOweColor()
                                    Box(
                                        modifier = Modifier
                                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = item.status,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = badgeColor
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF262626), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${item.participantCount} friends",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (billToDelete != null) {
        val bill = billToDelete!!
        val title = if (bill.description.isNotBlank()) bill.description else "Split Bill"
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = {
                Text(
                    text = "Delete Split Bill",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"$title\"? This action cannot be undone and will remove all participant split records.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f),
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSplitBill(bill.id)
                        billToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { billToDelete = null }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillDetailScreen(
    viewModel: TabSplitterViewModel,
    splitBillId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.getSplitBillDetailFlow(splitBillId).collectAsState(initial = null)
    val dateFormatter = remember {
        java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    }

    val isDark = isSystemInDarkTheme()
    val themeColor = if (isDark) Color.Black else Color.White
    val contentColor = if (isDark) Color.White else Color.Black

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = themeColor,
        topBar = {
            TopAppBar(
                title = { Text("Split Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
            )
        }
    ) { innerPadding ->
        detailState?.let { detail ->
            val bill = detail.splitBill
            val title = if (bill.description.isNotBlank()) bill.description else "Split Bill"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF161616) else Color(0xFFF3F3F3)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = contentColor
                            )
                            detail.category?.let { cat ->
                                val catColor = remember(cat.colorHex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(cat.colorHex))
                                    } catch (e: Exception) {
                                        Color.Gray
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(catColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat.name,
                                        color = catColor,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "₹${bill.totalAmount.toInt()}",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = contentColor
                        )
                        Text(
                            text = "Created on ${dateFormatter.format(bill.createdAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.5f)
                        )
                    }
                }

                Text(
                    text = "Participants",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detail.participants) { pDetail ->
                        val p = pDetail.participant
                        val isPaid = p.paymentStatus == PaymentStatus.PAID
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDark) Color(0xFF161616) else Color(0xFFF3F3F3),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = pDetail.friendName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = contentColor
                                )
                                Text(
                                    text = "Share: ₹${p.shareAmount.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor.copy(alpha = 0.6f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isPaid) "Paid" else "Pending",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = if (isPaid) getOwesYouColor() else getYouOweColor()
                                )
                                Switch(
                                    checked = isPaid,
                                    onCheckedChange = {
                                        viewModel.toggleParticipantPaymentStatus(p)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    viewModel: TabSplitterViewModel,
    bottomBarPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    var editingFriend by remember { mutableStateOf<Friend?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = bottomBarPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            if (friends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No friends added yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(friends) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { editingFriend = friend }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = friend.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = friend.upiVpa,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Friend Details",
                                tint = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingFriend != null) {
        val friendObj = editingFriend!!
        var editName by remember(friendObj) { mutableStateOf(friendObj.name) }
        var editUpiVpa by remember(friendObj) { mutableStateOf(friendObj.upiVpa) }

        AlertDialog(
            onDismissRequest = { editingFriend = null },
            title = { Text("Edit Friend Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Friend's Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editUpiVpa,
                        onValueChange = { editUpiVpa = it },
                        label = { Text("UPI VPA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val upiPattern = "^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$".toRegex()
                val isUpiValid = editUpiVpa.isEmpty() || upiPattern.matches(editUpiVpa)
                Button(
                    onClick = {
                        if (editName.isNotBlank() && isUpiValid) {
                            viewModel.updateFriend(friendObj.id, editName.trim(), editUpiVpa.trim())
                            editingFriend = null
                        }
                    },
                    enabled = editName.isNotBlank() && isUpiValid
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFriend = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
