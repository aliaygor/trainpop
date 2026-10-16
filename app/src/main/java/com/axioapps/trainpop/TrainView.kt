package com.axioapps.trainpop

import android.app.Activity
import android.graphics.*
import android.view.*
import kotlin.math.*
import kotlin.random.Random

@android.annotation.SuppressLint("ViewConstructor") // Created by the Activity; not inflated from XML.
class TrainView(private val activity:Activity):View(activity) {
    private val prefs=activity.getSharedPreferences("trainpop",0)
    private val economy=Economy(PreferenceLedger(prefs))
    private val art=Art()
    private val audio=Audio(activity)
    private val consent=AdConsent(activity)
    private val breakAds=BreakAds(activity,economy,consent)
    private val ads=RewardAds(activity,consent) { notice(it) }
    private val store=Store(activity,economy) { notice(it) }
    private val platformArt=BitmapFactory.decodeResource(resources,R.drawable.platform_v10)
    private var gameBackdrop:Bitmap?=null
    private val gardenArt=BitmapFactory.decodeResource(resources,R.drawable.garden_v7)
    private var welcomeUntil=0f
    private val station=BitmapFactory.decodeResource(resources,R.drawable.station)
    private val trainSprites=listOf(R.drawable.engine_blue_v11,R.drawable.engine_yellow_v11,R.drawable.engine_pink_v11,R.drawable.engine_purple_v11,R.drawable.engine_green_v11,R.drawable.engine_orange_v11).map { BitmapFactory.decodeResource(resources,it) }
    private val robotSprites=listOf(R.drawable.robot_blue_0_v11,R.drawable.robot_yellow_0_v11,R.drawable.robot_pink_0_v11,R.drawable.robot_purple_0_v11,R.drawable.robot_green_0_v11,R.drawable.robot_orange_0_v11,R.drawable.robot_blue_1_v11,R.drawable.robot_yellow_1_v11,R.drawable.robot_pink_1_v11,R.drawable.robot_purple_1_v11,R.drawable.robot_green_1_v11,R.drawable.robot_orange_1_v11,R.drawable.robot_blue_2_v11,R.drawable.robot_yellow_2_v11,R.drawable.robot_pink_2_v11,R.drawable.robot_purple_2_v11,R.drawable.robot_green_2_v11,R.drawable.robot_orange_2_v11,R.drawable.robot_blue_3_v11,R.drawable.robot_yellow_3_v11,R.drawable.robot_pink_3_v11,R.drawable.robot_purple_3_v11,R.drawable.robot_green_3_v11,R.drawable.robot_orange_3_v11,R.drawable.robot_blue_4_v11,R.drawable.robot_yellow_4_v11,R.drawable.robot_pink_4_v11,R.drawable.robot_purple_4_v11,R.drawable.robot_green_4_v11,R.drawable.robot_orange_4_v11,R.drawable.robot_blue_5_v11,R.drawable.robot_yellow_5_v11,R.drawable.robot_pink_5_v11,R.drawable.robot_purple_5_v11,R.drawable.robot_green_5_v11,R.drawable.robot_orange_5_v11,R.drawable.robot_blue_6_v11,R.drawable.robot_yellow_6_v11,R.drawable.robot_pink_6_v11,R.drawable.robot_purple_6_v11,R.drawable.robot_green_6_v11,R.drawable.robot_orange_6_v11,R.drawable.robot_blue_7_v11,R.drawable.robot_yellow_7_v11,R.drawable.robot_pink_7_v11,R.drawable.robot_purple_7_v11,R.drawable.robot_green_7_v11,R.drawable.robot_orange_7_v11).map { BitmapFactory.decodeResource(resources,it) }
    private val menuSprites=listOf(R.drawable.menu_starter_v8,R.drawable.menu_ads_v8,R.drawable.menu_shop_v8,R.drawable.menu_supply_v8,R.drawable.menu_wheel_v8,R.drawable.menu_daily_v8).map { BitmapFactory.decodeResource(resources,it) }
    private val hostParts=listOf(R.drawable.host_head_v7,R.drawable.host_body_v7,R.drawable.host_arm_left_v7,R.drawable.host_arm_right_v7,R.drawable.host_leg_left_v7,R.drawable.host_leg_right_v7).map { BitmapFactory.decodeResource(resources,it) }
    private val wagonSprites=listOf(R.drawable.carriage_blue_v11,R.drawable.carriage_yellow_v11,R.drawable.carriage_pink_v11,R.drawable.carriage_purple_v11,R.drawable.carriage_green_v11,R.drawable.carriage_orange_v11).map { BitmapFactory.decodeResource(resources,it) }
    private var game=Game.restore(prefs.getString("session","") ?: "")?.takeIf { it.level==economy.level } ?: Game(economy.level)
    private var screen="splash"
    private var modal=""
    private var time=0f
    private var last=0L
    private var flowDelta=0f
    private data class Merge(val start:PointF,var age:Float=0f,var previous:PointF=PointF(start.x,start.y),var facing:Float=0f)
    private val merges=java.util.IdentityHashMap<Passenger,Merge>()
    private var active=true
    private var scale=1f
    private var ox=0f
    private var oy=0f
    private var toast=""
    private var toastUntil=0f
    private var pressed:Hit?=null
    private var touchY=0f
    private var dragged=false
    private var shopScroll=0f
    private var shakeId=-1
    private var shakeUntil=0f
    private var hintId=-1
    private var selectedDock=0
    private val livePassengers=java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Passenger,Boolean>())
    private val waitingY=java.util.IdentityHashMap<Passenger,Float>()
    private var lastBoarded=0
    private var spinAge=-1f
    private var spinIndex=0
    private var spinAngle=0f
    private var spinStart=0f
    private var spinEnd=0f
    private var reward:Reward?=null
    private var rewardReturn=""
    private var selectedBooster=""
    private var selectedSpeed=2
    private var music=prefs.getBoolean("music",true)
    private var effects=prefs.getBoolean("effects",true)
    private var vibration=prefs.getBoolean("vibration",true)
    private var motion=prefs.getBoolean("motion",true)
    private data class Hit(val rect:RectF,val id:String,val action:()->Unit)
    private val hits=mutableListOf<Hit>()
    init {
        isFocusable=true;contentDescription="Train Pop"
        if(economy.repairLegacyDailySpin()) notice("Günlük hediyenden +1 çevirme hakkı çarka eklendi.")
        economy.finishSpin()?.let { reward=it;rewardReturn="wheel";modal="reward" }
        audio.effects=effects
        art.trainSprites=trainSprites;art.wagonSprites=wagonSprites;art.directionalPassengers=robotSprites.chunked(6);art.menuSprites=mapOf("starter" to menuSprites[0],"coin" to menuSprites[0],"ads" to menuSprites[1],"shop" to menuSprites[2],"supply" to menuSprites[3],"wheel" to menuSprites[4],"daily" to menuSprites[5])
    }
    private fun day()=java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(java.util.Date())
    fun pause() { active=false;last=0;audio.music(false);if(screen=="game" && !game.completed) prefs.edit().putString("session",game.checkpoint()).apply() }
    fun resume() { active=true;last=0;audio.music(music);invalidate();store.restore(false) }
    fun release() { breakAds.release();ads.release();art.release();audio.release();store.release();station.recycle();gardenArt.recycle();platformArt.recycle();gameBackdrop?.recycle();hostParts.forEach { it.recycle() };trainSprites.forEach { it.recycle() };wagonSprites.forEach { it.recycle() };robotSprites.forEach { it.recycle() };menuSprites.forEach { it.recycle() } }
    fun back() {
        if(spinAge>=0 || ads.showing || breakAds.showing) return
        when { modal=="win" -> { screen="home";modal="" };modal.startsWith("purchase:") -> modal="shop"; modal=="reward"->modal=rewardReturn;modal=="full" -> Unit; modal.isNotEmpty()->modal=""; screen=="game"->modal="leave";else->activity.finish() }
        invalidate()
    }
    private fun notice(s:String) { activity.runOnUiThread { toast=s;toastUntil=time+3f;invalidate() } }
    private fun open(name:String) { modal=name;shopScroll=0f;pressed=null }
    private fun start(fresh:Boolean=true) { if(fresh || game.level!=economy.level || game.completed) game=Game(economy.level);screen="game";modal=if(game.failed) "full" else "";hintId=-1;selectedDock=(0 until game.platformCount).firstOrNull { game.platforms[it]==null } ?: 0;lastBoarded=game.boarded;waitingY.clear();merges.clear() }
    private fun showReward(r:Reward?) { if(r!=null) { reward=r;rewardReturn=if(modal in setOf("wheel","daily","supply","shop","gold")) modal else "";modal="reward";audio.play("win") } else notice("Bu ödül zaten alındı.") }
    private fun haptic() { if(vibration) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    private fun hit(x:Float,y:Float,w:Float,h:Float,id:String,action:()->Unit) { hits+=Hit(RectF(x,y,x+w,y+h),id,action) }
    private fun button(c:Canvas,x:Float,y:Float,w:Float,h:Float,s:String,color:Int=0xFF41D62F.toInt(),id:String=s,enabled:Boolean=true,action:()->Unit) {
        val pushed=pressed?.id==id;val dy=if(pushed) 4f else 0f;val col=if(enabled) color else 0xFFA8B1C6.toInt()
        art.box(c,x,y+9,w,h,0x30203547,16f)
        art.box(c,x,y+7,w,h,art.shade(col,.64f),16f)
        art.gradient(c,x,y+dy,w,h,col,art.shade(col,.91f),16f)
        art.box(c,x+8,y+5+dy,w-16,3f,0x44FFFFFF,2f)
        val size=min(25f,w/(s.length*.57f+1)).coerceAtLeast(11f)
        art.label(c,s,x+w/2,y+h/2+size*.34f+dy,size,Color.WHITE,true)
        if(enabled) hit(x-2,y-3,w+4,h+12,id,action)
    }
    private fun iconButton(c:Canvas,x:Float,y:Float,kind:String,action:()->Unit) {
        button(c,x,y,42f,42f,"",art.colors[0],kind,action=action)
        art.icon(c,kind,x+21,y+21,.65f)
    }
    override fun onDraw(c:Canvas) {
        super.onDraw(c)
        val now=System.nanoTime();val dt=if(last==0L) 0f else ((now-last)/1e9f).coerceAtMost(.25f);last=now
        if(active) {
            flowDelta=0f
            time+=dt
            if(screen=="splash" && time>1.0f) screen="loading"
            if(screen=="loading" && time>2.1f) screen="home"
            if(spinAge>=0) {
                spinAge+=dt;val t=(spinAge/4f).coerceIn(0f,1f);spinAngle=spinStart+(spinEnd-spinStart)*(1-(1-t).pow(4))
                if(t==1f) { spinAge=-1f;showReward(economy.finishSpin()) }
            }
            if(screen=="game" && modal.isEmpty() && !ads.showing && !breakAds.showing) {
                flowDelta=dt*game.flowSpeed
                val prevDepart=game.departing.size
                var remaining=dt
                while(remaining>0f) { val step=min(.02f,remaining);game.update(step);remaining-=step }
                if(game.boarded>lastBoarded) { lastBoarded=game.boarded;audio.play("board") }
                if(game.departing.size>prevDepart) audio.play("depart")

                if(game.completed && economy.win(game.level)) { prefs.edit().remove("session").apply();modal="win";audio.play("win");haptic() }
                else if(game.failed) { modal="full";audio.play("error") }
            }
        }
        scale=min(width/400f,height/850f);ox=(width-400*scale)/2;oy=(height-850*scale)/2
        c.drawColor(art.ink);c.save();c.translate(ox,oy);c.scale(scale,scale);hits.clear()
        when(screen) {
            "splash"->{ art.gradient(c,0f,0f,400f,850f,0xFF152957.toInt(),0xFF41317D.toInt(),0f);art.label(c,"axio",200f,424f,68f,Color.WHITE);art.label(c,"✦",253f,361f,28f,art.colors[1]);art.caption(c,"LET’S PLAY",200f,466f,12f,0xFFC6D5F4.toInt()) }
            "loading"->{ backdrop(c);logo(c,200f,210f);art.label(c,"İstasyon hazırlanıyor",200f,755f,18f,Color.WHITE,true);art.box(c,52f,783f,296f,14f,art.ink);art.gradient(c,55f,786f,290f*((time-1f)/1.1f).coerceIn(0f,1f),8f,0xFF94F4FF.toInt(),art.colors[0],5f) }
            "home"->home(c)
            "game"->board(c)
        }
        if(modal.isNotEmpty()) dialog(c)
        if(ads.showing && ads.preparing) {
            hits.clear();art.box(c,0f,0f,400f,850f,0x99102337.toInt(),0f)
            art.box(c,57f,361f,286f,109f,0xFFF9FBFF.toInt(),22f)
            art.icon(c,"wheel",200f,395f,.65f)
            art.label(c,"Reklam hazırlanıyor…",200f,444f,18f)
        }
        if(time<toastUntil) { art.box(c,17f,803f,366f,35f,0xF0184665.toInt(),12f);art.caption(c,toast,200f,826f,12f,Color.WHITE) }
        c.restore();if(active) postInvalidateOnAnimation()
    }
    private fun backdrop(c:Canvas) { art.p.shader=null;art.p.alpha=255;art.p.isFilterBitmap=true;c.drawBitmap(station,null,RectF(0f,0f,400f,850f),art.p);art.gradient(c,0f,0f,400f,125f,0x55356491,0x00356491,0f) }
    private fun logo(c:Canvas,x:Float,y:Float) { art.label(c,"TRAIN",x,y,48f,0xFFFFD459.toInt(),true);art.label(c,"POP",x,y+45,52f,0xFFFDFDF6.toInt(),true) }
    private fun header(c:Canvas,controls:Boolean=true) {
        art.box(c,24f,34f,112f,32f,0xFFC6C9EB.toInt());art.box(c,24f,30f,112f,32f,Color.WHITE);art.coin(c,28f,45f,1.05f);art.label(c,"${economy.coins}",84f,53f,22f)
        if(controls) { button(c,35f,53f,25f,23f,"+",art.colors[4],"goldplus") { open("gold") };iconButton(c,340f,27f,"settings") { open("settings") } }
    }
    private fun home(c:Canvas) {
        backdrop(c);header(c);logo(c,200f,143f)
        val entries=listOf("starter" to "Başlangıç","ads" to "Reklamsız","shop" to "Mağaza","supply" to "Tedarik","wheel" to "Döndür","daily" to "Günlük Giriş")
        entries.forEachIndexed { i,(kind,name)->
            if((kind=="starter" && economy.welcomeClaimed) || (kind=="ads" && economy.noAds)) return@forEachIndexed
            val x=if(i<3) 49f else 351f;val y=263f+(i%3)*94f
            art.circle(c,x,y+3,38f,0x40203442);art.circle(c,x,y,38f,0xFF24485E.toInt())
            art.circle(c,x,y,32f,0xFF346276.toInt())
            if(kind=="wheel") { val phase=(time%2f/.65f).coerceAtMost(1f);c.save();c.rotate(if(motion) phase*360 else 0f,x,y);art.icon(c,kind,x,y,1.05f);c.restore() } else art.icon(c,kind,x,y,1.05f)
            art.box(c,x-46,y+26,92f,25f,0x40203442,10f);art.box(c,x-46,y+23,92f,25f,0xFF24485E.toInt(),10f);art.label(c,name,x,y+41,if(name.length>10) 12f else 15f,Color.WHITE)
            val badge=kind=="starter" && !economy.welcomeClaimed || kind=="daily" && economy.dailyAvailable(day()) || kind=="supply" && economy.supplyStep(day())<5 || kind=="wheel" && economy.spins(day())>0
            if(badge) { art.circle(c,x-30,y-27,11f,0xFFFF752C.toInt());art.label(c,"!",x-30,y-22,17f,Color.WHITE,true) }
            hit(x-47,y-40,94f,94f,kind) { open(kind) }
        }
        val rx=87f+(if(motion) sin(time*.9f)*3 else 0f)
        mascot(c,rx,613f,122f)
        hit(rx-61f,521f,122f,182f,"greeting") { welcomeUntil=time+4f;haptic() }
        val sayings=listOf("Hoş geldiniz!","Haydi, trene binelim!","Robotlar seni bekliyor!")
        art.box(c,145f,608f,236f,37f,0xEEFFFFFF.toInt(),14f);art.label(c,if(time<welcomeUntil) "Hoş geldiniz!" else sayings[(time/6).toInt()%3],263f,632f,14f)
        art.box(c,143f,555f,114f,28f,0xE9184665.toInt(),8f);art.label(c,"BÖLÜM ${economy.level}",200f,575f,16f,Color.WHITE)
        button(c,65f,722f,270f,66f,if(game.moves>0 && game.level==economy.level) "Devam Et" else "Oyna",0xFF52E42E.toInt(),"play") { start(false) }
    }
    private fun mascot(c:Canvas,x:Float,y:Float,w:Float) {
        val age=if(motion) time else 0f
        val breath=if(motion) sin(age*2f)*1.1f else 0f
        val lift=if(motion) (sin(age*.9f)+1f)*.5f else .3f
        c.save();c.translate(x,y);c.scale(w/115f,w/115f)
        art.p.shader=null;art.p.color=0x250B3249;art.p.style=Paint.Style.FILL
        c.drawOval(-32f,67f,32f,76f,art.p)
        fun part(index:Int,xp:Float,yp:Float,pw:Float,ph:Float,angle:Float=0f) {
            c.save();c.translate(xp,yp);c.rotate(angle)
            art.p.shader=null;art.p.colorFilter=null;art.p.alpha=255;art.p.isFilterBitmap=true
            c.drawBitmap(hostParts[index],null,RectF(-pw/2,0f,pw/2,ph),art.p);c.restore()
        }
        part(4,-13f,31f,31f,41f,if(motion) sin(age*2f)*1.5f else 0f)
        part(5,13f,31f,31f,41f,if(motion) -sin(age*2f)*1.5f else 0f)
        // Arms pivot independently around shoulder sockets.
        part(3,23f,-23f+breath,23f,48f,if(motion) -14f+sin(age*2f)*7f else -14f)
        part(2,-23f,-23f+breath,23f,48f,110f*lift+if(motion) sin(age*7f)*19f*lift else 0f)
        part(1,0f,-30f+breath,51f,65f)
        c.save();c.translate(0f,-29f+breath)
        if(motion) { c.rotate(sin(age*1.7f)*4f);c.scale(1f,1f+sin(age*3.4f)*.025f) }
        art.p.alpha=255;c.drawBitmap(hostParts[0],null,RectF(-36f,-62f,36f,1f),art.p)
        c.restore();c.restore()
    }
    private fun garden(c:Canvas) {
        art.p.shader=null;art.p.alpha=255;art.p.isFilterBitmap=true
        c.drawBitmap(gardenArt,null,RectF(0f,0f,400f,333f),art.p)
    }
    private fun drawPassenger(c:Canvas,robot:Passenger,pos:PointF,facingOverride:Float?=null) {
        val next=queuePosition(robot.phase+.001f,robot.lane)
        val here=pos
        val facing=facingOverride ?: (atan2(next.x-here.x,next.y-here.y)*180f/PI.toFloat())
        art.robot(c,pos.x,pos.y,art.colors[robot.color],.48f,facing=facing)
    }
    private val routeNames=listOf("KIYI","VADİ","ŞEHİR","GECE","EK HAT","EK HAT","EK HAT")
    private fun dockPosition(slot:Int):PointF = if(slot<4) PointF(57f+slot*95f,411f) else PointF(67f+(slot-4)*133f,480f)
    private fun slotX(slot:Int)=dockPosition(slot).x
    private fun yardPosition(t:Train):PointF { val cell=43f;return PointF(65f+(t.x+(if(t.direction.dx!=0) (t.length-1)/2f else 0f))*cell,523f+(t.y+(if(t.direction.dy!=0) (t.length-1)/2f else 0f))*cell) }
    private fun stationDocks(c:Canvas) {
        // Bridge and peron lights make the crowd-to-train relationship visible.
        for(i in 0..3) {
            val t=game.platforms[i]
            art.circle(c,57f+i*95,350f,3f,if(t==null) 0xFF8ABDB7.toInt() else art.colors[t.color])
        }
        val free=(0 until game.platformCount).firstOrNull { game.platforms[it]==null && game.departing.none { t -> t.slot==it } }
        for(i in 0..6) {
            val point=dockPosition(i);val t=game.platforms[i];val unlocked=i<game.platformCount
            if(i<4) {
                val x=point.x-44f
                if(i==free) { art.p.shader=null;art.p.style=Paint.Style.STROKE;art.p.strokeWidth=1.5f;art.p.color=0xFFFFD466.toInt();c.drawRoundRect(x-1,355f,x+89,465f,10f,10f,art.p);art.p.style=Paint.Style.FILL }
                art.caption(c,routeNames[i],point.x,366f,9f,0xFF254F58.toInt())
                if(t==null) {
                    art.circle(c,point.x,408f,12f,0xDD164B55.toInt())
                    art.caption(c,if(game.departing.any { it.slot==i }) "…" else "↓",point.x,414f,20f,0xFFE5C984.toInt())
                }
                art.caption(c,if(t==null) "HAZIR" else "${t.capacity-t.remaining}/${t.capacity}",point.x,460f,9f,0xFFF9E2A1.toInt())
                if(t!=null) { art.box(c,x+9,467f,70f,3f,0xFF132F3B.toInt(),2f);art.box(c,x+9,467f,70f*(t.capacity-t.remaining)/t.capacity,3f,art.colors[t.color],2f) }
                hit(x,354f,88f,115f,"route$i") { notice(if(t!=null) "${t.remaining} yolcu bekliyor" else "Depodaki trene dokun; peron otomatik seçilir.") }
            } else {
                val x=point.x-56f
                art.box(c,x,473f,112f,22f,0xFFC8DAD0.toInt(),7f)
                art.caption(c,if(unlocked) if(t==null) "Ek peron hazır" else "${t.remaining}" else "+ Ek peron",if(t!=null) point.x+44f else point.x,488f,10f,0xFF4C756B.toInt())
                hit(x,472f,112f,24f,"route$i") { if(!unlocked) open("slot") else notice("Ek peron otomatik kullanılır.") }
            }
        }
    }
    private fun cachedGameBackdrop():Bitmap {
        gameBackdrop?.let { return it }
        val bitmap=Bitmap.createBitmap(800,1700,Bitmap.Config.ARGB_8888)
        val c=Canvas(bitmap);c.scale(2f,2f)
        art.gradient(c,0f,0f,400f,850f,0xFFF4F3EA.toInt(),0xFFE0E9E2.toInt(),0f)
        garden(c)
        art.box(c,0f,0f,400f,333f,0xB8F1F3E8.toInt(),0f)
        c.save();c.clipRect(0f,84f,400f,327f)
        val path=Path();path.addOval(RectF(61f,179f,339f,299f),Path.Direction.CW)
        path.moveTo(61f,-20f);path.lineTo(61f,118f);path.quadTo(61f,171f,100f,195f)
        path.moveTo(339f,-20f);path.lineTo(339f,118f);path.quadTo(339f,171f,300f,195f)
        c.save();c.translate(0f,5f);art.p.style=Paint.Style.STROKE;art.p.strokeWidth=86f;art.p.color=0x24364646;c.drawPath(path,art.p);c.restore()
        art.p.strokeWidth=81f;art.p.color=0xFFACC1B5.toInt();c.drawPath(path,art.p)
        art.p.strokeWidth=77f;art.p.color=0xFFF9F6E9.toInt();c.drawPath(path,art.p)
        art.p.strokeWidth=70f;art.p.shader=LinearGradient(0f,120f,0f,325f,0xFFAEC5BC.toInt(),0xFF8DAFA4.toInt(),Shader.TileMode.CLAMP);c.drawPath(path,art.p)
        art.p.shader=null;art.p.style=Paint.Style.FILL
        art.gradient(c,159f,314f,82f,10f,0xFFFFE698.toInt(),0xFFBE902B.toInt(),4f);c.restore()
        art.box(c,0f,333f,400f,165f,0xFFDCE7DF.toInt(),0f)
        art.box(c,191f,322f,18f,20f,0xFFCCAE69.toInt(),3f)
        art.box(c,48f,337f,304f,11f,0xFF153A43.toInt(),4f)
        art.gradient(c,48f,333f,304f,11f,0xFFF0E6C7.toInt(),0xFFBCAB7A.toInt(),4f)
        for(i in 0..3) { art.line(c,57f+i*95,342f,57f+i*95,357f,0xFFE0D5B3.toInt(),8f) }
        val platformTint=ColorMatrix(floatArrayOf(.80f,0f,0f,0f,30f, 0f,.82f,0f,0f,33f, 0f,0f,.80f,0f,29f, 0f,0f,0f,1f,0f))
        art.p.colorFilter=ColorMatrixColorFilter(platformTint)
        for(i in 0..3) { art.p.shader=null;art.p.alpha=255;art.p.isFilterBitmap=true;c.drawBitmap(platformArt,null,RectF(13f+i*95,356f,101f+i*95,465f),art.p) }
        art.p.colorFilter=null
        art.box(c,14f,508f,372f,255f,0xFFA9BDB2.toInt(),20f)
        art.gradient(c,14f,502f,372f,255f,0xFFF7F5E9.toInt(),0xFFE4EBDF.toInt(),20f)
        art.box(c,24f,513f,352f,232f,0xFFEFF1E7.toInt(),14f)
        for(row in 0..5) art.line(c,29f,516f+row*42,371f,516f+row*42,0x11718B92,1f)
        for(col in 0..6) art.line(c,43f+col*51,517f,43f+col*51,725f,0x08718B92,1f)
        for(side in listOf(19f,381f)) { art.line(c,side,529f,side,746f,0xFFB7C9BE.toInt(),3f);art.line(c,side-1,529f,side-1,746f,0xFFDCE7E4.toInt(),1f) }
        gameBackdrop=bitmap;return bitmap
    }
    private fun route(t:Train):PointF {
        val start=yardPosition(t);val p=t.progress
        val exit=PointF(if(t.direction.dx<0) -40f else if(t.direction.dx>0) 440f else start.x,if(t.direction.dy<0) 440f else if(t.direction.dy>0) 785f else start.y)
        if(p<.42f) { val raw=p/.42f;val q=raw*raw*(3-2*raw);return PointF(start.x+(exit.x-start.x)*q,start.y+(exit.y-start.y)*q) }
        val raw=(p-.42f)/.58f;val q=raw*raw*(3-2*raw);val dock=dockPosition(t.slot);val target=PointF(dock.x-if(t.slot>=4) 15f else 0f,dock.y+4f)
        // A quadratic turn into the station. Position is continuous at the yard boundary.
        val control=PointF(exit.x,335f)
        return PointF((1-q).pow(2)*exit.x+2*(1-q)*q*control.x+q*q*target.x,(1-q).pow(2)*exit.y+2*(1-q)*q*control.y+q*q*target.y)
    }
    private fun board(c:Canvas) {
        art.p.shader=null;art.p.alpha=255;art.p.isFilterBitmap=true
        c.drawBitmap(cachedGameBackdrop(),null,RectF(0f,0f,400f,850f),art.p)
        button(c,16f,24f,42f,42f,"",0xFF8CAFA0.toInt(),"restart") { open("restart") };art.icon(c,"restart",37f,45f,.60f)
        button(c,342f,24f,42f,42f,"",0xFF8CAFA0.toInt(),"pause") { open("settings") };art.icon(c,"pause",363f,45f,.60f)
        art.caption(c,"BÖLÜM",200f,34f,10f,0xFF66867A.toInt());art.label(c,"${game.level}",200f,59f,26f,0xFF355C50.toInt())
        art.box(c,118f,72f,164f,7f,0xFFD2DFD3.toInt(),5f);art.box(c,118f,72f,164f*game.boarded/game.total,7f,0xFF74AC94.toInt(),5f)
        c.save();c.clipRect(0f,84f,400f,326f)
        val waiting=game.passengers.filter { it.phase<0f }
        livePassengers.clear();livePassengers.addAll(game.passengers)
        waitingY.keys.retainAll(livePassengers);merges.keys.retainAll(livePassengers)
        data class Sprite(val robot:Passenger,val point:PointF,val waiting:Boolean,val facing:Float?=null)
        val sprites=mutableListOf<Sprite>();val ranks=IntArray(8)
        waiting.forEach { robot ->
            val target=ConveyorGeometry.inletEndY-ranks[robot.lane]++*12.5f
            val old=waitingY[robot] ?: target;val yy=old+(target-old).coerceIn(-flowDelta*35f,flowDelta*35f)
            waitingY[robot]=yy;val offset=(robot.lane%4-1.5f)*15.5f
            if(yy>64f) sprites+=Sprite(robot,PointF(if(robot.lane<4) 61f-offset else 339f+offset,yy),true)
        }
        game.passengers.filter { it.phase>=0f }.forEach { robot ->
            val target=queuePosition(robot.phase,robot.lane)
            waitingY.remove(robot)?.let { yy -> val offset=(robot.lane%4-1.5f)*15.5f;merges[robot]=Merge(PointF(if(robot.lane<4) 61f-offset else 339f+offset,yy)) }
            val transition=merges[robot]
            val pos=if(transition!=null) {
                transition.age+=flowDelta;val u=(transition.age/1.1f).coerceIn(0f,1f);val q=u*u*(3-2*u)
                if(u>=1f) merges.remove(robot)
                PointF(transition.start.x+(target.x-transition.start.x)*q,transition.start.y+(target.y-transition.start.y)*q)
            } else target
            val facing=transition?.let { merge ->
                val dx=pos.x-merge.previous.x;val dy=pos.y-merge.previous.y
                if(flowDelta>0f && hypot(dx,dy)>.02f) merge.facing=atan2(dx,dy)*180f/PI.toFloat()
                merge.previous=PointF(pos.x,pos.y);merge.facing
            }
            sprites+=Sprite(robot,pos,false,facing)
        }
        // Queue and moving robots share the same ground-depth ordering. Inlets stop above the loop.
        sprites.sortedBy { it.point.y }.forEach { if(it.waiting) art.robot(c,it.point.x,it.point.y,art.colors[it.robot.color],.48f) else drawPassenger(c,it.robot,it.point,it.facing) }
        c.restore()
        if(game.level==1 && game.moves==0) art.caption(c,"Renkleri eşleştir • Çıkışı açık trenlere dokun",200f,98f,11f)
        button(c,145f,220f,110f,28f,if(game.flowSpeed==1) "Hızlandır ⚡" else "${game.flowSpeed}× Hız",0xFF94B6A3.toInt(),"flowSpeed") { selectedSpeed=if(game.flowSpeed<3) game.flowSpeed+1 else 3;open("speed") }
        stationDocks(c)
        game.yard.sortedBy { it.y }.forEach { t->
            val pos=yardPosition(t);val shake=if(t.id==shakeId && time<shakeUntil) sin(time*60)*4 else 0f
            if(t.id==hintId) { art.circle(c,pos.x,pos.y,31f+(if(motion) sin(time*4)*3 else 0f),0x77FFFFFF);art.circle(c,pos.x,pos.y,27f,0x66FFDD43) }
            art.train(c,pos.x+shake,pos.y,art.colors[t.color],if(t.length==2) 82f else 42f,28f,t.direction.angle)
            val horizontal=t.direction.dx!=0;val w=if(horizontal && t.length==2) 86f else 41f;val h=if(!horizontal && t.length==2) 86f else 41f
            hit(pos.x-w/2,pos.y-h/2,w,h,"train${t.id}") {
                when(game.dispatch(t.id)) {
                    Result.MOVED->{ hintId=-1;selectedDock=(0 until game.platformCount).firstOrNull { game.platforms[it]==null && game.departing.none { t -> t.slot==it } } ?: selectedDock;haptic();audio.play("depart") }
                    Result.BLOCKED->{ shakeId=t.id;shakeUntil=time+.35f;audio.play("error");notice("Bu trenin çıkışı kapalı.") }
                    Result.FULL->notice("Seçili hat dolu. Boş bir hat seç.")
                    Result.BUSY->Unit
                }
            }
        }
        game.platforms.filterNotNull().forEach { t->
            val pos=if(t.phase==TrainPhase.ARRIVING) route(t) else dockPosition(t.slot).let { PointF(it.x-if(t.slot>=4) 15f else 0f,it.y+4f) }
            val turn=((t.progress-.42f)/.58f).coerceIn(0f,1f);val ease=turn*turn*(3-2*turn)
            val difference=(((if(t.slot>=4) 90f else 0f)-t.direction.angle+540f)%360f)-180f
            val angle=if(t.phase==TrainPhase.ARRIVING) t.direction.angle+difference*ease else if(t.slot>=4) 90f else 0f
            art.train(c,pos.x,pos.y,art.colors[t.color],if(t.length==2) 78f else 49f,27f,angle,false)

        }
        game.departing.forEach { t -> val point=if(t.slot<0) PointF(35f,350f) else dockPosition(t.slot).let { PointF(it.x-if(t.slot>=4) 15f else 0f,it.y+4f) };art.train(c,point.x+(430-point.x)*t.progress,point.y,art.colors[t.color],if(t.length==2) 78f else 49f,27f,90f*(t.progress/.3f).coerceAtMost(1f),false) }
        game.boarding.forEach { b ->
            val q=(b.age/.5f).coerceAtMost(1f);val target=dockPosition(b.slot)
            val tx=target.x-if(b.slot>=4) 15f else 0f
            val x=if(q<.28f) 200f else if(q<.65f) 200f+(tx-200f)*(q-.28f)/.37f else tx
            val y=if(q<.28f) 299f+(b.lane%4-1.5f)*15.5f+(338f-299f-(b.lane%4-1.5f)*15.5f)*q/.28f else if(q<.65f) 338f else 338f+(target.y+4f-338f)*(q-.65f)/.35f
            art.robot(c,x,y,art.colors[b.color],.42f*(1-q*.35f),facing=if(q in .28f.. .65f) if(tx<200f) 270f else 90f else 0f)
        }
        val names=listOf("VIP","Düzenle","Karıştır");val keys=listOf("vip","arrange","shuffle")
        keys.forEachIndexed { i,key->val x=20f+i*126;val yy=765f;val down=pressed?.id=="boost$key";val offset=if(down) 4f else 0f
            val col=if(i==0) 0xFFE4D5B7.toInt() else if(i==1) 0xFFD9D5E7.toInt() else 0xFFC9DDD8.toInt()
            art.box(c,x,yy+8,108f,65f,0x30203442,15f)
            art.box(c,x,yy+7,108f,63f,art.shade(col,.86f),15f)
            art.gradient(c,x,yy+offset,108f,63f,art.shade(col,1.05f),col,15f)
            art.icon(c,key,x+54,yy+20+offset,.68f)
            art.label(c,names[i],x+54,yy+57+offset,15f,0xFF3A6057.toInt())
            val amount=economy.inventory(key)
            art.circle(c,x+98,yy+3+offset,12f,if(amount>0) 0xFF769F8B.toInt() else 0xFF8DAFA4.toInt())
            art.label(c,if(amount>0) "$amount" else "+",x+98,yy+8+offset,14f,Color.WHITE,true)
            hit(x-3,yy-7,114f,81f,"boost$key") { selectedBooster=key;if(amount>0) useBooster() else open("booster") }
        }
    }
    private fun useBooster() {
        val key=selectedBooster
        val applied=economy.consume(key) { when(key) { "vip"->game.vip();"arrange"->game.arrange();else->game.shuffle() } }
        if(applied) { modal="";audio.play("depart");haptic();notice(when(key) { "vip"->"VIP ekspres yola çıktı!";"arrange"->"Robotlar bekleyen trene yönlendirildi.";else->"Tren renkleri yeniden düzenlendi." }) }
        else notice("Şu anda uygun tren yok; hakkın korunuyor.")
    }
    private fun queuePosition(phase:Float,lane:Int):PointF {
        val point=ConveyorGeometry.position(phase,lane)
        return PointF(point.x,point.y)
    }
    override fun onTouchEvent(e:MotionEvent):Boolean {
        if(ads.showing || breakAds.showing) return true
        val x=(e.x-ox)/scale;val y=(e.y-oy)/scale
        when(e.actionMasked) {
            MotionEvent.ACTION_DOWN->{ touchY=y;dragged=false;pressed=hits.lastOrNull { it.rect.contains(x,y) };invalidate() }
            MotionEvent.ACTION_MOVE->{ if(modal=="shop" && abs(y-touchY)>5) { shopScroll=(shopScroll+touchY-y).coerceIn(0f,270f);touchY=y;dragged=true;pressed=null;invalidate() } }
            MotionEvent.ACTION_UP->{ val selected=pressed;pressed=null;if(!dragged && selected!=null && selected.rect.contains(x,y)) { audio.play("tap");selected.action();performClick() };invalidate() }
            MotionEvent.ACTION_CANCEL->{ pressed=null;invalidate() }
        }
        return true
    }
    override fun performClick():Boolean { super.performClick();return true }

    private fun dialog(c:Canvas) {
        hits.clear();art.box(c,0f,0f,400f,850f,0xB8102337.toInt(),0f)
        if(modal=="shop" || modal=="gold") { shop(c,modal=="gold");return }
        if(modal=="wheel") { wheelScreen(c);return }
        val title=when {
            modal.startsWith("purchase:")->productTitle(modal.substringAfter(':'))
            else->when(modal) { "starter"->"Başlangıç Hediyesi";"ads"->"Reklamsız Paket";"daily"->"Günlük Bonus";"supply"->"Günlük İkmal";"settings"->"Ayarlar";"win"->"Harika!";"full"->"Boş yer yok";"restart"->"Yeniden Başlat";"leave"->"Ana Sayfa";"slot"->"Ek Hat";"speed"->"Akış hızını seç";"booster"->when(selectedBooster) {"vip"->"VIP Ekspres";"arrange"->"Düzenle";else->"Karıştır"};"reward"->"Ödülün hazır!";else->"Train Pop" }
        }
        val tall=modal=="daily" || modal=="supply" || modal=="settings"
        val y=if(tall) 120f else 213f;val h=if(tall) 626f else 420f
        art.panel(c,24f,y,352f,h,if(screen=="game") 0xFF77978C.toInt() else 0xFF285466.toInt());art.label(c,title,200f,y+35,if(title.length>17) 21f else 26f,Color.WHITE,true)
        if(modal!="full" && modal!="win") button(c,337f,y+5,29f,29f,"×",art.colors[5],"close",enabled=spinAge<0) { modal=if(modal=="reward") rewardReturn else "" }
        when {
            modal=="daily"->daily(c,y)
            modal=="supply"->supply(c,y)
            modal=="settings"->settings(c,y)
            modal=="starter"->welcome(c,y)
            modal=="ads" || modal.startsWith("purchase:")->purchase(c,y)
            modal=="reward"->{ reward?.let { rewardArt(c,it,200f,y+145);art.label(c,it.label(),200f,y+227,19f) };button(c,69f,y+315,262f,53f,"Harika!") { modal=rewardReturn } }
            modal=="win"->{
                for(i in 0..19) { val xx=40f+(i*37%320);val yy=y+70f+(time*70+i*23)%180;art.box(c,xx,yy,5f,10f,art.colors[i%6],1f) }
                mascot(c,200f,y+143,92f)
                art.label(c,"Seviye ${game.level} tamamlandı",200f,y+224,20f);art.coin(c,177f,y+259,.8f);art.label(c,"+50",222f,y+267,23f)
                button(c,62f,y+315,276f,55f,"Devam",enabled=!breakAds.showing) { breakAds.betweenLevels(game.level) { start() } }
            }
            modal=="restart" || modal=="leave"->{ art.icon(c,"restart",200f,y+139,2f);art.caption(c,if(modal=="restart") "Bu bölümdeki hamleler sıfırlanacak." else "Bölüme sonra devam edebilirsin.",200f,y+223,15f);button(c,49f,y+316,142f,52f,"Hayır",art.colors[5]) { modal="" };button(c,209f,y+316,142f,52f,"Evet") { if(modal=="restart") start() else { prefs.edit().putString("session",game.checkpoint()).apply();screen="home";modal="" } } }
            modal=="full"->{ art.icon(c,"arrange",200f,y+129,1.6f);art.caption(c,"Bekleyen trenleri gönder",200f,y+204,19f);art.caption(c,"ve bir ek peron aç!",200f,y+230,19f);button(c,49f,y+273,142f,47f,"900 altın",art.colors[1]) { if(economy.spend(900) { game.rescue() }) modal="" else notice("Yeterli altın yok.") };button(c,209f,y+273,142f,47f,"Reklam İzle") { ads.show { if(game.rescue()) modal="" } };button(c,69f,y+343,262f,39f,"Yeniden Başlat",art.colors[0]) { start() } }
            modal=="slot"->{ art.icon(c,"supply",200f,y+143,1.8f);art.caption(c,"Bu bölüm için yeni bir hat aç.",200f,y+232,16f);button(c,49f,y+310,142f,50f,"150 altın",art.colors[1]) { if(economy.spend(150) { game.addPlatform() }) modal="" else notice("Altın yetersiz veya tüm peronlar açık.") };button(c,209f,y+310,142f,50f,"Reklam İzle") { ads.show { game.addPlatform();modal="" } } }
            modal=="speed"->speedOffer(c,y)
            modal=="booster"->booster(c,y)
        }
    }
    private fun rewardArt(c:Canvas,r:Reward,x:Float,y:Float) {
        val kind=if(r.coins>0) "coin" else if(r.vip>0) "vip" else if(r.arrange>0) "arrange" else if(r.spins>0) "wheel" else "shuffle"
        art.icon(c,kind,x,y,1.6f)
    }
    private fun daily(c:Canvas,y:Float) {
        val idx=economy.dailyIndex();val available=economy.dailyAvailable(day());val displayIndex=if(available) idx else (idx+6)%7
        economy.dailyRewards.forEachIndexed { i,r->val x=if(i==6) 48f else 48f+(i%3)*104;val yy=y+77+(i/3)*142;val w=if(i==6) 304f else 96f;val h=if(i==6) 109f else 132f
            art.gradient(c,x,yy,w,h,if(i==displayIndex) 0xFFFFD15A.toInt() else 0xFFEFE7CB.toInt(),0xFFFFF9E3.toInt(),12f)
            art.label(c,"Gün ${i+1}",x+w/2,yy+24,17f)
            val claimed=if(available) i<idx else i<=displayIndex
            if(claimed) art.label(c,"✓",x+w/2,yy+79,39f,art.colors[4]) else { art.icon(c,if(r.coins>0) "coin" else if(r.vip>0) "vip" else if(r.arrange>0) "arrange" else if(r.spins>0) "wheel" else "shuffle",x+w/2,yy+61,.78f);art.label(c,if(r.coins>0) "${r.coins}" else if(r.spins>0) "+1 Çevirme" else "×1",x+w/2,yy+106,18f) }
            if(i==6 && !claimed) { art.caption(c,"+1 Düzenle",x+w/2+95,yy+72,13f) }
        }
        button(c,72f,y+503,256f,51f,if(available) "Ödülü Al" else "Alındı",enabled=available) { showReward(economy.claimDaily(day())) }
        art.caption(c,"Yeni ödül her gün • 7 günlük döngü",200f,y+594,12f)
    }
    private fun supply(c:Canvas,y:Float) {
        val step=economy.supplyStep(day());val values=listOf(15,20,50,70,100)
        values.forEachIndexed { i,amount->val yy=y+76+i*89
            art.gradient(c,43f,yy,314f,76f,0xFFE8E3FF.toInt(),0xFFC6C6F3.toInt(),12f);art.coinPile(c,80f,yy+47,.8f);art.label(c,"×$amount",139f,yy+46,24f)
            val label=if(i<step) "Alındı" else if(i==0) "Ücretsiz" else if(i==step) "İzle ▶" else "Kilitli"
            button(c,207f,yy+13,133f,41f,label,enabled=i==step) {
                if(i==0) showReward(economy.claimSupply(day())) else ads.show { showReward(economy.claimSupplyAd(day(),i)) }
            }
        }
        art.caption(c,"İlk ikmal ücretsiz; diğerleri reklam ödülü.",200f,y+558,12f)
        art.caption(c,"Her gün yenilenir.",200f,y+589,13f)
    }
    private fun settings(c:Canvas,y:Float) {
        val labels=listOf("Müzik","Ses","Titreşim","Animasyonlar")
        val values=listOf(music,effects,vibration,motion)
        labels.forEachIndexed { i,label->val yy=y+83+i*70;art.label(c,label,125f,yy+22,22f)
            button(c,229f,yy-6,99f,40f,if(values[i]) "Açık" else "Kapalı",if(values[i]) art.colors[4] else 0xFFA6ADBD.toInt(),"toggle$i") {
                when(i) { 0->{music=!music;audio.music(music)};1->{effects=!effects;audio.effects=effects};2->vibration=!vibration;3->motion=!motion }
                prefs.edit().putBoolean("music",music).putBoolean("effects",effects).putBoolean("vibration",vibration).putBoolean("motion",motion).apply()
            }
        }
        button(c,49f,y+390,142f,46f,"Ana Sayfa",art.colors[5]) { if(screen=="game") open("leave") else modal="" }
        button(c,209f,y+390,142f,46f,if(screen=="game") "Yeniden" else "Oyna") { if(screen=="game") open("restart") else start() }
        button(c,63f,y+463,274f,43f,"Satın Almaları Geri Yükle",art.colors[0]) { store.restore() }
        if(consent.privacyRequired) button(c,80f,y+514,240f,28f,"Reklam Gizlilik Tercihleri",art.colors[0]) { consent.privacyOptions() }
        art.caption(c,"AXIO • Train Pop ${BuildConfig.VERSION_NAME}",200f,y+555,13f)
        art.caption(c,if(BuildConfig.DEBUG) "Test reklamları etkin" else "",200f,y+581,11f)
    }
    private fun productTitle(id:String)=when(id) { "vip"->"VIP Ekspres";"arrange"->"Düzenle";"shuffle"->"Karıştır";"starter_pack"->"Başlangıç Paketi";"remove_ads"->"Reklamsız";"ad_free_bundle"->"Reklamsız Paket";"coins_500"->"500 Altın";else->"3.000 Altın" }
    private fun productReward(id:String)=when(id) {"starter_pack"->Reward(1000,2,2,2);"ad_free_bundle"->Reward(1500,1,1,1);"coins_500"->Reward(500);"coins_3000"->Reward(3000);else->Reward() }
    private fun welcome(c:Canvas,y:Float) {
        art.icon(c,"starter",200f,y+128,1.6f)
        art.label(c,"Hoş geldin hediyesi",200f,y+209,23f)
        art.caption(c,"150 altın + her güçlendirmeden 1 adet",200f,y+244,13f)
        art.caption(c,"Ücretsiz · Bir kez alınabilir",200f,y+278,13f)
        button(c,60f,y+331,280f,51f,if(economy.welcomeClaimed) "Alındı" else "Hediyemi al",enabled=!economy.welcomeClaimed) { showReward(economy.claimWelcome()) }
    }
    private fun purchase(c:Canvas,y:Float) {
        val id=when(modal) {"starter"->"starter_pack";"ads"->"ad_free_bundle";else->modal.substringAfter(':') }
        val r=productReward(id);art.icon(c,if(id.contains("ads") || id=="ad_free_bundle") "ads" else "coin",200f,y+128,1.6f)
        if(id=="remove_ads") { art.caption(c,"Zorunlu reklamları kalıcı olarak kaldırır.",200f,y+206,14f);art.caption(c,"İsteğe bağlı ödül reklamları devam eder.",200f,y+232,12f) } else if(id=="ad_free_bundle") { art.label(c,"${r.coins} altın + reklamsız",200f,y+206,22f);art.caption(c,"Zorunlu reklamları kalıcı olarak kaldırır.",200f,y+232,12f) }
        else art.label(c,"${r.coins} altın",200f,y+206,26f)
        if(r.vip>0) { art.box(c,48f,y+247,304f,55f,0xFFE7D5F8.toInt(),12f);listOf("vip","arrange","shuffle").forEachIndexed { i,k->art.icon(c,k,89f+i*104,y+271,.66f);art.label(c,"×${r.vip}",115f+i*104,y+284,15f) } }
        if(r.vip==0) { art.box(c,65f,y+263,270f,44f,0xFFE1E8D9.toInt(),12f);art.caption(c,if(id=="remove_ads") "Bölüm sonu reklamlarını kaldırır" else "Satın alınca altın bakiyene eklenir",200f,y+290,13f) }
        if(id=="ad_free_bundle") art.caption(c,"İsteğe bağlı ödül reklamları devam eder",200f,y+319,11f)
        val owned=id=="starter_pack" && economy.starterOwned || (id=="remove_ads" || id=="ad_free_bundle") && economy.noAds
        button(c,60f,y+331,280f,51f,if(owned) "Alındı" else "${store.price(id)} · Satın al",enabled=!owned) { store.buy(id) }
        art.caption(c,if(store.available(id)) "Güvenli ödeme" else "Satış henüz açılmadı · Planlanan fiyat",200f,y+408,10f)
    }
    private fun shop(c:Canvas,gold:Boolean) {
        art.gradient(c,0f,0f,400f,850f,0xFF214B5D.toInt(),0xFF132E42.toInt(),0f);header(c,false)
        // Header/settings actions must not leak into this modal.
        hits.clear();art.label(c,if(gold) "Altın" else "Mağaza",200f,118f,35f,Color.WHITE,true)
        button(c,342f,85f,36f,36f,"×",art.colors[5],"shopclose") { modal="" }
        val ids=if(gold) listOf("coins_500","coins_3000") else listOf("vip","arrange","shuffle","remove_ads","ad_free_bundle","starter_pack")
        c.save();c.clipRect(12f,147f,388f,767f)
        ids.forEachIndexed { i,id->val yy=154f+i*144-shopScroll;val col=if(id=="starter_pack") 0xFF997C45.toInt() else if(id.contains("ads") || id=="ad_free_bundle") 0xFF655583.toInt() else 0xFF367D89.toInt()
            art.gradient(c,18f,yy,364f,127f,art.shade(col,1.15f),col,18f);art.icon(c,if(economy.boosterCost(id)!=null) id else if(id.contains("ads") || id=="ad_free_bundle") "ads" else "coin",64f,yy+45,1.15f)
            art.label(c,productTitle(id),226f,yy+29,20f,Color.WHITE,true)
            val r=productReward(id);art.caption(c,if(economy.boosterCost(id)!=null) "1 adet · Altınla satın al" else if(id=="remove_ads") "Zorunlu reklamları kaldır" else if(r.vip>0) "${r.coins} altın + güçlendirmeler" else "${r.coins} altın",226f,yy+57,12f,Color.WHITE)
            val owned=id=="starter_pack" && economy.starterOwned || (id=="remove_ads" || id=="ad_free_bundle") && economy.noAds
            val hitBefore=hits.size
            button(c,135f,yy+74,229f,36f,if(owned) "Alındı" else economy.boosterCost(id)?.let { "$it Altın · Satın al" } ?: "${store.price(id)} · Satın al",id="buy$id",enabled=!owned) {
                if(economy.boosterCost(id)!=null) { if(economy.buyBooster(id)) { haptic();notice("+1 ${productTitle(id)} • Kalan ${economy.coins} altın") } else notice("Yeterli altın yok. Günlük ödüllerini alabilirsin.") } else open("purchase:$id")
            }
            if(hits.size>hitBefore) { val old=hits.last();if(!old.rect.intersect(RectF(12f,147f,388f,767f))) hits.removeAt(hits.lastIndex) }
        };c.restore()
        button(c,60f,787f,280f,39f,"Reklam İzle · +50 Altın",art.colors[4],"adgold") { ads.show { showReward(economy.grantAdCoins()) } }
    }
    private fun wheelScreen(c:Canvas) {
        art.gradient(c,0f,0f,400f,850f,0xFF326679.toInt(),0xFF182F45.toInt(),0f)
        art.label(c,"Şanslı Çark",200f,133f,34f,Color.WHITE,true)
        button(c,341f,89f,36f,36f,"×",art.colors[5],"wheelclose",enabled=spinAge<0) { modal="" }
        art.wheel(c,200f,373f,157f,spinAngle,economy.wheelRewards)
        art.box(c,45f,586f,310f,39f,0xFF42346F.toInt());art.label(c,"Çevirme hakkı: ${economy.spins(day())}",200f,612f,20f,Color.WHITE)
        button(c,80f,674f,240f,61f,if(spinAge>=0) "Dönüyor…" else "Döndür",enabled=spinAge<0 && economy.spins(day())>0) {
            spinIndex=Random.nextInt(6)
            if(economy.beginSpin(day(),spinIndex)) { spinAge=0f;spinStart=spinAngle;spinEnd=spinStart+1800f+((360-spinIndex*60-spinStart%360)%360+360)%360 }
        }
        button(c,73f,764f,254f,43f,"Reklam İzle · +1 Hak",art.colors[0],"adspin",enabled=spinAge<0 && economy.adSpinsLeft(day())>0) { ads.show { economy.grantSpin(day());notice("+1 çevirme hakkı!") } }
        art.caption(c,"Her gün 1 ücretsiz hak • Ödüller eşit şanslı",200f,650f,11f,Color.WHITE)
    }
    private fun speedOffer(c:Canvas,y:Float) {
        art.caption(c,"Reklamı tamamla, akışı hızlandır",200f,y+102,18f)
        for(multiplier in 2..3) {
            val x=if(multiplier==2) 48f else 209f
            val enabled=multiplier>game.flowSpeed && !ads.showing
            button(c,x,y+145,142f,82f,if(multiplier==game.flowSpeed) "${multiplier}× Etkin" else "▶ ${multiplier}× Hız",if(enabled) 0xFF739F8D.toInt() else 0xFFACBBB4.toInt(),"speed$multiplier",enabled=enabled) {
                val target=game
                ads.show { if(game===target && target.activateFlowSpeed(multiplier)) { prefs.edit().putString("session",target.checkpoint()).commit();modal="";notice("${multiplier}× robot akışı bu bölüm için etkin!") } }
            }
        }
        art.caption(c,"Hızı seçtiğinde ödüllü reklam açılır.",200f,y+275,14f)
        art.caption(c,"Reklam bitince bu bölüm boyunca etkinleşir.",200f,y+304,13f)
        art.caption(c,"Yeni bölümde normal hıza döner.",200f,y+333,13f)
    }
    private fun booster(c:Canvas,y:Float) {
        val key=selectedBooster;art.icon(c,key,200f,y+137,1.8f)
        val lines=when(key) {"vip"->listOf("Sıradaki renkte bir treni", "çıkışı kapalı olsa da gönder.");"arrange"->listOf("Bekleyen bir trenin robotlarını", "biniş kapısına yönlendir.");else->listOf("Depodaki trenlerin renklerini", "ve robot sırasını yeniden düzenle.") }
        lines.forEachIndexed { i,s->art.caption(c,s,200f,y+216+i*26,16f) }
        art.label(c,"Kalan: ${economy.inventory(key)}",200f,y+284,20f)
        button(c,61f,y+331,278f,51f,if(economy.inventory(key)>0) "Kullan" else "Reklam İzle · +1 Hak") {
            if(economy.inventory(key)<=0) ads.show { economy.grantAdBooster(key);notice("+1 güçlendirme hazır!") } else useBooster()
        }
    }
}
