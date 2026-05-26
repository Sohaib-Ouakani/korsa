package com.example.corsa

import com.example.corsa.data.remote.supabase
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.logintester.LoginTesterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { supabase }

    viewModel { LoginTesterViewModel(get()) }

    viewModel { HomeViewModel() }
}