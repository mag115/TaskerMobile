package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentUserBinding
import com.taskermobile.ui.adapters.UserAdapter
import com.taskermobile.ui.main.controllers.UserController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var userController: UserController
    private lateinit var userAdapter: UserAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        userController = UserController(sessionManager, requireActivity().application)
        setupRecyclerView()
        observeUsers()

        userController.fetchAllUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter()
        binding.recyclerView.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            userController.users.collectLatest { users ->
                binding.progressBar.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
                userAdapter.submitList(users)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 