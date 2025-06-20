package io.cheddarswallet.tonkit.core

import android.util.Log
import io.cheddarswallet.tonkit.Address
import io.cheddarswallet.tonkit.api.IApi
import io.cheddarswallet.tonkit.models.Event
import io.cheddarswallet.tonkit.models.EventInfo
import io.cheddarswallet.tonkit.models.EventSyncState
import io.cheddarswallet.tonkit.models.SyncState
import io.cheddarswallet.tonkit.models.Tag
import io.cheddarswallet.tonkit.models.TagQuery
import io.cheddarswallet.tonkit.models.TagToken
import io.cheddarswallet.tonkit.storage.EventDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class EventManager(
    private val address: Address,
    private val api: IApi,
    private val dao: EventDao,
) {
    private val eventFlow = MutableSharedFlow<EventInfoWithTags>()

    private val _syncStateFlow =
        MutableStateFlow<SyncState>(SyncState.NotSynced(TonKit.SyncError.NotStarted))
    val syncStateFlow = _syncStateFlow.asStateFlow()

    fun events(tagQuery: TagQuery, beforeLt: Long?, limit: Int?): List<Event> {
        return dao.events(tagQuery, beforeLt, limit ?: 100)
    }

    fun eventFlow(tagQuery: TagQuery): Flow<EventInfo> {
        var filteredEventFlow: Flow<EventInfoWithTags> = eventFlow.asSharedFlow()

        if (!tagQuery.isEmpty) {
            filteredEventFlow = filteredEventFlow.filter { info: EventInfoWithTags ->
                info.events.any { eventWithTags ->
                    eventWithTags.tags.any { it.conforms(tagQuery) }
                }
            }
        }

        return filteredEventFlow.map { info ->
            EventInfo(
                info.events.map { it.event },
                info.initial
            )
        }
    }

    fun tagTokens(): List<TagToken> {
        return dao.tagTokens()
    }

    suspend fun sync() {
        Log.d("AAA", "Syncing events...")

        if (_syncStateFlow.value is SyncState.Syncing) {
            Log.d("AAA", "Syncing events is in progress")
            return
        }

        _syncStateFlow.update {
            SyncState.Syncing
        }

        try {
            val latestEvent = dao.latestEvent()

            if (latestEvent != null) {
                Log.d("AAA", "Fetching latest events...")

                val startTimestamp = latestEvent.timestamp
                var beforeLt: Long? = null

                do {
                    val events = api.getEvents(address, beforeLt, startTimestamp, limit)
                    Log.d(
                        "AAA",
                        "Got latest events: ${events.size}, beforeLt: $beforeLt, startTimestamp: $startTimestamp"
                    )

                    handleLatest(events)

                    if (events.size < limit) {
                        break
                    }

                    beforeLt = events.lastOrNull()?.lt

                } while (true)
            }
            val eventSyncState = dao.eventSyncState()
            val allSynced = eventSyncState?.allSynced ?: false

            if (!allSynced) {
                Log.d("AAA", "Fetching history events...")

                val oldestEvent = dao.oldestEvent()
                var beforeLt = oldestEvent?.lt
                do {
                    val events = api.getEvents(address, beforeLt, null, limit)
                    Log.d("AAA", "Got history events: ${events.size}, beforeLt: $beforeLt")

                    handle(events, true)

                    if (events.size < limit) {
                        break
                    }

                    beforeLt = events.lastOrNull()?.lt

                } while (true)

                val newOldestEvent = dao.oldestEvent()

                if (newOldestEvent != null) {
                    dao.save(EventSyncState(allSynced = true))
                }
            }

            _syncStateFlow.update {
                SyncState.Synced
            }
        } catch (e: Throwable) {
            _syncStateFlow.update {
                SyncState.NotSynced(e)
            }
        }
    }

    private suspend fun handleLatest(events: List<Event>) {
        val (inProgressEvents, completedEvents) = events.partition { it.inProgress }

        val eventsToHandle = mutableListOf<Event>()

        if (completedEvents.isNotEmpty()) {
            val existingEvents = dao.events(completedEvents.map { it.id })
            completedEvents.forEach { completedEvent ->
                val existingEvent = existingEvents.find { it.id == completedEvent.id }
                if (existingEvent == null || existingEvent.inProgress) {
                    eventsToHandle.add(completedEvent)
                }
            }
        }

        handle(inProgressEvents + eventsToHandle, false)
    }

    private suspend fun handle(events: List<Event>, initial: Boolean) {
        if (events.isEmpty()) return

        dao.save(events)
        val eventsWithTags = events.map { event ->
            EventWithTags(event, event.tags(address))
        }

        val tags = eventsWithTags.map { it.tags }.flatten()
        dao.resave(tags, events.map { it.id })

        eventFlow.emit(EventInfoWithTags(eventsWithTags, initial))
    }

    fun isEventCompleted(eventId: String) : Boolean {
        return dao.isEventCompleted(eventId)
    }

    companion object {
        private const val limit = 100
    }

    private data class EventWithTags(
        val event: Event,
        val tags: List<Tag>,
    )

    private data class EventInfoWithTags(
        val events: List<EventWithTags>,
        val initial: Boolean,
    )
}
