package com.example.demo

import com.google.gson.Gson

class Lista {
     companion object {
         var list = mutableListOf<Any>()
     }
     override fun toString(): String {
         val gson= Gson()
         return gson.toJson(this)
     }
}