package unlekker.mb2.util;

import java.io.File;
import java.lang.Character.Subset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphics3D;
import unlekker.mb2.geo.UGeo;
import unlekker.mb2.geo.UVertex;
import unlekker.mb2.geo.UVertexList;

/**
 * 
 * UMB is the base class for most of the classes in ModelbuilderMk2, meaning that
 * they all extend this class and thus inherit the capabilities it provides. This
 * includes convenient tools for common computational tasks (math, string formatting etc), 
 * as well as a mechanism to set and share a PApplet instance provided by the user.
 * 
 * Call {@see UMB#setPApplet(PApplet)} or {@see UMB#setGraphics(PGraphics)} to provide a PApplet 
 * or PGraphics instance,which can then be used by geometry classes like {@see UGeo} and {@see UVertexList}
 * for rendering etc.
 * 
 * 
 * 
 * @author <a href="https://github.com/mariuswatz">Marius Watz</a>
 *
 */
public class UMB implements UConst {

  /**
   * Options like <code>NODUPL</code>, <code>NOCOPY</code> etc. encoded as bit flags stored as an int. Options can be
   * toggled and checked with <code>enable()</code> and <code>isEnabled()</code>
   */
  public int options;
  
  private static String[] optionNames;
  protected static UMB UMB;
  protected static PApplet papplet=null;
  protected static PGraphics g;
  protected static PGraphics g3d;
  protected static boolean isGraphics3D;
  
  protected static int gErrorCnt=0;
  
  protected static long timerData[]=new long[300],timerTask[];
  protected static String taskName;
  
  public static HashMap<Integer, String> groupTypeNames;

  protected static boolean libraryPrinted=false;
  static {
    if(!libraryPrinted) {
      UMB.logDivider(VERSION);
      UMB.log(CREDIT);
      UMB.logDivider();
      
      libraryPrinted=true;
    }
  }

  
  public static UMB taskTimerStart(String name) {
    taskName=name;
    if(timerTask==null) timerTask=new long[2];
    
    timerTask[0]=System.currentTimeMillis();
    timerTask[1]=timerTask[0];
    return UMB.UMB;
  }

  public static UMB taskTimerUpdate(float perc) {
    long tNow=System.currentTimeMillis();
    long tD=tNow-timerTask[1];      
    
//    log("update "+tD+" "+(System.currentTimeMillis()-timerTask[0]));
    if(tD>1000) {    
      tD=tNow-timerTask[0];
      if(perc<1) perc=perc*100f;
      log(taskName+": "+(int)perc+"% - "+
          nf((float)tD/1000f,1,1)+" sec");
      timerTask[1]=tNow;
    }      
    
    return UMB.UMB;    
  }

  public static UMB taskTimerDone() {
    if(taskName!=null) {
      long tD=System.currentTimeMillis()-timerTask[0];
      if(tD>1000) log(taskName+": Done - "+
          nf((float)tD/1000f,1,1)+" sec");
      taskName=null;
    }

    return UMB.UMB;        
  }

  public static long timerStart(int id) {
    long t=System.currentTimeMillis();
    id*=3;
    timerData[id]=t;
    return t;
  }
  
  public static long timerElapsed(int id) {
    long t=System.currentTimeMillis();
    id*=3;
    
    return t-timerData[id];
  }
  

  public static long timerEnd(int id) {
    long t=System.currentTimeMillis();
    id*=3;
    timerData[id+1]=t;
    timerData[id+2]=t-timerData[id];
    return timerData[id+2];
  }

  public static String version() {
    return VERSION;
  }


  ///////////////////////////////////////////////
  // PGRAPHICS CONVENIENCE METHODS
  
  /**
   * Static method to call PGraphics.translate() with a UVertex instance as
   * input.
   * 
   * @param v
   * @return
   */
  public static UMB ptranslate(UVertex v) {
    return ptranslate(v.x,v.y,v.z);
  }

  /**
   * Static method to call PGraphics.translate() with a UVertex instance as
   * input.
   * 
   * @param v
   * @return
   */
  public static UMB ptranslate(float x,float y,float z) {
    if (checkGraphicsSet()) g.translate(x, y, z);
    else g.translate(x, y);
    return UMB.UMB;
  }

  /**
   * Static method to call PGraphics.translate() with a UVertex instance as
   * input.
   * 
   * @param v
   * @return
   */
  public static UMB ptranslate(float x,float y) {
    if (checkGraphicsSet()) g.translate(x, y);
    return UMB.UMB;
  }

  public static UMB pscale(float m) {return pscale(m,m,m);}

  public static UMB prect(float r,float r2) {
    return prect(-r*0.5f,-r2*0.5f, r, r2);
  }

  public static UMB pcross(UVertex loc,float w) {
    return ppush().ptranslate(loc).pline(-w,0,w,0).pline(0,-w,0,w).ppop();
  }
  
  public static UMB prect(UVertex loc,float r) {
    return prect(loc,r,r);
  }

  public static UMB prect(UVertex loc,float r,float r2) {
    if (checkGraphicsSet()) 
      ppush().ptranslate(loc).prect(0,0, r, r2).ppop();
    return UMB.UMB;
  }

  public static UMB prect(float mx,float my,float r,float r2) {
    if (checkGraphicsSet()) g.rect(mx, my, r, r2);
    return UMB.UMB;
  }

  public static UMB pquad(UVertex[] vv) {
    if (checkGraphicsSet()) {
      g.beginShape(QUADS);
      pvertex(vv);
      g.endShape();
    }
    return UMB.UMB;
  }


  public static UMB pellipse(float r,float r2) {
    return pellipse(0,0, r, r2);
  }

  public static UMB pellipse(UVertex loc,float r) {
    return pellipse(loc, r,r);
  }

  public static UMB pellipse(UVertex loc,float r,float r2) {
    if (checkGraphicsSet()) 
      ppush().ptranslate(loc).pellipse(0,0, r, r2).ppop();
    return UMB.UMB;
  }

  public static UMB pellipse(float mx,float my,float r,float r2) {
    if (checkGraphicsSet()) g.ellipse(mx, my, r, r2);
    return UMB.UMB;
  }

  public static UMB pscale(float mx,float my,float mz) {
    if (checkGraphicsSet()) g.scale(mx,my,mz);
    return UMB.UMB;
  }
  
  public static UMB protX(float deg) {
    if (checkGraphicsSet()) g.rotateX(deg);
    return UMB.UMB;
  }

  public static UMB protY(float deg) {
    if (checkGraphicsSet()) g.rotateY(deg);
    return UMB.UMB;
  }

  public static UMB protZ(float deg) {
    if (checkGraphicsSet()) g.rotateZ(deg);
    return UMB.UMB;
  }

  /**
   * Static convenience method to call PGraphics.line() with two UVertex
   * instances as input.
   */
  public static UMB pline(UVertex v, UVertex v2) {
    if (checkGraphicsSet()) {
      if(isGraphics3D)
        g.line(v.x, v.y, v.z, v2.x, v2.y, v2.z);
      else g.line(v.x, v.y, v2.x, v2.y);
    }
    return UMB.UMB;
  }

  /**
   * Static convenience method to call PGraphics.line() between the origin and
   * a single UVertex instance.  
   */
  public static UMB pline(UVertex v) {
    if (checkGraphicsSet()) {
      if(isGraphics3D)
        g.line(0,0,0, v.x, v.y, v.z);
      else g.line(0,0,v.x, v.y);
    }
    return UMB.UMB;
  }

  public static UMB pline(float x1,float y1,float x2,float y2) {
    if (checkGraphicsSet()) {
      g.line(x1,y1,x2,y2);
    }
    return UMB.UMB;
  }

  /**
   * Static convenience method to call both <code>PGraphics.pushMatrix()</code>
   * <code>PGraphics.pushStyle()</code>
   */
  public static UMB ppush() {
    if (checkGraphicsSet()) {
      g.pushMatrix();
      g.pushStyle();
    }
    return UMB.UMB;
  }

  /**
   * Static convenience method to call both <code>PGraphics.popMatrix()</code>
   * <code>PGraphics.popStyle()</code>
   */
  public static UMB ppop() {
    if (checkGraphicsSet()) {
      g.popStyle();
      g.popMatrix();
    }
    return UMB.UMB;
  }

  /**
   * Static convenience method to call <code>PGraphics.vertex()</code> with a
   * UVertex instance as input
   */
  public static UMB pvertex(UVertex v) {
    if (checkGraphicsSet()) {
      if (isGraphics3D) g3d.vertex(v.x, v.y, v.z);
      else g.vertex(v.x, v.y);
    }
    return UMB.UMB;
  }

  /**
   * Static convenience method to iterate through an array of
   * <code>UVertex</code> and call <code>PGraphics.vertex()</code> for each
   * instance.
   */
  public static UMB pvertex(UVertex varr[]) {
    return pvertex(varr,false);
  }
    

  /**
   * Static convenience method to iterate through an array of
   * <code>UVertex</code> and call <code>PGraphics.vertex()</code> for each
   * instance. Set <code>useUV</code> to <code>true</code> to include the UV
   * coordinates stored in UVertex.
   * 
   *   
   * @param varr
   * @param useUV
   * @return
   */
  public static UMB pvertex(UVertex varr[],boolean useUV) {
    if(checkGraphicsSet()) {
      if(useUV) {
        if (isGraphics3D) {
          for(UVertex vv:varr) g3d.vertex(vv.x, vv.y, vv.z,vv.U,vv.V);
        }
        else {
          for(UVertex vv:varr) g.vertex(vv.x, vv.y, vv.U,vv.V);
        }
      }
      else {
        if (isGraphics3D) {
          for(UVertex vv:varr) g3d.vertex(vv.x, vv.y, vv.z);
        }
        else {
          for(UVertex vv:varr) g.vertex(vv.x, vv.y);
        }
      }
    }
    return UMB.UMB;
  }

  /**
   * Static convenience method to call <code>PGraphics.fill()</code>
   */
  public static UMB pfill(int col) {
    if (checkGraphicsSet()) g.fill(col);
    return UMB.UMB;
  }

  public static UMB pfill(float rr,float gg,float bb) {
    if (checkGraphicsSet()) g.fill(color(rr,gg,bb));
    return UMB.UMB;
  }

  /**
   * Static convenience method to call <code>PGraphics.stroke()</code>
   */
  public static UMB pstroke(int col) {
    if (checkGraphicsSet()) g.stroke(col);
    return UMB.UMB;
  }

  public static UMB pstroke(int col,float strokeWeight) {
    if (checkGraphicsSet()) {
      if(strokeWeight>0) g.strokeWeight(strokeWeight);
      g.stroke(col);
    }
    return UMB.UMB;
  }

  /**
   * Static convenience method to call <code>PGraphics.noFill()</code>
   */
  public static UMB pnoFill() {
    if (checkGraphicsSet()) g.noFill();
    return UMB.UMB;
  }

  /**
   * Static convenience method to call <code>PGraphics.noStroke()</code>
   */
  public static UMB pnoStroke() {
    if (checkGraphicsSet()) g.noStroke();
    return UMB.UMB;
  }

  public static UMB draw(ArrayList<UVertexList> vl) {
    for(UVertexList l:vl) l.draw();
    return UMB.UMB;
  }



  
  ///////////////////////////////////////////////
  // GEOMETRY OPTIONS 

  public UMB setOptions(int opt) {
    options=opt;
//    log(optionStr());
    return this;
  }

  public UMB enable(int opt) {
    options=options|opt;
//    log(optionStr());
    return this;
  }

  public boolean isEnabled(int opt) {
    return (options & opt)==opt;
  }

  public static boolean isEnabled(int theOptions,int opt) {
    return (theOptions & opt)==opt;
  }


  public UMB disable(int opt) {
    options=options  & (~opt);
//    log(optionStr());
    return this;
  }
  
  public String optionStr() {
    if(optionNames==null) {
      optionNames=new String[1000];
      optionNames[COLORVERTEX]="COLORVERTEX";
      optionNames[COLORFACE]="COLORFACE";
      optionNames[NOCOPY]="NOCOPY";
      optionNames[NODUPL]="NODUPL";
    }
    
    StringBuffer buf=new StringBuffer();
    if(isEnabled(NODUPL)) buf.append(optionNames[NODUPL]).append(TAB);
    if(isEnabled(NOCOPY)) buf.append(optionNames[NOCOPY]).append(TAB);
    if(isEnabled(COLORFACE)) buf.append(optionNames[COLORFACE]).append(TAB);
    if(isEnabled(COLORVERTEX)) buf.append(optionNames[COLORVERTEX]).append(TAB);

    if(buf.length()>0) {
      buf.deleteCharAt(buf.length()-1);
      return "Options: "+buf.toString();
    }
    
    return "Options: None";
  }
  
  ///////////////////////////////////////////////
  // COLOR 
  
  public static final int color(int c,float a) {
    return ((int)a<< 24) & c;
  }

  public static final int color(int c) {
    return color(c,c,c);
  }

  public static final int color(float r, float g, float b) {
 //   return 0xff000000 | (v1 << 16) | (v2 << 8) | v3;
    int rr=(int)r,gg=(int)g,bb=(int)b;
    return (0xff000000)|
        ((rr)<<16)|((gg)<<8)|(bb);
  }

  public static final int color(int r, int g, int b, int a) {
    return (0xff000000)|((r&0xff)<<16)|((g&0xff)<<8)|(b&0xff);
  }

  public static String hex(int col) {
    String s="",tmp;
    
    int a=(col >> 24) & 0xff;
    if(a<255) s+=strPad(Integer.toHexString(a),2,ZERO);
    
    s+=strPad(Integer.toHexString((col>>16)&0xff),2,ZERO);
    s+=strPad(Integer.toHexString((col>>8)&0xff),2,ZERO);
    s+=strPad(Integer.toHexString((col)&0xff),2,ZERO);
//    s+=(tmp.length()<2 ? "0"+tmp : tmp);
//    tmp=Integer.toHexString((col>>8)&0xff);
//    s+=(tmp.length()<2 ? "0"+tmp : tmp);
//    tmp=Integer.toHexString((col)&0xff);
//    s+=(tmp.length()<2 ? "0"+tmp : tmp);
    
    s=s.toUpperCase();
    return s;
  }

  public static final int color(String hex) {
    int c=0xFFFF0000,alpha=255;
    
    boolean ok=true;
    
    if(hex==null) ok=false; 
    else for(int i=0; ok && i<hex.length(); i++) {
      char ch=hex.charAt(i);
      if(!(
          Character.isLetter(ch) ||
              Character.isDigit(ch)
              )) ok=false;
    }
    if(!ok) {
      log("toColor('"+hex+"') failed.");
      return c;
    }
    
    try {
      if(hex.length()==8) {
        alpha=Integer.parseInt(hex.substring(0,2),16);
//      UUtil.log("hex: "+hex+" alpha: "+alpha);
        hex=hex.substring(2);
      }
      c=(alpha<<24) | Integer.parseInt(hex, 16);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      c=color(255,0,0);
      e.printStackTrace();
    }
    
    return c;
  }

  //////////////////////////////////////////
  // MATH
  // map,lerp,max,constrain code taken from processing.core.PApplet
  
  
  static public final float abs(float n) {
    return (n < 0) ? -n : n;
  }

  static public final int abs(int n) {
    return (n < 0) ? -n : n;
  }

  static public final float sq(float a) {
    return a*a;
  }

  static public final float sqrt(float a) {
    return (float)Math.sqrt(a);
  }

  static public final int max(int a, int b) {
    return (a > b) ? a : b;
  }

  static public final float max(float a, float b) {
    return (a > b) ? a : b;
  }

  static public final int min(int a, int b) {
    return (a < b) ? a : b;
  }

  static public final float min(float a, float b) {
    return (a < b) ? a : b;
  }

  
  static public final float map(float value,
      float ostart, float ostop) {
        return ostart + (ostop - ostart) * (value);
  }
  
  static public final float map(float value,
      float istart, float istop,
      float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
  }

  static public final int constrain(int amt, int low, int high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }

  static public final float constrain(float amt, float low, float high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }

  static public final float lerp(float start, float stop, float amt) {
    return start + (stop-start) * amt;
  }

  
  // extended versions
  
  static public final float mod(float a, float b) { // code from David Bollinger
    return (a%b+b)%b; 
  }

  static public final float max(ArrayList<Float> val) {
    float theMax=Float.MIN_VALUE;
    
    for(float v:val) theMax=(v>theMax ? v : theMax);
    return theMax;
  }

  static public final float min(ArrayList<Float> val) {
    float theMin=Float.MAX_VALUE;
    
    for(float v:val) theMin=(v>theMin ? v : theMin);
    return theMin;
  }

  static public final float max(float val[]) {
    float theMax=val[0];
    for(float v:val) theMax=(v>theMax ? v : theMax);
    return theMax;
  }

  static public final int max(int val[]) {
    int theMax=val[0];
    for(int v:val) theMax=(v>theMax ? v : theMax);
    return theMax;
  }


  static public final double mapDbl(double value,
      double istart, double istop,
      double ostart, double ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
  }

  public final static float wraplerp(float a, float b, float t, float w) {
    a += (abs(b-a) > w/2f) ? ((a < b) ? w : -w) : 0;
    return lerp(a, b, t);
    }


  //////////////////////////////////////////
  // PARSING VALUES

  public static int parseInt(String s) {
    if(s==null) return Integer.MIN_VALUE;
    return Integer.parseInt(s.trim());
  }

  public static float parseFloat(String s) {
    if(s==null) return Float.NaN;
    return Float.parseFloat(s.trim());
  }

  public static float[] parseFloat(String s[]) {
    if(s==null) return null;
    
    float f[]=new float[s.length];
    int id=0;
    for(String ss:s) f[id++]=parseFloat(ss);
    
    return f;
  }

  
  //////////////////////////////////////////
  // RANDOM NUMBERS

  /**
   * Static copy of unlekker.util.Rnd for easy random number generation.
   */
  public static URnd rnd=new URnd(System.currentTimeMillis());
  
  public static void setRnd(URnd rnd) {
    UMB.rnd=rnd;
    UMB.UMB=new UMB();
  }
  
  public static float sign(float in) {
    return (in<0 ? -1 : 1);
  }
  
  /**
   * Returns <code>true</code> if <code>rnd(100) > prob</code>.
   * @param prob
   * @return
   */
  public boolean rndProb(float prob) {
    return rnd.prob(prob>100 ? 100 : prob);
  }

  public static boolean rndBool() {
    return rnd.bool();
  }

  public static float rnd() {
    return rnd.random(1);
  }

  public static float rnd(float max) {
    return rnd.random(max);
  }

  public static float rndSign() {
    return (rndBool() ? -1 : 1);
  }

  public static float rnd(float min, float max) {
    return rnd.random(min,max);
  }

  public static float rndSigned(float v) {
    return rnd(v)*rndSign();
  }
  
  /**
   * Generates randomly signed integer numbers in the ranges [min..max] and
   * [-max..-min], with equal chances of getting a negative or
   * positve outcome. Avoids the problem
   * of a call like <code>random(-1,1)</code> generating 
   * values close to zero.  
   * 
   * @param min Minimum absolute value
   * @param max Maximum absolute value
   * @return
   */
  public static float rndSigned(float min, float max) {
     float val=rnd.random(min,max);
      return rndBool() ? val : -val;
    }

  public static int rndInt(int max) {
    return rnd.integer(max);
  }

  public static int rndInt(int min, int max) {
    return rnd.integer(min,max);
  }

  /**
   * Generates randomly signed integer numbers in the ranges [min..max] and
   * [-max..-min], with equal chances of getting a negative or
   * positve outcome. Avoids the problem
   * of a call like <code>random(-1,1)</code> generating 
   * values close to zero.  
   * 
   * @param min Minimum absolute value
   * @param max Maximum absolute value
   * @return
   */
  public static int rndIntSigned(float min, float max) {
    int val=rnd.integer(min,max);
     return rndBool() ? val : -val;
   }
  
  
  //////////////////////////////////////////
  // SET + GET PAPPLET AND PGRAPHICS 

  public static void setPApplet(PApplet papplet) {
    setPApplet(papplet,true);
  }

  public static void setPApplet(PApplet papplet,boolean useGraphics) {
    UMB.papplet=papplet;    
    if(useGraphics) setGraphics(papplet);
  }

  public static PApplet  getPApplet() {
    return UMB.papplet;    
  }

  public static boolean checkGraphicsSet() {
    if(g==null) {
      if(gErrorCnt%100==0) logErr("ModelbuilderMk2: No PGraphics set. Use UMB.setGraphics(PApplet).");
      gErrorCnt++;
      return false;
    }
    return true;
  }

  public static boolean checkPAppletSet() {
    if(papplet==null) {
      if(gErrorCnt%100==0) logErr("ModelbuilderMk2: No PApplet set. Use UMB.setPApplet(PApplet).");
      gErrorCnt++;
      return false;
    }
    return true;
  }

  public static PGraphics getGraphics() {
    return g;
  }

  public static void setGraphics(PApplet papplet) {
    setGraphics(papplet.g);
  }

  public static void setGraphics(PGraphics gg) {
    UMB.g=gg;
    if(gg.is3D()) {
      UMB.g3d=(PGraphics3D)gg;
      isGraphics3D=true;
    }
    else isGraphics3D=false;
    
    log("UMB.setGraphics: "+
        g.getClass().getSimpleName()+
        " (is3D="+isGraphics3D+")");
  }

  //////////////////////////////////////////
  // LOGGING
  
  public static void log(String s) {
    System.out.println(timeStr()+" "+s);
  }

  public static void logf(String s,Object... arg) {
    log(String.format(s, arg));
  }

  
  public static <T> void log(T s[]) {
    StringBuffer buf=strBufGet();
    for(T ss:s) {
      if(buf.length()>0) buf.append(COMMA);
      buf.append(ss.toString());
    }
    log("["+strBufDispose(buf)+"]");
  }

  public static <T> void log(ArrayList<T> input) {
//    log("log "+input.getClass().getName()+" "+input.size());
    for(T tmp: input) {
      
      log(tmp.toString());
    }
  }

  public static void log(int i) {
    log(""+i);
  }

  public static void log(float f) {
    log(""+f);
  }

  public static void logErr(String s) {
    System.err.println(s);
  }

  public static void logDivider() {
    log(LOGDIVIDERNEWNL);
  }

  public static void logDivider(String s) {
    log(LOGDIVIDER+' '+s);
  }

  public static String timeStr(long t) {
    int tmp;
    StringBuffer buf=strBufGet();
    
    int hr=(int)(t/HOURMSEC);
    t-=hr*HOURMSEC;
    int m=(int)(t/MINUTEMSEC);
    t-=m*MINUTEMSEC;
    int s=(int)(t/SECONDMSEC);
    
    String str=strf(TIMESTR,hr,m,s);
//      buf.append(nf(hr,2)).append(':').append(nf(m,2)).append(':').append(nf(s,2));
//      return buf.toString();
    return str;
  }

  public static String timeStr() {
    return timeStr2(Calendar.getInstance());//.getTimeInMillis());
  }
  
  public static String timeStr2(Calendar c) {
    int tmp;
    StringBuffer buf=strBufGet();
    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss",Locale.US);

    buf.append(sdf.format(c.getTime()));
    return strBufDispose(buf);
  }

  
  //////////////////////////////////////////
  // FILE TOOLS
  
  public static String nextFile(String path,String pre) {
    return nextFilename(path, pre,null);
  }

  public static String nextFilename(String path,String pre,String ext) {
    return UFile.nextFile(path, pre, ext);
  }
  
  //////////////////////////////////////////
  // NUMBER FORMATTING
  
  private static NumberFormat formatFloat, formatInt;
  private static char numberChar[]=new char[] {'0', '1', '2', '3', '4', '5',
      '6', '7', '8', '9', '-', '.'};

  static public void nfInitFormats() {
    formatFloat=NumberFormat.getInstance();
    formatFloat.setGroupingUsed(false);

    formatInt=NumberFormat.getInstance();
    formatInt.setGroupingUsed(false);
  }

  /**
   * Format floating point number for printing
   * 
   * @param num
   *          Number to format
   * @param lead
   *          Minimum number of leading digits
   * @param decimal
   *          Number of decimal digits to show
   * @return Formatted number string
   */
  static public String nf(float num, int lead, int decimal) {
    if (formatFloat==null) nfInitFormats();
    
    if((num-Math.floor(num))<0.00001) return nf((int)num,lead);
    
    formatFloat.setMinimumIntegerDigits(lead);
    formatFloat.setMaximumFractionDigits(decimal);
    formatFloat.setMinimumFractionDigits(decimal);

    return formatFloat.format(num).replace(",", ".");
  }

  static public String nf(double num, int lead, int decimal) {
    return nf((float)num,lead,decimal);
  }

  /**
   * Format floating point number for printing with maximum 3 decimal points.
   * 
   * @param num
   *          Number to format
   * @return Formatted number string
   */
  static public String nf(float num) {
    return nf(num,0,3);
  }

  static public String nf(float num,int prec) {
    return nf(num,1,prec);
  }

  static public String nf(double num) {
    return nf((float)num);
  }

  /**
   * Format integer number for printing, padding with zeros if number has fewer
   * digits than desired.
   * 
   * @param num
   *          Number to format
   * @param digits
   *          Minimum number of digits to show
   * @return Formatted number string
   */
  static public String nf(int num, int digits) {
    if (formatInt==null) nfInitFormats();
    formatInt.setMinimumIntegerDigits(digits);
    return formatInt.format(num);
  }

  public static String fileSizeStr(File f) {
    long l=f.length();
    String str=null;
    if(l>MB) str=nf((float)l/(float)MB,1,1)+" MB";
    else if(l>KB) str=nf((float)l/(float)KB,1,1)+" KB";
    else str=l+"b";
    return str;
  }
  
  public static String strPad(String s,int len,char c) {
    len-=s.length();
    while(len>0) {
      s+=c;
      len--;
    }
    
    return s;
  }
  
  public static String strf(String format,Object... args) {
    return String.format(format, args);
  }

  public static <T> String str(ArrayList<T> o) {
    return str(o,NEWLN,null);
  }

  public static <T> String str(ArrayList<T> o, char delim,String enclosure) {
    StringBuffer buf=strBufGet();
    if(o==null) buf.append("null");
    else {
      int id=0;
      for(T oo:o) {
        if(buf.length()>0) buf.append(delim); 
//        buf.append(id++).append(' ');
        buf.append(oo.toString());
      }
    }
    
    if(enclosure!=null) {
      buf.insert(0, enclosure.charAt(0));
      buf.append(enclosure.charAt(1));
    }
    
    return strBufDispose(buf);
  }

  public static String str(int[] o) {
    StringBuffer buf=strBufGet();
    if(o==null) buf.append("null");
    else {
      buf.append('[');
      for(int i:o) buf.append(i).append(',');// (buf.length()>1 ? ','+i : ""+i));
      buf.deleteCharAt(buf.length()-1);
      buf.append(']');
    }
    return strBufDispose(buf);
  }

  public static <T> String str(T[] o) {
    return str(o,COMMA,ENCLSQ);
  }

  public static <T> String str(T[] o, char delim,String enclosure) {
    StringBuffer buf=strBufGet();
    if(o==null) buf.append("null");
    else {
      int id=0;
      for(T oo:o) {
        if(buf.length()>0) buf.append(delim); 
//        buf.append(id++).append(' ');
        buf.append(oo==null ? NULLSTR : oo.toString());
      }
    }
    
    if(enclosure!=null) {
      buf.insert(0, enclosure.charAt(0));
      buf.append(enclosure.charAt(1));
    }
    
    return strBufDispose(buf);
  }

  
  //////////////////////////////////////////
  // STRING BUFFER POOL
  
  protected static ArrayList<StringBuffer> strBufFree,strBufBusy;

  protected static String strBufDispose(StringBuffer buf) {
    if(strBufBusy!=null) {
      strBufBusy.remove(buf);
      strBufFree.add(buf);
    }
      
    return buf.toString();
  }

  
  protected static StringBuffer strBufGet() {
    try {
      StringBuffer buf;
      
      if(strBufBusy==null) {
        strBufBusy=new ArrayList<StringBuffer>();
        strBufFree=new ArrayList<StringBuffer>();
      }
      
      if(strBufFree.size()>1) {
        buf=strBufFree.remove(0);
        buf.setLength(0);
      }
      else buf=new StringBuffer();
      
      strBufBusy.add(buf);
      
      return buf;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return new StringBuffer();
  }
  
  //////////////////////////////////////////
  // STRING MANIPULATION
  
  public String strTrim(String s,int newlen) {
    return s.substring(0,newlen);
  }

  public String strStripContainer(String str) {
    String s=str.trim();
    int len=s.length();
    char ch1=s.charAt(0);
    char ch2=s.charAt(len-1);
    
    if((ch1=='[' && ch2==']') ||
        (ch1=='<' && ch2=='>')) {
      s=s.substring(1,len-1);
      return s;
    }
    
    return str;
  }

}

