# Cardiologist

## Overview

Build job schedules with [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) and [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines).

```kt
val tz = TimeZone.of("America/New_York")
Clock.System.schedulePulse(timeZone = tz, atSecond = 0).beat { scheduled, occurred ->
    println("A pulse was scheduled in $tz for ${scheduled.toLocalDateTime(tz)} and occurred at ${occurred.toLocalDateTime(tz)}.")
}
```

```
A pulse was scheduled in America/New_York for 2025-01-11T08:41 and occurred at 2025-01-11T08:41:00.005227606
A pulse was scheduled in America/New_York for 2025-01-11T08:42 and occurred at 2025-01-11T08:42:00.001463582
A pulse was scheduled in America/New_York for 2025-01-11T08:43 and occurred at 2025-01-11T08:43:00.001028731
A pulse was scheduled in America/New_York for 2025-01-11T08:44 and occurred at 2025-01-11T08:44:00.001037604
A pulse was scheduled in America/New_York for 2025-01-11T08:45 and occurred at 2025-01-11T08:45:00.001025281
A pulse was scheduled in America/New_York for 2025-01-11T08:46 and occurred at 2025-01-11T08:46:00.001062597
A pulse was scheduled in America/New_York for 2025-01-11T08:47 and occurred at 2025-01-11T08:47:00.000846953
```

## Download 

```toml
[versions]
cardiologist = "0.4.0"

[libraries]
cardiologist = { module = "io.github.kevincianfarini.cardiologist:cardiologist", version.ref = "cardiologist" }

```

## Usage

### How do I build a job schedule?

Cardiologist offers several different ways to build job schedules, ranging from simple periods to complex recurring 
dates and times. At the core of it all is the `Pulse` type which encapsulates a job schedule. Pulses can be created 
through several different functions exposed on a `kotlinx.datetime.Clock`. 

#### Fixed period job schedules

Cardiologist offers two functions to schedule fixed period schedules using a `kotlin.time.Duration` and a 
`kotlinx.datetime.DateTimePeriod`. Fixed period Pulses are rigid and the period specifies the time between the start 
of a job and the start of its successor. Interval schedules are intentionally omitted from Cardiologist because 
they can be trivially built with a while loop. 

```kt
// Some Clock implementation. 
val clock: Clock = ...
val timeZone = TimeZone.of("America/New_York")

// Creates a Pulse which beats on a fixed 30-minute period.
val durationPulse: Pulse = clock.fixedPeriodPulse(30.minutes)

// Creates a Pulse which beats on a fixed 1-month period in America/New_York.
val dateTimePeriodPulse: Pulse = clock.fixedPeriodPulse(period = DateTimePeriod(months = 1), timeZone = timeZone)
```

#### Complex job schedules

Cardiologist allows complex job schedules to be created using a simple function that accepts type-safe parameters; 
a complex function which exposes a type-safe DSL for building a schedule; and a function which accept a **standard**
loosely typed Cron expression. Cardiologist intentionally implements only standard Cron functionality and considers 
extensions of that specification, like the `@monthly` directive, out of scope.

```kt
// Some Clock implementation. 
val clock: Clock = ... 
val timeZone = TimeZone.of("America/New_York")

// Creates a Pulse which beats on the 5th minute of every hour in America/New_York.
val scheduledPulse: Pulse = clock.schedulePulse(atSecond = 0, atMinute = 5, timeZone = timeZone)

// Creates a Pulse with a complex schedule in America/New_York using a DSL. 
val complexPulse: Pulse = clock.schedulePulse(timeZone = timeZone) {
    atSeconds(0, 30)
    atMinutes(0, 5, 10, 15, 20, 25, 30)
    atHours(9..17)
    onDaysOfWeek(DayOfWeek.Monday..DayOfWeek.Friday)
}

// Creates a Pulse from a standard cron expression in America/New_York.
val cronPulse: Pulse = clock.schedulePulse(cronExpression = "* * * * *", timeZone = timeZone)
```

#### Executing jobs

Pulses alone don't do any work. You need to beat a pulse for it to do something! The lambda exposes two `Instant` 
parameters denoting when a pulse was scheduled and when it actually occurred. 

```kt
clock.fixedPeriodPulse(30.seconds).beat { scheduled, occurred ->
    println("My job was scheduled for $scheduled and occurred at $occurred.")
}
```

### Some of my jobs are slow. How do I stop them from executing concurrently? 

Beating a pulse is a suspending function which might not complete before the next pulse is scheduled to occur. By 
default, Cardiologist allows these jobs to run concurrently rather than silently canceling or skipping jobs. In 
addition, you can specify that when a job doesn't complete quickly enough it can be canceled or the next job can be 
skipped. 

```kt
// Cancels slow jobs. 
clock.fixedPeriodPulse(5.seconds).beat(strategy = PulseBackpressureStrategy.CancelPrevious) { 
    mySlowSuspendingFunction()
}

// Skips new jobs until this slow job completes. 
clock.fixedPeriodPulse(5.seconds).beat(strategy = PulseBackpressureStrategy.SkipNext) { 
    mySlowSuspendingFunction()
}
```

### How do I schedule one-off jobs? 

Cardiologist provides `Clock.executeAt` which executes an action at a specific `Instant` or a specific `LocalDateTime`.

```kt
val instant = Instant.parse("2025-05-10T13:15:00Z")
clock.executeAt(instant) { occurred: Instant ->
    println("I executed at $occurred!")
}

val localDateTime = LocalDatetime(2025, 5, 10, 13, 15, 0)
val timeZone = TimeZone.of("America/New_York")
clock.executeAt(localDateTime, timeZone) { occurred: LocalDateTime ->
    println("I executed at $occurred!")
}
```

### How do I name my jobs? 

Beating a Pulse is just a normal suspending function so we can name jobs just like any other coroutine. The following 
code gives the executing schedule's coroutine the name "5-second Pulse schedule", and each inner job gets its own name 
like "My job which started executing at 2025-05-07T15:24:00"

```kt
val scheduleName = CoroutineName("5-second Pulse schedule")
withContext(scheduleName) {
    clock.fixedPeriodPulse(5.seconds).beat(strategy = PulseBackpressureStrategy.CancelPrevious) { scheduled, _ ->
        val jobName = CoroutineName("My job which started executing at $scheduled")
        withContext(jobName) { myJob() }
    }
}
```

### How many jobs can Cardiologist run concurrently? 

To be determined, but definitely more than 5 and less than 1,000,000. Stress testing coming soon. 

### How can I persist my job schedules (and why doesn't Cardiologist do this for me)?

Cardiologist isn't interested in forcing you to use any persistence scheme. It doesn't care if you like SQL or not, 
and it doesn't want you to have to adopt a heavy piece of infrastructure simply to set up some recurring jobs. For 
people who do want to persist their job schedules, Cardiologist allows for job schedules to be built independently 
of a `Pulse`, and those schedules can be persisted however you like. 

```kt
val schedule: PulseSchedule = buildPulseSchedule {
    atSeconds(0, 30)
    atMinutes(0, 5, 10, 15, 20, 25, 30)
    atHours(9..17)
    onDaysOfWeek(DayOfWeek.Monday..DayOfWeek.Friday)
}
```

Furthermore, Cardiologist provides an out-of-the-box mechanism to serialize job schedules to standard Cron expressions.

```kt
val schedule: PulseSchedule = buildPulseSchedule {
    atSeconds(0, 30)
    atMinutes(0, 5, 10, 15, 20, 25, 30)
    atHours(9..17)
    onDaysOfWeek(DayOfWeek.Monday..DayOfWeek.Friday)
}
val cron: String = schedule.toCronExpression() // 0,5,10,15,20,25,30 9-17 * * 1-5
```

> [!WARNING]
> Transforming a `PulseSchedule` into a standard Cron expression will omit any seconds components as 
> that's not included in the standard Cron specification.

### What's the difference between Cardiologist and other tools like cron?

Cardiologist is an _in process_ job scheduling library. It is meant to run concurrently within the rest of
your process, not as a separate process. Use cases include job scheduling within a long-lived process like
a server or a daemon.

```kt
// With ktor, for example. 
fun main() = runBlocking { // this: CoroutineScope
    this.embeddedServer(Netty, port = 8080) { /* omitted */ }.start(wait = false)
    launch { 
        Clock.System.fixedPeriodPulse(30.minutes).beat { myJob() }
    }
}
```

Cron runs as its own process and will launch your program as a separate processes. This is outside the scope of 
Cardiologist's goals.
