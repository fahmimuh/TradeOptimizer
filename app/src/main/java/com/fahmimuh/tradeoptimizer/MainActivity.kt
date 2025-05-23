package com.fahmimuh.tradeoptimizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fahmimuh.core_ui.theme.TradeOptimizerAppTheme
import com.fahmimuh.feature_tradeoptimizer.ui.screens.TradeInputScreen
import com.fahmimuh.feature_tradeoptimizer.ui.screens.TradeResultScreen
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

object NavRoutes {
    const val INPUT_SCREEN = "input_screen"
    const val RESULT_SCREEN_BASE = "result_screen"
    const val RESULT_SCREEN_ROUTE = "$RESULT_SCREEN_BASE/{investmentJson}/{countriesJson}?date={date}"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradeOptimizerAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NavRoutes.INPUT_SCREEN) {
        composable(NavRoutes.INPUT_SCREEN) {
            TradeInputScreen(
                onCalculateClicked = { investment, countries, dateForRoute ->
                    val investmentJson = URLEncoder.encode(Gson().toJson(investment), "UTF-8")
                    val countriesJson = URLEncoder.encode(Gson().toJson(countries), "UTF-8")

                    var route = "${NavRoutes.RESULT_SCREEN_BASE}/$investmentJson/$countriesJson"

                    if (dateForRoute != null && dateForRoute.isNotBlank()) {
                        route += "?date=${URLEncoder.encode(dateForRoute, "UTF-8")}"
                    } else {
                        route += "?date="
                    }

                    val baseRoute = "${NavRoutes.RESULT_SCREEN_BASE}/$investmentJson/$countriesJson"
                    val finalRoute = if (dateForRoute != null && dateForRoute.isNotBlank()) {
                        "$baseRoute?date=${URLEncoder.encode(dateForRoute, "UTF-8")}"
                    } else {
                        baseRoute
                    }
                    navController.navigate(finalRoute)
                }
            )
        }
        composable(
            route = NavRoutes.RESULT_SCREEN_ROUTE,
            arguments = listOf(
                navArgument("investmentJson") { type = NavType.StringType },
                navArgument("countriesJson") { type = NavType.StringType },
                navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            TradeResultScreen(navController = navController)
        }
    }
}
