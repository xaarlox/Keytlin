package com.xaarlox.keytlin.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

fun NavController.navigate(route: Route, builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route.path, builder)
}

fun NavController.navigateToTab(route: Route) {
    navigate(route.path) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}