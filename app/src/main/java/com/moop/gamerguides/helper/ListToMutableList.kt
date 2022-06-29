package com.moop.gamerguides.helper

object ListToMutableList {
    fun removeElement(list: List<String>, str: String?): List<String> {
        val mutableList = list.toMutableList()
        mutableList.remove(str)

        return mutableList.toList()
    }

    fun addElement(list: List<String?>, str: String?): List<String?> {
        return if (list.isEmpty()) {
            listOf(str!!)
        }
        else {
            val mutableList = list.toMutableList()
            mutableList.add(str!!)

            mutableList.toList()
        }
    }
}