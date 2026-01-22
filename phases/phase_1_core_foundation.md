# Phase 1: Core Foundation
## Restock Android Application

**Duration:** Months 1-3 (12 weeks)
**Sprints:** 6 sprints (2-week cycles)
**Team:** Tech Lead/Senior Dev (1), UI/UX Engineer (1)
**Budget:** ~$305,000 (including contingency)

---

## Table of Contents

1. [Phase Overview](#1-phase-overview)
2. [Strategic Goals](#2-strategic-goals)
3. [Team Responsibilities](#3-team-responsibilities)
4. [Sprint Breakdown](#4-sprint-breakdown)
5. [Technical Decisions](#5-technical-decisions)
6. [Design Decisions](#6-design-decisions)
7. [Database Schema](#7-database-schema)
8. [Key Deliverables](#8-key-deliverables)
9. [Quality Gates](#9-quality-gates)
10. [Risk Management](#10-risk-management)
11. [Launch Checklist](#11-launch-checklist)

---

## 1. Phase Overview

Phase 1 establishes Restock as the simplest, most pleasant grocery list available while laying the invisible groundwork for intelligence. The goal is to launch an MVP that works perfectly as a basic list on day one, with pattern learning operating silently in the background.

### Core Philosophy
> *"A grocery list that adapts to you, not the other way around."*

The app must feel like magic, not machinery. Users should never configure rules or explain their habits—the system observes and learns silently.

### Success Definition
- New user adds first item within 15 seconds of opening app
- App works 100% offline with no degraded experience
- After 3 shopping trips, relevant suggestions begin appearing
- Suggestions achieve >50% acceptance rate in beta testing
- No P0 or P1 bugs remain open
- Google Play Store launch achieved

---

## 2. Strategic Goals

| Objective | Target | Measurement |
|-----------|--------|-------------|
| Time to first item added | < 15 seconds | Analytics event tracking |
| App cold start | < 2 seconds | Performance profiling |
| Offline functionality | 100% core features | Manual testing matrix |
| Crash-free users | > 99.5% | Firebase Crashlytics |
| ANR rate | < 0.2% | Play Console vitals |
| Beta suggestion acceptance | > 50% | In-app analytics |
| Play Store rating | 4.5+ stars | Store reviews |

---

## 3. Team Responsibilities

### Tech Lead / Senior Developer

**Primary Responsibilities:**
- Architecture design and implementation
- Core infrastructure setup (Gradle, Hilt, Room)
- Pattern learning engine development
- Code review and technical decisions
- Performance optimization
- CI/CD pipeline setup
- Security implementation

**Sprint Allocation:**
| Sprint | Focus Area |
|--------|------------|
| 1-2 | Project setup, architecture, database, DI |
| 3-4 | List feature backend, pattern tracking |
| 5-6 | Pattern learning engine, ML integration |
| 7-8 | Suggestions algorithm, data layer optimization |
| 9-10 | Performance tuning, accessibility fixes |
| 11-12 | Security audit prep, launch infrastructure |

### UI/UX Engineer

**Primary Responsibilities:**
- Design system implementation in Compose
- All screen designs and prototypes
- Component library development
- Animation implementation
- Accessibility compliance
- User testing coordination
- App Store assets preparation

**Sprint Allocation:**
| Sprint | Focus Area |
|--------|------------|
| 1-2 | Design system foundation, theme setup |
| 3-4 | List screen UI, item interactions |
| 5-6 | Check animations, swipe gestures |
| 7-8 | Suggestions UI, card components |
| 9-10 | Meal tags UI, polish and refinement |
| 11-12 | Store listing assets, final polish |

### Collaboration Points
- Daily standups (15 min)
- Sprint planning sessions
- Design-dev sync twice weekly
- Code reviews (Tech Lead reviews UI code, UI/UX reviews UX-critical logic)

---

## 4. Sprint Breakdown

### Sprint 1-2: Project Setup & Core Infrastructure (Weeks 1-4)

**Goal:** Establish solid technical foundation with all core dependencies configured.

#### Tech Lead Tasks
- [ ] Initialize multi-module Gradle project structure
- [ ] Configure version catalogs (`libs.versions.toml`)
- [ ] Set up Hilt dependency injection
- [ ] Implement Room database with core entities
- [ ] Create base ViewModel and UI state patterns
- [ ] Configure GitHub Actions CI pipeline
- [ ] Set up code quality tools (Detekt, Ktlint)
- [ ] Create repository interfaces in domain layer

#### UI/UX Engineer Tasks
- [ ] Design system specification finalization
- [ ] Implement `RestockTheme` with colors, typography
- [ ] Create spacing system (`RestockSpacing`)
- [ ] Build foundational components:
  - [ ] `RestockButton` (primary, secondary, text)
  - [ ] `RestockCard` (standard, elevated)
  - [ ] `RestockTextField` (with validation states)
  - [ ] `RestockCheckbox` (custom styled)
- [ ] Set up preview configurations
- [ ] Create component documentation

#### Definition of Done
- [ ] `./gradlew build` succeeds
- [ ] All modules compile without warnings
- [ ] CI pipeline runs on every PR
- [ ] Design tokens documented and implemented
- [ ] At least 3 base components with previews

---

### Sprint 3-4: The List Feature (Weeks 5-8)

**Goal:** Deliver fully functional grocery list with excellent UX.

#### Tech Lead Tasks
- [ ] Implement `ItemRepository` with Room
- [ ] Create `CurrentListRepository` for list management
- [ ] Build use cases:
  - [ ] `AddItemUseCase`
  - [ ] `CheckItemUseCase`
  - [ ] `DeleteItemUseCase`
  - [ ] `ReorderItemsUseCase`
  - [ ] `UpdateQuantityUseCase`
- [ ] Implement `ListViewModel` with UDF pattern
- [ ] Set up auto-complete data source (local history + common items)
- [ ] Implement position management for drag-drop

#### UI/UX Engineer Tasks
- [ ] Main list screen layout
- [ ] `GroceryItemCard` component with:
  - [ ] Checkbox with tap area
  - [ ] Item name display
  - [ ] Quantity badge (optional)
  - [ ] Swipe-to-delete gesture
- [ ] Add item input field with auto-complete dropdown
- [ ] "In Cart" section for checked items
- [ ] Drag-and-drop reordering with haptic feedback
- [ ] Long-press quantity adjustment sheet
- [ ] Undo snackbar for deletions
- [ ] Empty state design

#### Definition of Done
- [ ] Add items via text input works
- [ ] Check/uncheck with single tap
- [ ] Swipe delete with 5-second undo
- [ ] Drag-and-drop reordering persists
- [ ] Quantity adjustment on long-press
- [ ] All interactions feel smooth (60fps)

---

### Sprint 5-6: Pattern Learning Engine (Weeks 9-12)

**Goal:** Implement passive pattern learning that runs silently in background.

#### Tech Lead Tasks
- [ ] Design purchase event tracking system
- [ ] Implement `PurchaseRepository`
- [ ] Create `PatternCalculator` class:
  - [ ] Interval calculation algorithm
  - [ ] Confidence score computation (CV-based)
  - [ ] Minimum threshold enforcement (3 purchases)
- [ ] Build `PatternRepository` for storing computed patterns
- [ ] Set up WorkManager for background processing
- [ ] Implement TensorFlow Lite model integration for advanced patterns
- [ ] Create `RecordPurchaseUseCase` (triggered on item check-off)
- [ ] Create `CalculatePatternsUseCase`

#### UI/UX Engineer Tasks
- [ ] Check-off animation refinement
- [ ] Subtle "learning" indicator (non-intrusive)
- [ ] Purchase confirmation micro-interaction
- [ ] Celebration animation for list completion (optional)
- [ ] Loading states for pattern processing

#### Pattern Learning Algorithm
```kotlin
/**
 * Confidence Score = 1 / (1 + CV)
 * where CV = coefficient of variation = σ / μ
 *
 * Suggestion Trigger:
 *   IF confidence > 0.6
 *   AND days_since_last >= avg_interval - 1
 *   AND sample_count >= 3
 *   THEN suggest
 */
```

#### Definition of Done
- [ ] Every check-off records purchase event
- [ ] Patterns calculated for items with 3+ purchases
- [ ] Background processing doesn't block UI
- [ ] Pattern data persists correctly
- [ ] Unit tests cover edge cases (irregular shopping, long gaps)

---

### Sprint 7-8: Smart Suggestions (Weeks 13-16)

**Goal:** Surface intelligent suggestions based on learned patterns.

#### Tech Lead Tasks
- [ ] Create `SuggestionRepository`
- [ ] Implement suggestion prioritization algorithm:
  - [ ] Score by confidence × urgency
  - [ ] Cap at 5 suggestions maximum
  - [ ] Filter already-on-list items
- [ ] Build use cases:
  - [ ] `GetSuggestionsUseCase`
  - [ ] `AcceptSuggestionUseCase`
  - [ ] `DismissSuggestionUseCase`
- [ ] Implement confidence reduction on dismissal
- [ ] Persistent dismissal tracking (remove from suggestions)
- [ ] Create `SuggestionsViewModel`

#### UI/UX Engineer Tasks
- [ ] Suggestions section design (above main list)
- [ ] `SuggestionCard` component:
  - [ ] Item name prominent
  - [ ] Reason text ("Usually every 7 days")
  - [ ] One-tap add button
  - [ ] Dismiss X button
- [ ] Horizontal scroll for suggestion cards
- [ ] Add-to-list animation (card → list item)
- [ ] Dismiss animation (fade + slide)
- [ ] Empty suggestions state (subtle, non-intrusive)

#### Definition of Done
- [ ] Suggestions appear for high-confidence patterns
- [ ] Maximum 5 suggestions displayed
- [ ] One-tap adds item to list
- [ ] Dismiss reduces confidence
- [ ] Persistent dismissal removes item from suggestions
- [ ] Suggestion acceptance rate trackable

---

### Sprint 9-10: Meal Tags & Polish (Weeks 17-20)

**Goal:** Add meal categorization and polish entire experience.

#### Tech Lead Tasks
- [ ] Implement `MealTagRepository`
- [ ] Create meal tag data model and Room entities
- [ ] Build use cases:
  - [ ] `AddMealTagUseCase`
  - [ ] `RemoveMealTagUseCase`
  - [ ] `GetItemsByMealUseCase`
- [ ] Implement tag association learning
- [ ] Performance optimization pass:
  - [ ] Baseline Profiles generation
  - [ ] Memory leak detection
  - [ ] Database query optimization
- [ ] Accessibility audit implementation fixes

#### UI/UX Engineer Tasks
- [ ] Meal tag UI on item cards (colored chips)
- [ ] Tag assignment flow (tap to add tag)
- [ ] "Meals" tab with grouped view
- [ ] Meal tag colors implementation:
  - Breakfast: Amber (#F59E0B)
  - Lunch: Emerald (#10B981)
  - Dinner: Indigo (#6366F1)
  - Snack: Pink (#EC4899)
- [ ] Bottom navigation implementation (List, Meals, History)
- [ ] Accessibility improvements:
  - [ ] Content descriptions
  - [ ] Touch targets 48dp+
  - [ ] TalkBack navigation
- [ ] Final animation polish

#### Definition of Done
- [ ] Four meal categories working
- [ ] Items can have multiple tags
- [ ] Meals tab shows grouped view
- [ ] All touch targets 48dp+
- [ ] TalkBack compatible
- [ ] Performance budgets met

---

### Sprint 11-12: Launch Preparation (Weeks 21-24)

**Goal:** Prepare for Google Play Store launch with monitoring and quality assurance.

#### Tech Lead Tasks
- [ ] Firebase Crashlytics integration
- [ ] Privacy-respecting analytics setup
- [ ] ProGuard/R8 configuration
- [ ] Security hardening:
  - [ ] SQLCipher database encryption
  - [ ] No sensitive data in logs
  - [ ] Code obfuscation verification
- [ ] Release signing configuration
- [ ] Staged rollout setup (5% → 25% → 50% → 100%)
- [ ] On-call rotation documentation
- [ ] Rollback procedure documentation

#### UI/UX Engineer Tasks
- [ ] App icon finalization (all densities)
- [ ] Feature graphics for Play Store
- [ ] Screenshots for all supported sizes
- [ ] Play Store description and metadata
- [ ] Privacy policy page design
- [ ] Onboarding screen polish
- [ ] Beta testing coordination
- [ ] User feedback collection setup

#### Definition of Done
- [ ] Crash rate < 0.5% in beta
- [ ] ANR rate < 0.2%
- [ ] All store assets complete
- [ ] Privacy policy published
- [ ] Data safety questionnaire complete
- [ ] Content rating obtained
- [ ] Release notes prepared

---

## 5. Technical Decisions

### Architecture: MVVM + Clean Architecture

**Decision:** Use MVVM with Clean Architecture and Unidirectional Data Flow.

**Rationale:**
- Clear separation of concerns enables testability
- StateFlow for state, Channel for events provides predictable UI behavior
- Repository interfaces in domain layer allow easy testing with fakes
- Scales well for future feature additions

### Database: Room with SQLCipher

**Decision:** Use Room ORM with SQLCipher encryption.

**Rationale:**
- Room provides compile-time query validation
- Coroutines/Flow integration is seamless
- SQLCipher adds encryption at rest for privacy
- Migration support for future schema changes

### Dependency Injection: Hilt

**Decision:** Use Hilt for dependency injection.

**Rationale:**
- Official Android solution with compile-time safety
- Reduces boilerplate vs. manual DI
- ViewModel integration is straightforward
- Testing support with `@HiltAndroidTest`

### Pattern Learning: On-Device Statistical Model

**Decision:** Use statistical model (not neural network) for Phase 1 patterns.

**Rationale:**
- Simple coefficient of variation approach is sufficient
- No model training required
- Works entirely offline
- Low resource consumption
- Neural network can be added in Phase 2+ if needed

### UI Framework: Jetpack Compose

**Decision:** 100% Jetpack Compose, no XML layouts.

**Rationale:**
- Declarative UI is more maintainable
- Better performance with proper optimization
- Modern Android standard
- Easier to implement complex animations

---

## 6. Design Decisions

### Color System

| Color | Hex | Usage |
|-------|-----|-------|
| Primary | `#1F1F1F` | Text, icons, primary buttons |
| Accent | `#F59E0B` | Suggestions, smart badges |
| Success | `#10B981` | Checked items, confirmations |
| Background | `#FFFBF5` | Main background (warm off-white) |
| Card | `#FFFFFF` | Cards, inputs |
| Muted | `#8B8680` | Secondary text |

### Typography

| Element | Size | Weight |
|---------|------|--------|
| Display | 32sp | SemiBold |
| Title | 20sp | Medium |
| Body | 15sp | Regular |
| Caption | 12sp | Regular |
| Label | 11sp | Medium |

**Font:** DM Sans (primary), system fallback

### Component Specifications

- **List Item Card:** 14dp radius, white bg, shadow 0 1px 8px rgba(0,0,0,0.03)
- **Suggestion Card:** 16dp radius, more prominent shadow
- **Input Field:** 16dp radius, full-width
- **Touch Targets:** Minimum 48dp for accessibility
- **Spacing Base:** 4dp (xs:4, sm:8, md:16, lg:24, xl:32)

### Interaction Patterns

- **One-tap actions:** Check item, accept suggestion, dismiss suggestion
- **Swipe gestures:** Swipe-to-delete with undo
- **Long-press:** Quantity adjustment
- **Drag-and-drop:** Reorder items
- **No confirmation dialogs** for reversible actions

---

## 7. Database Schema

```sql
-- Core Tables
CREATE TABLE items (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    normalized_name TEXT NOT NULL UNIQUE,
    created_at INTEGER NOT NULL,
    is_active INTEGER DEFAULT 1
);

CREATE TABLE current_list (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES items(id),
    quantity INTEGER DEFAULT 1,
    unit TEXT,
    added_at INTEGER NOT NULL,
    checked_at INTEGER,
    position INTEGER NOT NULL,
    is_checked INTEGER DEFAULT 0
);

CREATE TABLE purchases (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES items(id),
    purchased_at INTEGER NOT NULL,
    quantity INTEGER DEFAULT 1,
    price_cents INTEGER,
    store_id TEXT
);

CREATE TABLE patterns (
    item_id TEXT PRIMARY KEY REFERENCES items(id),
    avg_interval_days REAL NOT NULL,
    confidence_score REAL NOT NULL,
    sample_count INTEGER NOT NULL,
    last_calculated INTEGER NOT NULL,
    last_suggested INTEGER
);

CREATE TABLE meal_tags (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES items(id),
    meal_type TEXT NOT NULL CHECK(meal_type IN ('breakfast','lunch','dinner','snack')),
    frequency_count INTEGER DEFAULT 1,
    UNIQUE(item_id, meal_type)
);

-- Performance Indices
CREATE INDEX idx_purchases_item_date ON purchases(item_id, purchased_at);
CREATE INDEX idx_patterns_confidence ON patterns(confidence_score DESC);
CREATE INDEX idx_current_list_position ON current_list(position);
```

---

## 8. Key Deliverables

### End of Phase 1

1. **Fully Functional Grocery List App**
   - Add, check, delete, reorder items
   - Works 100% offline
   - Smooth 60fps performance

2. **Passive Pattern Learning**
   - Records all purchase events
   - Calculates intervals and confidence
   - Runs in background without UI blocking

3. **Smart Suggestions**
   - Appears after 3+ shopping trips
   - Prioritized by confidence and urgency
   - One-tap accept, easy dismiss

4. **Meal Tagging**
   - Four categories (breakfast, lunch, dinner, snack)
   - Grouped view in Meals tab
   - Tag association learning

5. **Google Play Store Launch**
   - All store assets complete
   - Privacy policy and data safety
   - Staged rollout ready

---

## 9. Quality Gates

### PR Merge Requirements
- [ ] All unit tests pass
- [ ] Lint clean (no errors, warnings addressed)
- [ ] Code coverage maintained (80%+ unit)
- [ ] Code review approved

### Alpha Release (Week 8)
- [ ] Integration tests pass
- [ ] No P0/P1 bugs
- [ ] Core flows functional
- [ ] Internal testing complete

### Beta Release (Week 10)
- [ ] E2E tests pass
- [ ] Crash rate < 1%
- [ ] Performance benchmarks met
- [ ] External beta feedback addressed

### Production Launch (Week 12)
- [ ] Crash rate < 0.5%
- [ ] ANR rate < 0.2%
- [ ] No P0 bugs open
- [ ] Full team sign-off

---

## 10. Risk Management

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Pattern learning inaccuracy | Medium | High | Conservative thresholds (confidence > 0.6), easy dismissal, learn from rejections |
| Compose performance issues | Low | Medium | Profile aggressively, baseline profiles, lazy loading |
| Database migration failures | Low | High | Comprehensive migration tests, rollback capability |
| Scope creep | High | Medium | Strict sprint discipline, defer non-essential features |
| Key person dependency | Medium | High | Documentation, knowledge sharing, paired programming |

---

## 11. Launch Checklist

### Technical Readiness
- [ ] All P0 features complete and tested
- [ ] Crash rate < 0.5% in beta
- [ ] ANR rate < 0.2%
- [ ] Cold start < 2 seconds (p95)
- [ ] All critical flows tested on 10+ device configurations
- [ ] Accessibility: TalkBack compatible, touch targets 48dp+
- [ ] Offline mode fully functional
- [ ] Database encrypted with SQLCipher

### Store Readiness
- [ ] App listing complete
- [ ] Screenshots finalized (phone, tablet if applicable)
- [ ] Feature graphics created
- [ ] Privacy policy published
- [ ] Data safety questionnaire complete
- [ ] Content rating obtained
- [ ] Release notes prepared

### Operational Readiness
- [ ] Crashlytics monitoring configured
- [ ] Analytics events defined and implemented
- [ ] On-call rotation established
- [ ] Rollback procedure documented
- [ ] Customer support FAQ prepared
- [ ] Social media response plan ready

---

## Milestone Summary

| Milestone | Target | Success Criteria |
|-----------|--------|------------------|
| M1: Kickoff | Week 1 | Project structure complete, CI operational |
| M2: List Complete | Week 4 | All CRUD operations, offline-first working |
| M3: Patterns Operational | Week 6 | Suggestions after 3 purchases |
| M4: Alpha Release | Week 8 | Internal testing, core flows functional |
| M5: Beta Release | Week 10 | External beta, crash rate < 0.5% |
| M6: Production Launch | Week 12 | Google Play Store live |

---

*Phase 1 Document v1.0 | January 2026*
