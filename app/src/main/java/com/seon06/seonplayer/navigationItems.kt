package com.seon06.seonplayer

sealed class navigationItems(val route : String){
    object home : navigationItems("home_route")
    object playlist : navigationItems("playlist_route")
    object search : navigationItems("search_route")
    object profile : navigationItems("profile_route")
}