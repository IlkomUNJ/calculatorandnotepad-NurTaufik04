package com.example.superapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.round

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperApp()
        }
    }
}

@Composable
fun SuperApp() {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Calculator, 1 = Editor

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SuperApp — Calculator & Editor") })
        },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calc") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Calculator") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Description, contentDescription = "Editor") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Editor") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedTab == 0) {
                CalculatorScreen()
            } else {
                EditorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (display.isEmpty()) "0" else display,
                    fontSize = 28.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
                Text(
                    text = result ?: "",
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colors.primaryVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val buttons = listOf(
            listOf("7","8","9","/"),
            listOf("4","5","6","*"),
            listOf("1","2","3","-"),
            listOf("0",".","=","+")
        )

        for (row in buttons) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (btn in row) {
                    CalculatorButton(text = btn, onClick = {
                        when (btn) {
                            "=" -> {
                                val res = try {
                                    val evaluated = simpleEval(display)
                                    result = evaluated
                                } catch (e: Exception) {
                                    result = "Error"
                                }
                            }
                            else -> {
                                display += btn
                            }
                        }
                    }, modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { display = ""; result = null }, modifier = Modifier.weight(1f)) {
                Text("C")
            }
            Button(onClick = {
                if (display.isNotEmpty()) display = display.dropLast(1)
            }, modifier = Modifier.weight(1f)) {
                Text("DEL")
            }
        }
    }
}

@Composable
fun CalculatorButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
    ) {
        Text(text, fontSize = 20.sp)
    }
}

fun simpleEval(expr: String): String {
    // remove spaces
    val s = expr.replace(" ", "")
    if (s.isBlank()) return ""
    // Tokenize numbers and operators
    val nums = mutableListOf<Double>()
    val ops = mutableListOf<Char>()

    var i = 0
    fun applyOp() {
        if (nums.size < 2 || ops.isEmpty()) return
        val b = nums.removeAt(nums.lastIndex)
        val a = nums.removeAt(nums.lastIndex)
        val op = ops.removeAt(ops.lastIndex)
        val res = when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            else -> 0.0
        }
        nums.add(res)
    }
    while (i < s.length) {
        val ch = s[i]
        if (ch.isDigit() || ch == '.') {
            val sb = StringBuilder()
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                sb.append(s[i]); i++
            }
            nums.add(sb.toString().toDouble())
            continue
        } else if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
            // handle operator precedence
            while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(ch)) {
                applyOp()
            }
            ops.add(ch)
        } else {
            throw IllegalArgumentException("Invalid char: $ch")
        }
        i++
    }
    while (ops.isNotEmpty()) applyOp()
    return if (nums.isEmpty()) "" else {
        // format: if integer show without .0
        val v = nums.last()
        if (v == round(v)) v.toLong().toString() else v.toString()
    }
}
fun precedence(c: Char) = when(c) {
    '+', '-' -> 1
    '*', '/' -> 2
    else -> 0
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun EditorScreen() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var textValue by remember { mutableStateOf(TextFieldValue("")) }

    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuOffsetY by remember { mutableStateOf(0f) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Notepad Editor") },
                actions = {
                    IconButton(onClick = {
                        textValue = TextFieldValue("")
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("New file created")
                        }
                    }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New")
                    }

                    IconButton(onClick = {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("Save simulated (persistence not implemented)")
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }

                    IconButton(onClick = {
                        val sel = textValue.selection
                        if (sel.start != sel.end) {
                            val selectedText = textValue.text.substring(sel.start, sel.end)
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedText))
                            val newText = textValue.text.removeRange(sel.start, sel.end)
                            textValue = TextFieldValue(newText, TextRange(sel.start))
                        } else {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("No selection to cut")
                            }
                        }
                    }) {
                        Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                    }

                    IconButton(onClick = {
                        val sel = textValue.selection
                        if (sel.start != sel.end) {
                            val selectedText = textValue.text.substring(sel.start, sel.end)
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedText))
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Copied")
                            }
                        } else {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("No selection to copy")
                            }
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }

                    IconButton(onClick = {
                        val clip = clipboardManager.getText()
                        if (clip != null) {
                            val sel = textValue.selection
                            val start = sel.start.coerceAtLeast(0)
                            val end = sel.end.coerceAtLeast(0)
                            val before = textValue.text.substring(0, start)
                            val after = textValue.text.substring(end)
                            val newText = before + clip.text + after
                            val newCursor = before.length + clip.text.length
                            textValue = TextFieldValue(newText, TextRange(newCursor))
                        } else {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Clipboard empty")
                            }
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(12.dp)) {

            Text(
                text = "Editor (max 1 file). Ketentuan: New, Save (simulasi), Cut/Copy/Paste. Tahan teks yang dipilih untuk menu konteks.",
                style = MaterialTheme.typography.body2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier
                .fillMaxSize()
                .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            // show context menu only when there is a selection
                            if (textValue.selection.start != textValue.selection.end) {
                                showContextMenu = true
                                contextMenuOffsetY = offset.y
                            }
                        }
                    )
                }
            ) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        textValue = newValue
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }

            if (showContextMenu) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    elevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(modifier = Modifier
                        .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = {
                            // Copy
                            val sel = textValue.selection
                            if (sel.start != sel.end) {
                                val selectedText = textValue.text.substring(sel.start, sel.end)
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedText))
                            }
                            showContextMenu = false
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }

                        TextButton(onClick = {
                            // Cut
                            val sel = textValue.selection
                            if (sel.start != sel.end) {
                                val selectedText = textValue.text.substring(sel.start, sel.end)
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedText))
                                val newText = textValue.text.removeRange(sel.start, sel.end)
                                textValue = TextFieldValue(newText, TextRange(sel.start))
                            }
                            showContextMenu = false
                        }) {
                            Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cut")
                        }

                        TextButton(onClick = {
                            // Paste
                            val clip = clipboardManager.getText()
                            if (clip != null) {
                                val sel = textValue.selection
                                val start = sel.start.coerceAtLeast(0)
                                val end = sel.end.coerceAtLeast(0)
                                val before = textValue.text.substring(0, start)
                                val after = textValue.text.substring(end)
                                val newText = before + clip.text + after
                                val newCursor = before.length + clip.text.length
                                textValue = TextFieldValue(newText, TextRange(newCursor))
                            }
                            showContextMenu = false
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste")
                        }
                    }
                }
            }
        }
    }
}
