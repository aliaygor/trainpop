package com.aliaygor.trainpop

import android.graphics.*
import kotlin.math.*

/** Original shaded game primitives. UI and game objects share the same materials. */
class Art {
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val colors = intArrayOf(0xFF20BDE9.toInt(), 0xFFFFCF35.toInt(), 0xFFF45387.toInt(), 0xFF9C5EEA.toInt(), 0xFF59D252.toInt(), 0xFFFF9336.toInt())
    val ink = 0xFF184665.toInt()
    val white = 0xFFFFFEF7.toInt()
    var trainSprites:List<Bitmap> = emptyList()
    var wagonSprites:List<Bitmap> = emptyList()
    var passengerSprites:List<Bitmap> = emptyList()
    fun shade(color: Int, f: Float) = Color.rgb((Color.red(color)*f).toInt().coerceIn(0,255), (Color.green(color)*f).toInt().coerceIn(0,255), (Color.blue(color)*f).toInt().coerceIn(0,255))
    fun box(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int, r: Float = 16f) {
        p.shader=null; p.style=Paint.Style.FILL; p.color=color; c.drawRoundRect(x,y,x+w,y+h,r,r,p)
    }
    fun gradient(c: Canvas, x: Float, y: Float, w: Float, h: Float, top: Int, bottom: Int, r: Float = 16f) {
        p.style=Paint.Style.FILL; p.alpha=255; p.shader=LinearGradient(x,y,x,y+h,top,bottom,Shader.TileMode.CLAMP)
        c.drawRoundRect(x,y,x+w,y+h,r,r,p); p.shader=null
    }
    fun circle(c: Canvas,x:Float,y:Float,r:Float,color:Int) { p.shader=null;p.style=Paint.Style.FILL;p.color=color;c.drawCircle(x,y,r,p) }
    fun line(c:Canvas,x:Float,y:Float,x2:Float,y2:Float,color:Int,w:Float=3f) { p.color=color;p.strokeWidth=w;p.strokeCap=Paint.Cap.ROUND;c.drawLine(x,y,x2,y2,p) }
    fun label(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=ink,outline:Boolean=false) {
        p.shader=null;p.textSize=size;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-rounded",Typeface.BOLD)
        if(outline) { p.style=Paint.Style.STROKE;p.strokeWidth=size*.13f;p.strokeJoin=Paint.Join.ROUND;p.color=ink;c.drawText(s,x,y,p) }
        p.style=Paint.Style.FILL;p.color=color;c.drawText(s,x,y,p)
    }
    fun caption(c:Canvas,s:String,x:Float,y:Float,size:Float=14f,color:Int=ink) { p.typeface=Typeface.create("sans-serif",Typeface.NORMAL);p.textAlign=Paint.Align.CENTER;p.textSize=size;p.color=color;p.style=Paint.Style.FILL;p.shader=null;c.drawText(s,x,y,p) }
    fun panel(c:Canvas,x:Float,y:Float,w:Float,h:Float,color:Int=0xFFFFD35B.toInt()) {
        box(c,x,y+8,w,h,shade(color,.62f),28f); gradient(c,x,y,w,h,shade(color,1.1f),color,28f)
        box(c,x+9,y+51,w-18,h-61,white,21f)
    }
    fun coin(c:Canvas,x:Float,y:Float,size:Float=1f) {
        c.save();c.translate(x,y);c.scale(size,size)
        circle(c,0f,3f,16f,0xFFD7910A.toInt());circle(c,0f,0f,16f,0xFFFFDA3E.toInt());circle(c,0f,0f,12f,0xFFF9B821.toInt())
        p.color=0xFFFFEA78.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=2f;c.drawCircle(0f,0f,12f,p);p.style=Paint.Style.FILL
        label(c,"$",0f,7f,22f,0xFFFFF4A5.toInt());c.restore()
    }
    fun coinPile(c:Canvas,x:Float,y:Float,size:Float=1f) { c.save();c.translate(x,y);c.scale(size,size); for(i in 0..2) for(j in 0..(2-i/2)) coin(c,(i-1)*21f,j*-9f-i*4f,.75f);c.restore() }
    private val robotSprites=mutableMapOf<Int,Bitmap>()
    fun release() { robotSprites.values.forEach { it.recycle() };robotSprites.clear() }
    fun robot(c:Canvas,x:Float,y:Float,color:Int,size:Float=1f,pose:Float=0f,conductor:Boolean=false) {
        val index=colors.indexOf(color)
        if(!conductor && index>=0 && passengerSprites.size==6) {
            val sprite=passengerSprites[index]
            val h=52f*size;val w=h*sprite.width/sprite.height
            p.shader=null;p.colorFilter=null;p.alpha=255;p.isFilterBitmap=true
            c.drawBitmap(sprite,null,RectF(x-w/2,y-h+9f*size,x+w/2,y+9f*size),p)
            return
        }
        if(pose!=0f || conductor) { paintRobot(c,x,y,color,size,pose,conductor);return }
        val sprite=robotSprites.getOrPut(color) {
            Bitmap.createBitmap(120,180,Bitmap.Config.ARGB_8888).also { paintRobot(Canvas(it),60f,120f,color,2f) }
        }
        p.shader=null;p.colorFilter=null;p.alpha=255;p.isFilterBitmap=true
        c.drawBitmap(sprite,null,RectF(x-30f*size,y-60f*size,x+30f*size,y+30f*size),p)
    }
    private fun paintRobot(c:Canvas,x:Float,y:Float,color:Int,size:Float=1f,pose:Float=0f,conductor:Boolean=false) {
        c.save();c.translate(x,y);c.scale(size,size)
        p.color=0x26000000;c.drawOval(-14f,12f,14f,18f,p)
        box(c,-9f,8f,7f,7f,ink,3f);box(c,3f,8f,7f,7f,ink,3f)
        gradient(c,-10f,-6f,20f,20f,shade(color,1.12f),shade(color,.8f),7f)
        circle(c,0f,3f,3f,white)
        line(c,-14f,-2f,-17f,6f+pose*3,color,5f);line(c,14f,-2f,18f,-3f-pose*10,color,5f)
        gradient(c,-15f,-28f,30f,25f,shade(color,1.18f),color,9f)
        box(c,-10f,-23f,20f,13f,0xFF16354E.toInt(),6f)
        circle(c,-5f,-17f,2.7f,0xFF8BF5FF.toInt());circle(c,5f,-17f,2.7f,0xFF8BF5FF.toInt())
        line(c,-2f,-13f,2f,-13f,white,1.4f)
        line(c,0f,-28f,0f,-35f,color,2.5f);circle(c,0f,-36f,3f,colors[1])
        if(conductor) { box(c,-17f,-31f,34f,6f,ink,3f);box(c,-13f,-39f,26f,10f,0xFF3B77AE.toInt(),4f);circle(c,0f,-34f,2.6f,colors[1]);box(c,-7f,0f,14f,3f,ink,1f) }
        c.restore()
    }
    /** Top-down locomotive, nose points up; long versions add a carriage. */
    fun train(c:Canvas,x:Float,y:Float,color:Int,length:Float=48f,width:Float=30f,angle:Float=0f,arrow:Boolean=true,alpha:Int=255) {
        val index=colors.indexOf(color)
        if(index>=0 && trainSprites.size==6) {
            c.save();c.translate(x,y);c.rotate(angle)
            p.shader=null;p.colorFilter=null;p.alpha=alpha;p.isFilterBitmap=true
            val coupled=length>60f
            val engineHeight=if(coupled) 47f else length
            val engineY=if(coupled) -length/2+engineHeight/2 else 0f
            val sprite=trainSprites[index]
            val engineWidth=min(width,engineHeight*sprite.width/sprite.height)
            if(coupled) {
                val wagon=wagonSprites.getOrNull(index) ?: sprite
                val wagonY=length/2-17f
                line(c,0f,engineY+engineHeight/2-3,0f,wagonY-14,0xFF8B98A8.toInt(),4f)
                p.shader=null;p.alpha=alpha;p.isFilterBitmap=true
                c.drawBitmap(wagon,null,RectF(-engineWidth/2,wagonY-16,engineWidth/2,wagonY+16),p)
            }
            p.shader=null;p.alpha=alpha;p.isFilterBitmap=true
            c.drawBitmap(sprite,null,RectF(-engineWidth/2,engineY-engineHeight/2,engineWidth/2,engineY+engineHeight/2),p)
            if(arrow) { c.save();c.translate(0f,engineY);c.scale(.88f,.88f);drawArrow(c);c.restore() }
            p.alpha=255;c.restore();return
        }
        c.save();c.translate(x,y);c.rotate(angle);c.scale(width/30f,length/48f)
        p.alpha=alpha
        box(c,-16f,-20f,34f,46f,0x33000000,8f)
        box(c,-16f,-14f,4f,30f,ink,2f);box(c,12f,-14f,4f,30f,ink,2f)
        gradient(c,-14f,-24f,28f,48f,shade(color,1.16f),shade(color,.8f),7f)
        box(c,-11f,-20f,22f,8f,0xFFA8EAF9.toInt(),3f)
        box(c,-10f,12f,20f,7f,shade(color,.58f),3f)
        box(c,-10f,-12f,20f,24f,color,4f)
        // Chimney and cowcatcher distinguish the locomotive silhouette from a bus.
        circle(c,0f,-16f,4.8f,shade(color,.54f));circle(c,0f,-17f,3.4f,shade(color,1.12f))
        box(c,-11f,-27f,22f,4f,ink,2f)
        for(i in -2..2) line(c,i*4f,-27f,i*4f,-23f,0xFF8FA8B7.toInt(),1f)
        box(c,-9f,-10f,3f,7f,0x55FFFFFF,2f)
        box(c,-9f,-23f,4f,3f,0xFFFFF8BE.toInt(),1f);box(c,5f,-23f,4f,3f,0xFFFFF8BE.toInt(),1f)
        if(arrow) drawArrow(c)
        p.alpha=255;c.restore()
    }
    private fun drawArrow(c:Canvas) {
        val path=Path();path.moveTo(-2.5f,14f);path.lineTo(-2.5f,3f);path.lineTo(-7f,3f);path.lineTo(0f,-4f);path.lineTo(7f,3f);path.lineTo(2.5f,3f);path.lineTo(2.5f,14f);path.close()
        p.style=Paint.Style.STROKE;p.strokeWidth=2.4f;p.color=ink;c.drawPath(path,p);p.style=Paint.Style.FILL;p.color=Color.WHITE;c.drawPath(path,p)
    }
    fun wheel(c:Canvas,x:Float,y:Float,r:Float,rotation:Float=0f,rewards:List<Reward>?=null) {
        circle(c,x,y+6,r+5,0xFF624792.toInt());circle(c,x,y,r+5,0xFFF8F0FF.toInt());circle(c,x,y,r,0xFF9075D2.toInt())
        c.save();c.rotate(rotation,x,y)
        for(i in 0..5) { p.color=if(i==4) colors[1] else if(i%2==0) 0xFFD686F3.toInt() else 0xFFB151E0.toInt();c.drawArc(x-r+6,y-r+6,x+r-6,y+r-6,-120f+i*60,60f,true,p)
            if(rewards!=null) { val a=Math.toRadians((-90+i*60).toDouble());val rx=x+cos(a).toFloat()*r*.65f;val ry=y+sin(a).toFloat()*r*.65f; val reward=rewards[i]
                val kind=if(reward.coins>0) "coin" else if(reward.vip>0) "vip" else if(reward.arrange>0) "arrange" else "shuffle"
                icon(c,kind,rx,ry-5,r/160f);label(c,if(reward.coins>0) "${reward.coins}" else "×1",rx,ry+25*r/130,18*r/130,white,true)
            }
        };c.restore();circle(c,x,y,r*.19f,0xFFF0DFFF.toInt());circle(c,x,y,r*.13f,0xFFCEB5EF.toInt())
        if(rewards!=null) { val path=Path();path.moveTo(x-10,y-r-10);path.lineTo(x+10,y-r-10);path.lineTo(x,y-r+17);path.close();p.color=ink;c.drawPath(path,p);circle(c,x,y-r-7,4f,colors[1]) }
    }
    fun icon(c:Canvas,kind:String,x:Float,y:Float,size:Float=1f) {
        c.save();c.translate(x,y);c.scale(size,size)
        when(kind) {
            "coin","starter" -> coinPile(c,0f,9f,1.2f)
            "shop" -> { box(c,-24f,-11f,48f,37f,0xFFFFE7B5.toInt(),6f);for(i in 0..4) box(c,-27f+i*11,-22f,11f,19f,if(i%2==0) colors[0] else white,4f);coin(c,0f,8f,.65f) }
            "ads" -> { circle(c,0f,0f,26f,0xFFEF4A51.toInt());label(c,"ADS",0f,6f,18f,white);line(c,-19f,20f,19f,-20f,0xFFFFBC67.toInt(),6f) }
            "supply" -> { gradient(c,-19f,-27f,38f,54f,0xFF75D0FF.toInt(),0xFF2B83CE.toInt(),8f);box(c,-12f,-20f,24f,15f,ink,4f);line(c,0f,-15f,7f,-11f,colors[1],3f);coinPile(c,0f,23f,.7f) }
            "daily" -> { box(c,-22f,-25f,44f,54f,0xFFF7BE43.toInt(),6f);box(c,-17f,-17f,34f,39f,white,4f);box(c,-11f,-30f,22f,12f,colors[5],4f);line(c,-10f,3f,-3f,12f,colors[4],6f);line(c,-3f,12f,13f,-6f,colors[4],6f) }
            "wheel","shuffle" -> wheel(c,0f,0f,26f)
            "vip" -> { train(c,0f,0f,colors[5],44f,30f,35f,false);label(c,"★",16f,-15f,22f,colors[1],true) }
            "arrange" -> { train(c,5f,0f,colors[1],43f,29f,65f,false);for(i in 0..2) robot(c,-20f+i*10,22f,colors[i],.5f) }
            "settings" -> { for(i in 0..7) { c.save();c.rotate(i*45f);box(c,-4f,-23f,8f,12f,white,2f);c.restore() };circle(c,0f,0f,17f,white);circle(c,0f,0f,8f,0xFF5095C4.toInt()) }
            "pause" -> { box(c,-10f,-13f,7f,27f,white,2f);box(c,4f,-13f,7f,27f,white,2f) }
            "restart" -> { p.color=white;p.strokeWidth=6f;p.style=Paint.Style.STROKE;c.drawArc(-16f,-16f,16f,16f,-50f,280f,false,p);p.style=Paint.Style.FILL;val path=Path();path.moveTo(-17f,-17f);path.lineTo(-4f,-16f);path.lineTo(-14f,-5f);path.close();c.drawPath(path,p) }
        }
        c.restore()
    }
}
