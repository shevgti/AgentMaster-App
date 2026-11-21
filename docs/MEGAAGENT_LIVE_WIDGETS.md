# Мегаагент — Live Widgets architecture and delivery plan

## Project identity

This repository, `shevgti/AgentMaster-App`, is the new Android project for **Мегаагент** (AI Agent Orchestrator). It is the target project for all new Мегаагент development.

Do not implement new Мегаагент features in `SheV-Agent-Public` or its Chef Agent branches. That repository is a separate legacy/source project. The Live Widgets implementation there is a reference for behavior and lessons learned only.

## Existing application baseline

The current app is a Kotlin Android application using Jetpack Compose and Material 3. Its main flow is:
- `MainActivity`: owns task execution and settings navigation.
- `ChatScreen`: task/chat UI and prompt composer.
- `Orchestrator`: chooses specialist agents for a task and assembles their output.
- `AgentRepository`: calls the configured OpenAI-compatible API.
- `AgentCard`: displays individual specialist-agent progress/results.

The existing project is intentionally small. Integrate widgets into this architecture without replacing the orchestrator, provider settings, chat history/task display, or existing agent execution.

## Goal

Build Live Widgets as a native feature of Мегаагент: movable, resizable, persistent interactive cards that can be controlled from the same Russian-language chat/voice command pathway. The first widget types are clock, weather and battery. The design must support future calendar, memory, task, notification and agent-status widgets.

## Reference implementation lessons from SheV-Agent-Public

The older Chef Agent project contains an experimental Compose Live Widgets implementation. Reuse its product behavior as a reference, not its app wiring or package names. The audit identified several shortcomings that must not be repeated:
- hiding must not delete widget configuration;
- widget identity must be stable and multiple instances must be supported;
- dragging must promote a widget above others;
- directional movement must be relative to the current position;
- resize and visibility must be persistent;
- persistence must be versioned and migrated;
- RU/EN aliases must resolve through one extensible registry;
- chat viewport, composer, keyboard, system insets and rotation must be respected.

## Target architecture

Keep each responsibility separate:
- `LiveWidget`: stable ID, type, normalized position, scale, visibility, z-order and type-specific configuration.
- `LiveWidgetLayout`: versioned collection of widget instances.
- `LiveWidgetRegistry`: supported types, localized names/aliases and defaults.
- `LiveWidgetCommandParser`: converts Russian/English user requests into typed commands.
- `LiveWidgetManager`: applies validated state transitions.
- `LiveWidgetRepository`: persistence, schema versioning and migrations.
- `LiveWidgetsOverlay`: Compose rendering, drag/resize, spring animation and viewport constraints.
- `WidgetDataSources`: lifecycle-safe clock, Android battery state and a real weather provider abstraction.

Do not request Android draw-over-other-apps permission for the initial implementation. The first release should render widgets inside the Мегаагент chat viewport. A system-wide overlay is a separate feature requiring explicit product approval and permission UX.

## Required commands

Russian and English:
- show / показать / верни [виджет]
- hide / спрячь / убери [виджет]
- hide all / спрячь все блоки
- move [widget] left/right/up/down / перемести [виджет] влево/вправо/вверх/вниз
- place [widget] top-left/top-right/bottom-left/bottom-right
- resize bigger/smaller / сделай крупнее/мельче
- remove [widget] / удали блок (permanent removal is distinct from hide)

If multiple instances match and a command does not identify one, ask the user which instance they mean. Normal questions such as “какая погода?” must not be mistaken for widget visibility commands.

## State and UX requirements

- HIDE sets `visible=false`; SHOW restores the same ID, position, scale and config.
- HIDE ALL hides every instance without deleting it.
- Support multiple instances of a type, each with a stable ID.
- Promote dragged items to the front using z-order.
- Directional movement applies a bounded delta to current coordinates.
- Use normalized coordinates so layouts survive screen-size changes.
- Clamp the full widget bounds to the chat viewport, not just the anchor point.
- Respect WindowInsets, composer, IME and portrait/landscape changes.
- Use Compose spring animation for drag/release and visibility; snap gently only after release.
- Provide accessible touch targets and a compact/expanded presentation.
- Clock updates on lifecycle-aware cadence.
- Battery shows percentage and charging/discharging/full state, plus a low-battery warning.
- Weather must show a clear unavailable state until a real provider and location/permission flow are configured; never fabricate weather.

## Persistence and migration

Persist a versioned envelope (starting at schema v1) with stable IDs. On malformed data, recover safely and avoid silently discarding user layouts. All state transitions must be testable as pure functions where practical.

## Delivery stages

1. Add domain model, registry, typed commands and versioned repository.
2. Integrate an overlay into ChatScreen and preserve existing task orchestration.
3. Add clock and battery data sources; add weather provider interface and honest unconfigured state.
4. Add Russian/English command handling to the existing chat submission path. Voice transcription, when added, must call the same pathway.
5. Add multiple instances, visibility restore, z-order, relative move and resize.
6. Add spring motion, viewport/insets/IME/rotation handling.
7. Add unit and Compose UI tests; run Android CI and inspect the debug APK artifact.

## Acceptance tests

- Show/hide/show preserves widget ID, position, scale and config.
- Hide-all hides without deleting; show restores.
- Multiple clock/battery/weather instances can coexist.
- Ambiguous multi-instance commands request clarification.
- Relative directional movement preserves the expected delta and stays in bounds.
- Resize respects min/max and persists across restart.
- Z-order promotes the dragged widget.
- Legacy/unversioned state migrates without data loss.
- Corrupt state recovers safely.
- RU/EN aliases are tested.
- Keyboard, composer, insets, small screens and rotation do not clip or hide controls.
- Battery charging/discharging/full/low states are covered.
- Existing task orchestration, API settings and chat task cards remain functional.

## Safety and release policy

Work on the dedicated branch `feature/megaagent-live-widgets`. Do not change the default `main` branch or publish a release APK until implementation and CI pass. Keep commits small and reviewable. Never claim a physical-device test unless it was actually performed.
