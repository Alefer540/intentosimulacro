package com.example.demo

import com.google.gson.Gson

class Error (val CODIGO_ERROR:Int,val MOTIVO:String){
    override fun toString(): String {
        val gson= Gson()
        return gson.toJson(this)
    }
}