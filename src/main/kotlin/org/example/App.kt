package org.example

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class App : Application() {

    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("/sample.fxml"))
        openEmptyWindowWitTitle(loader, primaryStage)
        setIcon(primaryStage)
        primaryStage.setOnCloseRequest {
            val controller = loader.getController<Controller>()
            controller.clear()
        }
    }


    private fun openEmptyWindowWitTitle(loader: FXMLLoader, stage: Stage) {
        val root: Parent = loader.load()
        stage.maxHeight = 682.0
        stage.minHeight = 682.0
        stage.minWidth = 917.0
        stage.maxWidth = 917.0
        stage.title = "Чат Скворцова"
        stage.scene = Scene(root)
        stage.show()
    }

    private fun setIcon(stage: Stage) {
        val iconStream = javaClass.getResourceAsStream("/icon.png")
        val image = Image(iconStream)
        stage.icons.add(image)
    }
}