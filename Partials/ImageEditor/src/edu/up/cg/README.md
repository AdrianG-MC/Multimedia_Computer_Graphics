# Partial 1 - Image Editor

## Overview
This project is a console-based image editor written in Java that supports **crop**, **color invert**, and **region rotation** operations on PNG, JPG, and BMP files.
Was created for Multimedia & Computer Graphics course at Universidad Panamericana for deliverying 27/02/2026 as a Partial 1 Calification.

---

## Project Structure

```
ImageEditor/
├── src/
│   └── edu/up/cg/
│       ├── Main.java
│       ├── Images/
│       │   ├── Input/          ← Place your images HERE before running
│       │   └── Edited/         ← Saved results appear here (auto-created)
│       ├── handlers/
│       │   ├── ConsoleMenuHandler.java
│       │   ├── ImageEditorHandler.java
│       │   ├── ImageFileHandler.java
│       │   ├── InputHandler.java
│       │   └── OperationHandler.java
│       ├── templates/
│       │   └── ImageTemplate.java
│       ├── tools/
│       │   ├── CoordsValidator.java
│       │   ├── CropOperation.java
│       │   ├── InvertOperation.java
│       │   ├── RegionExtractor.java
│       │   └── RotateOperation.java
│       └── utils/
│           └── MenuPrinter.java
```

---

## How to Run

### Using an IDE (Recommended)
1. Open the project in your IDE (IntelliJ, Eclipse, etc.).
2. Make sure your **working directory** is set to the project root (the folder containing `src/`).
3. Run `Main.java`.

---

## What You Can Do

| Feature | Description |
|---------|-------------|
| **Crop** | Select a rectangular area to keep. Everything outside is removed and the canvas shrinks to that area. |
| **Invert Full Image** | Flip all pixel colours to their opposite (photographic negative effect). |
| **Invert Region** | Same as above, but only within a rectangle you define. |
| **Rotate Region** | Rotate a rectangular area 90°, 180°, or 270° clockwise. |
| **Save** | Write the edited image to `Images/Edited/` with `_edited` appended to the filename. |
| **Multiple Operations** | You can apply any combination of operations before saving. |

---

## Step-by-Step Operation Guide

### 1. Loading an Image

Before launching the program, copy your image file into:
```
src/edu/up/cg/Images/Input/
```

Supported formats: `.png`, `.jpg`, `.jpeg`, `.bmp`

When you run the program, you will see:
```
Available images in Input folder:
  1. photo.png
  2. sample.jpg
Select image number:
```
Type the number of the image you want to edit and press **Enter**.

---

### 2. Crop

**What it does:** Cuts the image down to a rectangle you select. The final image will only contain the pixels inside that rectangle.

**Main menu:** Press `1`

**Example interaction:**
```
  Top-Left     X: 100
  Top-Left     Y: 50
  Bottom-Right X: 400
  Bottom-Right Y: 300
  Crop complete.
```

>  **Warning:** Crop permanently replaces the image in memory. All further operations act on the smaller cropped image. There is no undo.

---

### 3. Invert Colors

**What it does:** Replaces each pixel's RGB values with their inverse (`new = 255 − old`), producing a photographic negative.

**Main menu:** Press `2`

You will see a sub-menu:
```
  1. Invert full image
  2. Invert selected region
Choose option:
```

**Option 1 — Full image:** Press `1`. The entire image is immediately inverted. No coordinates needed.

**Option 2 — Region only:** Press `2`, then enter the rectangle coordinates when prompted.

**Example (region):**
```
  Top-Left     X: 0
  Top-Left     Y: 0
  Bottom-Right X: 200
  Bottom-Right Y: 200
 Region inverted.
```

---

### 4. Rotate Region

**What it does:** Rotates a rectangular area of the image clockwise by 90°, 180°, or 270°. The rest of the image is unchanged. Canvas size stays the same.

**Main menu:** Press `3`

**Example interaction:**
```
  Top-Left     X: 50
  Top-Left     Y: 50
  Bottom-Right X: 250
  Bottom-Right Y: 250
Rotation angle — allowed values are 90, 180, 270
Enter angle: 90
Region rotated.
```

> Only `90`, `180`, and `270` are accepted. Any other value will show an error and return to the main menu.

---

### 5. Save

**What it does:** Writes the current state of the image to `Images/Edited/`.

**Main menu:** Press `4`

The output file is named automatically:
```
originalName_edited.extension
```
Example: `photo.png` → `Images/Edited/photo_edited.png`

> You can save multiple times. Each save **overwrites** the previous `_edited` file for the same image name.

> Save whenever you are happy with the current state. There is no auto-save.

---

### 6. Exit

**Main menu:** Press `5`

The program exits immediately. **If you have not saved, your changes will be lost.**

---

## Common Errors and Fixes

| Error message | Cause | Fix |
|---------------|-------|-----|
| `Input folder not found` | Working directory is wrong | Run from the project root, not from inside `src/` |
| `No images found in Input folder` | Input/ folder is empty | Copy a `.png`, `.jpg`, or `.bmp` file into `Images/Input/` |
| `Coordinates cannot be negative` | You entered a negative number | Enter values ≥ 0 |
| `Coordinates exceed image dimensions` | Selection goes beyond the image edge | Use coordinates within the image's width and height |
| `Rectangle must have positive width and height` | x1 ≥ x2 or y1 ≥ y2 | Make sure top-left is above and to the left of bottom-right |
| `Invalid angle` | Entered something other than 90, 180, 270 | Re-run Rotate and type only 90, 180, or 270 |
| `Failed to save image` | Unsupported format for writing | Use PNG or JPG input images |

