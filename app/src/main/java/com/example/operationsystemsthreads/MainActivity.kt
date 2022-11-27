package com.example.operationsystemsthreads

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.operationsystemsthreads.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    val listOfPlayers = mutableListOf<FootballObject>()
    var selected: FloatingActionButton? = null

    /**
     *1 - синяя команда, 2 - красная команда
     */
    var teamMove = 1

    //   var teamMove = (1..2).random()
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.fab1.setOnClickListener {
            if (teamMove == 1)
                changeStateOfButton(listOfPlayers[0].view)
        }
        binding.fab2.setOnClickListener {
            if (teamMove == 1)
                changeStateOfButton(listOfPlayers[1].view)
        }
        binding.fab3.setOnClickListener {
            if (teamMove == 1)
                changeStateOfButton(listOfPlayers[2].view)
        }

        binding.fab5.setOnClickListener {
            if (teamMove == 2)
                changeStateOfButton(listOfPlayers[4].view)
        }
        binding.fab6.setOnClickListener {
            if (teamMove == 2)
                changeStateOfButton(listOfPlayers[5].view)
        }
        binding.fab7.setOnClickListener {
            if (teamMove == 2)
                changeStateOfButton(listOfPlayers[6].view)
        }

        addToListPlayers()
        binding.frame.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP)
                selected?.let { selectedBtn ->
                    val x = motionEvent.x - (selectedBtn.width / 2)
                    val y = motionEvent.y - (selectedBtn.height / 2)
                    val maxDistance = sqrt(
                        abs(x - (selectedBtn.x + selectedBtn.width / 2)).pow(2) +
                                abs(y - (selectedBtn.y + selectedBtn.height / 2)).pow(2)
                    )
                    moveObjectToNewPosition(x, y, selectedBtn, maxDistance)
                    /*teamMove = if (teamMove == 1) 0 else 1
                    selected = null*/
                }
            true
        }
    }

    @SuppressLint("Recycle")
    /**
     * x - координата по x куда тапнули
     * y - координата по y куда тапнули
     * selectedBtn - выбранная вьюшка которая будет анимироваться
     * maxDistance - определякет максимальную дистанцую которую могут проанимровать вьюшки
     */
    private fun moveObjectToNewPosition(x: Float, y: Float, selectedBtn: FloatingActionButton, maxDistance: Float) {
        var objectForNewAnim: FootballObject? = null                    //тут будет храниться вью которая будет анимироваться(если будет при пересечении)
        val dataListNewMoving = mutableListOf<Float>()        //тут будут храниться центры x и y для объектов которые ударяются
        var cordX: Float? = null
        var cordY: Float? = null

        for (i in listOfPlayers)
            if (i.view.id != selectedBtn.id) {
                var iCordX = selectedBtn.x
                var iCordY = selectedBtn.y

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
                var rangeX = iCordX..iCordX + selectedBtn.width
                var rangeY = iCordY..iCordY + selectedBtn.height

                while (((i.view.x !in rangeX && i.view.x + i.view.width !in rangeX) || (i.view.y !in rangeY && i.view.y + i.view.width !in rangeY))
                    && abs(valueX) < xLenght && abs(valueY) < yLenght
                    && maxDistance > sqrt(valueX.pow(2) + valueY.pow(2))
                ) {
                    valueX += p.first
                    valueY += p.second

                    iCordX += p.first
                    iCordY += p.second
                    rangeX = iCordX..iCordX + selectedBtn.width
                    rangeY = iCordY..iCordY + selectedBtn.height
                }

                iCordX -= p.first
                iCordY -= p.second

                //если остановилось из-за припятствия
                if (abs(valueX) <= xLenght && abs(valueY) <= yLenght && maxDistance > sqrt(valueX.pow(2) + valueY.pow(2))) {
                    objectForNewAnim = i

                    //в dataListNewMoving записываем нужные данные для перехода к методу  ricochet
                    dataListNewMoving.add(iCordX + objectForNewAnim.view.width / 2)
                    dataListNewMoving.add(iCordY + objectForNewAnim.view.height / 2)
                    dataListNewMoving.add(objectForNewAnim.view.x + objectForNewAnim.view.width / 2)
                    dataListNewMoving.add(objectForNewAnim.view.y + objectForNewAnim.view.height / 2)
                    dataListNewMoving.add(sqrt(valueX.pow(2) + valueY.pow(2)))
                }

                if (cordX == null || cordY == null) {
                    cordX = iCordX
                    cordY = iCordY
                }

                if (getHypotenuse(cordX, cordY, selectedBtn.x + (selectedBtn.width / 2), selectedBtn.y + (selectedBtn.height / 2)) >
                    getHypotenuse(valueX, valueY)
                ) {
                    cordX = iCordX
                    cordY = iCordY
                }
            }

        val newX = cordX?.let { ObjectAnimator.ofFloat(selectedBtn, "x", it) }
        val newY = cordY?.let { ObjectAnimator.ofFloat(selectedBtn, "y", it) }
        newX?.duration = 500
        newY?.duration = 500
        AnimatorSet().apply {
            this.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) = Unit
                override fun onAnimationEnd(p0: Animator?) {
                    if (objectForNewAnim != null && objectForNewAnim.typeOfObject != 0) {
                        ricochet(
                            maxDistance - dataListNewMoving[4],
                            Pair(dataListNewMoving[0], dataListNewMoving[1]),
                            Pair(
                                dataListNewMoving[2], dataListNewMoving[3]
                            ),
                            objectForNewAnim.view,
                            if (objectForNewAnim.typeOfObject == 0)
                        )
                    } else {
                        selected?.let { changeStateOfButton(it) }
                        //selected = null
                        teamMove = if (teamMove == 1) 2 else 1
                    }
                }

                override fun onAnimationCancel(p0: Animator?) = Unit
                override fun onAnimationRepeat(p0: Animator?) = Unit
            })
            playTogether(newX, newY)
            start()
        }
    }

    @SuppressLint("Recycle")
    /**
     * remainsMaxDistance - остаток силы после прошлого соприкосновения
     * firstObj - координаты центра первого объекта(который ударяет)
     * secondObj - координаты центра второго объекта (по которому ударяют)
     */
    private fun ricochet(
        remainsMaxDistance: Float,
        firstObj: Pair<Float, Float>,
        secondObj: Pair<Float, Float>,
        newObject: FloatingActionButton,
        isBall: Boolean = false
    ) {
        //получаем длинну по каждой координате
        val length = Pair(secondObj.first - firstObj.first, secondObj.second - firstObj.second)
        val sign = Pair(if (length.first > 0) 1f else -1f, if (length.second > 0) 1f else -1f)

        //получаем шаги
        val steps = if (isBall) {
            if (length.first > length.second)
                Pair(-(sign.first * abs(length.first / length.second)), -sign.second)
            else Pair(-sign.first, -(sign.second * abs(length.second / length.first)))
        } else {
            if (length.first > length.second)
                Pair(sign.first * abs(length.first / length.second), sign.second)
            else Pair(sign.first, sign.second * abs(length.second / length.first))
        }

        //суммарная дистанция пройденая
        var sumDistanceX = 0f
        var sumDistanceY = 0f

        //итоговые координаты
        var newX = secondObj.first
        var newY = secondObj.second

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        //добавить проверку входит ли в другой обьект
        loop@ while (remainsMaxDistance > sqrt(sumDistanceX.pow(2) + sumDistanceY.pow(2))) {
            sumDistanceX += steps.first
            sumDistanceY += steps.second

            newX += steps.first
            newY += steps.second
            if (newX < 0 || newY < 0 || newY + (selected?.height ?: 1) > height || newX + (selected?.width ?: 1) > width)
                break@loop
        }
        moveObjectToNewPosition(newX, newY, newObject, remainsMaxDistance)
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

    private operator fun Pair<Float, Float>.times(i: Int) = Pair(first * i.toFloat(), second * i.toFloat())

    private fun getHypotenuse(firstX: Float, firstY: Float, secondX: Float, secondY: Float) =
        getHypotenuse(abs(firstX - secondX), abs(firstY - secondY))

    private fun getHypotenuse(first: Float, second: Float) =
        sqrt(first.pow(2) + second.pow(2))

    private fun addToListPlayers() {
        listOfPlayers.add(FootballObject(binding.fab1, 1))
        listOfPlayers.add(FootballObject(binding.fab2, 1))
        listOfPlayers.add(FootballObject(binding.fab3, 1))
        listOfPlayers.add(FootballObject(binding.fab4, 0))
        listOfPlayers.add(FootballObject(binding.fab5, 2))
        listOfPlayers.add(FootballObject(binding.fab6, 2))
        listOfPlayers.add(FootballObject(binding.fab7, 2))
    }
}