package com.example.doomappexamenfinal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.doomappexamenfinal.ui.theme.DoomAppExamenFinalTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoomAppExamenFinalTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("demons") { DemonsScreen(navController) }
                        composable(
                            "demonDetail/{demonDetailKey}",
                            arguments = listOf(navArgument("demonDetailKey") { type = NavType.StringType })
                        ) { key ->
                            key.arguments?.getString("demonDetailKey")?.let { DemonDetailsView(it) }
                        }
                        composable("weapons") { WeaponsScreen(navController) }
                        composable(
                            "weaponDetail/{weaponDetailKey}",
                            arguments = listOf(navArgument("weaponDetailKey") { type = NavType.StringType })
                        ) { key ->
                            key.arguments?.getString("weaponDetailKey")?.let { WeaponDetailsView(it) }
                        }
                    }
                }
            }
        }
    }
}

val eternal_ui_family = FontFamily(
    Font(R.font.eternal_ui),
)

//region API
interface ApiService {
    @GET("/demons")
    suspend fun getDemons(): List<String>

    @GET("/demons/{demonName}")
    suspend fun getDemonDetails(@Path("demonName") demonName: String): DemonDetails

    @GET("/weapons")
    suspend fun getWeapons(): List<String>

    @GET("/weapons/{weaponName}")
    suspend fun getWeaponDetails(@Path("weaponName") weaponName: String): WeaponDetails
}

class WeaponDetails (
    val name: String,
    val damage: String,
    val fire_mode: String,
    val location: String,
    val weapon_type: String,
    val ammo_type: String,
    val image: String,
)

data class DemonDetails(
    val name: String,
    val description: String,
    val hp: String,
    val rank: String,
    val speed: String,
    val image: String,
)

object ApiClient {
    private const val BASE_URL = "http://172.18.68.39"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
//endregion

//region Home
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionButton("Demons") { navController.navigate("demons") }
            SectionButton("Weapons") { navController.navigate("weapons") }
        }
    }
}

@Composable
fun SectionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onClick() }
            .paint(
                painterResource(id = R.drawable.button_background),
                contentScale = ContentScale.Fit
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = eternal_ui_family
        )
    }
}
//endregion

//region Demons
@Composable
fun DemonsScreen(navController: NavController) {
    val demons = remember { mutableStateOf<List<String>?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                demons.value = ApiClient.apiService.getDemons()
            } catch (e: Exception) {
                // Handle error
                Log.d("!!!!!!!!!!!!!!!!!!!", e.toString())
                demons.value = listOf("Failed to load demons")
            }
        }
    }

    when (val demonsKeys = demons.value) {
        null -> Text(
            text = "Loading...",
            color = Color.White,
            fontFamily = eternal_ui_family,
        )
        else -> LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth(),
        ) {
            items(demonsKeys.size) { demonInt->
                val demonKey = demonsKeys[demonInt]
                val demonDetails = remember { mutableStateOf<DemonDetails?>(null) }

                LaunchedEffect(Unit) {
                    scope.launch {
                        demonDetails.value = ApiClient.apiService.getDemonDetails(demonKey)
                    }
                }
                demonDetails.value?.let { DemonsScreenItem(it, navController) }
            }
        }
    }
}

@Composable
fun DemonsScreenItem(demonDetails: DemonDetails, navController: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .padding(vertical = 15.dp)
            .width(300.dp)
            .background(Color.Black)
            .clickable { navController.navigate("demonDetail/${demonDetails.name.lowercase().replace(" ", "_")}") }, ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(60.dp)
                .width(60.dp)
                .paint(
                    painterResource(id = R.drawable.image_frame),
                    contentScale = ContentScale.FillBounds,
                ),
        ) {
            AsyncImage(
                model = demonDetails.image,
                contentDescription = "Image of the weapon",
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .fillMaxSize(),
            )
        }
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        Text(
            text = demonDetails.name,
            color = Color.White,
            fontFamily = eternal_ui_family,
        )
    }
}

@Composable
fun DemonDetailsView(demonKey: String) {
    val demonDetailsMutable = remember { mutableStateOf<DemonDetails?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            demonDetailsMutable.value = ApiClient.apiService.getDemonDetails(demonKey)
        }
    }

    when (val demonDetail = demonDetailsMutable.value) {
        null -> Text(
            text = "Loading...",
            color = Color.White,
            fontFamily = eternal_ui_family,
        )
        else -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(Color.Black)
                .fillMaxSize(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .paint(
                            painterResource(id = R.drawable.image_frame),
                            contentScale = ContentScale.Fit,
                        ),
                ) {
                    AsyncImage(
                        model = demonDetail.image,
                        contentDescription = "Image of the demon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(10.dp)
                            .height(200.dp)
                            .fillMaxSize(),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .width(400.dp),
                ) {
                    DemonLabel("Name:", demonDetail.name)
                    DemonLabel("Health Points:", demonDetail.hp)
                    DemonLabel("Speed:", demonDetail.speed)
                    DemonLabel("Description:", demonDetail.description)
                    DemonLabel("Rank:", demonDetail.rank)
                }
            }
        }
    }
}

@Composable
fun DemonLabel(name: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            color = Color.White,
            fontFamily = eternal_ui_family,
            textAlign = TextAlign.End,
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        Text(
            text = value,
            color = Color.White,
            fontFamily = eternal_ui_family,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(250.dp),
        )
    }
}
//endregion
@Composable
fun WeaponsScreen(navController: NavController) {
    val weapons = remember { mutableStateOf<List<String>?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                weapons.value = ApiClient.apiService.getWeapons()
            } catch (e: Exception) {
                // Handle error
                Log.d("!!!!!!!!!!!!!!!!!!!", e.toString())
                weapons.value = listOf("Failed to load weapons")
            }
        }
    }

    when (val weaponKeys = weapons.value) {
        null -> Text(
            text = "Loading...",
            color = Color.White,
            fontFamily = eternal_ui_family,
        )
        else -> LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth(),
        ) {
            items(weaponKeys.size) { weaponInt ->
                val weaponKey = weaponKeys[weaponInt]
                val weaponDetails = remember { mutableStateOf<WeaponDetails?>(null) }

                LaunchedEffect(Unit) {
                    scope.launch {
                        weaponDetails.value = ApiClient.apiService.getWeaponDetails(weaponKey)
                    }
                }
                weaponDetails.value?.let { WeaponsScreenItem(it, navController) }
            }
        }
    }
}

@Composable
fun WeaponLabel(name: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            color = Color.White,
            fontFamily = eternal_ui_family,
            textAlign = TextAlign.End,
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        Text(
            text = value,
            color = Color.White,
            fontFamily = eternal_ui_family,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(250.dp),
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DoomAppExamenFinalTheme {
//        DemonsScreenItem("Soldier_(Blaster)", "https://static.wikia.nocookie.net/doom/images/1/1c/Arachnotron_DE.png/revision/latest?cb=20220914201033", navController = NavController())
    }
}
