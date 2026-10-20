package com.iosfb.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.iosfb.app.web.FbTab
import com.iosfb.app.web.TabHost
import com.iosfb.app.web.TabState

@Composable
fun FacebookApp(host: TabHost) {
    val current = host.current
    val cs = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // Back: page history first, then non-home tab -> Home, then exit.
    BackHandler(enabled = !current.canGoBack && host.selected != FbTab.HOME) { host.selected = FbTab.HOME }
    BackHandler(enabled = current.canGoBack) { current.goBack() }

    Column(
        Modifier
            .fillMaxSize()
            .background(cs.background)
            .imePadding(),
    ) {
        TopBar(
            tab = host.selected,
            canGoBack = current.canGoBack,
            onBack = { current.goBack() },
            onSearch = { current.open("/search/") },
        )

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Crossfade(targetState = host.selected, animationSpec = tween(180), label = "tab") { tab ->
                WebTab(host.state(tab))
            }

            val p by animateFloatAsState(current.progress / 100f, label = "progress")
            FadeVisibility(
                visible = current.progress in 1..99,
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = IosColors.Blue,
                    trackColor = Color.Transparent,
                )
            }

            FadeVisibility(
                visible = !current.firstLoaded && !current.hasError,
            ) { FeedSkeleton() }

            FadeVisibility(visible = current.hasError) {
                ErrorScreen(onRetry = { current.retry() })
            }
        }

        AnimatedVisibility(
            visible = !imeVisible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            TabBar(selected = host.selected) { tab ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (tab == host.selected) host.state(tab).scrollTopOrRefresh() else host.selected = tab
            }
        }
    }
}

@Composable
private fun WebTab(state: TabState) {
    AndroidView(
        factory = { state.detachedLayout() },
        modifier = Modifier.fillMaxSize(),
    )
    DisposableEffect(state) {
        state.resume()
        state.ensureLoaded()
        onDispose { state.pause() }
    }
}

@Composable
private fun TopBar(tab: FbTab, canGoBack: Boolean, onBack: () -> Unit, onSearch: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val showWordmark = tab == FbTab.HOME && !canGoBack
    Column(
        Modifier
            .fillMaxWidth()
            .background(cs.surface)
            .statusBarsPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 6.dp),
        ) {
            FadeVisibility(
                visible = showWordmark,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp),
            ) {
                Text(
                    "facebook",
                    color = IosColors.Blue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                )
            }
            FadeVisibility(
                visible = canGoBack,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBackIosNew, "Back", Modifier.size(20.dp), tint = IosColors.Blue)
                }
            }
            FadeVisibility(
                visible = !showWordmark,
                modifier = Modifier.align(Alignment.Center),
            ) {
                Text(tab.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface)
            }
            IconButton(onClick = onSearch, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Filled.Search, "Search", tint = cs.onSurface)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = cs.outlineVariant)
    }
}

@Composable
private fun TabBar(selected: FbTab, onSelect: (FbTab) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .background(cs.surface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = cs.outlineVariant)
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            FbTab.entries.forEach { tab ->
                TabItem(tab, tab == selected, Modifier.weight(1f)) { onSelect(tab) }
            }
        }
    }
}

@Composable
private fun TabItem(tab: FbTab, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "tabScale",
    )
    val tint by animateColorAsState(
        targetValue = if (selected) IosColors.Blue else IosColors.Gray,
        label = "tabTint",
    )
    Column(
        modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = if (selected) tab.iconSelected else tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        )
        Text(tab.label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FeedSkeleton() {
    val cs = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val block = cs.onSurface.copy(alpha = 0.10f * pulse)
    Column(
        Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        repeat(3) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(block))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(width = 140.dp, height = 12.dp).clip(RoundedCornerShape(6.dp)).background(block))
                    Box(Modifier.size(width = 90.dp, height = 10.dp).clip(RoundedCornerShape(5.dp)).background(block))
                }
            }
            Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(14.dp)).background(block))
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ErrorScreen(onRetry: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxSize()
            .background(cs.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.WifiOff, null, Modifier.size(56.dp), tint = IosColors.Gray)
        Spacer(Modifier.height(12.dp))
        Text("No Connection", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = cs.onBackground)
        Spacer(Modifier.height(4.dp))
        Text("Check your internet and try again.", color = IosColors.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(50)) { Text("Try Again") }
    }
}

/**
 * Fade in/out wrapper. Declared at top level (no layout-scope receiver) so it can be called from a
 * Box that is nested inside a Column without the ColumnScope.AnimatedVisibility ambiguity.
 */
@Composable
private fun FadeVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
        content = content,
    )
}
