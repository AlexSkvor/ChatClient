package org.example

import javafx.application.Application.launch

class Main{
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
           launch(App::class.java, *args)
        }
    }

}