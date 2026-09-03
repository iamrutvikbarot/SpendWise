package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun SpendWiseLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_spendwise_logo),
        contentDescription = "SpendWise Logo",
        modifier = modifier
    )
}
