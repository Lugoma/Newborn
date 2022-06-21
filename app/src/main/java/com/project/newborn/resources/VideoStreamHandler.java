package com.project.newborn.resources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Base64;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class VideoStreamHandler extends SurfaceView {

    private boolean playing;
    private final VideoStream videoStream;
    private final String address;

    public VideoStreamHandler(Context context, String address) {
        super(context);
        this.address = address;
        this.playing = true;
        videoStream = new VideoStream();
        videoStream.start();
    }

    private class VideoStream extends Thread {

        /**
         * Start streaming
         */
        @Override
        public void run() {

            if(playing) {
                try (ZContext zContext = new ZContext()) {
                    ZMQ.Socket socket = zContext.createSocket(SocketType.SUB);
                    socket.connect(address);
                    socket.subscribe("");
                    while(!Thread.currentThread().isInterrupted() && playing) {
                        byte[] data = socket.recv(0);
                        byte[] decodedData = Base64.decode(data, Base64.NO_WRAP);

                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedData, 0, decodedData.length);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(decodedByte, 1100, 1200, false);
                        draw(scaledBitmap);
                    }
                    socket.disconnect(address);
                }
            }
        }

        /**
         * Draws video on canvas
         * @param decodedByte
         */
        private void draw(Bitmap decodedByte) {
            SurfaceHolder holder = getHolder();
            Canvas canvas = holder.lockCanvas();
            if(canvas != null) {
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(decodedByte, 0, 0, new Paint());
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * Finish all threads and connexion to server
     */
    public void disconnect() {

        if(!videoStream.isInterrupted())
            playing = false;
    }
}
