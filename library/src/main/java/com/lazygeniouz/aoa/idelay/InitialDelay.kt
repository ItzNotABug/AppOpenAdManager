package com.lazygeniouz.aoa.idelay

import com.lazygeniouz.aoa.extensions.logDebug

/**
 * Allowing the developer to have an initial delay for showing the Ad after the app is first installed.
 * The InitialDelay class can pass on the information of initial delay.
 *
 * Default delay is set to 1 Day as it is a good practise to
 * allow the user to first explore the App & therefore 1 Day is fine.
 * Use InitialDelay.NONE for no Initial Delay.
 * @see InitialDelay.NONE
 *
 * @param delayCount Integer value used as initial delay
 * @param delayPeriodType Can either be "DelayType.HOUR" for Delay by X Hours, or "DelayType.DAYS" for Delay by X Days.
 *
 */
class InitialDelay(
    private val delayCount: Int = 1,
    internal val delayPeriodType: DelayType = DelayType.DAYS
) {
    init {
        // Zero is fine
        if (delayCount < 0) throw IllegalArgumentException("Delay Count cannot be Negative.")

        // Just a harmless warning message.
        if (delayPeriodType == DelayType.DAYS && delayCount > 2) logDebug(
            "You sure that the InitialDelay set by you is correct?"
        )
    }


    internal fun getTime(): Int {
        val oneHourInMillis = 3600000
        val periodTypeToMillis =
            when (delayPeriodType) {
                DelayType.DAYS -> (oneHourInMillis * 24)
                DelayType.HOUR -> oneHourInMillis
                else -> 0
            }
        return (delayCount * periodTypeToMillis)
    }

    companion object {
        @JvmField
        val NONE = InitialDelay(0, DelayType.NONE)
    }
}