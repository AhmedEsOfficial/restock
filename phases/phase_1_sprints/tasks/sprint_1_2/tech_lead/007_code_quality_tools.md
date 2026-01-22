# Task: Configure Code Quality Tools (Detekt, Ktlint)

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 3 hours (T-1.7, T-1.8, T-1.9 combined)
**Priority:** P1
**Prerequisites:** 001_project_structure_setup.md, 002_gradle_version_catalog.md

---

## Sprint Context

Code quality tools ensure consistent code style across the team and catch potential bugs early. Detekt provides static analysis for common issues, while Ktlint enforces formatting consistency.

---

## Task Description

Configure Detekt for static analysis and Ktlint for code formatting across all modules.

## Detekt Configuration

### Root build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
    }

    dependencies {
        "detektPlugins"(libs.detekt.formatting)
    }
}
```

### config/detekt/detekt.yml

```yaml
config:
  validation: true
  warningsAsErrors: false

build:
  maxIssues: 0
  weights:
    complexity: 2
    formatting: 1
    LongParameterList: 1
    style: 1
    comments: 0

processors:
  active: true

console-reports:
  active: true

output-reports:
  active: true

comments:
  CommentOverPrivateFunction:
    active: false
  CommentOverPrivateProperty:
    active: false
  UndocumentedPublicClass:
    active: false
  UndocumentedPublicFunction:
    active: false
  UndocumentedPublicProperty:
    active: false

complexity:
  ComplexCondition:
    active: true
    threshold: 4
  ComplexMethod:
    active: true
    threshold: 15
  LargeClass:
    active: true
    threshold: 600
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    functionThreshold: 6
    constructorThreshold: 7
    ignoreDefaultParameters: true
    ignoreDataClasses: true
    ignoreAnnotated: ['Composable']
  NestedBlockDepth:
    active: true
    threshold: 4
  TooManyFunctions:
    active: true
    thresholdInFiles: 25
    thresholdInClasses: 20
    thresholdInInterfaces: 15
    thresholdInObjects: 15
    thresholdInEnums: 10
    ignorePrivate: true

coroutines:
  GlobalCoroutineUsage:
    active: true
  RedundantSuspendModifier:
    active: true
  SuspendFunWithFlowReturnType:
    active: true

empty-blocks:
  EmptyCatchBlock:
    active: true
    allowedExceptionNameRegex: '_|(ignore|expected).*'
  EmptyClassBlock:
    active: true
  EmptyDefaultConstructor:
    active: true
  EmptyDoWhileBlock:
    active: true
  EmptyElseBlock:
    active: true
  EmptyFinallyBlock:
    active: true
  EmptyForBlock:
    active: true
  EmptyFunctionBlock:
    active: true
    ignoreOverridden: true
  EmptyIfBlock:
    active: true
  EmptyInitBlock:
    active: true
  EmptyKtFile:
    active: true
  EmptySecondaryConstructor:
    active: true
  EmptyTryBlock:
    active: true
  EmptyWhenBlock:
    active: true
  EmptyWhileBlock:
    active: true

exceptions:
  ExceptionRaisedInUnexpectedLocation:
    active: true
  InstanceOfCheckForException:
    active: true
  NotImplementedDeclaration:
    active: true
  PrintStackTrace:
    active: true
  RethrowCaughtException:
    active: true
  ReturnFromFinally:
    active: true
  SwallowedException:
    active: true
    ignoredExceptionTypes:
      - InterruptedException
      - MalformedURLException
      - NumberFormatException
      - ParseException
  ThrowingExceptionFromFinally:
    active: true
  ThrowingExceptionsWithoutMessageOrCause:
    active: true
    excludes: ['**/test/**', '**/androidTest/**']
  TooGenericExceptionCaught:
    active: true
    excludes: ['**/test/**', '**/androidTest/**']
    exceptionNames:
      - ArrayIndexOutOfBoundsException
      - Error
      - Exception
      - IllegalMonitorStateException
      - IndexOutOfBoundsException
      - NullPointerException
      - RuntimeException
      - Throwable

naming:
  ClassNaming:
    active: true
    classPattern: '[A-Z][a-zA-Z0-9]*'
  ConstructorParameterNaming:
    active: true
    parameterPattern: '[a-z][A-Za-z0-9]*'
  EnumNaming:
    active: true
    enumEntryPattern: '[A-Z][_a-zA-Z0-9]*'
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'
    ignoreAnnotated: ['Composable']
  FunctionParameterNaming:
    active: true
    parameterPattern: '[a-z][A-Za-z0-9]*'
  MatchingDeclarationName:
    active: true
  MemberNameEqualsClassName:
    active: true
  ObjectPropertyNaming:
    active: true
    constantPattern: '[A-Za-z][_A-Za-z0-9]*'
    propertyPattern: '[A-Za-z][_A-Za-z0-9]*'
  PackageNaming:
    active: true
    packagePattern: '[a-z]+(\.[a-z][A-Za-z0-9]*)*'
  TopLevelPropertyNaming:
    active: true
    constantPattern: '[A-Z][_A-Z0-9]*'
    propertyPattern: '[A-Za-z][_A-Za-z0-9]*'
  VariableNaming:
    active: true
    variablePattern: '[a-z][A-Za-z0-9]*'

performance:
  ArrayPrimitive:
    active: true
  ForEachOnRange:
    active: true
  SpreadOperator:
    active: true
  UnnecessaryTemporaryInstantiation:
    active: true

potential-bugs:
  CastToNullableType:
    active: true
  Deprecation:
    active: true
  DontDowncastCollectionTypes:
    active: true
  DoubleMutabilityForCollection:
    active: true
  EqualsAlwaysReturnsTrueOrFalse:
    active: true
  EqualsWithHashCodeExist:
    active: true
  ExitOutsideMain:
    active: true
  HasPlatformType:
    active: true
  IgnoredReturnValue:
    active: true
  ImplicitDefaultLocale:
    active: true
  ImplicitUnitReturnType:
    active: true
  InvalidRange:
    active: true
  IteratorHasNextCallsNextMethod:
    active: true
  IteratorNotThrowingNoSuchElementException:
    active: true
  LateinitUsage:
    active: false
  MapGetWithNotNullAssertionOperator:
    active: true
  NullableToStringCall:
    active: true
  UnconditionalJumpStatementInLoop:
    active: true
  UnnecessaryNotNullOperator:
    active: true
  UnnecessarySafeCall:
    active: true
  UnreachableCode:
    active: true
  UnsafeCallOnNullableType:
    active: true
  UnsafeCast:
    active: true
  UselessPostfixExpression:
    active: true
  WrongEqualsTypeParameter:
    active: true

style:
  ClassOrdering:
    active: true
  CollapsibleIfStatements:
    active: true
  DataClassContainsFunctions:
    active: false
  DataClassShouldBeImmutable:
    active: true
  EqualsNullCall:
    active: true
  EqualsOnSignatureLine:
    active: true
  ExplicitCollectionElementAccessMethod:
    active: true
  ExplicitItLambdaParameter:
    active: true
  ExpressionBodySyntax:
    active: true
    includeLineWrapping: false
  ForbiddenComment:
    active: true
    values:
      - 'TODO:'
      - 'FIXME:'
      - 'STOPSHIP:'
    allowedPatterns: ''
  ForbiddenMethodCall:
    active: true
    methods:
      - 'kotlin.io.println'
      - 'kotlin.io.print'
  ForbiddenVoid:
    active: true
  FunctionOnlyReturningConstant:
    active: true
  LoopWithTooManyJumpStatements:
    active: true
    maxJumpCount: 2
  MagicNumber:
    active: true
    excludes: ['**/test/**', '**/androidTest/**']
    ignoreNumbers: ['-1', '0', '1', '2']
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: true
    ignoreLocalVariableDeclaration: true
    ignoreConstantDeclaration: true
    ignoreCompanionObjectPropertyDeclaration: true
    ignoreAnnotation: true
    ignoreNamedArgument: true
    ignoreEnums: true
    ignoreRanges: true
  MandatoryBracesIfStatements:
    active: true
  MaxLineLength:
    active: true
    maxLineLength: 120
    excludePackageStatements: true
    excludeImportStatements: true
    excludeCommentStatements: true
  MayBeConst:
    active: true
  ModifierOrder:
    active: true
  NestedClassesVisibility:
    active: true
  NewLineAtEndOfFile:
    active: true
  NoTabs:
    active: true
  OptionalAbstractKeyword:
    active: true
  OptionalUnit:
    active: true
  PreferToOverPairSyntax:
    active: true
  ProtectedMemberInFinalClass:
    active: true
  RedundantExplicitType:
    active: true
  RedundantHigherOrderMapUsage:
    active: true
  RedundantVisibilityModifierRule:
    active: true
  ReturnCount:
    active: true
    max: 4
    excludedFunctions:
      - 'equals'
    excludeLabeled: true
    excludeReturnFromLambda: true
    excludeGuardClauses: true
  SafeCast:
    active: true
  SerialVersionUIDInSerializableClass:
    active: true
  SpacingBetweenPackageAndImports:
    active: true
  ThrowsCount:
    active: true
    max: 3
  TrailingWhitespace:
    active: true
  UnderscoresInNumericLiterals:
    active: true
    acceptableLength: 5
  UnnecessaryAbstractClass:
    active: true
  UnnecessaryApply:
    active: true
  UnnecessaryInheritance:
    active: true
  UnnecessaryLet:
    active: true
  UnnecessaryParentheses:
    active: true
  UntilInsteadOfRangeTo:
    active: true
  UnusedImports:
    active: true
  UnusedPrivateClass:
    active: true
  UnusedPrivateMember:
    active: true
    allowedNames: '(_|ignored|expected|serialVersionUID)'
  UseArrayLiteralsInAnnotations:
    active: true
  UseCheckNotNull:
    active: true
  UseCheckOrError:
    active: true
  UseDataClass:
    active: true
  UseEmptyCounterpart:
    active: true
  UseIfEmptyOrIfBlank:
    active: true
  UseIfInsteadOfWhen:
    active: true
  UseIsNullOrEmpty:
    active: true
  UseOrEmpty:
    active: true
  UseRequire:
    active: true
  UseRequireNotNull:
    active: true
  UselessCallOnNotNull:
    active: true
  VarCouldBeVal:
    active: true
  WildcardImport:
    active: true
    excludeImports:
      - 'java.util.*'
      - 'kotlinx.android.synthetic.*'
```

## Ktlint Configuration

### Root build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}
```

### .editorconfig

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
max_line_length = 120
tab_width = 4
trim_trailing_whitespace = true

[*.{kt,kts}]
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true

[*.md]
trim_trailing_whitespace = false

[*.{yml,yaml}]
indent_size = 2
```

## Gradle Tasks

After configuration, these tasks are available:

```bash
# Run all code quality checks
./gradlew check

# Detekt only
./gradlew detekt

# Ktlint check only
./gradlew ktlintCheck

# Ktlint auto-format
./gradlew ktlintFormat
```

## Acceptance Criteria

- [ ] Detekt configured with custom rules
- [ ] Ktlint configured for formatting
- [ ] .editorconfig created
- [ ] All modules pass quality checks
- [ ] Pre-commit hook optional setup documented
- [ ] CI integration ready

## Definition of Done

- [ ] detekt.yml config file created
- [ ] Ktlint plugin applied to all modules
- [ ] .editorconfig committed
- [ ] `./gradlew check` passes
- [ ] Team briefed on usage

## Notes

- Run `./gradlew ktlintFormat` to auto-fix formatting issues
- Detekt can be customized per-module if needed
- Consider adding pre-commit hooks for local enforcement
- Composable functions are excluded from naming rules
