package com.taskermobile.ui.main.fragments

import android.content.Intent
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
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.taskermobile.R
import kotlinx.coroutines.flow.first
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var database: TaskerDatabase
    private var userId: Long? = null

    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    // Take a persistent URI permission to keep access after restart
                    requireContext().contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    binding.imageViewAvatar.setImageURI(it)

                    // Save the image URI to the Room database
                    lifecycleScope.launch(Dispatchers.IO) {
                        userId?.let { id ->
                            database.userDao().updateUserProfileImage(id, it.toString())
                            Log.d("MyProfileFragment", "Profile image URI saved to database: $it")
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("MyProfileFragment", "Failed to take persistent URI permission", e)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        database = TaskerDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            sessionManager.username.collectLatest { username ->
                Log.d("MyProfileFragment", "Received username from DataStore: $username")
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
            // Get user ID from session manager
            userId = sessionManager.userId.first()
            
            userId?.let { id ->
                // Log the current user data for debugging
                withContext(Dispatchers.IO) {
                    val userEntity = database.userDao().getUserById(id)
                    Log.d("MyProfileFragment", "User from database: ${userEntity?.username}, imageUri=${userEntity?.imageUri}")
                }
                
                // Load user data including profile image from database
                withContext(Dispatchers.IO) {
                    val userEntity = database.userDao().getUserById(id)
                    withContext(Dispatchers.Main) {
                        userEntity?.let { user ->
                            if (!user.imageUri.isNullOrEmpty()) {
                                try {
                                    val uri = Uri.parse(user.imageUri)
                                    binding.imageViewAvatar.setImageURI(uri)
                                    Log.d("MyProfileFragment", "Set profile image from URI: ${user.imageUri}")
                                } catch (e: SecurityException) {
                                    e.printStackTrace()
                                    binding.imageViewAvatar.setImageResource(R.drawable.ic_person)
                                    Log.e("MyProfileFragment", "Failed to set image from URI: ${user.imageUri}", e)
                                }
                            } else {
                                binding.imageViewAvatar.setImageResource(R.drawable.ic_person)
                                Log.d("MyProfileFragment", "No image URI found, using default image")
                            }
                        }
                    }
                }
            } ?: run {
                Log.e("MyProfileFragment", "No user ID found in session")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}