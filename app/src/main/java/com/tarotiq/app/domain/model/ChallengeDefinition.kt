package com.tarotiq.app.domain.model

data class ChallengeDay(
    val day: Int,
    val intention: String,
    val spreadKey: String = "single"
)

data class ChallengeDefinition(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val durationDays: Int,
    val coinReward: Int,
    val days: List<ChallengeDay>
) {
    companion object {
        fun all(context: android.content.Context): List<ChallengeDefinition> {
            return listOf(
                sevenDayChallenge(context),
                twentyOneDayChallenge(context),
                thirtyDayChallenge(context)
            )
        }

        fun findById(id: String, context: android.content.Context): ChallengeDefinition? =
            all(context).firstOrNull { it.id == id }

        private fun sevenDayChallenge(context: android.content.Context): ChallengeDefinition {
            val intentions = listOf(
                context.getString(com.tarotiq.app.R.string.challenge_7_d1),
                context.getString(com.tarotiq.app.R.string.challenge_7_d2),
                context.getString(com.tarotiq.app.R.string.challenge_7_d3),
                context.getString(com.tarotiq.app.R.string.challenge_7_d4),
                context.getString(com.tarotiq.app.R.string.challenge_7_d5),
                context.getString(com.tarotiq.app.R.string.challenge_7_d6),
                context.getString(com.tarotiq.app.R.string.challenge_7_d7)
            )
            return ChallengeDefinition(
                id = "7day_awakening",
                titleRes = com.tarotiq.app.R.string.challenge_7_name,
                descriptionRes = com.tarotiq.app.R.string.challenge_7_desc,
                durationDays = 7,
                coinReward = 15,
                days = intentions.mapIndexed { i, intention -> ChallengeDay(i + 1, intention) }
            )
        }

        private fun twentyOneDayChallenge(context: android.content.Context): ChallengeDefinition {
            val intentions = listOf(
                "Jaká je moje životní energie dnes?",
                "Co potřebuji přijmout?",
                "Jaká část mě touží po změně?",
                "Co mi říká moje intuice?",
                "Jak se projevuje moje vnitřní síla?",
                "Co mi brání v sebevyjádření?",
                "Jaká energie mi pomůže v práci?",
                "Jak mohu prohloubit své vztahy?",
                "Co si zasluhuji v tomto období?",
                "Jaká lekce se mi opakuje v životě?",
                "Kde hledám rovnováhu?",
                "Co potřebuji uzdravit?",
                "Jaká přání nosím v srdci?",
                "Jak mohu lépe pečovat o sebe?",
                "Co mi přináší radost?",
                "Jaká energie mě provází v tomto týdnu?",
                "Co se snažím kontrolovat místo přijetí?",
                "Jaký dar přináší tato výzva?",
                "Jak mohu sloužit svému vyššímu já?",
                "Co se v mém životě transformuje?",
                "Jaké poselství si odnáším z 21 dnů?"
            )
            return ChallengeDefinition(
                id = "21day_transformation",
                titleRes = com.tarotiq.app.R.string.challenge_21_name,
                descriptionRes = com.tarotiq.app.R.string.challenge_21_desc,
                durationDays = 21,
                coinReward = 40,
                days = intentions.mapIndexed { i, intention -> ChallengeDay(i + 1, intention) }
            )
        }

        private fun thirtyDayChallenge(context: android.content.Context): ChallengeDefinition {
            val intentions = listOf(
                "Jakou energii přináším do nového měsíce?",
                "Co potřebuji v tomto měsíci prioritizovat?",
                "Jaká je moje stínová strana?",
                "Co mi přináší vnitřní klid?",
                "Jak se projevuji v lásce?",
                "Co mi říká má duše?",
                "Jaký archetyp v sobě nesu?",
                "Co chci v tomto měsíci stvořit?",
                "Jaké překážky stojí na mé cestě?",
                "Kde plýtvám energií?",
                "Co mi přináší inspiraci?",
                "Jak mohu být autentičtější?",
                "Jaké vzorce se opakují v mém životě?",
                "Kde hledám bezpečí?",
                "Co mi říká mé tělo?",
                "Jak mohu lépe naslouchat druhým?",
                "Co potřebuji odpustit?",
                "Jaká je moje životní mise?",
                "Co mi přináší sílu v těžkých chvílích?",
                "Jak mohu prohloubit svou spiritualitu?",
                "Co se snažím popírat?",
                "Jaký dar mám přinést světu?",
                "Kde jsem v nepochopení sám se sebou?",
                "Co si přeji pro svůj život?",
                "Jak mohu lépe zvládat emoce?",
                "Jaká energie mě provází ke konci měsíce?",
                "Co jsem se v tomto měsíci naučil/a?",
                "Jaká transformace ve mně probíhá?",
                "Co přijímám a co pouštím?",
                "Jaké poselství si neseme do dalšího měsíce?"
            )
            return ChallengeDefinition(
                id = "30day_mastery",
                titleRes = com.tarotiq.app.R.string.challenge_30_name,
                descriptionRes = com.tarotiq.app.R.string.challenge_30_desc,
                durationDays = 30,
                coinReward = 80,
                days = intentions.mapIndexed { i, intention -> ChallengeDay(i + 1, intention) }
            )
        }
    }
}
