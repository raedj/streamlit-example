package com.polyglot.academy.ui.app

import androidx.compose.runtime.Composable
import com.polyglot.academy.ui.feature.assessment.AssessmentScreen
import com.polyglot.academy.ui.feature.home.HomeScreen
import com.polyglot.academy.ui.feature.learn.CoursesScreen
import com.polyglot.academy.ui.feature.learn.LessonOverviewScreen
import com.polyglot.academy.ui.feature.lesson.LessonPlayerScreen
import com.polyglot.academy.ui.feature.me.HistoryScreen
import com.polyglot.academy.ui.feature.me.LibraryScreen
import com.polyglot.academy.ui.feature.me.MaterialScreen
import com.polyglot.academy.ui.feature.me.MeScreen
import com.polyglot.academy.ui.feature.me.SearchScreen
import com.polyglot.academy.ui.feature.me.VocabularyScreen
import com.polyglot.academy.ui.feature.plan.PlanScreen
import com.polyglot.academy.ui.feature.practice.GrammarListScreen
import com.polyglot.academy.ui.feature.practice.GrammarTopicScreen
import com.polyglot.academy.ui.feature.practice.ListeningScreen
import com.polyglot.academy.ui.feature.practice.MistakesScreen
import com.polyglot.academy.ui.feature.practice.PracticeScreen
import com.polyglot.academy.ui.feature.practice.PronunciationScreen
import com.polyglot.academy.ui.feature.practice.ReadingListScreen
import com.polyglot.academy.ui.feature.practice.ReadingPassageScreen
import com.polyglot.academy.ui.feature.practice.RelaxScreen
import com.polyglot.academy.ui.feature.practice.ReviewScreen
import com.polyglot.academy.ui.feature.practice.TonesScreen
import com.polyglot.academy.ui.feature.practice.WritingScreen
import com.polyglot.academy.ui.feature.progress.ProgressScreen
import com.polyglot.academy.ui.feature.settings.ProviderDetailScreen
import com.polyglot.academy.ui.feature.settings.SettingsPageScreen
import com.polyglot.academy.ui.feature.settings.SettingsScreen
import com.polyglot.academy.ui.feature.tutor.ConversationScreen
import com.polyglot.academy.ui.feature.tutor.ScenariosScreen
import com.polyglot.academy.ui.feature.tutor.TutorDetailScreen
import com.polyglot.academy.ui.feature.tutor.TutorsScreen
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.Tab

@Composable
fun RouteContent(route: Route) {
    when (route) {
        is Route.TabRoot -> when (route.tab) {
            Tab.HOME -> HomeScreen()
            Tab.LEARN -> CoursesScreen(null, isTabRoot = true)
            Tab.PRACTICE -> PracticeScreen()
            Tab.TUTORS -> TutorsScreen()
            Tab.ME -> MeScreen()
        }
        is Route.Courses -> CoursesScreen(route.trackId, isTabRoot = false)
        is Route.LessonOverview -> LessonOverviewScreen(route.lessonId)
        is Route.LessonPlayer -> LessonPlayerScreen(route.lessonId, route.restart)
        Route.Review -> ReviewScreen()
        Route.Mistakes -> MistakesScreen()
        Route.Grammar -> GrammarListScreen()
        is Route.GrammarTopic -> GrammarTopicScreen(route.topicId)
        Route.Reading -> ReadingListScreen()
        is Route.ReadingPassage -> ReadingPassageScreen(route.passageId)
        Route.Listening -> ListeningScreen()
        Route.Writing -> WritingScreen()
        Route.Pronunciation -> PronunciationScreen()
        Route.Characters -> com.polyglot.academy.ui.feature.practice.CharactersScreen()
        Route.SpeakingPath -> com.polyglot.academy.ui.feature.practice.SpeakingPathScreen()
        Route.Phrasebook -> com.polyglot.academy.ui.feature.practice.PhrasebookScreen()
        Route.Tones -> TonesScreen()
        Route.Relax -> RelaxScreen()
        Route.Vocabulary -> VocabularyScreen()
        Route.Library -> LibraryScreen()
        is Route.Material -> MaterialScreen(route.materialId)
        Route.Search -> SearchScreen()
        Route.Plan -> PlanScreen()
        Route.Progress -> ProgressScreen()
        is Route.Assessment -> AssessmentScreen(route.language, route.fromOnboarding)
        is Route.TutorDetail -> TutorDetailScreen(route.tutorId)
        is Route.Conversation -> ConversationScreen(route.tutorId, route.scenarioId, route.offline)
        Route.Scenarios -> ScenariosScreen()
        Route.Settings -> SettingsScreen()
        is Route.SettingsPage -> SettingsPageScreen(route.section)
        is Route.ProviderDetail -> ProviderDetailScreen(route.provider)
        Route.History -> HistoryScreen()
    }
}
