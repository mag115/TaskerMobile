package `is`.hbv501g.taskermobile.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import `is`.hbv501g.taskermobile.databinding.FragmentProjectListBinding
import `is`.hbv501g.taskermobile.ui.adapters.ProjectAdapter
import `is`.hbv501g.taskermobile.ui.viewmodels.ProjectViewModel
import kotlinx.coroutines.flow.collect

class ProjectListFragment : Fragment() {

    private var _binding: FragmentProjectListBinding? = null
    private val binding get() = _binding!!
    private lateinit var projectViewModel: ProjectViewModel
    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectViewModel = ViewModelProvider(requireActivity()).get(ProjectViewModel::class.java)

        projectAdapter = ProjectAdapter { project ->
            // TODO: Handle project item click (e.g. navigate to detail)
        }
        binding.projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = projectAdapter
        }

        lifecycleScope.launchWhenStarted {
            projectViewModel.projects.collect { projects ->
                projectAdapter.submitList(projects)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ProjectListFragment()
    }
}