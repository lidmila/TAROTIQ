# TAROTIQ — AMPHTML Ads (amp4ads) for Google Ads HTML5 Upload

AMPHTML creatives that satisfy the HTML5-upload **eligibility criterion #1** ("the ads media
bundle uses AMPHTML instead of traditional HTML") — so they can be uploaded **without** the
$9,000-spend / 90-day account requirement or allowlisting
(https://developers.google.com/google-ads/api/docs/display-upload-ads/html5-upload-ads).

**All 6 creatives PASS the official AMP validator** (`npx amphtml-validator --html_format=AMP4ADS`).

## Upload-ready files (`zips_upload/`)
| Concept | Portrait 320×480 | Landscape 480×320 |
|---|---|---|
| Three-card (Past·Present·Future) | `threecard_portrait.zip` | `threecard_landscape.zip` |
| Relationship (The Lovers) | `relationship_portrait.zip` | `relationship_landscape.zip` |
| Daily card (The Sun) | `dailycard_portrait.zip` | `dailycard_landscape.zip` |

53–158 KB each, flat structure (`index.html` + PNGs at root).

## Spec compliance (amp4ads)
- `<html amp4ads>` boilerplate, `amp4ads-v0.js` runtime, `<style amp4ads-boilerplate>`
- `<meta name="ad.size">` fixed dimensions
- `<amp-img layout="fill">` (no `<img>`), **no custom JavaScript**
- Animations use only `opacity`/`transform` (card entrance fade/slide, CTA pulse) — the only
  properties amp4ads allows
- Click-through: **no `<a href>` and no `<amp-ad-exit>`** — Google Ads explicitly forbids
  custom exits in uploaded AMPHTML ads ("Your entire ad will be clickable"); the campaign
  Final URL is applied automatically
- CSS < 2 KB (limit 20 KB), no `position:fixed/sticky`
- Google Ads upload limits honored: ZIP ≤ 600 KB, ≤ 40 files, only supported components
  (allowed list: amp-accordion, amp-animation, amp-anim, amp-carousel, amp-fit-text,
  amp-font, amp-gwd-animation, amp-img, amp-layout)

## Previews
`PREVIEW_portrait.png`, `PREVIEW_landscape.png` — rendered with the real AMP runtime.

## Regenerate & re-validate
```
python source/build_amp.py
npx -y amphtml-validator --html_format=AMP4ADS <work>/<name>/index.html
```
Card art comes from the app's HD major arcana (300px palette PNGs).
