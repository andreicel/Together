package br.com.noke.twogether.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import br.com.noke.twogether.R
import br.com.noke.twogether.model.Category
import br.com.noke.twogether.model.User
import br.com.noke.twogether.viewmodel.UserViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp



@Composable
fun ListagemScreen(viewModel: UserViewModel, navController: NavHostController) {
    // Obtenção das categorias de usuário e da lista de usuários do ViewModel
    val userCategories by viewModel.userCategories.collectAsState()
    val users by viewModel.users.collectAsState()

    // Estado que armazena todos os usuários
    var filteredUsers by remember { mutableStateOf(emptyList<User>()) }

    // Efeito que busca as categorias de usuário quando a tela é carregada
    LaunchedEffect(Unit) {
        viewModel.fetchUserCategories()
    }

    // Efeito que inicializa a filtragem de usuários quando `users` ou `userCategories` mudam
    LaunchedEffect(users, userCategories) {
        filteredUsers = users.filter { user ->
            user.imagemURL.isNotBlank() && user.categories.map { it.displayName }
                .intersect(userCategories.toSet()).isNotEmpty()
        }
    }
    Column (modifier = Modifier.padding(start = 8.dp)) {
        Logo()

        // Componente de busca avançada
        AdvancedSearch(users, userCategories) { filtered ->
            filteredUsers = filtered
        }
        Text( text = "#Topics primários:")
        // Exibe as categorias selecionadas
        if (userCategories.isNotEmpty()) {
            Text(
                text = userCategories.joinToString(", ") { "#${it.replace("#", "").trim()}" },
                modifier = Modifier.padding(bottom = 16.dp),
                fontWeight = FontWeight.Bold
            )
        }

        // Exibe a lista de usuários
        LazyColumn {
            items(filteredUsers) { user ->
                UserItem(user)
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    // Componente para exibir um item de usuário
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(2.dp)
            .background(color = Color.Black)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (user.imagemURL.isNotBlank()) {
            UserImage(imageUrl = user.imagemURL)
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "${user.nome} ${user.sobrenome}")
            Text(text = user.cargo)
        }
    }
}

@Composable
fun UserImage(imageUrl: String) {
    // Componente para exibir a imagem do usuário
    Log.d("UserImage", "Loading image: $imageUrl")
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .clip(CircleShape)
            .height(50.dp)
            .width(50.dp)
            .border(1.dp, color = Color.Black, shape = CircleShape)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedSearch(
    users: List<User>,
    userCategories: List<String>,
    onFilteredUsers: (List<User>) -> Unit
) {
    // Estado para armazenar o texto de busca
    var searchText by remember { mutableStateOf("") }
    // Estado para armazenar as categorias selecionadas
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    // Estado para armazenar os usuários filtrados
    var filteredUsers by remember { mutableStateOf(emptyList<User>()) }
    // Estado para controlar a visibilidade do Card
    var isCardVisible by remember { mutableStateOf(false) }

    // Função para realizar a filtragem dos usuários
    fun filterUsers() {
        filteredUsers = if (searchText.isEmpty() && selectedCategories.isEmpty()) {
            // Filtra por categoria e imageURL quando não há busca ativa
            users.filter { user ->
                user.imagemURL.isNotBlank() && user.categories.map { it.displayName }
                    .intersect(userCategories.toSet()).isNotEmpty()
            }
        } else {
            // Filtra por busca avançada
            users.filter { user ->
                val matchesCategory =
                    selectedCategories.isEmpty() || user.categories.map { it.name }
                        .intersect(selectedCategories).isNotEmpty()
                val matchesSearch = searchText.isEmpty() || user.nome.contains(
                    searchText,
                    ignoreCase = true
                ) || user.sobrenome.contains(searchText, ignoreCase = true)
                val hasImage = user.imagemURL.isNotBlank()
                matchesCategory && matchesSearch && hasImage
            }
        }
        onFilteredUsers(filteredUsers)
    }

    // Efeito que chama a função de filtragem quando `users`, `selectedCategories` ou `searchText` mudam
    LaunchedEffect(users, selectedCategories, searchText) {
        filterUsers()
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Campo de busca
            OutlinedTextField(
                value = searchText,
                onValueChange = { newValue ->
                    searchText = newValue
                    filterUsers()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 6.dp),
                placeholder = { Text("Search...") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        modifier = Modifier.clickable {
                            filterUsers()
                        }
                    )
                },
                singleLine = true
            )
            // Seta para abrir/fechar o Card
            IconButton(onClick = { isCardVisible = !isCardVisible }) {
                Icon(
                    imageVector = if (isCardVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isCardVisible) "Collapse" else "Expand"
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Card que aparece quando isCardVisible é verdadeiro
        if (isCardVisible) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp)
                    .border(3.dp, color = Color.Black, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(Color.White),

            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    // FlowRow para exibir as categorias
                    FlowRow(

                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Category.values().forEach { category ->
                            val isSelected = selectedCategories.contains(category.name)
                            val backgroundColor = if (isSelected) Color(0xFF03A9F4) else Color(0xFFE6E6E6)

                            Button(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                colors = ButtonDefaults.buttonColors(backgroundColor),
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomEnd = 4.dp,
                                    bottomStart = 4.dp
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 6.dp,  // Reduz o padding horizontal
                                    vertical = 4.dp     // Reduz o padding vertical
                                ),
                                onClick = {
                                    selectedCategories = if (isSelected) {
                                        selectedCategories - category.name
                                    } else {
                                        selectedCategories + category.name
                                    }
                                    filterUsers()
                                }
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(2.dp),
                                    text = "#${category.name}",
                                    color = if (isSelected) Color.White else Color.DarkGray,
                                )
//                                    size = 10.dp
                            }
                        }
                    }
                }
            }
        }
    }
}
