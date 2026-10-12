package com.aliaygor.trainpop

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
    private val ads=RewardAds(activity) { notice(it) }
    private val store=Store(activity,economy) { notice(it) }
    private val station=BitmapFactory.decodeResource(resources,R.drawable.station)
    private val conductor=BitmapFactory.decodeResource(resources,R.drawable.conductor)
    private val locomotive=BitmapFactory.decodeResource(resources,R.drawable.locomotive)
    private var game=Game.restore(prefs.getString("session","") ?: "")?.takeIf { it.level==economy.level } ?: Game(economy.level)
    private var screen="splash"
    private var modal=""
    private var time=0f
    private var last=0L
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
    private var queueShift=0f
    private var lastBoarded=0
    private var spinAge=-1f
    private var spinIndex=0
    private var spinAngle=0f
    private var spinStart=0f
    private var spinEnd=0f
    private var reward:Reward?=null
    private var rewardReturn=""
    private var selectedBooster=""
    private var music=prefs.getBoolean("music",true)
    private var effects=prefs.getBoolean("effects",true)
    private var vibration=prefs.getBoolean("vibration",true)
    private var motion=prefs.getBoolean("motion",true)
    private data class Hit(val rect:RectF,val id:String,val action:()->Unit)
    private val hits=mutableListOf<Hit>()
    init {
        isFocusable=true;contentDescription="Train Pop"
        economy.finishSpin()?.let { reward=it;rewardReturn="wheel";modal="reward" }
        audio.effects=effects
        art.locomotive=locomotive
    }
    private fun day()=java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(java.util.Date())
    fun pause() { active=false;last=0;audio.music(false);if(screen=="game" && !game.completed) prefs.edit().putString("session",game.checkpoint()).apply() }
    fun resume() { active=true;last=0;audio.music(music);invalidate();store.restore(false) }
    fun release() { audio.release();store.release();station.recycle();conductor.recycle();locomotive.recycle() }
    fun back() {
        if(spinAge>=0) return
        when { modal.startsWith("purchase:") -> modal="shop"; modal=="reward"->modal=rewardReturn;modal=="full" -> Unit; modal.isNotEmpty()->modal=""; screen=="game"->modal="leave";else->activity.finish() }
        invalidate()
    }
    private fun notice(s:String) { activity.runOnUiThread { toast=s;toastUntil=time+3f;invalidate() } }
    private fun open(name:String) { modal=name;shopScroll=0f;pressed=null }
    private fun start(fresh:Boolean=true) { if(fresh || game.level!=economy.level || game.completed) game=Game(economy.level);screen="game";modal=if(game.failed) "full" else "";hintId=-1;lastBoarded=game.boarded;queueShift=0f }
    private fun showReward(r:Reward?) { if(r!=null) { reward=r;rewardReturn=if(modal in setOf("wheel","daily","supply","shop","gold")) modal else "";modal="reward";audio.play("win") } else notice("Bu ödül zaten alındı.") }
    private fun haptic() { if(vibration) performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    private fun hit(x:Float,y:Float,w:Float,h:Float,id:String,action:()->Unit) { hits+=Hit(RectF(x,y,x+w,y+h),id,action) }
    private fun button(c:Canvas,x:Float,y:Float,w:Float,h:Float,s:String,color:Int=0xFF41D62F.toInt(),id:String=s,enabled:Boolean=true,action:()->Unit) {
        val pushed=pressed?.id==id;val dy=if(pushed) 4f else 0f;val col=if(enabled) color else 0xFFA8B1C6.toInt()
        art.box(c,x-3,y+7,w+6,h,0xFFF4F7F8.toInt(),19f)
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
            time+=dt
            if(screen=="splash" && time>1.0f) screen="loading"
            if(screen=="loading" && time>2.1f) screen="home"
            if(spinAge>=0) {
                spinAge+=dt;val t=(spinAge/4f).coerceIn(0f,1f);spinAngle=spinStart+(spinEnd-spinStart)*(1-(1-t).pow(4))
                if(t==1f) { spinAge=-1f;showReward(economy.finishSpin()) }
            }
            if(screen=="game" && modal.isEmpty() && !ads.showing) {
                val prevDepart=game.departing.size
                var remaining=dt
                while(remaining>0f) { val step=min(.02f,remaining);game.update(step);remaining-=step }
                if(game.boarded>lastBoarded) { queueShift+=(game.boarded-lastBoarded)*.25f;lastBoarded=game.boarded;audio.play("board") }
                if(game.departing.size>prevDepart) audio.play("depart")
                queueShift=(queueShift-dt*4f).coerceAtLeast(0f)
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
            if((kind=="starter" && economy.starterOwned) || (kind=="ads" && economy.noAds)) return@forEachIndexed
            val x=if(i<3) 49f else 351f;val y=263f+(i%3)*94f
            art.circle(c,x,y+3,38f,0xFFB1B3DC.toInt());art.circle(c,x,y,38f,Color.WHITE)
            art.circle(c,x,y,32f,if(i<3) 0xFFB37CE9.toInt() else 0xFFCE8AEE.toInt())
            if(kind=="wheel") { val phase=(time%2f/.55f).coerceAtMost(1f);art.wheel(c,x,y,27f,if(motion) phase*360 else 0f) } else art.icon(c,kind,x,y,.9f)
            art.box(c,x-46,y+27,92f,25f,0xFFB8BDDD.toInt(),10f);art.box(c,x-46,y+24,92f,25f,0xFFF9FBFF.toInt(),10f);art.label(c,name,x,y+42,if(name.length>10) 12f else 15f)
            val badge=kind=="daily" && economy.dailyAvailable(day()) || kind=="supply" && economy.supplyStep(day())==0 || kind=="wheel" && economy.spins(day())>0
            if(badge) { art.circle(c,x-30,y-27,11f,0xFFFF752C.toInt());art.label(c,"!",x-30,y-22,17f,Color.WHITE,true) }
            hit(x-47,y-40,94f,94f,kind) { open(kind) }
        }
        val rx=87f+(if(motion) sin(time*.9f)*14 else 0f)
        mascot(c,rx,613f,122f)
        val sayings=listOf("Herkes trene!","Sıradaki durak: eğlence!","Robotlar seni bekliyor!")
        art.box(c,145f,608f,236f,37f,0xEEFFFFFF.toInt(),14f);art.label(c,sayings[(time/6).toInt()%3],263f,632f,14f)
        art.box(c,143f,555f,114f,28f,0xE9184665.toInt(),8f);art.label(c,"BÖLÜM ${economy.level}",200f,575f,16f,Color.WHITE)
        button(c,65f,722f,270f,66f,if(game.moves>0 && game.level==economy.level) "Devam Et" else "Oyna",0xFF52E42E.toInt(),"play") { start(false) }
    }
    private fun mascot(c:Canvas,x:Float,y:Float,w:Float) {
        val bob=if(motion) sin(time*2.5f)*2 else 0f
        c.save();c.rotate(if(motion) sin(time*1.8f)*2 else 0f,x,y+w*.5f)
        art.p.alpha=255;art.p.isFilterBitmap=true
        c.drawBitmap(conductor,null,RectF(x-w/2,y-w*.75f+bob,x+w/2,y+w*.75f+bob),art.p);c.restore()
    }
    private fun yardPosition(t:Train):PointF { val cell=45f;return PointF(65f+(t.x+(if(t.direction.dx!=0) (t.length-1)/2f else 0f))*cell,490f+(t.y+(if(t.direction.dy!=0) (t.length-1)/2f else 0f))*cell) }
    private fun slotX(slot:Int)=82f+slot*43f
    private fun route(t:Train):PointF {
        val start=yardPosition(t);val p=t.progress
        val exit=PointF(if(t.direction.dx<0) -40f else if(t.direction.dx>0) 440f else start.x,if(t.direction.dy<0) 440f else if(t.direction.dy>0) 785f else start.y)
        if(p<.42f) { val q=p/.42f;return PointF(start.x+(exit.x-start.x)*q,start.y+(exit.y-start.y)*q) }
        val q=(p-.42f)/.58f;val target=PointF(slotX(t.slot),378f)
        // A quadratic turn into the station. Position is continuous at the yard boundary.
        val control=PointF(exit.x,335f)
        return PointF((1-q).pow(2)*exit.x+2*(1-q)*q*control.x+q*q*target.x,(1-q).pow(2)*exit.y+2*(1-q)*q*control.y+q*q*target.y)
    }
    private fun board(c:Canvas) {
        art.gradient(c,0f,0f,400f,850f,0xFFE6F6F6.toInt(),0xFFD6DCEA.toInt(),0f)
        iconButton(c,16f,24f,"restart") { open("restart") };iconButton(c,342f,24f,"pause") { open("settings") }
        art.label(c,"Seviye ${game.level}",200f,51f,24f,Color.WHITE,true)
        art.box(c,118f,72f,164f,7f,0xFFB7CDD8.toInt(),5f);art.box(c,118f,72f,164f*game.boarded/game.total,7f,art.colors[4],5f)
        // Incoming lanes and the loop form a station conveyor, with its gate at bottom center.
        val path=Path();path.addOval(RectF(61f,119f,339f,291f),Path.Direction.CW);path.moveTo(61f,91f);path.lineTo(61f,205f);path.moveTo(339f,91f);path.lineTo(339f,205f)
        art.p.style=Paint.Style.STROKE;art.p.strokeWidth=64f;art.p.color=0xFFFAFFFF.toInt();c.drawPath(path,art.p)
        art.p.strokeWidth=56f;art.p.color=0xFF69839D.toInt();c.drawPath(path,art.p)
        art.p.style=Paint.Style.FILL
        art.box(c,159f,305f,82f,13f,0xFFFFD056.toInt(),4f)
        val visible=game.robots.take(104)
        visible.indices.reversed().forEach { i->
            val row=i/4f+queueShift;val lane=i%4
            val pos=queuePosition(row,lane)
            art.robot(c,pos.x,pos.y+(if(motion) sin(time*5+i)*.7f else 0f),art.colors[visible[i]],.55f)
        }
        art.box(c,129f,159f,142f,75f,0xFFF8FFFF.toInt(),16f)
        art.caption(c,"Sıradaki robotlar",200f,179f,12f)
        game.robots.firstOrNull()?.let { art.robot(c,183f,222f,art.colors[it],.95f);art.label(c,"${game.robots.takeWhile { color->color==it }.size}",222f,215f,23f) }
        if(game.level==1 && game.moves==0) { art.caption(c,"Bu renkte bir trene dokun",200f,250f,11f) }
        art.box(c,0f,333f,400f,99f,0xFF6C7A94.toInt(),0f)
        art.label(c,"VIP",27f,359f,12f,art.colors[1]);art.box(c,11f,367f,33f,44f,0xFF526078.toInt(),7f)
        hit(7f,337f,40f,82f,"vipgate") { selectedBooster="vip";open("booster") }
        for(i in 0..6) {
            val x=slotX(i);val unlocked=i<game.platformCount
            art.box(c,x-19,346f,38f,57f,if(unlocked) 0xFFBECEDD.toInt() else 0xFF668C8C.toInt(),7f)
            art.box(c,x-16,349f,32f,51f,0xFF6C7A94.toInt(),5f)
            if(!unlocked) { art.label(c,"+",x,381f,29f,0xFF83E08F.toInt());hit(x-20,344f,40f,65f,"slot$i") { open("slot") } }
            else if(game.platforms[i]==null) art.caption(c,"${i+1}",x,383f,14f,0xFFC9D6E5.toInt())
        }
        art.line(c,0f,434f,400f,434f,Color.WHITE,4f)
        art.caption(c,"Çıkışı açık trenleri doğru sırayla gönder",200f,455f,12f)
        for(i in 0..6) { art.line(c,43f+i*45,469f,43f+i*45,749f,0xFFC6CFDC.toInt(),1f);art.line(c,43f,469f+i*45,358f,469f+i*45,0xFFC6CFDC.toInt(),1f) }
        game.yard.sortedBy { it.y }.forEach { t->
            val pos=yardPosition(t);val shake=if(t.id==shakeId && time<shakeUntil) sin(time*60)*4 else 0f
            if(t.id==hintId) { art.circle(c,pos.x,pos.y,31f+(if(motion) sin(time*4)*3 else 0f),0x77FFFFFF);art.circle(c,pos.x,pos.y,27f,0x66FFDD43) }
            art.train(c,pos.x+shake,pos.y,art.colors[t.color],if(t.length==2) 78f else 37f,27f,t.direction.angle)
            val horizontal=t.direction.dx!=0;val w=if(horizontal && t.length==2) 86f else 41f;val h=if(!horizontal && t.length==2) 86f else 41f
            hit(pos.x-w/2,pos.y-h/2,w,h,"train${t.id}") {
                when(game.dispatch(t.id)) {
                    Result.MOVED->{ hintId=-1;haptic();audio.play("depart") }
                    Result.BLOCKED->{ shakeId=t.id;shakeUntil=time+.35f;audio.play("error");notice("Bu trenin çıkışı kapalı.") }
                    Result.FULL->notice("Peronlar dolu. Robotların binmesini bekle.")
                    Result.BUSY->Unit
                }
            }
        }
        game.platforms.filterNotNull().forEach { t->
            val pos=if(t.phase==TrainPhase.ARRIVING) route(t) else PointF(slotX(t.slot),376f)
            val angle=if(t.phase==TrainPhase.ARRIVING && t.progress<.42f) t.direction.angle else 180f
            art.train(c,pos.x,pos.y,art.colors[t.color],47f,27f,angle,false)
            if(t.phase==TrainPhase.PARKED) { art.box(c,pos.x-16,411f,32f,17f,art.colors[t.color],5f);art.label(c,"${t.remaining}",pos.x,424f,13f,Color.WHITE,true) }
        }
        game.departing.forEach { t->val sx=if(t.slot<0) 26f else slotX(t.slot);art.train(c,sx+(430-sx)*t.progress,376f,art.colors[t.color],47f,27f,90f,false) }
        game.boarding.forEach { b->val q=(b.age/.5f).coerceAtMost(1f);art.robot(c,200f+(slotX(b.slot)-200)*q,310f+(367-310)*q,art.colors[b.color],.55f*(1-q*.4f)) }
        val names=listOf("VIP","Düzenle","Karıştır");val keys=listOf("vip","arrange","shuffle")
        keys.forEachIndexed { i,key->val x=20f+i*126
            button(c,x,781f,108f,45f,names[i],art.colors[0],"boost$key") { selectedBooster=key;open("booster") }
            art.icon(c,key,x+54,765f,.72f);art.circle(c,x+98,763f,11f,0xFFFF7629.toInt());art.label(c,"${economy.inventory(key)}",x+98,767f,12f,Color.WHITE)
        }
    }
    private fun queuePosition(row:Float,lane:Int):PointF {
        if(row<17) { val a=PI/2+row/17*PI*1.85;val rx=139f+(lane-1.5f)*12;val ry=86f+(lane-1.5f)*12;return PointF(200f+cos(a).toFloat()*rx,205f+sin(a).toFloat()*ry) }
        return PointF(20f+lane*13f,185f-(row-17)*15f)
    }
    override fun onTouchEvent(e:MotionEvent):Boolean {
        val x=(e.x-ox)/scale;val y=(e.y-oy)/scale
        when(e.actionMasked) {
            MotionEvent.ACTION_DOWN->{ touchY=y;dragged=false;pressed=hits.lastOrNull { it.rect.contains(x,y) };invalidate() }
            MotionEvent.ACTION_MOVE->{ if(modal=="shop" && abs(y-touchY)>5) { shopScroll=(shopScroll+touchY-y).coerceIn(0f,160f);touchY=y;dragged=true;pressed=null;invalidate() } }
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
            else->when(modal) { "starter"->"Başlangıç Paketi";"ads"->"Reklamsız Paket";"daily"->"Günlük Bonus";"supply"->"Günlük İkmal";"settings"->"Ayarlar";"win"->"Harika!";"full"->"Boş yer yok";"restart"->"Yeniden Başlat";"leave"->"Ana Sayfa";"slot"->"Ek Peron";"booster"->when(selectedBooster) {"vip"->"VIP Ekspres";"arrange"->"Düzenle";else->"Karıştır"};"reward"->"Ödülün hazır!";else->"Train Pop" }
        }
        val tall=modal=="daily" || modal=="supply" || modal=="settings"
        val y=if(tall) 120f else 213f;val h=if(tall) 626f else 420f
        art.panel(c,24f,y,352f,h);art.label(c,title,200f,y+35,if(title.length>17) 21f else 26f,Color.WHITE,true)
        if(modal!="full") button(c,337f,y+5,29f,29f,"×",art.colors[5],"close",enabled=spinAge<0) { modal=if(modal=="reward") rewardReturn else "" }
        when {
            modal=="daily"->daily(c,y)
            modal=="supply"->supply(c,y)
            modal=="settings"->settings(c,y)
            modal=="starter" || modal=="ads" || modal.startsWith("purchase:")->purchase(c,y)
            modal=="reward"->{ reward?.let { rewardArt(c,it,200f,y+145);art.label(c,it.label(),200f,y+227,19f) };button(c,69f,y+315,262f,53f,"Harika!") { modal=rewardReturn } }
            modal=="win"->{
                for(i in 0..19) { val xx=40f+(i*37%320);val yy=y+70f+(time*70+i*23)%180;art.box(c,xx,yy,5f,10f,art.colors[i%6],1f) }
                mascot(c,200f,y+143,92f)
                art.label(c,"Seviye ${game.level} tamamlandı",200f,y+224,20f);art.coin(c,177f,y+259,.8f);art.label(c,"+50",222f,y+267,23f)
                button(c,62f,y+315,276f,55f,"Devam") { start() }
            }
            modal=="restart" || modal=="leave"->{ art.icon(c,"restart",200f,y+139,2f);art.caption(c,if(modal=="restart") "Bu bölümdeki hamleler sıfırlanacak." else "Bölüme sonra devam edebilirsin.",200f,y+223,15f);button(c,49f,y+316,142f,52f,"Hayır",art.colors[5]) { modal="" };button(c,209f,y+316,142f,52f,"Evet") { if(modal=="restart") start() else { prefs.edit().putString("session",game.checkpoint()).apply();screen="home";modal="" } } }
            modal=="full"->{ art.icon(c,"arrange",200f,y+129,1.6f);art.caption(c,"Bekleyen trenleri gönder",200f,y+204,19f);art.caption(c,"ve bir ek peron aç!",200f,y+230,19f);button(c,49f,y+273,142f,47f,"900 altın",art.colors[1]) { if(economy.spend(900) { game.rescue() }) modal="" else notice("Yeterli altın yok.") };button(c,209f,y+273,142f,47f,"Reklam İzle") { ads.show { if(game.rescue()) modal="" } };button(c,69f,y+343,262f,39f,"Yeniden Başlat",art.colors[0]) { start() } }
            modal=="slot"->{ art.icon(c,"supply",200f,y+143,1.8f);art.caption(c,"Bu bölüm için bir ek peron aç.",200f,y+232,16f);button(c,49f,y+310,142f,50f,"150 altın",art.colors[1]) { if(economy.spend(150) { game.addPlatform() }) modal="" else notice("Altın yetersiz veya tüm peronlar açık.") };button(c,209f,y+310,142f,50f,"Reklam İzle") { ads.show { game.addPlatform();modal="" } } }
            modal=="booster"->booster(c,y)
        }
    }
    private fun rewardArt(c:Canvas,r:Reward,x:Float,y:Float) {
        val kind=if(r.coins>0) "coin" else if(r.vip>0) "vip" else if(r.arrange>0) "arrange" else "shuffle"
        art.icon(c,kind,x,y,1.6f)
    }
    private fun daily(c:Canvas,y:Float) {
        val idx=economy.dailyIndex();val available=economy.dailyAvailable(day());val displayIndex=if(available) idx else (idx+6)%7
        economy.dailyRewards.forEachIndexed { i,r->val x=if(i==6) 48f else 48f+(i%3)*104;val yy=y+77+(i/3)*142;val w=if(i==6) 304f else 96f;val h=if(i==6) 109f else 132f
            art.gradient(c,x,yy,w,h,if(i==displayIndex) 0xFFFFD15A.toInt() else 0xFFEFE7CB.toInt(),0xFFFFF9E3.toInt(),12f)
            art.label(c,"Gün ${i+1}",x+w/2,yy+24,17f)
            val claimed=if(available) i<idx else i<=displayIndex
            if(claimed) art.label(c,"✓",x+w/2,yy+79,39f,art.colors[4]) else { art.icon(c,if(r.coins>0) "coin" else if(r.vip>0) "vip" else if(r.arrange>0) "arrange" else "shuffle",x+w/2,yy+61,.78f);art.label(c,if(r.coins>0) "${r.coins}" else "×1",x+w/2,yy+106,18f) }
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
        art.caption(c,"AXIO • Train Pop 0.2",200f,y+555,13f)
        art.caption(c,if(BuildConfig.DEBUG) "Test reklamları etkin" else "",200f,y+581,11f)
    }
    private fun productTitle(id:String)=when(id) { "starter_pack"->"Başlangıç Paketi";"remove_ads"->"Reklamsız";"ad_free_bundle"->"Reklamsız Paket";"coins_500"->"500 Altın";else->"3.000 Altın" }
    private fun productReward(id:String)=when(id) {"starter_pack"->Reward(1000,2,2,2);"ad_free_bundle"->Reward(1500,1,1,1);"coins_500"->Reward(500);"coins_3000"->Reward(3000);else->Reward() }
    private fun purchase(c:Canvas,y:Float) {
        val id=when(modal) {"starter"->"starter_pack";"ads"->"ad_free_bundle";else->modal.substringAfter(':') }
        val r=productReward(id);art.icon(c,if(id.contains("ads") || id=="ad_free_bundle") "ads" else "coin",200f,y+134,1.9f)
        if(id=="remove_ads" || id=="ad_free_bundle") { art.caption(c,"Zorunlu reklamları kalıcı olarak kaldırır.",200f,y+206,14f);art.caption(c,"İsteğe bağlı ödül reklamları devam eder.",200f,y+232,12f) }
        else art.label(c,"${r.coins} altın",200f,y+206,26f)
        if(r.vip>0) { art.box(c,48f,y+247,304f,55f,0xFFE7D5F8.toInt(),12f);listOf("vip","arrange","shuffle").forEachIndexed { i,k->art.icon(c,k,89f+i*104,y+271,.66f);art.label(c,"×${r.vip}",115f+i*104,y+284,15f) } }
        val owned=id=="starter_pack" && economy.starterOwned || (id=="remove_ads" || id=="ad_free_bundle") && economy.noAds
        button(c,60f,y+331,280f,51f,if(owned) "Alındı" else store.price(id),enabled=!owned && store.available(id)) { store.buy(id) }
        art.caption(c,if(store.available(id)) "Google Play ile güvenli satın alma" else "Satın alma yakında açılacak",200f,y+408,10f)
    }
    private fun shop(c:Canvas,gold:Boolean) {
        art.gradient(c,0f,0f,400f,850f,0xFF63AAE9.toInt(),0xFF587AC7.toInt(),0f);header(c,false)
        // Header/settings actions must not leak into this modal.
        hits.clear();art.label(c,if(gold) "Altın" else "Mağaza",200f,118f,35f,Color.WHITE,true)
        button(c,342f,85f,36f,36f,"×",art.colors[5],"shopclose") { modal="" }
        val ids=if(gold) listOf("coins_500","coins_3000") else listOf("remove_ads","ad_free_bundle","starter_pack","coins_500","coins_3000")
        c.save();c.clipRect(12f,147f,388f,767f)
        ids.forEachIndexed { i,id->val yy=154f+i*144-shopScroll;val col=if(id=="starter_pack") art.colors[2] else if(id.contains("ads") || id=="ad_free_bundle") art.colors[3] else art.colors[0]
            art.gradient(c,18f,yy,364f,127f,art.shade(col,1.15f),col,18f);art.icon(c,if(id.contains("ads") || id=="ad_free_bundle") "ads" else "coin",64f,yy+45,1.15f)
            art.label(c,productTitle(id),226f,yy+29,20f,Color.WHITE,true)
            val r=productReward(id);art.caption(c,if(id=="remove_ads") "Zorunlu reklamları kaldır" else r.label(),226f,yy+57,12f,Color.WHITE)
            val owned=id=="starter_pack" && economy.starterOwned || (id=="remove_ads" || id=="ad_free_bundle") && economy.noAds
            val hitBefore=hits.size
            button(c,135f,yy+74,229f,36f,if(owned) "Alındı" else store.price(id),id="buy$id",enabled=!owned) { open("purchase:$id") }
            if(hits.size>hitBefore) { val old=hits.last();if(!old.rect.intersect(RectF(12f,147f,388f,767f))) hits.removeAt(hits.lastIndex) }
        };c.restore()
        button(c,60f,787f,280f,39f,"Reklam İzle · +50 Altın",art.colors[4],"adgold") { ads.show { showReward(economy.grantAdCoins()) } }
    }
    private fun wheelScreen(c:Canvas) {
        art.gradient(c,0f,0f,400f,850f,0xFF8166CB.toInt(),0xFF474985.toInt(),0f)
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
    private fun booster(c:Canvas,y:Float) {
        val key=selectedBooster;art.icon(c,key,200f,y+137,1.8f)
        val lines=when(key) {"vip"->listOf("Sıradaki renkte bir treni", "çıkışı kapalı olsa da gönder.");"arrange"->listOf("Bekleyen bir trenin robotlarını", "kuyruğun önüne getir.");else->listOf("Depodaki trenlerin renklerini", "ve robot sırasını yeniden düzenle.") }
        lines.forEachIndexed { i,s->art.caption(c,s,200f,y+216+i*26,16f) }
        art.label(c,"Kalan: ${economy.inventory(key)}",200f,y+284,20f)
        button(c,61f,y+331,278f,51f,if(economy.inventory(key)>0) "Kullan" else "Mağazaya Git") {
            if(economy.inventory(key)<=0) open("shop") else {
                val applied=economy.consume(key) { when(key) {"vip"->game.vip();"arrange"->game.arrange();else->game.shuffle()} }
                if(applied) { modal="";audio.play("depart");haptic() } else notice("Şu anda kullanılamıyor; hakkın korunuyor.")
            }
        }
    }
}
