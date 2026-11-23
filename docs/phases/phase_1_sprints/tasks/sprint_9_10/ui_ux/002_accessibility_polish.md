# Task: Accessibility Audit & Final Polish

**Sprint:** 9-10 (Meal Tags & Polish)
**Owner:** UI/UX Engineer
**Estimate:** 21 hours (D-11.1 through D-11.6, D-12.1 through D-12.6 combined)
**Priority:** P0
**Prerequisites:** 001_meal_tags_ui.md

---

## Sprint Context

Accessibility is required for Google Play feature eligibility and ensures the app is usable by everyone. This task also includes final visual polish.

### Sprint Theme
> *"Refine every detail until it delights."*

### Accessibility Targets

- TalkBack fully navigable
- Touch targets minimum 48dp
- Color contrast meets WCAG AA
- Dynamic text sizing supported

---

## Task Description

Complete accessibility audit, fix issues, and perform final visual polish pass.

## D-11.1: Accessibility Scanner Audit (3h)

### Setup and Run

1. Install Accessibility Scanner from Play Store
2. Enable Scanner in device settings
3. Run through all screens
4. Document all issues

### Common Issues to Check

- [ ] Touch targets < 48dp
- [ ] Missing content descriptions
- [ ] Color contrast issues
- [ ] Focus order problems
- [ ] Missing headings

## D-11.2: Fix Touch Target Issues (3h)

### Minimum Touch Target Implementation

```kotlin
// Ensure minimum touch targets
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp) // Minimum touch target
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Handled by semantics
            modifier = Modifier.size(24.dp), // Visual size can be smaller
        )
    }
}
```

### Fix Small Targets

```kotlin
// Before: Small dismiss button
IconButton(
    onClick = onDismiss,
    modifier = Modifier.size(24.dp), // Too small!
)

// After: Proper touch target
Box(
    modifier = Modifier
        .size(48.dp) // Touch target
        .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center,
) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Dismiss",
        modifier = Modifier.size(16.dp), // Visual size
    )
}
```

## D-11.3: Add Content Descriptions (2h)

### Icon Descriptions

```kotlin
// All icons need descriptions
Icon(
    imageVector = Icons.Default.Add,
    contentDescription = "Add item to list",
)

// Decorative icons can use null
Icon(
    imageVector = Icons.Default.ArrowRight,
    contentDescription = null, // Decorative only
)
```

### Complex Component Descriptions

```kotlin
@Composable
fun GroceryItemCard(
    item: CurrentListItem,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.semantics {
            contentDescription = buildString {
                append(item.name)
                if (item.quantity > 1) {
                    append(", quantity ${item.quantity}")
                }
                item.unit?.let { append(" $it") }
                if (item.isChecked) {
                    append(", checked")
                }
            }

            // Custom actions for accessibility
            customActions = listOf(
                CustomAccessibilityAction("Delete item") {
                    onDelete()
                    true
                },
                CustomAccessibilityAction("Change quantity") {
                    onQuantityClick()
                    true
                }
            )
        }
    ) {
        // Card content
    }
}
```

## D-11.4: TalkBack Testing and Fixes (4h)

### Testing Checklist

- [ ] Can navigate through entire list
- [ ] Each item announced correctly
- [ ] Suggestions section navigable
- [ ] Meals tab groups announced
- [ ] All buttons and actions accessible
- [ ] Form fields properly labeled
- [ ] State changes announced

### Focus Management

```kotlin
// Announce state changes
@Composable
fun GroceryItemCard(
    item: CurrentListItem,
) {
    val context = LocalContext.current

    // Announce when item is checked
    LaunchedEffect(item.isChecked) {
        if (item.isChecked) {
            val view = (context as Activity).window.decorView
            view.announceForAccessibility("${item.name} checked off")
        }
    }
}
```

### Heading Semantics

```kotlin
@Composable
fun SectionHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics {
            heading() // Mark as heading for navigation
        }
    )
}
```

## D-11.5: Color Contrast Verification (2h)

### Verify All Text Combinations

| Text | Background | Current Ratio | Required | Status |
|------|------------|---------------|----------|--------|
| Primary on Background | #1F1F1F on #FFFBF5 | TBD | 4.5:1 | ☐ |
| Muted on Background | #8B8680 on #FFFBF5 | TBD | 4.5:1 | ☐ |
| Accent on Card | #F59E0B on #FFFFFF | TBD | 4.5:1 | ☐ |
| Success on Card | #10B981 on #FFFFFF | TBD | 4.5:1 | ☐ |

### Fix Low Contrast

```kotlin
// If Muted text fails contrast, darken it
object RestockColors {
    val Muted = Color(0xFF6B6560) // Darker for better contrast
}
```

## D-11.6: Dynamic Text Size Testing (2h)

### Test at Different Scales

1. 85% (Small)
2. 100% (Default)
3. 115% (Large)
4. 130% (Largest)
5. 200% (Maximum)

### Handle Text Overflow

```kotlin
Text(
    text = item.name,
    style = MaterialTheme.typography.bodyMedium,
    maxLines = 2, // Prevent overflow
    overflow = TextOverflow.Ellipsis,
)
```

### Avoid Fixed Heights

```kotlin
// Bad: Fixed height clips large text
Box(modifier = Modifier.height(56.dp))

// Good: Minimum height with flexibility
Box(modifier = Modifier.heightIn(min = 56.dp))
```

## D-12.1: Animation Polish Pass (3h)

### Standardize Timings

```kotlin
object RestockAnimations {
    // Standard durations
    const val QUICK = 150
    const val STANDARD = 250
    const val ELABORATE = 400

    // Easing curves
    val emphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val standardEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
}
```

### Reduced Motion Support

```kotlin
@Composable
fun AnimatedComponent() {
    val reducedMotion = LocalReducedMotion.current

    val animationSpec = if (reducedMotion) {
        snap<Float>() // No animation
    } else {
        tween<Float>(durationMillis = 300)
    }
}

// Provide reduced motion setting
val LocalReducedMotion = compositionLocalOf { false }

@Composable
fun RestockTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val reducedMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }

    CompositionLocalProvider(
        LocalReducedMotion provides reducedMotion
    ) {
        content()
    }
}
```

## D-12.2: Visual Consistency Audit (3h)

### Spacing Consistency

- [ ] All padding uses RestockSpacing
- [ ] Card corners consistent (14dp or 16dp)
- [ ] Icon sizes consistent (24dp standard, 16dp small)
- [ ] Touch targets consistent (48dp minimum)

### Color Consistency

- [ ] Primary text uses RestockColors.Primary
- [ ] Secondary text uses RestockColors.Muted
- [ ] All accents use correct meal colors
- [ ] Error states use RestockColors.Error

## D-12.3: Empty States Refinement (2h)

### Empty List State

```kotlin
@Composable
fun EmptyListState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Illustration or icon
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = RestockColors.Muted.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(RestockSpacing.md))

        Text(
            text = "Your list is empty",
            style = MaterialTheme.typography.titleMedium,
            color = RestockColors.Primary,
        )

        Spacer(Modifier.height(RestockSpacing.xs))

        Text(
            text = "Add items using the field above",
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Muted,
        )
    }
}
```

## D-12.4: Error States Design (2h)

### Network Error State

```kotlin
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RestockSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = RestockColors.Error,
        )

        Spacer(Modifier.height(RestockSpacing.md))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Primary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(RestockSpacing.md))

        RestockButton(
            text = "Try Again",
            onClick = onRetry,
            variant = RestockButtonVariant.Secondary,
        )
    }
}
```

## D-12.6: User Testing Session (4h)

### Test Script

1. **First Impression (5 min)**
   - Show app, ask for initial thoughts
   - Note any confusion

2. **Core Tasks (15 min)**
   - Add 3 items to list
   - Check off 2 items
   - Delete 1 item
   - Note any friction

3. **Suggestions (if available) (5 min)**
   - Show suggestions
   - Accept one, dismiss one
   - Ask if helpful

4. **Meal Tags (5 min)**
   - Tag an item
   - View Meals tab
   - Ask if useful

5. **Wrap Up (5 min)**
   - Overall impression
   - What would they change?
   - Would they use this app?

## Acceptance Criteria

- [ ] Accessibility Scanner shows no errors
- [ ] TalkBack navigation complete
- [ ] Touch targets all ≥ 48dp
- [ ] Color contrast meets WCAG AA
- [ ] Dynamic text sizing works to 200%
- [ ] Animations respect reduced motion
- [ ] Visual consistency verified
- [ ] Empty and error states polished
- [ ] User testing completed

## Definition of Done

- [ ] Accessibility audit passed
- [ ] All issues fixed
- [ ] User testing feedback addressed
- [ ] Ready for beta release
