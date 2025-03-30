package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentMyProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher

class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageViewAvatar.setImageURI(it)

                // Persist the selected image URI
                lifecycleScope.launch {
                    sessionManager.saveProfilePictureUri(it.toString())
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Collect user info from DataStore
        lifecycleScope.launch {
            sessionManager.username.collectLatest { username ->
                binding.textViewUsername.text = username ?: "Unknown User"
            }
        }

        lifecycleScope.launch {
            sessionManager.role.collectLatest { role ->
                binding.textViewEmail.text = role ?: "No role found"
            }
        }
        binding.attachPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        lifecycleScope.launch {
            sessionManager.profilePictureUri.collectLatest { uriString ->
                uriString?.let {
                    val uri = Uri.parse(it)
                    binding.imageViewAvatar.setImageURI(uri)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}