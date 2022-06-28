package com.moop.gamerguides.adapter.model

class Course {
    var title: String? = ""
    var image: String? = ""
    var description: String? = ""
    var gameCategory: String? = ""
    var uid: String? = ""

    constructor(title: String, image: String, description: String, gameCategory: String, uid: String) {
        this.title = title
        this.image = image
        this.description = description
        this.gameCategory = gameCategory
        this.uid = uid
    }

    constructor() {} // needed for firebase
}