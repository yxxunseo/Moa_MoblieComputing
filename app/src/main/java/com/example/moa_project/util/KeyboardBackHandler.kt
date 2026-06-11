@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moa_project.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/** 입력 폼 시트: 뒤로가기로 시트가 닫히지 않게 */
val FormSheetProperties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)

private fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun InputMethodManager.hideFromToken(token: android.os.IBinder?) {
    if (token == null) return
    hideSoftInputFromWindow(token, InputMethodManager.HIDE_IMPLICIT_ONLY)
    hideSoftInputFromWindow(token, 0)
}

/** 갤럭시 등에서도 동작하도록 Activity window + IME controller 기준으로 키보드를 내린다. */
private fun hideKeyboardNow(
    context: Context,
    view: View,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
) {
    val activity = context.findActivity()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    focusManager.clearFocus(force = true)
    keyboardController?.hide()

    activity?.currentFocus?.clearFocus()

    activity?.window?.let { window ->
        WindowCompat.getInsetsController(window, view).hide(WindowInsetsCompat.Type.ime())
    }

    fun hideAll() {
        imm?.hideFromToken(activity?.currentFocus?.windowToken)
        imm?.hideFromToken(view.findFocus()?.windowToken)
        imm?.hideFromToken(view.windowToken)
        imm?.hideFromToken(view.rootView?.windowToken)
        imm?.hideFromToken(activity?.window?.decorView?.windowToken)
    }

    hideAll()
    view.post { hideAll() }
    view.postDelayed({ hideAll() }, 50L)
}

@Composable
private fun rememberKeyboardVisibleState(): MutableState<Boolean> {
    val visible = remember { mutableStateOf(false) }
    val view = LocalView.current
    DisposableEffect(view) {
        val rect = Rect()
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            visible.value = (screenHeight - rect.bottom) > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }
    return visible
}

private fun shouldBlockSheetDismiss(
    view: View,
    keyboardVisible: Boolean,
    hasDraftInput: Boolean,
): Boolean {
    if (hasDraftInput) return true
    if (keyboardVisible) return true
    if (view.findFocus()?.isFocused == true) return true
    return false
}

/**
 * 시트/다이얼로그에서 뒤로가기 → **키보드만** 내림. 시트는 닫지 않음.
 * ModalBottomSheet content **맨 아래**에 두면 BackHandler 우선순위가 가장 높아짐.
 */
@Composable
fun KeyboardHideBackHandler() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    BackHandler {
        hideKeyboardNow(context, view, focusManager, keyboardController)
    }
}

@Composable
fun rememberFormSheetState(
    skipPartiallyExpanded: Boolean = true,
    hasDraftInput: () -> Boolean = { false },
): SheetState {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val keyboardVisible = rememberKeyboardVisibleState()

    return rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = { target ->
            if (target != SheetValue.Hidden) return@rememberModalBottomSheetState true
            if (shouldBlockSheetDismiss(view, keyboardVisible.value, hasDraftInput())) {
                hideKeyboardNow(context, view, focusManager, keyboardController)
                false
            } else {
                true
            }
        },
    )
}

/** 스와이프·바깥 탭으로 내릴 때: 입력/키보드 중이면 키보드만 내리고 시트 유지 */
@Composable
fun rememberFormSheetDismissRequest(
    hasDraftInput: () -> Boolean = { false },
    onDismiss: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val keyboardVisible = rememberKeyboardVisibleState()

    return {
        if (shouldBlockSheetDismiss(view, keyboardVisible.value, hasDraftInput())) {
            hideKeyboardNow(context, view, focusManager, keyboardController)
        } else {
            onDismiss()
        }
    }
}

// 하위 호환
@Composable
fun rememberKeyboardGuardedSheetState(skipPartiallyExpanded: Boolean = true): SheetState =
    rememberFormSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

@Composable
fun rememberDismissOnBackUnlessKeyboard(onDismiss: () -> Unit): () -> Unit =
    rememberFormSheetDismissRequest(onDismiss = onDismiss)

@Composable
fun KeyboardAwareBackHandler(onDismiss: () -> Unit = {}) {
    KeyboardHideBackHandler()
}
