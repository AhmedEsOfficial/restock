# Task: Design System Specification

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** UI/UX Engineer
**Estimate:** 8 hours (D-1.1 through D-1.8 combined)
**Priority:** P0
**Prerequisites:** None (can start immediately)

---

## Sprint Context

Sprint 1-2 establishes the design system foundation that ensures visual consistency throughout the Restock app. This task creates the design specifications in Figma that will guide all UI implementation.

### Sprint Theme
> *"Build the foundation right so everything else is fast."*

### Design Philosophy
> *"A grocery list that adapts to you, not the other way around."*

The app must feel like magic, not machinery. Simple, clean, and delightful.

---

## Task Description

Create comprehensive design specifications in Figma including color palette, typography, spacing system, shadows, and component designs.

## Color Palette

### Primary Colors

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| Primary | `#1F1F1F` | 31, 31, 31 | Text, icons, primary buttons |
| Accent | `#F59E0B` | 245, 158, 11 | Suggestions, smart badges, highlights |
| Success | `#10B981` | 16, 185, 129 | Checked items, confirmations |
| Background | `#FFFBF5` | 255, 251, 245 | Main background (warm off-white) |
| Card | `#FFFFFF` | 255, 255, 255 | Cards, inputs, elevated surfaces |
| Muted | `#8B8680` | 139, 134, 128 | Secondary text, placeholders |

### Meal Tag Colors

| Meal | Color | Hex | Emoji |
|------|-------|-----|-------|
| Breakfast | Amber | `#F59E0B` | 🌅 |
| Lunch | Emerald | `#10B981` | ☀️ |
| Dinner | Indigo | `#6366F1` | 🌙 |
| Snack | Pink | `#EC4899` | ✨ |

### State Colors

| State | Color | Hex |
|-------|-------|-----|
| Error | Red | `#EF4444` |
| Warning | Amber | `#F59E0B` |
| Info | Blue | `#3B82F6` |
| Disabled | Gray | `#D1D5DB` |

## Typography Scale

**Font Family:** DM Sans (primary), SF Pro / Roboto (system fallback)

| Element | Size | Weight | Line Height | Usage |
|---------|------|--------|-------------|-------|
| Display | 32sp | SemiBold (600) | 40sp | App title, welcome |
| Title Large | 24sp | SemiBold (600) | 32sp | Section headers |
| Title Medium | 20sp | Medium (500) | 28sp | Card titles |
| Body Large | 16sp | Regular (400) | 24sp | Primary content |
| Body Medium | 15sp | Regular (400) | 22sp | Item names |
| Label Large | 14sp | Medium (500) | 20sp | Button text |
| Label Medium | 12sp | Regular (400) | 16sp | Captions, metadata |
| Label Small | 11sp | Medium (500) | 14sp | Badges, tags |

## Spacing System

Base unit: 4dp. All spacing uses multiples of 4.

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Tight spacing, icon gaps |
| `sm` | 8dp | Between related elements |
| `md` | 16dp | Card padding, item gaps |
| `lg` | 24dp | Section spacing |
| `xl` | 32dp | Major section breaks |
| `xxl` | 48dp | Screen margins (top) |

## Corner Radius

| Token | Value | Usage |
|-------|-------|-------|
| `sm` | 8dp | Chips, small buttons |
| `md` | 12dp | Input fields |
| `lg` | 14dp | List item cards |
| `xl` | 16dp | Suggestion cards, modals |
| `full` | 9999dp | Pills, circular buttons |

## Shadows / Elevation

| Level | Shadow | Usage |
|-------|--------|-------|
| Level 0 | None | Flat elements |
| Level 1 | 0 1px 3px rgba(0,0,0,0.05) | List item cards |
| Level 2 | 0 2px 8px rgba(0,0,0,0.08) | Suggestion cards, inputs |
| Level 3 | 0 4px 16px rgba(0,0,0,0.12) | Modals, bottom sheets |

## Interaction Patterns

### One-Tap Actions
- Check item: Single tap on checkbox
- Accept suggestion: Single tap on card
- Dismiss suggestion: Tap X button

### Swipe Gestures
- Swipe left: Delete with undo
- Minimum swipe threshold: 72dp

### Long-Press Actions
- Long-press item: Quantity adjustment
- Long-press duration: 300ms

### Drag Actions
- Long-press to initiate drag
- Visual elevation on drag
- Drop to reorder

## Component Specifications

### List Item Card
- Corner radius: 14dp
- Background: White (#FFFFFF)
- Shadow: Level 1
- Padding: 12dp horizontal, 14dp vertical
- Min height: 56dp
- Checkbox size: 24dp
- Touch target: 48dp minimum

### Suggestion Card
- Corner radius: 16dp
- Background: White (#FFFFFF)
- Shadow: Level 2
- Width: 140dp
- Padding: 12dp
- Dismiss button: 24dp touch target

### Input Field
- Corner radius: 16dp
- Background: White (#FFFFFF)
- Shadow: Level 2
- Height: 52dp
- Padding: 16dp horizontal
- Placeholder color: Muted (#8B8680)

### Bottom Navigation
- Height: 64dp + safe area
- Background: White with blur (optional)
- Icon size: 24dp
- Label size: 11sp
- Active color: Primary (#1F1F1F)
- Inactive color: Muted (#8B8680)

### Snackbar
- Corner radius: 8dp
- Background: Primary (#1F1F1F)
- Text color: White
- Action color: Accent (#F59E0B)
- Max width: Screen - 32dp

### Meal Tag Chip
- Corner radius: 8dp
- Background: Meal color @ 15% opacity
- Text color: Meal color
- Padding: 8dp horizontal, 4dp vertical
- Font: Label Small

## Figma Library Structure

```
Restock Design System/
├── Colors/
│   ├── Primary
│   ├── Meal Tags
│   └── States
├── Typography/
│   ├── Display
│   ├── Titles
│   ├── Body
│   └── Labels
├── Spacing/
│   └── Spacing tokens
├── Icons/
│   ├── Navigation
│   ├── Actions
│   └── Status
├── Components/
│   ├── Buttons
│   ├── Cards
│   ├── Inputs
│   ├── Chips
│   ├── Checkboxes
│   └── Bottom Navigation
└── Patterns/
    ├── List Item
    ├── Suggestion Card
    └── Empty States
```

## Acceptance Criteria

- [ ] Color palette defined with all values documented
- [ ] Typography scale finalized
- [ ] Spacing system documented
- [ ] Shadow/elevation system defined
- [ ] Interaction patterns documented
- [ ] Component specifications created
- [ ] Figma library published
- [ ] All values ready for code implementation

## Definition of Done

- [ ] Figma library contains all design tokens
- [ ] Components created with variants
- [ ] Auto-layout used consistently
- [ ] Component documentation added
- [ ] Exported specs shared with Tech Lead
- [ ] Style guide page created

## Handoff to Code

Deliver to Tech Lead:
1. Color hex values (provided above)
2. Typography specs (size, weight, line height)
3. Spacing values in dp
4. Corner radius values
5. Shadow CSS/Compose values
6. Component dimension specifications

## Notes

- Prefer warm, approachable colors
- Ensure sufficient color contrast (WCAG AA)
- Design for accessibility from the start
- Keep it simple - complexity is the enemy
- Test designs on actual devices
