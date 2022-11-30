package com.example.mvvmtodolistapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)


    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    fun getTasks(query:String,
                 sortOrder: SortOrder,
                 hideCompleted: Boolean): Flow<List<Task>> =
        when(sortOrder){
            SortOrder.BY_DATE->getTasksSortedByDateCreated(query,hideCompleted)
            SortOrder.BY_NAME-> getTasksSortedByName(query,hideCompleted)
        }


    @Query(value = "select * from task_table where (completed != :hideCompleted or completed = 0) and name like '%' || :searchQuery || '%' order by important desc," +
            " name")
    fun getTasksSortedByName(searchQuery:String, hideCompleted:Boolean): Flow<List<Task>>

    @Query(value = "select * from task_table where (completed != :hideCompleted or completed = 0) and name like '%' || :searchQuery || '%' order by important desc," +
            " created")
    fun getTasksSortedByDateCreated(searchQuery:String,hideCompleted:Boolean): Flow<List<Task>>


    @Query(value="delete from task_table where completed=1")
    suspend fun deleteCompletedTasks()

}