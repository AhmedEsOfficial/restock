# Sprint 11: Recipe Integration - Parsing Service
## Phase 2, Month 9, Weeks 1-2

**Sprint Duration:** 2 weeks
**Sprint Number:** 11 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint implements the recipe URL parsing service that allows users to paste a recipe URL and automatically extract ingredients. The backend will fetch recipes, parse ingredients, normalize them to grocery items, and detect items the user already has.

### Sprint Goal
> *"Build a robust recipe URL parsing service that accurately extracts and normalizes ingredients from popular recipe websites."*

### Key Outcomes
- Recipe URL fetching service functional
- Recipe schema (schema.org) parsing working
- HTML fallback parsing for non-schema sites
- Ingredient extraction and normalization
- "Already have" detection implemented
- Recipe API endpoint operational

### Prerequisites from Sprint 10
- Natural language features complete
- LLM infrastructure operational
- Context building for user data working

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-11.1: Recipe URL Fetching
**As a** backend service
**I want** to fetch recipe pages from URLs
**So that** I can extract recipe data

**Acceptance Criteria:**
- [ ] Accepts URLs from supported sites
- [ ] Handles HTTPS redirects
- [ ] Respects robots.txt (ethical scraping)
- [ ] Timeout after 10 seconds
- [ ] User-Agent identifies as Restock app
- [ ] Rate limits to avoid abuse
- [ ] Handles invalid/unreachable URLs

**Story Points:** 5
**Assignee:** Backend Developer 1

**URL Fetcher Service:**
```kotlin
class RecipeUrlFetcher @Inject constructor(
    private val httpClient: HttpClient,
) {
    companion object {
        private const val TIMEOUT_MS = 10_000L
        private const val USER_AGENT = "RestockApp/1.0 (Grocery List App; +https://restock.app/bot)"
        private val SUPPORTED_DOMAINS = setOf(
            "allrecipes.com",
            "foodnetwork.com",
            "seriouseats.com",
            "cooking.nytimes.com",
            "epicurious.com",
            "bonappetit.com",
            "delish.com",
            "tasty.co",
            "simplyrecipes.com",
            "budgetbytes.com"
        )
    }

    suspend fun fetchRecipePage(url: String): FetchResult {
        // Validate URL
        val parsedUrl = try {
            URL(url)
        } catch (e: Exception) {
            return FetchResult.InvalidUrl
        }

        // Check supported domain
        val domain = parsedUrl.host.removePrefix("www.")
        if (domain !in SUPPORTED_DOMAINS && !hasRecipeSchema(url)) {
            return FetchResult.UnsupportedSite(domain)
        }

        // Fetch page
        return try {
            withTimeout(TIMEOUT_MS) {
                val response = httpClient.get(url) {
                    header("User-Agent", USER_AGENT)
                    header("Accept", "text/html")
                }

                when (response.status) {
                    HttpStatusCode.OK -> FetchResult.Success(
                        html = response.bodyAsText(),
                        finalUrl = response.request.url.toString()
                    )
                    HttpStatusCode.NotFound -> FetchResult.NotFound
                    else -> FetchResult.HttpError(response.status.value)
                }
            }
        } catch (e: TimeoutCancellationException) {
            FetchResult.Timeout
        } catch (e: Exception) {
            FetchResult.NetworkError(e.message)
        }
    }
}

sealed interface FetchResult {
    data class Success(val html: String, val finalUrl: String) : FetchResult
    data object InvalidUrl : FetchResult
    data class UnsupportedSite(val domain: String) : FetchResult
    data object NotFound : FetchResult
    data object Timeout : FetchResult
    data class HttpError(val code: Int) : FetchResult
    data class NetworkError(val message: String?) : FetchResult
}
```

---

#### US-11.2: Recipe Schema Parsing
**As a** backend service
**I want** to parse schema.org Recipe markup
**So that** I can extract structured recipe data

**Acceptance Criteria:**
- [ ] Parses JSON-LD @type: Recipe
- [ ] Extracts recipe name
- [ ] Extracts ingredients list
- [ ] Extracts yield/servings
- [ ] Extracts prep/cook time (optional)
- [ ] Handles multiple schema formats
- [ ] Falls back to HTML parsing if no schema

**Story Points:** 8
**Assignee:** Backend Developer 1

**Schema Parser:**
```kotlin
class RecipeSchemaParser @Inject constructor() {

    fun parseSchema(html: String): SchemaParseResult {
        // Find JSON-LD scripts
        val jsonLdPattern = Regex(
            """<script[^>]*type=["']application/ld\+json["'][^>]*>(.*?)</script>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )

        val matches = jsonLdPattern.findAll(html)

        for (match in matches) {
            val jsonContent = match.groupValues[1].trim()
            val recipe = tryParseRecipeJson(jsonContent)
            if (recipe != null) {
                return SchemaParseResult.Success(recipe)
            }
        }

        return SchemaParseResult.NoSchema
    }

    private fun tryParseRecipeJson(json: String): RecipeSchema? {
        return try {
            val parsed = Json.parseToJsonElement(json)

            // Handle array of schemas
            val schemas = when (parsed) {
                is JsonArray -> parsed.map { it.jsonObject }
                is JsonObject -> listOf(parsed)
                else -> return null
            }

            // Find Recipe type
            for (schema in schemas) {
                val type = schema["@type"]?.let { type ->
                    when (type) {
                        is JsonPrimitive -> type.content
                        is JsonArray -> type.map { it.jsonPrimitive.content }
                            .find { it == "Recipe" }
                        else -> null
                    }
                }

                if (type == "Recipe" || type?.contains("Recipe") == true) {
                    return extractRecipeData(schema)
                }

                // Check @graph for nested schemas
                schema["@graph"]?.jsonArray?.forEach { item ->
                    val graphType = item.jsonObject["@type"]?.jsonPrimitive?.content
                    if (graphType == "Recipe") {
                        return extractRecipeData(item.jsonObject)
                    }
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    private fun extractRecipeData(schema: JsonObject): RecipeSchema {
        val name = schema["name"]?.jsonPrimitive?.content ?: "Unknown Recipe"

        val ingredients = schema["recipeIngredient"]?.jsonArray
            ?.map { it.jsonPrimitive.content }
            ?: emptyList()

        val yield = schema["recipeYield"]?.let { yieldValue ->
            when (yieldValue) {
                is JsonPrimitive -> parseYield(yieldValue.content)
                is JsonArray -> parseYield(yieldValue.first().jsonPrimitive.content)
                else -> null
            }
        }

        return RecipeSchema(
            name = name,
            ingredients = ingredients,
            servings = yield,
            prepTime = schema["prepTime"]?.jsonPrimitive?.content,
            cookTime = schema["cookTime"]?.jsonPrimitive?.content,
            imageUrl = schema["image"]?.let { extractImageUrl(it) }
        )
    }

    private fun parseYield(text: String): Int? {
        return Regex("(\\d+)").find(text)?.value?.toIntOrNull()
    }
}

data class RecipeSchema(
    val name: String,
    val ingredients: List<String>,
    val servings: Int?,
    val prepTime: String?,
    val cookTime: String?,
    val imageUrl: String?
)

sealed interface SchemaParseResult {
    data class Success(val recipe: RecipeSchema) : SchemaParseResult
    data object NoSchema : SchemaParseResult
}
```

---

#### US-11.3: HTML Fallback Parsing
**As a** backend service
**I want** to extract ingredients from HTML
**So that** non-schema sites still work

**Acceptance Criteria:**
- [ ] Identifies ingredient list sections
- [ ] Handles common CSS class patterns
- [ ] Extracts list items from ul/ol
- [ ] Cleans HTML tags and entities
- [ ] Reasonable accuracy (>80%)
- [ ] Clear failure when can't parse

**Story Points:** 8
**Assignee:** Backend Developer 2

**HTML Parser:**
```kotlin
class RecipeHtmlParser @Inject constructor() {

    // Common CSS class patterns for ingredients
    private val ingredientClassPatterns = listOf(
        "ingredient",
        "recipe-ingredient",
        "ingredients-item",
        "wprm-recipe-ingredient",
        "tasty-recipes-ingredient"
    )

    fun parseHtml(html: String): HtmlParseResult {
        val doc = Jsoup.parse(html)

        // Try to find ingredient section
        val ingredients = mutableListOf<String>()

        // Method 1: Look for elements with ingredient-related classes
        for (pattern in ingredientClassPatterns) {
            val elements = doc.select("[class*=$pattern]")
            if (elements.isNotEmpty()) {
                elements.forEach { element ->
                    val text = element.text().trim()
                    if (text.isNotEmpty() && isLikelyIngredient(text)) {
                        ingredients.add(text)
                    }
                }
                if (ingredients.size >= 3) break
            }
        }

        // Method 2: Look for lists after "Ingredients" heading
        if (ingredients.isEmpty()) {
            val headings = doc.select("h1, h2, h3, h4")
            for (heading in headings) {
                if (heading.text().contains("ingredient", ignoreCase = true)) {
                    val nextList = heading.nextElementSibling()
                    if (nextList?.tagName() in listOf("ul", "ol")) {
                        nextList?.select("li")?.forEach { li ->
                            ingredients.add(li.text().trim())
                        }
                        break
                    }
                }
            }
        }

        // Extract recipe name
        val name = doc.select("h1").firstOrNull()?.text()
            ?: doc.title().split("|").first().trim()

        return if (ingredients.size >= 3) {
            HtmlParseResult.Success(
                name = name,
                ingredients = ingredients
            )
        } else {
            HtmlParseResult.InsufficientData
        }
    }

    private fun isLikelyIngredient(text: String): Boolean {
        // Heuristics to identify ingredient text
        val hasQuantity = text.contains(Regex("^\\d|\\b\\d+\\s"))
        val hasUnit = text.contains(Regex("\\b(cup|tbsp|tsp|oz|lb|g|kg|ml)s?\\b", RegexOption.IGNORE_CASE))
        val isReasonableLength = text.length in 3..200
        val notInstruction = !text.contains(Regex("^(step|\\d\\.\\s)"))

        return isReasonableLength && notInstruction && (hasQuantity || hasUnit || text.split(" ").size <= 6)
    }
}

sealed interface HtmlParseResult {
    data class Success(val name: String, val ingredients: List<String>) : HtmlParseResult
    data object InsufficientData : HtmlParseResult
}
```

---

#### US-11.4: Ingredient Normalization
**As a** system
**I want** to normalize ingredient text to grocery items
**So that** users see clean, usable items

**Acceptance Criteria:**
- [ ] Extracts item name from ingredient text
- [ ] Extracts quantity and unit
- [ ] Removes preparation instructions ("chopped", "diced")
- [ ] Handles common abbreviations
- [ ] Normalizes units to standard forms
- [ ] Groups similar items (various cheeses → cheese)

**Story Points:** 8
**Assignee:** Backend Developer 2

**Ingredient Normalizer:**
```kotlin
class IngredientNormalizer @Inject constructor() {

    private val preparationWords = setOf(
        "chopped", "diced", "minced", "sliced", "grated", "shredded",
        "melted", "softened", "crushed", "ground", "fresh", "frozen",
        "cooked", "uncooked", "raw", "dried", "canned", "drained",
        "rinsed", "peeled", "seeded", "trimmed", "divided", "optional",
        "for garnish", "to taste", "as needed"
    )

    private val unitMap = mapOf(
        "tablespoon" to "tbsp", "tablespoons" to "tbsp", "tbsps" to "tbsp",
        "teaspoon" to "tsp", "teaspoons" to "tsp", "tsps" to "tsp",
        "ounce" to "oz", "ounces" to "oz",
        "pound" to "lb", "pounds" to "lb", "lbs" to "lb",
        "cup" to "cup", "cups" to "cup",
        "pint" to "pint", "pints" to "pint",
        "quart" to "qt", "quarts" to "qt",
        "gallon" to "gal", "gallons" to "gal",
        "liter" to "L", "liters" to "L",
        "milliliter" to "ml", "milliliters" to "ml",
        "gram" to "g", "grams" to "g",
        "kilogram" to "kg", "kilograms" to "kg"
    )

    fun normalize(ingredientText: String): NormalizedIngredient {
        var text = ingredientText.lowercase().trim()

        // Remove parenthetical notes
        text = text.replace(Regex("\\([^)]*\\)"), "")

        // Remove preparation instructions
        for (prep in preparationWords) {
            text = text.replace(Regex("\\b$prep\\b,?"), "")
        }

        // Extract quantity
        val quantityMatch = Regex("^([\\d./]+(?:\\s*-\\s*[\\d./]+)?|\\d+\\s*\\d+/\\d+)\\s*")
            .find(text)
        val quantity = quantityMatch?.value?.let { parseQuantity(it.trim()) }
        text = quantityMatch?.let { text.removePrefix(it.value) } ?: text

        // Extract unit
        val unitMatch = Regex("^(${unitMap.keys.joinToString("|")})\\s+(?:of\\s+)?", RegexOption.IGNORE_CASE)
            .find(text)
        val unit = unitMatch?.groupValues?.get(1)?.let { unitMap[it.lowercase()] }
        text = unitMatch?.let { text.removePrefix(it.value) } ?: text

        // Clean remaining text
        val itemName = text
            .replace(Regex(",.*"), "")  // Remove everything after comma
            .replace(Regex("\\s+"), " ")
            .trim()
            .capitalizeWords()

        return NormalizedIngredient(
            name = itemName,
            quantity = quantity,
            unit = unit,
            rawText = ingredientText
        )
    }

    private fun parseQuantity(text: String): Double? {
        return try {
            when {
                // Range: "1-2" -> take upper bound
                text.contains("-") -> text.split("-").last().trim().toDouble()
                // Mixed number: "1 1/2" -> 1.5
                text.contains(" ") && text.contains("/") -> {
                    val parts = text.split(" ")
                    parts[0].toDouble() + parseFraction(parts[1])
                }
                // Fraction: "1/2" -> 0.5
                text.contains("/") -> parseFraction(text)
                // Plain number
                else -> text.toDouble()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseFraction(text: String): Double {
        val parts = text.split("/")
        return parts[0].toDouble() / parts[1].toDouble()
    }
}

data class NormalizedIngredient(
    val name: String,
    val quantity: Double?,
    val unit: String?,
    val rawText: String
)
```

---

#### US-11.5: "Already Have" Detection
**As a** user
**I want** to know which ingredients I already have
**So that** I only add what I need

**Acceptance Criteria:**
- [ ] Compares ingredients against current list
- [ ] Compares against recent purchases (30 days)
- [ ] Fuzzy matching for similar names
- [ ] Handles plurals and variations
- [ ] Clearly marks "already have" items
- [ ] User can override detection

**Story Points:** 5
**Assignee:** Tech Lead

**Already Have Detector:**
```kotlin
class AlreadyHaveDetector @Inject constructor(
    private val itemRepository: ItemRepository,
    private val purchaseRepository: PurchaseRepository,
) {
    suspend fun detectAlreadyHave(
        ingredients: List<NormalizedIngredient>,
        userId: String
    ): List<IngredientWithStatus> {
        // Get user's items
        val currentListItems = itemRepository
            .getCurrentListItems(userId)
            .map { it.normalizedName }
            .toSet()

        val recentPurchases = purchaseRepository
            .getRecentPurchases(userId, days = 30)
            .map { it.normalizedName }
            .toSet()

        val knownItems = currentListItems + recentPurchases

        return ingredients.map { ingredient ->
            val normalizedName = ingredient.name.lowercase().trim()

            val match = findMatch(normalizedName, knownItems)

            IngredientWithStatus(
                ingredient = ingredient,
                alreadyHave = match != null,
                matchedItem = match,
                source = when {
                    match in currentListItems -> MatchSource.CURRENT_LIST
                    match in recentPurchases -> MatchSource.RECENT_PURCHASE
                    else -> null
                }
            )
        }
    }

    private fun findMatch(name: String, knownItems: Set<String>): String? {
        // Exact match
        if (name in knownItems) return name

        // Plural/singular variations
        val singularized = name.removeSuffix("s").removeSuffix("es")
        val pluralized = name + "s"

        if (singularized in knownItems) return singularized
        if (pluralized in knownItems) return pluralized

        // Fuzzy match (Levenshtein distance <= 2 for short words)
        for (item in knownItems) {
            if (levenshteinDistance(name, item) <= 2 && name.length > 4) {
                return item
            }
            // Substring match for compound names
            if (item.contains(name) || name.contains(item)) {
                return item
            }
        }

        return null
    }
}

data class IngredientWithStatus(
    val ingredient: NormalizedIngredient,
    val alreadyHave: Boolean,
    val matchedItem: String?,
    val source: MatchSource?
)

enum class MatchSource {
    CURRENT_LIST,
    RECENT_PURCHASE
}
```

---

#### US-11.6: Recipe Import Endpoint
**As a** mobile app
**I want** an API to import recipes from URLs
**So that** users can add recipe ingredients easily

**Acceptance Criteria:**
- [ ] POST /api/v1/recipe/import endpoint
- [ ] Requires premium subscription
- [ ] Returns parsed recipe with ingredients
- [ ] Includes already-have information
- [ ] Rate limited (10/hour per user)
- [ ] Handles all error cases

**Story Points:** 5
**Assignee:** Backend Developer 1

**API Specification:**
```
POST /api/v1/recipe/import
Authorization: Bearer <access_token>
X-Premium-Token: <subscription_token>
Content-Type: application/json

Request:
{
    "url": "https://www.allrecipes.com/recipe/123/amazing-tacos"
}

Response (200 OK):
{
    "success": true,
    "recipe": {
        "name": "Amazing Tacos",
        "source_url": "https://www.allrecipes.com/recipe/123/amazing-tacos",
        "servings": 6,
        "image_url": "https://...",
        "ingredients": [
            {
                "raw_text": "1 pound ground beef",
                "normalized_name": "Ground Beef",
                "quantity": 1,
                "unit": "lb",
                "already_have": false
            },
            {
                "raw_text": "1 packet taco seasoning mix",
                "normalized_name": "Taco Seasoning",
                "quantity": 1,
                "unit": "packet",
                "already_have": false
            },
            {
                "raw_text": "12 taco shells",
                "normalized_name": "Taco Shells",
                "quantity": 12,
                "unit": null,
                "already_have": true,
                "match_source": "recent_purchase"
            }
        ],
        "missing_count": 2,
        "already_have_count": 1
    }
}

Response (400 Bad Request):
{
    "error": "unsupported_site",
    "message": "We don't support recipes from example.com yet",
    "supported_sites": ["allrecipes.com", "foodnetwork.com", ...]
}

Response (422 Unprocessable Entity):
{
    "error": "parse_failed",
    "message": "Couldn't extract ingredients from this recipe"
}
```

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-11.1 | Create RecipeUrlFetcher service | Backend Dev 1 | 6 | - |
| T-11.2 | Implement supported site validation | Backend Dev 1 | 2 | T-11.1 |
| T-11.3 | Create RecipeSchemaParser | Backend Dev 1 | 10 | - |
| T-11.4 | Test schema parser with real sites | Backend Dev 1 | 6 | T-11.3 |
| T-11.5 | Create RecipeHtmlParser | Backend Dev 2 | 10 | - |
| T-11.6 | Test HTML parser with real sites | Backend Dev 2 | 6 | T-11.5 |
| T-11.7 | Create IngredientNormalizer | Backend Dev 2 | 10 | - |
| T-11.8 | Test normalizer with diverse inputs | Backend Dev 2 | 4 | T-11.7 |
| T-11.9 | Implement AlreadyHaveDetector | Tech Lead | 8 | - |
| T-11.10 | Implement fuzzy matching | Tech Lead | 4 | T-11.9 |
| T-11.11 | Create /recipe/import endpoint | Backend Dev 1 | 6 | T-11.1-T-11.9 |
| T-11.12 | Add recipe rate limiting | Backend Dev 1 | 2 | T-11.11 |
| T-11.13 | Design recipe import UI | UI/UX | 8 | - |
| T-11.14 | Design ingredient preview UI | UI/UX | 6 | - |
| T-11.15 | Write schema parser tests | Backend Dev 1 | 4 | T-11.3 |
| T-11.16 | Write HTML parser tests | Backend Dev 2 | 4 | T-11.5 |
| T-11.17 | Write normalizer tests | Backend Dev 2 | 4 | T-11.7 |
| T-11.18 | Write integration tests | Backend Dev 1 | 6 | All |
| T-11.19 | Set up recipe service monitoring | DevOps | 4 | T-11.11 |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Already-have detection
- Implement AlreadyHaveDetector
- Implement fuzzy matching algorithm
- Code review parsing logic
- Integration guidance

### UI/UX Engineer
**Focus:** Recipe UI design
- Design recipe import flow UI
- Design ingredient preview interface
- Design already-have badges
- Prepare for Sprint 12 implementation

### Backend Developer 1
**Focus:** URL fetching and schema parsing
- Create RecipeUrlFetcher
- Implement RecipeSchemaParser
- Create /recipe/import endpoint
- Write parser tests

### Backend Developer 2
**Focus:** HTML parsing and normalization
- Create RecipeHtmlParser
- Implement IngredientNormalizer
- Test with diverse inputs
- Write comprehensive tests

### DevOps Engineer
**Focus:** Service infrastructure
- Set up recipe service monitoring
- Rate limiting infrastructure
- External request logging

---

## 4. Definition of Done

### Sprint Level
- [ ] Recipe URLs from 10 supported sites parse correctly
- [ ] Schema parsing works for standard schema.org recipes
- [ ] HTML fallback works for common patterns
- [ ] Ingredient normalization accurate >90%
- [ ] Already-have detection working
- [ ] API endpoint functional and documented
- [ ] 85% test coverage for parsing code

### Story Level
- [ ] Acceptance criteria met
- [ ] Tested with real recipe URLs
- [ ] Edge cases handled
- [ ] Code reviewed

---

## 5. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Recipe parse success rate | >85% | API metrics |
| Schema detection rate | >70% | Logging |
| Ingredient extraction accuracy | >90% | Manual review |
| API response time | <5 seconds | Monitoring |
| Already-have accuracy | >80% | User feedback |

---

## 6. Handoff to Sprint 12

### Completed Items for Next Sprint
- Recipe parsing service operational
- Ingredient normalization working
- Already-have detection ready
- API endpoint functional

### Prepared Items for Next Sprint
- Ready to build recipe import UI
- Ready to implement recipe storage
- Ready for one-tap recipe shopping

---

*Sprint 11 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
