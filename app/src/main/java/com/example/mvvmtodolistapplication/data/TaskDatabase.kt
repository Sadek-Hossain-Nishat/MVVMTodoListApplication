package com.example.mvvmtodolistapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mvvmtodolistapplication.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities =[Task::class], version = 1)
abstract class TaskDatabase:RoomDatabase() {

    abstract fun taskDao():TaskDao

    class Callback @Inject constructor(
      private  val database: Provider<TaskDatabase>,
      @ApplicationScope private val applicationScope: CoroutineScope
    ):RoomDatabase.Callback(){

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // db operation
           val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Wash the dishes"))
                dao.insert(Task("go to laundry"))
                dao.insert(Task("buy groceries",important = true))
                dao.insert(Task("prepare food", completed = true))
                dao.insert(Task("call mom"))
                dao.insert(Task("visit grandma", completed = true))
                dao.insert(Task("repair my bike"))
                dao.insert(Task("must wear musk"))

            }




        }

    }





}