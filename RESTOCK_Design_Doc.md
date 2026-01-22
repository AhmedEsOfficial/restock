# 🛒 RESTOCK
## The Grocery List That Learns

---

**Product Design Document**

Version 1.0 | January 2026

*CONFIDENTIAL*

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Problem Analysis](#2-problem-analysis)
3. [User Personas](#3-user-personas)
4. [Core Design Principles](#4-core-design-principles)
5. [Feature Specifications](#5-feature-specifications)
6. [Core User Flows](#6-core-user-flows)
7. [Design System](#7-design-system)
8. [Technical Architecture](#8-technical-architecture)
9. [Monetization Strategy](#9-monetization-strategy)
10. [Success Metrics & KPIs](#10-success-metrics--kpis)
11. [Competitive Analysis](#11-competitive-analysis)
12. [Product Roadmap](#12-product-roadmap)
13. [Risks & Mitigation](#13-risks--mitigation)
14. [Appendix](#14-appendix)

---

# 1. Executive Summary

## 1.1 The Opportunity

Grocery shopping is a universal activity performed by billions of people weekly, yet the tools available to manage it have remained fundamentally unchanged for decades. Existing solutions fall into two categories: overly complex meal-planning applications that demand significant time investment, or basic checklist apps that forget everything the moment items are checked off.

Restock represents a third way: an intelligently simple grocery list that learns user patterns passively, requiring zero configuration while delivering increasingly personalized suggestions over time.

## 1.2 Product Vision

> *"A grocery list that adapts to you, not the other way around."*

Restock is designed to feel like a simple list on day one, but becomes an intelligent shopping assistant over months of use. The magic happens invisibly: every checked item teaches the system about the user's purchasing rhythms, dietary patterns, and household needs.

## 1.3 Key Differentiators

- **Passive Intelligence:** Learns purchase cycles without any user configuration
- **Zero Friction:** Works as a basic list from the first use; intelligence emerges over time
- **Soft Meal Linking:** Optional meal tags without rigid meal planning requirements
- **AI-Ready Architecture:** Built from the ground up for future natural language and visual AI integration
- **Privacy-First:** All learning happens on-device; data never leaves the user's control

## 1.4 Target Market

**Primary:** Adults aged 25-55 who shop for themselves or their household at least once weekly. This includes busy professionals, parents, health-conscious individuals, and anyone who has tried and abandoned more complex meal-planning solutions.

**Secondary:** Shared households, couples, and families who need collaborative list management.

## 1.5 Business Model Summary

Freemium model with a generous free tier that covers most individual users' needs. Premium subscription ($4.99/month or $39.99/year) unlocks household sharing, advanced analytics, AI-powered features, and integrations.

---

# 2. Problem Analysis

## 2.1 The Current Landscape

Grocery list management tools have failed to evolve meaningfully despite advances in mobile technology and AI. A comprehensive analysis of the market reveals three dominant paradigms, each with critical flaws:

### 2.1.1 Basic Checklist Apps

**Examples:** Apple Reminders, Google Keep, Any.do, simple note apps

- **Strengths:** Simple, familiar, no learning curve
- **Fatal Flaw:** Zero memory between sessions; users must rebuild lists from scratch
- **User Pain:** "I keep forgetting to add things I always buy"

### 2.1.2 Meal Planning Apps

**Examples:** Mealime, Plan to Eat, Paprika, Yummly

- **Strengths:** Comprehensive, recipe integration, nutritional tracking
- **Fatal Flaw:** Requires users to plan entire weeks upfront; high abandonment rate
- **User Pain:** "I don't have time to plan every meal; life doesn't work that way"

### 2.1.3 Smart Grocery Apps

**Examples:** AnyList, OurGroceries, Bring!

- **Strengths:** History features, sharing capabilities, store organization
- **Fatal Flaw:** Features are opt-in and require manual configuration; intelligence is buried
- **User Pain:** "Too many features I don't use; feels cluttered"

## 2.2 The Gap

No existing solution combines simplicity with intelligence. Users are forced to choose between:

1. A simple tool that doesn't help them
2. A complex tool that demands too much from them

Restock fills this gap by delivering intelligence without demanding configuration.

## 2.3 User Research Insights

Qualitative research with 50+ potential users revealed consistent themes:

| Insight | Implication |
|---------|-------------|
| 87% have abandoned a grocery app within 2 weeks | Onboarding must be instant; value must be immediate |
| 72% buy the same 30-40 items repeatedly | Pattern recognition has enormous value; users have predictable cycles |
| 68% shop without a complete list | Proactive suggestions prevent forgotten items; don't require full planning |
| 91% want app simplicity over features | Features must be invisible until needed; complexity is a deterrent |

---

# 3. User Personas

## 3.1 Primary Persona: Sarah, The Busy Professional

| Attribute | Details |
|-----------|---------|
| Age | 34 |
| Occupation | Marketing Manager |
| Household | Lives with partner |
| Shopping Frequency | 1-2 times per week |

**Goals:** Spend minimal time on grocery management; never run out of essentials; eat healthier without planning every meal

**Frustrations:** Forgetting items at the store; wasting food that goes bad; feeling guilty about not meal planning

**Quote:** *"I just want to know what I need without having to think about it."*

**Restock Value:** Suggestions appear automatically based on her patterns. She adds items in 2 seconds. Never forgets milk again.

## 3.2 Secondary Persona: Marcus, The Family Coordinator

| Attribute | Details |
|-----------|---------|
| Age | 42 |
| Occupation | Software Engineer |
| Household | Spouse + 2 kids (ages 8 and 12) |
| Shopping Frequency | Weekly large trip + 1-2 quick runs |

**Goals:** Keep household stocked; coordinate with spouse; stay within budget; accommodate kids' preferences

**Frustrations:** Duplicate purchases when spouse doesn't check list; kids requesting random items; budget creep

**Quote:** *"We need one list the whole family can see and add to."*

**Restock Value:** Shared household list syncs in real-time. Budget insights show spending trends. Kids can add requests that need approval.

## 3.3 Tertiary Persona: Elena, The Health-Conscious Optimizer

| Attribute | Details |
|-----------|---------|
| Age | 28 |
| Occupation | Physical Therapist |
| Household | Lives alone |
| Shopping Frequency | 2-3 times per week (fresh items) |

**Goals:** Maintain specific dietary patterns; reduce food waste; try new healthy recipes; track what she eats

**Frustrations:** Produce going bad before she can use it; not knowing what to make with what she has

**Quote:** *"I want to eat well without it becoming a full-time job."*

**Restock Value:** Meal tags help her see dietary balance. Fresh item cycles are shorter. AI can suggest recipes based on what she has.

---

# 4. Core Design Principles

These principles guide every design and engineering decision. When in doubt, return to these foundations.

## 4.1 Invisible Intelligence

> *"The best technology feels like magic, not machinery."*

Intelligence should emerge from use, not configuration. Users should never have to set up rules, configure patterns, or explain their habits. The system observes and learns silently. When a suggestion appears, it should feel obvious in retrospect, not algorithmic.

**Application:** No onboarding wizard. No "tell us about your household." The app works as a basic list on day one. After 3-4 shopping trips, suggestions begin appearing naturally.

## 4.2 Progressive Disclosure

> *"Show only what's needed, when it's needed."*

Features reveal themselves at the moment of relevance. A first-time user sees a clean, simple list. A power user who has demonstrated need sees advanced options. Complexity is earned, not imposed.

**Application:** Meal tags don't appear until the user has added items that could logically be tagged. Analytics only appear after sufficient data exists to be meaningful.

## 4.3 One-Tap Everything

> *"Every additional tap is a small failure."*

The most common actions should require exactly one tap. Adding an item: type and press enter. Checking off an item: one tap. Accepting a suggestion: one tap. Dismissing a suggestion: one tap. No confirmation dialogs for reversible actions.

**Application:** All primary actions are immediate. Undo is always available for 5 seconds after any destructive action. Settings and configurations are accessible but never required.

## 4.4 Respect User Intelligence

> *"Assist, don't patronize."*

Suggestions are gentle nudges, not commands. Users can always dismiss suggestions without penalty. The system adapts when it's wrong rather than insisting. No gamification, no badges, no "streaks" that create guilt.

**Application:** Dismissed suggestions reduce confidence for that pattern. No push notifications unless explicitly requested. No dark patterns to drive engagement.

## 4.5 Privacy by Design

> *"Your groceries are your business."*

All pattern learning happens on-device by default. Data is never sold or used for advertising. Users own their data completely and can export or delete it at any time. Cloud sync is optional and encrypted end-to-end.

**Application:** Core ML models run locally. Cloud features are clearly labeled as requiring data upload. Data export available in standard formats (JSON, CSV).

---

# 5. Feature Specifications

## 5.1 Phase 1: Core Foundation

**Target:** MVP launch  
**Timeline:** 3 months development

Phase 1 establishes Restock as the simplest, most pleasant grocery list available while laying the invisible groundwork for intelligence.

### 5.1.1 The List

**Description:** A clean, fast, responsive grocery list that works offline and syncs seamlessly.

**Functional Requirements:**
- Add items via text input with auto-complete from personal history and common items
- Check/uncheck items with single tap; checked items move to "In Cart" section
- Swipe to delete items; 5-second undo toast
- Reorder items via drag-and-drop
- Optional quantity field (appears on long-press)
- Works fully offline; syncs when connection restored

### 5.1.2 Passive Pattern Learning

**Description:** The system silently learns purchase patterns from user behavior.

**Technical Requirements:**
- Record timestamp of every item check-off
- Calculate rolling average purchase interval for each unique item
- Track confidence score based on consistency (high variance = low confidence)
- All processing on-device; no server calls required
- Minimum 3 purchases required before pattern is established

### 5.1.3 Smart Suggestions

**Description:** Proactive item suggestions based on learned patterns.

**Functional Requirements:**
- Suggestions appear in dedicated section above main list
- Each suggestion shows item name and reason ("Usually every 7 days")
- Tap to add to list; X to dismiss
- Dismissed suggestions reduce pattern confidence; persistent dismissal removes from suggestions
- Maximum 5 suggestions shown at once (prioritized by confidence and urgency)

### 5.1.4 Soft Meal Tags

**Description:** Optional, lightweight meal categorization for items.

**Functional Requirements:**
- Four meal categories: Breakfast, Lunch, Dinner, Snack
- Tags added via small button on item row; appear as colored chip
- Items can have multiple tags or no tags
- "Meals" tab shows items grouped by meal tag
- System learns tag associations over time (eggs → breakfast after repeated tagging)

---

## 5.2 Phase 2: Natural Language Intelligence

**Target:** Version 2.0  
**Timeline:** 6 months post-launch

Phase 2 introduces conversational interaction, allowing users to communicate with Restock naturally.

### 5.2.1 Voice & Text Input

**Description:** Add items and interact via natural language.

**Capabilities:**
- "Add milk, eggs, and bread" → Adds three items
- "I'm making tacos tonight" → Suggests tortillas, ground beef, cheese, salsa, etc.
- "We're out of breakfast stuff" → Suggests items tagged as breakfast
- "What should I cook this week?" → Suggests meals based on what's in stock
- Voice activation via microphone button or wake word (optional)

### 5.2.2 Recipe Integration

**Description:** Import recipes and auto-populate ingredient lists.

**Capabilities:**
- Paste recipe URL → AI extracts ingredients and adds to list
- Save favorite recipes for one-tap future shopping
- Smart ingredient detection ("1 cup flour" → "Flour")
- Excludes ingredients user already has (based on recent purchases)

### 5.2.3 Contextual Awareness

**Description:** System understands context beyond explicit requests.

**Capabilities:**
- Seasonal suggestions (more grilling items in summer)
- Holiday awareness (suggests turkey in November)
- Weather-based suggestions (soup ingredients when cold)
- All contextual features can be disabled in settings

---

## 5.3 Phase 3: Visual Intelligence

**Target:** Version 3.0  
**Timeline:** 12 months post-launch

Phase 3 adds camera-based features for even faster input and smarter inventory awareness.

### 5.3.1 Fridge/Pantry Scan

**Description:** Photo-based inventory assessment.

**Capabilities:**
- Take photo of fridge interior → AI identifies items present
- Suggests items that appear low or missing
- Over time, builds model of typical fridge contents
- Privacy: Photos processed on-device, never uploaded unless explicitly shared

### 5.3.2 Receipt Scanning

**Description:** Scan receipts to log purchases automatically.

**Capabilities:**
- Photo of receipt → Extract items and prices
- Auto-check items that match current list
- Build price history for budget tracking
- Detect price increases and alert user to deals

### 5.3.3 Product Barcode Scanning

**Description:** Scan product barcodes for instant item addition.

**Capabilities:**
- Scan empty container → Add item to list
- Product database lookup for accurate naming
- Optional: Add to specific store list if user shops at multiple stores

---

## 5.4 Phase 4: Ecosystem & Integration

**Target:** Version 4.0  
**Timeline:** 18 months post-launch

Phase 4 connects Restock to the broader smart home and retail ecosystem.

### 5.4.1 Household Collaboration (Premium)

**Description:** Shared lists for families and roommates.

**Capabilities:**
- Create household with shared list visible to all members
- Real-time sync across all devices
- See who added/checked items
- Kid accounts with "request" items that need approval
- Assign items to specific people

### 5.4.2 Smart Home Integration

**Description:** Connect to IoT devices for automated inventory.

**Integrations:**
- Amazon Alexa / Google Assistant: Voice commands from any room
- Smart refrigerators: Auto-detect low inventory
- Smart scales: Track consumption of bulk items
- Apple Watch / Wear OS: Quick add from wrist

### 5.4.3 Delivery Service Integration

**Description:** Order groceries directly from the app.

**Capabilities:**
- Connect Instacart, Amazon Fresh, Walmart+, etc.
- One-tap conversion of list to delivery order
- Price comparison across services
- Auto-schedule regular delivery based on patterns

### 5.4.4 Budget & Analytics (Premium)

**Description:** Insights into spending and consumption patterns.

**Features:**
- Weekly/monthly spending trends
- Category breakdown (produce, dairy, meat, etc.)
- Budget setting with gentle alerts
- Waste reduction insights ("You often buy spinach but don't use it all")
- Year-over-year comparison

---

# 6. Core User Flows

## 6.1 First-Time User Experience

**Goal:** User completes first shopping trip with Restock within 60 seconds of opening app.

1. User opens app for first time
2. Single welcome screen: "Your grocery list, simplified. Start typing to add items."
3. Input field is auto-focused; keyboard appears immediately
4. User types "milk" and presses enter; item appears in list
5. User continues adding items naturally
6. No account required; list saves locally automatically

**Key Metric:** Time to first item added < 15 seconds

## 6.2 Weekly Shopping Flow

**Goal:** User builds complete shopping list in under 2 minutes.

1. User opens app; sees suggestions section (after pattern learning kicks in)
2. "Milk - usually every 7 days" → User taps to add
3. "Greek yogurt - running low?" → User taps to add
4. "Bananas" → User dismisses (not this week)
5. User manually adds "chicken breast" and "broccoli"
6. List is ready; user goes shopping
7. At store, user checks items as they go in cart

**Key Metric:** Items from suggestions vs. manually added (target: 60%+ from suggestions after 8 weeks)

## 6.3 In-Store Flow

**Goal:** Frictionless checkout of items while shopping.

1. User arrives at store; opens app
2. List shows unchecked items prominently
3. User finds milk → single tap to check off
4. Item animates to "In Cart" section
5. Repeat for all items
6. When list is complete, subtle celebration animation (optional)

**Key Metric:** Taps per item checked < 1.2 (accounting for scrolling)

## 6.4 Returning User Flow (Suggestion Learning)

**Goal:** System learns and improves suggestions over time.

1. Week 1: User manually adds all items
2. Week 2: User manually adds all items (system observing)
3. Week 3: User manually adds items (system building confidence)
4. Week 4: First suggestions appear for highest-confidence items
5. Week 8: Most regular items appear as suggestions automatically
6. Ongoing: System refines timing based on acceptance/dismissal patterns

**Key Metric:** Suggestion acceptance rate (target: >70% by week 8)

---

# 7. Design System

## 7.1 Color Palette

### Primary Colors

| Name | Hex | Usage |
|------|-----|-------|
| Primary | `#1F1F1F` | Text, icons, primary buttons |
| Accent | `#F59E0B` | Suggestions, smart badges, highlights |
| Success | `#10B981` | Checked items, confirmations |
| Background | `#FFFBF5` | Main background (warm off-white) |
| Card | `#FFFFFF` | Cards, inputs, elevated surfaces |
| Muted | `#8B8680` | Secondary text, placeholders |

### Meal Tag Colors

| Meal | Color | Emoji |
|------|-------|-------|
| Breakfast | `#F59E0B` (Amber) | 🌅 Sunrise |
| Lunch | `#10B981` (Emerald) | ☀️ Sun |
| Dinner | `#6366F1` (Indigo) | 🌙 Moon |
| Snack | `#EC4899` (Pink) | ✨ Sparkle |

## 7.2 Typography

| Element | Size / Weight | Usage |
|---------|---------------|-------|
| Display | 32px / Semibold | App title |
| Title | 20px / Medium | Section headers |
| Body | 15px / Regular | Item names, primary content |
| Caption | 12px / Regular | Suggestions reason, metadata |
| Label | 11px / Medium | Badges, tags, nav labels |

**Font Family:** DM Sans (primary), SF Pro / Roboto (system fallback)

## 7.3 Spacing System

Base unit: 4px. All spacing uses multiples of 4.

- **xs:** 4px (tight spacing, icon gaps)
- **sm:** 8px (between related elements)
- **md:** 16px (card padding, item gaps)
- **lg:** 24px (section spacing)
- **xl:** 32px (major section breaks)

## 7.4 Component Library

### 7.4.1 List Item Card

Rounded rectangle (14px radius), white background, subtle shadow (0 1px 8px rgba(0,0,0,0.03)). Contains: checkbox area, item name, optional cycle info, optional meal tag, swipe actions.

### 7.4.2 Suggestion Card

Rounded rectangle (16px radius), white background, slightly more prominent shadow. Contains: item name, dismissal X, suggestion reason, optional meal tag. Horizontally scrollable in suggestion section.

### 7.4.3 Input Field

Full-width container (16px radius), white background, integrated add button. Placeholder: "Add an item...". Auto-complete dropdown appears below when typing.

### 7.4.4 Bottom Navigation

Frosted glass effect (backdrop-blur: 20px), 3 tabs: List, Meals, History. Active tab shows filled icon, inactive shows outline. Safe area padding for devices with home indicator.

---

# 8. Technical Architecture

## 8.1 Platform Strategy

| Platform | Approach |
|----------|----------|
| iOS | Native Swift/SwiftUI for performance; Core ML for on-device learning |
| Android | Native Kotlin/Jetpack Compose; TensorFlow Lite for on-device learning |
| Web | Progressive Web App (PWA) for cross-platform access; offline-first architecture |
| Watch | Companion app for Apple Watch / Wear OS (Phase 4) |

## 8.2 Data Architecture

### 8.2.1 Local Storage

Primary data store is local SQLite database with the following key tables:

- **items:** id, name, normalized_name, created_at, is_active
- **purchases:** id, item_id, purchased_at, quantity, price (optional)
- **patterns:** item_id, avg_interval_days, confidence_score, last_calculated
- **meal_tags:** item_id, meal_type, frequency_count
- **current_list:** item_id, added_at, checked_at, position

### 8.2.2 Cloud Sync (Optional)

For users who enable cloud features:

- End-to-end encryption using user-derived keys
- Conflict resolution: Last-write-wins with timestamp comparison
- Delta sync for bandwidth efficiency
- Backend: Firebase Firestore or Supabase (to be evaluated)

## 8.3 AI/ML Architecture

### 8.3.1 Phase 1: Pattern Recognition (On-Device)

Simple statistical model running entirely on device:

- Calculate rolling average of intervals between purchases
- Confidence = 1 / (standard deviation / mean), capped at 1.0
- Suggestion threshold: confidence > 0.6 AND days_since_last >= avg_interval - 1
- No network calls required; works offline

### 8.3.2 Phase 2+: LLM Integration

For natural language features:

- On-device small language model (Apple Intelligence / Gemini Nano) for basic parsing
- Cloud API (Claude/GPT) for complex queries, recipe parsing, meal suggestions
- Graceful degradation: Complex features disable when offline
- User data anonymized before any cloud processing

## 8.4 API Design

RESTful API for cloud features with the following key endpoints:

- `POST /sync` - Sync local changes to cloud
- `GET /sync` - Fetch changes since last sync
- `POST /household` - Create/join household (Premium)
- `POST /ai/parse` - Natural language parsing (Premium)
- `POST /ai/recipe` - Recipe URL parsing (Premium)

---

# 9. Monetization Strategy

## 9.1 Pricing Philosophy

Restock follows a "generous free, valuable premium" model. The free tier must be genuinely useful for individual users indefinitely. Premium features target power users, families, and those who want advanced AI capabilities.

## 9.2 Tier Comparison

| Feature | Free | Premium |
|---------|:----:|:-------:|
| Basic list functionality | ✓ | ✓ |
| Pattern learning & suggestions | ✓ | ✓ |
| Meal tags | ✓ | ✓ |
| Offline mode | ✓ | ✓ |
| Cloud sync across devices | 1 device | Unlimited |
| Household sharing | - | Up to 6 members |
| Natural language input | - | ✓ |
| Recipe import & parsing | - | ✓ |
| Visual scanning (fridge, receipts) | - | ✓ |
| Budget tracking & analytics | - | ✓ |
| Delivery service integration | - | ✓ |
| Priority support | - | ✓ |

## 9.3 Pricing

| Plan | Price | Savings |
|------|-------|---------|
| Free | $0 forever | - |
| Premium Monthly | $4.99/month | - |
| Premium Annual | $39.99/year | 33% ($20/year) |

## 9.4 Revenue Projections

Conservative estimates based on comparable consumer apps:

- **Year 1:** 100,000 users, 3% conversion = 3,000 premium subscribers = **$120K ARR**
- **Year 2:** 500,000 users, 5% conversion = 25,000 premium subscribers = **$1M ARR**
- **Year 3:** 2,000,000 users, 7% conversion = 140,000 premium subscribers = **$5.6M ARR**

## 9.5 Additional Revenue Streams (Future)

- **Affiliate partnerships:** Commission on grocery delivery orders placed through app
- **Anonymous aggregated insights:** Sell trend data to CPG companies (opt-in only)
- **White-label licensing:** License technology to grocery chains

---

# 10. Success Metrics & KPIs

## 10.1 North Star Metric

> **Weekly Active Users Who Complete a Shopping Trip**

This metric captures both engagement (weekly activity) and value delivery (completing an actual shopping trip, not just opening the app).

## 10.2 Key Performance Indicators

| Metric | Target (Launch) | Target (Year 1) |
|--------|-----------------|-----------------|
| Day 1 Retention | 40% | 50% |
| Day 7 Retention | 20% | 30% |
| Day 30 Retention | 10% | 20% |
| Suggestion Acceptance Rate | 50% | 70% |
| Items from Suggestions (%) | 30% | 60% |
| Premium Conversion | 2% | 5% |
| App Store Rating | 4.5+ | 4.7+ |
| NPS Score | 30+ | 50+ |

## 10.3 Qualitative Success Indicators

- Unsolicited testimonials about "never forgetting items anymore"
- Word-of-mouth referrals (trackable via referral program)
- Press coverage highlighting simplicity + intelligence balance
- Users expressing surprise at how well suggestions match their needs

---

# 11. Competitive Analysis

## 11.1 Direct Competitors

| App | Strengths | Weaknesses | Restock Advantage |
|-----|-----------|------------|-------------------|
| AnyList | Sharing, recipe import, cross-platform | No predictive features, cluttered UI | Passive intelligence, simpler design |
| OurGroceries | Simple, reliable, good sharing | Dated design, no AI features | Modern UX, smart suggestions |
| Bring! | Beautiful design, good UX | Limited to Europe, no learning | Pattern learning, global market |
| Mealime | Meal planning, recipe integration | Requires weekly planning, high friction | Zero planning required, flexible |

## 11.2 Indirect Competitors

- **Apple Reminders / Google Keep:** Default choices; simple but lack grocery-specific features
- **Notes apps:** Paper list replacements; no learning, no sync
- **Grocery delivery apps (Instacart):** Different use case but could add list features

## 11.3 Competitive Moat

Restock's defensibility increases over time through:

- **Data network effects:** More usage = better suggestions = higher retention
- **Switching costs:** Personal patterns take months to rebuild elsewhere
- **Brand identity:** "The grocery list that learns" positioning is ownable
- **AI-first architecture:** Purpose-built for AI features vs. retrofitting existing apps

---

# 12. Product Roadmap

## 12.1 Phase 1: Foundation (Months 1-3)

**Goal:** Launch MVP with core list functionality and passive learning.

- **Month 1:** Core list functionality (add, check, delete, reorder)
- **Month 2:** Pattern learning engine, suggestion UI, meal tags
- **Month 3:** Polish, beta testing, App Store submission

**🎯 Milestone:** Public launch on iOS App Store

## 12.2 Phase 2: Growth (Months 4-9)

**Goal:** Expand platform reach and introduce natural language features.

- **Month 4-5:** Android app development and launch
- **Month 6:** Cloud sync, account system, basic Premium tier
- **Month 7-8:** Natural language input (Premium feature)
- **Month 9:** Recipe URL import (Premium feature)

**🎯 Milestone:** 100,000 total users, Premium launch

## 12.3 Phase 3: Intelligence (Months 10-15)

**Goal:** Add visual AI features and household sharing.

- **Month 10-11:** Household sharing (Premium)
- **Month 12:** Barcode scanning
- **Month 13-14:** Fridge scanning (Premium)
- **Month 15:** Receipt scanning (Premium)

**🎯 Milestone:** 500,000 total users, 25,000 premium subscribers

## 12.4 Phase 4: Ecosystem (Months 16-24)

**Goal:** Build platform integrations and advanced analytics.

- **Month 16-17:** Smart home integration (Alexa, Google Assistant)
- **Month 18-19:** Delivery service integration (Instacart, Amazon Fresh)
- **Month 20-21:** Budget tracking and analytics (Premium)
- **Month 22-24:** Apple Watch / Wear OS apps, Web PWA

**🎯 Milestone:** 2,000,000 total users, $5M+ ARR

---

# 13. Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Low differentiation at launch | Users may not immediately see value vs. existing apps | Emphasize "magic moment" when first suggestions appear; design for delight |
| Pattern learning fails | Suggestions are irrelevant or annoying | Conservative confidence thresholds; easy dismissal; learn from rejections |
| Competitor copies features | Established apps add pattern learning | Move fast; build data moat; focus on execution quality |
| Privacy concerns | Users uncomfortable with shopping data collection | On-device processing default; transparent privacy policy; data export/delete |
| Premium conversion too low | Free tier too good; no incentive to upgrade | Target premium features at families and power users; household sharing is key driver |
| AI costs exceed revenue | LLM API costs make Premium unprofitable | On-device models where possible; usage caps; monitor unit economics closely |

---

# 14. Appendix

## 14.1 Glossary

| Term | Definition |
|------|------------|
| Pattern | A detected regularity in user purchasing behavior (e.g., buys milk every 7 days) |
| Confidence Score | 0-1 value indicating reliability of a pattern (higher = more consistent) |
| Suggestion | A proactive recommendation to add an item based on learned patterns |
| Meal Tag | Optional label associating an item with a meal type (breakfast, lunch, dinner, snack) |
| Household | A group of users sharing a common grocery list (Premium feature) |
| Purchase Cycle | Average number of days between purchases of a specific item |

## 14.2 Document Revision History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | January 2026 | Initial release - Complete product design document |

## 14.3 Related Documents

- Technical Architecture Specification (TBD)
- API Design Document (TBD)
- Brand Guidelines (TBD)
- Privacy Policy Template (TBD)
- User Research Findings (TBD)

## 14.4 Contact

For questions about this document or the Restock product, contact the Product team.

---

*— End of Document —*
