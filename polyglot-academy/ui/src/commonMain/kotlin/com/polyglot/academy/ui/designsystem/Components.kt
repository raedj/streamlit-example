package com.polyglot.academy.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages

/** Constrains content width on large screens and applies the screen gutter. */
@Composable
fun ContentColumn(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val sp = PolyglotTheme.spacing
    Box(modifier.fillMaxWidth().padding(padding), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = sp.maxContentWidth).fillMaxWidth().padding(horizontal = sp.gutter),
            verticalArrangement = Arrangement.spacedBy(sp.lg),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    backLabel: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val c = PolyglotTheme.colors
    Scaffold(
        modifier = modifier,
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() }) },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backLabel) }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background, titleContentColor = c.textPrimary),
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        content = content,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    loadingLabel: String? = null,
) {
    val c = PolyglotTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = 52.dp).semantics { if (loading && loadingLabel != null) stateDescription = loadingLabel },
        shape = RoundedCornerShape(PolyglotTheme.radii.md),
        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary, disabledContainerColor = c.disabled, disabledContentColor = c.onDisabled),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = c.onDisabled)
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null) {
    val c = PolyglotTheme.colors
    OutlinedButton(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(PolyglotTheme.radii.md), border = BorderStroke(1.dp, if (enabled) c.border else c.disabled),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = c.primary),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun QuietButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color = PolyglotTheme.colors.primary) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (enabled) color else PolyglotTheme.colors.onDisabled)
    }
}

@Composable
fun PolyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    containerColor: Color = PolyglotTheme.colors.card,
    border: Boolean = true,
    contentPadding: Dp = PolyglotTheme.spacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = PolyglotTheme.colors
    val shape = RoundedCornerShape(PolyglotTheme.radii.lg)
    val base = modifier.fillMaxWidth()
    Card(
        modifier = if (onClick != null) base.clip(shape).clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick) else base,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = c.textPrimary),
        border = if (border) BorderStroke(1.dp, c.divider) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(PolyglotTheme.spacing.sm), content = content)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = PolyglotTheme.colors.textPrimary,
            modifier = Modifier.weight(1f).semantics { heading() })
        if (action != null && onAction != null) QuietButton(action, onAction)
    }
}

@Composable
fun SupportingText(text: String, modifier: Modifier = Modifier, color: Color = PolyglotTheme.colors.textSecondary, style: TextStyle = MaterialTheme.typography.bodyMedium) {
    Text(text, modifier = modifier, style = style, color = color)
}

@Composable
fun StatTile(value: String, label: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    val c = PolyglotTheme.colors
    Column(
        modifier.clip(RoundedCornerShape(PolyglotTheme.radii.lg)).background(c.card).border(1.dp, c.divider, RoundedCornerShape(PolyglotTheme.radii.lg))
            .padding(PolyglotTheme.spacing.md).semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(value, style = PolyglotTheme.learning.statSmall, color = c.textPrimary, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textSecondary, maxLines = 2)
    }
}

/** Circular progress with a centred label. Exposes progress semantics for screen readers. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    stroke: Dp = 9.dp,
    color: Color = PolyglotTheme.colors.primary,
    description: String,
    center: @Composable BoxScope.() -> Unit = {},
) {
    val c = PolyglotTheme.colors
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(PolyglotTheme.motion.duration(700)))
    Box(
        modifier.size(size).semantics { contentDescription = description; progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val arc = Size(this.size.width - s, this.size.height - s)
            drawArc(c.progressTrack, 0f, 360f, false, Offset(s / 2, s / 2), arc, style = Stroke(s))
            drawArc(color, -90f, 360f * animated, false, Offset(s / 2, s / 2), arc, style = Stroke(s, cap = StrokeCap.Round))
        }
        center()
    }
}

@Composable
fun LabeledProgress(label: String, valueText: String, fraction: Float, modifier: Modifier = Modifier, color: Color = PolyglotTheme.colors.primary) {
    val c = PolyglotTheme.colors
    Column(modifier.fillMaxWidth().semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color, trackColor = c.progressTrack, drawStopIndicator = {},
        )
    }
}

enum class BannerKind { INFO, SUCCESS, WARNING, ERROR }

@Composable
fun Banner(kind: BannerKind, text: String, modifier: Modifier = Modifier, title: String? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    val c = PolyglotTheme.colors
    val (bg, fg, icon) = when (kind) {
        BannerKind.INFO -> Triple(c.infoContainer, c.info, Icons.Filled.Info)
        BannerKind.SUCCESS -> Triple(c.successContainer, c.success, Icons.Filled.CheckCircle)
        BannerKind.WARNING -> Triple(c.warningContainer, c.warning, Icons.Filled.WarningAmber)
        BannerKind.ERROR -> Triple(c.errorContainer, c.error, Icons.Filled.ErrorOutline)
    }
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(PolyglotTheme.radii.md)).background(bg).padding(PolyglotTheme.spacing.md)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.Top,
    ) {
        // The icon shape differs per kind, so meaning is not conveyed by colour alone.
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp).padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (title != null) Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
            if (action != null && onAction != null) QuietButton(action, onAction, color = fg)
        }
    }
}

@Composable
fun LevelBadge(level: CefrLevel, estimateLabel: String?, modifier: Modifier = Modifier) {
    val c = PolyglotTheme.colors
    Row(
        modifier.clip(RoundedCornerShape(PolyglotTheme.radii.pill)).background(c.secondaryContainer).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(level.label, style = MaterialTheme.typography.labelLarge, color = c.onSecondaryContainer)
        if (estimateLabel != null) Text(" · $estimateLabel", style = MaterialTheme.typography.labelMedium, color = c.onSecondaryContainer)
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, container: Color = PolyglotTheme.colors.surfaceElevated, content: Color = PolyglotTheme.colors.textSecondary) {
    Text(
        text, style = MaterialTheme.typography.labelMedium, color = content, maxLines = 1,
        modifier = modifier.clip(RoundedCornerShape(PolyglotTheme.radii.pill)).background(container).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Large selectable option used in onboarding and settings. Exposes radio/checkbox semantics. */
@Composable
fun ChoiceCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    multiSelect: Boolean = false,
    /** Set when [title] is in the learned language, so it gets that language's font, locale and direction. */
    language: LanguageCode? = null,
) {
    val c = PolyglotTheme.colors
    val shape = RoundedCornerShape(PolyglotTheme.radii.lg)
    val borderColor by animateColorAsState(if (selected) c.primary else c.divider)
    val m = if (multiSelect) Modifier.toggleable(selected, role = Role.Checkbox) { onClick() } else Modifier.selectable(selected, role = Role.RadioButton, onClick = onClick)
    Row(
        modifier.fillMaxWidth().clip(shape).background(if (selected) c.primaryContainer.copy(alpha = if (c.isDark) 0.55f else 0.6f) else c.card)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape).then(m).heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { leading(); Spacer(Modifier.width(14.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val titleStyle = if (language == null) MaterialTheme.typography.titleSmall
                else styleFor(language, MaterialTheme.typography.titleSmall).copy(
                    localeList = localeFor(Languages.of(language).defaultVariant.localeTag), textDirection = textDirectionFor(language))
            // Full width so a right-to-left title (Urdu, Arabic) aligns to its own start in a left-to-right layout.
            Text(title, if (language != null) Modifier.fillMaxWidth().padding(end = 12.dp) else Modifier, style = titleStyle, color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        if (multiSelect) {
            androidx.compose.material3.Checkbox(checked = selected, onCheckedChange = null)
        } else RadioButton(selected = selected, onClick = null)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipGroup(options: List<T>, selected: Set<T>, label: (T) -> String, onToggle: (T) -> Unit, modifier: Modifier = Modifier) {
    val c = PolyglotTheme.colors
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o ->
            FilterChip(
                selected = o in selected, onClick = { onToggle(o) }, label = { Text(label(o)) },
                modifier = Modifier.heightIn(min = 40.dp),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.primaryContainer, selectedLabelColor = c.onPrimaryContainer),
            )
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = PolyglotTheme.colors
    Row(
        modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .heightIn(min = 56.dp).padding(horizontal = PolyglotTheme.spacing.gutter, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(16.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        if (trailing != null) { Spacer(Modifier.width(12.dp)); trailing() }
    }
}

@Composable
fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, subtitle: String? = null, enabled: Boolean = true) {
    val c = PolyglotTheme.colors
    Row(
        modifier.fillMaxWidth().toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .heightIn(min = 56.dp).padding(horizontal = PolyglotTheme.spacing.gutter, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) c.textPrimary else c.onDisabled)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
fun SettingsGroup(title: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = PolyglotTheme.colors
    Column(modifier.fillMaxWidth()) {
        if (title != null) Text(title, style = MaterialTheme.typography.labelLarge, color = c.primary,
            modifier = Modifier.padding(horizontal = PolyglotTheme.spacing.gutter, vertical = 8.dp).semantics { heading() })
        Surface(color = c.card, modifier = Modifier.fillMaxWidth()) { Column(content = content) }
    }
}

@Composable
fun Divider() = HorizontalDivider(color = PolyglotTheme.colors.divider)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    secret: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = modifier.fillMaxWidth(),
        supportingText = supporting?.let { { Text(it) } }, isError = isError, singleLine = singleLine, minLines = minLines,
        textStyle = textStyle, shape = RoundedCornerShape(PolyglotTheme.radii.md),
        visualTransformation = if (secret) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
    )
}

/** Centered state used for empty, error and permission states so they all look consistent. */
@Composable
fun StateMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryAction: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        illustration?.invoke()
        Text(title, style = MaterialTheme.typography.titleMedium, color = PolyglotTheme.colors.textPrimary, textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() })
        Text(body, style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary, textAlign = TextAlign.Center)
        if (action != null && onAction != null) PrimaryButton(action, onAction, Modifier.widthIn(min = 200.dp))
        if (secondaryAction != null && onSecondaryAction != null) QuietButton(secondaryAction, onSecondaryAction)
    }
}

/** Skeleton placeholder; static when reduced motion is on. */
@Composable
fun LoadingPlaceholder(label: String, modifier: Modifier = Modifier, rows: Int = 3) {
    val c = PolyglotTheme.colors
    val reduced = PolyglotTheme.motion.reduced
    val alpha = if (reduced) 0.6f else {
        val t = rememberInfiniteTransition()
        t.animateFloat(0.35f, 0.8f, infiniteRepeatable(tween(900), RepeatMode.Reverse)).value
    }
    Column(modifier.fillMaxWidth().semantics { contentDescription = label; liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(rows) { i ->
            Box(Modifier.fillMaxWidth(if (i % 2 == 0) 1f else 0.7f).height(if (i == 0) 120.dp else 56.dp)
                .clip(RoundedCornerShape(PolyglotTheme.radii.lg)).background(c.surfaceElevated.copy(alpha = alpha)))
        }
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, dismiss: String, onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = false) {
    val c = PolyglotTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { QuietButton(confirm, onConfirm, color = if (destructive) c.error else c.primary) },
        dismissButton = { QuietButton(dismiss, onDismiss) },
        containerColor = c.surface,
    )
}

@Composable
fun AudioButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, playing: Boolean = false) {
    val c = PolyglotTheme.colors
    FilledTonalIconButton(onClick = onClick, enabled = enabled, modifier = modifier.size(48.dp).semantics { if (playing) stateDescription = label }) {
        Icon(if (playing) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp, contentDescription = label, tint = if (enabled) c.primary else c.onDisabled)
    }
}

enum class MicVisualState { IDLE, LISTENING, PROCESSING, UNAVAILABLE }

/** Push-to-talk microphone. Listening is shown by colour, a pulsing ring, the icon and the state description. */
@Composable
fun MicButton(state: MicVisualState, label: String, stateLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier, level: Float = 0f) {
    val c = PolyglotTheme.colors
    val listening = state == MicVisualState.LISTENING
    val pulse = if (listening && !PolyglotTheme.motion.reduced) {
        rememberInfiniteTransition().animateFloat(1f, 1.18f, infiniteRepeatable(tween(700), RepeatMode.Reverse)).value
    } else 1f
    val bg = when (state) {
        MicVisualState.LISTENING -> c.error
        MicVisualState.PROCESSING -> c.secondary
        MicVisualState.UNAVAILABLE -> c.disabled
        MicVisualState.IDLE -> c.primary
    }
    Box(modifier.size(84.dp), contentAlignment = Alignment.Center) {
        if (listening) Box(Modifier.size(84.dp).scale(pulse + level.coerceIn(0f, 1f) * 0.1f).clip(CircleShape).background(c.error.copy(alpha = 0.18f)))
        Box(
            Modifier.size(68.dp).clip(CircleShape).background(bg)
                .clickable(enabled = state != MicVisualState.PROCESSING, role = Role.Button, onClickLabel = label, onClick = onClick)
                .semantics { contentDescription = label; stateDescription = stateLabel },
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                MicVisualState.PROCESSING -> CircularProgressIndicator(Modifier.size(28.dp), color = c.onSecondary, strokeWidth = 3.dp)
                MicVisualState.LISTENING -> Icon(Icons.Filled.Stop, null, tint = Color.White, modifier = Modifier.size(32.dp))
                MicVisualState.UNAVAILABLE -> Icon(Icons.Filled.MicOff, null, tint = c.onDisabled, modifier = Modifier.size(30.dp))
                MicVisualState.IDLE -> Icon(Icons.Filled.Mic, null, tint = c.onPrimary, modifier = Modifier.size(32.dp))
            }
        }
    }
}

/**
 * Text in the language being learned. Applies a locale hint (correct CJK glyphs, Arabic shaping)
 * and optionally shows pinyin/transliteration above it.
 */
@Composable
fun LearningText(
    text: String,
    language: LanguageCode,
    modifier: Modifier = Modifier,
    style: TextStyle = PolyglotTheme.learning.term,
    reading: String? = null,
    color: Color = PolyglotTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
) {
    val tag = Languages.of(language).defaultVariant.localeTag
    Column(modifier, horizontalAlignment = when (textAlign) { TextAlign.Center -> Alignment.CenterHorizontally; else -> Alignment.Start }) {
        if (reading != null) Text(reading, style = PolyglotTheme.learning.pinyin.copy(localeList = localeFor("en")), color = PolyglotTheme.colors.textSecondary, textAlign = textAlign)
        Text(text, style = styleFor(language, style).copy(localeList = localeFor(tag), textDirection = textDirectionFor(language)), color = color, textAlign = textAlign)
    }
}

@Composable
fun FeedbackPanel(correct: Boolean, title: String, detail: String?, modifier: Modifier = Modifier, extra: (@Composable ColumnScope.() -> Unit)? = null) {
    val c = PolyglotTheme.colors
    AnimatedVisibility(true, enter = fadeIn(tween(PolyglotTheme.motion.duration(200))), exit = fadeOut()) {
        Column(
            modifier.fillMaxWidth().clip(RoundedCornerShape(PolyglotTheme.radii.lg)).background(if (correct) c.successContainer else c.errorContainer)
                .padding(PolyglotTheme.spacing.lg).semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive },
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (correct) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline, null, tint = if (correct) c.success else c.error)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
            }
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
            extra?.invoke(this)
        }
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, background: Color, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(modifier.size(size).clip(RoundedCornerShape(PolyglotTheme.radii.md)).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
fun SpacerV(h: Dp) = Spacer(Modifier.height(h))

/** Minimum-size wrapper for small custom tap targets. */
fun Modifier.minTouchTarget(): Modifier = this.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
