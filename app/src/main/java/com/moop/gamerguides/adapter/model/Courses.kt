package com.moop.gamerguides.adapter.model

class Courses {
    var title: String? = ""
    var image: String? = ""
    var description: String? = ""
    var gameCategory: String? = ""
    var uid: String? = ""
    var id: String? = ""

    constructor(title: String, image: String, description: String, gameCategory: String, uid: String, id: String) {
        this.title = title
        this.image = image
        this.description = description
        this.gameCategory = gameCategory
        this.uid = uid
        this.id = id
    }

    constructor() {} // needed for firebase
}