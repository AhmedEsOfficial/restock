# Task: Create Play Store Assets

**Sprint:** 11-12 (Launch Preparation)
**Owner:** UI/UX Engineer
**Estimate:** 22 hours (D-11.1 through D-11.5, D-12.1 through D-12.6 combined)
**Priority:** P0
**Prerequisites:** Sprint 9-10 completed

---

## Sprint Context

Play Store assets are critical for app discovery and conversion. High-quality screenshots, icons, and descriptions significantly impact download rates.

### Sprint Theme
> *"Ship with confidence."*

---

## Task Description

Create all required Play Store assets including app icon, feature graphic, screenshots, and store copy.

## D-11.1: App Icon Finalization (4h)

### Icon Requirements

| Size | Use | File |
|------|-----|------|
| 512 × 512 | Play Store | ic_launcher_512.png |
| 192 × 192 | xxxhdpi | ic_launcher_xxxhdpi.png |
| 144 × 144 | xxhdpi | ic_launcher_xxhdpi.png |
| 96 × 96 | xhdpi | ic_launcher_xhdpi.png |
| 72 × 72 | hdpi | ic_launcher_hdpi.png |
| 48 × 48 | mdpi | ic_launcher_mdpi.png |

### Icon Design Guidelines

- Simple, recognizable shape
- Works at small sizes
- Follows Android adaptive icon guidelines
- Foreground layer: Icon graphic
- Background layer: Brand color (#F59E0B or gradient)

### Adaptive Icon

```xml
<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
```

## D-11.2: Feature Graphic Design (4h)

### Requirements

- Size: 1024 × 500 pixels
- Format: PNG or JPEG
- No alpha/transparency

### Design Elements

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│    [App Icon]     RESTOCK                                   │
│                   The grocery list                          │
│                   that learns.                              │
│                                                             │
│                   [Phone mockup showing app]                │
│                                                             │
│    ✨ Smart suggestions  📝 Simple list  📴 Works offline  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Color Scheme

- Background: Gradient from #FFFBF5 to #FFF0E0
- Primary text: #1F1F1F
- Accent elements: #F59E0B

## D-11.3: Screenshot Planning (2h)

### Screenshot Requirements

| Device | Resolution | Count |
|--------|------------|-------|
| Phone | 1080 × 1920 (or 9:16) | 6-8 |
| 7" Tablet | 1200 × 1920 | Optional |
| 10" Tablet | 1600 × 2560 | Optional |

### Screenshot Content Plan

| # | Screen | Caption | Key Message |
|---|--------|---------|-------------|
| 1 | Empty list with input | "Get started in seconds" | Speed |
| 2 | List with items | "Your grocery list, simplified" | Simplicity |
| 3 | Suggestions section | "Smart suggestions learn your habits" | Intelligence |
| 4 | Checking items | "Check off as you shop" | Core action |
| 5 | Meal tags view | "Organize by meal" | Organization |
| 6 | Meals tab | "Plan your shopping by meal" | Feature |

## D-11.4: Screenshot Creation (6h)

### Screenshot Frame Template

```
┌─────────────────────────────────────────┐
│                                         │
│     [Caption text in brand font]        │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │                                 │   │
│  │                                 │   │
│  │        [Device frame with      │   │
│  │         app screenshot]        │   │
│  │                                 │   │
│  │                                 │   │
│  └─────────────────────────────────┘   │
│                                         │
│     [Supporting text or icon]           │
│                                         │
└─────────────────────────────────────────┘
```

### Screenshot Capture Process

1. Set up test device with sample data
2. Use Android Studio Layout Inspector for perfect captures
3. Export at 2x resolution
4. Add to frame template in Figma
5. Export at required resolution

### Sample Data for Screenshots

```kotlin
// Create realistic test data
val sampleItems = listOf(
    "Milk", "Eggs", "Bread", "Butter", "Cheese",
    "Chicken breast", "Broccoli", "Rice", "Pasta",
    "Tomatoes", "Onions", "Garlic"
)

val sampleSuggestions = listOf(
    Suggestion("Milk", "Usually every 7 days"),
    Suggestion("Eggs", "Last bought 14 days ago"),
)
```

## D-11.5: Screenshot Frame Overlays (2h)

### Caption Styles

```
Font: Inter or SF Pro Display
Weight: SemiBold
Size: 48px (scaled to screenshot)
Color: #1F1F1F
```

### Background Colors

- Screenshot 1-2: #FFFBF5 (warm)
- Screenshot 3-4: #F0F9FF (cool)
- Screenshot 5-6: #F0FDF4 (fresh)

## D-12.1: Short Description (2h)

**80 characters max:**

```
The grocery list that learns. Smart suggestions, simple interface, works offline.
```

Alternative options:
```
Smart grocery list that learns your shopping habits. Simple, fast, works offline.
```
```
Never forget the milk again. A grocery list that learns what you need.
```

## D-12.2: Full Description (4h)

```
RESTOCK - The Grocery List That Learns

Tired of forgetting items at the store? Restock is the intelligently simple grocery list that learns your shopping patterns and reminds you what you need.

✨ SMART SUGGESTIONS
Restock learns your buying habits automatically. After a few shopping trips, you'll see suggestions for items you might need—right when you need them. No setup required.

📝 SIMPLE & FAST
Add items in seconds with smart auto-complete. Check off items with a single tap. Swipe to delete. Drag to reorder. Everything just works.

🍳 MEAL TAGS
Optionally organize your list by meal. Tag items as Breakfast, Lunch, Dinner, or Snack and see your list grouped by meal type.

📴 WORKS OFFLINE
Your list is always available, even without internet. Perfect for stores with spotty reception.

🔒 PRIVACY FIRST
All your data stays on your device. We don't collect, sell, or share your grocery information. Your shopping habits are your business.

---

WHY RESTOCK?

Other grocery apps are either too simple (just a checklist that forgets everything) or too complicated (meal planning that requires hours of setup).

Restock is different. It works like a simple list on day one, but gets smarter over time. The more you use it, the better it gets—without you lifting a finger.

---

FEATURES:
• Add items with smart auto-complete
• Check off items with one tap
• Swipe to delete with undo
• Drag and drop to reorder
• Smart suggestions based on your patterns
• Meal tags for organization
• Works 100% offline
• Clean, modern design
• Fast and lightweight

---

COMING SOON:
• Cloud sync across devices
• Household sharing
• Voice input
• Recipe import
• And more!

---

Questions or feedback? Email us at support@restock.app

Download Restock today and never forget the milk again.
```

## D-12.3: Release Notes for v1.0 (2h)

```
🎉 Welcome to Restock!

This is our first release, and we're excited to share it with you.

What's included:
• Simple, fast grocery list
• Smart suggestions that learn your shopping patterns
• Meal tags to organize by breakfast, lunch, dinner, or snack
• Works completely offline
• Dark mode support

We built Restock because we believe grocery lists should be simple but smart. After a few shopping trips, you'll start seeing suggestions for items you might need.

Thank you for trying Restock! We'd love to hear your feedback at support@restock.app

Happy shopping! 🛒
```

## D-12.4: Privacy Policy Page Design (3h)

### Privacy Policy URL

Host at: `https://restock.app/privacy`

### Key Sections

1. **What data we collect**
   - Analytics events (non-identifiable)
   - Crash reports (technical info only)

2. **What we don't collect**
   - Your grocery items stay on your device
   - We don't access your shopping list
   - No personal information collected

3. **How we protect your data**
   - Encrypted local storage
   - No cloud transmission of list data

4. **Your rights**
   - Delete all data by uninstalling
   - Contact us for questions

## D-12.5: Final UI Polish Pass (4h)

### Polish Checklist

- [ ] All colors consistent
- [ ] All spacing uses design tokens
- [ ] All animations smooth
- [ ] All text fits at max size
- [ ] All touch targets 48dp+
- [ ] All empty states designed
- [ ] All error states designed
- [ ] Dark mode works (if applicable)

## D-12.6: Fix Beta Feedback UI Issues (4h)

Document and fix any UI issues reported during beta testing.

## Acceptance Criteria

- [ ] App icon in all required sizes
- [ ] Feature graphic complete
- [ ] 6-8 screenshots with frames
- [ ] Short description written
- [ ] Full description written
- [ ] Release notes written
- [ ] Privacy policy designed
- [ ] Final UI polish complete

## Figma Deliverables

1. App icon (all sizes)
2. Feature graphic (1024×500)
3. Screenshot frames (6-8)
4. Privacy policy page design
5. Social media assets (stretch)

## Definition of Done

- [ ] All assets exported at correct sizes
- [ ] Assets uploaded to Play Console
- [ ] Store listing previewed and approved
- [ ] Privacy policy published

## Notes

- Screenshots should show realistic data
- Captions should highlight benefits, not features
- Feature graphic is the first thing users see
- Good screenshots increase conversion significantly
