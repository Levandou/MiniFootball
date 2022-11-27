package com.example.operationsystemsthreads

import com.google.android.material.floatingactionbutton.FloatingActionButton

data class FootballObject(
    var view: FloatingActionButton,
    /**
     * 0 - футбольный мяч, 1 - синяя команда, 2 - красная команда
     */
    var typeOfObject: Int
)
