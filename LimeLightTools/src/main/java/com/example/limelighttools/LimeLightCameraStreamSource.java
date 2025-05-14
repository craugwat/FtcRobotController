package com.example.limelighttools;

import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource;
import android.graphics.Bitmap;


import org.firstinspires.ftc.robotcore.external.function.Continuation;

public class LimeLightCameraStreamSource implements CameraStreamSource {

//    private BufferedImage bmpImage;

    public LimeLightCameraStreamSource() {
        // Load the BMP image
//        File bmpFile = new File(bmpFilePath);
//        this.bmpImage = ImageIO.read(bmpFile);
    }



//    public void getFrameBitmap(Continuation<? extends Consumer<Bitmap>> continuation) {
//        // Convert BufferedImage to Bitmap and pass it to the consumer
//        Bitmap bmpBitmap = convertBufferedImageToBitmap(bmpImage);
//        continuation.dispatch(bmpBitmapConsumer -> bmpBitmapConsumer.accept(bmpBitmap));
//    }

    // Helper function to convert BufferedImage to Bitmap
//    private Bitmap convertBufferedImageToBitmap(BufferedImage bufferedImage) {
//        if (bufferedImage == null) {
//            return null;
//        }
//        int width = bufferedImage.getWidth();
//        int height = bufferedImage.getHeight();
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bufferedImage.getRGB(0, 0, width, height, bitmap.getPixels(), 0, width);
//        return bitmap;
//    }



    public void close() {
        // Perform any necessary cleanup if needed
    }


    @Override
    public void getFrameBitmap(Continuation<? extends org.firstinspires.ftc.robotcore.external.function.Consumer<Bitmap>> continuation) {
        // Convert BufferedImage to Bitmap and pass it to the consumer
        LimeLightImageTools llIt = new LimeLightImageTools("172.29.0.1");
        Bitmap bmpBitmap = llIt.getProcessedBMP();
        continuation.dispatch(bmpBitmapConsumer -> bmpBitmapConsumer.accept(bmpBitmap));

    }
}