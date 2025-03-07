package `is`.hbv501g.taskermobile.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import `is`.hbv501g.taskermobile.R
import `is`.hbv501g.taskermobile.ui.adapters.UserAdapter
import `is`.hbv501g.taskermobile.ui.viewmodels.UserViewModel

class UserFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_user, container, false)
        // Set up RecyclerView (assume your fragment_user.xml contains RecyclerView with id userRecyclerView)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.userRecyclerView)
        adapter = UserAdapter() // create an instance of your adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        userViewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }
        // Trigger data fetch if needed
        userViewModel.fetchAllUsers()
    }

    companion object {
        fun newInstance() = UserFragment()
    }
}