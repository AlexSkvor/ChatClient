package org.example

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class App : Application() {
    override fun start(primaryStage: Stage) {
        val root: Parent = FXMLLoader.load(javaClass.getResource("/sample.fxml"))
        primaryStage.title = "Hello world"
        primaryStage.scene = Scene(root, 300.0, 275.0)
        primaryStage.show()
    }
}