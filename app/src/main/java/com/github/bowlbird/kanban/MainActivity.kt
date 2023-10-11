package com.github.bowlbird.kanban

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.bowlbird.kanban.ui.theme.KanbanTheme
import kotlinx.coroutines.runBlocking

class KanbanApplication : Application() {
    lateinit var appRepository: AppRepository

    override fun onCreate() {
        super.onCreate()
        appRepository = AppRepository(
            AppDataSource(
                this
            )
        )
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KanbanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Layout()
                }
            }
        }
    }
}

@Composable
fun Layout(modifier: Modifier = Modifier) = Box(modifier, contentAlignment = Alignment.Center) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "board") {
        composable("board") {Board(navController = navController)}
        composable("add/{to}") {
            Add(to = it.arguments?.getString("to") ?: "todo", navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Add(modifier: Modifier = Modifier, to: String, navController: NavController) = Box(modifier, contentAlignment = Alignment.Center) {
    val addViewModel: AddViewModel = viewModel(factory = AddViewModel.Factory)
    val addUiState = addViewModel.addUiState.collectAsState()

    Column(Modifier.fillMaxSize(.7f)) {
        when (to) {
            "todo" -> {
                Text("Create new entry for TODO")
            }

            "doing" -> {
                Text("Create new entry for DOING")
            }

            "done" -> {
                Text("Create new entry for DONE")
            }
        }
        TextField(value = addUiState.value.text, singleLine = true, onValueChange = {
             addViewModel.updateAddUiState(
                 addUiState.value.copy(text = it)
             )
        }, placeholder = {Text("Name...")})
        Button(onClick = {
            val entry = Entry(text = addUiState.value.text)
            runBlocking {
                addViewModel.addEntry(
                    when (to) {
                        "todo" -> ListType.Todo
                        "doing" -> ListType.Doing
                        "done" -> ListType.Done
                        else -> ListType.Todo
                    }, entry
                )
            }
            navController.navigate("board")
        }) {
            Text("Add")
        }
    }

}

@Composable
fun Board(modifier: Modifier = Modifier, navController: NavController) = Column(modifier) {
    val viewModel: KanbanViewModel = viewModel(factory = KanbanViewModel.Factory)
    val (todo, doing, done) = viewModel.kanbanUiState.collectAsState().value

    EntryColumn(Modifier.weight(1f), title = "TODO", todo, navController, "todo", ListType.Todo)
    EntryColumn(Modifier.weight(1f), title = "DOING", doing, navController, "doing", ListType.Doing)
    EntryColumn(Modifier.weight(1f), title = "DONE", done, navController, "done", ListType.Done)
}

@Composable
fun EntryColumn(modifier: Modifier = Modifier, title: String, entries: List<Entry>, navController: NavController, to: String, listType: ListType) =
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        
    val viewModel: KanbanViewModel = viewModel(factory = KanbanViewModel.Factory)


    Text(title)
    Column(
        Modifier
            .padding(10.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())) {

        entries.forEach {
            Entry(modifier = Modifier
                .padding(5.dp)
                .height(40.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.background),
                text = it.text,
                onDelete = {
                    runBlocking {
                        viewModel.deleteEntry(listType, it)
                    }
                },
                buttons = listOf(
                    Pair(ListType.Todo.name) {
                        runBlocking {
                            viewModel.deleteEntry(listType, it)
                            viewModel.addEntry(ListType.Todo, it)
                            Unit
                        }
                    },
                    Pair(ListType.Doing.name) {
                        runBlocking {
                            viewModel.deleteEntry(listType, it)
                            viewModel.addEntry(ListType.Doing, it)
                            Unit
                        }
                    },
                    Pair(ListType.Done.name) {
                        runBlocking {
                            viewModel.deleteEntry(listType, it)
                            viewModel.addEntry(ListType.Done, it)
                            Unit
                        }
                    }
                ).filter {pair -> pair.first != listType.name}
            )
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(onClick = {
                navController.navigate("add/$to")
            }) {
                Text("Add")
            }
        }

    }
}

@Composable
fun Entry(modifier: Modifier = Modifier, text: String, onDelete: () -> Unit, buttons: List<Pair<String, () -> Unit>>) {
    var opened by remember { mutableStateOf(false) }
    Box(modifier
        .fillMaxSize()
        .clickable {
            opened = !opened
        }
    ) {
        if (!opened) {
            ConstraintLayout(Modifier.fillMaxSize()) {
                val (textComposable, button) = createRefs()
                Text(
                    modifier = Modifier
                        .padding(5.dp)
                        .constrainAs(textComposable) {
                            start.linkTo(parent.start)
                            centerVerticallyTo(parent)
                        },
                    text = text
                )
                Button(
                    modifier = Modifier
                        .padding(5.dp)
                        .constrainAs(button) {
                            end.linkTo(parent.end)
                        },
                    onClick = onDelete,
                    contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text("Delete")
                }
            }
        } else {
            ConstraintLayout(Modifier.fillMaxSize()) {
                val (textComposable, row) = createRefs()

                Text(modifier = Modifier
                    .padding(5.dp)
                    .constrainAs(textComposable) {
                        start.linkTo(parent.start)
                        centerVerticallyTo(parent)
                    },
                    text = "Move To:"
                )
                Row(Modifier
                    .fillMaxWidth(.6f)
                    .constrainAs(row) {
                        end.linkTo(parent.end)
                    },
                    horizontalArrangement = Arrangement.End
                ) {
                    repeat(buttons.size) {
                        Button(
                            modifier = Modifier.padding(5.dp),
                            onClick = buttons[it].second,
                            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp)
                        ) {
                            Text(buttons[it].first)
                        }
                    }
                }
            }
        }
    }
}