package com.aliaygor.trainpop

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.*
import kotlin.math.*

class MainActivity : Activity() {
    private lateinit var gameView: TrainView
    override fun onCreate(state: Bundle?) { super.onCreate(state); window.statusBarColor=Color.rgb(16,40,64); window.navigationBarColor=Color.rgb(16,40,64); gameView=TrainView(this); setContentView(gameView) }
    override fun onPause() { super.onPause(); gameView.pause() }
    override fun onResume() { super.onResume(); if(::gameView.isInitialized) gameView.resume() }
}

private class TrainView(val activity: Activity): View(activity) {
    private val prefs=activity.getSharedPreferences("trainpop",0)
    private var level=prefs.getInt("level",1)
    private var coins=prefs.getInt("coins",200)
    private var game=Game(level)
    private var screen="splash"
    private var modal=""
    private var time=0f
    private var last=0L
    private var tickTime=0f
    private var active=true
    private var motion=prefs.getBoolean("motion",true)
    private var message=""
    private var messageUntil=0f
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val palette=intArrayOf(0xFF23BFD5.toInt(),0xFFFFCA55.toInt(),0xFFFF668A.toInt(),0xFF8F7CEA.toInt(),0xFF54CC88.toInt())
    private val ink=0xFF173D59.toInt()
    private data class Hit(val rect: RectF,val action:()->Unit)
    private val hits=mutableListOf<Hit>()
    private var scale=1f
    private var offset=0f
    init { isFocusable=true; contentDescription="Train Pop tren ve robot renk eşleştirme oyunu" }
    fun pause() { active=false; last=0L; save() }
    fun resume() { active=true; last=0L; invalidate() }
    private fun save() { prefs.edit().putInt("level",level).putInt("coins",coins).putBoolean("motion",motion).apply() }
    private fun notice(s:String) { message=s; messageUntil=time+2.5f }
    private fun start() { game=Game(level); screen="game"; modal=""; tickTime=0f }
    private fun button(c:Canvas,x:Float,y:Float,w:Float,h:Float,title:String,color:Int=0xFF38C96F.toInt(),action:()->Unit) {
        box(c,x,y+5,w,h,shade(color,.68f),18f); box(c,x,y,w,h,color,18f)
        box(c,x+6,y+4,w-12,4f,0x44FFFFFF,3f)
        text(c,title,x+w/2,y+h/2+7,if(title.length>16) 15f else 19f,Color.WHITE,true)
        hits += Hit(RectF(x,y,x+w,y+h+5),action)
    }
    private fun box(c:Canvas,x:Float,y:Float,w:Float,h:Float,color:Int,r:Float=16f) { paint.shader=null; paint.color=color; paint.style=Paint.Style.FILL; c.drawRoundRect(x,y,x+w,y+h,r,r,paint) }
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=ink,bold:Boolean=false) { paint.shader=null; paint.color=color; paint.textSize=size; paint.textAlign=Paint.Align.CENTER; paint.typeface=if(bold) Typeface.create("sans-serif-rounded",Typeface.BOLD) else Typeface.create("sans-serif",Typeface.NORMAL); c.drawText(s,x,y,paint) }
    private fun shade(color:Int,f:Float)=Color.rgb((Color.red(color)*f).toInt(),(Color.green(color)*f).toInt(),(Color.blue(color)*f).toInt())
    private fun robot(c:Canvas,x:Float,y:Float,color:Int,size:Float=1f) {
        c.save(); c.translate(x,y); c.scale(size,size)
        box(c,-12f,9f,24f,5f,0x22000000,4f)
        box(c,-9f,-5f,18f,17f,shade(color,.8f),6f)
        box(c,-13f,-23f,26f,22f,color,7f)
        box(c,-9f,-18f,18f,10f,ink,4f)
        box(c,-6f,-15f,3f,3f,Color.WHITE,2f); box(c,3f,-15f,3f,3f,Color.WHITE,2f)
        box(c,-2f,-28f,4f,6f,color,2f)
        box(c,-15f,0f,4f,10f,color,3f); box(c,11f,0f,4f,10f,color,3f)
        c.restore()
    }
    private fun train(c:Canvas,x:Float,y:Float,color:Int,w:Float=66f,h:Float=47f,arrow:String="",count:String="") {
        box(c,x+3,y+6,w,h,0x24000000,12f)
        box(c,x+7,y+h-5,w-14,10f,ink,5f)
        box(c,x,y,w,h,shade(color,.75f),12f)
        box(c,x,y,w,h-8,color,12f)
        box(c,x+7,y+7,w-14,13f,0xFFDAF5FB.toInt(),5f)
        box(c,x+7,y+h-18,7f,5f,0xFFFFEAAA.toInt(),2f); box(c,x+w-14,y+h-18,7f,5f,0xFFFFEAAA.toInt(),2f)
        if(arrow.isNotEmpty()) text(c,arrow,x+w/2,y+36,25f,ink,true)
        if(count.isNotEmpty()) text(c,count,x+w/2,y+h+22,16f,ink,true)
    }
    override fun onDraw(c:Canvas) {
        super.onDraw(c)
        val now=System.nanoTime(); val dt=if(last==0L) 0f else ((now-last)/1e9f).coerceAtMost(.05f); last=now
        if(active) { time+=dt; if(screen=="game" && modal.isEmpty() && !game.completed) { tickTime+=dt; if(tickTime>.12f) { tickTime=0f; game.tick(); if(game.completed) { coins+=50; level++; save(); modal="win" } } } }
        if(screen=="splash" && time>1f) screen="loading"
        if(screen=="loading" && time>2.2f) screen="home"
        scale=min(width/400f,height/850f); offset=(height-850*scale)/2
        c.drawColor(0xFF102840.toInt()); c.save(); c.translate((width-400*scale)/2,offset); c.scale(scale,scale)
        paint.shader=LinearGradient(0f,0f,0f,850f,intArrayOf(0xFFD9F8FA.toInt(),0xFFB4D9EB.toInt()),null,Shader.TileMode.CLAMP); c.drawRect(0f,0f,400f,850f,paint); paint.shader=null
        hits.clear()
        when(screen) { "splash" -> { box(c,0f,0f,400f,850f,ink,0f); text(c,"axio",200f,425f,66f,Color.WHITE,true); text(c,"LET’S PLAY",200f,465f,12f,palette[1],true) }
            "loading" -> { homeScene(c); text(c,"TRAIN POP",200f,185f,43f,ink,true); text(c,"İstasyon hazırlanıyor…",200f,740f,17f); box(c,50f,765f,300f,12f,ink); box(c,50f,765f,300f*((time-1f)/1.2f).coerceIn(0f,1f),12f,palette[0]) }
            "home" -> home(c)
            "game" -> board(c)
        }
        if(modal.isNotEmpty()) dialog(c)
        if(time<messageUntil) { box(c,22f,795f,356f,36f,ink); text(c,message,200f,819f,13f,Color.WHITE) }
        c.restore(); if(active) postInvalidateOnAnimation()
    }
    private fun homeScene(c:Canvas) {
        for(i in 0..5) { val x=i*76f-35; box(c,x,260f-(i%3)*35,65f,220f,if(i%2==0) 0xFF9ABFD4.toInt() else 0xFFA6CCDF.toInt(),8f); for(j in 0..3) box(c,x+15,285f+j*32-(i%3)*35,16f,18f,0xFFDFF3F7.toInt(),3f) }
        box(c,0f,480f,400f,370f,0xFF7297AD.toInt(),0f)
        box(c,143f,480f,6f,370f,0xFFDDE9EB.toInt(),0f); box(c,263f,480f,6f,370f,0xFFDDE9EB.toInt(),0f)
        for(i in 0..8) box(c,138f,500f+i*40,136f,8f,0xFF4B7089.toInt(),2f)
        train(c,145f,425f,palette[0],130f,150f)
        text(c,"TRAIN POP",210f,519f,17f,ink,true)
        for(i in 0..5) robot(c,111f,470f+i*25,palette[i%5],.8f)
        robot(c,200f+(if(motion) sin(time*1.3f)*18 else 0f),666f,palette[1],2.1f)
    }
    private fun header(c:Canvas) {
        box(c,18f,26f,115f,40f,Color.WHITE); text(c,"● $coins",70f,53f,20f,ink,true)
        button(c,124f,29f,32f,30f,"+",palette[4]) { modal="gold" }
        button(c,338f,26f,44f,40f,"⚙",palette[0]) { modal="settings" }
    }
    private fun home(c:Canvas) {
        homeScene(c); header(c); text(c,"TRAIN POP",200f,142f,37f,ink,true); text(c,"ROBOT İSTASYONU",200f,166f,12f,ink,true)
        val names=listOf("Başlangıç","Reklamsız","Mağaza","İkmal","Döndür","Günlük giriş")
        names.forEachIndexed { i,n -> val x=if(i<3) 16f else 292f; val y=210f+(i%3)*83f
            button(c,x,y,92f,61f,n,if(i<3) palette[0] else palette[3]) { modal=when(i) {0->"starter";1->"ads";2->"shop";3->"supply";4->"wheel";else->"daily"} }
            if(i==4) { c.save(); c.rotate(if(motion) ((time%2f)/.4f).coerceAtMost(1f)*360 else 0f,x+46,y-9); text(c,"✦",x+46,y-3,20f,palette[1],true); c.restore() }
            if(i==3 && prefs.getString("supply","")!=day()) text(c,"!",x+82,y+6,23f,palette[2],true)
            if(i==5 && prefs.getString("daily","")!=day()) text(c,"!",x+82,y+6,23f,palette[2],true)
        }
        text(c,"Herkes trene!",200f,613f,18f,Color.WHITE,true)
        button(c,65f,705f,270f,67f,"OYNA · BÖLÜM $level") { start() }
        text(c,"AXIO  •  İlk oynanabilir sürüm",200f,822f,12f,Color.WHITE)
    }
    private fun board(c:Canvas) {
        button(c,16f,24f,42f,40f,"↻",palette[0]) { modal="restart" }
        button(c,342f,24f,42f,40f,"Ⅱ",palette[0]) { modal="settings" }
        text(c,"BÖLÜM ${game.level}",200f,52f,22f,ink,true)
        text(c,"${game.boarded} / ${game.total} robot trene bindi",200f,81f,14f)
        val oval=RectF(45f,105f,355f,290f)
        paint.color=0xFF7297AD.toInt(); paint.style=Paint.Style.STROKE; paint.strokeWidth=48f; c.drawOval(oval,paint); paint.style=Paint.Style.FILL
        game.robots.take(48).forEachIndexed { i,color -> val a=(i/48f*2*PI+(if(motion) time*.18 else 0.0)); robot(c,200f+155*cos(a).toFloat(),197f+92*sin(a).toFloat(),palette[color],.65f) }
        text(c,"ROBOT HALKASI",200f,194f,14f,ink,true); text(c,"Renkleri eşleştir",200f,216f,12f)
        box(c,14f,323f,372f,108f,0xFF89B0C5.toInt())
        for(i in 0..3) { val x=28f+i*88; box(c,x,339f,80f,58f,0xFFD6E9F1.toInt()); val t=game.platforms[i]
            if(t!=null) train(c,x+7,343f,palette[t.color],66f,43f,count="${t.remaining}/${t.capacity}") else text(c,"P${i+1}",x+40,375f,17f,0xFF7996AB.toInt(),true)
        }
        text(c,"Çıkışı açık trene dokun",200f,464f,16f,ink,true)
        box(c,14f,484f,372f,255f,0xFFEAF3F7.toInt(),22f)
        game.yard.forEach { t -> val x=28f+t.x*88; val y=504f+t.y*56
            train(c,x,y,palette[t.color],66f,43f,t.direction.glyph)
            hits+=Hit(RectF(x-4,y-3,x+76,y+49)) { when(game.dispatch(t.id)) { Result.BLOCKED->notice("Önce çıkışı kapatan treni çıkar."); Result.FULL-> { modal="full" }; Result.MOVED->Unit } }
        }
        button(c,20f,759f,112f,49f,"Ekspres",palette[3]) { modal="rescue" }
        button(c,144f,759f,112f,49f,"İpucu",palette[0]) { val t=game.yard.firstOrNull { game.canExit(it) }; notice(if(t==null) "Robotların binmesini bekle." else "${t.y+1}. sıra, ${t.x+1}. trenin çıkışı açık.") }
        button(c,268f,759f,112f,49f,"Menü",palette[0]) { modal="leave" }
    }
    private fun day()=java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(java.util.Date())
    private fun reward(key:String,amount:Int) { if(prefs.getString(key,"")==day()) notice("Bugünkü ödül zaten alındı.") else { coins+=amount; prefs.edit().putString(key,day()).apply(); save(); notice("+$amount altın kazandın!") }; modal="" }
    private fun dialog(c:Canvas) {
        hits.clear(); box(c,0f,0f,400f,850f,0xBB102840.toInt(),0f)
        box(c,24f,194f,352f,446f,0xFFFBF6E8.toInt(),26f)
        val title=when(modal) { "settings"->"Ayarlar";"win"->"Harika iş!";"restart"->"Yeniden başlat?";"leave"->"Ana menüye dön?";"full"->"Peronlar dolu";"rescue"->"Ekspres yardım";"daily"->"Günlük giriş";"supply"->"Günlük ikmal";"wheel"->"Şanslı çark";"gold"->"Altın";"starter"->"Başlangıç paketi";"ads"->"Reklamsız";else->"Mağaza" }
        text(c,title,200f,241f,26f,ink,true)
        button(c,325f,205f,34f,32f,"×",palette[2]) { modal="" }
        when(modal) {
            "settings" -> { text(c,"Bu prototipte ses ve reklam yok.",200f,303f,15f); button(c,55f,337f,290f,48f,if(motion) "Hareket: Açık" else "Hareket: Kapalı",palette[0]) { motion=!motion; save() }; button(c,55f,408f,290f,48f,"Ana menü",palette[0]) { screen="home";modal="" }; button(c,55f,479f,290f,48f,"Bölümü yeniden başlat") { modal="restart" } }
            "win" -> { robot(c,200f,357f,palette[1],3f); text(c,"Tüm robotlar yola çıktı!",200f,411f,19f,ink,true); text(c,"+50 altın",200f,449f,23f,ink,true); button(c,55f,519f,290f,55f,"Sıradaki bölüm") { start() } }
            "restart","leave" -> { text(c,"Bu bölümdeki hamleler sıfırlanacak.",200f,350f,16f); button(c,55f,431f,290f,48f,"Devam et",palette[0]) { modal="" }; button(c,55f,503f,290f,48f,"Evet") { if(modal=="restart") start() else { screen="home";modal="" } } }
            "full","rescue" -> { text(c,"Ekspres, bekleyen trenleri gönderir.",200f,330f,15f); text(c,"Kalan robotları da beraberinde götürür.",200f,357f,14f); text(c,"Altın: $coins",200f,403f,19f,ink,true); button(c,55f,447f,290f,48f,"50 altın · Kullan") { if(coins<50) notice("Yeterli altın yok.") else if(game.rescue()) { coins-=50;save();modal=""; if(game.completed) { coins+=50;level++;save();modal="win" } } else notice("Peronda bekleyen tren yok.") }; button(c,55f,520f,290f,48f,"Oyuna dön",palette[0]) { modal="" } }
            "daily","supply" -> { val key=if(modal=="daily") "daily" else "supply"; val amount=if(key=="daily") 100 else 30
                robot(c,200f,345f,palette[1],2.8f); text(c,"Bugünün ödülü: $amount altın",200f,417f,20f,ink,true); text(c,"Her gün yeni bir istasyon ödülü.",200f,454f,15f); button(c,55f,518f,290f,52f,if(prefs.getString(key,"")==day()) "Alındı" else "Ödülü al") { reward(key,amount) } }
            "wheel" -> { val colors=intArrayOf(palette[0],palette[3],palette[1]); for(i in 0..5) { paint.color=colors[i%3]; c.drawArc(105f,270f,295f,460f,i*60f,60f,true,paint) }; text(c,"✦",200f,385f,40f,Color.WHITE,true); text(c,"Günde bir demo çeviriş",200f,491f,15f); button(c,55f,526f,290f,48f,"Döndür") { reward("wheel",listOf(30,50,100,150).random()) } }
            else -> { text(c,"DEMO EKRANI",200f,325f,17f,palette[3],true); text(c,"Gerçek satın alma ve reklam",200f,379f,18f); text(c,"bağlantısı henüz eklenmedi.",200f,408f,18f); text(c,"Bu sürümde ücret alınmaz.",200f,455f,15f); button(c,55f,521f,290f,48f,"Kapat",palette[0]) { modal="" } }
        }
    }
    override fun onTouchEvent(event:MotionEvent):Boolean { if(event.action==MotionEvent.ACTION_UP) { val x=(event.x-(width-400*scale)/2)/scale; val y=(event.y-offset)/scale; hits.lastOrNull { it.rect.contains(x,y) }?.action?.invoke(); invalidate(); performClick() }; return true }
    override fun performClick():Boolean { super.performClick(); return true }
}
