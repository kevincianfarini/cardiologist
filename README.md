# Cardiologist

Build job schedules with [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) and [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines). 

```kt
val tz = TimeZone.of("America/New_York")
Clock.System.schedulePulse(atSecond = 0).beat { instant ->
    println("The time in $tz is ${instant.toLocalDateTime(tz)}.")
}
```

```
The time in America/New_York is 2023-10-07T19:49:00.
The time in America/New_York is 2023-10-07T19:50:00.
The time in America/New_York is 2023-10-07T19:51:00.
The time in America/New_York is 2023-10-07T19:52:00.
The time in America/New_York is 2023-10-07T19:53:00.
```

## Introduction 

Cardiologist integrates with kotlinx-datetime to provide you scheduling based on `Instant`, `LocalDateTime`, 
and `LocalTime`, and integrates with kotlinx-coroutines to provide you a Flow API to trigger your pending jobs
with a suspending API. 

Simple suspending functions are available to delay until a given moment in time.

```kt
Clock.System.delayUntil(instant = Instant.DISTANT_FUTURE)
Clock.System.delayUntil(dateTime = LocalDateTime.MAX, timeZone = TimeZone.UTC)
```

...or suspending for a given period of time. 

```kt
Clock.System.delayFor(period = DateTimePeriod(months = 1, days = 2), timeZone = TimeZone.UTC)
```

...or suspending until the next time a `LocalTime` occurs in a certain time zone. 

```kt
val midnight = LocalTime(hour = 0, minute = 0)
Clock.System.delayUntilNext(time = midnight, timeZone = TimeZone.UTC)
```

Repeating intervals are provided as a `Flow<Pulse>`. 

```kt
val hourlyPulse: Flow<Pulse> = Clock.System.intervalPulse(interval = 1.hours)
val dailyPulse: Flow<Pulse> = Clock.System.intervalPulse(
    period = DateTimePeriod(days = 1),
    timeZone = TimeZone.UTC,
)
```

Pulse schedules can be built with a cron like API. 

```kt
// Schedules a pulse to occur on the 5th of every month at 12:30 in UTC. 
val scheduledPulse = Clock.System.schedulePulse(
    timeZone = TimeZone.UTC,
    atSecond = 0,
    atMinute = 30,
    atHour = 12,
    onDayOfMonth = 5, 
)
```

...and can be collected by calling `Flow<Pulse>.beat`. This will unwrap a `Pulse` and yield
the instant at which the pulse occurred.

```kt
scheduledPulse.beat { instant -> println("$instant") }
```

Beating a pulse is a backpressure sensitive operation. If your job has not completed when the next 
pulse is scheduled to occur, it will by default be cancelled. Cardiologist provides two other modes 
to beat a pulse which allow jobs to run concurrently or apply backpressure. 

You should not collect a `Flow<Pulse>` without calling `beat`. 

```kt
import tech.kraken.cardiologist.RecurringJobMode.*

// Cancels previous job when next job is scheduled to occur. 
hourlyPulse.beat(mode = CancellingSequential) { instant -> longRunningOperation(instant) }
        
// Allows jobs to run concurrently if previous job is still active. 
hourlyPulse.beat(mode = Concurrent) { instant -> longRunningOperation(instant) }

// Will not emit again until previous job finishes, applying backpressure.  
hourlyPulse.beat(mode = DelayBetweenSequential) { instant -> longRunningOperation(instant) }
```



## Cardiologist is not a replacement for crontab(5) or Android WorkManager.

Cardiologist is an _in process_ job scheduling library. It is meant to run concurrently to the rest of 
your process, not as a separate process. Use cases include job scheduling within a long-lived process like 
a server or a daemon. 

```kt
// With ktor, for example. 
fun main() = runBlocking { // this: CoroutineScope
    this.embeddedServer(Netty, port = 8080) { /* omitted */ }.start(wait = false)
    launch { recurringJob() }
}

private suspend fun recurringJob() {
    Clock.System.schedulePulse(
        onDayOfMonth = 1, 
        atHour = 0, 
        atMinute = 0, 
        atSecond = 0
    ).beat { instant -> someWork(instant) }
}
```

Cron jobs and Android's WorkManager leverage standalone processes and will launch your program as a separate processes. 
This is outside the scope of Cadiologist's goals. 