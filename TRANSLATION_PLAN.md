# TAROTIQ — Translation Plan for Additional Languages

## Current State (v1.0)

The app is released with **Czech (cs)** and **English (en)** fully translated:
- UI strings (`strings.xml`)
- Card names (`card_names.xml`) — 78 cards
- Card meanings (`card_meanings*.xml`) — 780 strings (10 interpretations per card)

## Existing Partial Translations

Some languages already have partial card meaning translations in `app/src/main/res/`:

| Language | Folder | card_meanings.xml | card_meanings_minor1.xml | card_meanings_minor2.xml | card_names.xml |
|----------|--------|:-:|:-:|:-:|:-:|
| German | values-de/ | Major Arcana (0-21) | Cups + Pentacles (22-49) | **MISSING** | Created |
| Spanish | values-es/ | Major Arcana (0-21) | Cups + Pentacles (22-49) | **MISSING** | Created |
| French | values-fr/ | Major Arcana (0-21) | **MISSING** | **MISSING** | Created |
| Dutch | values-nl/ | Major Arcana (0-21) | **MISSING** | **MISSING** | Created |
| Italian | values-it/ | **MISSING** | **MISSING** | **MISSING** | Created |
| Polish | values-pl/ | **MISSING** | **MISSING** | **MISSING** | Created |

`card_names.xml` files have been created for all 8 languages.

## What Needs to Be Done Per Language

### German (de) — ~60% complete
- [ ] Create `values-de/card_meanings_minor2.xml` (Swords 50-63 + Wands 64-77, 280 strings)
- [ ] Review existing translations for consistency

### Spanish (es) — ~60% complete
- [ ] Create `values-es/card_meanings_minor2.xml` (Swords 50-63 + Wands 64-77, 280 strings)
- [ ] Review existing translations for consistency

### French (fr) — ~26% complete
- [ ] Create `values-fr/card_meanings_minor1.xml` (Cups 22-35 + Pentacles 36-49, 280 strings)
- [ ] Create `values-fr/card_meanings_minor2.xml` (Swords 50-63 + Wands 64-77, 280 strings)
- [ ] Review existing Major Arcana translations

### Dutch (nl) — ~26% complete
- [ ] Create `values-nl/card_meanings_minor1.xml` (Cups 22-35 + Pentacles 36-49, 280 strings)
- [ ] Create `values-nl/card_meanings_minor2.xml` (Swords 50-63 + Wands 64-77, 280 strings)
- [ ] Review existing Major Arcana translations

### Italian (it) — 0% complete
- [ ] Create `values-it/card_meanings.xml` (Major Arcana 0-21, 220 strings)
- [ ] Create `values-it/card_meanings_minor1.xml` (Cups + Pentacles 22-49, 280 strings)
- [ ] Create `values-it/card_meanings_minor2.xml` (Swords + Wands 50-77, 280 strings)

### Polish (pl) — 0% complete
- [ ] Create `values-pl/card_meanings.xml` (Major Arcana 0-21, 220 strings)
- [ ] Create `values-pl/card_meanings_minor1.xml` (Cups + Pentacles 22-49, 280 strings)
- [ ] Create `values-pl/card_meanings_minor2.xml` (Swords + Wands 50-77, 280 strings)

## How to Enable a Language

When translations for a language are complete:

1. Add the language code to `supportedLanguages` in `LocaleUtils.kt` (line 20)
2. Add the language to `getAvailableLanguages()` in `LocaleUtils.kt` (line 95)
3. Add `<locale android:name="{code}" />` to `locales_config.xml`

## XML Format Reference

Each card meaning file follows this pattern:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Card {id}: {EnglishName} / {LocalName} -->
    <string name="card_{id}_love_up">...</string>
    <string name="card_{id}_love_rev">...</string>
    <string name="card_{id}_career_up">...</string>
    <string name="card_{id}_career_rev">...</string>
    <string name="card_{id}_finances_up">...</string>
    <string name="card_{id}_finances_rev">...</string>
    <string name="card_{id}_feelings_up">...</string>
    <string name="card_{id}_feelings_rev">...</string>
    <string name="card_{id}_actions_up">...</string>
    <string name="card_{id}_actions_rev">...</string>
</resources>
```

**Important:**
- Escape apostrophes: `\'` (critical for French, Italian)
- Escape ampersands: `&amp;`
- Tone: mystical, advisory, second-person, present tense
- Use semicolons as clause separators
- Use traditional tarot terminology for each language

## Total Remaining Work

| Language | Remaining strings |
|----------|------------------|
| German | 280 |
| Spanish | 280 |
| French | 560 |
| Dutch | 560 |
| Italian | 780 |
| Polish | 780 |
| **Total** | **3,240** |
