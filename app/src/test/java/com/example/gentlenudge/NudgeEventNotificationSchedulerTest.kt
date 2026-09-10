package com.example.gentlenudge

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.gentlenudge.notification.EventNotificationMode
import com.example.gentlenudge.notification.NudgeEventNotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NudgeEventNotificationSchedulerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Reset to default
        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.OFF
        )
    }

    @Test
    fun testDefaultSettingIsOff() {
        val mode = NudgeEventNotificationScheduler.getEventNotificationMode(context)
        val days = NudgeEventNotificationScheduler.getEventNotificationDays(context)
        assertEquals(EventNotificationMode.OFF, mode)
        assertEquals(0, days)
    }

    @Test
    fun testSetOneDayBefore() {
        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.ONE_DAY_BEFORE
        )

        val mode = NudgeEventNotificationScheduler.getEventNotificationMode(context)
        val days = NudgeEventNotificationScheduler.getEventNotificationDays(context)

        assertEquals(EventNotificationMode.ONE_DAY_BEFORE, mode)
        assertEquals(1, days)
    }

    @Test
    fun testSetTwoDaysBefore() {
        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.TWO_DAYS_BEFORE
        )

        val mode = NudgeEventNotificationScheduler.getEventNotificationMode(context)
        val days = NudgeEventNotificationScheduler.getEventNotificationDays(context)

        assertEquals(EventNotificationMode.TWO_DAYS_BEFORE, mode)
        assertEquals(2, days)
    }

    @Test
    fun testSetCustomDays() {
        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.CUSTOM,
            customDays = 5
        )

        val mode = NudgeEventNotificationScheduler.getEventNotificationMode(context)
        val days = NudgeEventNotificationScheduler.getEventNotificationDays(context)

        assertEquals(EventNotificationMode.CUSTOM, mode)
        assertEquals(5, days)
    }

    @Test
    fun testTurnOffCancels() {
        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.CUSTOM,
            customDays = 4
        )
        assertEquals(4, NudgeEventNotificationScheduler.getEventNotificationDays(context))

        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.OFF
        )

        val mode = NudgeEventNotificationScheduler.getEventNotificationMode(context)
        val days = NudgeEventNotificationScheduler.getEventNotificationDays(context)

        assertEquals(EventNotificationMode.OFF, mode)
        assertEquals(0, days)
    }

    @Test
    fun testRequestCodeGeneration() {
        val code1 = NudgeEventNotificationScheduler.getEventRequestCode("diwali", 2026, 11, 8)
        val code2 = NudgeEventNotificationScheduler.getEventRequestCode("holi", 2026, 3, 4)
        val code3 = NudgeEventNotificationScheduler.getEventRequestCode("diwali", 2026, 11, 8)

        assertEquals(code1, code3)
        assertNotEquals(code1, code2)
    }

    @Test
    fun testDeliveredReminderTrackingAndDuplicatePrevention() {
        val reminderKey = NudgeEventNotificationScheduler.getEventReminderKey("test_event", 2026, 9, 4)
        assertEquals("test_event-2026-9-4", reminderKey)

        NudgeEventNotificationScheduler.clearDeliveredEventReminders(context)
        assertEquals(false, NudgeEventNotificationScheduler.isEventReminderDelivered(context, reminderKey))

        NudgeEventNotificationScheduler.markEventReminderDelivered(context, reminderKey)
        assertEquals(true, NudgeEventNotificationScheduler.isEventReminderDelivered(context, reminderKey))

        // Delivering again should return false (duplicate prevented)
        val secondDelivery = NudgeEventNotificationScheduler.deliverMissedReminder(
            context = context,
            eventId = "test_event",
            eventName = "Test Event",
            category = "Festivals",
            description = "Test description",
            year = 2026,
            month = 9,
            day = 4,
            daysBefore = 1
        )
        assertEquals(false, secondDelivery)
    }

    @Test
    fun testSwitchingModeClearsPreviousAlarms() {
        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.ONE_DAY_BEFORE
        )
        assertEquals(1, NudgeEventNotificationScheduler.getEventNotificationDays(context))

        NudgeEventNotificationScheduler.setEventNotificationSetting(
            context = context,
            mode = EventNotificationMode.TWO_DAYS_BEFORE
        )
        assertEquals(2, NudgeEventNotificationScheduler.getEventNotificationDays(context))
        assertEquals(EventNotificationMode.TWO_DAYS_BEFORE, NudgeEventNotificationScheduler.getEventNotificationMode(context))
    }
}
