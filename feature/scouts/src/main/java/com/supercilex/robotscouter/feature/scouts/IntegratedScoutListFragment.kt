package com.supercilex.robotscouter.feature.scouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postDelayed
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.AppBarLayout
import com.supercilex.robotscouter.Bridge
import com.supercilex.robotscouter.IntegratedScoutListFragmentCompanion
import com.supercilex.robotscouter.ScoutListFragmentCompanionBase.Companion.TAG
import com.supercilex.robotscouter.TeamSelectionListener
import com.supercilex.robotscouter.core.data.mainHandler
import com.supercilex.robotscouter.core.ui.isInTabletMode
import com.supercilex.robotscouter.core.ui.transitionAnimationDuration
import com.supercilex.robotscouter.core.unsafeLazy
import org.jetbrains.anko.find
import com.supercilex.robotscouter.R as RC

@Bridge
internal class IntegratedScoutListFragment : ScoutListFragmentBase() {
    private val appBar by unsafeLazy { requireActivity().find<AppBarLayout>(RC.id.appBar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (requireContext().isInTabletMode()) {
            val listener = context as TeamSelectionListener
            val bundle = bundle
            mainHandler.post { listener.onTeamSelected(bundle) }

            removeFragment()
        } else if (sharedElementEnterTransition != null) {
            postponeEnterTransition()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.activity_scout_list, container, false)
        root.find<FrameLayout>(R.id.scoutList).addView(
                inflater.inflate(R.layout.fragment_scout_list, container, false))
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (sharedElementEnterTransition == null) {
            mainHandler.post { appBar.setExpanded(false) }
        }
    }

    override fun startPostponedEnterTransition() {
        super.startPostponedEnterTransition()
        appBar.postDelayed(transitionAnimationDuration) {
            appBar.setExpanded(false)
        }
    }

    override fun newViewModel(savedInstanceState: Bundle?) = AppBarViewHolderBase(
            this, savedInstanceState, dataHolder.teamListener, onScoutingReadyTask.task)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity as AppCompatActivity
        viewHolder.toolbar.apply {
            navigationIcon = checkNotNull(activity.drawerToggleDelegate).themeUpIndicator
            setNavigationOnClickListener { requireFragmentManager().popBackStack() }
            setOnMenuItemClickListener {
                forceRecursiveMenuItemSelection(it) || onOptionsItemSelected(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.integrated_scout_list_menu, viewHolder.toolbar.menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_move_window) {
            val bundle = bundle
            removeFragment()
            startActivity(ScoutListActivity.createIntent(bundle))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appBar.setExpanded(true)
        sharedElementEnterTransition = null
    }

    override fun onStop() {
        // This has to be done in onStop so fragment transactions from the view pager can be
        // committed. Only reset the adapter if the user is switching destinations.
        if (isDetached) pagerAdapter?.reset()
        super.onStop()
    }

    override fun onTeamDeleted() = removeFragment()

    private fun removeFragment() {
        requireFragmentManager().popBackStack()
    }

    companion object : IntegratedScoutListFragmentCompanion {
        override fun newInstance(args: Bundle) =
                IntegratedScoutListFragment().apply { arguments = args }

        override fun getInstance(manager: FragmentManager) =
                manager.findFragmentByTag(TAG) as? IntegratedScoutListFragment
    }
}