package com.joeho.pokedexapp.ui.tabB

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.joeho.pokedex.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TabBFragment : Fragment(R.layout.fragment_tab_b) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 收藏/最近瀏覽功能可延伸這裡
    }
}
