# Task: Prepare Suggestions Section Design

**Sprint:** 5-6 (Pattern Learning Engine)
**Owner:** UI/UX Engineer
**Estimate:** 12 hours (D-7.1 through D-7.4 combined)
**Priority:** P0
**Prerequisites:** Sprint 3-4 completed

---

## Sprint Context

While the Tech Lead implements the pattern learning backend, the UI/UX Engineer should prepare the designs for the suggestions feature that will be implemented in Sprint 7-8.

### Sprint Theme
> *"The intelligence that learns without asking."*

### Why Prepare Now
- Suggestions are the core value proposition
- Design needs to be ready when backend is complete
- User testing can validate approach before implementation
- Reduces Sprint 7-8 pressure

---

## Task Description

Design the suggestions section UI, suggestion cards, and animations in preparation for Sprint 7-8 implementation.

## D-7.1: Prepare Suggestions Section Design (4h)

### Layout Specification

```
┌─────────────────────────────────────────────────────────────┐
│  Suggestions                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  →       │
│  │             │  │             │  │             │          │
│  │    Milk     │  │    Eggs     │  │   Bread     │          │
│  │  [× dismiss]│  │  [× dismiss]│  │  [× dismiss]│          │
│  │             │  │             │  │             │          │
│  │ Usually     │  │ Last bought │  │ Usually     │          │
│  │ every 7 days│  │ 20 days ago │  │ every 5 days│          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│                                                             │
│  ─────────────────────────────────────────────────────────  │
└─────────────────────────────────────────────────────────────┘
```

### Design Requirements

- **Position:** Above the main list, below the input field
- **Scroll:** Horizontal scrolling for 3+ suggestions
- **Maximum:** 5 suggestions visible at once
- **Visibility:** Section hidden when no suggestions

### Figma Deliverables

1. Suggestions section layout (desktop frame)
2. Mobile responsive versions
3. States: empty, 1-2 suggestions, 3+ suggestions (scrollable)
4. Section header design

---

## D-7.2: Design SuggestionCard Component (4h)

### Card Anatomy

```
┌───────────────────────────┐
│                      [×]  │  ← Dismiss button (14dp icon)
│                           │
│       Item Name           │  ← Primary text (bodyLarge, medium)
│                           │
│   Usually every 7 days    │  ← Reason text (labelMedium, muted)
│                           │
│   ═══════════════════     │  ← Accent indicator bar
└───────────────────────────┘
```

### Specifications

| Element | Specification |
|---------|--------------|
| Card width | 140dp |
| Card height | Auto (content-based) |
| Corner radius | 16dp |
| Padding | 12dp |
| Shadow | 2dp elevation |
| Background | Card color (white) |
| Dismiss icon | 16dp, muted color |
| Item name | bodyLarge, medium weight, centered |
| Reason text | labelMedium, muted, centered |
| Accent bar | 3dp height, Accent color @ 50% |

### States

1. **Default:** Normal appearance
2. **Pressed:** Slight scale (0.98) + ripple
3. **Dismissing:** Slide right + fade out
4. **Accepting:** Slide up + fade out

### Figma Deliverables

1. Card component with all states
2. Component variants (default, pressed)
3. Animation storyboard

---

## D-7.3: Plan Suggestions Animation (2h)

### Animation Specifications

| Animation | Duration | Easing | Description |
|-----------|----------|--------|-------------|
| Card accept | 300ms | EaseOut | Slide up 100dp + fade to 0 |
| Card dismiss | 200ms | EaseIn | Slide right 100dp + fade to 0 |
| Section appear | 300ms | EaseOut | Fade in + expand from 0 height |
| Section hide | 200ms | EaseIn | Fade out + collapse to 0 height |
| Card entry | 200ms | EaseOut | Fade in + scale from 0.95 |
| Card reorder | 150ms | EaseInOut | Slide to new position |

### Accept Animation Sequence

```
1. User taps card
2. Card scales to 1.05 (50ms)
3. Card moves up (-100dp) while fading (250ms)
4. Card removed from DOM
5. Remaining cards slide left to fill gap (150ms)
6. Item appears in list (separate animation)
```

### Dismiss Animation Sequence

```
1. User taps X button
2. Card moves right (+100dp) while fading (200ms)
3. Card removed from DOM
4. Remaining cards slide left to fill gap (150ms)
```

### Figma Deliverables

1. Animation storyboard frames
2. Timing diagram
3. After Effects/Lottie prototype (stretch)

---

## D-7.4: Document Design Decisions (2h)

Create a design decision document covering:

### Decision 1: Horizontal vs. Vertical Suggestions

**Decision:** Horizontal scrolling row

**Rationale:**
- Keeps main list visible
- Feels more like "suggestions" than "additional list"
- Familiar pattern (Netflix, Spotify)
- Easy to dismiss

**Alternatives Considered:**
- Vertical cards above list (takes too much space)
- Floating overlay (too intrusive)
- Tab-based (hidden)

### Decision 2: Card Size

**Decision:** 140dp wide, content-based height

**Rationale:**
- Fits 2-3 cards on screen
- Large enough for readable text
- Small enough to feel lightweight

### Decision 3: Reason Text Format

**Decision:** "Usually every X days" or "Last bought X days ago"

**Rationale:**
- Explains why item is suggested
- Builds trust in algorithm
- Simple, non-technical language

### Decision 4: Maximum Suggestions

**Decision:** 5 maximum

**Rationale:**
- More feels overwhelming
- Forces algorithm to prioritize
- Maintains "suggestion" feeling vs "another list"

### Decision 5: Dismiss Behavior

**Decision:** Swipe right or tap X to dismiss

**Rationale:**
- X button provides clear affordance
- Swipe matches list item delete pattern
- Multiple options for different users

---

## Implementation Preview

```kotlin
// Preview of SuggestionCard for Sprint 7-8
@Composable
fun SuggestionCard(
    suggestion: Suggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(140.dp)
            .clickable { onAccept() },
        shape = RoundedCornerShape(16.dp),
        color = RestockColors.Card,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Header with dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = RestockColors.Muted,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Item name
            Text(
                text = suggestion.itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Reason
            Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.labelMedium,
                color = RestockColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        RestockColors.Accent.copy(alpha = 0.5f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
```

---

## Acceptance Criteria

- [ ] Suggestions section layout designed
- [ ] SuggestionCard component designed with all states
- [ ] Animation sequences documented
- [ ] Design decisions documented
- [ ] Figma file organized for Sprint 7-8 handoff

## Figma Deliverables Summary

1. Suggestions section layout
2. SuggestionCard component (all states)
3. Animation storyboards
4. Learning state designs (empty, learning, ready)
5. Design decision documentation

## Definition of Done

- [ ] All designs complete in Figma
- [ ] Components match Restock design system
- [ ] Animations specified with timing
- [ ] Ready for Sprint 7-8 implementation
- [ ] Design review completed

## Notes

- These designs will be implemented in Sprint 7-8
- Keep designs simple - complexity can be added later
- Consider accessibility in all designs
- Test designs with real suggestion data when available
