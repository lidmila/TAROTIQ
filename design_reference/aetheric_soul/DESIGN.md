# Design System Specification: Editorial Mysticism

## 1. Overview & Creative North Star: "The Celestial Archive"
The Creative North Star for this design system is **The Celestial Archive**. We are moving away from the "app-as-a-tool" utility and toward "app-as-a-sanctuary." This system rejects the rigid, boxy constraints of standard Material Design in favor of a fluid, editorial experience that feels like reading a modern grimoire.

To break the "template" look, we employ **Intentional Asymmetry**. Headers should not always be centered; cards should vary in height based on content "weight," and overlapping elements (like a serif title bleeding slightly over a glassmorphic card) are encouraged to create a sense of physical depth and curated intent.

---

## 2. Colors & The Surface Philosophy
The palette is rooted in the deep cosmos, utilizing `background (#14121b)` as our infinite canvas.

### The "No-Line" Rule
**Explicit Instruction:** 1px solid borders are strictly prohibited for sectioning. We define boundaries through tonal shifts. If you need to separate a list of tarot meanings or astronomical events, use a `surface-container-low` background against the `surface` base. 

### Surface Hierarchy & Nesting
Treat the UI as a series of nested celestial spheres.
*   **Base Level:** `surface` (#14121b)
*   **Secondary Content:** `surface-container` (#211e28)
*   **Interactive Cards:** `surface-container-high` (#2b2933)
*   **Floating/Active Elements:** `surface-bright` (#3b3842)

### The "Glass & Gold" Rule
To capture the "magical" requirement, use `secondary` (#e9c349) and `secondary-container` (#af8d11) as light-sources. 
*   **Glassmorphism:** For floating navigation or modal overlays, use `surface-variant` at 60% opacity with a `backdrop-blur` of 20px. 
*   **Signature Textures:** Apply a linear gradient from `primary_container` (#28007b) to `surface` at a 45-degree angle for hero sections to simulate a nebula effect.

---

## 3. Typography: The Dual-Spirit System
We use a high-contrast pairing to balance ancient wisdom with modern clarity.

*   **The Sophisticated Serif (Noto Serif):** Used for `display`, `headline`, and card titles. This is the "voice" of the mystic. It should feel intentional and slow to read.
    *   *Usage:* Use `display-lg` for daily horoscopes or major card titles to create an editorial "magazine" feel.
*   **The Clean Sans-Serif (Manrope):** Used for `title`, `body`, and `label`. This is the "guide." It provides the legibility required for complex spiritual data.
    *   *Usage:* All interactive UI elements (buttons, inputs) must use `label-md` or `title-sm` to ensure the interface feels grounded and functional.

---

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are too "heavy" for a spiritual experience. We use light and layering instead.

*   **The Layering Principle:** Rather than shadows, place a `surface-container-lowest` card inside a `surface-container-high` area to create an "inset" feel, or vice versa for a "lifted" feel.
*   **Ambient Shadows:** If a card must float (e.g., a pulled Tarot card), use a shadow tinted with `primary` (#cbbeff) at 6% opacity with a 32px blur. It should look like the card is glowing, not casting a shadow.
*   **The "Ghost Border" Fallback:** If a container lacks contrast, use `outline-variant` (#49454e) at **15% opacity**. It should be felt, not seen.

---

## 5. Components & Primitive Styling

### Buttons (The Sigils)
*   **Primary:** A gradient fill from `primary` (#cbbeff) to `inverse_primary` (#6349c0). Roundedness: `full`. No border.
*   **Secondary:** Ghost style. No fill. A `secondary` (#e9c349) text color with a soft `secondary` outer glow (4px blur) on hover.

### Cards & Lists (The Tablets)
*   **Constraint:** Forbid divider lines. Use `spacing-6` (2rem) of vertical white space to separate entries.
*   **Styling:** Use `xl` (1.5rem) rounded corners. Background should be `surface-container-low`. For "Featured" content, use a subtle `on-surface-variant` top-left glow.

### Input Fields (The Vessel)
*   **Styling:** Minimalist. Only a bottom "Ghost Border" (15% opacity `outline`). When focused, the border transitions to 100% `secondary` gold with a subtle `secondary_container` glow.

### Spiritual Specialty Components
*   **The "Aura" Chip:** Use `tertiary_container` for the background with `tertiary` text. Roundedness: `md`. Use these for "Energy Tags" or "Zodiac Elements."
*   **The "Cosmic" Progress Bar:** A thin 2dp track using `surface-container-highest`. The progress indicator should be a gradient of `secondary` to `primary` with a "spark" (small white dot) at the leading edge.

---

## 6. Do’s and Don’ts

### Do:
*   **Do** use asymmetrical margins. Try `spacing-8` on the left and `spacing-4` on the right for headline text to create an editorial look.
*   **Do** allow images of the night sky or sacred geometry to bleed behind the `surface` layer using 20% opacity.
*   **Do** use `notoSerif` for numbers if they represent mystical values (dates, degrees, numerology).

### Don’t:
*   **Don’t** use pure black (#000000). Always use the `background` token (#14121b) to keep the "midnight" depth.
*   **Don’t** use standard Material icons in their "Filled" state. Use "Light" or "Thin" stroke icons to match the elegance of the typography.
*   **Don’t** crowd the interface. If in doubt, add more `spacing-5` or `spacing-6` blocks. The stars need space to breathe.