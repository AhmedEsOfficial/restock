# Restock Android Application
## High-Level Implementation Plan

**Version:** 1.0  
**Date:** January 2026  
**Classification:** CONFIDENTIAL  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Technical Architecture](#2-technical-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Development Phases](#4-development-phases)
5. [Team Structure & Resources](#5-team-structure--resources)
6. [Development Timeline](#6-development-timeline)
7. [Testing Strategy](#7-testing-strategy)
8. [CI/CD & DevOps](#8-cicd--devops)
9. [Security & Privacy Implementation](#9-security--privacy-implementation)
10. [Risk Assessment & Mitigation](#10-risk-assessment--mitigation)
11. [Budget Estimation](#11-budget-estimation)
12. [Success Criteria & Launch Readiness](#12-success-criteria--launch-readiness)

---

## 1. Executive Summary

### 1.1 Project Overview

This document outlines the complete implementation strategy for the Restock Android application—an intelligently simple grocery list that learns user patterns passively. The Android implementation will be native Kotlin/Jetpack Compose, optimized for performance and aligned with the product vision of "invisible intelligence."

### 1.2 Strategic Objectives

| Objective | Target |
|-----------|--------|
| Time to first item added | < 15 seconds |
| App launch to interaction | < 2 seconds cold start |
| Offline-first architecture | 100% core functionality offline |
| Pattern learning accuracy | > 70% suggestion acceptance by week 8 |
| Google Play Store rating | 4.5+ stars |

### 1.3 Development Approach

We will adopt an **iterative, milestone-driven development** approach with 2-week sprints. The architecture follows Clean Architecture principles with clear separation between presentation, domain, and data layers—enabling testability, maintainability, and future feature expansion.

---

## 2. Technical Architecture

### 2.1 Architecture Pattern

**MVVM + Clean Architecture** with Unidirectional Data Flow (UDF)

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Screens   │  │  ViewModels │  │    UI State Holders │  │
│  │  (Compose)  │◄─┤   (Hilt)    │◄─┤    (StateFlow)      │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Use Cases  │  │  Entities   │  │ Repository Interfaces│  │
│  │             │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Repositories│  │  Local DB   │  │   Remote Services   │  │
│  │   (Impl)    │  │   (Room)    │  │   (Retrofit/Ktor)   │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              ML LAYER (TensorFlow Lite)                  ││
│  │  ┌──────────────────┐  ┌──────────────────────────────┐ ││
│  │  │ Pattern Engine   │  │ On-Device Inference Manager  │ ││
│  │  └──────────────────┘  └──────────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Core Modules

| Module | Responsibility |
|--------|----------------|
| `:app` | Application entry point, DI setup, navigation |
| `:core:ui` | Design system, themes, reusable Compose components |
| `:core:data` | Database, network, data sources |
| `:core:domain` | Business logic, entities, use cases |
| `:core:ml` | Pattern learning engine, TensorFlow Lite integration |
| `:feature:list` | Main grocery list functionality |
| `:feature:suggestions` | Smart suggestions UI and logic |
| `:feature:meals` | Meal tagging feature |
| `:feature:history` | Purchase history and analytics |
| `:feature:settings` | User preferences and configuration |
| `:feature:sync` | Cloud synchronization (Phase 2+) |

### 2.3 Data Flow Architecture

```
User Action → ViewModel → Use Case → Repository → Data Source
                                          │
                                          ├─► Room (Local)
                                          ├─► DataStore (Preferences)
                                          ├─► TFLite (ML Inference)
                                          └─► Remote API (Phase 2+)
```

### 2.4 Database Schema

```sql
-- Core Tables
items (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    is_active INTEGER DEFAULT 1,
    UNIQUE(normalized_name)
)

current_list (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES items(id),
    quantity INTEGER DEFAULT 1,
    unit TEXT,
    added_at INTEGER NOT NULL,
    checked_at INTEGER,
    position INTEGER NOT NULL,
    is_checked INTEGER DEFAULT 0
)

purchases (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES items(id),
    purchased_at INTEGER NOT NULL,
    quantity INTEGER DEFAULT 1,
    price_cents INTEGER,
    store_id TEXT
)

patterns (
    item_id TEXT PRIMARY KEY REFERENCES items(id),
    avg_interval_days REAL NOT NULL,
    confidence_score REAL NOT NULL,
    sample_count INTEGER NOT NULL,
    last_calculated INTEGER NOT NULL,
    last_suggested INTEGER
)

meal_tags (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES items(id),
    meal_type TEXT NOT NULL CHECK(meal_type IN ('breakfast','lunch','dinner','snack')),
    frequency_count INTEGER DEFAULT 1,
    UNIQUE(item_id, meal_type)
)

-- Indices for performance
CREATE INDEX idx_purchases_item_date ON purchases(item_id, purchased_at);
CREATE INDEX idx_patterns_confidence ON patterns(confidence_score DESC);
CREATE INDEX idx_current_list_position ON current_list(position);
```

---

## 3. Technology Stack

### 3.1 Core Technologies

| Category | Technology | Version | Rationale |
|----------|------------|---------|-----------|
| **Language** | Kotlin | 2.0+ | Modern, concise, null-safe, coroutines |
| **UI Framework** | Jetpack Compose | 1.6+ | Declarative, performant, modern Android UI |
| **Architecture** | MVVM + Clean Architecture | — | Testability, separation of concerns |
| **DI** | Hilt | 2.50+ | Official Android DI, compile-time safety |
| **Database** | Room | 2.6+ | SQLite abstraction, coroutines support |
| **Preferences** | DataStore | 1.0+ | Type-safe, async, replaces SharedPreferences |
| **Networking** | Ktor Client | 2.3+ | Kotlin-first, multiplatform ready |
| **Serialization** | Kotlin Serialization | 1.6+ | Native Kotlin, compile-time safe |
| **Image Loading** | Coil | 2.5+ | Kotlin-first, Compose integration |
| **ML** | TensorFlow Lite | 2.14+ | On-device inference, privacy-preserving |
| **Testing** | JUnit 5 + Turbine + Mockk | — | Modern testing stack |

### 3.2 Build & Tooling

| Tool | Purpose |
|------|---------|
| Gradle (Kotlin DSL) | Build system with version catalogs |
| Android Gradle Plugin | 8.2+ |
| KSP | Kotlin Symbol Processing (Room, Hilt) |
| Detekt | Static code analysis |
| Ktlint | Code formatting |
| Baseline Profiles | Startup optimization |
| R8 | Code shrinking and optimization |

### 3.3 Third-Party Libraries

| Library | Purpose | Phase |
|---------|---------|-------|
| `accompanist` | Compose utilities (permissions, navigation) | 1 |
| `lottie-compose` | Animations (celebration, empty states) | 1 |
| `reorderable` | Drag-and-drop list reordering | 1 |
| `mlkit-barcode` | Barcode scanning | 3 |
| `mlkit-text` | Receipt OCR | 3 |
| `cameraX` | Camera integration | 3 |

---

## 4. Development Phases

### 4.1 Phase 1: Core Foundation (Months 1-3)

**Objective:** Launch MVP with core list functionality and passive learning.

#### Sprint 1-2: Project Setup & Core Infrastructure
- [ ] Initialize multi-module project structure
- [ ] Configure Gradle with version catalogs
- [ ] Set up Hilt dependency injection
- [ ] Implement Room database with all core entities
- [ ] Create base ViewModel and UI state patterns
- [ ] Establish design system foundation (colors, typography, spacing)

#### Sprint 3-4: The List Feature
- [ ] Main list screen with Compose LazyColumn
- [ ] Add item functionality with auto-complete
- [ ] Check/uncheck animations
- [ ] Swipe-to-delete with undo snackbar
- [ ] Drag-and-drop reordering
- [ ] Quantity adjustment (long-press)
- [ ] "In Cart" section with checked items

#### Sprint 5-6: Pattern Learning Engine
- [ ] Purchase event tracking on item check-off
- [ ] Interval calculation algorithm
- [ ] Confidence score computation
- [ ] Minimum threshold enforcement (3 purchases)
- [ ] Background processing with WorkManager
- [ ] TensorFlow Lite model integration for advanced patterns

#### Sprint 7-8: Smart Suggestions
- [ ] Suggestions repository and use cases
- [ ] Suggestion prioritization algorithm
- [ ] Suggestions UI section (horizontally scrollable cards)
- [ ] One-tap add to list
- [ ] Dismiss with confidence reduction
- [ ] Maximum 5 suggestions limit

#### Sprint 9-10: Meal Tags & Polish
- [ ] Meal tag data model and UI
- [ ] Tag assignment flow
- [ ] Meals tab with grouped view
- [ ] System learns tag associations
- [ ] Performance optimization
- [ ] Accessibility audit and fixes
- [ ] Beta testing release

#### Sprint 11-12: Launch Preparation
- [ ] Crash reporting integration (Firebase Crashlytics)
- [ ] Analytics setup (privacy-respecting)
- [ ] App Store listing preparation
- [ ] Play Store review submission
- [ ] Launch monitoring dashboard

**Deliverables:**
- Fully functional grocery list app
- Passive pattern learning operational
- Smart suggestions working after 3+ shopping trips
- Meal tagging available
- Google Play Store launch

---

### 4.2 Phase 2: Growth & Intelligence (Months 4-9)

**Objective:** Expand with cloud sync and natural language features.

#### Month 4-5: Cloud Infrastructure
- [ ] User authentication (Firebase Auth / custom)
- [ ] Cloud sync architecture design
- [ ] End-to-end encryption implementation
- [ ] Conflict resolution strategy
- [ ] Delta sync for efficiency
- [ ] Account management UI

#### Month 6: Premium Subscription
- [ ] Google Play Billing integration
- [ ] Premium feature gating
- [ ] Subscription management UI
- [ ] Free/Premium tier enforcement

#### Month 7-8: Natural Language Input
- [ ] Voice input integration (SpeechRecognizer)
- [ ] Natural language parser (on-device)
- [ ] Multi-item addition ("milk, eggs, and bread")
- [ ] Intent recognition ("I'm making tacos")
- [ ] Meal-based suggestions
- [ ] Cloud LLM integration for complex queries (Premium)

#### Month 9: Recipe Integration
- [ ] URL parsing service
- [ ] Ingredient extraction AI
- [ ] Recipe storage model
- [ ] One-tap recipe shopping
- [ ] "Already have" detection

**Deliverables:**
- Cloud sync operational
- Premium tier launched
- Voice input working
- Natural language processing
- Recipe URL import

---

### 4.3 Phase 3: Visual Intelligence (Months 10-15)

**Objective:** Add camera-based features for advanced input.

#### Month 10-11: Household Sharing
- [ ] Household data model
- [ ] Real-time sync (WebSocket/Firebase)
- [ ] Member management
- [ ] Activity attribution
- [ ] Kid account request system

#### Month 12: Barcode Scanning
- [ ] CameraX integration
- [ ] ML Kit barcode detection
- [ ] Product database lookup (Open Food Facts)
- [ ] Quick-add from barcode

#### Month 13-14: Fridge Scanning
- [ ] Custom TFLite model for item detection
- [ ] Image processing pipeline
- [ ] Inventory assessment algorithm
- [ ] "Low/missing" suggestions
- [ ] Privacy: on-device only

#### Month 15: Receipt Scanning
- [ ] OCR with ML Kit
- [ ] Receipt parsing algorithm
- [ ] Auto-check matching items
- [ ] Price extraction and history

**Deliverables:**
- Household sharing (Premium)
- Barcode scanner
- Fridge scanning (Premium)
- Receipt scanning (Premium)

---

### 4.4 Phase 4: Ecosystem (Months 16-24)

**Objective:** Integrate with external services and platforms.

#### Month 16-17: Smart Home Integration
- [ ] Google Assistant Actions
- [ ] Widget for home screen
- [ ] Android Auto support

#### Month 18-19: Delivery Integration
- [ ] Instacart API integration
- [ ] Amazon Fresh connector
- [ ] Price comparison engine
- [ ] One-tap ordering

#### Month 20-21: Budget & Analytics
- [ ] Spending tracking
- [ ] Category analysis
- [ ] Budget alerts
- [ ] Waste reduction insights

#### Month 22-24: Platform Expansion
- [ ] Wear OS companion app
- [ ] Tablet-optimized UI
- [ ] Chromebook support
- [ ] Web PWA companion

---

## 5. Team Structure & Resources

### 5.1 Core Team (Phase 1)

| Role | Count | Responsibilities |
|------|-------|------------------|
| **Tech Lead / Senior Android** | 1 | Architecture, code review, technical decisions |
| **Senior Android Developer** | 1 | Core features, ML integration |
| **Mid Android Developer** | 2 | Feature development, testing |
| **UI/UX Designer** | 1 | Design system, user flows, accessibility |
| **QA Engineer** | 1 | Test automation, manual testing |
| **Product Manager** | 0.5 | Requirements, prioritization (shared) |

### 5.2 Expanded Team (Phase 2+)

| Role | Count | Phase |
|------|-------|-------|
| Backend Developer | 2 | Phase 2 |
| ML Engineer | 1 | Phase 3 |
| DevOps Engineer | 1 | Phase 2 |
| Additional Android Dev | 1 | Phase 3 |

### 5.3 External Resources

| Resource | Purpose | Engagement |
|----------|---------|------------|
| Security Auditor | Privacy & security review | Pre-launch (Phase 1, 2) |
| Accessibility Consultant | WCAG compliance | Pre-launch |
| Localization Service | Multi-language support | Phase 2+ |

---

## 6. Development Timeline

### 6.1 Phase 1 Gantt Chart

```
Month 1         Month 2         Month 3
|----|----|----|----|----|----|----|----|----|----|----|----|
[=== Project Setup & Infrastructure ===]
         [======= List Feature ========]
                  [=== Pattern Learning Engine ===]
                           [==== Smart Suggestions ====]
                                    [== Meal Tags ==]
                                             [= Launch Prep =]
                                                      🚀 Launch
```

### 6.2 Key Milestones

| Milestone | Target Date | Success Criteria |
|-----------|-------------|------------------|
| **M1:** Development Kickoff | Week 1 | Project structure complete, CI/CD operational |
| **M2:** List Feature Complete | Week 4 | All CRUD operations, offline-first working |
| **M3:** Pattern Engine Operational | Week 6 | Suggestions appearing after 3 purchases |
| **M4:** Alpha Release | Week 8 | Internal testing, core flows functional |
| **M5:** Beta Release | Week 10 | External beta, crash rate < 0.5% |
| **M6:** Production Launch | Week 12 | Google Play Store live |
| **M7:** Premium Launch | Month 6 | Subscription system operational |
| **M8:** Visual Features | Month 15 | Camera features complete |
| **M9:** Full Platform | Month 24 | All integrations live |

---

## 7. Testing Strategy

### 7.1 Testing Pyramid

```
                    ┌─────────────┐
                    │   Manual    │  5%
                    │   E2E       │
                    ├─────────────┤
                    │Integration  │  15%
                    │   Tests     │
                    ├─────────────┤
                    │             │
                    │    Unit     │  80%
                    │   Tests     │
                    │             │
                    └─────────────┘
```

### 7.2 Testing Requirements by Layer

| Layer | Framework | Coverage Target | Focus Areas |
|-------|-----------|-----------------|-------------|
| **Unit** | JUnit 5 + Mockk | 80%+ | ViewModels, Use Cases, Repositories, Pattern Engine |
| **Integration** | AndroidX Test | 60%+ | Room queries, DataStore, ML inference |
| **UI** | Compose Testing | Key flows | Add item, check item, suggestions, navigation |
| **E2E** | Maestro | Critical paths | Onboarding, full shopping trip |

### 7.3 Specific Testing Areas

**Pattern Learning Engine:**
- Unit tests for interval calculation accuracy
- Edge cases: irregular shopping, long gaps, seasonal items
- Confidence score validation
- Suggestion prioritization logic

**Offline Functionality:**
- Network disconnection during operations
- Data persistence validation
- Sync conflict resolution (Phase 2)

**Performance Testing:**
- Cold start time < 2 seconds
- List scroll at 60fps with 500+ items
- Memory usage under 150MB typical
- Battery impact assessment

### 7.4 Quality Gates

| Gate | Criteria | Enforcement |
|------|----------|-------------|
| PR Merge | Unit tests pass, lint clean, coverage maintained | CI blocking |
| Alpha | Integration tests pass, no P0/P1 bugs | Release manager |
| Beta | E2E pass, crash rate < 1%, performance benchmarks met | QA sign-off |
| Production | Crash rate < 0.5%, ANR rate < 0.2%, no P0 bugs | Full team sign-off |

---

## 8. CI/CD & DevOps

### 8.1 CI/CD Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                        GitHub Actions                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐  │
│  │  Lint &  │───►│   Unit   │───►│  Build   │───►│Integration│  │
│  │  Format  │    │  Tests   │    │   APK    │    │   Tests   │  │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘  │
│        │              │               │               │          │
│        └──────────────┴───────────────┴───────────────┘          │
│                              │                                   │
│                    ┌─────────▼─────────┐                        │
│                    │   Quality Gates   │                        │
│                    └─────────┬─────────┘                        │
│                              │                                   │
│         ┌────────────────────┼────────────────────┐             │
│         ▼                    ▼                    ▼             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │   Alpha     │    │    Beta     │    │  Production │         │
│  │  (Internal) │    │  (Firebase) │    │(Play Store) │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 Build Variants

| Variant | Purpose | Signing | Endpoints |
|---------|---------|---------|-----------|
| `debug` | Development | Debug keystore | Local/staging |
| `alpha` | Internal testing | Release keystore | Staging |
| `beta` | External testing | Release keystore | Production |
| `release` | Production | Release keystore | Production |

### 8.3 Release Process

1. **Feature Complete:** All sprint features merged to `develop`
2. **Code Freeze:** Branch `release/x.y.z` from `develop`
3. **QA Cycle:** Regression testing on release branch
4. **Bug Fixes:** Cherry-pick critical fixes
5. **Release Candidate:** Deploy to beta track
6. **Staged Rollout:** 5% → 25% → 50% → 100%
7. **Monitoring:** 48-hour observation at each stage

### 8.4 Infrastructure

| Service | Purpose | Provider |
|---------|---------|----------|
| CI/CD | Build automation | GitHub Actions |
| Crash Reporting | Error tracking | Firebase Crashlytics |
| Analytics | Usage metrics | Firebase Analytics (minimal) |
| Beta Distribution | Test builds | Firebase App Distribution |
| Feature Flags | Gradual rollout | Firebase Remote Config |
| Backend (Phase 2) | API & sync | GCP / AWS (TBD) |

---

## 9. Security & Privacy Implementation

### 9.1 Privacy-First Architecture

| Principle | Implementation |
|-----------|----------------|
| **On-device by default** | All ML inference runs locally via TensorFlow Lite |
| **Minimal collection** | No PII required for core functionality |
| **Encryption at rest** | Room database encrypted with SQLCipher |
| **Encryption in transit** | TLS 1.3 for all network calls |
| **User control** | Complete data export (JSON/CSV) and deletion |

### 9.2 Data Classification

| Data Type | Storage | Sync | Encryption |
|-----------|---------|------|------------|
| Grocery list | Local | Optional | AES-256 |
| Purchase history | Local | Optional | AES-256 |
| Patterns | Local | Never | AES-256 |
| User preferences | Local | Optional | AES-256 |
| Auth tokens | Android Keystore | N/A | Hardware-backed |

### 9.3 Security Measures

**Authentication (Phase 2+):**
- OAuth 2.0 / OpenID Connect
- Biometric authentication option
- Session management with secure token refresh
- Rate limiting on auth endpoints

**Code Security:**
- ProGuard/R8 obfuscation
- Certificate pinning for API calls
- Root/tampering detection (optional)
- No sensitive data in logs

**Third-Party Audit:**
- Pre-launch security assessment
- Annual penetration testing
- OWASP MASVS compliance review

---

## 10. Risk Assessment & Mitigation

### 10.1 Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Pattern learning inaccuracy** | Medium | High | Conservative thresholds, easy dismissal, continuous refinement |
| **ML model performance issues** | Medium | Medium | Benchmark on low-end devices, fallback to simple stats |
| **Compose performance edge cases** | Low | Medium | Profile aggressively, baseline profiles, lazy loading |
| **Database migration failures** | Low | High | Comprehensive migration tests, rollback capability |
| **Third-party API changes** | Medium | Medium | Abstraction layers, version pinning, monitoring |

### 10.2 Product Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Low differentiation at launch** | Medium | High | Focus on "magic moment" marketing, delight in details |
| **Premium conversion too low** | Medium | High | Household sharing as key driver, value demonstration |
| **User data loss** | Low | Critical | Automated backups, sync redundancy, data recovery tools |

### 10.3 Operational Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **Key person dependency** | Medium | High | Documentation, knowledge sharing, code review culture |
| **Scope creep** | High | Medium | Strict sprint discipline, PM gatekeeping |
| **Release delays** | Medium | Medium | Buffer time in schedule, MVP prioritization |

---

## 11. Budget Estimation

### 11.1 Phase 1 Costs (3 Months)

| Category | Monthly Cost | Total (3 mo) |
|----------|--------------|--------------|
| **Personnel** | | |
| Tech Lead (1) | $18,000 | $54,000 |
| Senior Dev (1) | $15,000 | $45,000 |
| Mid Dev (2) | $22,000 | $66,000 |
| UI/UX Designer (1) | $12,000 | $36,000 |
| QA Engineer (1) | $10,000 | $30,000 |
| PM (0.5) | $6,000 | $18,000 |
| **Subtotal Personnel** | **$83,000** | **$249,000** |
| | | |
| **Infrastructure** | | |
| GitHub Team | $200 | $600 |
| Firebase (Spark→Blaze) | $100 | $300 |
| CI/CD compute | $300 | $900 |
| Test devices | — | $3,000 |
| **Subtotal Infra** | **$600** | **$4,800** |
| | | |
| **Services** | | |
| Security audit | — | $8,000 |
| Accessibility review | — | $3,000 |
| Design tools (Figma) | $150 | $450 |
| **Subtotal Services** | **$150** | **$11,450** |
| | | |
| **Contingency (15%)** | | **$39,787** |
| | | |
| **TOTAL PHASE 1** | | **$305,037** |

### 11.2 Annual Run Rate (Post-Launch)

| Category | Monthly | Annual |
|----------|---------|--------|
| Core team (scaled) | $60,000 | $720,000 |
| Infrastructure | $2,000 | $24,000 |
| Third-party services | $1,500 | $18,000 |
| Marketing/ASO | $5,000 | $60,000 |
| **Total** | **$68,500** | **$822,000** |

---

## 12. Success Criteria & Launch Readiness

### 12.1 Launch Checklist

**Technical Readiness:**
- [ ] All P0 features complete and tested
- [ ] Crash rate < 0.5% in beta
- [ ] ANR rate < 0.2%
- [ ] Cold start < 2 seconds (p95)
- [ ] All critical user flows tested on 10+ device configurations
- [ ] Accessibility: TalkBack compatible, touch targets 48dp+
- [ ] Offline mode fully functional

**Store Readiness:**
- [ ] App listing complete (all locales)
- [ ] Screenshots and feature graphics finalized
- [ ] Privacy policy published
- [ ] Data safety questionnaire complete
- [ ] Content rating obtained
- [ ] Release notes prepared

**Operational Readiness:**
- [ ] Crashlytics monitoring configured
- [ ] On-call rotation established
- [ ] Rollback procedure documented
- [ ] Customer support FAQ prepared
- [ ] Social media response plan ready

### 12.2 Post-Launch Monitoring (First 30 Days)

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Crash-free users | > 99.5% | < 99% |
| ANR rate | < 0.2% | > 0.5% |
| Day 1 retention | > 40% | < 30% |
| Day 7 retention | > 20% | < 15% |
| Store rating | > 4.5 | < 4.0 |
| Suggestion acceptance | > 50% | < 30% |

### 12.3 Definition of Done (Launch)

> **Restock Android v1.0 is launch-ready when:**
>
> 1. A new user can add their first item within 15 seconds of opening the app
> 2. The app works 100% offline with no degraded experience
> 3. After 3 shopping trips, relevant suggestions begin appearing
> 4. Suggestions achieve > 50% acceptance rate in beta testing
> 5. No P0 or P1 bugs remain open
> 6. All quality gates are passed
> 7. The team is confident in the product

---

## Appendix A: Technical Specifications

### A.1 Minimum SDK Requirements

| Requirement | Value |
|-------------|-------|
| minSdk | 26 (Android 8.0) |
| targetSdk | 35 (Android 15) |
| compileSdk | 35 |

**Rationale:** SDK 26 covers 95%+ of active Android devices while enabling modern APIs (WorkManager, Autofill, etc.)

### A.2 Device Support Matrix

| Category | Devices | Priority |
|----------|---------|----------|
| **Tier 1** (primary testing) | Pixel 6+, Samsung S21+, OnePlus 9+ | P0 |
| **Tier 2** (compatibility) | Pixel 4, Samsung A52, Xiaomi 11 | P1 |
| **Tier 3** (edge cases) | Low-RAM devices, tablets | P2 |

### A.3 Performance Budgets

| Metric | Budget |
|--------|--------|
| APK size | < 15 MB |
| Cold start | < 2 sec |
| Warm start | < 500 ms |
| Frame rate | 60 fps (no drops) |
| Memory (typical) | < 150 MB |
| Battery (hourly background) | < 1% |

---

## Appendix B: API Contracts (Phase 2)

### B.1 Sync Endpoint

```
POST /api/v1/sync
Authorization: Bearer <token>

Request:
{
  "client_id": "uuid",
  "last_sync_at": "ISO8601",
  "changes": [
    {
      "table": "items|current_list|purchases|meal_tags",
      "operation": "INSERT|UPDATE|DELETE",
      "id": "uuid",
      "data": { ... },
      "timestamp": "ISO8601"
    }
  ]
}

Response:
{
  "server_changes": [ ... ],
  "conflicts": [ ... ],
  "sync_token": "opaque_string"
}
```

---

## Appendix C: ML Model Specifications

### C.1 Pattern Recognition Model (Phase 1)

**Approach:** Statistical model (no neural network required)

```
Confidence Score = 1 / (1 + CV)
where CV = coefficient of variation = σ / μ

Suggestion Trigger:
  IF confidence > 0.6
  AND days_since_last >= avg_interval - 1
  AND sample_count >= 3
  THEN suggest
```

### C.2 Advanced Pattern Model (Phase 2+)

**Model Type:** Time series prediction (TensorFlow Lite)
**Input Features:** Purchase history, day of week, time of year, price sensitivity
**Output:** Next purchase probability distribution
**Model Size Target:** < 5 MB

---

*Document End*

**Prepared by:** CTO Office  
**Review Status:** Draft  
**Next Review:** Pre-development kickoff
