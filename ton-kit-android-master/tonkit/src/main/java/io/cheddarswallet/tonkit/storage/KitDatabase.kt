package io.cheddarswallet.tonkit.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.cheddarswallet.tonkit.models.Account
import io.cheddarswallet.tonkit.models.Event
import io.cheddarswallet.tonkit.models.EventSyncState
import io.cheddarswallet.tonkit.models.JettonBalance
import io.cheddarswallet.tonkit.models.Tag

@Database(
    entities = [
        Account::class,
        JettonBalance::class,
        Event::class,
        EventSyncState::class,
        Tag::class,
    ],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class KitDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun jettonDao(): JettonDao
    abstract fun eventDao(): EventDao

    companion object {
        fun getInstance(context: Context, name: String): KitDatabase {
            return Room
                .databaseBuilder(context, KitDatabase::class.java, name)
                .allowMainThreadQueries()
                .build()
        }
    }
}
