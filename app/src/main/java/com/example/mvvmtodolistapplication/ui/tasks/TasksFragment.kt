package com.example.mvvmtodolistapplication.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmtodolistapplication.R
import com.example.mvvmtodolistapplication.data.SortOrder
import com.example.mvvmtodolistapplication.data.Task
import com.example.mvvmtodolistapplication.databinding.FragmentTasksBinding
import com.example.mvvmtodolistapplication.util.exhaustive
import com.example.mvvmtodolistapplication.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener,
    MenuProvider {

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var searchView:SearchView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)
        val tasksAdapter = TasksAdapter(this)

        binding.apply {
            idRecyclerViewTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]

                    viewModel.onTaskSwiped(task)


                }

            }).attachToRecyclerView(idRecyclerViewTasks)


            idFabAddTasks.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }


        }

       setFragmentResultListener("add_edit_request"){
           _,bundle ->
           val result =bundle.getInt("add_edit_result")
           viewModel.onAddEditResult(result)


       }

        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }






        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TaskViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                viewModel.onUndoDeleteClick(event.task)

                            }.show()
                    }
                    is TaskViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                "New Task",
                                null
                            )
                        findNavController().navigate(action)

                    }
                    is TaskViewModel.TasksEvent.NavigateToEditTaskScreen -> {

                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                "Edit Task",
                                event.task,
                            )
                        findNavController().navigate(action)


                    }
                    is TaskViewModel.TasksEvent.ShowTaskSavedConfirmationMessage ->
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_SHORT).show()
                    TaskViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {

                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment2()
                        findNavController().navigate(action)

                    }
                }.exhaustive

            }
        }


    }


    lateinit var menuHost: MenuHost

    override fun onStart() {
        super.onStart()
        menuHost = requireActivity()

        Log.d("see", "onStart: ")



        menuHost.addMenuProvider(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("see", "onDestroy: ")


    }

    override fun onDestroyView() {
        Log.d("see", "onDestroyView: ")
        super.onDestroyView()

        searchView.setOnQueryTextListener(null)


        menuHost.removeMenuProvider(this)


    }

    override fun onDetach() {
        Log.d("see", "onDetach: ")
        super.onDetach()
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // Add menu items here
        menuInflater.inflate(R.menu.menu_fragment_tasks, menu)
        val searchItem = menu.findItem(R.id.id_action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }
        searchView.onQueryTextChanged {

            viewModel.searchQuery.value = it


        }


        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.id_action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        return when (menuItem.itemId) {
            R.id.id_action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.id_action_sort_by_date_created -> {
                // loadTasks(true)
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.id_action_hide_completed_tasks -> {
                menuItem.isChecked = !menuItem.isChecked
                viewModel.onHideCompletedClick(menuItem.isChecked)
                true

            }
            R.id.id_action_delete_all_completed_tasks -> {
                viewModel.onDeletAllCompletedClick()
                true

            }
            else -> false

        }
    }


}







