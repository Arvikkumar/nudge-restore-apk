package com.example.gentlenudge

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.gentlenudge.ui.viewmodel.NudgeViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = GentleNudgeApp::class)
class UserNameBadgeTest {

    private lateinit var app: GentleNudgeApp

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        val prefs = app.getSharedPreferences("gentle_nudge_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun testDefaultUserNameIsCyrus() {
        val vm = NudgeViewModel(app, app.repository)
        assertEquals("cyrus", vm.userName.value)
    }

    @Test
    fun testUpdateUserNameChangesValueAndPersists() {
        val vm = NudgeViewModel(app, app.repository)
        assertEquals("cyrus", vm.userName.value)

        vm.updateUserName("Arvi")
        assertEquals("Arvi", vm.userName.value)

        // Verify it was written to SharedPreferences
        val prefs = app.getSharedPreferences("gentle_nudge_prefs", Context.MODE_PRIVATE)
        assertEquals("Arvi", prefs.getString("user_name", null))

        // Verify simulating app restart (new ViewModel initialization) preserves the changed name
        val restartedVm = NudgeViewModel(app, app.repository)
        assertEquals("Arvi", restartedVm.userName.value)
    }

    @Test
    fun testBlankUserNameFallsBackToCyrus() {
        val vm = NudgeViewModel(app, app.repository)
        vm.updateUserName("   ")
        assertEquals("cyrus", vm.userName.value)
    }
}
