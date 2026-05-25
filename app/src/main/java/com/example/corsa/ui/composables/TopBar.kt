package com.example.corsa.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar( navController: NavController)  {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp, vertical = 12.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        IconButton(onClick = {  }) {
//            Icon(
//                imageVector = Icons.Filled.Menu,
//                contentDescription = "Menu",
//                tint = cs.onBackground,
//                modifier = Modifier.size(26.dp),
//            )
//        }
//
//
//
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .clip(CircleShape)
//                .background(cs.secondary)
//                .clickable(onClick = {  }),
//            contentAlignment = Alignment.Center,
//        ) {
//            Icon(
//                imageVector = Icons.Filled.Person,
//                contentDescription = "Profile",
//                tint = cs.onBackground,
//                modifier = Modifier.size(22.dp),
//            )
//        }
//    }
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Korsa",
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 28.sp,
                letterSpacing = 2.sp,
            )
        },

        navigationIcon = {
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    )
}