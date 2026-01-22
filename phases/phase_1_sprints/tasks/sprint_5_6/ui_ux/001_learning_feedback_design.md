# Task: Design Learning Feedback UI

**Sprint:** 5-6 (Pattern Learning Engine)
**Owner:** UI/UX Engineer
**Estimate:** 15 hours (D-5.1 through D-5.5 combined)
**Priority:** P1
**Prerequisites:** Sprint 3-4 completed (list feature functional)

---

## Sprint Context

This sprint implements the "invisible intelligence" that makes Restock unique. While the pattern learning engine works silently in the background, subtle UI feedback helps users understand that the app is learning without being intrusive.

### Sprint Theme
> *"The intelligence that learns without asking."*

### Key Principles
1. **Subtle, not distracting:** Learning feedback should never interrupt the primary action
2. **Optional:** Can be disabled in settings
3. **Delightful:** Small details that create an emotional connection
4. **Progressive disclosure:** First-time explanation, then subtle ongoing feedback

---

## Task Description

Research, design, and prototype subtle feedback mechanisms that indicate the app is learning from user behavior.

## Research Phase

### D-5.1: Research Subtle Feedback Patterns (3h)

Research existing apps that communicate "learning" or "intelligence":

**Apps to Study:**
- Google Photos (organizing, face recognition)
- Spotify (Discover Weekly generation)
- Apple Photos (Memories)
- Smart home apps (routine learning)

**Key Questions:**
- How do they communicate learning without being annoying?
- What visual language indicates "smart" behavior?
- How much feedback is too much?

**Deliverables:**
- Research document with screenshots
- List of patterns that could work for Restock

### Feedback Concepts to Explore

1. **Sparkle Animation on Checkbox**
   - Tiny particles briefly appear when checking off
   - Subtle glow effect
   - Duration: 300ms

2. **"Brain" Icon Pulse**
   - Small brain/lightbulb icon in header
   - Pulses briefly after learning events
   - Very subtle, almost subliminal

3. **Progress Indicator**
   - "Learning your patterns..." text
   - Only during onboarding period
   - Disappears after suggestions start

4. **Check Animation Enhancement**
   - Slightly more elaborate checkmark animation
   - Conveys that the action was "recorded"

---

## Design Phase

### D-5.2: Design Learning Indicator Concepts (3h)

Create 3-4 concept options in Figma:

**Option A: Sparkle Effect**
```
┌─────────────────────────────────────┐
│ ☑ Milk                        ✨    │
│    (tiny sparkles fade out)         │
└─────────────────────────────────────┘
```

**Option B: Glow Checkbox**
```
┌─────────────────────────────────────┐
│ ☑ Milk                              │
│  ↑                                  │
│  Checkbox has brief golden glow     │
└─────────────────────────────────────┘
```

**Option C: Status Bar Indicator**
```
┌─────────────────────────────────────┐
│ 🧠 Learning your patterns...        │
│─────────────────────────────────────│
│ List items...                       │
└─────────────────────────────────────┘
```

**Option D: Subtle Badge**
```
┌─────────────────────────────────────┐
│ ☑ Milk                      [📚]   │
│      "Noted!" badge fades in/out    │
└─────────────────────────────────────┘
```

### D-5.3: Prototype Animation Options (4h)

Create Figma prototypes or Compose previews for each option.

**Animation Specs:**

| Option | Duration | Easing | Trigger |
|--------|----------|--------|---------|
| Sparkle | 300ms | EaseOut | On check |
| Glow | 200ms | EaseInOut | On check |
| Status | Persistent | - | First 3 trips |
| Badge | 400ms | EaseOut | On check |

### D-5.4: User Testing of Feedback Options (3h)

Quick guerrilla testing with 3-5 people:

**Test Protocol:**
1. Show each animation option
2. Ask: "Did you notice anything special when checking items?"
3. Ask: "Does this feel intrusive or pleasant?"
4. Ask: "Which option do you prefer?"

**Questions to Answer:**
- Do users notice the feedback?
- Is it distracting from the primary action?
- Does it feel "magical" or "gimmicky"?

### D-5.5: Select Final Feedback Approach (2h)

Based on research and testing, select the final approach.

**Selection Criteria:**
- Noticeable but not distracting
- Technically feasible
- Consistent with Restock design language
- Battery/performance friendly

---

## Recommended Implementation: Enhanced Check Animation

Based on typical outcomes, the recommended approach is a combination:

1. **Enhanced checkbox animation** - Slightly more elaborate check animation
2. **Subtle glow** - Brief accent color glow on successful check
3. **Learning status** - Text indicator during first 3 shopping trips

### LearningFeedbackIndicator.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LearningSparkle(
    trigger: Boolean,
    modifier: Modifier = Modifier,
) {
    var animationTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(trigger) {
        if (trigger) {
            animationTrigger++
        }
    }

    val sparkles = remember(animationTrigger) {
        if (animationTrigger > 0) generateSparkles() else emptyList()
    }

    val progress by animateFloatAsState(
        targetValue = if (animationTrigger > 0) 1f else 0f,
        animationSpec = tween(300, easing = EaseOut),
        label = "sparkleProgress"
    )

    Canvas(modifier = modifier.size(24.dp)) {
        sparkles.forEach { sparkle ->
            drawSparkle(sparkle, progress)
        }
    }
}

private data class Sparkle(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val color: Color,
)

private fun generateSparkles(): List<Sparkle> {
    return List(6) {
        Sparkle(
            angle = Random.nextFloat() * 360f,
            distance = Random.nextFloat() * 0.3f + 0.7f,
            size = Random.nextFloat() * 2f + 1f,
            color = RestockColors.Accent.copy(
                alpha = Random.nextFloat() * 0.5f + 0.5f
            ),
        )
    }
}

private fun DrawScope.drawSparkle(sparkle: Sparkle, progress: Float) {
    val maxRadius = size.minDimension / 2
    val radius = maxRadius * sparkle.distance * progress
    val alpha = 1f - progress

    val x = center.x + cos(Math.toRadians(sparkle.angle.toDouble())).toFloat() * radius
    val y = center.y + sin(Math.toRadians(sparkle.angle.toDouble())).toFloat() * radius

    drawCircle(
        color = sparkle.color.copy(alpha = alpha * sparkle.color.alpha),
        radius = sparkle.size * (1f - progress * 0.5f),
        center = Offset(x, y),
    )
}

@Composable
fun LearningStatusBanner(
    shoppingTripCount: Int,
    modifier: Modifier = Modifier,
) {
    if (shoppingTripCount >= 3) return // Don't show after enough trips

    val message = when (shoppingTripCount) {
        0 -> "Start adding items to begin learning your patterns"
        1 -> "Learning your patterns..."
        2 -> "Almost ready to suggest items!"
        else -> ""
    }

    if (message.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = RestockColors.Accent.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RestockSpacing.md,
                vertical = RestockSpacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "🧠",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.width(RestockSpacing.sm))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = RestockColors.Muted,
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun LearningStatusBannerPreview() {
    RestockPreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LearningStatusBanner(shoppingTripCount = 0)
            LearningStatusBanner(shoppingTripCount = 1)
            LearningStatusBanner(shoppingTripCount = 2)
        }
    }
}
```

---

## Acceptance Criteria

- [ ] Research document completed with findings
- [ ] 3-4 concept designs created in Figma
- [ ] Animation prototypes functional
- [ ] User testing completed with 3-5 people
- [ ] Final approach selected with rationale
- [ ] Learning status banner designed

## Figma Deliverables

1. Research findings document
2. Concept explorations (3-4 options)
3. Animation spec sheets
4. Final selected design
5. Component specifications

## Definition of Done

- [ ] Research phase completed
- [ ] Concepts explored and documented
- [ ] User testing validates approach
- [ ] Final design selected
- [ ] Ready for implementation

## Notes

- The feedback should feel like a "moment of delight", not a notification
- Less is more - subtle > obvious
- Consider haptic feedback in addition to visual
- Should work with reduced motion settings (provide static alternative)
- Battery impact should be negligible
