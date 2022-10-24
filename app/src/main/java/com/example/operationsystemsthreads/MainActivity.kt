package com.example.operationsystemsthreads

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.operationsystemsthreads.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    val listOfPlayers = mutableListOf<View>()
    var selected: FloatingActionButton? = null
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab1.setOnClickListener { changeStateOfButton(binding.fab1) }
        binding.fab2.setOnClickListener { changeStateOfButton(binding.fab2) }

        addToListPlayers()
        binding.frame.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP)
                selected?.let {
                    moveObjectToNewPosition(motionEvent.x - (it.width / 2), motionEvent.y - (it.height / 2), it, 1000f)
                }
            true
        }
    }

    @SuppressLint("Recycle")
    //maxDistance определякет максимальную дистанцую которую могут проанимровать
    private fun moveObjectToNewPosition(x: Float, y: Float, selected: FloatingActionButton, maxDistance: Float) {
        val dataListNewMoving = mutableListOf<Float>()
        var cordX = selected.x
        var cordY = selected.y

        for (i in listOfPlayers)
            if (i.id != selected.id) {
                val signX = if (selected.x > x) -1f else 1f   //знак x
                val signY = if (selected.y > y) -1f else 1f   //знак y
                val xLenght = abs(x - selected.x)          //определение расстояния между объектами по x
                val yLenght = abs(y - selected.y)          //определение расстояния между объектами по y

                //кладёт шаги учитывая знак где одно из направлений равно 1
                val p = if (xLenght > yLenght)
                    Pair(signX * xLenght / yLenght, signY)
                else Pair(signX, signY * yLenght / xLenght)

                var valueX = 0f
                var valueY = 0f


                var rangeX = cordX..cordX + selected.width
                var rangeY = cordY..cordY + selected.height
                while (((i.x !in rangeX && i.x + i.width !in rangeX) || (i.y !in rangeY && i.y + i.width !in rangeY))
                    && abs(valueX) < xLenght && abs(valueY) < yLenght
                    && maxDistance > sqrt(valueX.pow(2) + valueY.pow(2))
                ) {

                    valueX += p.first
                    valueY += p.second

                    cordX += p.first
                    cordY += p.second
                    rangeX = cordX..cordX + selected.width
                    rangeY = cordY..cordY + selected.height
                }
                //если остановилось из-за припятствия
                if (abs(valueX) <= xLenght && abs(valueY) <= yLenght && maxDistance > sqrt(valueX.pow(2) + valueY.pow(2))) {
                    dataListNewMoving.add(listOfPlayers.indexOf(i).toFloat())
                    cordX + selected.width///найти точку между 2 центрами
                    dataListNewMoving.add(listOfPlayers.indexOf(i).toFloat())
                    dataListNewMoving.add(listOfPlayers.indexOf(i).toFloat())
                    dataListNewMoving.add(listOfPlayers.indexOf(i).toFloat())
                }

                cordX -= p.first
                cordY -= p.second
                break
            }

        val newX = ObjectAnimator.ofFloat(selected, "x", cordX)
        val newY = ObjectAnimator.ofFloat(selected, "y", cordY)
        newX.duration = 500
        newY.duration = 500
        AnimatorSet().apply {
            playTogether(newX, newY)
            start()
        }
    }

    /**
     * remainsMaxDistance - остаток силы после прошлого соприкосновения
     * firstObj - координаты центра первого объекта(который ударяет)
     * secondObj - координаты центра второго объекта (по которому ударяют)
     */
    private fun ricochet(remainsMaxDistance: Float, firstObj: Pair<Float, Float>, secondObj: Pair<Float, Float>) {
        //получаем длинну по каждой координате
        val length = Pair(secondObj.first - firstObj.first, secondObj.second - firstObj.second)
        val sign = Pair(if (length.first > 0) 1f else -1f, if (length.second > 0) 1f else -1f)
    }

    private fun startAnimationNewObject(objectShouldMove: FloatingActionButton, intersectionX: Float, intersectionY: Float, maxDistance: Float) {
        val cordsOfCenter = Pair(objectShouldMove.x + objectShouldMove.width / 2, objectShouldMove.y + objectShouldMove.height / 2)
        val signX = if (intersectionX > cordsOfCenter.first) -1f else 1f
        val signY = if (intersectionY > cordsOfCenter.second) -1f else 1f
        val xLenght = abs(cordsOfCenter.first - intersectionX)
        val yLenght = abs(cordsOfCenter.second - intersectionY)

        val p = if (xLenght > yLenght)
            Pair(signX * xLenght / yLenght, signY)
        else Pair(signX, signY * yLenght / xLenght)
        selected?.let { moveObjectToNewPosition(p.times(100).first, p.times(100).second, it, maxDistance) }
    }

    private fun changeStateOfButton(clicked: FloatingActionButton) {
        if (selected != clicked) {
            selected = clicked
            binding.frame.elevation = 50f
            clicked.compatElevation = 200f
            clicked.elevation = 200f
        } else {
            selected = null
            binding.frame.elevation = 0f
            clicked.elevation = 1f
            clicked.compatElevation = 1f
        }
    }

    private fun addToListPlayers() {
        listOfPlayers.add(binding.fab1)
        listOfPlayers.add(binding.fab2)
    }
}

private operator fun Pair<Float, Float>.times(i: Int) = Pair(first * i.toFloat(), second * i.toFloat())
