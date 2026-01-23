package com.restock.core.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.restock.core.data.database.RestockDatabase
import com.restock.core.data.database.entity.ItemEntity
import org.junit.After
import org.junit.Before

/**
 * Base class for DAO instrumented tests providing in-memory database setup.
 */
abstract class BaseDaoTest {
    protected lateinit var database: RestockDatabase
    protected lateinit var itemDao: ItemDao
    protected lateinit var currentListDao: CurrentListDao
    protected lateinit var purchaseDao: PurchaseDao
    protected lateinit var patternDao: PatternDao
    protected lateinit var mealTagDao: MealTagDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, RestockDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        itemDao = database.itemDao()
        currentListDao = database.currentListDao()
        purchaseDao = database.purchaseDao()
        patternDao = database.patternDao()
        mealTagDao = database.mealTagDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    /**
     * Helper to create a test item entity.
     */
    protected fun createTestItem(
        id: String = TEST_ITEM_ID,
        name: String = TEST_ITEM_NAME,
        normalizedName: String = TEST_NORMALIZED_NAME,
        createdAt: Long = TEST_CREATED_AT,
        isActive: Boolean = true,
    ) = ItemEntity(
        id = id,
        name = name,
        normalizedName = normalizedName,
        createdAt = createdAt,
        isActive = isActive,
    )

    companion object {
        const val TEST_ITEM_ID = "test-item-1"
        const val TEST_ITEM_ID_2 = "test-item-2"
        const val TEST_ITEM_ID_3 = "test-item-3"
        const val TEST_ITEM_NAME = "Milk"
        const val TEST_NORMALIZED_NAME = "milk"
        const val TEST_CREATED_AT = 1700000000000L
    }
}
