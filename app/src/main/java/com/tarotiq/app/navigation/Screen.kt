package com.tarotiq.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")

    // Reading flow
    object TopicSelection : Screen("reading/topic")
    object QuestionInput : Screen("reading/question/{topic}") {
        fun createRoute(topic: String) = "reading/question/$topic"
    }
    object SpreadSelection : Screen("reading/spread/{topic}?question={question}") {
        fun createRoute(topic: String, question: String? = null): String {
            return if (question.isNullOrBlank()) {
                "reading/spread/$topic"
            } else {
                "reading/spread/$topic?question=${java.net.URLEncoder.encode(question, "UTF-8")}"
            }
        }
    }
    object CardDrawing : Screen("reading/draw/{topic}/{spreadType}?question={question}") {
        fun createRoute(topic: String, spreadType: String, question: String? = null): String {
            return if (question.isNullOrBlank()) {
                "reading/draw/$topic/$spreadType"
            } else {
                "reading/draw/$topic/$spreadType?question=${java.net.URLEncoder.encode(question, "UTF-8")}"
            }
        }
    }
    object ReadingInterpretation : Screen("reading/interpretation/{readingId}") {
        fun createRoute(readingId: String) = "reading/interpretation/$readingId"
    }
    object ReadingSummary : Screen("reading/summary/{readingId}") {
        fun createRoute(readingId: String) = "reading/summary/$readingId"
    }

    // History
    object ReadingHistory : Screen("history")
    object ReadingDetail : Screen("history/{readingId}") {
        fun createRoute(readingId: String) = "history/$readingId"
    }

    // Card Library
    object CardLibrary : Screen("library")
    object CardDetail : Screen("library/{cardId}") {
        fun createRoute(cardId: Int) = "library/$cardId"
    }

    // Daily Card
    object DailyCard : Screen("daily")

    // Shop
    object CoinShop : Screen("shop")

    // Profile & Info
    object Profile : Screen("profile")
    object FAQ : Screen("faq")
    object About : Screen("about")
    object Contact : Screen("contact")
}
