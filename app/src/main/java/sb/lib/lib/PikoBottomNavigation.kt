package  sb.lib.lib

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.XmlRes
import androidx.navigation.NavController
import sb.lib.R


class PikoBottomNavigation @JvmOverloads constructor(context: Context, attr:AttributeSet?=null, defStyle:Int =0)  : View(context,attr,defStyle){



    companion object {

        private const val INVALID_RES = -1

    }


    private var defaultHeight: Int = 0
    private val heightMargin: Int = 60
    private var items = listOf<BottomBarItem>()


    @XmlRes
    private var _itemMenuRes: Int =INVALID_RES

    var itemMenuRes: Int
        @XmlRes get() = _itemMenuRes
        set(@XmlRes value) {
            _itemMenuRes = value
            if (value != INVALID_RES) {
                items = BottomBarParser(context, value).parse()
                invalidate()
            }
        }


    init {


        init(context,attr,defStyle)


    }


    private fun init(context: Context, attr: AttributeSet?,defStyle: Int) {


        val typedArray = context.obtainStyledAttributes(attr, R.styleable.PikoNavigationRail,defStyle,0)

        itemMenuRes =         typedArray.getResourceId(R.styleable.PikoNavigationRail_item_menu ,itemMenuRes)


    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val icon = drawableToBitmap(items[0].icon)


        defaultHeight =  icon!!.height + heightMargin + heightMargin/2

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        setMeasuredDimension(widthSize,defaultHeight)

    }

    private lateinit var  rects : Array<Rect>

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        rects = Array<Rect>(items.size){Rect()}

        val widthLength = width/items.size
        val icon = drawableToBitmap(items[0].icon)

        defaultHeight =  icon!!.height + heightMargin

        var defaultLeft = 0

        for(i:Int in items.indices step 1){

            val rect = Rect()

            rect.left = defaultLeft
            rect.right = defaultLeft + widthLength
            rect.top = heightMargin/2
            rect.bottom = defaultHeight + heightMargin/2

            defaultLeft += widthLength
            rects[i] = rect

        }


        YAxis =30
    }


    private var path = Path()

    private var paint = Paint().apply {
        this.strokeWidth = 7f }

    private var pathPaint = Paint().apply {

        this.style = Paint.Style.FILL
        this.color = Color.GREEN

        this.strokeWidth = 7f }



    private var activeIndex = 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(canvas == null ) return



        path.reset()

         path.lineTo(0f,heightMargin/2f)
         path.lineTo(rects[activeIndex].left.toFloat(),heightMargin/2f)


        val leftX =   rects[activeIndex].centerX() - defaultHeight/2f
        val centerX =   rects[activeIndex].centerX().toFloat()



        path.cubicTo(leftX ,heightMargin/2f,
           leftX ,rects[activeIndex].bottom.toFloat() ,
           centerX , rects[activeIndex].bottom.toFloat() )


        path.cubicTo( rects[activeIndex].centerX().toFloat() +defaultHeight/2f,rects[activeIndex].bottom.toFloat()
            ,rects[activeIndex].centerX().toFloat() +defaultHeight/2f ,heightMargin/2f ,
            rects[activeIndex].right.toFloat()  ,heightMargin/2f)



        path.lineTo(width.toFloat(), heightMargin/2f)
        path.lineTo(width.toFloat() , defaultHeight.toFloat() +heightMargin/2f)
        path.lineTo(0f, defaultHeight.toFloat()+heightMargin/2f)




        path.close()



        drawCircle(canvas)

        canvas.drawPath(path ,pathPaint)

        drawIcons(canvas)
    }


    private var YAxis =0

   private  fun animateYAxis(){


    val heightAxis =   heightMargin/2
        ValueAnimator.ofInt(0,heightAxis).apply {

            this.duration = 100
            this.interpolator = LinearInterpolator()

            addUpdateListener {
                YAxis = it.animatedValue as Int
                invalidate()
            }


            start() }

    }

    private  val circlePaint = Paint().apply {

        this.color =  Color.GREEN

    }

    private fun drawCircle(canvas: Canvas) {

        for(i:Int in items.indices step 1){

            val rect = rects[i]
            val centerX = rect.centerX().toFloat()
            val centerY = rect.centerY().toFloat()


            if(activeIndex ==i) {
                canvas.drawCircle(centerX, centerY - YAxis, centerY - heightMargin / 2, circlePaint)

            }else{
                canvas.drawCircle(centerX, centerY, centerY - heightMargin / 2, circlePaint)


            }


    }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private val bitmapPaint = Paint().apply {


    }

    private fun drawIcons(canvas: Canvas) {


        for(i:Int in items.indices step 1 ){

          val bitmap = drawableToBitmap(items[i].icon)

            val rectCenterX = rects[i].centerX() - bitmap!!.width/2
            val rectCenterY = rects[i].centerY() - bitmap!!.height/2


            if(activeIndex ==i) {

                canvas.drawBitmap(
                    bitmap,
                    rectCenterX.toFloat(),
                    rectCenterY.toFloat()- YAxis, bitmapPaint
                )

            }else{

                canvas.drawBitmap(
                    bitmap,
                    rectCenterX.toFloat(),
                    rectCenterY.toFloat(), bitmapPaint
                )

            }





            }
        }





    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event ==null)return false

        when(event.action){


            MotionEvent.ACTION_DOWN ->{

                for(i:Int in 0 until items.size step 1){

                    val rect = rects[i]
                    if(rect.contains(event.x.toInt(),event.y.toInt())){

                        println("clicked $i")

                        if(activeIndex != i ) {

                            activeIndex =i

                           // navController?.navigate(i)

                            animateYAxis()
                        }

                    }

                }

        }



        }


        return true
    }


    private var navController: NavController?=null

    fun setNavigation(navController: NavController) {

        this.navController = navController
    }

}


