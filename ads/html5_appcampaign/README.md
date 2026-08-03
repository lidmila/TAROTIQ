# TAROTIQ — HTML5 Ads for Google Ads App Campaigns

Built strictly to the Google Ads App-campaign HTML5 spec
(https://support.google.com/google-ads/answer/9981650):

- ✅ **Responsive full-screen** design (scales across device sizes via `vmin` units) — NOT a fixed-pixel box
- ✅ `<meta name="ad.orientation" content="portrait">` / `content="landscape"` (per file)
- ✅ Click handled by **`ExitApi.exit()`** via `https://tpc.googlesyndication.com/pagead/gadgets/html5/api/exitapi.js` (no clickTag — that's for Display)
- ✅ `<!DOCTYPE html>`, `<html>`, `<body>`, explicit end tags, UTF-8
- ✅ ZIP, flat structure (index.html + images at root, relative refs), **PNG images** (NOT webp), ≤ 5 MB, ≤ 512 files
- ✅ Only Google-hosted external resources (the exitapi script)

## Upload-ready files (`zips_upload/`)
| Concept | Portrait | Landscape |
|---|---|---|
| **Three-card** (Past·Present·Future) | `threecard_portrait.zip` | `threecard_landscape.zip` |
| **Relationship** (The Lovers) | `relationship_portrait.zip` | `relationship_landscape.zip` |
| **Daily card** (The Sun) | `dailycard_portrait.zip` | `dailycard_landscape.zip` |

All 79–183 KB. Animation: cards flip to reveal, CTA pulses, loops, freezes before 30 s.
Whole ad is clickable → `ExitApi.exit()` (Google routes it to your app's store listing).

Preview at real device sizes: `PREVIEW_fullscreen.png`.

## Upload (Google Ads App campaign)
Ad group → **Ads → HTML5** → upload each ZIP. Google reads `ad.orientation` automatically.
If you omit a custom CTA, Google adds an "Install" button; these include their own CTA and call `ExitApi.exit()`.

## Edit / rebuild
`source/threecard.html` and `source/single.html` are the responsive creatives.
Run `python source/build_appads.py` to regenerate all 6 ZIPs (converts card art to PNG, injects
`ad.orientation`, zips flat). Card art is optimized from the app's HD major arcana.
