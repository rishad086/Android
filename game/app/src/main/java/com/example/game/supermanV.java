package com.example.game;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class supermanV extends View {
    private int points;
    private Bitmap superman[]=new Bitmap[2];
    private int sX=10;
    private int sY;
    private int speed;
    private int lifeSpan;
    private int width,height;
    private Bitmap backgroundImage;
    private Paint score=new Paint();
    private Bitmap life[] = new Bitmap[2];
    private int yX,yY,yS=16;
    private Paint yP=new Paint();
    private int gX,gY,gS=20;
    private Paint gp=new Paint();
    private int rX,rY,rS=25;
    private Paint rp=new Paint();
    private boolean t=false;
    public supermanV(Context context) {

        super(context);
        superman[0]= BitmapFactory.decodeResource(getResources(),R.drawable.c_superman);
        superman[1]= BitmapFactory.decodeResource(getResources(),R.drawable.c_superman_2);
        backgroundImage= BitmapFactory.decodeResource(getResources(),R.drawable.back);
        yP.setColor(Color.YELLOW);
        yP.setAntiAlias(false);
        gp.setColor(Color.GREEN);
        gp.setAntiAlias(false);
        rp.setColor(Color.RED);
        rp.setAntiAlias(false);
        score.setColor(Color.WHITE);
        score.setTextSize(70);
        score.setTypeface(Typeface.DEFAULT_BOLD);
        score.setAntiAlias(true);
        life[0]= BitmapFactory.decodeResource(getResources(),R.drawable.c_heart);
        life[1]= BitmapFactory.decodeResource(getResources(),R.drawable.rocks);
        sY=550;
        points=0;
        lifeSpan=3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width=canvas.getWidth();
        height=canvas.getHeight();
        canvas.drawBitmap(backgroundImage,0,0,null);
        int minY=superman[0].getHeight();
        int maxY=height-superman[0].getHeight()*3;
        sY=sY+speed;
        if(sY<minY)
        {
            sY=minY;
        }
        if(sY>maxY)
        {
            sY=maxY;
        }
        speed=speed+2;
        if(t)
        {
           canvas.drawBitmap(superman[1],sX,sY,null);
           t=false;
        }
        else {
            canvas.drawBitmap(superman[0],sX,sY,null);
        }

        gX=gX-gS;
        if(hitBall(gX,gY))
        {
            points=points+20;
            gX=-100;

        }
        if(gX<0)
        {
            gX=width+21;
            gY=(int) Math.floor(Math.random()*(maxY-minY))+minY;

        }
        canvas.drawCircle(gX,gY,28,gp);


        rX=rX-rS;
        if(hitBall(rX,rY))
        {

            rX=-100;
            lifeSpan=lifeSpan-1;
            if(lifeSpan==0)
            {
                Toast.makeText(getContext(),"SuperMan Finished",Toast.LENGTH_SHORT).show();
                Intent finalActivity=new Intent(getContext(),finalActivity2.class);
                finalActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finalActivity.putExtra("score",points);
                getContext().startActivity(finalActivity);
            }

        }
        if(rX<0)
        {
            rX=width+21;
            rY=(int) Math.floor(Math.random()*(maxY-minY))+minY;

        }
        canvas.drawCircle(rX,rY,35,rp);


        yX=yX-yS;
        if(hitBall(yX,yY))
        {
            points=points+10;
            yX=-100;

        }
        if(yX<0)
        {
            yX=width+21;
            yY=(int) Math.floor(Math.random()*(maxY-minY))+minY;

        }
        canvas.drawCircle(yX,yY,20,yP);

        canvas.drawText("score:"+ points,20,60,score);
        for(int i=0;i<3;i++)
        {
            int x=(int)(580+life[0].getWidth()*1.5*i);
            int y=30;
            if(i<lifeSpan)
            {
                canvas.drawBitmap(life[0],x,y,null);
            }
            else
                {
                    canvas.drawBitmap(life[1],x,y,null);
                }
            }





    }

    public boolean hitBall(int x,int y)
    {
        if(sX<x && x<(sX+superman[0].getWidth()) && sY<y && y<(sY+superman[0].getHeight()))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
        {
            t=true;
            speed=-30;
        }
        return  true;
    }
}
