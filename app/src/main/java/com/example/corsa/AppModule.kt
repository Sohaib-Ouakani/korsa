package com.example.corsa

import com.example.corsa.data.remote.supabase
import com.example.corsa.data.repositories.FakeProfileRepository
import com.example.corsa.data.repositories.FakeRunsRepository
import com.example.corsa.data.repositories.ProfileRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.screens.friends.FriendsViewModel
import com.example.corsa.ui.screens.home.HomeViewModel
import com.example.corsa.ui.screens.logintester.LoginTesterViewModel
import com.example.corsa.ui.screens.profiledetail.ProfileDetailViewModel
import com.example.corsa.ui.screens.rundetail.RunDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { supabase }

    single<RunsRepository> { FakeRunsRepository() } // TODO: make sure to change this

    single<ProfileRepository> { FakeProfileRepository() } // TODO: make sure to change this

    viewModel { LoginTesterViewModel(get()) }

    viewModel { HomeViewModel() }

    viewModel { FriendsViewModel() }

    viewModel { params -> RunDetailViewModel(get(), params.get()) }

    viewModel { ProfileDetailViewModel(get(), get(), get()) }
}