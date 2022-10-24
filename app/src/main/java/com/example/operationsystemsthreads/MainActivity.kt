package com.example.operationsystemsthreads

import android.animation.Animator
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
import kotlin.math.max
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
    private fun moveObjectToNewPosition(x: Float, y: Float, selectedBtn: FloatingActionButton, maxDistance: Float) {
        var objectForNewAnim: View? = null                    //тут будет храниться вью которая будет анимироваться(если будет при пересечении)
        val dataListNewMoving = mutableListOf<Float>()        //тут будут храниться центры x и y для объектов которые ударяются
        var cordX = selectedBtn.x
        var cordY = selectedBtn.y

        for (i in listOfPlayers)
            if (i.id != selectedBtn.id) {
                val signX = if (selectedBtn.x > x) -1f else 1f   //знак x
                val signY = if (selectedBtn.y > y) -1f else 1f   //знак y
                val xLenght = abs(x - selectedBtn.x)          //определение расстояния между объектами по x
                val yLenght = abs(y - selectedBtn.y)          //определение расстояния между объектами по y

                //кладёт шаги учитывая знак где одно из направлений равно 1
                val p = if (xLenght > yLenght)
                    Pair(signX * xLenght / yLenght, signY)
                else Pair(signX, signY * yLenght / xLenght)

                //сумма пройденых шагов по x и y
                var valueX = 0f
                var valueY = 0f

                //получение диапозона выбранного(перемещаемого объекта по x и y
                var rangeX = cordX..cordX + selectedBtn.width
                var rangeY = cordY..cordY + selectedBtn.height

                while (((i.x !in rangeX && i.x + i.width !in rangeX) || (i.y !in rangeY && i.y + i.width !in rangeY))
                    && abs(valueX) < xLenght && abs(valueY) < yLenght
                    && maxDistance > sqrt(valueX.pow(2) + valueY.pow(2))
                ) {

                    valueX += p.first
                    valueY += p.second

                    cordX += p.first
                    cordY += p.second
                    rangeX = cordX..cordX + selectedBtn.width
                    rangeY = cordY..cordY + selectedBtn.height
                }

                cordX -= p.first
                cordY -= p.second

                //если остановилось из-за припятствия
                if (abs(valueX) <= xLenght && abs(valueY) <= yLenght && maxDistance > sqrt(valueX.pow(2) + valueY.pow(2))) {
                    objectForNewAnim = i
                    dataListNewMoving.add(cordX)
                    dataListNewMoving.add(cordY)
                    dataListNewMoving.add(objectForNewAnim.x + objectForNewAnim.width / 2)
                    dataListNewMoving.add(objectForNewAnim.y + objectForNewAnim.height / 2)
                    dataListNewMoving.add(sqrt(valueX.pow(2) + valueY.pow(2)))
                }
                break
            }

        val newX = ObjectAnimator.ofFloat(selectedBtn, "x", cordX)
        val newY = ObjectAnimator.ofFloat(selectedBtn, "y", cordY)
        newX.duration = 500
        newY.duration = 500
        AnimatorSet().apply {
            this.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) = Unit
                override fun onAnimationEnd(p0: Animator?) {
                    objectForNewAnim?.let {
                        selected = it as FloatingActionButton
                        ricochet(
                            maxDistance - dataListNewMoving[4],
                            Pair(dataListNewMoving[0], dataListNewMoving[1]),
                            Pair(dataListNewMoving[2], dataListNewMoving[3])
                        )
                    }
                }

                override fun onAnimationCancel(p0: Animator?) = Unit
                override fun onAnimationRepeat(p0: Animator?) = Unit
            })
            playTogether(newX, newY)
            start()
        }
    }

    /**
     * remainsMaxDistance - остаток силы после прошлого соприкосновения
     * firstObj - координаты центра первого объекта(который ударяет)
     * secondObj - координаты центра второго объекта (по которому ударяют)
     */
    @SuppressLint("Recycle")
    private fun ricochet(remainsMaxDistance: Float, firstObj: Pair<Float, Float>, secondObj: Pair<Float, Float>) {
        //получаем длинну по каждой координате
        val length = Pair(secondObj.first - firstObj.first, secondObj.second - firstObj.second)
        val sign = Pair(if (length.first > 0) 1f else -1f, if (length.second > 0) 1f else -1f)

        //получаем шаги
        val steps = if (length.first > length.second)
            Pair(sign.first * abs(length.first / length.second), sign.second)
        else Pair(sign.first, sign.second * abs(length.second / length.first))

        //суммарная дистанция пройденая
        var sumDistanceX = 0f
        var sumDistanceY = 0f

        //итоговые координаты
        var newX = secondObj.first
        var newY = secondObj.second

        //добавить проверку входит ли в другой обьект
        while (remainsMaxDistance > sqrt(sumDistanceX.pow(2) + sumDistanceY.pow(2))) {
            sumDistanceX = +steps.first
            sumDistanceY = +steps.second

            newX = +steps.first
            newY = +steps.second
        }

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(selected, "x", newX).setDuration(500),
                ObjectAnimator.ofFloat(selected, "y", newY).setDuration(500)
            )
            start()
        }
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
