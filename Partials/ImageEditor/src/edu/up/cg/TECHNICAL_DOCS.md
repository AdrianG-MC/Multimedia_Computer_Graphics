# Technical Documentation — Java Console Image Editor

**Version:** 1.1  
**Language:** Java 8+  
**Dependencies:** Java Standard Library only

---

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Package Structure](#2-package-structure)
3. [Class Reference](#3-class-reference)
4. [Data Flow Diagrams](#4-data-flow-diagrams)
5. [Algorithms](#5-algorithms)
6. [Known Limitations](#6-known-limitations)

---

## 1. Architecture Overview

The application is divided into four clearly separated layers:

```
  [ UI Layer ]
  ConsoleMenuHandler · OperationHandler · InputHandler · MenuPrinter
          │
          ▼
  [ Service Layer ]
  ImageEditorHandler
          │
     ┌────┴────┐
     ▼         ▼
[ Data ]   [ Operations ]
ImageTemplate  CropOperation · InvertOperation · RotateOperation

  [ Infrastructure ]
  ImageFileHandler  (reads and writes files)
```
---

## 2. Package Structure

| Package | Purpose |
|---------|---------|
| `edu.up.cg` | Application entry point (`Main.java`) |
| `edu.up.cg.handlers` | Application-level controllers (menu, file, input, editor service) |
| `edu.up.cg.templates` | Data model — wraps `BufferedImage` |
| `edu.up.cg.tools` | Pixel-level operation implementations |
| `edu.up.cg.utils` | Stateless helpers (menu printing) |

---

## 3. Class Reference

### `Main.java`
Entry point. Creates `ConsoleMenuHandler` and calls `start()`.

| Method | Description |
|--------|-------------|
| `main(String[] args)` | JVM entry point. Instantiates and starts `ConsoleMenuHandler`. |

---

### `handlers/ConsoleMenuHandler.java`
Top-level application controller. Manages the full session lifecycle.

| Method | Description |
|--------|-------------|
| `start()` | Entry method. Calls `loadImage()`, `initializeOperationHandler()`, `runMenuLoop()`. |
| `loadImage()` | Lists Input/ files, prompts for selection, loads `BufferedImage`, creates `ImageEditorHandler`. |
| `initializeOperationHandler()` | Creates `OperationHandler` after image is loaded. |
| `runMenuLoop()` | Infinite loop: print menu → read choice → dispatch → repeat until Exit. |
| `handleSave()` | Calls `ImageFileHandler.save()` with the current image. |

---

### `handlers/ImageEditorHandler.java`
Service layer. Translates high-level editing commands into operation object calls and manages the image state wrapper.

| Method | Description |
|--------|-------------|
| `getCurrentImage()` | Returns the current `BufferedImage` for saving. |
| `crop(x, y, width, height)` | Creates `CropOperation`, calls `.crop()`, stores the new smaller image via `template.setImage()`. |
| `invertFull()` | Creates `InvertOperation()` (full-image mode), calls `.invertImage()`. |
| `invertRegion(x, y, width, height)` | Creates `InvertOperation(...)` (region mode), calls `.invertImage()`. |
| `rotate(x, y, width, height, angle)` | Creates `RotateOperation(...)`, calls `.rotateImage()`. |

---

### `handlers/ImageFileHandler.java`
All file-system I/O. Finds input images, loads them, saves results.

| Method | Description |
|--------|-------------|
| `listInputImages()` | Scans Input/ for PNG/JPG/JPEG/BMP files. Throws `RuntimeException` if folder missing or empty. |
| `load(File file)` | Decodes image with `ImageIO.read()`. Throws `IOException` if result is `null`. |
| `save(BufferedImage, String originalName)` | Derives output name, creates directory if needed, writes with `ImageIO.write()`. |

Save file naming logic:
```
"photo.png"  →  name="photo", ext="png"  →  "photo_edited.png"
```

---

### `handlers/InputHandler.java`
Safe console input reading with retry on invalid input.

| Method | Description |
|--------|-------------|
| `readInt(String message)` | Prompts, reads a line, parses integer. Retries indefinitely on `NumberFormatException`. |
| `readString(String message)` | Prompts and reads a raw line. No retry. |
| `readCoordinates()` | Reads x1, y1, x2, y2 in sequence. Returns `int[4]` = `{x1, y1, x2, y2}`. |

---

### `handlers/OperationHandler.java`
Reads operation-specific inputs, validates angle, delegates to `ImageEditorHandler`.

| Method | Description |
|--------|-------------|
| `handleCrop()` | Reads coordinates, calls `editorHandler.crop()`. |
| `handleInvert()` | Shows invert sub-menu, reads option (1=full, 2=region), calls appropriate editor method. |
| `handleRotate()` | Reads coordinates + angle, validates angle ∈ {90, 180, 270}, calls `editorHandler.rotate()`. |

---

### `templates/ImageTemplate.java`
Mutable container for the current `BufferedImage`.

Operations like crop return a **new** `BufferedImage` object. Without this wrapper, classes holding a direct `BufferedImage` reference would still point to the old (full-size) image after a crop. By routing all access through `ImageTemplate`, a single `setImage()` call updates the state visible to all classes.

| Method | Description |
|--------|-------------|
| `getImage()` | Returns the current image. |
| `setImage(BufferedImage)` | Replaces the stored image (used by crop). |

---

### `tools/CoordsValidator.java`
Validates (x1, y1) → (x2, y2) coordinates against a `BufferedImage`. All methods are static.

| Rule | Check |
|------|-------|
| 1 | All four values ≥ 0 |
| 2 | All four values ≤ image width/height |
| 3 | x1 < x2 AND y1 < y2 |

---

### `tools/RegionExtractor.java`
Stores a rectangle and cuts it from a `BufferedImage` via `getSubimage()`.

> `BufferedImage.getSubimage()` returns a **shared view** — writing to the sub-image writes to the parent. The sub-image uses local coordinates starting at (0,0).

| Method | Description |
|--------|-------------|
| `getTopLeftX()` | X offset of the region in the parent image. |
| `getTopLeftY()` | Y offset of the region in the parent image. |
| `extract(BufferedImage)` | Returns a shared sub-image view for the stored rectangle. |

---

### `tools/CropOperation.java`
Implements crop by extracting a sub-region and returning it as the new image.

Validates coordinates on construction, then `crop(BufferedImage)` calls `extractor.extract()` and returns the result. The caller (`ImageEditorHandler`) must store this via `template.setImage()`.

---

### `tools/InvertOperation.java`
Inverts RGB channels of all pixels in either the full image or a region.

| Constructor | Mode |
|-------------|------|
| `InvertOperation()` | Full-image mode. |
| `InvertOperation(x1, y1, x2, y2, image)` | Region mode. Validates coords. |

`invertImage(BufferedImage)`: in full mode inverts all pixels directly; in region mode extracts the sub-image (shared view) and inverts its pixels — changes propagate automatically to the parent.

---

### `tools/RotateOperation.java`
Rotates a rectangular region by 90°, 180°, or 270° clockwise and writes results back into the parent image.

`rotateImage(BufferedImage)`: extracts the region, allocates a rotated canvas (swaps W×H for 90°/270°), maps each pixel to its new position, then copies back into the parent starting at `(startX, startY)`. Pixels outside image bounds are clipped.

---

### `utils/MenuPrinter.java`
Prints formatted console menus. Pure output, no logic.

| Method | Description |
|--------|-------------|
| `printMainMenu()` | Prints options 1–5 and prompts for input. |
| `printInvertMenu()` | Prints invert sub-options (Full / Region). |

---

## 4. Data Flow Diagrams

### Startup Flow
```
main()
  └─ new ConsoleMenuHandler()
       └─ start()
            ├─ loadImage()
            │    ├─ fileHandler.listInputImages()
            │    ├─ inputHandler.readInt()  [user selects]
            │    ├─ fileHandler.load(file)  → BufferedImage
            │    └─ new ImageEditorHandler(new ImageTemplate(image))
            ├─ initializeOperationHandler()
            └─ runMenuLoop()
```

### Crop Flow
```
User: "1"
  └─ OperationHandler.handleCrop()
       ├─ inputHandler.readCoordinates() → {x1,y1,x2,y2}
       └─ editorHandler.crop(x1,y1,x2,y2)
            ├─ new CropOperation(x1,y1,x2,y2, image)
            │    └─ CoordsValidator.validate()
            ├─ cropOp.crop(image)
            │    └─ extractor.extract(image)
            │         └─ image.getSubimage(...)  → new smaller BufferedImage
            └─ template.setImage(croppedImage)
```

### Invert Region Flow
```
User: "2" → "2"
  └─ OperationHandler.handleInvert()
       ├─ inputHandler.readCoordinates() → {x1,y1,x2,y2}
       └─ editorHandler.invertRegion(x1,y1,x2,y2)
            ├─ new InvertOperation(x1,y1,x2,y2, image)
            │    └─ CoordsValidator.validate()
            └─ invertOp.invertImage(image)
                 ├─ extractor.extract(image) → region (shared view)
                 └─ invertPixels(region, 0, 0, w, h)
                      └─ for each pixel: read ARGB → invert RGB → write back
```

### Save Flow
```
User: "4"
  └─ ConsoleMenuHandler.handleSave()
       └─ fileHandler.save(editorHandler.getCurrentImage(), currentFileName)
            ├─ create Images/Edited/ if not exists
            ├─ compute outputName = "name_edited.ext"
            └─ ImageIO.write(image, ext, outputFile)
```

---

## 5. Algorithms

### Color Inversion

Each pixel's RGB channels are flipped using `new = 255 − old`, producing a photographic negative. The Alpha channel is preserved.

Reference: [Image Processing in Java — Colored Image to Negative Image Conversion](https://www.geeksforgeeks.org/java/image-processing-in-java-colored-image-to-negative-image-conversion/)

For a pixel with 32-bit ARGB value `rgb`:

```java
int a = (rgb >> 24) & 0xFF;   // extract alpha (bits 31-24) — preserved
int r = (rgb >> 16) & 0xFF;   // extract red   (bits 23-16)
int g = (rgb >>  8) & 0xFF;   // extract green (bits 15-8)
int b =  rgb        & 0xFF;   // extract blue  (bits  7-0)

int result = (a << 24) | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
```

The formula `255 - channel` maps the full luminance range:
- `0` (darkest) → `255` (brightest)
- `255` (brightest) → `0` (darkest)

---

### Pixel Rotation

Given a source region of width `W` and height `H`, each pixel `(x, y)` is remapped to a new position in a freshly allocated canvas:

| Angle | Destination | New canvas size |
|-------|-------------|-----------------|
| 90° CW | `(H-1-y, x)` | `H × W` |
| 180° CW | `(W-1-x, H-1-y)` | `W × H` |
| 270° CW | `(y, W-1-x)` | `H × W` |

For 90° and 270° the canvas dimensions are swapped because a portrait rectangle becomes landscape after a quarter-turn. After building the rotated canvas, it is pasted back into the parent image at `(startX, startY)`. Pixels that exceed image bounds are clipped.

---

### Crop

Crop uses Java's `BufferedImage.getSubimage(x, y, width, height)`:
- Returns a **view** of the parent data — no pixel copy is made during extraction.
- `ImageEditorHandler` calls `template.setImage()` with the result, making the cropped image the new working canvas with permanently reduced dimensions.

---

## 6. Known Limitations

| Limitation | Notes |
|------------|-------|
| No undo/redo | Operations are applied directly to the in-memory image. |
| Rotate clips near edges | For 90°/270°, the rotated canvas may not fit back in the same area — pixels outside image bounds are silently dropped. |
| Single image per session | One image loaded and operated on per run. |
| No format conversion | Output extension always matches input format. |

---

AI was used for formatting some documents or comments included into the project.
Those were pre-wrote and passed to an AI to make it look better.
