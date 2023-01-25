package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.repository.FILTER_PARAM

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        val adapter = AsteroidListAdapter(AsteroidClickListener { asteroidId ->
            val selectedAsteroid = viewModel.onAsteroidClicked(asteroidId)
            this.findNavController().navigate(MainFragmentDirections.actionShowDetail(selectedAsteroid!!))
        })
        binding.asteroidRecycler.adapter = adapter

        viewModel.pictureOfDayList.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                binding.picOfDay = it[0]
            } else binding.picOfDay = PictureOfDay("", "", "")
        })

        viewModel.asteroids.observe(viewLifecycleOwner, Observer{
            it?.let{
                adapter.submitList(it)
                binding.statusLoadingWheel.visibility = View.GONE
            }
        })

        viewModel.listStatus.observe(viewLifecycleOwner, Observer{
            when(it){
                NasaApiStatus.LOADING -> binding.statusLoadingWheel.visibility = View.VISIBLE
                else -> binding.statusLoadingWheel.visibility = View.GONE
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            getString(R.string.next_week_asteroids) -> viewModel.filterData(FILTER_PARAM.WEEK)
            getString(R.string.today_asteroids)-> viewModel.filterData(FILTER_PARAM.TODAY)
            getString(R.string.saved_asteroids) -> viewModel.filterData(FILTER_PARAM.ALL_DATA)
        }
        return true
    }
}
