package com.app.ripple.presentation.screen.home

import android.icu.text.CaseMap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.ripple.presentation.screen.active_users.ActiveUsersScreen
import com.app.ripple.presentation.screen.inbox.InboxScreen
import com.app.ripple.presentation.shared.BottomTitledIcon
import com.app.ripple.presentation.shared.RippleLogo
import com.app.ripple.presentation.ui.theme.DarkBG
import com.app.ripple.presentation.ui.theme.MontserratFamily
import com.app.ripple.presentation.ui.theme.SecondaryDarkBG

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {

    var activeTab by remember {
        mutableStateOf(HomeTab.CHATS)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = DarkBG),
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeScreenHeader(
                title = activeTab.title
            )

            if (activeTab == HomeTab.CHATS){
                InboxScreen(
                    navController = navController
                )
            }
            else{
                ActiveUsersScreen()
            }
        }

        HomeScreenNavigationBottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .padding(horizontal = 20.dp),
            activeHomeTab = activeTab,
            onTabClick = {
                activeTab = it
            }
        )
    }
}

@Composable
fun HomeScreenHeader(
    title: String = "RIPPLE"
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RippleLogo(size = 40.dp)

        Spacer(modifier = Modifier.width(15.dp))

        Text(
            text = title,
            fontFamily = MontserratFamily,
            fontSize = 30.sp,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun HomeScreenNavigationBottomBar(
    modifier: Modifier = Modifier,
    activeHomeTab: HomeTab = HomeTab.CHATS,
    onTabClick: (HomeTab) -> Unit = {}
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.White,
                spotColor = Color.White
            )
            .clip(shape = RoundedCornerShape(30.dp))
            .background(color = SecondaryDarkBG)
            .padding(10.dp)
            .padding(horizontal = 30.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        BottomTitledIcon(
            modifier = Modifier
                .scale(if (activeHomeTab == HomeTab.CHATS) 1.2f else 1f)
                .clickable {
                    onTabClick(HomeTab.CHATS)
                },
            title = "Chats",
            imageVector = Icons.Rounded.Forum,
            iconSize = 25.dp,
            fontSize = 15.sp,
            color = if (activeHomeTab == HomeTab.CHATS) Color.White else Color.Gray
        )

        Spacer(modifier = Modifier.width(50.dp))
        Spacer(modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(color = Color.Gray))
        Spacer(modifier = Modifier.width(50.dp))

        BottomTitledIcon(
            modifier = Modifier
                .scale(if (activeHomeTab == HomeTab.ACTIVE) 1.2f else 1f)
                .clickable {
                    onTabClick(HomeTab.ACTIVE)
                },
            title = "Actives",
            imageVector = Icons.Rounded.Groups,
            iconSize = 25.dp,
            fontSize = 15.sp,
            color = if (activeHomeTab == HomeTab.ACTIVE) Color.White else Color.Gray
        )
    }
}

enum class HomeTab(val title : String) {
    CHATS(title = "Chats"),
    ACTIVE(title = "Actives")
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}