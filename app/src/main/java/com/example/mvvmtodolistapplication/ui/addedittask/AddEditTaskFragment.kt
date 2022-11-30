package com.example.mvvmtodolistapplication.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mvvmtodolistapplication.R
import com.example.mvvmtodolistapplication.databinding.FragmentAddEditTaskBinding
import com.example.mvvmtodolistapplication.databinding.FragmentTasksBinding
import com.example.mvvmtodolistapplication.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class AddEditTaskFragment:Fragment(R.layout.fragment_add_edit_task) {


    private val viewModel:AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {

            idEditTaskName.setText(viewModel.taskName)
            idCheckBoxImportant.isChecked = viewModel.taskImportance
            idCheckBoxImportant.jumpDrawablesToCurrentState()
            idTextViewDateCreated.isVisible = viewModel.task!=null
            idTextViewDateCreated.text =  "Created: ${viewModel.task?.createdDateFormatted}"


            idEditTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            idCheckBoxImportant.setOnCheckedChangeListener { _,isChecked->
                viewModel.taskImportance = isChecked
            }
            idFavSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }





        }

      viewLifecycleOwner.lifecycleScope.launchWhenStarted {
          viewModel.addEditTaskEvent.collect{
              event ->
              when(event){
                  is AddEditTaskViewModel.AddEditTaskEvent.NaviagateBackWithResult -> {

                      binding.idEditTaskName.clearFocus()
                      setFragmentResult("add_edit_request",
                      bundleOf("add_edit_result" to event.result))
                      findNavController().popBackStack()


                  }
                  is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {

                      Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()

                  }
              }.exhaustive
          }
      }



    }







}