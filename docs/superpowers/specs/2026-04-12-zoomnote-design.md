# ZoomNote — Fractal Zoom Note-Taking App

## Overview

A native Android note-taking app with a fractal zoom canvas. Users write handwritten notes and simple drawings with a stylus, then zoom into any area to add nested detail at deeper scales. Local-first persistence with a data model designed for future cloud sync.

**Platform:** Native Android (Kotlin)
**Target:** Any Android device with stylus support (Samsung S Pen, etc.)
**Min SDK:** 26 (Android 8+)

## Data Model

### Stroke

Each stroke is a list of points captured from stylus input.

- `id: UUID` — unique identifier (sync-ready)
- `canvasId: UUID` — which canvas this stroke belongs to
- `zoomLevel: Double` — the zoom level at which the stroke was drawn
- `color: Int` — ARGB color value
- `thickness: Float` — base thickness (modified by pressure at render time)
- `points: ByteArray` — compressed point data (delta-encoded)
- `createdAt: Long` — epoch millis
- `modifiedAt: Long` — epoch millis

Each point contains: `x: Double, y: Double, pressure: Float, timestamp: Long`

### Canvas

- `id: UUID`
- `name: String`
- `createdAt: Long`
- `modifiedAt: Long`

### Coordinate System

- Single continuous coordinate space using `Double` precision (64-bit)
- 15 significant digits of precision supports 10^6+ zoom range
- No tile or page system — one infinite coordinate space per canvas

### Persistence

- SQLite via Room
- Each stroke is one row; points stored as compressed byte array
- UUIDs and timestamps on all entities for future sync compatibility

## Rendering Architecture

### Spatial Index

- R-tree indexes each stroke's bounding box
- Viewport changes trigger R-tree query for visible strokes: O(log n)
- Strokes outside the viewport are not rendered

### Zoom-Level Filtering

- Strokes drawn at a much deeper zoom level than the current view are too small to see — skip them
- Strokes drawn at a much shallower zoom level are too coarse — fade them out
- This keeps the visible stroke count manageable at any zoom depth

### Rendering Pipeline

1. User pans/zooms — compute new viewport in world coordinates
2. Query R-tree for strokes intersecting viewport within a zoom-level band
3. Transform stroke points through the view matrix (translate + scale)
4. Draw to hardware-accelerated `Canvas` via `SurfaceView`

### Active Stroke Rendering

- In-progress stroke (stylus down) renders on a transparent overlay
- No spatial index lookup during active drawing — immediate draw for minimal latency
- On stylus up: completed stroke added to spatial index and main canvas

### Performance Target

60fps during pan/zoom with thousands of visible strokes.

## Input & Gesture Handling

### Input Separation

- **Stylus** (`TOOL_TYPE_STYLUS`) — always draws
- **Single finger** — pans the canvas
- **Pinch (two fingers)** — zooms in/out, centered on finger midpoint
- **Palm** — rejected via Android built-in palm rejection + large contact area heuristic

### Zoom Behavior

- Continuous zoom (no snapping to discrete levels)
- Subtle visual cue at depth thresholds (grid density or background shade shift) to indicate you're at a new depth
- Zoom level at stroke creation captured automatically — user never manually selects a "level"

### Undo/Redo

- Stroke-level undo stack
- Two-finger tap = undo, three-finger tap = redo

## UI & Tools

### Toolbar (floating, collapsible)

- Pen (default) — variable width from pressure
- Eraser — stroke-level (tap to delete whole stroke)
- Color picker — 6-8 presets + one custom slot
- Thickness — 3 presets (fine, medium, bold)
- Undo / Redo buttons

### Navigation

- **Minimap** — small corner overlay showing viewport position relative to content. Tap to jump.
- **Bookmarks** — long-press to save position + zoom level. Swipe from edge to access list.
- **Home** — double-tap minimap to zoom out to see all content

### Canvas Management

- App opens to a canvas list (name, thumbnail, last modified)
- Create new / rename / delete
- No folders or tags in v1

## Project Structure

```
app/src/main/java/com/bounty/zoomnote/
├── data/           # Room DB, entities, DAO, repository
├── rendering/      # SurfaceView, stroke renderer, view matrix
├── spatial/        # R-tree implementation, zoom-level filtering
├── input/          # Gesture detector, stylus handler, palm rejection
├── ui/             # Activities, toolbar, minimap, bookmark list
└── model/          # Stroke, Canvas, Point data classes
```

## Tech Stack

- **Language:** Kotlin (100%)
- **Room** — SQLite persistence
- **Kotlin Coroutines** — async stroke saving, spatial index queries off main thread
- **AndroidX Core** — lifecycle, ViewModel
- **No third-party rendering libraries** — custom `SurfaceView` + `Canvas` API

**Build:** Gradle with Kotlin DSL. Min SDK 26, Target SDK 35.

## Testing Strategy

- Unit tests: spatial index queries, zoom-level filtering, coordinate transforms
- Instrumented tests: stroke persistence (Room)
- Manual testing: stylus feel, gesture handling, zoom smoothness

## Out of Scope for v1

- Cloud sync
- iOS / web
- Typed text input
- Image import
- Collaboration
- Folders / tags / organization beyond canvas list
