package com.moop.gamerguides.adapter.model

class Videos {
    var id: String = ""
    var uid: String? = ""
    var title: String = ""
    var description: String = ""
    var image: String = ""
    var video: String = ""

    constructor(title: String, image: String, description: String, video: String, uid: String, id: String) {
        this.title = title
        this.image = image
        this.description = description
        this.video = video
        this.uid = uid
        this.id = id
    }

    constructor() {} // needed for firebase
}
