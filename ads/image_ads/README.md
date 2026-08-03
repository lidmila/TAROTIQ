# TAROTIQ — Static Image Ads

Static PNG ads exported from the HTML5 creatives. **No account eligibility gate** — unlike
HTML5 upload ads, image assets can be used by any Google Ads account (App campaigns,
Display, Demand Gen).

> Why these exist: HTML5 upload ads require the account to (a) use AMPHTML, (b) have spent
> >$9,000 USD and be >90 days old, or (c) be allowlisted
> (https://developers.google.com/google-ads/api/docs/display-upload-ads/html5-upload-ads).
> That eligibility gate — not the creatives — was why HTML5 previews rendered blank.

## Files (9 = 3 concepts × 3 sizes)
| Concept | 320×480 | 480×320 | 1200×628 |
|---|---|---|---|
| Three-card (Past·Present·Future) | `threecard_320x480.png` | `threecard_480x320.png` | `threecard_1200x628.png` |
| Relationship (The Lovers) | `relationship_320x480.png` | `relationship_480x320.png` | `relationship_1200x628.png` |
| Daily card (The Sun) | `dailycard_320x480.png` | `dailycard_480x320.png` | `dailycard_1200x628.png` |

All ≤ 360 KB (limit 5120 KB). 1200×628 is the primary landscape size for App campaigns.

## Regenerate
Sources live in the session scratchpad pipeline (`safe/` fixed-size HTML + `safe/tc_1200.html`,
`safe/sg_1200.html`); rendered via Playwright at 2× and Lanczos-downscaled to exact pixels.
The HTML5 zip sources remain in `../html5_appcampaign/source/` if the HTML5 route is ever
unblocked (allowlist or AMPHTML rebuild).
